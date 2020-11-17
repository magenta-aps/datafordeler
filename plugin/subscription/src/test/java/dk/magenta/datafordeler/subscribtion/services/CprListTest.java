package dk.magenta.datafordeler.subscribtion.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import dk.magenta.datafordeler.core.Application;
import dk.magenta.datafordeler.core.database.SessionManager;
import dk.magenta.datafordeler.core.user.DafoUserManager;
import dk.magenta.datafordeler.subscribtion.data.subscribtionModel.*;
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
public class CprListTest {

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
            Subscriber subscriber = new Subscriber("PITU/GOV/DIA/magenta_services".replaceAll("/","_"));
            Transaction transaction = session.beginTransaction();

            subscriber.addBusinessEventSubscribtion(new BusinessEventSubscription("subscribtion1", "A01"));
            subscriber.addBusinessEventSubscribtion(new BusinessEventSubscription("subscribtion2", "A02"));
            subscriber.addBusinessEventSubscribtion(new BusinessEventSubscription("subscribtion3", "A03"));
            subscriber.addDataEventSubscribtion(new DataEventSubscription("subscribtion1", ""));
            subscriber.addDataEventSubscribtion(new DataEventSubscription("subscribtion2", ""));
            subscriber.addDataEventSubscribtion(new DataEventSubscription("subscribtion3", ""));

            CprList cprList1 = new CprList("myList1");
            session.save(cprList1);
            CprList cprList2 = new CprList("myList2");
            session.save(cprList2);
            subscriber.addCvrList(cprList1);
            subscriber.addCvrList(cprList2);
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
            Query query = session.createQuery(" from "+ CprList.class.getName() +" where listId = :listId", CprList.class);
            query.setParameter("listId", "myList1");
            CprList cprList = (CprList) query.getResultList().get(0);
            cprList.addCprStrings(Arrays.asList(new String[]{"1111111111", "1111111112"}));
            transaction.commit();
        }

        try(Session session = sessionManager.getSessionFactory().openSession()) {
            Transaction transaction = session.beginTransaction();
            Query query = session.createQuery(" from "+ CprList.class.getName() +" where listId = :listId", CprList.class);
            query.setParameter("listId", "myList1");
            CprList cprList = (CprList) query.getResultList().get(0);
            cprList.addCprStrings(Arrays.asList(new String[]{"1111111113", "1111111114"}));
            transaction.commit();
        }

        boolean exception = false;
        try(Session session = sessionManager.getSessionFactory().openSession()) {
            Transaction transaction = session.beginTransaction();
            Query query = session.createQuery(" from "+ CprList.class.getName() +" where listId = :listId", CprList.class);
            query.setParameter("listId", "myList1");
            CprList cprList = (CprList) query.getResultList().get(0);
            cprList.addCprStrings(Arrays.asList(new String[]{"1111111113"}));
            transaction.commit();
        } catch(Exception e) {
            exception = true;
        }
        Assert.assertTrue(exception);

        try(Session session = sessionManager.getSessionFactory().openSession()) {
            Transaction transaction = session.beginTransaction();
            Query query = session.createQuery(" from "+ CprList.class.getName() +" where listId = :listId", CprList.class);
            query.setParameter("listId", "myList1");
            CprList cprList = (CprList) query.getResultList().get(0);
            Assert.assertEquals(4, cprList.getCpr().size());
        }

        try(Session session = sessionManager.getSessionFactory().openSession()) {
            Transaction transaction = session.beginTransaction();
            Query query = session.createQuery(" from "+ CprList.class.getName() +" where listId = :listId", CprList.class);
            query.setParameter("listId", "myList1");
            CprList cprList = (CprList) query.getResultList().get(0);
            cprList.getCpr().removeIf(f -> "1111111113".equals(f.getCprNumber()));
            transaction.commit();;
        }

        try(Session session = sessionManager.getSessionFactory().openSession()) {
            Transaction transaction = session.beginTransaction();
            Query query = session.createQuery(" from "+ CprList.class.getName() +" where listId = :listId", CprList.class);
            query.setParameter("listId", "myList1");
            CprList cprList = (CprList) query.getResultList().get(0);
            Assert.assertEquals(3, cprList.getCpr().size());
        }

        try(Session session = sessionManager.getSessionFactory().openSession()) {
            Transaction transaction = session.beginTransaction();
            Query query = session.createQuery(" from "+ CprList.class.getName() +" where listId = :listId", CprList.class);
            query.setParameter("listId", "myList1");
            CprList cprList = (CprList) query.getResultList().get(0);
            session.delete(cprList);
            transaction.commit();;
        }

        try(Session session = sessionManager.getSessionFactory().openSession()) {
            Transaction transaction = session.beginTransaction();
            Query query = session.createQuery(" from "+ CprList.class.getName() +" where listId = :listId", CprList.class);
            query.setParameter("listId", "myList1");
            Assert.assertEquals(0, query.getResultList().size());
        }

    }




    /**
     * Test that it is possible to find a specific CPR-list and add new items
     * @throws Exception
     */
    @Test
    public void testGetAndAddCprList() throws Exception {

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add("uxp-client", "PITU/GOV/DIA/magenta_services");

        HttpEntity<String> httpEntity = new HttpEntity<String>("", httpHeaders);
        dk.magenta.datafordeler.subscribtion.services.TestUserDetails testUserDetails = new dk.magenta.datafordeler.subscribtion.services.TestUserDetails();
        testUserDetails.setIdentity("PITU/GOV/DIA/magenta_services");
        this.applyAccess(testUserDetails);

        //Confirm that the CPR-list is empty
        ResponseEntity<String> response = restTemplate.exchange(
                "/subscriptionplugin/v1/manager/subscriber/cprList/list",
                HttpMethod.GET,
                httpEntity,
                String.class
        );
        Assert.assertEquals(HttpStatus.OK, response.getStatusCode());
        JSONAssert.assertEquals("[{\"listId\":\"myList1\"}," +
                "{\"listId\":\"myList2\"}]", response.getBody(), false);

        //ADD an element to the CPR-list  cprTestList1
        httpEntity = new HttpEntity<String>("", httpHeaders);
        response = restTemplate.exchange(
                "/subscriptionplugin/v1/manager/subscriber/cprList/?cprList=cprTestList1",
                HttpMethod.POST,
                httpEntity,
                String.class
        );
        Assert.assertEquals(HttpStatus.OK, response.getStatusCode());

        //Confirm that the CPR-list has one element
        response = restTemplate.exchange(
                "/subscriptionplugin/v1/manager/subscriber/cprList/list",
                HttpMethod.GET,
                httpEntity,
                String.class
        );
        Assert.assertEquals(HttpStatus.OK, response.getStatusCode());
        JSONAssert.assertEquals("[{\"listId\":\"myList1\"}," +
                "{\"listId\":\"myList2\"}," +
                "{\"listId\":\"cprTestList1\"}]", response.getBody(), false);

        //ADD an element to the CPR-list
        httpEntity = new HttpEntity<String>("", httpHeaders);
        response = restTemplate.exchange(
                "/subscriptionplugin/v1/manager/subscriber/cprList/?cprList=cprTestList2",
                HttpMethod.POST,
                httpEntity,
                String.class
        );
        Assert.assertEquals(HttpStatus.OK, response.getStatusCode());

        //Confirm that the CPR-list has two elements
        response = restTemplate.exchange(
                "/subscriptionplugin/v1/manager/subscriber/cprList/list",
                HttpMethod.GET,
                httpEntity,
                String.class
        );
        Assert.assertEquals(HttpStatus.OK, response.getStatusCode());
        JSONAssert.assertEquals("[{\"listId\":\"myList1\"}," +
                "{\"listId\":\"myList2\"}," +
                "{\"listId\":\"cprTestList1\"}," +
                "{\"listId\":\"cprTestList2\"}]", response.getBody(), false);


        //Add CPR-numbers to the CPR-list
        /*List<String> cprList = new ArrayList<String>();
        cprList.add("1111111110");
        cprList.add("1111111111");
        cprList.add("1111111112");
        cprList.add("1111111113");
        cprList.add("1111111114");
        cprList.add("1111111115");
        StringValuesDto dDes= new StringValuesDto("cprTestList1", null);
        dDes.setValues(cprList);


        String jsonString = objectMapper.writeValueAsString(dDes);
        System.out.println(jsonString);

        ResponseEntity responseEntity = restTemplate.postForEntity("/subscriptionplugin/v1/manager/subscriber/cprList/cpr/add/", dDes, String.class);
        Assert.assertEquals(HttpStatus.OK, responseEntity.getStatusCode());*/

        //Try fetching with no cpr access rights
        response = restTemplate.exchange(
                "/subscriptionplugin/v1/manager/subscriber/cprList/cpr/cprTestList1?cpr=1111111110,1111111111,1111111112,1111111113,1111111114,1111111115",
                HttpMethod.POST,
                httpEntity,
                String.class
        );
        Assert.assertEquals(HttpStatus.OK, response.getStatusCode());

        //Confirm that the CPR-list has 6 elements
        response = restTemplate.exchange(
                "/subscriptionplugin/v1/manager/subscriber/cprList/cpr/list/?listId=cprTestList1",
                HttpMethod.GET,
                httpEntity,
                String.class
        );
        Assert.assertEquals(HttpStatus.OK, response.getStatusCode());
        ObjectNode responseContent = (ObjectNode) objectMapper.readTree(response.getBody());
        JsonNode results = responseContent.get("results");
        Assert.assertEquals(6, results.size());

        //Add CPR-numbers to the CPR-list
        response = restTemplate.exchange(
                "/subscriptionplugin/v1/manager/subscriber/cprList/cpr/cprTestList1?cpr=1111111110,1111111111,1111111112,1111111113,1111111114,1111111115",
                HttpMethod.POST,
                httpEntity,
                String.class
        );
        Assert.assertEquals(HttpStatus.NOT_ACCEPTABLE, response.getStatusCode());

        //Confirm that the CPR-list has still 6 elements
        response = restTemplate.exchange(
                "/subscriptionplugin/v1/manager/subscriber/cprList/cpr/list/?listId=cprTestList1",
                HttpMethod.GET,
                httpEntity,
                String.class
        );
        Assert.assertEquals(HttpStatus.OK, response.getStatusCode());
        responseContent = (ObjectNode) objectMapper.readTree(response.getBody());
        results = responseContent.get("results");
        Assert.assertEquals(6, results.size());

        //Add CPR-numbers to the CPR-list
        response = restTemplate.exchange(
                "/subscriptionplugin/v1/manager/subscriber/cprList/cpr/cprTestList1?cpr=1111111116,1111111117,1111111118",
                HttpMethod.POST,
                httpEntity,
                String.class
        );
        Assert.assertEquals(HttpStatus.OK, response.getStatusCode());
        //Confirm that the CPR-list has 9 elements
        results = responseContent.get("results");
        Assert.assertEquals(6, results.size());

        response = restTemplate.exchange(
                "/subscriptionplugin/v1/manager/subscriber/cprList/cpr/list/?listId=cprTestList1",
                HttpMethod.GET,
                httpEntity,
                String.class
        );
        Assert.assertEquals(HttpStatus.OK, response.getStatusCode());
        responseContent = (ObjectNode) objectMapper.readTree(response.getBody());
        results = responseContent.get("results");
        Assert.assertEquals(9, results.size());

        //Try fetching with no cpr access rights
        response = restTemplate.exchange(
                "/subscriptionplugin/v1/manager/subscriber/cprList/cpr/cprTestList1?cpr=1111111115,1111111117",
                HttpMethod.DELETE,
                httpEntity,
                String.class
        );
        Assert.assertEquals(HttpStatus.OK, response.getStatusCode());

        response = restTemplate.exchange(
                "/subscriptionplugin/v1/manager/subscriber/cprList/cpr/list/?listId=cprTestList1",
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
