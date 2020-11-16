package dk.magenta.datafordeler.subscribtion.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import dk.magenta.datafordeler.core.Application;
import dk.magenta.datafordeler.core.database.QueryManager;
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

import javax.persistence.PersistenceException;
import java.util.*;

import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;


@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = Application.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class SubscribtionTest {

    @Autowired
    TestRestTemplate restTemplate;


    @Autowired
    private SessionManager sessionManager;

    @Autowired
    private ObjectMapper objectMapper;
    @SpyBean
    private DafoUserManager dafoUserManager;

    MockMvc mvc;



    @Before
    public void setUp() throws Exception {

        initMocks(this);
        ManageSubscribtion controller = new ManageSubscribtion();
        mvc = MockMvcBuilders.standaloneSetup(controller).build();



        //Initiate a list of subscribtions
        try(Session session = sessionManager.getSessionFactory().openSession()) {

            Transaction transaction = session.beginTransaction();

            List<Subscriber> subscriptions = QueryManager.getAllItems(session, Subscriber.class);

            subscriptions.add(new Subscriber("user1"));
            subscriptions.add(new Subscriber("user2"));
            subscriptions.add(new Subscriber("user3"));
            subscriptions.add(new Subscriber("user4"));


            for(Subscriber subscriber : subscriptions) {
                session.save(subscriber);
            }

            transaction.commit();
        }
    }




    private void applyAccess(dk.magenta.datafordeler.subscribtion.services.TestUserDetails testUserDetails) {
        when(dafoUserManager.getFallbackUser()).thenReturn(testUserDetails);
    }


    /**
     * Confirm that the datamodel accepts specified modifications to the datamodel
     */
    @Test
    public void testModifications() {

        try(Session session = sessionManager.getSessionFactory().openSession()) {
            Transaction transaction = session.beginTransaction();
            Query query = session.createQuery(" from "+ Subscriber.class.getName() +" where subscriberId = :subscriberId", Subscriber.class);

            query.setParameter("subscriberId", "user2");
            Subscriber subscriber = (Subscriber) query.getResultList().get(0);
            subscriber.addDataEventSubscribtion(new DataEventSubscription("de1", ""));
            transaction.commit();
        }

        try(Session session = sessionManager.getSessionFactory().openSession()) {
            Transaction transaction = session.beginTransaction();
            Query query = session.createQuery(" from "+ Subscriber.class.getName() +" where subscriberId = :subscriberId", Subscriber.class);

            query.setParameter("subscriberId", "user2");
            Subscriber subscriber = (Subscriber) query.getResultList().get(0);
            DataEventSubscription dataEventSubscription = subscriber.getDataEventSubscription().iterator().next();
            CprList cprList = new CprList("listId");
            session.save(cprList);
            dataEventSubscription.setCprList(cprList);
            transaction.commit();
        }

        try(Session session = sessionManager.getSessionFactory().openSession()) {
            Transaction transaction = session.beginTransaction();
            Query query = session.createQuery(" from "+ Subscriber.class.getName() +" where subscriberId = :subscriberId", Subscriber.class);

            query.setParameter("subscriberId", "user2");
            Subscriber subscriber = (Subscriber) query.getResultList().get(0);
            subscriber.addBusinessEventSubscribtion(new BusinessEventSubscription("be1", "A01"));

            transaction.commit();
        }

        try(Session session = sessionManager.getSessionFactory().openSession()) {
            Transaction transaction = session.beginTransaction();
            Query query = session.createQuery(" from "+ Subscriber.class.getName() +" where subscriberId = :subscriberId", Subscriber.class);

            query.setParameter("subscriberId", "user2");
            Subscriber subscriber = (Subscriber) query.getResultList().get(0);
            BusinessEventSubscription businessEventSubscription = subscriber.getBusinessEventSubscription().iterator().next();
            CprList cprList = new CprList("listId1");
            session.save(cprList);
            businessEventSubscription.setCprList(cprList);
            transaction.commit();
        }

        try(Session session = sessionManager.getSessionFactory().openSession()) {
            Transaction transaction = session.beginTransaction();
            Query query = session.createQuery("select a from "+ Subscriber.class.getName() +" a where a.subscriberId = :subscriberId", Subscriber.class);
            query.setParameter("subscriberId", "user2");
            Subscriber subscriber = (Subscriber) query.getResultList().get(0);
            Assert.assertEquals(1, subscriber.getBusinessEventSubscription().size());
            Assert.assertEquals(1, subscriber.getDataEventSubscription().size());
            transaction.commit();
        }

        //Create cprList1
        try(Session session = sessionManager.getSessionFactory().openSession()) {
            Transaction transaction = session.beginTransaction();
            CprList pnrList = new CprList("cprList1");
            session.save(pnrList);
            transaction.commit();
        }

        //Create cprList2
        try(Session session = sessionManager.getSessionFactory().openSession()) {
            Transaction transaction = session.beginTransaction();
            CprList pnrList = new CprList("cprList2");
            session.save(pnrList);
            transaction.commit();
        }

        //Find cprList2
        try(Session session = sessionManager.getSessionFactory().openSession()) {
            Transaction transaction = session.beginTransaction();
            Query query = session.createQuery(" from "+ CprList.class.getName() +" where listId = :listId", CprList.class);
            query.setParameter("listId", "cprList2");
            Assert.assertEquals(1, query.getResultList().size());
            transaction.commit();
        }

        //Add cprs to cprList2
        boolean requiredExeptionHit = false;
        try(Session session = sessionManager.getSessionFactory().openSession()) {
            Transaction transaction = session.beginTransaction();
            Query query = session.createQuery(" from "+ CprList.class.getName() +" where listId = :listId", CprList.class);
            query.setParameter("listId", "cprList2");
            CprList pnrList = (CprList) query.getResultList().get(0);
            List<SubscribedCprNumber> list = new ArrayList<SubscribedCprNumber>();
            SubscribedCprNumber prn1 = new SubscribedCprNumber("1234");
            SubscribedCprNumber prn2 = new SubscribedCprNumber("1235");
            SubscribedCprNumber prn3 = new SubscribedCprNumber("1236");
            list.add(prn1);
            list.add(prn2);
            list.add(prn3);
            pnrList.setCprs(list);
            Assert.assertEquals(1, query.getResultList().size());
            transaction.commit();
        }

        //Find added cprs in cprList2
        try(Session session = sessionManager.getSessionFactory().openSession()) {
            Transaction transaction = session.beginTransaction();
            Query query = session.createQuery(" from "+ CprList.class.getName() +" where listId = :listId", CprList.class);
            query.setParameter("listId", "cprList2");

            CprList pnrList = (CprList) query.getResultList().get(0);

            Assert.assertEquals(3, pnrList.getCpr().size());
            transaction.commit();
        }


        //Add pnrList into businessEventSubscribtion
        try(Session session = sessionManager.getSessionFactory().openSession()) {
            Transaction transaction = session.beginTransaction();
            Query query = session.createQuery(" from "+ CprList.class.getName() +" where listId = :listId", CprList.class);
            query.setParameter("listId", "cprList2");

            CprList pnrList = (CprList) query.getResultList().get(0);
            Query query2 = session.createQuery(" from "+ Subscriber.class.getName() +" where subscriberId = :subscriberId", Subscriber.class);

            query2.setParameter("subscriberId", "user2");
            Subscriber subscriber = (Subscriber) query2.getResultList().get(0);
            BusinessEventSubscription businessEventSubscription = subscriber.getBusinessEventSubscription().iterator().next();

            businessEventSubscription.setCprList(pnrList);

            transaction.commit();
        }

        //Confirm that the pnrList has been added to businessEventSubscribtion
        try(Session session = sessionManager.getSessionFactory().openSession()) {
            Transaction transaction = session.beginTransaction();
            Query query2 = session.createQuery(" from "+ Subscriber.class.getName() +" where subscriberId = :subscriberId", Subscriber.class);
            query2.setParameter("subscriberId", "user2");

            Subscriber subscriber = (Subscriber) query2.getResultList().get(0);
            BusinessEventSubscription businessEventSubscription = subscriber.getBusinessEventSubscription().iterator().next();
            Assert.assertEquals(3, businessEventSubscription.getCprList().getCpr().size());
            transaction.commit();
        }


    }




    /**
     * Test that it is possible to call a service that deliveres a list of subscribtion that has been created in datafordeler
     */
    @Test
    public void testGetSubscriberList() {

        HttpEntity<String> httpEntity = new HttpEntity<String>("", new HttpHeaders());
        dk.magenta.datafordeler.subscribtion.services.TestUserDetails testUserDetails = new dk.magenta.datafordeler.subscribtion.services.TestUserDetails();
        this.applyAccess(testUserDetails);

        ResponseEntity<String> response = restTemplate.exchange(
                "/subscriptionplugin/v1/manager/subscriber/list",
                HttpMethod.GET,
                httpEntity,
                String.class
        );
        JSONAssert.assertEquals("[{\"subscriberId\":\"user1\",\"businessEventSubscription\":[]," +
                "\"dataEventSubscription\":[]},{\"subscriberId\":\"user2\",\"businessEventSubscription\":[]," +
                "\"dataEventSubscription\":[]},{\"subscriberId\":\"user3\",\"businessEventSubscription\":[]," +
                "\"dataEventSubscription\":[]},{\"subscriberId\":\"user4\",\"businessEventSubscription\":[]," +
                "\"dataEventSubscription\":[]}]", response.getBody(), false);

    }

    /**
     * Test that it is possible to find a specific subscribtion
     * @throws Exception
     */
    @Test
    public void testGetSpecificSubscriber() throws Exception {

        HttpEntity<String> httpEntity = new HttpEntity<String>("", new HttpHeaders());
        dk.magenta.datafordeler.subscribtion.services.TestUserDetails testUserDetails = new dk.magenta.datafordeler.subscribtion.services.TestUserDetails();
        this.applyAccess(testUserDetails);

        ResponseEntity<String> response = restTemplate.exchange(
                "/subscriptionplugin/v1/manager/subscriber/user1",
                HttpMethod.GET,
                httpEntity,
                String.class
        );
        Assert.assertEquals(HttpStatus.OK, response.getStatusCode());
        JSONAssert.assertEquals("{\"subscriberId\":\"user1\",\"businessEventSubscription\":[],\"dataEventSubscription\":[]}", response.getBody(), false);


        httpEntity = new HttpEntity<String>("", new HttpHeaders());
        testUserDetails = new dk.magenta.datafordeler.subscribtion.services.TestUserDetails();
        this.applyAccess(testUserDetails);

        response = restTemplate.exchange(
                "/subscriptionplugin/v1/manager/subscriber/user2",
                HttpMethod.GET,
                httpEntity,
                String.class
        );
        Assert.assertEquals(HttpStatus.OK, response.getStatusCode());
        JSONAssert.assertEquals("{\"subscriberId\":\"user2\",\"businessEventSubscription\":[],\"dataEventSubscription\":[]}", response.getBody(), false);

        httpEntity = new HttpEntity<String>("", new HttpHeaders());
        testUserDetails = new dk.magenta.datafordeler.subscribtion.services.TestUserDetails();
        this.applyAccess(testUserDetails);

        //Try fetching with no cpr access rights
        response = restTemplate.exchange(
                "/subscriptionplugin/v1/manager/subscriber/nonExistingUser",
                HttpMethod.GET,
                httpEntity,
                String.class
        );
        Assert.assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());

    }

    /**
     * Test that it is possible to create a new subscriber
     * @throws Exception
     */
    @Test
    public void testCreateSubscriber() throws Exception {

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add("uxp-client", "PITU/GOV/DIA/magenta_services");

        HttpEntity<String> httpEntity = new HttpEntity<String>("", httpHeaders);
        dk.magenta.datafordeler.subscribtion.services.TestUserDetails testUserDetails = new dk.magenta.datafordeler.subscribtion.services.TestUserDetails();
        this.applyAccess(testUserDetails);

        ResponseEntity<String> response = restTemplate.exchange(
                "/subscriptionplugin/v1/manager/subscriber/list",
                HttpMethod.GET,
                httpEntity,
                String.class
        );

        JSONAssert.assertEquals("[{\"subscriberId\":\"user1\",\"businessEventSubscription\":[]," +
                "\"dataEventSubscription\":[]},{\"subscriberId\":\"user2\",\"businessEventSubscription\":[]," +
                "\"dataEventSubscription\":[]},{\"subscriberId\":\"user3\",\"businessEventSubscription\":[]," +
                "\"dataEventSubscription\":[]},{\"subscriberId\":\"user4\",\"businessEventSubscription\":[]," +
                "\"dataEventSubscription\":[]}]", response.getBody(), false);

        httpEntity = new HttpEntity<String>("{\"subscriberId\":\"createdUser1\",\"businessEventSubscription\":[],\"dataEventSubscription\":[]}", httpHeaders);
        testUserDetails = new dk.magenta.datafordeler.subscribtion.services.TestUserDetails();
        this.applyAccess(testUserDetails);

        //Try fetching with no cpr access rights
        response = restTemplate.exchange(
                "/subscriptionplugin/v1/manager/subscriber/create/",
                HttpMethod.POST,
                httpEntity,
                String.class
        );
        Assert.assertEquals(HttpStatus.OK, response.getStatusCode());

        //Try fetching with no cpr access rights
        response = restTemplate.exchange(
                "/subscriptionplugin/v1/manager/subscriber/create/",
                HttpMethod.POST,
                httpEntity,
                String.class
        );
        Assert.assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());


        response = restTemplate.exchange(
                "/subscriptionplugin/v1/manager/subscriber/list",
                HttpMethod.GET,
                httpEntity,
                String.class
        );

        JSONAssert.assertEquals("[{\"subscriberId\":\"user1\",\"businessEventSubscription\":[]," +
                "\"dataEventSubscription\":[]},{\"subscriberId\":\"user2\",\"businessEventSubscription\":[]," +
                "\"dataEventSubscription\":[]},{\"subscriberId\":\"user3\",\"businessEventSubscription\":[]," +
                "\"dataEventSubscription\":[]},{\"subscriberId\":\"user4\",\"businessEventSubscription\":[]," +
                "\"dataEventSubscription\":[]},{\"subscriberId\":\"createdUser1\",\"businessEventSubscription\":[]," +
                "\"dataEventSubscription\":[]}]", response.getBody(), false);



        //testUserDetails.setIdentity("myCreatedUser");
        this.applyAccess(testUserDetails);

        //Try fetching with no cpr access rights
        response = restTemplate.exchange(
                "/subscriptionplugin/v1/manager/subscriber/",
                HttpMethod.POST,
                httpEntity,
                String.class
        );
        Assert.assertEquals(HttpStatus.OK, response.getStatusCode());

        response = restTemplate.exchange(
                "/subscriptionplugin/v1/manager/subscriber/",
                HttpMethod.POST,
                httpEntity,
                String.class
        );
        Assert.assertEquals(HttpStatus.NOT_ACCEPTABLE, response.getStatusCode());

        response = restTemplate.exchange(
                "/subscriptionplugin/v1/manager/subscriber/list",
                HttpMethod.GET,
                httpEntity,
                String.class
        );
        Assert.assertEquals(HttpStatus.OK, response.getStatusCode());
        JSONAssert.assertEquals("[{\"subscriberId\":\"user1\",\"businessEventSubscription\":[]," +
                "\"dataEventSubscription\":[]},{\"subscriberId\":\"user2\",\"businessEventSubscription\":[]," +
                "\"dataEventSubscription\":[]},{\"subscriberId\":\"user3\",\"businessEventSubscription\":[]," +
                "\"dataEventSubscription\":[]},{\"subscriberId\":\"user4\",\"businessEventSubscription\":[]," +
                "\"dataEventSubscription\":[]},{\"subscriberId\":\"PITU_GOV_DIA_magenta_services\",\"businessEventSubscription\":[]," +
                "\"dataEventSubscription\":[]},{\"subscriberId\":\"createdUser1\",\"businessEventSubscription\":[]," +
                "\"dataEventSubscription\":[]}]", response.getBody(), false);

    }


    /**
     * Test that it is possible to delete a new subscribtion
     * @throws Exception
     */
    @Test
    public void testDeleteSubscriber() throws Exception {

        HttpEntity<String> httpEntity = new HttpEntity<String>("", new HttpHeaders());
        dk.magenta.datafordeler.subscribtion.services.TestUserDetails testUserDetails = new dk.magenta.datafordeler.subscribtion.services.TestUserDetails();
        this.applyAccess(testUserDetails);

        ResponseEntity<String> response = restTemplate.exchange(
                "/subscriptionplugin/v1/manager/subscriber/list",
                HttpMethod.GET,
                httpEntity,
                String.class
        );

        JSONAssert.assertEquals("[{\"subscriberId\":\"user1\",\"businessEventSubscription\":[]," +
                "\"dataEventSubscription\":[]},{\"subscriberId\":\"user2\",\"businessEventSubscription\":[]," +
                "\"dataEventSubscription\":[]},{\"subscriberId\":\"user3\",\"businessEventSubscription\":[]," +
                "\"dataEventSubscription\":[]},{\"subscriberId\":\"user4\",\"businessEventSubscription\":[]," +
                "\"dataEventSubscription\":[]}]", response.getBody(), false);

        httpEntity = new HttpEntity<String>("{\"subscriberId\":\"createdUser1\",\"businessEventSubscription\":[],\"dataEventSubscription\":[]}", new HttpHeaders());
        testUserDetails = new dk.magenta.datafordeler.subscribtion.services.TestUserDetails();
        this.applyAccess(testUserDetails);

        //Try fetching with no cpr access rights
        response = restTemplate.exchange(
                "/subscriptionplugin/v1/manager/subscriber/user2",
                HttpMethod.DELETE,
                httpEntity,
                String.class
        );
        Assert.assertEquals(HttpStatus.OK, response.getStatusCode());
        JSONAssert.assertEquals("{\"subscriberId\":\"user2\",\"businessEventSubscription\":[],\"dataEventSubscription\":[]}", response.getBody(), false);

        response = restTemplate.exchange(
                "/subscriptionplugin/v1/manager/subscriber/list",
                HttpMethod.GET,
                httpEntity,
                String.class
        );
        JSONAssert.assertEquals("[{\"subscriberId\":\"user1\",\"businessEventSubscription\":[]," +
                "\"dataEventSubscription\":[]},{\"subscriberId\":\"user3\",\"businessEventSubscription\":[]," +
                "\"dataEventSubscription\":[]},{\"subscriberId\":\"user4\",\"businessEventSubscription\":[]," +
                "\"dataEventSubscription\":[]}]", response.getBody(), false);
    }





    /**
     * Test that it is possible to find a specific subscription and add new subscriptions
     * @throws Exception
     */
    @Test
    public void testGetAddSubscriptions() throws Exception {

        try(Session session = sessionManager.getSessionFactory().openSession()) {
            Transaction transaction = session.beginTransaction();
            Subscriber subscriber =  new Subscriber("PITU/GOV/DIA/magenta_services".replaceAll("/","_"));
            subscriber.addBusinessEventSubscribtion(new BusinessEventSubscription("subscribtion1", "A01"));
            subscriber.addBusinessEventSubscribtion(new BusinessEventSubscription("subscribtion2", "A02"));
            subscriber.addBusinessEventSubscribtion(new BusinessEventSubscription("subscribtion3", "A03"));
            subscriber.addDataEventSubscribtion(new DataEventSubscription("subscribtion1", ""));
            subscriber.addDataEventSubscribtion(new DataEventSubscription("subscribtion2", ""));
            subscriber.addDataEventSubscribtion(new DataEventSubscription("subscribtion3", ""));
            session.save(subscriber);
            transaction.commit();
        }

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add("uxp-client", "PITU/GOV/DIA/magenta_services");

        HttpEntity<String> httpEntity = new HttpEntity<String>("", httpHeaders);
        dk.magenta.datafordeler.subscribtion.services.TestUserDetails testUserDetails = new dk.magenta.datafordeler.subscribtion.services.TestUserDetails();
        //testUserDetails.setIdentity("myUser");
        this.applyAccess(testUserDetails);


        ResponseEntity<String> response = restTemplate.exchange(
                "/subscriptionplugin/v1/manager/subscriber/businessEventSubscribtion/list",
                HttpMethod.GET,
                httpEntity,
                String.class
        );
        Assert.assertEquals(HttpStatus.OK, response.getStatusCode());
        JSONAssert.assertEquals("[{\"cprList\":null,\"businessEventId\":\"subscribtion3\"}," +
                "{\"cprList\":null,\"businessEventId\":\"subscribtion1\"}," +
                "{\"cprList\":null,\"businessEventId\":\"subscribtion2\"}]", response.getBody(), false);

        response = restTemplate.exchange(
                "/subscriptionplugin/v1/manager/subscriber/businessEventSubscribtion/?businessEventId=newBusinessEventId&kodeId=A04",
                HttpMethod.POST,
                httpEntity,
                String.class
        );


        response = restTemplate.exchange(
                "/subscriptionplugin/v1/manager/subscriber/businessEventSubscribtion/list",
                HttpMethod.GET,
                httpEntity,
                String.class
        );
        Assert.assertEquals(HttpStatus.OK, response.getStatusCode());

        JSONAssert.assertEquals("[{\"cprList\":null,\"businessEventId\":\"newBusinessEventId\",\"kodeId\":\"A04\"}," +
                "{\"cprList\":null,\"businessEventId\":\"subscribtion3\",\"kodeId\":\"A03\"}," +
                "{\"cprList\":null,\"businessEventId\":\"subscribtion1\",\"kodeId\":\"A01\"}," +
                "{\"cprList\":null,\"businessEventId\":\"subscribtion2\",\"kodeId\":\"A02\"}]", response.getBody(), false);

        response = restTemplate.exchange(
                "/subscriptionplugin/v1/manager/subscriber/dataEventSubscribtion/?dataEventId=newDataEventId",
                HttpMethod.POST,
                httpEntity,
                String.class
        );


        response = restTemplate.exchange(
                "/subscriptionplugin/v1/manager/subscriber/dataEventSubscribtion/list",
                HttpMethod.GET,
                httpEntity,
                String.class
        );
        Assert.assertEquals(HttpStatus.OK, response.getStatusCode());

        JSONAssert.assertEquals("[{\"cprList\":null,\"dataEventId\":\"newDataEventId\"}," +
                "{\"cprList\":null,\"dataEventId\":\"subscribtion3\"}," +
                "{\"cprList\":null,\"dataEventId\":\"subscribtion1\"}," +
                "{\"cprList\":null,\"dataEventId\":\"subscribtion2\"}]", response.getBody(), false);


    }


    /**
     * Test that it is possible to delete a new subscribtion
     * @throws Exception
     */
    @Test
    public void testDeleteSubscribtion() throws Exception {

        try(Session session = sessionManager.getSessionFactory().openSession()) {
            Transaction transaction = session.beginTransaction();
            Subscriber subscriber =  new Subscriber("PITU/GOV/DIA/magenta_services".replaceAll("/","_"));
            BusinessEventSubscription bs1 = new BusinessEventSubscription("subscribtion1", "A01");
            BusinessEventSubscription bs2 = new BusinessEventSubscription("subscribtion2", "A01");
            BusinessEventSubscription bs3 = new BusinessEventSubscription("subscribtion3", "A01");
            bs1.setSubscriber(subscriber);
            bs2.setSubscriber(subscriber);
            bs3.setSubscriber(subscriber);
            subscriber.addBusinessEventSubscribtion(bs1);
            subscriber.addBusinessEventSubscribtion(bs2);
            subscriber.addBusinessEventSubscribtion(bs3);

            DataEventSubscription ds1 = new DataEventSubscription("subscribtion1", "");
            DataEventSubscription ds2 = new DataEventSubscription("subscribtion2", "");
            DataEventSubscription ds3 = new DataEventSubscription("subscribtion3", "");
            ds1.setSubscriber(subscriber);
            ds2.setSubscriber(subscriber);
            ds3.setSubscriber(subscriber);

            subscriber.addDataEventSubscribtion(ds1);
            subscriber.addDataEventSubscribtion(ds2);
            subscriber.addDataEventSubscribtion(ds3);
            session.save(subscriber);
            transaction.commit();
        }

        try(Session session = sessionManager.getSessionFactory().openSession()) {

            Transaction transaction = session.beginTransaction();

            List<BusinessEventSubscription> subscriptions = QueryManager.getAllItems(session, BusinessEventSubscription.class);
            System.out.println(subscriptions);

        }

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add("uxp-client", "PITU/GOV/DIA/magenta_services");

        HttpEntity<String> httpEntity = new HttpEntity<String>("", httpHeaders);
        dk.magenta.datafordeler.subscribtion.services.TestUserDetails testUserDetails = new dk.magenta.datafordeler.subscribtion.services.TestUserDetails();
        testUserDetails.setIdentity("PITU/GOV/DIA/magenta_services");
        this.applyAccess(testUserDetails);


        ResponseEntity<String> response = restTemplate.exchange(
                "/subscriptionplugin/v1/manager/subscriber/businessEventSubscribtion/list",
                HttpMethod.GET,
                httpEntity,
                String.class
        );
        Assert.assertEquals(HttpStatus.OK, response.getStatusCode());
        JSONAssert.assertEquals("[{\"cprList\":null,\"businessEventId\":\"subscribtion3\"}," +
                "{\"cprList\":null,\"businessEventId\":\"subscribtion1\"}," +
                "{\"cprList\":null,\"businessEventId\":\"subscribtion2\"}]", response.getBody(), false);


        //Try fetching with no cpr access rights
        response = restTemplate.exchange(
                "/subscriptionplugin/v1/manager/subscriber/businessEventSubscribtion/subscribtion1",
                HttpMethod.DELETE,
                httpEntity,
                String.class
        );
        Assert.assertEquals(HttpStatus.OK, response.getStatusCode());
        //JSONAssert.assertEquals("{\"subscriberId\":\"user2\",\"businessEventSubscribtion\":[],\"dataEventSubscribtion\":[]}", response.getBody(), false);

        response = restTemplate.exchange(
                "/subscriptionplugin/v1/manager/subscriber/businessEventSubscribtion/list",
                HttpMethod.GET,
                httpEntity,
                String.class
        );
        Assert.assertEquals(HttpStatus.OK, response.getStatusCode());
        JSONAssert.assertEquals("[{\"cprList\":null,\"businessEventId\":\"subscribtion3\"}," +
                "{\"cprList\":null,\"businessEventId\":\"subscribtion2\"}]", response.getBody(), false);

        response = restTemplate.exchange(
                "/subscriptionplugin/v1/manager/subscriber/dataEventSubscribtion/list",
                HttpMethod.GET,
                httpEntity,
                String.class
        );
        Assert.assertEquals(HttpStatus.OK, response.getStatusCode());
        JSONAssert.assertEquals("[{\"cprList\":null,\"dataEventId\":\"subscribtion3\"}," +
                "{\"cprList\":null,\"dataEventId\":\"subscribtion1\"}," +
                "{\"cprList\":null,\"dataEventId\":\"subscribtion2\"}]", response.getBody(), false);


        //Try fetching with no cpr access rights
        response = restTemplate.exchange(
                "/subscriptionplugin/v1/manager/subscriber/dataEventSubscribtion/subscribtion1",
                HttpMethod.DELETE,
                httpEntity,
                String.class
        );
        Assert.assertEquals(HttpStatus.OK, response.getStatusCode());
        //JSONAssert.assertEquals("{\"subscriberId\":\"user2\",\"businessEventSubscribtion\":[],\"dataEventSubscribtion\":[]}", response.getBody(), false);

        response = restTemplate.exchange(
                "/subscriptionplugin/v1/manager/subscriber/dataEventSubscribtion/list",
                HttpMethod.GET,
                httpEntity,
                String.class
        );
        Assert.assertEquals(HttpStatus.OK, response.getStatusCode());
        JSONAssert.assertEquals("[{\"cprList\":null,\"dataEventId\":\"subscribtion3\"}," +
                "{\"cprList\":null,\"dataEventId\":\"subscribtion2\"}]", response.getBody(), false);


    }














}
