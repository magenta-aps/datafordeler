package dk.magenta.datafordeler.subscription.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import dk.magenta.datafordeler.core.Application;
import dk.magenta.datafordeler.core.database.SessionManager;
import dk.magenta.datafordeler.core.user.DafoUserManager;
import dk.magenta.datafordeler.subscription.data.subscriptionModel.*;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.skyscreamer.jsonassert.JSONAssert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Arrays;

import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;


@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = Application.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class CvrListTest {

    @Autowired
    TestRestTemplate restTemplate;


    @Autowired
    private SessionManager sessionManager;

    @Autowired
    private ObjectMapper objectMapper;
    @SpyBean
    private DafoUserManager dafoUserManager;

    MockMvc mvc;


    /**
     * Initiate lists
     * Test that it is possible to find a specific CPR-list and add new items
     * @throws Exception
     */
    @Before
    public void setUp() throws Exception {

        initMocks(this);
        ManageSubscription controller = new ManageSubscription();
        mvc = MockMvcBuilders.standaloneSetup(controller).build();


        try (Session session = sessionManager.getSessionFactory().openSession()) {
            Transaction transaction = session.beginTransaction();
            Subscriber subscriber =  new Subscriber("PITU/GOV/DIA/magenta_services".replaceAll("/","_"));
            subscriber.addDataEventSubscription(new DataEventSubscription("subscription1", ""));
            subscriber.addDataEventSubscription(new DataEventSubscription("subscription2", ""));
            subscriber.addDataEventSubscription(new DataEventSubscription("subscription3", ""));

            CvrList cvrList1 = new CvrList("myList1");
            subscriber.addCvrList(cvrList1);
            session.save(cvrList1);
            CvrList cvrList2 = new CvrList("myList2");
            subscriber.addCvrList(cvrList2);
            session.save(cvrList2);
            session.save(subscriber);
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

        try(Session session = sessionManager.getSessionFactory().openSession()) {
            Transaction transaction = session.beginTransaction();
            Query query = session.createQuery(" from "+ CvrList.class.getName() +" where listId = :listId", CvrList.class);
            query.setParameter("listId", "myList1");
            CvrList cvrList = (CvrList) query.getResultList().get(0);
            cvrList.addCvrStrings(Arrays.asList(new String[]{"1111111111", "1111111112"}));
            transaction.commit();
        }

        try(Session session = sessionManager.getSessionFactory().openSession()) {
            Transaction transaction = session.beginTransaction();
            Query query = session.createQuery(" from "+ CvrList.class.getName() +" where listId = :listId", CvrList.class);
            query.setParameter("listId", "myList1");
            CvrList cvrList = (CvrList) query.getResultList().get(0);
            cvrList.addCvrStrings(Arrays.asList(new String[]{"1111111113", "1111111114"}));
            transaction.commit();
        }

        try(Session session = sessionManager.getSessionFactory().openSession()) {
            Transaction transaction = session.beginTransaction();
            Query query = session.createQuery(" from "+ CvrList.class.getName() +" where listId = :listId", CvrList.class);
            query.setParameter("listId", "myList1");
            CvrList cvrList = (CvrList) query.getResultList().get(0);
            Assert.assertEquals(4, cvrList.getCvr().size());
        }

        try(Session session = sessionManager.getSessionFactory().openSession()) {
            Transaction transaction = session.beginTransaction();
            Query query = session.createQuery(" from "+ CvrList.class.getName() +" where listId = :listId", CvrList.class);
            query.setParameter("listId", "myList1");
            CvrList subscriber = (CvrList) query.getResultList().get(0);
            subscriber.getCvr().removeIf(f -> "1111111113".equals(f.getCvrNumber()));
            transaction.commit();;
        }

        try(Session session = sessionManager.getSessionFactory().openSession()) {
            Transaction transaction = session.beginTransaction();
            Query query = session.createQuery(" from "+ CvrList.class.getName() +" where listId = :listId", CvrList.class);
            query.setParameter("listId", "myList1");
            CvrList subscriber = (CvrList) query.getResultList().get(0);
            Assert.assertEquals(3, subscriber.getCvr().size());
        }

        try(Session session = sessionManager.getSessionFactory().openSession()) {
            Transaction transaction = session.beginTransaction();
            Query query = session.createQuery(" from "+ CvrList.class.getName() +" where listId = :listId", CvrList.class);
            query.setParameter("listId", "myList1");
            CvrList cvrList = (CvrList) query.getResultList().get(0);
            session.delete(cvrList);
            transaction.commit();;
        }

        try(Session session = sessionManager.getSessionFactory().openSession()) {
            Transaction transaction = session.beginTransaction();
            Query query = session.createQuery(" from "+ CvrList.class.getName() +" where listId = :listId", CvrList.class);
            query.setParameter("listId", "myList1");
            Assert.assertEquals(0, query.getResultList().size());
        }

    }




    /**
     * Test that it is possible to find a specific CPR-list and add new items
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

        //Confirm that the CPR-list is empty
        ResponseEntity<String> response = restTemplate.exchange(
                "/subscription/1/manager/subscriber/cvrList",
                HttpMethod.GET,
                httpEntity,
                String.class
        );
        Assert.assertEquals(HttpStatus.OK, response.getStatusCode());
        JSONAssert.assertEquals("[{\"listId\":\"myList1\"}," +
                "{\"listId\":\"myList2\"}]", response.getBody(), false);

        //ADD an element to the CPR-list
        httpEntity = new HttpEntity<String>("", httpHeaders);
        response = restTemplate.exchange(
                "/subscription/1/manager/subscriber/cvrList/?cvrList=cvrTestList1",
                HttpMethod.POST,
                httpEntity,
                String.class
        );
        Assert.assertEquals(HttpStatus.OK, response.getStatusCode());

        //Confirm that the CPR-list has one element
        response = restTemplate.exchange(
                "/subscription/1/manager/subscriber/cvrList",
                HttpMethod.GET,
                httpEntity,
                String.class
        );
        Assert.assertEquals(HttpStatus.OK, response.getStatusCode());
        JSONAssert.assertEquals("[{\"listId\":\"myList1\"}," +
                "{\"listId\":\"myList2\"}," +
                "{\"listId\":\"cvrTestList1\"}]", response.getBody(), false);

        //ADD an element to the CPR-list
        httpEntity = new HttpEntity<String>("", httpHeaders);
        response = restTemplate.exchange(
                "/subscription/1/manager/subscriber/cvrList/?cvrList=cvrTestList2",
                HttpMethod.POST,
                httpEntity,
                String.class
        );
        Assert.assertEquals(HttpStatus.OK, response.getStatusCode());

        //Confirm that the CPR-list has two elements
        response = restTemplate.exchange(
                "/subscription/1/manager/subscriber/cvrList",
                HttpMethod.GET,
                httpEntity,
                String.class
        );
        Assert.assertEquals(HttpStatus.OK, response.getStatusCode());
        JSONAssert.assertEquals("[{\"listId\":\"myList1\"}," +
                "{\"listId\":\"myList2\"}," +
                "{\"listId\":\"cvrTestList1\"}," +
                "{\"listId\":\"cvrTestList2\"}]", response.getBody(), false);

        ObjectNode body;
        ArrayNode cprList;

        body = objectMapper.createObjectNode();
        cprList = objectMapper.createArrayNode();
        cprList.add("11111110");
        cprList.add("11111111");
        cprList.add("11111112");
        cprList.add("11111113");
        cprList.add("11111114");
        cprList.add("11111115");
        body.set("cvr", cprList);
        httpEntity = new HttpEntity<String>(body.toString(), httpHeaders);

        //Add CPR-numbers to the CPR-list
        response = restTemplate.exchange(
                "/subscription/1/manager/subscriber/cvrList/cvr/cvrTestList1",
                HttpMethod.POST,
                httpEntity,
                String.class
        );
        Assert.assertEquals(HttpStatus.OK, response.getStatusCode());

        body = objectMapper.createObjectNode();
        cprList = objectMapper.createArrayNode();
        cprList.add("11111116");
        cprList.add("11111117");
        cprList.add("11111118");
        body.set("cvr", cprList);
        httpEntity = new HttpEntity<String>(body.toString(), httpHeaders);

        //Add CPR-numbers to the CPR-list
        response = restTemplate.exchange(
                "/subscription/1/manager/subscriber/cvrList/cvr/cvrTestList1",
                HttpMethod.POST,
                httpEntity,
                String.class
        );
        Assert.assertEquals(HttpStatus.OK, response.getStatusCode());

        //Confirm that the CPR-list has two elements
        response = restTemplate.exchange(
                "/subscription/1/manager/subscriber/cvrList/cvr/?listId=cvrTestList1",
                HttpMethod.GET,
                httpEntity,
                String.class
        );
        Assert.assertEquals(HttpStatus.OK, response.getStatusCode());
        ObjectNode responseContent = (ObjectNode) objectMapper.readTree(response.getBody());
        JsonNode results = responseContent.get("results");
        Assert.assertEquals(9, results.size());

        //Try fetching with no cpr access rights
        response = restTemplate.exchange(
                "/subscription/1/manager/subscriber/cvrList/cvr/cvrTestList1?cvr=11111115,11111117",
                HttpMethod.DELETE,
                httpEntity,
                String.class
        );
        Assert.assertEquals(HttpStatus.OK, response.getStatusCode());

        response = restTemplate.exchange(
                "/subscription/1/manager/subscriber/cvrList/cvr/?listId=cvrTestList1",
                HttpMethod.GET,
                httpEntity,
                String.class
        );
        Assert.assertEquals(HttpStatus.OK, response.getStatusCode());
        responseContent = (ObjectNode) objectMapper.readTree(response.getBody());
        results = responseContent.get("results");
        Assert.assertEquals(7, results.size());
    }

}
