package dk.magenta.datafordeler.subscription.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import dk.magenta.datafordeler.core.Application;
import dk.magenta.datafordeler.core.exception.ConflictException;
import dk.magenta.datafordeler.subscription.data.subscriptionModel.BusinessEventSubscription;
import dk.magenta.datafordeler.subscription.data.subscriptionModel.CprList;
import dk.magenta.datafordeler.subscription.data.subscriptionModel.DataEventSubscription;
import dk.magenta.datafordeler.subscription.data.subscriptionModel.Subscriber;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.*;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Arrays;

import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;


@ContextConfiguration(classes = Application.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class CprListTest extends TestBase {

    MockMvc mvc;

    /**
     * Initiate lists
     * Test that it is possible to find a specific CPR-list and add new items
     */
    @BeforeEach
    public void setUp() {

        initMocks(this);
        ManageSubscription controller = new ManageSubscription();
        mvc = MockMvcBuilders.standaloneSetup(controller).build();


        try (Session session = sessionManager.getSessionFactory().openSession()) {
            Subscriber subscriber = new Subscriber("PITU/GOV/DIA/magenta_services".replaceAll("/", "_"));
            Transaction transaction = session.beginTransaction();

            subscriber.addBusinessEventSubscription(new BusinessEventSubscription("subscription1", "A01", subscriber));
            subscriber.addBusinessEventSubscription(new BusinessEventSubscription("subscription2", "A02", subscriber));
            subscriber.addBusinessEventSubscription(new BusinessEventSubscription("subscription3", "A03", subscriber));
            subscriber.addDataEventSubscription(new DataEventSubscription("subscription1", "", subscriber));
            subscriber.addDataEventSubscription(new DataEventSubscription("subscription2", "", subscriber));
            subscriber.addDataEventSubscription(new DataEventSubscription("subscription3", "", subscriber));

            CprList cprList1 = new CprList("myList1");
            session.persist(cprList1);
            CprList cprList2 = new CprList("myList2");
            session.persist(cprList2);
            subscriber.addCprList(cprList1);
            subscriber.addCprList(cprList2);
            session.persist(subscriber);
            transaction.commit();
        }

    }


    private void applyAccess(TestUserDetails testUserDetails) {
        when(dafoUserManager.getFallbackUser()).thenReturn(testUserDetails);
    }


    /**
     * Confirm that the datamodel accepts specified modifications to the datamodel
     */
    @Test
    public void testModifications() throws ConflictException {

        try (Session session = sessionManager.getSessionFactory().openSession()) {
            Transaction transaction = session.beginTransaction();
            Query query = session.createQuery(" from " + CprList.class.getName() + " where listId = :listId", CprList.class);
            query.setParameter("listId", "myList1");
            CprList cprList = (CprList) query.getResultList().get(0);
            cprList.addCprStrings(Arrays.asList("1111111111", "1111111112"));
            transaction.commit();
        }

        try (Session session = sessionManager.getSessionFactory().openSession()) {
            Transaction transaction = session.beginTransaction();
            Query query = session.createQuery(" from " + CprList.class.getName() + " where listId = :listId", CprList.class);
            query.setParameter("listId", "myList1");
            CprList cprList = (CprList) query.getResultList().get(0);
            cprList.addCprStrings(Arrays.asList("1111111113", "1111111114"));
            transaction.commit();
        }

        boolean exception = false;
        try (Session session = sessionManager.getSessionFactory().openSession()) {
            Transaction transaction = session.beginTransaction();
            Query query = session.createQuery(" from " + CprList.class.getName() + " where listId = :listId", CprList.class);
            query.setParameter("listId", "myList1");
            CprList cprList = (CprList) query.getResultList().get(0);
            cprList.addCprStrings(Arrays.asList("1111111113"));
            transaction.commit();
        } catch (Exception e) {
            exception = true;
        }
        Assertions.assertTrue(exception);

        try (Session session = sessionManager.getSessionFactory().openSession()) {
            Transaction transaction = session.beginTransaction();
            Query query = session.createQuery(" from " + CprList.class.getName() + " where listId = :listId", CprList.class);
            query.setParameter("listId", "myList1");
            CprList cprList = (CprList) query.getResultList().get(0);
            Assertions.assertEquals(4, cprList.getCpr().size());
        }

        try (Session session = sessionManager.getSessionFactory().openSession()) {
            Transaction transaction = session.beginTransaction();
            Query query = session.createQuery(" from " + CprList.class.getName() + " where listId = :listId", CprList.class);
            query.setParameter("listId", "myList1");
            CprList cprList = (CprList) query.getResultList().get(0);
            cprList.removeCprString("1111111113", session);
            transaction.commit();
        }

        try (Session session = sessionManager.getSessionFactory().openSession()) {
            Transaction transaction = session.beginTransaction();
            Query query = session.createQuery(" from " + CprList.class.getName() + " where listId = :listId", CprList.class);
            query.setParameter("listId", "myList1");
            CprList cprList = (CprList) query.getResultList().get(0);
            Assertions.assertEquals(3, cprList.getCpr().size());
            transaction.commit();
        }

        try (Session session = sessionManager.getSessionFactory().openSession()) {
            Transaction transaction = session.beginTransaction();
            Query query = session.createQuery(" from " + CprList.class.getName() + " where listId = :listId", CprList.class);
            query.setParameter("listId", "myList2");
            CprList cprList = (CprList) query.getResultList().get(0);
            session.remove(cprList);
            transaction.commit();
        }

        try (Session session = sessionManager.getSessionFactory().openSession()) {
            Transaction transaction = session.beginTransaction();
            Query query = session.createQuery(" from " + CprList.class.getName() + " where listId = :listId", CprList.class);
            query.setParameter("listId", "myList2");
            Assertions.assertEquals(0, query.getResultList().size());
            transaction.commit();
        }

    }


    /**
     * Test that it is possible to find a specific CPR-list and add new items
     *
     * @throws Exception
     */
    @Test
    public void testGetAndAddCprList() throws Exception {

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add("uxp-client", "PITU/GOV/DIA/magenta_services");

        HttpEntity<String> httpEntity = new HttpEntity<String>("", httpHeaders);
        dk.magenta.datafordeler.subscription.services.TestUserDetails testUserDetails = new dk.magenta.datafordeler.subscription.services.TestUserDetails();
        testUserDetails.setIdentity("PITU/GOV/DIA/magenta_services");
        this.applyAccess(testUserDetails);

        //Confirm that the CPR-list is empty
        ResponseEntity<String> response = restTemplate.exchange(
                "/subscription/1/manager/subscriber/cprList",
                HttpMethod.GET,
                httpEntity,
                String.class
        );
        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
        JSONAssert.assertEquals("[{\"listId\":\"myList1\"}," +
                "{\"listId\":\"myList2\"}]", response.getBody(), false);

        //ADD an element to the CPR-list  cprTestList1
        httpEntity = new HttpEntity<String>("", httpHeaders);
        response = restTemplate.exchange(
                "/subscription/1/manager/subscriber/cprList/?cprList=cprTestList1",
                HttpMethod.POST,
                httpEntity,
                String.class
        );
        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());

        //Confirm that the CPR-list has one element
        response = restTemplate.exchange(
                "/subscription/1/manager/subscriber/cprList",
                HttpMethod.GET,
                httpEntity,
                String.class
        );
        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
        JSONAssert.assertEquals("[{\"listId\":\"myList1\"}," +
                "{\"listId\":\"myList2\"}," +
                "{\"listId\":\"cprTestList1\"}]", response.getBody(), false);

        //ADD an element to the CPR-list
        httpEntity = new HttpEntity<String>("", httpHeaders);
        response = restTemplate.exchange(
                "/subscription/1/manager/subscriber/cprList/?cprList=cprTestList2",
                HttpMethod.POST,
                httpEntity,
                String.class
        );
        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());

        //Confirm that the CPR-list has two elements
        response = restTemplate.exchange(
                "/subscription/1/manager/subscriber/cprList",
                HttpMethod.GET,
                httpEntity,
                String.class
        );
        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
        JSONAssert.assertEquals("[{\"listId\":\"myList1\"}," +
                "{\"listId\":\"myList2\"}," +
                "{\"listId\":\"cprTestList1\"}," +
                "{\"listId\":\"cprTestList2\"}]", response.getBody(), false);


        //Try fetching with no cpr access rights
        ObjectNode body;
        ArrayNode cprList;

        body = objectMapper.createObjectNode();
        cprList = objectMapper.createArrayNode();
        cprList.add("1111111110");
        cprList.add("1111111111");
        cprList.add("1111111112");
        cprList.add("1111111113");
        cprList.add("1111111114");
        cprList.add("1111111115");
        body.set("cpr", cprList);
        httpEntity = new HttpEntity<String>(body.toString(), httpHeaders);

        response = restTemplate.exchange(
                "/subscription/1/manager/subscriber/cprList/cpr/cprTestList1",
                HttpMethod.POST,
                httpEntity,
                String.class
        );
        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());

        //Confirm that the CPR-list has 6 elements
        response = restTemplate.exchange(
                "/subscription/1/manager/subscriber/cprList/cpr/?listId=cprTestList1",
                HttpMethod.GET,
                httpEntity,
                String.class
        );
        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
        ObjectNode responseContent = (ObjectNode) objectMapper.readTree(response.getBody());
        JsonNode results = responseContent.get("results");
        Assertions.assertEquals(6, results.size());

        body = objectMapper.createObjectNode();
        cprList = objectMapper.createArrayNode();
        cprList.add("1111111110");
        cprList.add("1111111111");
        cprList.add("1111111112");
        cprList.add("1111111113");
        cprList.add("1111111114");
        cprList.add("1111111115");
        body.set("cpr", cprList);
        httpEntity = new HttpEntity<String>(body.toString(), httpHeaders);

        //Add CPR-numbers to the CPR-list
        response = restTemplate.exchange(
                "/subscription/1/manager/subscriber/cprList/cpr/cprTestList1",
                HttpMethod.POST,
                httpEntity,
                String.class
        );
        Assertions.assertEquals(HttpStatus.CONFLICT, response.getStatusCode());

        //Confirm that the CPR-list has still 6 elements
        response = restTemplate.exchange(
                "/subscription/1/manager/subscriber/cprList/cpr/?listId=cprTestList1",
                HttpMethod.GET,
                httpEntity,
                String.class
        );
        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
        responseContent = (ObjectNode) objectMapper.readTree(response.getBody());
        results = responseContent.get("results");
        Assertions.assertEquals(6, results.size());

        body = objectMapper.createObjectNode();
        cprList = objectMapper.createArrayNode();
        cprList.add("1111111116");
        cprList.add("1111111117");
        cprList.add("1111111118");
        body.set("cpr", cprList);
        httpEntity = new HttpEntity<String>(body.toString(), httpHeaders);

        //Add CPR-numbers to the CPR-list
        response = restTemplate.exchange(
                "/subscription/1/manager/subscriber/cprList/cpr/cprTestList1",
                HttpMethod.POST,
                httpEntity,
                String.class
        );
        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
        //Confirm that the CPR-list has 9 elements
        results = responseContent.get("results");
        Assertions.assertEquals(6, results.size());

        response = restTemplate.exchange(
                "/subscription/1/manager/subscriber/cprList/cpr/?listId=cprTestList1",
                HttpMethod.GET,
                httpEntity,
                String.class
        );
        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
        responseContent = (ObjectNode) objectMapper.readTree(response.getBody());
        results = responseContent.get("results");
        Assertions.assertEquals(9, results.size());

        body = objectMapper.createObjectNode();
        cprList = objectMapper.createArrayNode();
        cprList.add("1111111115");
        cprList.add("1111111117");
        body.set("cpr", cprList);
        httpEntity = new HttpEntity<String>(body.toString(), httpHeaders);

        //Try fetching with no cpr access rights
        response = restTemplate.exchange(
                "/subscription/1/manager/subscriber/cprList/cpr/cprTestList1?cpr=1111111115,1111111117",
                HttpMethod.DELETE,
                httpEntity,
                String.class
        );
        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());

        response = restTemplate.exchange(
                "/subscription/1/manager/subscriber/cprList/cpr/?listId=cprTestList1",
                HttpMethod.GET,
                httpEntity,
                String.class
        );
        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
        responseContent = (ObjectNode) objectMapper.readTree(response.getBody());
        results = responseContent.get("results");
        Assertions.assertEquals(7, results.size());
    }

}
