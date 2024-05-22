package dk.magenta.datafordeler.core;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import dk.magenta.datafordeler.core.command.ScheduleChangedCommandHandler;
import dk.magenta.datafordeler.core.database.*;
import dk.magenta.datafordeler.core.database.SessionManager;
import dk.magenta.datafordeler.core.dump.Dump;
import dk.magenta.datafordeler.core.dump.DumpConfiguration;
import dk.magenta.datafordeler.core.dump.DumpConfiguration.Format;
import dk.magenta.datafordeler.core.exception.DataFordelerException;
import dk.magenta.datafordeler.core.gapi.GapiTestBase;
import dk.magenta.datafordeler.core.plugin.Plugin;
import dk.magenta.datafordeler.core.plugin.TaskListener;
import dk.magenta.datafordeler.core.testutil.Order;
import dk.magenta.datafordeler.core.testutil.OrderedRunner;
import dk.magenta.datafordeler.core.util.OffsetDateTimeAdapter;
import dk.magenta.datafordeler.plugindemo.model.DemoDataRecord;
import dk.magenta.datafordeler.plugindemo.model.DemoEntityRecord;
import org.apache.commons.io.Charsets;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.util.TriConsumer;
import org.hibernate.LazyInitializationException;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.quartz.JobKey;
import org.quartz.ListenerManager;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.impl.StdSchedulerFactory;
import org.quartz.impl.matchers.KeyMatcher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;

import javax.persistence.criteria.CriteriaBuilder;
import javax.transaction.Transactional;
import java.io.IOException;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@RunWith(OrderedRunner.class)
@ContextConfiguration(classes = Application.class)
@PropertySource("classpath:application-test.properties")
@TestPropertySource(
    properties = {
        "spring.jackson.serialization.indent-output=true",
        "dafo.testing=true",
    }
)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class DumpTest extends GapiTestBase {

    private static Logger log = LogManager.getLogger(DumpTest.class.getCanonicalName());

    @Autowired
    private PluginManager pluginManager;

    @Autowired
    private Engine engine;

    @Autowired
    private SessionManager sessionManager;

    @Autowired
    private ObjectMapper objectMapper;

    @LocalServerPort
    private int port;

    private static final OffsetDateTime FROM_TIME =
        OffsetDateTime.parse("2001-01-01T00:00:00+00:00");

    /**
     * Perform some sanity checks before each test.
     */
    @Before
    public void setUp() throws Exception {
        QueryManager.clearCaches();

        List<String> entityNames = engine.pluginManager.getPlugins().stream()
            .flatMap(
                p -> p.getRegisterManager().getEntityManagers().stream()
            ).map(
                em -> String.format("%s (%s)",
                    em.getManagedEntityClass().getCanonicalName(),
                    em.getSchema())
            ).collect(Collectors.toList());

        log.info("{} entities: {}",
            entityNames.size(),
            entityNames.stream().collect(Collectors.joining(", ")));

        Assert.assertNotEquals("At least one entity required", 0,
            entityNames);

        Session session = sessionManager.getSessionFactory().openSession();
        Assert.assertTrue(
            "no pre-existing dumps allowed",
            QueryManager.getAllItems(session, DumpInfo.class).isEmpty()
        );
        Assert.assertTrue(
            "no pre-existing dumps allowed",
            QueryManager.getAllItems(session, DumpData.class).isEmpty()
        );
        session.close();
    }

    @After
    public void tearDown() throws Exception {
        Session session = sessionManager.getSessionFactory().openSession();
        Transaction transaction = session.beginTransaction();

        CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
        for (Class<? extends DatabaseEntry> cls : Arrays.asList(
            DemoDataRecord.class,
            DemoEntityRecord.class,
            Identification.class,
            DumpInfo.class,
            DumpData.class
        )) {
            for (DatabaseEntry entry : QueryManager.getAllItems(session, cls)) {
                session.delete(entry);
            }
        }
        transaction.commit();
        session.close();

        setUp();
    }

    private void createOneEntity(int postalcode, String cityname)
        throws DataFordelerException {

        Session session = sessionManager.getSessionFactory().openSession();
        Transaction transaction = session.beginTransaction();


        DemoEntityRecord entity = new DemoEntityRecord(
            new UUID(0, Integer.parseInt(Integer.toString(postalcode), 16)),
            "http://example.com"
        );
        entity.setPostnr(postalcode);

        DemoDataRecord data = new DemoDataRecord(cityname);
        data.setBitemporality(FROM_TIME, null, FROM_TIME, null);
        entity.addBitemporalRecord(data, session);

        session.saveOrUpdate(entity);
        transaction.commit();
        session.close();
    }

    private void createOneEntity() throws DataFordelerException {
        createOneEntity(3900, "Nuuk");
    }

    /**
     * Test scheduling.
     */
    @Test
    public void schedule() throws SchedulerException, InterruptedException {
        Scheduler scheduler = StdSchedulerFactory.getDefaultScheduler();
        ListenerManager listenerManager = scheduler.getListenerManager();
        TaskListener taskListener = new TaskListener("DumpTest.schedule");

        // A schedule to fire every second
        // Because we're down to 'every second', it will also fire immediately
        DumpConfiguration config = new DumpConfiguration(
            null,
            null,
            null,
            null,
            "* * * * * *",
            null,
            null);
        listenerManager.addJobListener(
            taskListener,
            KeyMatcher.keyEquals(new JobKey("DUMP-" + config.getId()))
        );

        this.waitToMilliseconds(500, 50);

        engine.setupDumpSchedule(
            config,
            true);

        Thread.sleep(1000);
        // One second has passed, should now have executed exactly twice (initial + 1 second)
        Assert.assertEquals(2,
            taskListener.size(TaskListener.Event.jobToBeExecuted));
        Assert.assertEquals(2,
            taskListener.size(TaskListener.Event.jobWasExecuted));

        Thread.sleep(2000);
        // Three seconds have passed, should now have executed exactly four times (initial plus 3 seconds)
        Assert.assertEquals(4, taskListener.size(TaskListener.Event
            .jobToBeExecuted));
        Assert.assertEquals(4, taskListener.size(TaskListener.Event
            .jobWasExecuted));

        this.waitToMilliseconds(500, 50);

        taskListener.reset();

        config.setSchedule(null);
        engine.setupDumpSchedule(config, true);
        Thread.sleep(500);
        // Should not run any further
        Assert.assertEquals(0,
            taskListener.size(TaskListener.Event.jobToBeExecuted));
        Assert.assertEquals(0,
            taskListener.size(TaskListener.Event.jobWasExecuted));
    }

    /**
     * Test empty dumps.
     */
    @Test
    public void emptyDump() throws Exception {
        Plugin plugin = pluginManager.getPluginForSchema("Postnummer");

        {
            Session session = sessionManager.getSessionFactory().openSession();

            Assert.assertEquals("Initial state; no dumps", 0,
                QueryManager.getAllItems(session, DumpInfo.class).size());
            session.close();
        }

        List<DumpConfiguration> configs = Arrays
            .stream(Format.values()).map(f ->
                new DumpConfiguration(
                    "duuump-" + f.name(),
                    "/demo/postnummer/1/rest/search?postnr=*",
                    f,
                    Charsets.UTF_8,
                    "* * * * * *",
                    "Testfætter Hestesens filhåndteringsudtræksafprøvning",
                    null
                )).collect(Collectors.toList());

        // first dump
        for (DumpConfiguration config : configs) {
            new Dump(this.engine, sessionManager, config).run();
        }

        Session session = sessionManager.getSessionFactory().openSession();

        List<DumpInfo> dumps =
            QueryManager.getAllItems(session, DumpInfo.class);

        List<DumpData> dumpDatas =
            QueryManager.getAllItems(session, DumpData.class);

        TriConsumer<String, List<DumpConfiguration>, List<DumpInfo>> check =
            (message, configList, dumpList) -> Assert.assertEquals(message,
                configList.stream()
                    .map(DumpConfiguration::getFormat)
                    .collect(Collectors.toList()),
                dumpList.stream()
                    .map(DumpInfo::getFormat)
                    .collect(Collectors.toList()));

        check.accept("After one run", configs, dumps);

        Assert.assertArrayEquals("Dump contents",
            new String[]{
                unifyNewlines("<Envelope>\n"
                    + "  <path></path>\n"
                    + "  <terms>https://doc.test.data.gl/terms</terms>\n"
                    + "  <requestTimestamp/>\n"
                    + "  <responseTimestamp/>\n"
                    + "  <username>[DUMP]@[INTERNAL]</username>\n"
                    + "  <page>1</page>\n"
                    + "  <pageSize>10</pageSize>\n"
                    + "  <results/>\n"
                    + "</Envelope>\n"),
                unifyNewlines("{\n"
                    + "  \"path\" : \"\",\n"
                    + "  \"terms\" : \"https://doc.test.data.gl/terms\",\n"
                    + "  \"requestTimestamp\" : null,\n"
                    + "  \"responseTimestamp\" : null,\n"
                    + "  \"username\" : \"[DUMP]@[INTERNAL]\",\n"
                    + "  \"page\" : 1,\n"
                    + "  \"pageSize\" : 10,\n"
                    + "  \"results\" : [ ]\n"
                    + "}"),
                "",
            },
            dumps.stream().map(DumpInfo::getStringData).map(DumpTest::unifyNewlines).toArray());

        session.close();

        // second dump
        for (DumpConfiguration config : configs) {
            new Dump(this.engine, sessionManager, config).run();
        }

        session = sessionManager.getSessionFactory().openSession();

        dumps = QueryManager.getAllItems(session, DumpInfo.class);

        dumpDatas = QueryManager.getAllItems(session, DumpData.class);

        check.accept("After two runs", configs, dumps);

        session.close();
    }

    /**
     * Test dumping actual entries.
     */
    @Test
    public void actualDump() throws Exception {
        Session session;
        createOneEntity(3900, "Nuuk");
        createOneEntity(3992, "Siriuspatruljen");

        session = sessionManager.getSessionFactory().openSession();

        Assert.assertEquals("Initial state; no dumps", 0,
            QueryManager.getAllItems(session, DumpInfo.class).size());

        session.close();

        List<DumpConfiguration> configs = Arrays
            .stream(Format.values()).map(f ->
                new DumpConfiguration(
                    "duuump-" + f.name(),
                    "/demo/postnummer/1/rest/search?postnr=*&fmt=rvd",
                    f,
                    Charsets.UTF_8,
                    "* * * * * *",
                    "Testfætter Hestesens filhåndteringsudtræksafprøvning",
                    null
                )).collect(Collectors.toList());

        // first dump
        for (DumpConfiguration config : configs) {
            new Dump(this.engine, sessionManager, config).run();
        }

        session = sessionManager.getSessionFactory().openSession();

        List<DumpInfo> dumps =
            QueryManager.getAllItems(session, DumpInfo.class);

        Assert.assertEquals("After one run",
            configs.size(), dumps.size());

        OffsetDateTimeAdapter adapter = new OffsetDateTimeAdapter();
        OffsetDateTime localFrom = FROM_TIME.withOffsetSameInstant(
            ZoneOffset.systemDefault().getRules()
                .getOffset(Instant.from(FROM_TIME))
        );

        final String localString = adapter.marshal(localFrom);

        Assert.assertArrayEquals("Dump contents",
            Arrays.stream(DumpConfiguration.Format.values()).map(
                s -> {
                    try {
                        return getPayload("/dump." + s.name());
                    } catch (IOException e) {
                        e.printStackTrace();
                        return null;
                    }
                }
            ).map(DumpTest::unifyNewlines).toArray(),
            dumps.stream().map(DumpInfo::getStringData).map(
                s -> s
                    .replace(localString, "XXX")
                    .replace("\"XXX\"", "XXX")
                    .strip()
            ).map(DumpTest::unifyNewlines).toArray());

        session.close();

        // second dump
        for (DumpConfiguration config : configs) {
            new Dump(this.engine, sessionManager, config).run();
        }

        session = sessionManager.getSessionFactory().openSession();

        Assert.assertEquals(
            configs.size(),
            QueryManager.getAllItems(session, DumpData.class).size()
        );

        session.close();
    }

    /**
     * The actual dump data might be rather large, so check that we don't load
     * it willy-nilly.
     */
    @Test
    @Transactional
    public void lazyLoad() throws Exception {
        createOneEntity();

        Session session = sessionManager.getSessionFactory().openSession();

        new Dump(this.engine, sessionManager, new DumpConfiguration(
            "duuump-whatever",
            "/demo/postnummer/1/rest/search?postnr=*",
            DumpConfiguration.Format.csv,
            Charsets.UTF_8,
            "* * * * * *",
            "Testfætter Hestesens filhåndteringsudtræksafprøvning",
            null
        )).run();

        List<DumpInfo> dumps =
            QueryManager.getAllItems(session, DumpInfo.class);

        session.close();

        Assert.assertEquals("After one run", 1, dumps.size());

        for (DumpInfo dump : dumps) {
            try {
                dump.getData();
                Assert.fail("should have failed data access");
            } catch (LazyInitializationException exc) {
                log.info("yes, this is exactly right", exc);
            }
        }
    }

    /**
     * Simple sanity test of the index.
     */
    @Test
    @Order(order = 4)
    @Transactional
    public void index() throws Exception {
        createOneEntity();

        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", "application/json");
        HttpEntity<String> httpEntity = new HttpEntity<String>("", headers);
        ResponseEntity<String> resp = this.restTemplate
            .exchange("/", HttpMethod.GET, httpEntity, String.class);
        JsonNode json = objectMapper.readTree(resp.getBody());

        Assert.assertNotNull(json);
    }

    private static Pattern newlinePattern = Pattern.compile("\\R");

    private static String unifyNewlines(String input) {
        return newlinePattern.matcher(input).replaceAll("\r\n");
    }

    @Test
    @Order(order = 5)
    public void testScheduleParse() throws IOException {
        ScheduleChangedCommandHandler.ScheduleChangedCommandData data;
        data = objectMapper.readerFor(ScheduleChangedCommandHandler.ScheduleChangedCommandData.class).readValue(
                "{\"table\":\"dummy\", \"id\": \"somestring\", \"fields\":[\"field1\"]}"
        );
        Assert.assertEquals("dummy", data.table);
        Assert.assertEquals("somestring", data.id);
        Assert.assertEquals(1, data.fields.size());
        Assert.assertEquals("field1", data.fields.get(0));

        data = objectMapper.readerFor(ScheduleChangedCommandHandler.ScheduleChangedCommandData.class).readValue(
                "{\"table\":\"dummy\", \"id\": 3, \"fields\":[\"field1\", \"field2\"]}"
        );
        Assert.assertEquals("dummy", data.table);
        Assert.assertEquals("3", data.id);
        Assert.assertEquals(2, data.fields.size());
        Assert.assertEquals("field1", data.fields.get(0));
        Assert.assertEquals("field2", data.fields.get(1));
    }

}
