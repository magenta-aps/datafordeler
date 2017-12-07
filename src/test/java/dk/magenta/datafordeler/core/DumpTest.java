package dk.magenta.datafordeler.core;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import dk.magenta.datafordeler.core.database.*;
import dk.magenta.datafordeler.core.dump.Dump;
import dk.magenta.datafordeler.core.dump.DumpConfiguration;
import dk.magenta.datafordeler.core.dump.DumpConfiguration.Format;
import dk.magenta.datafordeler.core.exception.DataFordelerException;
import dk.magenta.datafordeler.core.gapi.GapiTestBase;
import dk.magenta.datafordeler.core.plugin.Plugin;
import dk.magenta.datafordeler.core.plugin.TaskListener;
import dk.magenta.datafordeler.core.testutil.Order;
import dk.magenta.datafordeler.core.testutil.OrderedRunner;
import dk.magenta.datafordeler.plugindemo.model.DemoData;
import dk.magenta.datafordeler.plugindemo.model.DemoEffect;
import dk.magenta.datafordeler.plugindemo.model.DemoEntity;
import dk.magenta.datafordeler.plugindemo.model.DemoRegistration;
import org.apache.commons.io.Charsets;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
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
import org.springframework.boot.context.embedded.LocalServerPort;
import org.springframework.boot.test.context.SpringBootTest;
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
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Created by lars on 03-04-17.
 */
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

    private Logger log = LogManager.getLogger(this.getClass().getName());

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
            DemoRegistration.class,
            DemoEffect.class,
            DemoData.class,
            DemoEntity.class,
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
        final OffsetDateTime from =
            OffsetDateTime.parse("2001-01-01T00:00:00+00:00");
        final OffsetDateTime split =
            OffsetDateTime.parse("2011-01-01T00:00:00+00:00");

        DemoEntity entity = new DemoEntity(
            new UUID(0, Integer.parseInt(Integer.toString(postalcode), 16)),
            "http://example.com"
        );
        DemoRegistration registration = new DemoRegistration(from, null, 0);
        entity.addRegistration(registration);

        DemoEffect effect1 = new DemoEffect(registration, from, null);
        effect1.setDataItems(Arrays.asList(
            new DemoData(postalcode, cityname)
        ));
        registration.addEffect(effect1);

        Session session = sessionManager.getSessionFactory().openSession();
        Transaction transaction = session.beginTransaction();
        QueryManager.saveRegistration(session, entity, registration);
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
                    "/demo/postnummer/1/rest/search",
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

        Assert.assertEquals("After one run",
            configs.size(), dumps.size());

        Assert.assertEquals("After one run",
            configs.size(), dumpDatas.size());

        Assert.assertArrayEquals("Dump contents",
            new String[]{
                "<Envelope>\n"
                    + "  <path></path>\n"
                    + "  <terms>https://doc.test.data.gl/terms</terms>\n"
                    + "  <requestTimestamp/>\n"
                    + "  <responseTimestamp/>\n"
                    + "  <username>[DUMP]@[INTERNAL]</username>\n"
                    + "  <page>1</page>\n"
                    + "  <pageSize>10</pageSize>\n"
                    + "  <results/>\n"
                    + "</Envelope>\n",
                "{\n"
                    + "  \"path\" : \"\",\n"
                    + "  \"terms\" : \"https://doc.test.data.gl/terms\",\n"
                    + "  \"requestTimestamp\" : null,\n"
                    + "  \"responseTimestamp\" : null,\n"
                    + "  \"username\" : \"[DUMP]@[INTERNAL]\",\n"
                    + "  \"page\" : 1,\n"
                    + "  \"pageSize\" : 10,\n"
                    + "  \"results\" : [ ]\n"
                    + "}",
                "",
                "",
            },
            dumps.stream().map(DumpInfo::getStringData).toArray());

        session.close();

        // second dump
        for (DumpConfiguration config : configs) {
            new Dump(this.engine, sessionManager, config).run();
        }

        session = sessionManager.getSessionFactory().openSession();

        dumps = QueryManager.getAllItems(session, DumpInfo.class);

        Assert.assertEquals("After two runs",
            configs.size(), dumps.size());

        dumpDatas = QueryManager.getAllItems(session, DumpData.class);

        Assert.assertEquals("After two runs",
            configs.size(), dumpDatas.size());

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
                    "/demo/postnummer/1/rest/search",
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
            ).toArray(),
            dumps.stream().map(DumpInfo::getStringData).toArray());

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
            "/demo/postnummer/1/rest/search",
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

}