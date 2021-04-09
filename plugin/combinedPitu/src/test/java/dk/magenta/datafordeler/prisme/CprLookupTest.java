package dk.magenta.datafordeler.prisme;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import dk.magenta.datafordeler.core.Application;
import dk.magenta.datafordeler.core.database.QueryManager;
import dk.magenta.datafordeler.core.database.SessionManager;
import dk.magenta.datafordeler.core.exception.DataFordelerException;
import dk.magenta.datafordeler.core.io.ImportMetadata;
import dk.magenta.datafordeler.core.user.DafoUserManager;
import dk.magenta.datafordeler.core.util.InputStreamReader;
import dk.magenta.datafordeler.cpr.CprPlugin;
import dk.magenta.datafordeler.cpr.CprRolesDefinition;
import dk.magenta.datafordeler.cpr.data.person.PersonEntityManager;
import dk.magenta.datafordeler.cpr.data.person.PersonSubscription;
import dk.magenta.datafordeler.cpr.data.person.PersonSubscriptionAssignmentStatus;
import dk.magenta.datafordeler.cpr.direct.CprDirectLookup;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.junit.*;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.StringJoiner;
import static org.mockito.Mockito.when;


@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = Application.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class CprLookupTest extends TestBase {

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

    @After
    public void cleanup() {
        this.cleanupPersonData(sessionManager);
        this.cleanupGeoData(sessionManager);
    }

    public void loadPerson(String personfile) throws Exception {
        InputStream testData = CprLookupTest.class.getResourceAsStream(personfile);
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
        String testData = InputStreamReader.readInputStream(CprLookupTest.class.getResourceAsStream("/person.txt"));
        String[] lines = testData.split("\n");
        for (int i = start; i < count + start; i++) {
            StringJoiner sb = new StringJoiner("\n");
            String newCpr = String.format("%010d", i);
            for (int j = 0; j < lines.length; j++) {
                String line = lines[j];
                line = line.substring(0, 3) + newCpr + line.substring(13);
                sb.add(line);
            }
            ByteArrayInputStream bais = new ByteArrayInputStream(sb.toString().getBytes("UTF-8"));
            personEntityManager.parseData(bais, importMetadata);
            bais.close();
        }
        transaction.commit();
        session.close();
    }


    @Before
    public void load() throws Exception {
        this.loadAllGeoAdress(sessionManager);
        this.loadPerson("/person.txt");
        loadManyPersons(50);
    }

    @Test
    public void test2PersonPrisme() throws Exception {
        loadPerson("/person.txt");

        HttpEntity<String> httpEntity = new HttpEntity<String>("", new HttpHeaders());
        ResponseEntity<String> response = restTemplate.exchange(
                "/combined/cpr/1/" + "0101001234",
                HttpMethod.GET,
                httpEntity,
                String.class
        );
        Assert.assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }


    @Test
    public void test3PersonPrisme() throws Exception {


        TestUserDetails testUserDetails = new TestUserDetails();

        HttpEntity<String> httpEntity = new HttpEntity<String>("", new HttpHeaders());

        testUserDetails.giveAccess(CprRolesDefinition.READ_CPR_ROLE);
        this.applyAccess(testUserDetails);




        ResponseEntity<String> response = restTemplate.exchange(
                "/combinedPersonLookup/1/cpr/?cpr=" + "0000000000,0000000001,0000000002,0000000003,0000000004,0000000005,0000000006,0000000007,0101001234,1111111111",
                HttpMethod.GET,
                httpEntity,
                String.class
        );

        /*ResponseEntity<String> response = restTemplate.exchange(
                "/combinedPersonLookup/1/cpr/?cpr=" + "1111111111,1111111112",
                HttpMethod.GET,
                httpEntity,
                String.class
        );*/
        Assert.assertEquals(HttpStatus.OK, response.getStatusCode());



        ResponseEntity<String> response1 = restTemplate.exchange(
                "/combinedPersonLookup/1/cpr/0101001234",
                HttpMethod.GET,
                httpEntity,
                String.class
        );

        /*ResponseEntity<String> response = restTemplate.exchange(
                "/combinedPersonLookup/1/cpr/?cpr=" + "1111111111,1111111112",
                HttpMethod.GET,
                httpEntity,
                String.class
        );*/
        Assert.assertEquals(HttpStatus.OK, response1.getStatusCode());








        //Assert.assertTrue(objectMapper.readTree(response.getBody()).size() > 0);


        System.out.println(response1.getBody());



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
                "/combined/cpr/1/" + cpr,
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
                "/combined/cpr/1/" + cpr,
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
                "/combined/cpr/1/",
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
     * Validate that when a person is without address it finds the address direct, if the person is not dead it will also create subscribtion
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
                "/combined/cpr/1/" + cpr,
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
                "/combined/cpr/1/" + cpr,
                HttpMethod.GET,
                httpEntity,
                String.class
        );
        responseObject = (ObjectNode) objectMapper.readTree(response.getBody());
        Assert.assertEquals("0607621234", responseObject.get("cprNummer").asText());

        testUserDetails.giveAccess(CprRolesDefinition.READ_CPR_ROLE);
        this.applyAccess(testUserDetails);
        response = restTemplate.exchange(
                "/combined/cpr/1/" + cpr + "?forceDirect=true",
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
     * Validate that when a person is without address it finds the address direct, if the person is not dead it will also create subscribtion
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
                "/combined/cpr/1/1111111111",
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

}
