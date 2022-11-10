package dk.magenta.datafordeler.cpr;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import dk.magenta.datafordeler.core.Application;
import dk.magenta.datafordeler.core.arearestriction.AreaRestriction;
import dk.magenta.datafordeler.core.database.QueryManager;
import dk.magenta.datafordeler.core.database.SessionManager;
import dk.magenta.datafordeler.core.fapi.ParameterMap;
import dk.magenta.datafordeler.core.io.ImportMetadata;
import dk.magenta.datafordeler.core.user.DafoUserManager;
import dk.magenta.datafordeler.cpr.data.person.PersonEntityManager;
import dk.magenta.datafordeler.cpr.data.residence.ResidenceEntityManager;
import dk.magenta.datafordeler.cpr.data.road.RoadEntityManager;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.InputStream;
import java.time.OffsetDateTime;
import java.util.Collections;

import static org.mockito.Mockito.when;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = Application.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_CLASS)
public class QueryTest {

    @Autowired
    private CprPlugin plugin;

    @SpyBean
    private DafoUserManager dafoUserManager;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private PersonEntityManager personEntityManager;

    @Autowired
    private RoadEntityManager roadEntityManager;

    @Autowired
    private ResidenceEntityManager residenceEntityManager;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private SessionManager sessionManager;

    @Before
    public void initialize() throws Exception {
        QueryManager.clearCaches();
    }

    @After
    public void cleanup() {
        QueryManager.clearCaches();
        residenceEntityManager = new ResidenceEntityManager();
        personEntityManager = new PersonEntityManager();
    }

    public void loadPerson(ImportMetadata importMetadata) throws Exception {
        InputStream testData = QueryTest.class.getResourceAsStream("/persondata.txt");
        personEntityManager.parseData(testData, importMetadata);
        testData.close();
    }

    public void loadResidence(ImportMetadata importMetadata) throws Exception {
        InputStream testData = QueryTest.class.getResourceAsStream("/roaddata.txt");
        residenceEntityManager.parseData(testData, importMetadata);
        testData.close();
    }

    public void loadRoad(ImportMetadata importMetadata) throws Exception {
        InputStream testData = QueryTest.class.getResourceAsStream("/roaddata.txt");
        roadEntityManager.parseData(testData, importMetadata);
        testData.close();
    }

    private void applyAccess(TestUserDetails testUserDetails) {
        when(dafoUserManager.getFallbackUser()).thenReturn(testUserDetails);
    }

    private void whitelistLocalhost() {
        when(dafoUserManager.getIpWhitelist()).thenReturn(Collections.singleton("127.0.0.1"));
    }

    private ResponseEntity<String> restSearch(ParameterMap parameters, String type) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", "application/json");
        HttpEntity<String> httpEntity = new HttpEntity<String>("", headers);
        return this.restTemplate.exchange("/cpr/" + type + "/1/rest/search?" + parameters.asUrlParams(), HttpMethod.GET, httpEntity, String.class);
    }

    private ResponseEntity<String> uuidSearch(String id, String type) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", "application/json");
        HttpEntity<String> httpEntity = new HttpEntity<String>("", headers);
        return this.restTemplate.exchange("/cpr/" + type + "/1/rest/" + id, HttpMethod.GET, httpEntity, String.class);
    }


    @Test
    public void testPersonAccess() throws Exception {
        whitelistLocalhost();
        ImportMetadata importMetadata = new ImportMetadata();
        Session session = sessionManager.getSessionFactory().openSession();
        importMetadata.setSession(session);
        Transaction transaction = session.beginTransaction();
        importMetadata.setTransactionInProgress(true);
        loadPerson(importMetadata);
        transaction.commit();
        session.close();

        TestUserDetails testUserDetails = new TestUserDetails();

        ParameterMap searchParameters = new ParameterMap();
        searchParameters.add("fornavn", "Tester");
        ResponseEntity<String> response = restSearch(searchParameters, "person");
        Assert.assertEquals(403, response.getStatusCode().value());

        testUserDetails.giveAccess(CprRolesDefinition.READ_CPR_ROLE);
        this.applyAccess(testUserDetails);

        response = restSearch(searchParameters, "person");
        Assert.assertEquals(200, response.getStatusCode().value());
        JsonNode jsonBody = objectMapper.readTree(response.getBody());
        JsonNode results = jsonBody.get("results");
        Assert.assertTrue(results.isArray());
        Assert.assertEquals(1, results.size());
        Assert.assertEquals("4ccc3b64-1779-38f2-a96c-458e541a010d", results.get(0).get("uuid").asText());

        searchParameters.add("registrationFromBefore", "ALWAYS");
        searchParameters.add("registrationToAfter", "ALWAYS");
        searchParameters.add("effectFromBefore", "ALWAYS");
        searchParameters.add("effectToAfter", "ALWAYS");
        response = restSearch(searchParameters, "person");
        Assert.assertEquals(200, response.getStatusCode().value());


        testUserDetails.giveAccess(
                plugin.getAreaRestrictionDefinition().getAreaRestrictionTypeByName(
                        CprAreaRestrictionDefinition.RESTRICTIONTYPE_KOMMUNEKODER
                ).getRestriction(
                        CprAreaRestrictionDefinition.RESTRICTION_KOMMUNE_SERMERSOOQ
                )
        );
        this.applyAccess(testUserDetails);

        response = restSearch(searchParameters, "person");
        Assert.assertEquals(200, response.getStatusCode().value());
        jsonBody = objectMapper.readTree(response.getBody());
        results = jsonBody.get("results");
        System.out.println(objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(results));
        Assert.assertTrue(results.isArray());
        Assert.assertEquals(0, results.size());

        response = uuidSearch("4ccc3b64-1779-38f2-a96c-458e541a010d", "person");
        Assert.assertEquals(200, response.getStatusCode().value());
        jsonBody = objectMapper.readTree(response.getBody());
        results = jsonBody.get("results");
        Assert.assertTrue(results.isArray());
        Assert.assertEquals(0, results.size());

        searchParameters.add("kommunekode", "95*");
        response = restSearch(searchParameters, "person");
        Assert.assertEquals(200, response.getStatusCode().value());
        jsonBody = objectMapper.readTree(response.getBody());
        results = jsonBody.get("results");
        Assert.assertTrue(results.isArray());
        Assert.assertEquals(0, results.size());

        testUserDetails.giveAccess(
                plugin.getAreaRestrictionDefinition().getAreaRestrictionTypeByName(
                        CprAreaRestrictionDefinition.RESTRICTIONTYPE_KOMMUNEKODER
                ).getRestriction(
                        CprAreaRestrictionDefinition.RESTRICTION_KOMMUNE_QAASUITSUP
                )
        );
        this.applyAccess(testUserDetails);

        response = restSearch(searchParameters, "person");
        Assert.assertEquals(200, response.getStatusCode().value());
        jsonBody = objectMapper.readTree(response.getBody());
        results = jsonBody.get("results");
        Assert.assertTrue(results.isArray());
        Assert.assertEquals(1, results.size());
        Assert.assertEquals("4ccc3b64-1779-38f2-a96c-458e541a010d", results.get(0).get("uuid").asText());

        response = uuidSearch("4ccc3b64-1779-38f2-a96c-458e541a010d", "person");
        Assert.assertEquals(200, response.getStatusCode().value());
        jsonBody = objectMapper.readTree(response.getBody());
        results = jsonBody.get("results");
        Assert.assertTrue(results.isArray());
        Assert.assertEquals(1, results.size());
        Assert.assertEquals("4ccc3b64-1779-38f2-a96c-458e541a010d", results.get(0).get("uuid").asText());
    }

    @Test
    public void testPersonRecordTime() throws Exception {
        whitelistLocalhost();
        OffsetDateTime now = OffsetDateTime.now();
        ImportMetadata importMetadata = new ImportMetadata();
        Session session = sessionManager.getSessionFactory().openSession();
        importMetadata.setSession(session);
        Transaction transaction = session.beginTransaction();
        importMetadata.setTransactionInProgress(true);
        loadPerson(importMetadata);
        transaction.commit();
        session.close();

        TestUserDetails testUserDetails = new TestUserDetails();
        testUserDetails.giveAccess(CprRolesDefinition.READ_CPR_ROLE);
        this.applyAccess(testUserDetails);

        ParameterMap searchParameters = new ParameterMap();
        searchParameters.add("registreringFra", "2011-06-17T14:06:19.196");
        searchParameters.add("recordAfter", "2011-06-17T14:06:24.196");

        ResponseEntity<String> response = restSearch(searchParameters, "person");
        Assert.assertEquals(200, response.getStatusCode().value());
        JsonNode jsonBody = objectMapper.readTree(response.getBody());
        JsonNode results = jsonBody.get("results");
        Assert.assertTrue(results.isArray());

        Assert.assertEquals(1, results.size());
        Assert.assertEquals(8, results.get(0).size());

        searchParameters = new ParameterMap();
        searchParameters.add("registreringFraFør", "2011-06-17T14:06:19.196");
        searchParameters.add("recordAfter", "2011-06-16T14:06:19.196");
        searchParameters.add("fornavn", "Tester");
        searchParameters.add("fmt", "drv");

        response = restSearch(searchParameters, "person");
        Assert.assertEquals(200, response.getStatusCode().value());
        jsonBody = objectMapper.readTree(response.getBody());
        results = jsonBody.get("results");
        System.out.println(objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(results));
        Assert.assertTrue(results.isArray());
        Assert.assertEquals(1, results.size());
    }

    @Test
    public void testPersonDataOnly() throws Exception {
        whitelistLocalhost();
        OffsetDateTime now = OffsetDateTime.now();
        ImportMetadata importMetadata = new ImportMetadata();
        Session session = sessionManager.getSessionFactory().openSession();
        importMetadata.setSession(session);
        Transaction transaction = session.beginTransaction();
        importMetadata.setTransactionInProgress(true);
        loadPerson(importMetadata);
        transaction.commit();
        session.close();

        TestUserDetails testUserDetails = new TestUserDetails();
        testUserDetails.giveAccess(CprRolesDefinition.READ_CPR_ROLE);
        this.applyAccess(testUserDetails);

        ParameterMap searchParameters = new ParameterMap();
        searchParameters.add("fornavn", "Tester");
        searchParameters.add("fmt", "rvd");

        ResponseEntity<String> response = restSearch(searchParameters, "person");
        Assert.assertEquals(200, response.getStatusCode().value());
        JsonNode jsonBody = objectMapper.readTree(response.getBody());
        JsonNode results = jsonBody.get("results");

        JsonNode firstElement = results.get(0);
        JsonNode registreringer = firstElement.get("registreringer");
        Assert.assertNotNull(registreringer);

        searchParameters.replace("fmt", "dataonly");

        response = restSearch(searchParameters, "person");
        Assert.assertEquals(200, response.getStatusCode().value());
        jsonBody = objectMapper.readTree(response.getBody());
        results = jsonBody.get("results");

        firstElement = results.get(0);
        registreringer = firstElement.get("registreringer");
        Assert.assertNull(registreringer);
    }


    @Test
    public void testPersonGenericSearchParameters() throws Exception {
        whitelistLocalhost();
        OffsetDateTime now = OffsetDateTime.now();
        ImportMetadata importMetadata = new ImportMetadata();
        Session session = sessionManager.getSessionFactory().openSession();
        importMetadata.setSession(session);
        Transaction transaction = session.beginTransaction();
        importMetadata.setTransactionInProgress(true);
        loadPerson(importMetadata);
        transaction.commit();
        session.close();

        TestUserDetails testUserDetails = new TestUserDetails();
        testUserDetails.giveAccess(CprRolesDefinition.READ_CPR_ROLE);
        this.applyAccess(testUserDetails);

        ParameterMap searchParameters = new ParameterMap();
        searchParameters.add("fmt", "dataonly");

        searchParameters.add("pnr", "0101001234");
        ResponseEntity<String> response = restSearch(searchParameters, "person");
        JsonNode jsonBody = objectMapper.readTree(response.getBody());
        JsonNode results1 = jsonBody.get("results");
        searchParameters.remove("pnr");

        searchParameters.add("pnr", "0101006666");
        response = restSearch(searchParameters, "person");
        jsonBody = objectMapper.readTree(response.getBody());
        JsonNode mustFail = jsonBody.get("results");
        Assert.assertTrue(mustFail.isArray());
        Assert.assertEquals(0, mustFail.size());
        searchParameters.remove("pnr");

        searchParameters.add("fornavn", "Tester");
        response = restSearch(searchParameters, "person");
        jsonBody = objectMapper.readTree(response.getBody());
        JsonNode results2 = jsonBody.get("results");
        Assert.assertEquals(results1, results2);
        searchParameters.remove("fornavn");

        searchParameters.add("fornavn", "Fejler");
        response = restSearch(searchParameters, "person");
        jsonBody = objectMapper.readTree(response.getBody());
        mustFail = jsonBody.get("results");
        Assert.assertTrue(mustFail.isArray());
        Assert.assertEquals(0, mustFail.size());
        searchParameters.remove("fornavn");

        searchParameters.add("efternavn", "Tystersen");
        response = restSearch(searchParameters, "person");
        jsonBody = objectMapper.readTree(response.getBody());
        JsonNode results3 = jsonBody.get("results");
        Assert.assertEquals(results2, results3);
        searchParameters.remove("efternavn");

        searchParameters.add("efternavn", "Fejlersen");
        response = restSearch(searchParameters, "person");
        jsonBody = objectMapper.readTree(response.getBody());
        mustFail = jsonBody.get("results");
        Assert.assertTrue(mustFail.isArray());
        Assert.assertEquals(0, mustFail.size());
        searchParameters.remove("efternavn");

        searchParameters.add("kommunekode", "958");
        response = restSearch(searchParameters, "person");
        jsonBody = objectMapper.readTree(response.getBody());
        JsonNode results4 = jsonBody.get("results");
        Assert.assertEquals(results3, results4);
        searchParameters.remove("kommunekode");

        searchParameters.add("kommunekode", "000");
        response = restSearch(searchParameters, "person");
        jsonBody = objectMapper.readTree(response.getBody());
        mustFail = jsonBody.get("results");
        Assert.assertTrue(mustFail.isArray());
        Assert.assertEquals(0, mustFail.size());
        searchParameters.remove("kommunekode");

        searchParameters.add("vejkode", "111");
        response = restSearch(searchParameters, "person");
        jsonBody = objectMapper.readTree(response.getBody());
        JsonNode results5 = jsonBody.get("results");
        Assert.assertEquals(results4, results5);
        searchParameters.remove("vejkode");

        searchParameters.add("vejkode", "000");
        response = restSearch(searchParameters, "person");
        jsonBody = objectMapper.readTree(response.getBody());
        mustFail = jsonBody.get("results");
        Assert.assertTrue(mustFail.isArray());
        Assert.assertEquals(0, mustFail.size());
        searchParameters.remove("vejkode");

        searchParameters.add("husnummer", "3");
        response = restSearch(searchParameters, "person");
        System.out.println(response.getBody());
        jsonBody = objectMapper.readTree(response.getBody());
        JsonNode results6 = jsonBody.get("results");
        Assert.assertEquals(results5, results6);
        searchParameters.remove("husnummer");

        searchParameters.add("husnummer", "1234");
        response = restSearch(searchParameters, "person");
        jsonBody = objectMapper.readTree(response.getBody());
        mustFail = jsonBody.get("results");
        Assert.assertTrue(mustFail.isArray());
        Assert.assertEquals(0, mustFail.size());
        searchParameters.remove("husnummer");

        searchParameters.add("etage", "02");
        response = restSearch(searchParameters, "person");
        jsonBody = objectMapper.readTree(response.getBody());
        JsonNode results7 = jsonBody.get("results");
        Assert.assertEquals(results6, results7);
        searchParameters.remove("etage");

        searchParameters.add("etage", "01");
        response = restSearch(searchParameters, "person");
        jsonBody = objectMapper.readTree(response.getBody());
        mustFail = jsonBody.get("results");
        Assert.assertTrue(mustFail.isArray());
        Assert.assertEquals(0, mustFail.size());
        searchParameters.remove("etage");

        searchParameters.add("sidedør", "4");
        response = restSearch(searchParameters, "person");
        jsonBody = objectMapper.readTree(response.getBody());
        JsonNode results8 = jsonBody.get("results");
        Assert.assertEquals(results7, results8);
        searchParameters.remove("sidedør");

        searchParameters.add("sidedør", "5");
        response = restSearch(searchParameters, "person");
        jsonBody = objectMapper.readTree(response.getBody());
        mustFail = jsonBody.get("results");
        Assert.assertTrue(mustFail.isArray());
        Assert.assertEquals(0, mustFail.size());
        searchParameters.remove("sidedør");
    }

    @Test
    public void testPersonMunicipalityRestriction() throws Exception {
        whitelistLocalhost();
        OffsetDateTime now = OffsetDateTime.now();
        ImportMetadata importMetadata = new ImportMetadata();
        Session session = sessionManager.getSessionFactory().openSession();
        importMetadata.setSession(session);
        Transaction transaction = session.beginTransaction();
        importMetadata.setTransactionInProgress(true);
        loadPerson(importMetadata);
        transaction.commit();
        session.close();

        TestUserDetails testUserDetails = new TestUserDetails();
        testUserDetails.giveAccess(CprRolesDefinition.READ_CPR_ROLE);
        AreaRestriction a = AreaRestriction.lookup(plugin.getName() + ":" + CprAreaRestrictionDefinition.RESTRICTIONTYPE_KOMMUNEKODER + ":" + CprAreaRestrictionDefinition.RESTRICTION_KOMMUNE_SERMERSOOQ);
        testUserDetails.giveAccess(a);
        this.applyAccess(testUserDetails);

        ParameterMap searchParameters = new ParameterMap();
        searchParameters.add("fmt", "dataonly");

        searchParameters.add("pnr", "0101001234");
        ResponseEntity<String> response = restSearch(searchParameters, "person");
        JsonNode jsonBody = objectMapper.readTree(response.getBody());
        JsonNode mustFail = jsonBody.get("results");
        Assert.assertTrue(mustFail.isArray());
        Assert.assertEquals(0, mustFail.size());
        searchParameters.remove("pnr");

        searchParameters.set("pnr", "0101006666");
        response = restSearch(searchParameters, "person");
        jsonBody = objectMapper.readTree(response.getBody());
        mustFail = jsonBody.get("results");
        Assert.assertTrue(mustFail.isArray());
        Assert.assertEquals(0, mustFail.size());
        searchParameters.remove("pnr");

        searchParameters.set("fornavn", "Tester");
        response = restSearch(searchParameters, "person");
        jsonBody = objectMapper.readTree(response.getBody());
        JsonNode results2 = jsonBody.get("results");
        Assert.assertTrue(results2.isArray());
        Assert.assertEquals(0, results2.size());
        searchParameters.remove("fornavn");

        searchParameters.set("efternavn", "Tystersen");
        response = restSearch(searchParameters, "person");
        jsonBody = objectMapper.readTree(response.getBody());
        JsonNode results3 = jsonBody.get("results");
        Assert.assertTrue(results3.isArray());
        Assert.assertEquals(0, results3.size());
        searchParameters.remove("efternavn");

        searchParameters.set("kommunekode", "958");
        response = restSearch(searchParameters, "person");
        jsonBody = objectMapper.readTree(response.getBody());
        JsonNode results4 = jsonBody.get("results");
        Assert.assertTrue(results4.isArray());
        Assert.assertEquals(0, results4.size());
        searchParameters.remove("kommunekode");

        searchParameters.set("vejkode", "111");
        response = restSearch(searchParameters, "person");
        jsonBody = objectMapper.readTree(response.getBody());
        JsonNode results5 = jsonBody.get("results");
        Assert.assertTrue(results5.isArray());
        Assert.assertEquals(0, results5.size());
        searchParameters.remove("vejkode");

        searchParameters.set("husnummer", "3");
        response = restSearch(searchParameters, "person");
        System.out.println(response.getBody());
        jsonBody = objectMapper.readTree(response.getBody());
        JsonNode results6 = jsonBody.get("results");
        Assert.assertTrue(results6.isArray());
        Assert.assertEquals(0, results6.size());
        searchParameters.remove("husnummer");

        searchParameters.set("etage", "02");
        response = restSearch(searchParameters, "person");
        jsonBody = objectMapper.readTree(response.getBody());
        JsonNode results7 = jsonBody.get("results");
        Assert.assertTrue(results7.isArray());
        Assert.assertEquals(0, results7.size());
        searchParameters.remove("etage");

        searchParameters.set("sidedør", "4");
        response = restSearch(searchParameters, "person");
        jsonBody = objectMapper.readTree(response.getBody());
        JsonNode results8 = jsonBody.get("results");
        Assert.assertTrue(results8.isArray());
        Assert.assertEquals(0, results8.size());
        searchParameters.remove("sidedør");
    }

}
