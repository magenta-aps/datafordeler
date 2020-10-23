package dk.magenta.datafordeler.subscribtion.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import dk.magenta.datafordeler.core.Application;
import dk.magenta.datafordeler.core.database.QueryManager;
import dk.magenta.datafordeler.core.database.SessionManager;
import dk.magenta.datafordeler.core.exception.DataFordelerException;
import dk.magenta.datafordeler.core.fapi.ResultSet;
import dk.magenta.datafordeler.core.io.ImportMetadata;
import dk.magenta.datafordeler.core.user.DafoUserManager;
import dk.magenta.datafordeler.cpr.data.person.PersonEntity;
import dk.magenta.datafordeler.cpr.data.person.PersonEntityManager;
import dk.magenta.datafordeler.cpr.data.person.PersonRecordQuery;
import dk.magenta.datafordeler.cvr.CvrPlugin;
import dk.magenta.datafordeler.cvr.entitymanager.CompanyEntityManager;
import dk.magenta.datafordeler.cvr.query.CompanyRecordQuery;
import dk.magenta.datafordeler.cvr.records.CompanyRecord;
import dk.magenta.datafordeler.cvr.records.CompanyUnitRecord;
import dk.magenta.datafordeler.cvr.records.ParticipantRecord;
import dk.magenta.datafordeler.subscribtion.data.subscribtionModel.*;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.IOException;
import java.io.InputStream;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.List;
import java.util.MissingResourceException;
import java.util.Scanner;

import static org.mockito.Mockito.when;



@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = Application.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class FetchEventsTest {

    @Autowired
    TestRestTemplate restTemplate;


    @Autowired
    private SessionManager sessionManager;

    @Autowired
    private ObjectMapper objectMapper;
    @SpyBean
    private DafoUserManager dafoUserManager;

    @Autowired
    private CvrPlugin plugin;


    @Autowired
    private PersonEntityManager personEntityManager;

    private OffsetDateTime timestampInitial = OffsetDateTime.now(ZoneOffset.UTC);

    private static HashMap<String, String> schemaMap = new HashMap<>();
    static {
        schemaMap.put("_doc", CompanyRecord.schema);
        schemaMap.put("produktionsenhed", CompanyUnitRecord.schema);
        schemaMap.put("deltager", ParticipantRecord.schema);
    }

    @Before
    public void setUp() throws Exception {
        try(Session session = sessionManager.getSessionFactory().openSession()) {


            ImportMetadata importMetadata = new ImportMetadata();
            importMetadata.setSession(session);
            InputStream testData = FindCprBusinessEvent.class.getResourceAsStream("/personsWithEvents.txt");
            personEntityManager.parseData(testData, importMetadata);
            testData.close();

            testData = FindCprBusinessEvent.class.getResourceAsStream("/personsChangingAdresses.txt");
            personEntityManager.parseData(testData, importMetadata);
            testData.close();

            loadCompany("/company_in.json");
            loadCompany("/company_in2.json");
            loadCompany("/company_in3.json");

            Transaction tx = session.beginTransaction();

            Subscriber subscriber = new Subscriber("user1");

            BusinessEventSubscription subscribtionT1 = new BusinessEventSubscription("BE1", "cpr.businessevent.A01");
            subscribtionT1.setSubscriber(subscriber);
            BusinessEventSubscription subscribtionT2 = new BusinessEventSubscription("BE2", "cpr.businessevent.A02");
            subscribtionT2.setSubscriber(subscriber);
            BusinessEventSubscription subscribtionT3 = new BusinessEventSubscription("BE3", "cpr.businessevent.A03");
            subscribtionT3.setSubscriber(subscriber);

            DataEventSubscription subscribtionDE1 = new DataEventSubscription("DE1", "cpr.dataevent.cpr_person_address_record");
            subscribtionDE1.setSubscriber(subscriber);
            DataEventSubscription subscribtionDE2 = new DataEventSubscription("DE2", "cpr.dataevent.anything");
            subscribtionDE2.setSubscriber(subscriber);
            DataEventSubscription subscribtionDE3 = new DataEventSubscription("DE3", "cvr.dataevent.anything");
            subscribtionDE3.setSubscriber(subscriber);

            CprList cprList = new CprList("L1");
            cprList.addCprString("0101011235");
            cprList.addCprString("0101011236");
            cprList.addCprString("0101011237");
            cprList.addCprString("0101011238");
            cprList.addCprString("0101011239");
            cprList.addCprString("0101011240");
            cprList.addCprString("0101011241");
            cprList.addCprString("0101011242");
            cprList.addCprString("0101011243");
            cprList.addCprString("0101011244");
            cprList.addCprString("0101011245");
            cprList.addCprString("0101011246");
            cprList.addCprString("0101011247");
            cprList.addCprString("0101011248");
            cprList.addCprString("0101011249");
            cprList.addCprString("0101011250");
            session.save(cprList);

            CvrList cvrList = new CvrList("L2");
            cvrList.addCvrsString("25052943");
            session.save(cvrList);

            subscribtionT1.setCprList(cprList);
            subscribtionT2.setCprList(cprList);
            subscribtionT3.setCprList(cprList);

            subscribtionDE1.setCprList(cprList);
            subscribtionDE2.setCprList(cprList);

            subscribtionDE3.setCvrList(cvrList);

            subscriber.addBusinessEventSubscribtion(subscribtionT1);
            subscriber.addBusinessEventSubscribtion(subscribtionT2);
            subscriber.addBusinessEventSubscribtion(subscribtionT3);

            subscriber.addDataEventSubscribtion(subscribtionDE1);
            subscriber.addDataEventSubscribtion(subscribtionDE2);
            subscriber.addDataEventSubscribtion(subscribtionDE3);
            session.save(subscriber);
            tx.commit();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }




    private void applyAccess(TestUserDetails testUserDetails) {
        when(dafoUserManager.getFallbackUser()).thenReturn(testUserDetails);
    }

    @Test
    public void testPrintLoadedPersons() {

        try(Session session = sessionManager.getSessionFactory().openSession()) {
            PersonRecordQuery query = new PersonRecordQuery();
            query.setPageSize(100);
            List<ResultSet<PersonEntity>> entities = QueryManager.getAllEntitySets(session, query, PersonEntity.class);
            System.out.println(entities);

        }
    }




    /**
     * Test that it is possible to call a service for fetching events
     */
    @Test
    public void testGetCPRBusinessEvents() throws IOException {

        HttpEntity<String> httpEntity = new HttpEntity<String>("", new HttpHeaders());
        TestUserDetails testUserDetails = new TestUserDetails();
        this.applyAccess(testUserDetails);

        ResponseEntity<String> response = restTemplate.exchange(
                "/subscriptionplugin/v1/findCprBusinessEvent/fetchEvents?subscribtion=BE1&timestamp.GTE=2016-10-26T12:00-06:00&pageSize=100",
                HttpMethod.GET,
                httpEntity,
                String.class
        );
        Assert.assertEquals(HttpStatus.OK, response.getStatusCode());
        ObjectNode responseContent = (ObjectNode) objectMapper.readTree(response.getBody());
        JsonNode results = responseContent.get("results");
        Assert.assertEquals(6, results.size());

        response = restTemplate.exchange(
                "/subscriptionplugin/v1/findCprBusinessEvent/fetchEvents?subscribtion=BE1&pageSize=100",
                HttpMethod.GET,
                httpEntity,
                String.class
        );
        Assert.assertEquals(HttpStatus.OK, response.getStatusCode());
        responseContent = (ObjectNode) objectMapper.readTree(response.getBody());
        results = responseContent.get("results");
        Assert.assertEquals(6, results.size());

        response = restTemplate.exchange(
                "/subscriptionplugin/v1/findCprBusinessEvent/fetchEvents?subscribtion=BE2&timestamp.GTE=2016-10-26T12:00-06:00&pageSize=100",
                HttpMethod.GET,
                httpEntity,
                String.class
        );
        Assert.assertEquals(HttpStatus.OK, response.getStatusCode());
        responseContent = (ObjectNode) objectMapper.readTree(response.getBody());
        results = responseContent.get("results");
        Assert.assertEquals(4, results.size());

        response = restTemplate.exchange(
                "/subscriptionplugin/v1/findCprBusinessEvent/fetchEvents?subscribtion=BE3&timestamp.GTE=2016-10-26T12:00-06:00&pageSize=100",
                HttpMethod.GET,
                httpEntity,
                String.class
        );
        Assert.assertEquals(HttpStatus.OK, response.getStatusCode());
        responseContent = (ObjectNode) objectMapper.readTree(response.getBody());
        results = responseContent.get("results");
        Assert.assertEquals(0, results.size());

        response = restTemplate.exchange(
                "/subscriptionplugin/v1/findCprBusinessEvent/fetchEvents?subscribtion=BE4&timestamp.GTE=2016-10-26T12:00-06:00&pageSize=100",
                HttpMethod.GET,
                httpEntity,
                String.class
        );
        Assert.assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());

        response = restTemplate.exchange(
                "/subscriptionplugin/v1/findCprBusinessEvent/fetchEvents?subscribtion=BE1&timestamp.GTE=2016-10-26T12:00-06:00&pageSize=100",
                HttpMethod.GET,
                httpEntity,
                String.class
        );
        Assert.assertEquals(HttpStatus.OK, response.getStatusCode());
        responseContent = (ObjectNode) objectMapper.readTree(response.getBody());
        results = responseContent.get("results");
        Assert.assertEquals(6, results.size());

        response = restTemplate.exchange(
                "/subscriptionplugin/v1/findCprBusinessEvent/fetchEvents?subscribtion=BE1&timestamp.GTE=2020-09-01T12:00-06:00&pageSize=100",
                HttpMethod.GET,
                httpEntity,
                String.class
        );
        Assert.assertEquals(HttpStatus.OK, response.getStatusCode());
        responseContent = (ObjectNode) objectMapper.readTree(response.getBody());
        results = responseContent.get("results");
        Assert.assertEquals(2, results.size());

        response = restTemplate.exchange(
                "/subscriptionplugin/v1/findCprBusinessEvent/fetchEvents?subscribtion=BE1&timestamp.GTE=2020-09-05T12:00-06:00&pageSize=100",
                HttpMethod.GET,
                httpEntity,
                String.class
        );
        Assert.assertEquals(HttpStatus.OK, response.getStatusCode());
        responseContent = (ObjectNode) objectMapper.readTree(response.getBody());
        results = responseContent.get("results");
        Assert.assertEquals(1, results.size());

        response = restTemplate.exchange(
                "/subscriptionplugin/v1/findCprBusinessEvent/fetchEvents?subscribtion=BE1&timestamp.GTE=2020-09-10T12:00-06:00&pageSize=100",
                HttpMethod.GET,
                httpEntity,
                String.class
        );
        Assert.assertEquals(HttpStatus.OK, response.getStatusCode());
        responseContent = (ObjectNode) objectMapper.readTree(response.getBody());
        results = responseContent.get("results");
        Assert.assertEquals(0, results.size());

    }


    /**
     * Test that it is possible to call a service for fetching events
     */
    @Test
    public void testGetCPRDataAddressChangeEvents() throws IOException {

        HttpEntity<String> httpEntity = new HttpEntity<String>("", new HttpHeaders());
        TestUserDetails testUserDetails = new TestUserDetails();
        testUserDetails.setIdentity("user1");
        this.applyAccess(testUserDetails);

        ResponseEntity<String> response = restTemplate.exchange(
                "/subscriptionplugin/v1/findCprDataEvent/fetchEvents?subscribtion=DE1&timestamp.GTE=2010-11-26T12:00-06:00&pageSize=100",
                HttpMethod.GET,
                httpEntity,
                String.class
        );
        Assert.assertEquals(HttpStatus.OK, response.getStatusCode());
        ObjectNode responseContent = (ObjectNode) objectMapper.readTree(response.getBody());
        JsonNode results = responseContent.get("results");
        Assert.assertEquals(6, results.size());

        response = restTemplate.exchange(
                "/subscriptionplugin/v1/findCprDataEvent/fetchEvents?subscribtion=DE1&pageSize=100",
                HttpMethod.GET,
                httpEntity,
                String.class
        );
        Assert.assertEquals(HttpStatus.OK, response.getStatusCode());
        responseContent = (ObjectNode) objectMapper.readTree(response.getBody());
        results = responseContent.get("results");
        Assert.assertEquals(6, results.size());

        response = restTemplate.exchange(
                "/subscriptionplugin/v1/findCprDataEvent/fetchEvents?subscribtion=DE1&timestamp.GTE=2020-01-26T12:00-06:00&pageSize=100",
                HttpMethod.GET,
                httpEntity,
                String.class
        );
        Assert.assertEquals(HttpStatus.OK, response.getStatusCode());
        responseContent = (ObjectNode) objectMapper.readTree(response.getBody());
        results = responseContent.get("results");
        Assert.assertEquals(2, results.size());

        response = restTemplate.exchange(
                "/subscriptionplugin/v1/findCprDataEvent/fetchEvents?subscribtion=DE1&timestamp.GTE=2020-10-26T12:00-06:00&pageSize=100",
                HttpMethod.GET,
                httpEntity,
                String.class
        );
        Assert.assertEquals(HttpStatus.OK, response.getStatusCode());
        responseContent = (ObjectNode) objectMapper.readTree(response.getBody());
        results = responseContent.get("results");
        Assert.assertEquals(0, results.size());
    }

    /**
     * Test that it is possible to call a service for fetching events
     */
    @Test
    public void testGetCPRDataAnythingChangeEvents() throws IOException {

        HttpEntity<String> httpEntity = new HttpEntity<String>("", new HttpHeaders());
        TestUserDetails testUserDetails = new TestUserDetails();
        testUserDetails.setIdentity("user1");
        this.applyAccess(testUserDetails);

        OffsetDateTime timestamp = OffsetDateTime.now(ZoneOffset.UTC);

        ResponseEntity<String> response = restTemplate.exchange(
                "/subscriptionplugin/v1/findCprDataEvent/fetchEvents?subscribtion=DE2&timestamp.GTE=2020-09-26T12:00-06:00&pageSize=100",
                HttpMethod.GET,
                httpEntity,
                String.class
        );
        Assert.assertEquals(HttpStatus.OK, response.getStatusCode());
        ObjectNode responseContent = (ObjectNode) objectMapper.readTree(response.getBody());
        JsonNode results = responseContent.get("results");
        Assert.assertEquals(16, results.size());

        response = restTemplate.exchange(
                "/subscriptionplugin/v1/findCprDataEvent/fetchEvents?subscribtion=DE2&pageSize=100",
                HttpMethod.GET,
                httpEntity,
                String.class
        );
        Assert.assertEquals(HttpStatus.OK, response.getStatusCode());
        responseContent = (ObjectNode) objectMapper.readTree(response.getBody());
        results = responseContent.get("results");
        Assert.assertEquals(16, results.size());

        response = restTemplate.exchange(
                "/subscriptionplugin/v1/findCprDataEvent/fetchEvents?subscribtion=DE2&timestamp.GTE="+timestamp.toString()+"&pageSize=100",
                HttpMethod.GET,
                httpEntity,
                String.class
        );
        Assert.assertEquals(HttpStatus.OK, response.getStatusCode());
        responseContent = (ObjectNode) objectMapper.readTree(response.getBody());
        results = responseContent.get("results");
        Assert.assertEquals(0, results.size());

        try(Session session = sessionManager.getSessionFactory().openSession()) {

            ImportMetadata importMetadata = new ImportMetadata();
            importMetadata.setSession(session);
            InputStream testData = FindCprBusinessEvent.class.getResourceAsStream("/personsChangingAdressesAppend.txt");
            personEntityManager.parseData(testData, importMetadata);
            testData.close();
        } catch (DataFordelerException e) {
            e.printStackTrace();
        }

        response = restTemplate.exchange(
                "/subscriptionplugin/v1/findCprDataEvent/fetchEvents?subscribtion=DE2&timestamp.GTE="+timestamp.toString()+"&pageSize=100",
                HttpMethod.GET,
                httpEntity,
                String.class
        );
        Assert.assertEquals(HttpStatus.OK, response.getStatusCode());
        responseContent = (ObjectNode) objectMapper.readTree(response.getBody());
        results = responseContent.get("results");
        Assert.assertEquals(2, results.size());
    }

    /**
     * Test that it is possible to call a service for fetching events
     */
    @Test
    public void testGetCVRDataAddressChangeEvents() throws IOException {

        HttpEntity<String> httpEntity = new HttpEntity<String>("", new HttpHeaders());
        TestUserDetails testUserDetails = new TestUserDetails();
        testUserDetails.setIdentity("user1");
        this.applyAccess(testUserDetails);

        ResponseEntity<String> response = restTemplate.exchange(
                "/subscriptionplugin/v1/findCvrDataEvent/fetchEvents?subscribtion=DE3&timestamp.GTE=1980-11-26T12:00-06:00&pageSize=100",
                HttpMethod.GET,
                httpEntity,
                String.class
        );
        Assert.assertEquals(HttpStatus.OK, response.getStatusCode());
        ObjectNode responseContent = (ObjectNode) objectMapper.readTree(response.getBody());
        JsonNode results = responseContent.get("results");
        Assert.assertEquals(1, results.size());

        response = restTemplate.exchange(
                "/subscriptionplugin/v1/findCvrDataEvent/fetchEvents?subscribtion=DE3&pageSize=100",
                HttpMethod.GET,
                httpEntity,
                String.class
        );
        Assert.assertEquals(HttpStatus.OK, response.getStatusCode());
        responseContent = (ObjectNode) objectMapper.readTree(response.getBody());
        results = responseContent.get("results");
        Assert.assertEquals(1, results.size());

        response = restTemplate.exchange(
                "/subscriptionplugin/v1/findCvrDataEvent/fetchEvents?subscribtion=DE3&timestamp.GTE=2010-01-20T12:00-06:00&pageSize=100",
                HttpMethod.GET,
                httpEntity,
                String.class
        );
        Assert.assertEquals(HttpStatus.OK, response.getStatusCode());
        responseContent = (ObjectNode) objectMapper.readTree(response.getBody());
        results = responseContent.get("results");
        Assert.assertEquals(1, results.size());

        response = restTemplate.exchange(
                "/subscriptionplugin/v1/findCvrDataEvent/fetchEvents?subscribtion=DE3&timestamp.GTE=2020-10-26T12:00-06:00&pageSize=100",
                HttpMethod.GET,
                httpEntity,
                String.class
        );
        Assert.assertEquals(HttpStatus.OK, response.getStatusCode());
        responseContent = (ObjectNode) objectMapper.readTree(response.getBody());
        results = responseContent.get("results");
        Assert.assertEquals(0, results.size());
    }

    private HashMap<Integer, JsonNode> loadCompany(String resource) throws IOException, DataFordelerException {
        InputStream input = FetchEventsTest.class.getResourceAsStream(resource);
        if (input == null) {
            throw new MissingResourceException("Missing resource \""+resource+"\"", resource, "key");
        }
        return loadCompany(input, false);
    }

    private HashMap<Integer, JsonNode> loadCompany(InputStream input, boolean linedFile) throws IOException, DataFordelerException {
        ImportMetadata importMetadata = new ImportMetadata();
        Session session = sessionManager.getSessionFactory().openSession();
        Transaction transaction = session.beginTransaction();

        HashMap<Integer, JsonNode> companies = new HashMap<>();
        try {
            importMetadata.setSession(session);

            if (linedFile) {
                int lineNumber = 0;
                Scanner lineScanner = new Scanner(input, "UTF-8").useDelimiter("\n");
                while (lineScanner.hasNext()) {
                    String data = lineScanner.next();

                    JsonNode root = objectMapper.readTree(data);
                    JsonNode itemList = root.get("hits").get("hits");
                    Assert.assertTrue(itemList.isArray());
                    for (JsonNode item : itemList) {
                        String type = item.get("_type").asText();
                        CompanyEntityManager entityManager = (CompanyEntityManager) plugin.getRegisterManager().getEntityManager(schemaMap.get(type));
                        JsonNode companyInputNode = item.get("_source").get("Vrvirksomhed");
                        entityManager.parseData(companyInputNode, importMetadata, session);
                        companies.put(companyInputNode.get("cvrNummer").asInt(), companyInputNode);
                    }
                    lineNumber++;
                    if (lineNumber % 100 == 0) {
                        System.out.println("loaded line " + lineNumber);
                    }
                }
            } else {
                JsonNode root = objectMapper.readTree(input);
                JsonNode itemList = root.get("hits").get("hits");
                Assert.assertTrue(itemList.isArray());
                for (JsonNode item : itemList) {
                    String type = item.get("_type").asText();
                    CompanyEntityManager entityManager = (CompanyEntityManager) plugin.getRegisterManager().getEntityManager(schemaMap.get(type));
                    JsonNode companyInputNode = item.get("_source").get("Vrvirksomhed");
                    entityManager.parseData(companyInputNode, importMetadata, session);
                    companies.put(companyInputNode.get("cvrNummer").asInt(), companyInputNode);
                }
            }
            transaction.commit();
        } finally {
            session.close();
            QueryManager.clearCaches();
            input.close();
        }
        return companies;
    }


}
