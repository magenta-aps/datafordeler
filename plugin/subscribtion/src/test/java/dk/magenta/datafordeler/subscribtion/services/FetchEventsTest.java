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
import java.util.List;

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
    private PersonEntityManager personEntityManager;

    private OffsetDateTime timestampInitial = OffsetDateTime.now(ZoneOffset.UTC);

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

            Transaction tx = session.beginTransaction();

            Subscriber subscriber = new Subscriber("user1");

            BusinessEventSubscription subscribtionT1 = new BusinessEventSubscription("BE1", "A01");
            BusinessEventSubscription subscribtionT2 = new BusinessEventSubscription("BE2", "A02");
            BusinessEventSubscription subscribtionT3 = new BusinessEventSubscription("BE3", "A03");

            DataEventSubscription subscribtionDE1 = new DataEventSubscription("DE1", "cpr.cpr_person_address_record");
            DataEventSubscription subscribtionDE2 = new DataEventSubscription("DE2", "cpr.cpr_person_civil_record");
            DataEventSubscription subscribtionDE3 = new DataEventSubscription("DE3", "cpr.cpr_person_civil_record");

            CprList cprList = new CprList("L1", "user1");
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

            subscribtionT1.setCprList(cprList);
            subscribtionT2.setCprList(cprList);
            subscribtionT3.setCprList(cprList);

            subscribtionDE1.setCprList(cprList);
            subscribtionDE2.setCprList(cprList);
            subscribtionDE3.setCprList(cprList);

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
                "/subscriptionplugin/v1/findCprBusinessEvent/fetchEvents?subscribtion=BE1&timestamp=2016-10-26T12:00-06:00&pageSize=100",
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
                "/subscriptionplugin/v1/findCprBusinessEvent/fetchEvents?subscribtion=BE2&timestamp=2016-10-26T12:00-06:00&pageSize=100",
                HttpMethod.GET,
                httpEntity,
                String.class
        );
        Assert.assertEquals(HttpStatus.OK, response.getStatusCode());
        responseContent = (ObjectNode) objectMapper.readTree(response.getBody());
        results = responseContent.get("results");
        Assert.assertEquals(4, results.size());

        response = restTemplate.exchange(
                "/subscriptionplugin/v1/findCprBusinessEvent/fetchEvents?subscribtion=BE3&timestamp=2016-10-26T12:00-06:00&pageSize=100",
                HttpMethod.GET,
                httpEntity,
                String.class
        );
        Assert.assertEquals(HttpStatus.OK, response.getStatusCode());
        responseContent = (ObjectNode) objectMapper.readTree(response.getBody());
        results = responseContent.get("results");
        Assert.assertEquals(0, results.size());

        response = restTemplate.exchange(
                "/subscriptionplugin/v1/findCprBusinessEvent/fetchEvents?subscribtion=BE4&timestamp=2016-10-26T12:00-06:00&pageSize=100",
                HttpMethod.GET,
                httpEntity,
                String.class
        );
        Assert.assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());

        response = restTemplate.exchange(
                "/subscriptionplugin/v1/findCprBusinessEvent/fetchEvents?subscribtion=BE1&timestamp=2016-10-26T12:00-06:00&pageSize=100",
                HttpMethod.GET,
                httpEntity,
                String.class
        );
        Assert.assertEquals(HttpStatus.OK, response.getStatusCode());
        responseContent = (ObjectNode) objectMapper.readTree(response.getBody());
        results = responseContent.get("results");
        Assert.assertEquals(6, results.size());

        response = restTemplate.exchange(
                "/subscriptionplugin/v1/findCprBusinessEvent/fetchEvents?subscribtion=BE1&timestamp=2020-09-01T12:00-06:00&pageSize=100",
                HttpMethod.GET,
                httpEntity,
                String.class
        );
        Assert.assertEquals(HttpStatus.OK, response.getStatusCode());
        responseContent = (ObjectNode) objectMapper.readTree(response.getBody());
        results = responseContent.get("results");
        Assert.assertEquals(2, results.size());

        response = restTemplate.exchange(
                "/subscriptionplugin/v1/findCprBusinessEvent/fetchEvents?subscribtion=BE1&timestamp=2020-09-05T12:00-06:00&pageSize=100",
                HttpMethod.GET,
                httpEntity,
                String.class
        );
        Assert.assertEquals(HttpStatus.OK, response.getStatusCode());
        responseContent = (ObjectNode) objectMapper.readTree(response.getBody());
        results = responseContent.get("results");
        Assert.assertEquals(1, results.size());

        response = restTemplate.exchange(
                "/subscriptionplugin/v1/findCprBusinessEvent/fetchEvents?subscribtion=BE1&timestamp=2020-09-10T12:00-06:00&pageSize=100",
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
        this.applyAccess(testUserDetails);

        ResponseEntity<String> response = restTemplate.exchange(
                "/subscriptionplugin/v1/findCprDataEvent/fetchEvents?subscribtion=DE1&timestamp=2010-11-26T12:00-06:00&pageSize=100",
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
                "/subscriptionplugin/v1/findCprDataEvent/fetchEvents?subscribtion=DE1&timestamp=2020-01-26T12:00-06:00&pageSize=100",
                HttpMethod.GET,
                httpEntity,
                String.class
        );
        Assert.assertEquals(HttpStatus.OK, response.getStatusCode());
        responseContent = (ObjectNode) objectMapper.readTree(response.getBody());
        results = responseContent.get("results");
        Assert.assertEquals(2, results.size());

        response = restTemplate.exchange(
                "/subscriptionplugin/v1/findCprDataEvent/fetchEvents?subscribtion=DE1&timestamp=2020-10-26T12:00-06:00&pageSize=100",
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
        this.applyAccess(testUserDetails);

        OffsetDateTime timestamp = OffsetDateTime.now(ZoneOffset.UTC);

        ResponseEntity<String> response = restTemplate.exchange(
                "/subscriptionplugin/v1/findCprDataEvent/fetchEvents?subscribtion=DE2&timestamp=2020-09-26T12:00-06:00&pageSize=100",
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
                "/subscriptionplugin/v1/findCprDataEvent/fetchEvents?subscribtion=DE2&timestamp="+timestamp.toString()+"&pageSize=100",
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
                "/subscriptionplugin/v1/findCprDataEvent/fetchEvents?subscribtion=DE2&timestamp="+timestamp.toString()+"&pageSize=100",
                HttpMethod.GET,
                httpEntity,
                String.class
        );
        Assert.assertEquals(HttpStatus.OK, response.getStatusCode());
        responseContent = (ObjectNode) objectMapper.readTree(response.getBody());
        results = responseContent.get("results");
        Assert.assertEquals(2, results.size());
    }


}
