package dk.magenta.datafordeler.combined;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import dk.magenta.datafordeler.core.Application;
import dk.magenta.datafordeler.core.database.SessionManager;
import dk.magenta.datafordeler.core.io.ImportMetadata;
import dk.magenta.datafordeler.core.user.DafoUserManager;
import dk.magenta.datafordeler.core.util.InputStreamReader;
import dk.magenta.datafordeler.cpr.CprRolesDefinition;
import dk.magenta.datafordeler.cpr.data.person.PersonEntityManager;
import dk.magenta.datafordeler.cpr.direct.CprDirectLookup;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.junit.*;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;
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
    public void testNoAccessLookup() throws IOException {
        TestUserDetails testUserDetails = new TestUserDetails();
        HttpEntity<String> httpEntity = new HttpEntity<String>("", new HttpHeaders());
        this.applyAccess(testUserDetails);

        ResponseEntity<String> response = restTemplate.exchange(
                "/combined/personLookup/1/cpr/0000000007",
                HttpMethod.GET,
                httpEntity,
                String.class
        );
        Assert.assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        Assert.assertEquals(null, objectMapper.readTree(response.getBody()).get("cprNummer"));

        response = restTemplate.exchange(
                "/combined/personLookup/1/cpr/?cpr=0000000007",
                HttpMethod.GET,
                httpEntity,
                String.class
        );
        Assert.assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());

    }

    @Test
    public void testSinglePersonDBLookup() throws IOException {
        TestUserDetails testUserDetails = new TestUserDetails();
        HttpEntity<String> httpEntity = new HttpEntity<String>("", new HttpHeaders());
        testUserDetails.giveAccess(CprRolesDefinition.READ_CPR_ROLE);
        this.applyAccess(testUserDetails);

        ResponseEntity<String> response = restTemplate.exchange(
                "/combined/personLookup/1/cpr/0000000007",
                HttpMethod.GET,
                httpEntity,
                String.class
        );
        Assert.assertEquals(HttpStatus.OK, response.getStatusCode());
        Assert.assertEquals("0000000007", objectMapper.readTree(response.getBody()).get("cprNummer").asText());

        response = restTemplate.exchange(
                "/combined/personLookup/1/cpr/0101001234",
                HttpMethod.GET,
                httpEntity,
                String.class
        );
        Assert.assertEquals(HttpStatus.OK, response.getStatusCode());
        Assert.assertEquals("0101001234", objectMapper.readTree(response.getBody()).get("cprNummer").asText());

        response = restTemplate.exchange(
                "/combined/personLookup/1/cpr/1111111188",
                HttpMethod.GET,
                httpEntity,
                String.class
        );
        Assert.assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    public void testPersonInterval() throws Exception {
        this.loadPerson("/different_persons.txt");

        HttpEntity<String> httpEntity = new HttpEntity<String>("", new HttpHeaders());
        ResponseEntity<String> response = restTemplate.exchange(
                "/combined/cpr/birthIntervalDate/1/search/?birthAfter=1990-01-01&birthBefore=2041-01-01&lokalitet_kode=0601&pageSize=1000",
                HttpMethod.GET,
                httpEntity,
                String.class
        );
        Assert.assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());

        TestUserDetails testUserDetails = new TestUserDetails();
        httpEntity = new HttpEntity<String>("", new HttpHeaders());
        testUserDetails.giveAccess(CprRolesDefinition.READ_CPR_ROLE);
        this.applyAccess(testUserDetails);
        //"municipalitycode"
        //"localitycode"
        response = restTemplate.exchange(
                "/combined/cpr/birthIntervalDate/1/search/?birthAfter=1990-01-01&birthBefore=2041-01-01&lokalitet_kode=0601&pageSize=1000",
                HttpMethod.GET,
                httpEntity,
                String.class
        );

        JsonNode jsonNode = objectMapper.readTree(response.getBody());
        ArrayNode resultList = (ArrayNode)jsonNode.get("results");
        Assert.assertEquals(3, resultList.size());
        Assert.assertEquals(HttpStatus.OK, response.getStatusCode());

        response = restTemplate.exchange(
                "/combined/cpr/birthIntervalDate/1/search/?birthAfter=1990-01-01&birthBefore=2041-01-01&lokalitet_kode=0600&pageSize=1000",
                HttpMethod.GET,
                httpEntity,
                String.class
        );

        jsonNode = objectMapper.readTree(response.getBody());
        resultList = (ArrayNode)jsonNode.get("results");
        Assert.assertEquals(51, resultList.size());
        Assert.assertEquals(HttpStatus.OK, response.getStatusCode());

        response = restTemplate.exchange(
                "/combined/cpr/birthIntervalDate/1/search/?birthAfter=1990-01-01&birthBefore=2041-01-01&kommune_kode=957&pageSize=1000",
                HttpMethod.GET,
                httpEntity,
                String.class
        );

        jsonNode = objectMapper.readTree(response.getBody());
        resultList = (ArrayNode)jsonNode.get("results");
        Assert.assertEquals(0, resultList.size());
        Assert.assertEquals(HttpStatus.OK, response.getStatusCode());

        response = restTemplate.exchange(
                "/combined/cpr/birthIntervalDate/1/search/?birthAfter=1990-01-01&birthBefore=2041-01-01&kommune_kode=956&pageSize=1000",
                HttpMethod.GET,
                httpEntity,
                String.class
        );

        jsonNode = objectMapper.readTree(response.getBody());
        resultList = (ArrayNode)jsonNode.get("results");
        Assert.assertEquals(54, resultList.size());
        Assert.assertEquals(HttpStatus.OK, response.getStatusCode());

        response = restTemplate.exchange(
                "/combined/cpr/birthIntervalDate/1/search/?birthAfter=2010-01-01&birthBefore=2012-01-01&kommune_kode=956&pageSize=1000",
                HttpMethod.GET,
                httpEntity,
                String.class
        );

        jsonNode = objectMapper.readTree(response.getBody());
        resultList = (ArrayNode)jsonNode.get("results");
        Assert.assertEquals(3, resultList.size());
        Assert.assertEquals(HttpStatus.OK, response.getStatusCode());

        response = restTemplate.exchange(
                "/combined/cpr/birthIntervalDate/1/search/?birthAfter=2012-01-01&birthBefore=2014-01-01&kommune_kode=956&pageSize=1000",
                HttpMethod.GET,
                httpEntity,
                String.class
        );

        jsonNode = objectMapper.readTree(response.getBody());
        resultList = (ArrayNode)jsonNode.get("results");
        Assert.assertEquals(0, resultList.size());
        Assert.assertEquals(HttpStatus.OK, response.getStatusCode());
    }



    @Test
    public void testSinglePersonAllowDirectLookup() throws Exception {
        String cpr = "1111111188";
        String data = "038406fJrr7CCxWUDI0178001590000000000000003840120190808000000000011111111188          01000000000000 M1961-07-07 1961-07-07*           Socialrådg.                       002111111118809560254018 01  mf                                      198010102000 196107071034 0000000000000000                                                                                                                                                                                                   0031111111188Mortensen,Jens                                                                                        Boulevarden 101,1 mf                                                6800Varde               05735731101 01  mf    Boulevarden         0081111111188Jens                                                                                        Mortensen                                196107072000 Mortensen,Jens                    00911111111885150                    01011111111885100199103201299*0111111111188F1961-07-07*0121111111188F0706611234                                              198010012000             014111111118813018140770141111111188131281123401511111111881961-07-07*0912414434                                              1961-07-07*0909414385                                              01711111111882019-04-10*          0002                    Terd                              2019-04-10grd                                                                                                                                                                       999999999999900000012";
        Mockito.doReturn(data).when(cprDirectLookup).lookup(ArgumentMatchers.eq(cpr));
        TestUserDetails testUserDetails = new TestUserDetails();

        HttpEntity<String> httpEntity = new HttpEntity<String>("", new HttpHeaders());
        testUserDetails.giveAccess(CprRolesDefinition.READ_CPR_ROLE);
        this.applyAccess(testUserDetails);

        ResponseEntity<String> response = restTemplate.exchange(
                "/combined/personLookup/1/cpr/0000000007?allowDirect=true",
                HttpMethod.GET,
                httpEntity,
                String.class
        );
        Assert.assertEquals(HttpStatus.OK, response.getStatusCode());
        Assert.assertEquals("0000000007", objectMapper.readTree(response.getBody()).get("cprNummer").asText());

        response = restTemplate.exchange(
                "/combined/personLookup/1/cpr/0101001234?allowDirect=true",
                HttpMethod.GET,
                httpEntity,
                String.class
        );
        Assert.assertEquals(HttpStatus.OK, response.getStatusCode());
        Assert.assertEquals("0101001234", objectMapper.readTree(response.getBody()).get("cprNummer").asText());

        response = restTemplate.exchange(
                "/combined/personLookup/1/cpr/1111111188?allowDirect=true",
                HttpMethod.GET,
                httpEntity,
                String.class
        );
        Assert.assertEquals(HttpStatus.OK, response.getStatusCode());
        Assert.assertEquals("1111111188", objectMapper.readTree(response.getBody()).get("cprNummer").asText());
    }

    @Test
    public void testSinglePersonForceDirectLookup() throws Exception {
        String cpr = "1111111188";
        String data = "038406fJrr7CCxWUDI0178001590000000000000003840120190808000000000011111111188          01000000000000 M1961-07-07 1961-07-07*           Socialrådg.                       002111111118809560254018 01  mf                                      198010102000 196107071034 0000000000000000                                                                                                                                                                                                   0031111111188Mortensen,Jens                                                                                        Boulevarden 101,1 mf                                                6800Varde               05735731101 01  mf    Boulevarden         0081111111188Jens                                                                                        Mortensen                                196107072000 Mortensen,Jens                    00911111111885150                    01011111111885100199103201299*0111111111188F1961-07-07*0121111111188F0706611234                                              198010012000             014111111118813018140770141111111188131281123401511111111881961-07-07*0912414434                                              1961-07-07*0909414385                                              01711111111882019-04-10*          0002                    Terd                              2019-04-10grd                                                                                                                                                                       999999999999900000012";
        Mockito.doReturn(data).when(cprDirectLookup).lookup(ArgumentMatchers.eq(cpr));
        TestUserDetails testUserDetails = new TestUserDetails();

        HttpEntity<String> httpEntity = new HttpEntity<String>("", new HttpHeaders());
        testUserDetails.giveAccess(CprRolesDefinition.READ_CPR_ROLE);
        this.applyAccess(testUserDetails);

        ResponseEntity<String> response = restTemplate.exchange(
                "/combined/personLookup/1/cpr/0000000007?forceDirect=true",
                HttpMethod.GET,
                httpEntity,
                String.class
        );
        Assert.assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());

        response = restTemplate.exchange(
                "/combined/personLookup/1/cpr/0101001234?forceDirect=true",
                HttpMethod.GET,
                httpEntity,
                String.class
        );
        Assert.assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());

        response = restTemplate.exchange(
                "/combined/personLookup/1/cpr/1111111188?forceDirect=true",
                HttpMethod.GET,
                httpEntity,
                String.class
        );
        Assert.assertEquals(HttpStatus.OK, response.getStatusCode());
        Assert.assertEquals("1111111188", objectMapper.readTree(response.getBody()).get("cprNummer").asText());
    }

    @Test
    public void testFamilyRelation() throws Exception {
        this.loadPerson("/familyTestData.txt");

        TestUserDetails testUserDetails = new TestUserDetails();
        HttpEntity<String> httpEntity = new HttpEntity<String>("", new HttpHeaders());
        this.applyAccess(testUserDetails);
        ResponseEntity<String> response = restTemplate.exchange(
                "/combined/familyRelation/1/cpr/0101001234",
                HttpMethod.GET,
                httpEntity,
                String.class
        );
        Assert.assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        Assert.assertTrue(response.getBody().contains("error"));

        testUserDetails.giveAccess(CprRolesDefinition.READ_CPR_ROLE);
        this.applyAccess(testUserDetails);
        response = restTemplate.exchange(
                "/combined/familyRelation/1/cpr/0101001234",
                HttpMethod.GET,
                httpEntity,
                String.class
        );
        Assert.assertEquals(HttpStatus.OK, response.getStatusCode());
        JSONAssert.assertEquals("{\"person\":{\"cprNummer\":\"0101001234\",\"fornavn\":\"Tester Testmember\"," +
                "\"efternavn\":\"Testersen\",\"adresse\":{\"tilflytningsdato\":\"2016-08-31\",\"myndighedskode\":956," +
                "\"vejkode\":254,\"kommune\":\"Kommuneqarfik Sermersooq\",\"adresse\":\"Mut aqqut 18, 1. tv (B-3197)\"," +
                "\"postnummer\":0,\"bynavn\":null,\"stedkode\":600,\"landekode\":\"GL\"}},\"far\":{},\"mor\":{}," +
                "\"soeskende\":[{\"cprNummer\":\"0000000040\",\"fornavn\":\"Tester Testmember\",\"efternavn\":\"Testersen\"}]}", response.getBody(), JSONCompareMode.LENIENT);
    }

    @Test
    public void testListPersonDBLookup() throws IOException {
        TestUserDetails testUserDetails = new TestUserDetails();
        HttpEntity<String> httpEntity = new HttpEntity<String>("", new HttpHeaders());
        testUserDetails.giveAccess(CprRolesDefinition.READ_CPR_ROLE);
        this.applyAccess(testUserDetails);

        ResponseEntity<String> response = restTemplate.exchange(
                "/combined/personLookup/1/cpr/?cpr=0000000001,0000000002,0000000003,0000000004,0000000005,0000000006,0000000007,1000000007,1111111188",
                HttpMethod.GET,
                httpEntity,
                String.class
        );
        Assert.assertEquals(HttpStatus.OK, response.getStatusCode());
        Assert.assertEquals(7, objectMapper.readTree(response.getBody()).size());
        Assert.assertNotNull(objectMapper.readTree(response.getBody()).get("0000000001"));
        Assert.assertNotNull(objectMapper.readTree(response.getBody()).get("0000000002"));
        Assert.assertNotNull(objectMapper.readTree(response.getBody()).get("0000000003"));
        Assert.assertNotNull(objectMapper.readTree(response.getBody()).get("0000000004"));
        Assert.assertNotNull(objectMapper.readTree(response.getBody()).get("0000000005"));
        Assert.assertNotNull(objectMapper.readTree(response.getBody()).get("0000000006"));
        Assert.assertNotNull(objectMapper.readTree(response.getBody()).get("0000000007"));
        Assert.assertNull(objectMapper.readTree(response.getBody()).get("1000000007"));
        Assert.assertNull(objectMapper.readTree(response.getBody()).get("1111111188"));
    }

    @Test
    public void testListPersonAllowDirectLookup() throws Exception {
        String cpr = "1111111188";
        String data = "038406fJrr7CCxWUDI0178001590000000000000003840120190808000000000011111111188          01000000000000 M1961-07-07 1961-07-07*           Socialrådg.                       002111111118809560254018 01  mf                                      198010102000 196107071034 0000000000000000                                                                                                                                                                                                   0031111111188Mortensen,Jens                                                                                        Boulevarden 101,1 mf                                                6800Varde               05735731101 01  mf    Boulevarden         0081111111188Jens                                                                                        Mortensen                                196107072000 Mortensen,Jens                    00911111111885150                    01011111111885100199103201299*0111111111188F1961-07-07*0121111111188F0706611234                                              198010012000             014111111118813018140770141111111188131281123401511111111881961-07-07*0912414434                                              1961-07-07*0909414385                                              01711111111882019-04-10*          0002                    Terd                              2019-04-10grd                                                                                                                                                                       999999999999900000012";
        Mockito.doReturn(data).when(cprDirectLookup).lookup(ArgumentMatchers.eq(cpr));
        TestUserDetails testUserDetails = new TestUserDetails();
        HttpEntity<String> httpEntity = new HttpEntity<String>("", new HttpHeaders());
        testUserDetails.giveAccess(CprRolesDefinition.READ_CPR_ROLE);
        this.applyAccess(testUserDetails);

        ResponseEntity<String> response = restTemplate.exchange(
                "/combined/personLookup/1/cpr/?cpr=0000000001,0000000002,0000000003,0000000004,0000000005,0000000006,0000000007,1000000007,1111111188&allowDirect=true",
                HttpMethod.GET,
                httpEntity,
                String.class
        );
        Assert.assertEquals(HttpStatus.OK, response.getStatusCode());
        Assert.assertEquals(8, objectMapper.readTree(response.getBody()).size());
        Assert.assertNotNull(objectMapper.readTree(response.getBody()).get("0000000001"));
        Assert.assertNotNull(objectMapper.readTree(response.getBody()).get("0000000002"));
        Assert.assertNotNull(objectMapper.readTree(response.getBody()).get("0000000003"));
        Assert.assertNotNull(objectMapper.readTree(response.getBody()).get("0000000004"));
        Assert.assertNotNull(objectMapper.readTree(response.getBody()).get("0000000005"));
        Assert.assertNotNull(objectMapper.readTree(response.getBody()).get("0000000006"));
        Assert.assertNotNull(objectMapper.readTree(response.getBody()).get("0000000007"));
        Assert.assertNull(objectMapper.readTree(response.getBody()).get("1000000007"));
        Assert.assertNotNull(objectMapper.readTree(response.getBody()).get("1111111188"));
    }

    private void applyAccess(TestUserDetails testUserDetails) {
        when(dafoUserManager.getFallbackUser()).thenReturn(testUserDetails);
    }

    @SpyBean
    private CprDirectLookup cprDirectLookup;
}
