package dk.magenta.datafordeler.core.fapi;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import dk.magenta.datafordeler.core.Application;
import dk.magenta.datafordeler.core.PluginManager;
import dk.magenta.datafordeler.core.database.QueryManager;
import dk.magenta.datafordeler.core.database.SessionManager;
import dk.magenta.datafordeler.core.exception.DataFordelerException;
import dk.magenta.datafordeler.core.exception.InvalidClientInputException;
import dk.magenta.datafordeler.core.plugin.Plugin;
import dk.magenta.datafordeler.core.testutil.Order;
import dk.magenta.datafordeler.core.testutil.OrderedRunner;
import dk.magenta.datafordeler.core.testutil.TestUserDetails;
import dk.magenta.datafordeler.core.user.DafoUserManager;
import dk.magenta.datafordeler.core.user.UserProfile;
import dk.magenta.datafordeler.plugindemo.DemoPlugin;
import dk.magenta.datafordeler.plugindemo.DemoRolesDefinition;
import dk.magenta.datafordeler.plugindemo.model.DemoDataRecord;
import dk.magenta.datafordeler.plugindemo.model.DemoEntityRecord;
import org.apache.commons.io.Charsets;
import org.apache.commons.io.IOUtils;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.mockito.Mockito.when;

@RunWith(OrderedRunner.class)
@ContextConfiguration(classes = Application.class)
@TestPropertySource(
        properties = {
                "dafo.testing=true",
        }
)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class FapiTest {

    @SpyBean
    private DafoUserManager dafoUserManager;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private PluginManager pluginManager;

    @Autowired
    private SessionManager sessionManager;

    @Autowired
    private ObjectMapper objectMapper;

    private static String veryEarly = "1800-01-01T00:00:00Z";
    private static String veryLate = "2200-12-31T23:59:59Z";

    private static ZoneId systemZone = ZoneOffset.systemDefault();

    @Test
    @Order(order = 1)
    public void findDemoPluginTest() {
        String testSchema = DemoEntityRecord.schema;
        Plugin foundPlugin = this.pluginManager.getPluginForSchema(testSchema);
        Assert.assertEquals(DemoPlugin.class, foundPlugin.getClass());
    }

    @Test
    @Order(order = 3)
    public void restExistsTest() throws IOException {
        this.setupUser();
        HttpEntity<String> httpEntity = new HttpEntity<String>("", new HttpHeaders());
        ResponseEntity<String> resp = this.restTemplate.exchange("/demo/postnummer/1/rest/search", HttpMethod.GET, httpEntity, String.class);
        Assert.assertEquals(400, resp.getStatusCode().value());
    }



    @Test
    @Order(order = 5)
    public void restFailOnInvalidUUIDTest() throws IOException {
        this.setupUser();
        HttpEntity<String> httpEntity = new HttpEntity<String>("", new HttpHeaders());
        ResponseEntity<String> resp = this.restTemplate.exchange("/demo/postnummer/1/rest/invalid-uuid", HttpMethod.GET, httpEntity, String.class);
        Assert.assertEquals(400, resp.getStatusCode().value());
    }

    @Test
    @Order(order = 6)
    public void restFailOnInvalidDateTest() throws IOException {
        this.setupUser();
        HttpEntity<String> httpEntity = new HttpEntity<String>("", new HttpHeaders());
        ResponseEntity<String> resp = this.restTemplate.exchange("/demo/postnummer/1/rest/search?postnr=8000&registrationFromBefore=2000-02-31", HttpMethod.GET, httpEntity, String.class);
        Assert.assertEquals(400, resp.getStatusCode().value());
    }

    @Test
    @Order(order = 8)
    public void restLookupJSONByUUIDTest() throws IOException, DataFordelerException {
        this.setupUser();
        UUID uuid = this.addTestObject();
        try {

            HttpHeaders headers = new HttpHeaders();
            headers.set("Accept", "application/json");
            HttpEntity<String> httpEntity = new HttpEntity<String>("", headers);


            ResponseEntity<String> resp = this.restTemplate.exchange(
                    "/demo/postnummer/1/rest/search?postnr=*",
                    HttpMethod.GET, httpEntity, String.class
            );
            Assert.assertEquals(200, resp.getStatusCode().value());
            JsonNode jsonBody = objectMapper.readTree(resp.getBody());


            System.out.println("jsonBody: "+objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(jsonBody));


/*
            ResponseEntity<String> resp = this.restTemplate.exchange(
                    "/demo/postnummer/1/rest/" + uuid.toString()+"?registreringFra="+veryEarly,
                    HttpMethod.GET, httpEntity, String.class
            );
            Assert.assertEquals(200, resp.getStatusCode().value());
            JsonNode jsonBody = objectMapper.readTree(resp.getBody());



            Assert.assertNotNull(jsonBody);

            JsonNode firstResult = jsonBody.get("results").get(0);
            System.out.println(firstResult);
            Assert.assertEquals(uuid.toString(), firstResult.findValue("uuid").asText());
            JsonNode registrations = jsonBody.get("results").get(0).get("registreringer");

            System.out.println("registrations: " + registrations);

            Assert.assertTrue(registrations.isArray());
            Assert.assertEquals(2, registrations.size());

            JsonNode registration1 = registrations.get(0);
            Assert.assertNotNull(registration1);

            Assert.assertTrue(OffsetDateTime.parse("2017-02-21T16:02:50+01:00").isEqual(OffsetDateTime.parse(registration1.get("registreringFra").asText())));

            JsonNode registration2 = registrations.get(1);
            Assert.assertNotNull(registration2);
            Assert.assertTrue(OffsetDateTime.parse("2017-05-01T16:06:22+02:00").isEqual(OffsetDateTime.parse(registration2.get("registreringFra").asText())));
            Assert.assertTrue(registration2.get("registreringTil").isNull());

            // Restrict on registrationFromBefore
            this.testRegistrationFilter("/demo/postnummer/1/rest/" + uuid, new int[][]{{2}}, "2017-06-01T00:00:00+00:00", veryLate, veryEarly, veryLate);
            this.testRegistrationFilter("/demo/postnummer/1/rest/" + uuid, new int[][]{{2, 2}}, "2017-05-01T15:06:22+01:00", veryLate, veryEarly, veryLate);
            this.testRegistrationFilter("/demo/postnummer/1/rest/" + uuid, new int[][]{{2, 2}}, "2017-05-01T15:06:21+01:00", veryLate, veryEarly, veryLate);
            this.testRegistrationFilter("/demo/postnummer/1/rest/" + uuid, new int[][]{{2, 2}}, "2017-05-01T15:06:21+01:00", "2017-05-01T15:06:23+01:00", veryEarly, veryLate);
            this.testRegistrationFilter("/demo/postnummer/1/rest/" + uuid, new int[][]{{2}}, "2017-05-15T00:00:00+01:00", "2017-06-01T00:00:00+01:00", veryEarly, veryLate);
            this.testRegistrationFilter("/demo/postnummer/1/rest/" + uuid, new int[][]{{2, 2}}, "2017-05-01T15:06:21+01:00", veryLate, "2016-06-01T00:00:00+01:00", "2019-06-01T00:00:00+01:00");
            this.testRegistrationFilter("/demo/postnummer/1/rest/" + uuid, new int[][]{{1, 1}}, "2017-05-01T15:06:21+01:00", veryLate, "2017-06-01T00:00:00+01:00", "2017-07-01T00:00:00+01:00");
            this.testRegistrationFilter("/demo/postnummer/1/rest/" + uuid, new int[][]{{}}, "2017-05-01T15:06:21+01:00", veryLate, "2014-06-01T00:00:00+01:00", "2015-06-01T00:00:00+01:00");
            this.testRegistrationFilter("/demo/postnummer/1/rest/" + uuid, new int[][]{{2}}, veryEarly, "2017-04-01T15:06:21+01:00", "2016-06-01T00:00:00+01:00", "2019-06-01T00:00:00+01:00");
            this.testRegistrationFilter("/demo/postnummer/1/rest/" + uuid, new int[][]{{1}}, veryEarly, "2017-04-01T15:06:21+01:00", "2016-06-01T00:00:00+01:00", "2017-06-01T00:00:00+01:00");

            // Use current timestamp
            String now = OffsetDateTime.now().toString();
            this.testRegistrationFilter("/demo/postnummer/1/rest/" + uuid, new int[][]{{1}}, now, now, now, now);
*/
        } finally {
            this.removeTestObject(uuid);
        }
    }

    @Test
    @Order(order = 13)
    public void restLookupCSVByUUIDTest() throws IOException,
            DataFordelerException {
        this.setupUser();
        UUID uuid = this.addTestObject();
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Accept", "text/csv");
            HttpEntity<String> httpEntity = new HttpEntity<String>("", headers);
            ResponseEntity<String> resp = this.restTemplate
                    .exchange("/demo/postnummer/1/rest/" + uuid.toString()
                                    + "?registrationFromBefore=ALWAYS&"
                                    + "registrationToAfter=ALWAYS&"
                                    + "effectFromBefore=ALWAYS&"
                                    + "effectToAfter=ALWAYS&",
                            HttpMethod.GET, httpEntity, String.class);
            Assert.assertEquals(200, resp.getStatusCode().value());
            Assert.assertEquals(new MediaType("text", "csv"),
                    resp.getHeaders().getContentType());
            String expected = unifyNewlines(
                    updateTimestamps(
                            getResourceAsString("/rest-get-1.csv")
                    )
            );
            String actual = unifyNewlines(
                    resp.getBody().replaceAll(uuid.toString(), "UUID")
            );
            actual = sortCsvColumns(actual, getCsvHeaders(expected, ","), ",");
            Assert.assertEquals(expected, actual);

        } finally {
            this.removeTestObject(uuid);
        }
    }

    @Test
    @Order(order = 13)
    public void restLookupTSVByUUIDTest() throws IOException,
            DataFordelerException {
        this.setupUser();
        UUID uuid = this.addTestObject();
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Accept", "text/tsv");
            HttpEntity<String> httpEntity = new HttpEntity<>("", headers);
            ResponseEntity<String> resp = this.restTemplate.exchange(
                    "/demo/postnummer/1/rest/" + uuid.toString()
                    + "?registrationFromBefore=ALWAYS&"
                    + "registrationToAfter=ALWAYS&"
                    + "effectFromBefore=ALWAYS&"
                    + "effectToAfter=ALWAYS&",
                    HttpMethod.GET, httpEntity, String.class
            );
            Assert.assertEquals(200, resp.getStatusCode().value());
            String expected = unifyNewlines(
                    updateTimestamps(
                            getResourceAsString("/rest-get-1.csv")
                    ).replaceAll(",", "\t")
            );
            String actual = unifyNewlines(
                    resp.getBody().replaceAll(uuid.toString(), "UUID")
            );
            actual = sortCsvColumns(actual, getCsvHeaders(expected, "\t"), "\t");
            Assert.assertEquals(expected, actual);

        } finally {
            this.removeTestObject(uuid);
        }
    }

    @Test
    @Order(order = 10)
    public void restLookupJSONByParametersTest() throws IOException, DataFordelerException, URISyntaxException {
        this.setupUser();
        UUID uuid1 = this.addTestObject();
        UUID uuid2 = this.addTestObject();
        try {
            this.testRegistrationFilter(
                    "/demo/postnummer/1/rest/search?postnr=8000",
                    new int[][]{{1}, {1}}, // Expect 2 entities (length of outer array), each with 1 registration (length of sub-arrays), each with 1 effect (contents of sub-arrays)
                    null, null, null, null // Null values mean "now"
            );

            this.testRegistrationFilter("/demo/postnummer/1/rest/search?postnr=8000",
                    new int[][]{{2, 2}, {2, 2}}, // Expect 2 entities (length of outer array), each with 2 registrations (length of sub-arrays), each with 2 effects (contents of sub-arrays)
                    veryEarly, veryLate, veryEarly, veryLate // Large timespans that encapsulate all registrations and effects
            );

            this.testRegistrationFilter("/demo/postnummer/1/rest/search?postnr=8000&page=1&pageSize=1",
                    new int[][]{{1}}, // Expect one entity (because we set pageSize=1), with one registration, with one effect
                    null, null, null, null // Null values mean "now"
            );
            this.testRegistrationFilter("/demo/postnummer/1/rest/search?postnr=8000&page=1&pageSize=1",
                    new int[][]{{2, 2}},// Expect one entity, with 2 registrations, each with 2 effects
                    veryEarly, veryLate, veryEarly, veryLate
            );

            this.testRegistrationFilter("/demo/postnummer/1/rest/search?postnr=8000&page=1&pageSize=2",
                    new int[][]{{1}, {1}}, // Expect two entities, each with one registration, with one effect
                    null, null, null, null // Null values mean "now"
            );
            this.testRegistrationFilter("/demo/postnummer/1/rest/search?postnr=8000&page=1&pageSize=2",
                    new int[][]{{2, 2}, {2, 2}},// Expect two entities, each with 2 registrations, each with 2 effects
                    veryEarly, veryLate, veryEarly, veryLate
            );

            this.testRegistrationFilter("/demo/postnummer/1/rest/search?postnr=8000&page=2&pageSize=1",
                    new int[][]{{1}}, // Expect one entity (because we set pageSize=1), with one registration, with one effect
                    null, null, null, null
            );
            this.testRegistrationFilter("/demo/postnummer/1/rest/search?postnr=8000&page=2&pageSize=1",
                    new int[][]{{2, 2}},// Expect one entity, with 2 registrations, each with 2 effects
                    veryEarly, veryLate, veryEarly, veryLate
            );

            this.testRegistrationFilter("/demo/postnummer/1/rest/search?postnr=8000",
                    new int[][]{{1}, {1}},
                    "2017-04-01T00:00:00+01:00", "2017-04-01T15:06:21+01:00", "2016-06-01T00:00:00+01:00", "2017-06-01T00:00:00+01:00"
            );
        } finally {
            this.removeTestObject(uuid1);
            this.removeTestObject(uuid2);
        }
    }
/*
    @Test
    @Order(order = 12)
    public void restLookupCSVByParametersTest() throws IOException,
            DataFordelerException {
        this.setupUser();

        UUID uuid1 = this.addTestObject();
        UUID uuid2 = this.addTestObject();

        try {
            MediaType mediaType =
                    new MediaType("text", "csv", Charsets.UTF_8);

            Assert.assertEquals(
                    unifyNewlines(
                            updateTimestamps(
                                    getResourceAsString("/rest-search-1.csv")
                            )
                    ),
                    unifyNewlines(
                            getRegistrationFilterRequest(
                                    "/demo/postnummer/1/rest/search?postnr=8000",
                                    veryEarly, veryLate,
                                    veryEarly,veryLate,
                                    mediaType.toString()
                            ).getBody()
                                    .replaceAll(uuid1.toString(), "UUID#1")
                                    .replaceAll(uuid2.toString(), "UUID#2")
                    )
            );

            Assert.assertEquals(
                    unifyNewlines(
                            updateTimestamps(
                                    getResourceAsString("/rest-search-2.csv")
                            )
                    ),
                    unifyNewlines(
                            getRegistrationFilterRequest(
                                    "/demo/postnummer/1/rest/search?postnr=8000&page=1&pageSize=1",
                                    veryEarly, veryLate,
                                    veryEarly,veryLate,
                                    mediaType.toString()
                            ).getBody()
                                    .replaceAll(uuid1.toString(), "UUID#1")
                                    .replaceAll(uuid2.toString(), "UUID#2"))
            );

            Assert.assertEquals(
                    unifyNewlines(
                            updateTimestamps(
                                    getResourceAsString("/rest-search-3.csv")
                            )
                    ),
                    unifyNewlines(
                            getRegistrationFilterRequest(
                                    "/demo/postnummer/1/rest/search?postnr=8000&page=2&pageSize=1",
                                    veryEarly, veryLate,
                                    veryEarly,veryLate,
                                    mediaType.toString()
                            ).getBody()
                                    .replaceAll(uuid1.toString(), "UUID#1")
                                    .replaceAll(uuid2.toString(), "UUID#2"))
            );

            Assert.assertEquals(
                    unifyNewlines(
                            updateTimestamps(
                                    getResourceAsString("/rest-search-4.csv")
                            )),
                    unifyNewlines(
                            getRegistrationFilterRequest(
                                    "/demo/postnummer/1/rest/search?postnr=8000",
                                    "2017-04-01T00:00:00+01:00",
                                    "2017-04-01T15:06:21+01:00",
                                    "2016-06-01T00:00:00+01:00",
                                    "2017-06-01T00:00:00+01:00",
                                    mediaType.toString()
                            ).getBody()
                                    .replaceAll(uuid1.toString(), "UUID#1")
                                    .replaceAll(uuid2.toString(), "UUID#2")
                    )
            );
        } finally {
            this.removeTestObject(uuid1);
            this.removeTestObject(uuid2);
        }
    }

    @Test
    @Order(order = 12)
    public void restLookupTSVByParametersTest() throws IOException,
            DataFordelerException {
        this.setupUser();

        UUID uuid1 = this.addTestObject();
        UUID uuid2 = this.addTestObject();

        try {
            MediaType mediaType =
                    new MediaType("text", "tsv");

            Assert.assertEquals(
                    unifyNewlines(
                            updateTimestamps(
                                    getResourceAsString("/rest-search-1.csv")
                            )
                    )
                            .replaceAll(",", "\t"),

                    unifyNewlines(
                            getRegistrationFilterRequest(
                                    "/demo/postnummer/1/rest/search?postnr=8000",
                                    veryEarly, veryLate,
                                    veryEarly, veryLate,
                                    mediaType.toString()
                            ).getBody()
                                    .replaceAll(uuid1.toString(), "UUID#1")
                                    .replaceAll(uuid2.toString(), "UUID#2")
                    )
            );

            Assert.assertEquals(
                    unifyNewlines(
                            updateTimestamps(
                                    getResourceAsString("/rest-search-2.csv")
                            )
                                    .replaceAll(",", "\t")
                    ),
                    unifyNewlines(
                            getRegistrationFilterRequest(
                                    "/demo/postnummer/1/rest/search?postnr=8000&page=1&pageSize=1",
                                    veryEarly,veryLate,
                                    veryEarly,veryLate,
                                    mediaType.toString()).getBody()
                                    .replaceAll(uuid1.toString(), "UUID#1")
                                    .replaceAll(",", "\t")
                                    .replaceAll(uuid2.toString(), "UUID#2")
                    )
            );

            Assert.assertEquals(
                    unifyNewlines(
                            updateTimestamps(
                                    getResourceAsString("/rest-search-3.csv")
                            )
                    )
                            .replaceAll(",", "\t"),
                    unifyNewlines(
                            getRegistrationFilterRequest(
                                    "/demo/postnummer/1/rest/search?postnr=8000&page=2&pageSize=1",
                                    veryEarly, veryLate,
                                    veryEarly, veryLate,
                                    mediaType.toString()).getBody()
                                    .replaceAll(uuid1.toString(), "UUID#1")
                                    .replaceAll(uuid2.toString(), "UUID#2")
                    )
            );

            Assert.assertEquals(
                    unifyNewlines(
                            updateTimestamps(
                                    getResourceAsString("/rest-search-4.csv")
                            )
                                    .replaceAll(",", "\t")
                    ),
                    unifyNewlines(
                            getRegistrationFilterRequest(
                                    "/demo/postnummer/1/rest/search?postnr=8000",
                                    "2017-04-01T00:00:00+01:00",
                                    "2017-04-01T15:06:21+01:00",
                                    "2016-06-01T00:00:00+01:00",
                                    "2017-06-01T00:00:00+01:00",
                                    mediaType.toString()).getBody()
                                    .replaceAll(uuid1.toString(), "UUID#1")
                                    .replaceAll(uuid2.toString(), "UUID#2")
                    )
            );
        } finally {
            this.removeTestObject(uuid1);
            this.removeTestObject(uuid2);
        }
    }
*/
    @Test
    @Order(order = 11)
    public void restLookupXMLByUUIDTest() throws IOException, DataFordelerException {
        this.setupUser();
        UUID uuid = this.addTestObject();
        try {

            HttpHeaders headers = new HttpHeaders();
            headers.set("Accept", "application/xml");
            HttpEntity<String> httpEntity = new HttpEntity<>("", headers);
            ResponseEntity<String> resp = this.restTemplate.exchange("/demo/postnummer/1/rest/" + uuid.toString(), HttpMethod.GET, httpEntity, String.class);
            Assert.assertEquals(200, resp.getStatusCode().value());

            String xmlBody = resp.getBody();
            Assert.assertTrue(xmlBody.contains(uuid.toString()));
            Assert.assertTrue(xmlBody.contains("fapitest"));
        } finally {
            this.removeTestObject(uuid);
        }
    }

    private void testRegistrationFilter(String urlBase, int[][] expected, String registerOverlapStart, String registerOverlapEnd, String effectOverlapStart, String effectOverlapEnd) throws IOException, URISyntaxException {
        ResponseEntity<String> resp = getRegistrationFilterRequest(urlBase,
                null, registerOverlapEnd, registerOverlapStart, null,
                null,effectOverlapEnd, effectOverlapStart, null,
                MediaType.APPLICATION_JSON_VALUE, "rvd");
        JsonNode jsonBody = objectMapper.readTree(resp.getBody());

        ArrayNode list = (ArrayNode) jsonBody.get("results");
        Assert.assertEquals(expected.length, list.size());
        int i = 0;
        for (JsonNode entity : list) {
            JsonNode registrations = entity.get("registreringer");
            Assert.assertEquals(expected[i].length, registrations.size());
            for (int j = 0; j < expected[i].length; j++) {
                Assert.assertEquals(expected[i][j], registrations.get(j).get("virkninger").size());
            }
            i++;
        }
    }

    private ResponseEntity<String> getRegistrationFilterRequest(
            String urlBase,
            String registerFromAfter, String registerFromBefore, String registerToAfter, String registerToBefore,
            String effectFromAfter, String effectFromBefore, String effectToAfter, String effectToBefore,
            String mediaType, String mode
    ) throws URISyntaxException {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", mediaType);
        HttpEntity<String> httpEntity = new HttpEntity<>("", headers);
        String now = OffsetDateTime.now().truncatedTo(ChronoUnit.SECONDS).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);

        ParameterMap parameters = new ParameterMap();
        if (registerFromAfter != null) {
            parameters.add(Query.PARAM_REGISTRATION_FROM_AFTER[0], registerFromAfter);
        }
        if (registerFromBefore != null) {
            parameters.add(Query.PARAM_REGISTRATION_FROM_BEFORE[0], registerFromBefore);
        } else {
            parameters.add(Query.PARAM_REGISTRATION_FROM_BEFORE[0], now);
        }
        if (registerToAfter != null) {
            parameters.add(Query.PARAM_REGISTRATION_TO_AFTER[0], registerToAfter);
        } else {
            parameters.add(Query.PARAM_REGISTRATION_TO_AFTER[0], now);
        }
        if (registerToBefore != null) {
            parameters.add(Query.PARAM_REGISTRATION_TO_BEFORE[0], registerToBefore);
        }
        if (effectFromAfter != null) {
            parameters.add(Query.PARAM_EFFECT_FROM_AFTER[0], effectFromAfter);
        }
        if (effectFromBefore != null) {
            parameters.add(Query.PARAM_EFFECT_FROM_BEFORE[0], effectFromBefore);
        } else {
            parameters.add(Query.PARAM_EFFECT_FROM_BEFORE[0], now);
        }
        if (effectToAfter != null) {
            parameters.add(Query.PARAM_EFFECT_TO_AFTER[0], effectToAfter);
        } else {
            parameters.add(Query.PARAM_EFFECT_TO_AFTER[0], now);
        }
        if (effectToBefore != null) {
            parameters.add(Query.PARAM_EFFECT_TO_BEFORE[0], effectToBefore);
        }
        if (mode != null) {
            parameters.add(Query.PARAM_OUTPUT_WRAPPING[0], mode);
        }

        String sep = urlBase.contains("?") ? "&" : "?";

        ResponseEntity<String> resp = this.restTemplate.exchange(new URI(urlBase + sep + parameters.asUrlParams()), HttpMethod.GET, httpEntity, String.class);
        Assert.assertEquals(200, resp.getStatusCode().value());
        return resp;
    }


    private UUID addTestObject() throws DataFordelerException {
        UUID uuid = UUID.randomUUID();
        Session session = sessionManager.getSessionFactory().openSession();
        Transaction transaction = session.beginTransaction();
        try {

            DemoEntityRecord demoEntityRecord = new DemoEntityRecord(uuid, "fapitest");
            demoEntityRecord.setPostnr(8000);
            session.saveOrUpdate(demoEntityRecord);

            DemoDataRecord demoDataRecord1 = new DemoDataRecord("Århus C");
            demoDataRecord1.setBitemporality("2017-02-21T16:02:50+01:00", "2017-05-01T15:06:22+01:00", "2017-02-22T13:59:30+01:00", "2017-12-31T23:59:59+01:00");
            demoEntityRecord.addBitemporalRecord(demoDataRecord1, session);

            DemoDataRecord demoDataRecord2 = new DemoDataRecord("AArhus C");
            demoDataRecord2.setBitemporality("2017-02-21T16:02:50+01:00", "2017-05-01T15:06:22+01:00", "2018-01-01T00:00:00+01:00", null);
            demoEntityRecord.addBitemporalRecord(demoDataRecord2, session);

            DemoDataRecord demoDataRecord3 = new DemoDataRecord("Århus C");
            demoDataRecord3.setBitemporality("2017-05-01T15:06:22+01:00", null, "2017-02-22T13:59:30+01:00", "2017-12-31T23:59:59+01:00");
            demoEntityRecord.addBitemporalRecord(demoDataRecord3, session);

            DemoDataRecord demoDataRecord4 = new DemoDataRecord("Aarhus C");
            demoDataRecord4.setBitemporality("2017-05-01T15:06:22+01:00", null, "2018-01-01T00:00:00+01:00", null);
            demoEntityRecord.addBitemporalRecord(demoDataRecord4, session);

            transaction.commit();

        } catch (Exception e) {
            transaction.rollback();
            throw e;
        } finally {
            session.close();
        }

        return uuid;
    }

    private void removeTestObject(UUID uuid) {
        Session session = sessionManager.getSessionFactory().openSession();
        try {
            Transaction transaction = session.beginTransaction();
            DemoEntityRecord entity = QueryManager.getEntity(session, uuid, DemoEntityRecord.class);
            if (entity != null) {
                session.delete(entity);
            }
            transaction.commit();
        } finally {
            session.close();
        }
    }


    private void setupUser() {

        UserProfile testUserProfile = new UserProfile("TestProfile", Arrays.asList(
                DemoRolesDefinition.READ_DEMO_ENTITY_ROLE.getRoleName(),
                DemoRolesDefinition.READ_SERVICE_ROLE.getRoleName()
        ));

        TestUserDetails testUserDetails = new TestUserDetails();
        testUserDetails.addUserProfile(testUserProfile);
        when(dafoUserManager.getFallbackUser()).thenReturn(testUserDetails);
    }

    private static String getResourceAsString(String resourceName)
            throws IOException {
        return IOUtils.toString(
                FapiTest.class.getResourceAsStream(resourceName),
                Charsets.UTF_8
        );
    }

    private static Pattern newlinePattern = Pattern.compile("\\R");

    private static String unifyNewlines(String input) {
        return newlinePattern.matcher(input).replaceAll(System.getProperty("line.separator"));
    }

    private static boolean UTCisWithoutQuotes = true;
    private static Pattern quotePattern = Pattern.compile("^\"(.*)\"$");

    private static String updateTimestamps(String input) {
        return updateTimestamps(input, systemZone);
    }

    private static List<String> getCsvHeaders(String data, String separator) {
        String headerLine = data.split("\n", 2)[0];
        return Arrays.asList(headerLine.split(separator));
    }

    private static String sortCsvColumns(String data, List<String> sortKey, String separator) {
        List<String> currentHeaders = getCsvHeaders(data, separator);
        StringJoiner output = new StringJoiner("\n");
        String[] rows = data.split("\n");
        for (int i=1; i<rows.length; i++) {
            HashMap<String, String> rowData = new HashMap<>();
            String[] cells = rows[i].split(separator);
            for (int j=0; j<cells.length; j++) {
                rowData.put(currentHeaders.get(j), cells[j]);
            }
            StringJoiner outputRow = new StringJoiner(separator);
            for (String header : sortKey) {
                String cellData = rowData.get(header);
                outputRow.add(cellData != null ? cellData : "");
            }
            output.add(outputRow.toString());
        }
        StringJoiner headerJoiner = new StringJoiner(separator);
        for (String headerValue : sortKey) {
            headerJoiner.add(headerValue);
        }
        return headerJoiner.toString() + "\n" + output.toString();
    }

    private static String updateTimestamps(String input, ZoneId zone) {
        String[] lines = input.split("\n", -1);
        StringJoiner output = new StringJoiner("\n");
        for (String line : lines) {
            String[] tokens = line.split(",");
            StringJoiner lineOutput = new StringJoiner(",");
            for (String token : tokens) {
                Matcher m = quotePattern.matcher(token);
                boolean quoted = false;
                if (m.find()) {
                    token = m.group(1);
                    quoted = true;
                }
                try {
                    OffsetDateTime dateTime = OffsetDateTime.parse(token, DateTimeFormatter.ISO_OFFSET_DATE_TIME);
                    dateTime = dateTime.atZoneSameInstant(zone).toOffsetDateTime();
                    token = dateTime.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
                    if (UTCisWithoutQuotes && zone.normalized().equals(ZoneOffset.UTC)) {
                        quoted = false;
                    }
                } catch (DateTimeParseException e) {
                    // pass
                }
                if (quoted) {
                    token = "\"" + token + "\"";
                }

                lineOutput.add(token);
            }
            output.add(lineOutput.toString());
        }
        return output.toString();
    }

    @Test
    public void testEnsureNumeric() throws InvalidClientInputException {

        // These should not throw exception
        BaseQuery.ensureNumeric("test", "1234");
        BaseQuery.ensureNumeric("test", "1234*");
        BaseQuery.ensureNumeric("test", "1234", false);
        BaseQuery.ensureNumeric("test", "1234*", false);
        BaseQuery.ensureNumeric("test", "1234", true);
        BaseQuery.ensureNumeric("test", "1234*", true);
        BaseQuery.ensureNumeric("test", "4003279411", true);
        BaseQuery.ensureNumeric("test", "2147483647", true);
        BaseQuery.ensureNumeric("test", "9223372036854775807", true);

        // These should throw exception
        Assert.assertThrows(InvalidClientInputException.class, () -> {
            BaseQuery.ensureNumeric("test", "2147483648", false);
            BaseQuery.ensureNumeric("test", "4003279411", false);
            BaseQuery.ensureNumeric("test", "4003279411", true);
            BaseQuery.ensureNumeric("test", "9223372036854775808", true);
            BaseQuery.ensureNumeric("test", "124A", false);
        });
    }
}
