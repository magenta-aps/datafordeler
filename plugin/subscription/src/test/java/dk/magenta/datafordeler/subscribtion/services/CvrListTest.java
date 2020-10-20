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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
        ManageSubscribtion controller = new ManageSubscribtion();
        mvc = MockMvcBuilders.standaloneSetup(controller).build();


        try (Session session = sessionManager.getSessionFactory().openSession()) {
            Transaction transaction = session.beginTransaction();
            CvrList cvrList1 = new CvrList("myList1", "myUser");
            session.save(cvrList1);
            CvrList cvrList2 = new CvrList("myList2", "myUser");
            session.save(cvrList2);
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
            CvrList subscriber = (CvrList) query.getResultList().get(0);
            subscriber.addCvrsStrings(Arrays.asList(new String[]{"1111111111", "1111111112"}));
            transaction.commit();
        }

        try(Session session = sessionManager.getSessionFactory().openSession()) {
            Transaction transaction = session.beginTransaction();
            Query query = session.createQuery(" from "+ CvrList.class.getName() +" where listId = :listId", CvrList.class);
            query.setParameter("listId", "myList1");
            CvrList subscriber = (CvrList) query.getResultList().get(0);
            subscriber.addCvrsStrings(Arrays.asList(new String[]{"1111111113", "1111111114"}));
            transaction.commit();
        }

        try(Session session = sessionManager.getSessionFactory().openSession()) {
            Transaction transaction = session.beginTransaction();
            Query query = session.createQuery(" from "+ CvrList.class.getName() +" where listId = :listId", CvrList.class);
            query.setParameter("listId", "myList1");
            CvrList subscriber = (CvrList) query.getResultList().get(0);
            Assert.assertEquals(4, subscriber.getCvr().size());
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
            CvrList subscriber = (CvrList) query.getResultList().get(0);
            session.delete(subscriber);
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
    public void testGetAndAddCprList() throws Exception {

        try(Session session = sessionManager.getSessionFactory().openSession()) {
            Transaction transaction = session.beginTransaction();
            Subscriber subscriber =  new Subscriber("myUser");
            subscriber.addDataEventSubscribtion(new DataEventSubscription("subscribtion1", ""));
            subscriber.addDataEventSubscribtion(new DataEventSubscription("subscribtion2", ""));
            subscriber.addDataEventSubscribtion(new DataEventSubscription("subscribtion3", ""));
            session.save(subscriber);
            transaction.commit();
        }

        HttpEntity<String> httpEntity = new HttpEntity<String>("", new HttpHeaders());
        TestUserDetails testUserDetails = new TestUserDetails();
        testUserDetails.setIdentity("myUser");
        this.applyAccess(testUserDetails);

        //Confirm that the CPR-list is empty
        ResponseEntity<String> response = restTemplate.exchange(
                "/cvrlistplugin/v1/manager/subscriber/cvrList/list",
                HttpMethod.GET,
                httpEntity,
                String.class
        );
        Assert.assertEquals(HttpStatus.OK, response.getStatusCode());
        JSONAssert.assertEquals("[{\"listId\":\"myList1\"}," +
                "{\"listId\":\"myList2\"}]", response.getBody(), false);

        //ADD an element to the CPR-list
        httpEntity = new HttpEntity<String>("cvrTestList1", new HttpHeaders());
        response = restTemplate.exchange(
                "/cvrlistplugin/v1/manager/subscriber/cvrList/create/",
                HttpMethod.POST,
                httpEntity,
                String.class
        );

        //Confirm that the CPR-list has one element
        response = restTemplate.exchange(
                "/cvrlistplugin/v1/manager/subscriber/cvrList/list",
                HttpMethod.GET,
                httpEntity,
                String.class
        );
        Assert.assertEquals(HttpStatus.OK, response.getStatusCode());
        JSONAssert.assertEquals("[{\"listId\":\"myList1\"}," +
                "{\"listId\":\"myList2\"}," +
                "{\"listId\":\"cvrTestList1\"}]", response.getBody(), false);

        //ADD an element to the CPR-list
        httpEntity = new HttpEntity<String>("cvrTestList2", new HttpHeaders());
        response = restTemplate.exchange(
                "/cvrlistplugin/v1/manager/subscriber/cvrList/create/",
                HttpMethod.POST,
                httpEntity,
                String.class
        );
        Assert.assertEquals(HttpStatus.OK, response.getStatusCode());

        //Confirm that the CPR-list has two elements
        response = restTemplate.exchange(
                "/cvrlistplugin/v1/manager/subscriber/cvrList/list",
                HttpMethod.GET,
                httpEntity,
                String.class
        );
        Assert.assertEquals(HttpStatus.OK, response.getStatusCode());
        JSONAssert.assertEquals("[{\"listId\":\"myList1\"}," +
                "{\"listId\":\"myList2\"}," +
                "{\"listId\":\"cvrTestList1\"}," +
                "{\"listId\":\"cvrTestList2\"}]", response.getBody(), false);


        //Add CPR-numbers to the CPR-list
        List<String> cvrList = new ArrayList<String>();
        cvrList.add("1111111110");
        cvrList.add("1111111111");
        cvrList.add("1111111112");
        cvrList.add("1111111113");
        cvrList.add("1111111114");
        cvrList.add("1111111115");
        StringValuesDto dDes= new StringValuesDto("cvrTestList1", null);
        dDes.setValues(cvrList);
        ResponseEntity responseEntity = restTemplate.postForEntity("/cvrlistplugin/v1/manager/subscriber/cvrList/cvr/add/", dDes, StringValuesDto.class);
        Assert.assertEquals(HttpStatus.OK, responseEntity.getStatusCode());

        //Add CPR-numbers to the CPR-list
        cvrList = new ArrayList<String>();
        cvrList.add("1111111116");
        cvrList.add("1111111117");
        cvrList.add("1111111118");
        dDes= new StringValuesDto("cvrTestList1", null);
        dDes.setValues(cvrList);
        responseEntity = restTemplate.postForEntity("/cvrlistplugin/v1/manager/subscriber/cvrList/cvr/add/", dDes, StringValuesDto.class);
        Assert.assertEquals(HttpStatus.OK, responseEntity.getStatusCode());

        //Confirm that the CPR-list has two elements
        response = restTemplate.exchange(
                "/cvrlistplugin/v1/manager/subscriber/cvrList/cvr/list",
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
                "/cvrlistplugin/v1/manager/subscriber/cvrList/cvr/remove/cvrTestList1?cvr=1111111115,1111111117",
                HttpMethod.DELETE,
                httpEntity,
                String.class
        );
        Assert.assertEquals(HttpStatus.OK, response.getStatusCode());

        response = restTemplate.exchange(
                "/cvrlistplugin/v1/manager/subscriber/cvrList/cvr/list",
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
