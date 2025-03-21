package dk.magenta.datafordeler.subscription.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import dk.magenta.datafordeler.core.Application;
import dk.magenta.datafordeler.subscription.data.subscriptionModel.CvrList;
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
public class CvrListTest extends TestBase {

    MockMvc mvc;

    /**
     * Initiate lists
     * Test that it is possible to find a specific CVR-list and add new items
     *
     * @throws Exception
     */
    @BeforeEach
    public void setUp() throws Exception {

        initMocks(this);
        ManageSubscription controller = new ManageSubscription();
        mvc = MockMvcBuilders.standaloneSetup(controller).build();


        try (Session session = sessionManager.getSessionFactory().openSession()) {
            Transaction transaction = session.beginTransaction();
            Subscriber subscriber = new Subscriber("PITU/GOV/DIA/magenta_services".replaceAll("/", "_"));
            subscriber.addDataEventSubscription(new DataEventSubscription("subscription1", "", subscriber));
            subscriber.addDataEventSubscription(new DataEventSubscription("subscription2", "", subscriber));
            subscriber.addDataEventSubscription(new DataEventSubscription("subscription3", "", subscriber));

            CvrList cvrList1 = new CvrList("myList1");
            subscriber.addCvrList(cvrList1);
            session.persist(cvrList1);
            CvrList cvrList2 = new CvrList("myList2");
            subscriber.addCvrList(cvrList2);
            session.persist(cvrList2);
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
    public void testModifications() {

        try (Session session = sessionManager.getSessionFactory().openSession()) {
            Transaction transaction = session.beginTransaction();
            Query query = session.createQuery(" from " + CvrList.class.getName() + " where listId = :listId", CvrList.class);
            query.setParameter("listId", "myList1");
            CvrList cvrList = (CvrList) query.getResultList().get(0);
            cvrList.addCvrStrings(Arrays.asList("1111111111", "1111111112"));
            transaction.commit();
        }

        try (Session session = sessionManager.getSessionFactory().openSession()) {
            Transaction transaction = session.beginTransaction();
            Query query = session.createQuery(" from " + CvrList.class.getName() + " where listId = :listId", CvrList.class);
            query.setParameter("listId", "myList1");
            CvrList cvrList = (CvrList) query.getResultList().get(0);
            cvrList.addCvrStrings(Arrays.asList("1111111113", "1111111114"));
            transaction.commit();
        }

        try (Session session = sessionManager.getSessionFactory().openSession()) {
            Transaction transaction = session.beginTransaction();
            Query query = session.createQuery(" from " + CvrList.class.getName() + " where listId = :listId", CvrList.class);
            query.setParameter("listId", "myList1");
            CvrList cvrList = (CvrList) query.getResultList().get(0);
            Assertions.assertEquals(4, cvrList.getCvr().size());
        }

        try (Session session = sessionManager.getSessionFactory().openSession()) {
            Transaction transaction = session.beginTransaction();
            Query query = session.createQuery(" from " + CvrList.class.getName() + " where listId = :listId", CvrList.class);
            query.setParameter("listId", "myList1");
            CvrList subscriber = (CvrList) query.getResultList().get(0);
            subscriber.removeCvrString("1111111113", session);
            transaction.commit();
        }

        try (Session session = sessionManager.getSessionFactory().openSession()) {
            Transaction transaction = session.beginTransaction();
            Query query = session.createQuery(" from " + CvrList.class.getName() + " where listId = :listId", CvrList.class);
            query.setParameter("listId", "myList1");
            CvrList list = (CvrList) query.getResultList().get(0);
            Assertions.assertEquals(3, list.getCvr().size());
        }

        try (Session session = sessionManager.getSessionFactory().openSession()) {
            Transaction transaction = session.beginTransaction();
            Query query = session.createQuery(" from " + CvrList.class.getName() + " where listId = :listId", CvrList.class);
            query.setParameter("listId", "myList1");
            CvrList cvrList = (CvrList) query.getResultList().get(0);
            session.delete(cvrList);
            transaction.commit();
        }

        try (Session session = sessionManager.getSessionFactory().openSession()) {
            Transaction transaction = session.beginTransaction();
            Query query = session.createQuery(" from " + CvrList.class.getName() + " where listId = :listId", CvrList.class);
            query.setParameter("listId", "myList1");
            Assertions.assertEquals(0, query.getResultList().size());
        }

    }


    /**
     * Test that it is possible to find a specific CVR-list and add new items
     *
     * @throws Exception
     */
    @Test
    public void testGetAndAddCvrList() throws Exception {

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add("uxp-client", "PITU/GOV/DIA/magenta_services");

        HttpEntity<String> httpEntity = new HttpEntity<String>("", httpHeaders);
        TestUserDetails testUserDetails = new TestUserDetails();
        testUserDetails.setIdentity("PITU/GOV/DIA/magenta_services");
        this.applyAccess(testUserDetails);

        //Confirm that the CVR-list is empty
        ResponseEntity<String> response = restTemplate.exchange(
                "/subscription/1/manager/subscriber/cvrList",
                HttpMethod.GET,
                httpEntity,
                String.class
        );
        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
        JSONAssert.assertEquals("[{\"listId\":\"myList1\"}," +
                "{\"listId\":\"myList2\"}]", response.getBody(), false);

        //ADD an element to the CVR-list
        httpEntity = new HttpEntity<String>("", httpHeaders);
        response = restTemplate.exchange(
                "/subscription/1/manager/subscriber/cvrList/?cvrList=cvrTestList1",
                HttpMethod.POST,
                httpEntity,
                String.class
        );
        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());

        //Confirm that the CVR-list has one element
        response = restTemplate.exchange(
                "/subscription/1/manager/subscriber/cvrList",
                HttpMethod.GET,
                httpEntity,
                String.class
        );
        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
        JSONAssert.assertEquals("[{\"listId\":\"myList1\"}," +
                "{\"listId\":\"myList2\"}," +
                "{\"listId\":\"cvrTestList1\"}]", response.getBody(), false);

        //ADD an element to the CVR list
        httpEntity = new HttpEntity<String>("", httpHeaders);
        response = restTemplate.exchange(
                "/subscription/1/manager/subscriber/cvrList/?cvrList=cvrTestList2",
                HttpMethod.POST,
                httpEntity,
                String.class
        );
        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());

        //Confirm that the CVR-list has two elements
        response = restTemplate.exchange(
                "/subscription/1/manager/subscriber/cvrList",
                HttpMethod.GET,
                httpEntity,
                String.class
        );
        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
        JSONAssert.assertEquals("[{\"listId\":\"myList1\"}," +
                "{\"listId\":\"myList2\"}," +
                "{\"listId\":\"cvrTestList1\"}," +
                "{\"listId\":\"cvrTestList2\"}]", response.getBody(), false);

        ObjectNode body;
        ArrayNode cvrList;

        body = objectMapper.createObjectNode();
        cvrList = objectMapper.createArrayNode();
        cvrList.add("11111110");
        cvrList.add("11111111");
        cvrList.add("11111112");
        cvrList.add("11111113");
        cvrList.add("11111114");
        cvrList.add("11111115");
        body.set("cvr", cvrList);
        httpEntity = new HttpEntity<String>(body.toString(), httpHeaders);

        //Add CVR-numbers to the CVR-list
        response = restTemplate.exchange(
                "/subscription/1/manager/subscriber/cvrList/cvr/cvrTestList1",
                HttpMethod.POST,
                httpEntity,
                String.class
        );
        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());

        body = objectMapper.createObjectNode();
        cvrList = objectMapper.createArrayNode();
        cvrList.add("11111116");
        cvrList.add("11111117");
        cvrList.add("11111118");
        body.set("cvr", cvrList);
        httpEntity = new HttpEntity<String>(body.toString(), httpHeaders);

        //Add CVR-numbers to the CVR-list
        response = restTemplate.exchange(
                "/subscription/1/manager/subscriber/cvrList/cvr/cvrTestList1",
                HttpMethod.POST,
                httpEntity,
                String.class
        );
        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());

        response = restTemplate.exchange(
                "/subscription/1/manager/subscriber/cvrList/cvr/nonexistent",
                HttpMethod.POST,
                httpEntity,
                String.class
        );
        Assertions.assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());

        //Confirm that the CVR-list has two elements
        response = restTemplate.exchange(
                "/subscription/1/manager/subscriber/cvrList/cvr/?listId=cvrTestList1",
                HttpMethod.GET,
                httpEntity,
                String.class
        );
        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
        ObjectNode responseContent = (ObjectNode) objectMapper.readTree(response.getBody());
        JsonNode results = responseContent.get("results");
        Assertions.assertEquals(9, results.size());

        //Try fetching with no cvr access rights
        response = restTemplate.exchange(
                "/subscription/1/manager/subscriber/cvrList/cvr/cvrTestList1?cvr=11111115,11111117",
                HttpMethod.DELETE,
                httpEntity,
                String.class
        );
        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());


        response = restTemplate.exchange(
                "/subscription/1/manager/subscriber/cvrList/cvr/nonexistent?cvr=11111115,11111117",
                HttpMethod.DELETE,
                httpEntity,
                String.class
        );
        Assertions.assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());

        response = restTemplate.exchange(
                "/subscription/1/manager/subscriber/cvrList/cvr/?listId=cvrTestList1",
                HttpMethod.GET,
                httpEntity,
                String.class
        );
        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
        responseContent = (ObjectNode) objectMapper.readTree(response.getBody());
        results = responseContent.get("results");
        Assertions.assertEquals(7, results.size());

        response = restTemplate.exchange(
                "/subscription/1/manager/subscriber/cvrList/cvr/?listId=nonexistent",
                HttpMethod.GET,
                httpEntity,
                String.class
        );
        Assertions.assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

}
