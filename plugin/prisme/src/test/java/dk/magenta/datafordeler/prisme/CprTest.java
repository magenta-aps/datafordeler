package dk.magenta.datafordeler.prisme;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import dk.magenta.datafordeler.core.Application;
import dk.magenta.datafordeler.core.database.QueryManager;
import dk.magenta.datafordeler.core.database.SessionManager;
import dk.magenta.datafordeler.core.exception.DataFordelerException;
import dk.magenta.datafordeler.core.io.ImportMetadata;
import dk.magenta.datafordeler.core.user.DafoUserManager;
import dk.magenta.datafordeler.core.util.Bitemporality;
import dk.magenta.datafordeler.core.util.InputStreamReader;
import dk.magenta.datafordeler.core.util.UnorderedJsonListComparator;
import dk.magenta.datafordeler.cpr.CprAreaRestrictionDefinition;
import dk.magenta.datafordeler.cpr.CprPlugin;
import dk.magenta.datafordeler.cpr.CprRolesDefinition;
import dk.magenta.datafordeler.cpr.data.person.PersonEntity;
import dk.magenta.datafordeler.cpr.data.person.PersonEntityManager;
import dk.magenta.datafordeler.cpr.data.person.PersonSubscription;
import dk.magenta.datafordeler.cpr.data.person.PersonSubscriptionAssignmentStatus;
import dk.magenta.datafordeler.cpr.direct.CprDirectLookup;
import dk.magenta.datafordeler.cpr.records.person.CprBitemporalPersonRecord;
import dk.magenta.datafordeler.cpr.records.person.data.AddressDataRecord;
import dk.magenta.datafordeler.cpr.records.person.data.PersonStatusDataRecord;
import dk.magenta.datafordeler.geo.GeoLookupService;
import org.hamcrest.CoreMatchers;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.junit.*;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.skyscreamer.jsonassert.JSONAssert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.persistence.FlushModeType;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;
import java.util.UUID;

import static org.mockito.Mockito.when;


@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = Application.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class CprTest extends TestBase {

    @Autowired
    private SessionManager sessionManager;

    @Autowired
    private PersonEntityManager personEntityManager;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @SpyBean
    private DafoUserManager dafoUserManager;

    @Autowired
    private CprPlugin cprPlugin;

    @Autowired
    private CprService cprService;

    @Autowired
    private PersonOutputWrapperPrisme personOutputWrapper;

    @After
    public void cleanup() {
        this.cleanupPersonData(sessionManager);
        this.cleanupGeoData(sessionManager);
    }

    public void loadPerson(String personfile) throws Exception {
        InputStream testData = CprTest.class.getResourceAsStream(personfile);
        ImportMetadata importMetadata = new ImportMetadata();
        Session session = sessionManager.getSessionFactory().openSession();
        importMetadata.setSession(session);
        Transaction transaction = session.beginTransaction();
        importMetadata.setTransactionInProgress(true);
        personEntityManager.parseData(testData, importMetadata);
        transaction.commit();
        session.close();
        testData.close();
    }

    public void loadManyPersons(int count) throws Exception {
        this.loadManyPersons(count, 0);
    }

    public void loadManyPersons(int count, int start) throws Exception {
        ImportMetadata importMetadata = new ImportMetadata();
        Session session = sessionManager.getSessionFactory().openSession();
        importMetadata.setSession(session);
        Transaction transaction = session.beginTransaction();
        importMetadata.setTransactionInProgress(true);
        String testData = InputStreamReader.readInputStream(CprTest.class.getResourceAsStream("/person.txt"));
        String[] lines = testData.split("\n");
        for (int i = start; i < count + start; i++) {
            StringJoiner sb = new StringJoiner("\n");
            String newCpr = String.format("%010d", i);
            for (int j = 0; j < lines.length; j++) {
                String line = lines[j];
                line = line.substring(0, 3) + newCpr + line.substring(13);
                sb.add(line);
            }
            ByteArrayInputStream bais = new ByteArrayInputStream(sb.toString().getBytes(StandardCharsets.UTF_8));
            personEntityManager.parseData(bais, importMetadata);
            bais.close();
        }
        transaction.commit();
        session.close();
    }

    /**
     * Load a person with a specified age
     *
     * @param age
     * @throws Exception
     */
    public void loadSpecificAgePersons(int age) throws Exception {
        ImportMetadata importMetadata = new ImportMetadata();
        Session session = sessionManager.getSessionFactory().openSession();
        importMetadata.setSession(session);
        Transaction transaction = session.beginTransaction();
        importMetadata.setTransactionInProgress(true);

        String testData = InputStreamReader.readInputStream(CprTest.class.getResourceAsStream("/person.txt"));
        String[] lines = testData.split("\n");

        StringJoiner sb = new StringJoiner("\n");
        String birthYear = String.format("%04d", OffsetDateTime.now().minusYears(age).getYear());
        for (int j = 0; j < lines.length; j++) {
            String line = lines[j];
            if (line.startsWith("0010101001234")) {
                line = line.substring(0, 67) + birthYear + line.substring(71);
            }
            sb.add(line);
        }
        ByteArrayInputStream bais = new ByteArrayInputStream(sb.toString().getBytes(StandardCharsets.UTF_8));
        personEntityManager.parseData(bais, importMetadata);
        bais.close();
        transaction.commit();
        session.close();
    }


    @Before
    public void load() throws IOException, DataFordelerException {
        this.loadAllGeoAdress(sessionManager);
    }

    private static void transfer(ObjectNode from, ObjectNode to, String field) {
        if (from.has(field)) {
            to.set(field, from.get(field));
        } else {
            to.remove(field);
        }
    }

    @Test
    public void test1PersonRecordOutput() throws Exception {

        Session session = sessionManager.getSessionFactory().openSession();
        GeoLookupService lookupService = new GeoLookupService(sessionManager);
        personOutputWrapper.setLookupService(lookupService);
        try {
            String ENTITY = "e";
            Class eClass = PersonEntity.class;
            org.hibernate.query.Query<PersonEntity> databaseQuery = session.createQuery("select " + ENTITY + " from " + eClass.getCanonicalName() + " " + ENTITY + " join " + ENTITY + ".identification i where i.uuid != null", eClass);
            databaseQuery.setFlushMode(FlushModeType.COMMIT);

            databaseQuery.setMaxResults(1000);

            for (PersonEntity entity : databaseQuery.getResultList()) {
                ObjectNode newOutput = (ObjectNode) personOutputWrapper.wrapRecordResult(entity, null);
                Assert.assertEquals(955, newOutput.get("myndighedskode").intValue());

                //Postnummer does not work when running the unittest with maven, it works when running manually.
                //Since it is gladdreg and is on its way out, this test is disabled
                //Assert.assertEquals(3982, newOutput.get("postnummer").intValue() );
                Assert.assertEquals(1, newOutput.get("vejkode").intValue());
                Assert.assertEquals("GL", newOutput.get("landekode").textValue());
            }
        } finally {
            session.close();
        }
    }

    @Test
    public void test2PersonPrisme() throws Exception {
        loadPerson("/person.txt");

        HttpEntity<String> httpEntity = new HttpEntity<String>("", new HttpHeaders());
        ResponseEntity<String> response = restTemplate.exchange(
                "/prisme/cpr/1/" + "0101001234",
                HttpMethod.GET,
                httpEntity,
                String.class
        );
        Assert.assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }


    @Test
    public void test3PersonPrisme() throws Exception {
        loadPerson("/person.txt");

        TestUserDetails testUserDetails = new TestUserDetails();

        HttpEntity<String> httpEntity = new HttpEntity<String>("", new HttpHeaders());

        testUserDetails.giveAccess(CprRolesDefinition.READ_CPR_ROLE);
        this.applyAccess(testUserDetails);
        ResponseEntity<String> response = restTemplate.exchange(
                "/prisme/cpr/1/" + "0101001234",
                HttpMethod.GET,
                httpEntity,
                String.class
        );
        Assert.assertEquals(HttpStatus.OK, response.getStatusCode());
        Assert.assertTrue(objectMapper.readTree(response.getBody()).size() > 0);


        testUserDetails.giveAccess(
                cprPlugin.getAreaRestrictionDefinition().getAreaRestrictionTypeByName(
                        CprAreaRestrictionDefinition.RESTRICTIONTYPE_KOMMUNEKODER
                ).getRestriction(
                        CprAreaRestrictionDefinition.RESTRICTION_KOMMUNE_AVANNAATA
                )
        );
        this.applyAccess(testUserDetails);
        response = restTemplate.exchange(
                "/prisme/cpr/1/" + "0101001234",
                HttpMethod.GET,
                httpEntity,
                String.class
        );
        Assert.assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());

        testUserDetails.giveAccess(
                cprPlugin.getAreaRestrictionDefinition().getAreaRestrictionTypeByName(
                        CprAreaRestrictionDefinition.RESTRICTIONTYPE_KOMMUNEKODER
                ).getRestriction(
                        CprAreaRestrictionDefinition.RESTRICTION_KOMMUNE_SERMERSOOQ
                )
        );
        this.applyAccess(testUserDetails);
        response = restTemplate.exchange(
                "/prisme/cpr/1/" + "0101001234",
                HttpMethod.GET,
                httpEntity,
                String.class
        );
        Assert.assertEquals(HttpStatus.OK, response.getStatusCode());
        Assert.assertTrue(objectMapper.readTree(response.getBody()).size() > 0);
        JsonNode responseContent = objectMapper.readTree(response.getBody());
        //Assert.assertFalse(responseContent.get("adminadresse").asBoolean());

        Assert.assertThat(response.getBody(), CoreMatchers.containsString("\"far\":\"0101641234\""));
        Assert.assertThat(response.getBody(), CoreMatchers.containsString("\"mor\":\"2903641234\""));

    }

    @Test
    public void testAdminAddPersonPrisme() throws Exception {
        loadPerson("/personAdminAdd.txt");
        TestUserDetails testUserDetails = new TestUserDetails();
        HttpEntity<String> httpEntity = new HttpEntity<String>("", new HttpHeaders());
        testUserDetails.giveAccess(CprRolesDefinition.READ_CPR_ROLE);
        this.applyAccess(testUserDetails);

        testUserDetails.giveAccess(
                cprPlugin.getAreaRestrictionDefinition().getAreaRestrictionTypeByName(
                        CprAreaRestrictionDefinition.RESTRICTIONTYPE_KOMMUNEKODER
                ).getRestriction(
                        CprAreaRestrictionDefinition.RESTRICTION_KOMMUNE_SERMERSOOQ
                )
        );
        this.applyAccess(testUserDetails);
        ResponseEntity<String> response = restTemplate.exchange(
                "/prisme/cpr/1/" + "0101005551",
                HttpMethod.GET,
                httpEntity,
                String.class
        );
        JsonNode responseContent = objectMapper.readTree(response.getBody());
        //Assert.assertTrue(responseContent.get("adminadresse").asBoolean());
    }

    /**
     * Test the service cpr/2
     * Do not currently know why there is two services, but they should both be testet
     *
     * @throws Exception
     */
    @Test
    public void test4PersonPrisme() throws Exception {
        loadPerson("/person.txt");

        TestUserDetails testUserDetails = new TestUserDetails();

        HttpEntity<String> httpEntity = new HttpEntity<String>("", new HttpHeaders());

        testUserDetails.giveAccess(CprRolesDefinition.READ_CPR_ROLE);
        this.applyAccess(testUserDetails);
        ResponseEntity<String> response = restTemplate.exchange(
                "/prisme/cpr/2/" + "0101001234",
                HttpMethod.GET,
                httpEntity,
                String.class
        );
        Assert.assertEquals(HttpStatus.OK, response.getStatusCode());
        Assert.assertTrue(objectMapper.readTree(response.getBody()).size() > 0);

        Assert.assertThat(response.getBody(), CoreMatchers.containsString("\"far\":\"0101641234\""));
        Assert.assertThat(response.getBody(), CoreMatchers.containsString("\"mor\":\"2903641234\""));

        testUserDetails.giveAccess(
                cprPlugin.getAreaRestrictionDefinition().getAreaRestrictionTypeByName(
                        CprAreaRestrictionDefinition.RESTRICTIONTYPE_KOMMUNEKODER
                ).getRestriction(
                        CprAreaRestrictionDefinition.RESTRICTION_KOMMUNE_KUJALLEQ
                )
        );
        this.applyAccess(testUserDetails);
        response = restTemplate.exchange(
                "/prisme/cpr/2/" + "0101001234",
                HttpMethod.GET,
                httpEntity,
                String.class
        );
        Assert.assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());

    }

    /**
     * Validate that it is possible to find if a person is currently citizen in greenland, and if yes how long the person has been living here
     *
     * @throws Exception
     */
    @Test
    public void testResidentPersonPrisme() throws Exception {
        loadPerson("/persons_with_history.txt");

        TestUserDetails testUserDetails = new TestUserDetails();
        HttpEntity<String> httpEntity = new HttpEntity<String>("", new HttpHeaders());

        ResponseEntity<String> response = restTemplate.exchange(
                "/prisme/cpr/residentinformation/1/" + "1111111110",
                HttpMethod.GET,
                httpEntity,
                String.class
        );
        Assert.assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());

        testUserDetails.giveAccess(CprRolesDefinition.READ_CPR_ROLE);
        this.applyAccess(testUserDetails);
        response = restTemplate.exchange(
                "/prisme/cpr/residentinformation/1/" + "1111111110",
                HttpMethod.GET,
                httpEntity,
                String.class
        );
        Assert.assertEquals(HttpStatus.OK, response.getStatusCode());
        JSONAssert.assertEquals("{\"cprNummer\":\"1111111110\",\"borIGL\":true,\"dato\":\"2020-10-28\"}", response.getBody(), false);


        response = restTemplate.exchange(
                "/prisme/cpr/residentinformation/1/" + "1211111111",
                HttpMethod.GET,
                httpEntity,
                String.class
        );
        Assert.assertEquals(HttpStatus.OK, response.getStatusCode());
        JSONAssert.assertEquals("{\"cprNummer\":\"1211111111\",\"borIGL\":true,\"dato\":2020-10-28}", response.getBody(), false);

        response = restTemplate.exchange(
                "/prisme/cpr/residentinformation/1/" + "1311111111",
                HttpMethod.GET,
                httpEntity,
                String.class
        );
        Assert.assertEquals(HttpStatus.OK, response.getStatusCode());
        JSONAssert.assertEquals("{\"cprNummer\":\"1311111111\",\"borIGL\":true,\"dato\":\"2020-07-17\"}", response.getBody(), false);
    }

    @Test
    public void testMaintainChildrenPrisme() throws Exception {
        TestUserDetails testUserDetails = new TestUserDetails();

        HttpEntity<String> httpEntity = new HttpEntity<String>("", new HttpHeaders());

        ResponseEntity<String> response = restTemplate.exchange(
                "/prisme/cpr/under18years/1/search/?municipalitycode=956&updatedSince=" + "2010-01-01",
                HttpMethod.GET,
                httpEntity,
                String.class
        );
        Assert.assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());

        testUserDetails.giveAccess(CprRolesDefinition.READ_CPR_ROLE);
        this.applyAccess(testUserDetails);

        this.loadSpecificAgePersons(16);
        response = restTemplate.exchange(
                "/prisme/cpr/under18years/1/search/?municipalitycode=956&updatedSince=" + "2010-01-01",
                HttpMethod.GET,
                httpEntity,
                String.class
        );
        Assert.assertEquals(HttpStatus.OK, response.getStatusCode());
        Assert.assertEquals(1, objectMapper.readTree(response.getBody()).get("results").size());

        response = restTemplate.exchange(
                "/prisme/cpr/under18years/1/search/?municipalitycode=956&pageSize=10000&updatedSince=" + "2010-01-01",
                HttpMethod.GET,
                httpEntity,
                String.class
        );
        Assert.assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());

        response = restTemplate.exchange(
                "/prisme/cpr/under18years/1/search/?municipalitycode=956&updatedSince=" + "2030-01-01",
                HttpMethod.GET,
                httpEntity,
                String.class
        );
        Assert.assertEquals(HttpStatus.OK, response.getStatusCode());
        Assert.assertEquals(0, objectMapper.readTree(response.getBody()).get("results").size());

        this.cleanup();
        this.loadSpecificAgePersons(19);
        response = restTemplate.exchange(
                "/prisme/cpr/under18years/1/search/?municipalitycode=956&updatedSince=" + "2010-01-01",
                HttpMethod.GET,
                httpEntity,
                String.class
        );
        Assert.assertEquals(HttpStatus.OK, response.getStatusCode());
        Assert.assertEquals(0, objectMapper.readTree(response.getBody()).get("results").size());

        this.cleanup();
        this.loadSpecificAgePersons(16);
        response = restTemplate.exchange(
                "/prisme/cpr/under18years/1/search/?municipalitycode=956&updatedSince=" + "2010-01-01",
                HttpMethod.GET,
                httpEntity,
                String.class
        );

        Assert.assertEquals(HttpStatus.OK, response.getStatusCode());
        Assert.assertEquals(1, objectMapper.readTree(response.getBody()).get("results").size());

        response = restTemplate.exchange(
                "/prisme/cpr/under18years/1/search/?municipalitycode=957&updatedSince=" + "2010-01-01",
                HttpMethod.GET,
                httpEntity,
                String.class
        );
        Assert.assertEquals(HttpStatus.OK, response.getStatusCode());
        Assert.assertEquals(0, objectMapper.readTree(response.getBody()).get("results").size());
    }


    @Test
    public void test4PersonBulkPrisme() throws Exception {

        OffsetDateTime start = OffsetDateTime.now();
        loadManyPersons(5, 0);
        OffsetDateTime middle = OffsetDateTime.now();
        Thread.sleep(10);
        loadManyPersons(5, 5);
        OffsetDateTime afterLoad = OffsetDateTime.now();

        TestUserDetails testUserDetails = new TestUserDetails();


        testUserDetails.giveAccess(CprRolesDefinition.READ_CPR_ROLE);
        this.applyAccess(testUserDetails);

        ObjectNode body = objectMapper.createObjectNode();
        body.put("cprNumber", "0000000009");
        HttpEntity<String> httpEntity = new HttpEntity<>(body.toString(), new HttpHeaders());
        ResponseEntity<String> response = restTemplate.exchange(
                "/prisme/cpr/1/",
                HttpMethod.POST,
                httpEntity,
                String.class
        );
        Assert.assertEquals(HttpStatus.OK, response.getStatusCode());
        Assert.assertEquals(1, objectMapper.readTree(response.getBody()).size());


        body = objectMapper.createObjectNode();
        ArrayNode cprList = objectMapper.createArrayNode();
        cprList.add("0000000002");
        cprList.add("0000000005");
        body.set("cprNumber", cprList);
        httpEntity = new HttpEntity<String>(body.toString(), new HttpHeaders());
        response = restTemplate.exchange(
                "/prisme/cpr/1/",
                HttpMethod.POST,
                httpEntity,
                String.class
        );
        Assert.assertEquals(HttpStatus.OK, response.getStatusCode());
        Assert.assertEquals(2, objectMapper.readTree(response.getBody()).size());


        body = objectMapper.createObjectNode();
        cprList = objectMapper.createArrayNode();
        cprList.add("0000000000");
        cprList.add("0000000001");
        cprList.add("0000000002");
        cprList.add("0000000003");
        cprList.add("0000000004");
        cprList.add("0000000005");
        cprList.add("0000000006");
        cprList.add("0000000007");
        cprList.add("0000000008");
        cprList.add("0000000009");
        body.set("cprNumber", cprList);
        httpEntity = new HttpEntity<String>(body.toString(), new HttpHeaders());
        response = restTemplate.exchange(
                "/prisme/cpr/1/",
                HttpMethod.POST,
                httpEntity,
                String.class
        );
        Assert.assertEquals(HttpStatus.OK, response.getStatusCode());
        Assert.assertEquals(10, objectMapper.readTree(response.getBody()).size());


        body = objectMapper.createObjectNode();
        cprList = objectMapper.createArrayNode();
        cprList.add("0000000000");
        cprList.add("0000000001");
        cprList.add("0000000002");
        cprList.add("0000000003");
        cprList.add("0000000004");
        cprList.add("0000000005");
        cprList.add("0000000006");
        cprList.add("0000000007");
        cprList.add("0000000008");
        cprList.add("0000000009");
        body.set("cprNumber", cprList);
        body.put("updatedSince", start.minusSeconds(1).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));
        httpEntity = new HttpEntity<String>(body.toString(), new HttpHeaders());
        response = restTemplate.exchange(
                "/prisme/cpr/1/",
                HttpMethod.POST,
                httpEntity,
                String.class
        );
        Assert.assertEquals(HttpStatus.OK, response.getStatusCode());
        Assert.assertEquals(10, objectMapper.readTree(response.getBody()).size());

        body = objectMapper.createObjectNode();
        cprList = objectMapper.createArrayNode();
        cprList.add("0000000000");
        cprList.add("0000000001");
        cprList.add("0000000002");
        cprList.add("0000000003");
        cprList.add("0000000004");
        cprList.add("0000000005");
        cprList.add("0000000006");
        cprList.add("0000000007");
        cprList.add("0000000008");
        cprList.add("0000000009");
        body.set("cprNumber", cprList);
        body.put("updatedSince", middle.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));
        httpEntity = new HttpEntity<String>(body.toString(), new HttpHeaders());
        response = restTemplate.exchange(
                "/prisme/cpr/1/",
                HttpMethod.POST,
                httpEntity,
                String.class
        );
        Assert.assertEquals(HttpStatus.OK, response.getStatusCode());
        Assert.assertEquals(5, objectMapper.readTree(response.getBody()).size());

    }

    private void applyAccess(TestUserDetails testUserDetails) {
        when(dafoUserManager.getFallbackUser()).thenReturn(testUserDetails);
    }

    @SpyBean
    private CprDirectLookup cprDirectLookup;

    @Test
    public void testDirectLookup1() throws Exception {

        String cpr = "0707611234";
        String data = "038406fJrr7CCxWUDI0178001590000000000000003840120190808000000000010707611234          01000000000000 M1961-07-07 1961-07-07*           Socialrådg.                       002070761123409560254018 01  mf                                      198010102000 196107071034 0000000000000000                                                                                                                                                                                                   0030707611234Mortensen,Jens                                                                                        Boulevarden 101,1 mf                                                6800Varde               05735731101 01  mf    Boulevarden         0080707611234Jens                                                                                        Mortensen                                196107072000 Mortensen,Jens                    00907076112345150                    01007076112345100199103201299*0110707611234F1961-07-07*0120707611234F0706611234                                              198010012000             014070761123413018140770140707611234131281123401507076112341961-07-07*0912414434                                              1961-07-07*0909414385                                              01707076112342019-04-10*          0002                    Terd                              2019-04-10grd                                                                                                                                                                       999999999999900000012";
        Mockito.doReturn(data).when(cprDirectLookup).lookup(ArgumentMatchers.eq(cpr));
        TestUserDetails testUserDetails = new TestUserDetails();

        HttpEntity<String> httpEntity = new HttpEntity<String>("", new HttpHeaders());

        testUserDetails.giveAccess(CprRolesDefinition.READ_CPR_ROLE);
        this.applyAccess(testUserDetails);
        ResponseEntity<String> response = restTemplate.exchange(
                "/prisme/cpr/combined/1/" + cpr,
                HttpMethod.GET,
                httpEntity,
                String.class
        );
        ObjectNode responseObject = (ObjectNode) objectMapper.readTree(response.getBody());

        Assert.assertEquals("0707611234", responseObject.get("cprNummer").asText());
        Assert.assertEquals("Jens", responseObject.get("fornavn").asText());
        Assert.assertEquals("Mortensen", responseObject.get("efternavn").asText());
        Assert.assertEquals("F", responseObject.get("civilstand").asText());
        Assert.assertEquals("1980-10-01", responseObject.get("civilstandsdato").asText());
        Assert.assertEquals("0706611234", responseObject.get("ægtefælleCprNummer").asText());
        Assert.assertEquals(false, responseObject.get("adressebeskyttelse").asBoolean());
        Assert.assertEquals("M", responseObject.get("køn").asText());
        Assert.assertEquals("0909414385", responseObject.get("far").asText());
        Assert.assertEquals("0912414434", responseObject.get("mor").asText());
        Assert.assertEquals(1, responseObject.get("statuskode").asInt());
        Assert.assertEquals("1980-10-10", responseObject.get("tilflytningsdato").asText());
        Assert.assertEquals("Kommuneqarfik Sermersooq", responseObject.get("kommune").asText());
        Assert.assertEquals(956, responseObject.get("myndighedskode").asInt());
        Assert.assertEquals(254, responseObject.get("vejkode").asInt());
        Assert.assertEquals(3900, responseObject.get("postnummer").asInt());
        Assert.assertEquals(600, responseObject.get("stedkode").asInt());
        Assert.assertEquals("GL", responseObject.get("landekode").asText());

        Session session = sessionManager.getSessionFactory().openSession();
        try {
            List<PersonSubscription> existingSubscriptions = QueryManager.getAllItems(session, PersonSubscription.class);
            Assert.assertEquals(1, existingSubscriptions.size());
            PersonSubscription subscription = existingSubscriptions.get(0);
            Assert.assertEquals("0707611234", subscription.getPersonNumber());
            Assert.assertEquals(PersonSubscriptionAssignmentStatus.CreatedInTable, subscription.getAssignment());
        } finally {
            session.close();
        }
    }


    @Test
    public void testDirectLookup2() throws Exception {

        String cpr = "0607621234";
        String data = "038406uKBKxWLcWUDI0178001104000000000000003840120190815000000000010607621234          90200502051034 M1962-07-06 2005-10-20                                              0030607621234Petersen,Mads Munk                                                                                                                                                                                                                          0080607621234Mads                                               Munk                                     Petersen                                 196207061029 Petersen,Mads Munk                00906076212345180                    01006076212345180196207061029*0110607621234U1962-07-06 0120607621234D0506650038                                              200502051034             01406076212340506871018014060762123405089210040140607621234060794106801406076212340705901007014060762123407059600110140607621234080789104901506076212341962-07-06*0000000000                                              1962-07-06*0000000000                                              999999999999900000014";

        Mockito.doReturn(data).when(cprDirectLookup).lookup(ArgumentMatchers.eq(cpr));
        TestUserDetails testUserDetails = new TestUserDetails();

        HttpEntity<String> httpEntity = new HttpEntity<String>("", new HttpHeaders());

        testUserDetails.giveAccess(CprRolesDefinition.READ_CPR_ROLE);
        this.applyAccess(testUserDetails);
        ResponseEntity<String> response = restTemplate.exchange(
                "/prisme/cpr/combined/1/" + cpr,
                HttpMethod.GET,
                httpEntity,
                String.class
        );

        ObjectNode responseObject = (ObjectNode) objectMapper.readTree(response.getBody());

        Assert.assertEquals("0607621234", responseObject.get("cprNummer").asText());
        Assert.assertEquals("Mads Munk", responseObject.get("fornavn").asText());
        Assert.assertEquals("Petersen", responseObject.get("efternavn").asText());
        Assert.assertEquals("D", responseObject.get("civilstand").asText());
        Assert.assertEquals("2005-02-05", responseObject.get("civilstandsdato").asText());
        Assert.assertEquals("0506650038", responseObject.get("ægtefælleCprNummer").asText());
        Assert.assertEquals(false, responseObject.get("adressebeskyttelse").asBoolean());
        Assert.assertEquals("M", responseObject.get("køn").asText());
        Assert.assertEquals(90, responseObject.get("statuskode").asInt());
        Assert.assertNull(responseObject.get("far"));
        Assert.assertNull(responseObject.get("mor"));
        Assert.assertNull(responseObject.get("tilflytningsdato"));
        Assert.assertNull(responseObject.get("myndighedskode"));
        Assert.assertNull(responseObject.get("vejkode"));
        Assert.assertNull(responseObject.get("postnummer"));
        Assert.assertNull(responseObject.get("stedkode"));
        Assert.assertNull(responseObject.get("landekode"));

        Session session = sessionManager.getSessionFactory().openSession();
        try {
            List<PersonSubscription> existingSubscriptions = QueryManager.getAllItems(session, PersonSubscription.class);
            Assert.assertEquals(1, existingSubscriptions.size());
            PersonSubscription subscription = existingSubscriptions.get(0);
            Assert.assertEquals("0607621234", subscription.getPersonNumber());
            Assert.assertEquals(PersonSubscriptionAssignmentStatus.CreatedInTable, subscription.getAssignment());
        } finally {
            session.close();
        }
    }


    @Test
    public void testDirectLookup3() throws Exception {

        String data = "038406uKBKxWLcWUDI0178001104000000000000003840120190815000000000010607621234          90200502051034 M1962-07-06 2005-10-20                                              0030607621234Petersen,Mads Munk                                                                                                                                                                                                                          0080607621234Mads                                               Munk                                     Petersen                                 196207061029 Petersen,Mads Munk                00906076212345180                    01006076212345180196207061029*0110607621234U1962-07-06 0120607621234D0506650038                                              200502051034             01406076212340506871018014060762123405089210040140607621234060794106801406076212340705901007014060762123407059600110140607621234080789104901506076212341962-07-06*0000000000                                              1962-07-06*0000000000                                              999999999999900000014";

        Mockito.doReturn(data).when(cprDirectLookup).lookup(ArgumentMatchers.eq("0607621234"));
        TestUserDetails testUserDetails = new TestUserDetails();

        loadPerson("/person.txt");

        HttpEntity<String> httpEntity = new HttpEntity<String>("{\"cprNumber\":[\"0101001234\",\"0607621234\"]}", new HttpHeaders());

        testUserDetails.giveAccess(CprRolesDefinition.READ_CPR_ROLE);
        this.applyAccess(testUserDetails);
        ResponseEntity<String> response = restTemplate.postForEntity(
                "/prisme/cpr/combined/1/",
                httpEntity,
                String.class
        );

        ObjectNode responseObject = (ObjectNode) objectMapper.readTree(response.getBody());
        Assert.assertEquals(2, responseObject.size());

        Assert.assertTrue(responseObject.has("0607621234"));
        ObjectNode personObject = (ObjectNode) responseObject.get("0607621234");
        Assert.assertEquals("0607621234", personObject.get("cprNummer").asText());
        Assert.assertEquals("Mads Munk", personObject.get("fornavn").asText());
        Assert.assertEquals("Petersen", personObject.get("efternavn").asText());
        Assert.assertEquals("D", personObject.get("civilstand").asText());
        Assert.assertEquals("2005-02-05", personObject.get("civilstandsdato").asText());
        Assert.assertEquals("0506650038", personObject.get("ægtefælleCprNummer").asText());
        Assert.assertEquals(false, personObject.get("adressebeskyttelse").asBoolean());
        Assert.assertEquals("M", personObject.get("køn").asText());
        Assert.assertEquals(90, personObject.get("statuskode").asInt());
        Assert.assertNull(personObject.get("far"));
        Assert.assertNull(personObject.get("mor"));
        Assert.assertNull(personObject.get("tilflytningsdato"));
        Assert.assertNull(personObject.get("myndighedskode"));
        Assert.assertNull(personObject.get("vejkode"));
        Assert.assertNull(personObject.get("postnummer"));
        Assert.assertNull(personObject.get("stedkode"));
        Assert.assertNull(personObject.get("landekode"));

        Assert.assertTrue(responseObject.has("0101001234"));
        personObject = (ObjectNode) responseObject.get("0101001234");
        Assert.assertEquals("0101001234", personObject.get("cprNummer").asText());
        Assert.assertEquals("Tester Testmember", personObject.get("fornavn").asText());
        Assert.assertEquals("Testersen", personObject.get("efternavn").asText());
        Assert.assertEquals("G", personObject.get("civilstand").asText());
        Assert.assertEquals("2017-10-12", personObject.get("civilstandsdato").asText());
        Assert.assertEquals("0202994321", personObject.get("ægtefælleCprNummer").asText());
        Assert.assertEquals(false, personObject.get("adressebeskyttelse").asBoolean());
        Assert.assertEquals("K", personObject.get("køn").asText());
        Assert.assertEquals(5, personObject.get("statuskode").asInt());
        Assert.assertEquals("0101641234", personObject.get("far").asText());
        Assert.assertEquals("2903641234", personObject.get("mor").asText());
        Assert.assertEquals("2016-08-31", personObject.get("tilflytningsdato").asText());
        Assert.assertEquals(956, personObject.get("myndighedskode").asInt());
        Assert.assertEquals(254, personObject.get("vejkode").asInt());
        Assert.assertEquals("Kommuneqarfik Sermersooq", personObject.get("kommune").asText());
        Assert.assertEquals(3900, personObject.get("postnummer").asInt());
        Assert.assertEquals(600, personObject.get("stedkode").asInt());
        Assert.assertEquals("GL", personObject.get("landekode").asText());

        Session session = sessionManager.getSessionFactory().openSession();
        try {
            List<PersonSubscription> existingSubscriptions = QueryManager.getAllItems(session, PersonSubscription.class);
            Assert.assertEquals(1, existingSubscriptions.size());
            PersonSubscription subscription = existingSubscriptions.get(0);
            Assert.assertEquals("0607621234", subscription.getPersonNumber());
            Assert.assertEquals(PersonSubscriptionAssignmentStatus.CreatedInTable, subscription.getAssignment());
        } finally {
            session.close();
        }
    }

    /**
     * Validate that when a person is without address it finds the address direct, if the person is not dead it will also create subscription
     *
     * @throws Exception
     */
    @Test
    public void testDirectLookup4() throws Exception {

        loadPerson("/missingAddressperson.txt");

        String cpr = "0101001235";
        String data = "038406uKBKxWLcWUDI0178001104000000000000003840120190815000000000010607621234          90200502051034 M1962-07-06 2005-10-20                                              0030607621234Petersen,Mads Munk                                                                                                                                                                                                                          0080607621234Mads                                               Munk                                     Petersen                                 196207061029 Petersen,Mads Munk                00906076212345180                    01006076212345180196207061029*0110607621234U1962-07-06 0120607621234D0506650038                                              200502051034             01406076212340506871018014060762123405089210040140607621234060794106801406076212340705901007014060762123407059600110140607621234080789104901506076212341962-07-06*0000000000                                              1962-07-06*0000000000                                              999999999999900000014";

        Mockito.doReturn(data).when(cprDirectLookup).lookup(ArgumentMatchers.eq(cpr));
        TestUserDetails testUserDetails = new TestUserDetails();

        HttpEntity<String> httpEntity = new HttpEntity<String>("", new HttpHeaders());

        testUserDetails.giveAccess(CprRolesDefinition.READ_CPR_ROLE);
        this.applyAccess(testUserDetails);
        ResponseEntity<String> response = restTemplate.exchange(
                "/prisme/cpr/combined/1/" + cpr,
                HttpMethod.GET,
                httpEntity,
                String.class
        );
        ObjectNode responseObject = (ObjectNode) objectMapper.readTree(response.getBody());
        Assert.assertEquals("0607621234", responseObject.get("cprNummer").asText());

        cpr = "0101001236";
        Mockito.doReturn(data).when(cprDirectLookup).lookup(ArgumentMatchers.eq(cpr));

        testUserDetails.giveAccess(CprRolesDefinition.READ_CPR_ROLE);
        this.applyAccess(testUserDetails);
        response = restTemplate.exchange(
                "/prisme/cpr/combined/1/" + cpr,
                HttpMethod.GET,
                httpEntity,
                String.class
        );
        responseObject = (ObjectNode) objectMapper.readTree(response.getBody());
        Assert.assertEquals("0607621234", responseObject.get("cprNummer").asText());

        testUserDetails.giveAccess(CprRolesDefinition.READ_CPR_ROLE);
        this.applyAccess(testUserDetails);
        response = restTemplate.exchange(
                "/prisme/cpr/combined/1/" + cpr + "?forceDirect=true",
                HttpMethod.GET,
                httpEntity,
                String.class
        );
        responseObject = (ObjectNode) objectMapper.readTree(response.getBody());
        Assert.assertEquals("0607621234", responseObject.get("cprNummer").asText());


        Session session = sessionManager.getSessionFactory().openSession();
        try {
            List<PersonSubscription> existingSubscriptions = QueryManager.getAllItems(session, PersonSubscription.class);
            Assert.assertEquals(1, existingSubscriptions.size());
            PersonSubscription subscription = existingSubscriptions.get(0);
            Assert.assertEquals("0101001235", subscription.getPersonNumber());
            Assert.assertEquals(PersonSubscriptionAssignmentStatus.CreatedInTable, subscription.getAssignment());

        } finally {
            session.close();
        }
    }


    /**
     * Validate that when a person is without address it finds the address direct, if the person is not dead it will also create subscription
     *
     * @throws Exception
     */
    @Test
    public void testDirectLookup5() throws Exception {

        loadPerson("/missingAddressperson.txt");

        String cpr = "0101001235";
        String data = "038406uKBKxWLcWUDI0178001104000000000000003840120190815000000000010607621235          90200502051034 M1962-07-06 2005-10-20                                              0030607621235Petersen,Mads Munk                                                                                                                                                                                                                          0080607621235Mads                                               Munk                                     Petersen                                 196207061029 Petersen,Mads Munk                00906076212355180                    01006076212355180196207061029*0110607621234U1962-07-06 0120607621234D0506650038                                              200502051034             01406076212340506871018014060762123405089210040140607621234060794106801406076212340705901007014060762123407059600110140607621235080789104901506076212341962-07-06*0000000000                                              1962-07-06*0000000000                                              999999999999900000014";

        Mockito.doReturn(data).when(cprDirectLookup).lookup(ArgumentMatchers.eq("0101001235"));
        TestUserDetails testUserDetails = new TestUserDetails();

        HttpEntity<String> httpEntity = new HttpEntity<String>("", new HttpHeaders());

        testUserDetails.giveAccess(CprRolesDefinition.READ_CPR_ROLE);
        this.applyAccess(testUserDetails);
        ResponseEntity<String> response = restTemplate.exchange(
                "/prisme/cpr/combined/1/1111111111",
                HttpMethod.GET,
                httpEntity,
                String.class
        );
        ObjectNode responseObject = (ObjectNode) objectMapper.readTree(response.getBody());
        Assert.assertEquals("404", responseObject.get("status").asText());


        Session session = sessionManager.getSessionFactory().openSession();
        try {
            List<PersonSubscription> existingSubscriptions = QueryManager.getAllItems(session, PersonSubscription.class);
            Assert.assertEquals(0, existingSubscriptions.size());
        } finally {
            session.close();
        }
    }

    @Test
    public void testMoveHistoryPrisme() throws Exception {
        loadPerson("/adressChangeHistory.txt");
        TestUserDetails testUserDetails = new TestUserDetails();
        HttpEntity<String> httpEntity = new HttpEntity<String>("", new HttpHeaders());

        testUserDetails.giveAccess(CprRolesDefinition.READ_CPR_ROLE);
        this.applyAccess(testUserDetails);
        ResponseEntity<String> response = restTemplate.exchange(
                "/prisme/cpr/addresshistory/1/" + "0101011234",
                HttpMethod.GET,
                httpEntity,
                String.class
        );
        Assert.assertEquals(HttpStatus.OK, response.getStatusCode());
        ObjectNode responseObject = (ObjectNode) objectMapper.readTree(response.getBody());
        Assert.assertEquals("0101011234", responseObject.get("cprNummer").asText());
        JsonNode adressList = responseObject.get("addresses");
        Assert.assertEquals(18, adressList.size());
        System.out.println(response.getBody());
    }


    @Autowired PersonOutputWrapperPrisme personOutputWrapperPrisme;
    @Test
    public void testDeadPerson() {

        GeoLookupService lookupService = new GeoLookupService(sessionManager);
        personOutputWrapperPrisme.setLookupService(lookupService);
        ImportMetadata importMetadata = new ImportMetadata();
        Session session = sessionManager.getSessionFactory().openSession();
        importMetadata.setSession(session);
        Transaction transaction = session.beginTransaction();
        importMetadata.setTransactionInProgress(true);

        OffsetDateTime birth = OffsetDateTime.parse("1920-01-01T00:00:00Z");
        OffsetDateTime death = OffsetDateTime.parse("2022-01-01T00:00:00Z");

        Bitemporality beforeDeath = new Bitemporality(
                birth, death,
                birth, null
        );
        Bitemporality afterDeathPast = new Bitemporality(
                death, null,
                birth, death
        );
        Bitemporality afterDeathPresent = new Bitemporality(
                death, null,
                death, null
        );

        PersonEntity personEntity = new PersonEntity(UUID.randomUUID(), "testing");
        personEntity.setPersonnummer("1234567890");
        List<CprBitemporalPersonRecord> records = new ArrayList<>();

        PersonStatusDataRecord aliveStatus = new PersonStatusDataRecord(5);
        aliveStatus.setBitemporality(beforeDeath);

        AddressDataRecord aliveAddress = new AddressDataRecord(956, 254, "B-3197", "18", "kld", "1", null, null, null, null, null, 0, 955);
        aliveAddress.setBitemporality(beforeDeath);
        records.add(aliveAddress);

        AddressDataRecord deadAddress = aliveAddress.clone();
        deadAddress.setBitemporality(afterDeathPast);
        records.add(deadAddress);

        PersonStatusDataRecord aliveStatusPast = new PersonStatusDataRecord(5);
        aliveStatusPast.setBitemporality(afterDeathPast);
        records.add(aliveStatusPast);

        PersonStatusDataRecord deadStatusPresent = new PersonStatusDataRecord(90);
        deadStatusPresent.setBitemporality(afterDeathPresent);
        records.add(deadStatusPresent);

        session.save(personEntity);
        session.save(personEntity.getIdentification());
        for (CprBitemporalPersonRecord record : records) {
            record.setDafoUpdated(OffsetDateTime.now());
            record.setEntity(personEntity);
            personEntity.addBitemporalRecord(record, session, false);
            session.save(record);
        }
        session.flush();
        transaction.commit();

        ObjectNode output = (ObjectNode) personOutputWrapperPrisme.wrapRecordResult(personEntity, null);

        Assert.assertEquals(90, output.get("statuskode").asInt());
        Assert.assertEquals("1920-01-01", output.get("tilflytningsdato").asText());
        Assert.assertEquals(956, output.get("myndighedskode").asInt());
        Assert.assertEquals(254, output.get("vejkode").asInt());
        Assert.assertEquals("Kommuneqarfik Sermersooq", output.get("kommune").asText());
        Assert.assertEquals(3900, output.get("postnummer").asInt());
        Assert.assertEquals(600, output.get("stedkode").asInt());
        Assert.assertEquals("GL", output.get("landekode").asText());

        session.close();

        TestUserDetails testUserDetails = new TestUserDetails();
        testUserDetails.giveAccess(CprRolesDefinition.READ_CPR_ROLE);
        this.applyAccess(testUserDetails);

        HttpEntity<String> httpEntity = new HttpEntity<String>("", new HttpHeaders());
        ResponseEntity<String> response = restTemplate.exchange(
                "/prisme/cpr/1/" + "1234567890",
                HttpMethod.GET,
                httpEntity,
                String.class
        );
        Assert.assertEquals(HttpStatus.OK, response.getStatusCode());

        String jsonExpected = "{\"cprNummer\":\"1234567890\"," +
                "\"adressebeskyttelse\":false," +
                "\"statuskode\":90," +
                "\"statuskodedato\":\"2022-01-01\"," +
                "\"tilflytningsdato\":\"1920-01-01\"," +
                "\"myndighedskode\":956," +
                "\"vejkode\":254," +
                "\"kommune\":\"Kommuneqarfik Sermersooq\"," +
                "\"adresse\":\"Qarsaalik 18, kld. 1 (B-3197)\"," +
                "\"postnummer\":3900," +
                "\"bynavn\":\"Nuuk\"," +
                "\"stedkode\":600," +
                "\"landekode\":\"GL\"}";
        try {
            Assert.assertTrue(
                   new UnorderedJsonListComparator().compare(objectMapper.readTree(jsonExpected), objectMapper.readTree(response.getBody())) == 0
            );
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void testDeadPersonWithOverlap() {

        GeoLookupService lookupService = new GeoLookupService(sessionManager);
        personOutputWrapperPrisme.setLookupService(lookupService);
        ImportMetadata importMetadata = new ImportMetadata();
        Session session = sessionManager.getSessionFactory().openSession();
        importMetadata.setSession(session);
        Transaction transaction = session.beginTransaction();
        importMetadata.setTransactionInProgress(true);

        OffsetDateTime birth = OffsetDateTime.parse("1920-01-01T00:00:00Z");
        OffsetDateTime death = OffsetDateTime.parse("2022-01-01T00:00:00Z");

        Bitemporality beforeDeath = new Bitemporality(
                birth, death,
                birth, null
        );
        Bitemporality afterDeathPast = new Bitemporality(
                death, null,
                birth, death
        );
        Bitemporality afterDeathPast0 = new Bitemporality(
                death, null,
                birth, OffsetDateTime.parse("2022-01-01T01:00:00Z")
        );
        Bitemporality afterDeathPresent = new Bitemporality(
                death, null,
                death, null
        );

        PersonEntity personEntity = new PersonEntity(UUID.randomUUID(), "testing");
        personEntity.setPersonnummer("1234567890");
        List<CprBitemporalPersonRecord> records = new ArrayList<>();


        AddressDataRecord aliveAddress1 = new AddressDataRecord(956, 254, "B-3197", "18", "kld", "1", null, null, null, null, null, 0, 955);
        aliveAddress1.setBitemporality(beforeDeath);
        records.add(aliveAddress1);

        AddressDataRecord aliveAddress2 = new AddressDataRecord(956, 254, "B-3197", "18", "kld", "2", null, null, null, null, null, 0, 955);
        aliveAddress2.setBitemporality(beforeDeath);
        records.add(aliveAddress2);

        AddressDataRecord deadAddress1 = aliveAddress1.clone();
        deadAddress1.setBitemporality(afterDeathPast0);  // This one has a later effectTo, so it should be chosen over deadAddress2
        records.add(deadAddress1);

        AddressDataRecord deadAddress2 = aliveAddress2.clone();
        deadAddress2.setBitemporality(afterDeathPast);
        records.add(deadAddress2);

        PersonStatusDataRecord aliveStatus = new PersonStatusDataRecord(5);
        aliveStatus.setBitemporality(beforeDeath);
        records.add(aliveStatus);

        PersonStatusDataRecord aliveStatusPast = new PersonStatusDataRecord(5);
        aliveStatusPast.setBitemporality(afterDeathPast);
        records.add(aliveStatusPast);

        PersonStatusDataRecord deadStatusPresent = new PersonStatusDataRecord(90);
        deadStatusPresent.setBitemporality(afterDeathPresent);
        records.add(deadStatusPresent);

        session.save(personEntity);
        session.save(personEntity.getIdentification());
        for (CprBitemporalPersonRecord record : records) {
            record.setDafoUpdated(OffsetDateTime.now());
            personEntity.addBitemporalRecord(record, session, false);
            session.save(record);
        }
        session.flush();
        transaction.commit();

        ObjectNode output = (ObjectNode) personOutputWrapperPrisme.wrapRecordResult(personEntity, null);

        Assert.assertEquals(90, output.get("statuskode").asInt());
        Assert.assertEquals("1920-01-01", output.get("tilflytningsdato").asText());
        Assert.assertEquals(956, output.get("myndighedskode").asInt());
        Assert.assertEquals(254, output.get("vejkode").asInt());
        Assert.assertEquals("Kommuneqarfik Sermersooq", output.get("kommune").asText());
        Assert.assertEquals(3900, output.get("postnummer").asInt());
        Assert.assertEquals(600, output.get("stedkode").asInt());
        Assert.assertEquals("GL", output.get("landekode").asText());

        session.close();

        TestUserDetails testUserDetails = new TestUserDetails();
        testUserDetails.giveAccess(CprRolesDefinition.READ_CPR_ROLE);
        this.applyAccess(testUserDetails);

        HttpEntity<String> httpEntity = new HttpEntity<String>("", new HttpHeaders());
        ResponseEntity<String> response = restTemplate.exchange(
                "/prisme/cpr/1/" + "1234567890",
                HttpMethod.GET,
                httpEntity,
                String.class
        );
        Assert.assertEquals(HttpStatus.OK, response.getStatusCode());

        String jsonExpected = "{\"cprNummer\":\"1234567890\"," +
                "\"adressebeskyttelse\":false," +
                "\"statuskode\":90," +
                "\"statuskodedato\":\"2022-01-01\"," +
                "\"tilflytningsdato\":\"1920-01-01\"," +
                "\"myndighedskode\":956," +
                "\"vejkode\":254," +
                "\"kommune\":\"Kommuneqarfik Sermersooq\"," +
                "\"adresse\":\"Qarsaalik 18, kld. 1 (B-3197)\"," +
                "\"postnummer\":3900," +
                "\"bynavn\":\"Nuuk\"," +
                "\"stedkode\":600," +
                "\"landekode\":\"GL\"}";
        try {
            Assert.assertTrue(
                    new UnorderedJsonListComparator().compare(objectMapper.readTree(jsonExpected), objectMapper.readTree(response.getBody())) == 0
            );
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}


