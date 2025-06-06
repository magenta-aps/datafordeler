package dk.magenta.datafordeler.subscription.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import dk.magenta.datafordeler.core.Application;
import dk.magenta.datafordeler.core.database.QueryManager;
import dk.magenta.datafordeler.core.util.UnorderedJsonListComparator;
import dk.magenta.datafordeler.subscription.data.subscriptionModel.*;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.*;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

@ContextConfiguration(classes = Application.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class SubscriptionTest extends TestBase {

    MockMvc mvc;

    @BeforeEach
    public void setUp() {

        initMocks(this);
        ManageSubscription controller = new ManageSubscription();
        mvc = MockMvcBuilders.standaloneSetup(controller).build();


        //Initiate a list of subscriptions
        try (Session session = sessionManager.getSessionFactory().openSession()) {

            Transaction transaction = session.beginTransaction();

            List<Subscriber> subscriptions = QueryManager.getAllItems(session, Subscriber.class);

            subscriptions.add(new Subscriber("user1"));
            subscriptions.add(new Subscriber("user2"));
            subscriptions.add(new Subscriber("user3"));
            subscriptions.add(new Subscriber("user4"));

            for (Subscriber subscriber : subscriptions) {
                session.persist(subscriber);
            }

            transaction.commit();
        }
    }

    private void assertJsonEquals(String jsonExpected, String jsonActual) throws JsonProcessingException {
        Assertions.assertEquals(
                0, new UnorderedJsonListComparator().compare(objectMapper.readTree(jsonExpected), objectMapper.readTree(jsonActual)),
                objectMapper.readTree(jsonExpected) + "   !=   " + objectMapper.readTree(jsonActual)
        );
    }


    private void applyAccess(dk.magenta.datafordeler.subscription.services.TestUserDetails testUserDetails) {
        when(dafoUserManager.getFallbackUser()).thenReturn(testUserDetails);
    }


    /**
     * Confirm that the datamodel accepts specified modifications to the datamodel
     */
    @Test
    public void testModifications() {

        try (Session session = sessionManager.getSessionFactory().openSession()) {
            Transaction transaction = session.beginTransaction();
            Query query = session.createQuery(" from " + Subscriber.class.getName() + " where subscriberId = :subscriberId", Subscriber.class);

            query.setParameter("subscriberId", "user2");
            Subscriber subscriber = (Subscriber) query.getResultList().get(0);
            subscriber.addDataEventSubscription(new DataEventSubscription("de1", "", subscriber));
            transaction.commit();
        }

        try (Session session = sessionManager.getSessionFactory().openSession()) {
            Transaction transaction = session.beginTransaction();
            Query query = session.createQuery(" from " + Subscriber.class.getName() + " where subscriberId = :subscriberId", Subscriber.class);

            query.setParameter("subscriberId", "user2");
            Subscriber subscriber = (Subscriber) query.getResultList().get(0);
            DataEventSubscription dataEventSubscription = subscriber.getDataEventSubscription().iterator().next();
            CprList cprList = new CprList("listId");
            session.persist(cprList);
            dataEventSubscription.setCprList(cprList);
            transaction.commit();
        }

        try (Session session = sessionManager.getSessionFactory().openSession()) {
            Transaction transaction = session.beginTransaction();
            Query query = session.createQuery(" from " + Subscriber.class.getName() + " where subscriberId = :subscriberId", Subscriber.class);

            query.setParameter("subscriberId", "user2");
            Subscriber subscriber = (Subscriber) query.getResultList().get(0);
            subscriber.addBusinessEventSubscription(new BusinessEventSubscription("be1", "A01", subscriber));

            transaction.commit();
        }

        try (Session session = sessionManager.getSessionFactory().openSession()) {
            Transaction transaction = session.beginTransaction();
            Query query = session.createQuery(" from " + Subscriber.class.getName() + " where subscriberId = :subscriberId", Subscriber.class);

            query.setParameter("subscriberId", "user2");
            Subscriber subscriber = (Subscriber) query.getResultList().get(0);
            BusinessEventSubscription businessEventSubscription = subscriber.getBusinessEventSubscription().iterator().next();
            CprList cprList = new CprList("listId1");
            session.persist(cprList);
            businessEventSubscription.setCprList(cprList);
            transaction.commit();
        }

        try (Session session = sessionManager.getSessionFactory().openSession()) {
            Transaction transaction = session.beginTransaction();
            Query query = session.createQuery("select a from " + Subscriber.class.getName() + " a where a.subscriberId = :subscriberId", Subscriber.class);
            query.setParameter("subscriberId", "user2");
            Subscriber subscriber = (Subscriber) query.getResultList().get(0);
            Assertions.assertEquals(1, subscriber.getBusinessEventSubscription().size());
            Assertions.assertEquals(1, subscriber.getDataEventSubscription().size());
            transaction.commit();
        }

        //Create cprList1
        try (Session session = sessionManager.getSessionFactory().openSession()) {
            Transaction transaction = session.beginTransaction();
            CprList pnrList = new CprList("cprList1");
            session.persist(pnrList);
            transaction.commit();
        }

        //Create cprList2
        try (Session session = sessionManager.getSessionFactory().openSession()) {
            Transaction transaction = session.beginTransaction();
            CprList pnrList = new CprList("cprList2");
            session.persist(pnrList);
            transaction.commit();
        }

        //Find cprList2
        try (Session session = sessionManager.getSessionFactory().openSession()) {
            Transaction transaction = session.beginTransaction();
            Query query = session.createQuery(" from " + CprList.class.getName() + " where listId = :listId", CprList.class);
            query.setParameter("listId", "cprList2");
            Assertions.assertEquals(1, query.getResultList().size());
            transaction.commit();
        }

        //Add cprs to cprList2
        boolean requiredExeptionHit = false;
        try (Session session = sessionManager.getSessionFactory().openSession()) {
            Transaction transaction = session.beginTransaction();
            Query query = session.createQuery(" from " + CprList.class.getName() + " where listId = :listId", CprList.class);
            query.setParameter("listId", "cprList2");
            CprList pnrList = (CprList) query.getResultList().get(0);
            SubscribedCprNumber prn1 = new SubscribedCprNumber(pnrList, "1234");
            SubscribedCprNumber prn2 = new SubscribedCprNumber(pnrList, "1235");
            SubscribedCprNumber prn3 = new SubscribedCprNumber(pnrList, "1236");
            session.persist(prn1);
            session.persist(prn2);
            session.persist(prn3);
            Assertions.assertEquals(1, query.getResultList().size());
            transaction.commit();
        }

        //Find added cprs in cprList2
        try (Session session = sessionManager.getSessionFactory().openSession()) {
            Transaction transaction = session.beginTransaction();
            Query query = session.createQuery(" from " + CprList.class.getName() + " where listId = :listId", CprList.class);
            query.setParameter("listId", "cprList2");

            CprList pnrList = (CprList) query.getResultList().get(0);

            Assertions.assertEquals(3, pnrList.getCpr().size());
            transaction.commit();
        }


        //Add pnrList into businessEventSubscription
        try (Session session = sessionManager.getSessionFactory().openSession()) {
            Transaction transaction = session.beginTransaction();
            Query query = session.createQuery(" from " + CprList.class.getName() + " where listId = :listId", CprList.class);
            query.setParameter("listId", "cprList2");

            CprList pnrList = (CprList) query.getResultList().get(0);
            Query query2 = session.createQuery(" from " + Subscriber.class.getName() + " where subscriberId = :subscriberId", Subscriber.class);

            query2.setParameter("subscriberId", "user2");
            Subscriber subscriber = (Subscriber) query2.getResultList().get(0);
            BusinessEventSubscription businessEventSubscription = subscriber.getBusinessEventSubscription().iterator().next();

            businessEventSubscription.setCprList(pnrList);

            transaction.commit();
        }

        //Confirm that the pnrList has been added to businessEventSubscription
        try (Session session = sessionManager.getSessionFactory().openSession()) {
            Transaction transaction = session.beginTransaction();
            Query query2 = session.createQuery(" from " + Subscriber.class.getName() + " where subscriberId = :subscriberId", Subscriber.class);
            query2.setParameter("subscriberId", "user2");

            Subscriber subscriber = (Subscriber) query2.getResultList().get(0);
            BusinessEventSubscription businessEventSubscription = subscriber.getBusinessEventSubscription().iterator().next();
            Assertions.assertEquals(3, businessEventSubscription.getCprList().getCpr().size());
            transaction.commit();
        }
    }


    /**
     * Test that it is possible to call a service that deliveres a list of subscription that has been created in datafordeler
     */
    @Test
    public void testGetSubscriberList() throws JsonProcessingException {

        HttpEntity<String> httpEntity = new HttpEntity<String>("", new HttpHeaders());
        dk.magenta.datafordeler.subscription.services.TestUserDetails testUserDetails = new dk.magenta.datafordeler.subscription.services.TestUserDetails();
        this.applyAccess(testUserDetails);

        ResponseEntity<String> response = restTemplate.exchange(
                "/subscription/1/manager/subscriber",
                HttpMethod.GET,
                httpEntity,
                String.class
        );
        assertJsonEquals(
                "[" +
                        "{\"subscriberId\":\"user1\",\"cprLists\":[],\"cvrLists\":[],\"businessEventSubscription\":[],\"dataEventSubscription\":[]}," +
                        "{\"subscriberId\":\"user2\",\"cprLists\":[],\"cvrLists\":[],\"businessEventSubscription\":[],\"dataEventSubscription\":[]}," +
                        "{\"subscriberId\":\"user3\",\"cprLists\":[],\"cvrLists\":[],\"businessEventSubscription\":[],\"dataEventSubscription\":[]}," +
                        "{\"subscriberId\":\"user4\",\"cprLists\":[],\"cvrLists\":[],\"businessEventSubscription\":[],\"dataEventSubscription\":[]}" +
                        "]",
                response.getBody()
        );
    }

    /**
     * Test that it is possible to find a specific subscription
     *
     * @throws Exception
     */
    @Test
    public void testGetSpecificSubscription() throws Exception {

        HttpEntity<String> httpEntity = new HttpEntity<String>("", new HttpHeaders());
        dk.magenta.datafordeler.subscription.services.TestUserDetails testUserDetails = new dk.magenta.datafordeler.subscription.services.TestUserDetails();
        this.applyAccess(testUserDetails);

        ResponseEntity<String> response = restTemplate.exchange(
                "/subscription/1/manager/subscriber/user1",
                HttpMethod.GET,
                httpEntity,
                String.class
        );
        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
        assertJsonEquals(
                "{\"subscriberId\":\"user1\",\"businessEventSubscription\":[],\"dataEventSubscription\":[],\"cprLists\":[],\"cvrLists\":[]}",
                response.getBody()
        );


        httpEntity = new HttpEntity<String>("", new HttpHeaders());
        testUserDetails = new dk.magenta.datafordeler.subscription.services.TestUserDetails();
        this.applyAccess(testUserDetails);

        response = restTemplate.exchange(
                "/subscription/1/manager/subscriber/user2",
                HttpMethod.GET,
                httpEntity,
                String.class
        );
        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
        assertJsonEquals(
                "{\"subscriberId\":\"user2\",\"businessEventSubscription\":[],\"dataEventSubscription\":[],\"cprLists\":[],\"cvrLists\":[]}",
                response.getBody()
        );

        httpEntity = new HttpEntity<String>("", new HttpHeaders());
        testUserDetails = new dk.magenta.datafordeler.subscription.services.TestUserDetails();
        this.applyAccess(testUserDetails);

        //Try fetching with no cpr access rights
        response = restTemplate.exchange(
                "/subscription/1/manager/subscriber/nonExistingUser",
                HttpMethod.GET,
                httpEntity,
                String.class
        );
        Assertions.assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    /**
     * Test that it is possible to create a new subscriber
     *
     * @throws Exception
     */
    @Test
    public void testCreateSubscriber() throws Exception {

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add("uxp-client", "PITU/GOV/DIA/magenta_services");

        HttpEntity<String> httpEntity = new HttpEntity<String>("", httpHeaders);
        dk.magenta.datafordeler.subscription.services.TestUserDetails testUserDetails = new dk.magenta.datafordeler.subscription.services.TestUserDetails();
        this.applyAccess(testUserDetails);

        ResponseEntity<String> response = restTemplate.exchange(
                "/subscription/1/manager/subscriber",
                HttpMethod.GET,
                httpEntity,
                String.class
        );

        assertJsonEquals(
                "[" +
                        "{\"subscriberId\":\"user1\",\"businessEventSubscription\":[],\"dataEventSubscription\":[],\"cprLists\":[],\"cvrLists\":[]}," +
                        "{\"subscriberId\":\"user2\",\"businessEventSubscription\":[],\"dataEventSubscription\":[],\"cprLists\":[],\"cvrLists\":[]}," +
                        "{\"subscriberId\":\"user3\",\"businessEventSubscription\":[],\"dataEventSubscription\":[],\"cprLists\":[],\"cvrLists\":[]}," +
                        "{\"subscriberId\":\"user4\",\"businessEventSubscription\":[],\"dataEventSubscription\":[],\"cprLists\":[],\"cvrLists\":[]}" +
                        "]",
                response.getBody()
        );

        httpEntity = new HttpEntity<String>("{\"subscriberId\":\"createdUser1\",\"businessEventSubscription\":[],\"dataEventSubscription\":[]}", httpHeaders);
        testUserDetails = new dk.magenta.datafordeler.subscription.services.TestUserDetails();
        this.applyAccess(testUserDetails);

        //Try fetching with no cpr access rights
        response = restTemplate.exchange(
                "/subscription/1/manager/subscriber/create/",
                HttpMethod.POST,
                httpEntity,
                String.class
        );
        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());

        //Try fetching with no cpr access rights
        response = restTemplate.exchange(
                "/subscription/1/manager/subscriber/create/",
                HttpMethod.POST,
                httpEntity,
                String.class
        );
        Assertions.assertEquals(HttpStatus.CONFLICT, response.getStatusCode());

        response = restTemplate.exchange(
                "/subscription/1/manager/subscriber",
                HttpMethod.GET,
                httpEntity,
                String.class
        );

        assertJsonEquals("[" +
                        "{\"subscriberId\":\"user1\",\"businessEventSubscription\":[],\"dataEventSubscription\":[],\"cprLists\":[],\"cvrLists\":[]}," +
                        "{\"subscriberId\":\"user2\",\"businessEventSubscription\":[],\"dataEventSubscription\":[],\"cprLists\":[],\"cvrLists\":[]}," +
                        "{\"subscriberId\":\"user3\",\"businessEventSubscription\":[],\"dataEventSubscription\":[],\"cprLists\":[],\"cvrLists\":[]}," +
                        "{\"subscriberId\":\"user4\",\"businessEventSubscription\":[],\"dataEventSubscription\":[],\"cprLists\":[],\"cvrLists\":[]}," +
                        "{\"subscriberId\":\"createdUser1\",\"businessEventSubscription\":[],\"dataEventSubscription\":[],\"cprLists\":[],\"cvrLists\":[]}" +
                        "]",
                response.getBody()
        );


        //testUserDetails.setIdentity("myCreatedUser");
        this.applyAccess(testUserDetails);

        //Try fetching with no cpr access rights
        response = restTemplate.exchange(
                "/subscription/1/manager/subscriber/",
                HttpMethod.POST,
                httpEntity,
                String.class
        );
        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());

        response = restTemplate.exchange(
                "/subscription/1/manager/subscriber/",
                HttpMethod.POST,
                httpEntity,
                String.class
        );
        Assertions.assertEquals(HttpStatus.CONFLICT, response.getStatusCode());

        response = restTemplate.exchange(
                "/subscription/1/manager/subscriber",
                HttpMethod.GET,
                httpEntity,
                String.class
        );
        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
        assertJsonEquals(
                "[" +
                        "{\"subscriberId\":\"user1\",\"businessEventSubscription\":[],\"dataEventSubscription\":[],\"cprLists\":[],\"cvrLists\":[]}," +
                        "{\"subscriberId\":\"user2\",\"businessEventSubscription\":[],\"dataEventSubscription\":[],\"cprLists\":[],\"cvrLists\":[]}," +
                        "{\"subscriberId\":\"user3\",\"businessEventSubscription\":[],\"dataEventSubscription\":[],\"cprLists\":[],\"cvrLists\":[]}," +
                        "{\"subscriberId\":\"user4\",\"businessEventSubscription\":[],\"dataEventSubscription\":[],\"cprLists\":[],\"cvrLists\":[]}," +
                        "{\"subscriberId\":\"PITU_GOV_DIA_magenta_services\",\"businessEventSubscription\":[],\"dataEventSubscription\":[],\"cprLists\":[],\"cvrLists\":[]}," +
                        "{\"subscriberId\":\"createdUser1\",\"businessEventSubscription\":[],\"dataEventSubscription\":[],\"cprLists\":[],\"cvrLists\":[]}" +
                        "]",
                response.getBody()
        );
    }


    /**
     * Test that it is possible to delete a new subscription
     *
     * @throws Exception
     */
    @Test
    public void testDeleteSubscriber() throws Exception {

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add("uxp-client", "user2");
        HttpEntity<String> httpEntity = new HttpEntity<String>("", httpHeaders);
        dk.magenta.datafordeler.subscription.services.TestUserDetails testUserDetails = new dk.magenta.datafordeler.subscription.services.TestUserDetails();
        this.applyAccess(testUserDetails);

        ResponseEntity<String> response = restTemplate.exchange(
                "/subscription/1/manager/subscriber",
                HttpMethod.GET,
                httpEntity,
                String.class
        );

        assertJsonEquals(
                "[{\"subscriberId\":\"user1\",\"businessEventSubscription\":[],\"dataEventSubscription\":[],\"cprLists\":[],\"cvrLists\":[]}," +
                "{\"subscriberId\":\"user2\",\"businessEventSubscription\":[],\"dataEventSubscription\":[],\"cprLists\":[],\"cvrLists\":[]}," +
                "{\"subscriberId\":\"user3\",\"businessEventSubscription\":[],\"dataEventSubscription\":[],\"cprLists\":[],\"cvrLists\":[]}," +
                "{\"subscriberId\":\"user4\",\"businessEventSubscription\":[],\"dataEventSubscription\":[],\"cprLists\":[],\"cvrLists\":[]}" +
                "]",
                response.getBody()
        );

        httpEntity = new HttpEntity<String>("{\"subscriberId\":\"createdUser1\",\"businessEventSubscription\":[],\"dataEventSubscription\":[]}", httpHeaders);
        testUserDetails = new dk.magenta.datafordeler.subscription.services.TestUserDetails();
        this.applyAccess(testUserDetails);

        //Try fetching with no cpr access rights
        response = restTemplate.exchange(
                "/subscription/1/manager/subscriber/",
                HttpMethod.DELETE,
                httpEntity,
                String.class
        );
        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
        assertJsonEquals(
                "{\"subscriberId\":\"user2\",\"businessEventSubscription\":[],\"dataEventSubscription\":[],\"cprLists\":[],\"cvrLists\":[]}",
                response.getBody()
        );

        response = restTemplate.exchange(
                "/subscription/1/manager/subscriber",
                HttpMethod.GET,
                httpEntity,
                String.class
        );
        Assertions.assertEquals(
                objectMapper.readTree("[" +
                        "{\"subscriberId\":\"user1\",\"businessEventSubscription\":[],\"dataEventSubscription\":[],\"cprLists\":[],\"cvrLists\":[]}," +
                        "{\"subscriberId\":\"user3\",\"businessEventSubscription\":[],\"dataEventSubscription\":[],\"cprLists\":[],\"cvrLists\":[]}," +
                        "{\"subscriberId\":\"user4\",\"businessEventSubscription\":[],\"dataEventSubscription\":[],\"cprLists\":[],\"cvrLists\":[]}" +
                "]"),
                objectMapper.readTree(response.getBody())
        );
    }


    /**
     * Test that it is possible to find a specific subscription and add new subscriptions
     *
     * @throws Exception
     */
    @Test
    public void testGetAddSubscriptions() throws Exception {

        try (Session session = sessionManager.getSessionFactory().openSession()) {
            Transaction transaction = session.beginTransaction();
            Subscriber subscriber = new Subscriber("PITU/GOV/DIA/magenta_services".replaceAll("/", "_"));
            subscriber.addBusinessEventSubscription(new BusinessEventSubscription("subscription1", "A01", subscriber));
            subscriber.addBusinessEventSubscription(new BusinessEventSubscription("subscription2", "A02", subscriber));
            subscriber.addBusinessEventSubscription(new BusinessEventSubscription("subscription3", "A03", subscriber));
            subscriber.addDataEventSubscription(new DataEventSubscription("subscription1", "", subscriber));
            subscriber.addDataEventSubscription(new DataEventSubscription("subscription2", "", subscriber));
            subscriber.addDataEventSubscription(new DataEventSubscription("subscription3", "", subscriber));
            session.persist(subscriber);
            transaction.commit();
        }

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add("uxp-client", "PITU/GOV/DIA/magenta_services");

        HttpEntity<String> httpEntity = new HttpEntity<String>("", httpHeaders);
        dk.magenta.datafordeler.subscription.services.TestUserDetails testUserDetails = new dk.magenta.datafordeler.subscription.services.TestUserDetails();
        //testUserDetails.setIdentity("myUser");
        this.applyAccess(testUserDetails);


        ResponseEntity<String> response = restTemplate.exchange(
                "/subscription/1/manager/subscriber/subscription/businesseventSubscription",
                HttpMethod.GET,
                httpEntity,
                String.class
        );
        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
        assertJsonEquals(
                "[" +
                        "{\"cprList\":null,\"businessEventId\":\"subscription1\",\"kodeId\":\"A01\"}," +
                        "{\"cprList\":null,\"businessEventId\":\"subscription2\",\"kodeId\":\"A02\"}," +
                        "{\"cprList\":null,\"businessEventId\":\"subscription3\",\"kodeId\":\"A03\"}" +
                        "]",
                response.getBody()
        );

        response = restTemplate.exchange(
                "/subscription/1/manager/subscriber/subscription/businesseventSubscription/?businessEventId=newBusinessEventId&kodeId=A04",
                HttpMethod.POST,
                httpEntity,
                String.class
        );
        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());

        response = restTemplate.exchange(
                "/subscription/1/manager/subscriber/subscription/businesseventSubscription",
                HttpMethod.GET,
                httpEntity,
                String.class
        );
        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());

        assertJsonEquals("[" +
                        "{\"cprList\":null,\"businessEventId\":\"newBusinessEventId\",\"kodeId\":\"A04\"}," +
                        "{\"cprList\":null,\"businessEventId\":\"subscription3\",\"kodeId\":\"A03\"}," +
                        "{\"cprList\":null,\"businessEventId\":\"subscription1\",\"kodeId\":\"A01\"}," +
                        "{\"cprList\":null,\"businessEventId\":\"subscription2\",\"kodeId\":\"A02\"}" +
                        "]",
                response.getBody()
        );

        response = restTemplate.exchange(
                "/subscription/1/manager/subscriber/subscription/dataeventSubscription/?dataEventId=newDataEventId",
                HttpMethod.POST,
                httpEntity,
                String.class
        );
        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());

        response = restTemplate.exchange(
                "/subscription/1/manager/subscriber/subscription/dataeventSubscription",
                HttpMethod.GET,
                httpEntity,
                String.class
        );
        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());

        assertJsonEquals(
                "[" +
                    "{\"cprList\":null,\"cvrList\":null,\"dataEventId\":\"newDataEventId\",\"kodeId\":\"\"}," +
                    "{\"cprList\":null,\"cvrList\":null,\"dataEventId\":\"subscription3\",\"kodeId\":\"\"}," +
                    "{\"cprList\":null,\"cvrList\":null,\"dataEventId\":\"subscription1\",\"kodeId\":\"\"}," +
                    "{\"cprList\":null,\"cvrList\":null,\"dataEventId\":\"subscription2\",\"kodeId\":\"\"}" +
                "]",
                response.getBody()
        );
    }


    /**
     * Test that it is possible to delete a new subscription
     *
     * @throws Exception
     */
    @Test
    public void testDeleteSubscription() throws Exception {

        try (Session session = sessionManager.getSessionFactory().openSession()) {
            Transaction transaction = session.beginTransaction();
            Subscriber subscriber = new Subscriber("PITU/GOV/DIA/magenta_services".replaceAll("/", "_"));
            BusinessEventSubscription bs1 = new BusinessEventSubscription("subscription1", "A01", subscriber);
            BusinessEventSubscription bs2 = new BusinessEventSubscription("subscription2", "A01", subscriber);
            BusinessEventSubscription bs3 = new BusinessEventSubscription("subscription3", "A01", subscriber);
            subscriber.addBusinessEventSubscription(bs1);
            subscriber.addBusinessEventSubscription(bs2);
            subscriber.addBusinessEventSubscription(bs3);

            DataEventSubscription ds1 = new DataEventSubscription("subscription1", "", subscriber);
            DataEventSubscription ds2 = new DataEventSubscription("subscription2", "", subscriber);
            DataEventSubscription ds3 = new DataEventSubscription("subscription3", "", subscriber);

            subscriber.addDataEventSubscription(ds1);
            subscriber.addDataEventSubscription(ds2);
            subscriber.addDataEventSubscription(ds3);
            session.persist(subscriber);
            transaction.commit();
        }

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add("uxp-client", "PITU/GOV/DIA/magenta_services");

        HttpEntity<String> httpEntity = new HttpEntity<String>("", httpHeaders);
        dk.magenta.datafordeler.subscription.services.TestUserDetails testUserDetails = new dk.magenta.datafordeler.subscription.services.TestUserDetails();
        testUserDetails.setIdentity("PITU/GOV/DIA/magenta_services");
        this.applyAccess(testUserDetails);

        ResponseEntity<String> response = restTemplate.exchange(
                "/subscription/1/manager/subscriber/subscription/businesseventSubscription",
                HttpMethod.GET,
                httpEntity,
                String.class
        );
        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
        assertJsonEquals("[" +
                "{\"cprList\":null,\"businessEventId\":\"subscription1\",\"kodeId\":\"A01\"}," +
                "{\"cprList\":null,\"businessEventId\":\"subscription2\",\"kodeId\":\"A01\"}," +
                "{\"cprList\":null,\"businessEventId\":\"subscription3\",\"kodeId\":\"A01\"}" +
                "]", response.getBody());


        //Try fetching with no cpr access rights
        response = restTemplate.exchange(
                "/subscription/1/manager/subscriber/subscription/businesseventSubscription/subscription1",
                HttpMethod.DELETE,
                httpEntity,
                String.class
        );
        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());

        response = restTemplate.exchange(
                "/subscription/1/manager/subscriber/subscription/businesseventSubscription",
                HttpMethod.GET,
                httpEntity,
                String.class
        );
        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
        assertJsonEquals(
                "[" +
                "{\"cprList\":null,\"businessEventId\":\"subscription3\",\"kodeId\":\"A01\"}," +
                "{\"cprList\":null,\"businessEventId\":\"subscription2\",\"kodeId\":\"A01\"}" +
                "]",
                response.getBody()
        );

        response = restTemplate.exchange(
                "/subscription/1/manager/subscriber/subscription/dataeventSubscription",
                HttpMethod.GET,
                httpEntity,
                String.class
        );
        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
        assertJsonEquals("[" +
                "{\"cprList\":null,\"dataEventId\":\"subscription3\",\"cprList\":null,\"cvrList\":null,\"kodeId\":\"\"}," +
                "{\"cprList\":null,\"dataEventId\":\"subscription1\",\"cprList\":null,\"cvrList\":null,\"kodeId\":\"\"}," +
                "{\"cprList\":null,\"dataEventId\":\"subscription2\",\"cprList\":null,\"cvrList\":null,\"kodeId\":\"\"}" +
                "]", response.getBody());

        //Try fetching with no cpr access rights
        response = restTemplate.exchange(
                "/subscription/1/manager/subscriber/subscription/dataeventSubscription/subscription1",
                HttpMethod.DELETE,
                httpEntity,
                String.class
        );
        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
        //JSONAssert.assertEquals("{\"subscriberId\":\"user2\",\"businessEventSubscription\":[],\"dataEventSubscription\":[]}", response.getBody(), false);

        response = restTemplate.exchange(
                "/subscription/1/manager/subscriber/subscription/dataeventSubscription",
                HttpMethod.GET,
                httpEntity,
                String.class
        );
        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
        assertJsonEquals(
                "[" +
                "{\"cprList\":null,\"dataEventId\":\"subscription3\",\"kodeId\":\"\",\"cprList\":null,\"cvrList\":null}," +
                "{\"cprList\":null,\"dataEventId\":\"subscription2\",\"kodeId\":\"\",\"cprList\":null,\"cvrList\":null}" +
                "]",
                response.getBody()
        );

    }
}
