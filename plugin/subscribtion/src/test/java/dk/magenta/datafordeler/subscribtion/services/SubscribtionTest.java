package dk.magenta.datafordeler.subscribtion.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import dk.magenta.datafordeler.core.Application;
import dk.magenta.datafordeler.core.database.QueryManager;
import dk.magenta.datafordeler.core.database.SessionManager;
import dk.magenta.datafordeler.core.io.ImportMetadata;
import dk.magenta.datafordeler.core.user.DafoUserManager;

import dk.magenta.datafordeler.subscribtion.data.subscribtionModel.BusinessEventSubscribtion;
import dk.magenta.datafordeler.subscribtion.data.subscribtionModel.DataEventSubscribtion;
import dk.magenta.datafordeler.subscribtion.data.subscribtionModel.Subscriber;
import org.hibernate.Session;
import org.hibernate.Transaction;
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

import java.util.*;

import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;




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






    @Before
    public void setUp() throws Exception {
        //Initiate a list of subscribtions
        try(Session session = sessionManager.getSessionFactory().openSession()) {

            Transaction transaction = session.beginTransaction();

            List<Subscriber> subscriptions = QueryManager.getAllItems(session, Subscriber.class);

            subscriptions.add(new Subscriber("user1"));
            subscriptions.add(new Subscriber("user2"));
            subscriptions.add(new Subscriber("user3"));
            subscriptions.add(new Subscriber("user4"));

            /*subscriptions.get(0).addBusinessEventSubscribtion(new BusinessEventSubscribtion("ww"));
            subscriptions.get(1).addDataEventSubscribtion(new DataEventSubscribtion("ww"));

            //There is 5 fathers in the test-file
            //One should not be added to subscribtion becrause the child is more than 18 years old.
            //One person should not be added becrause the father allready exists as a person.
            Assert.assertEquals(4, subscriptions.size());
            Assert.assertEquals(1, subscriptions.get(0).getBusinessEventSubscribtion().size());
            Assert.assertEquals(0, subscriptions.get(1).getBusinessEventSubscribtion().size());

            Assert.assertEquals(0, subscriptions.get(0).getDataEventSubscribtion().size());
            Assert.assertEquals(1, subscriptions.get(1).getDataEventSubscribtion().size());*/

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
     * Test that it is possible to call a service that deliveres a list of subscribtion that has been created in datafordeler
     */
    @Test
    public void testGetSubscribtionList() {

        HttpEntity<String> httpEntity = new HttpEntity<String>("", new HttpHeaders());
        dk.magenta.datafordeler.subscribtion.services.TestUserDetails testUserDetails = new dk.magenta.datafordeler.subscribtion.services.TestUserDetails();
        this.applyAccess(testUserDetails);

        ResponseEntity<String> response = restTemplate.exchange(
                "/subscribtionplugin/v1/manager/subscriber/list",
                HttpMethod.GET,
                httpEntity,
                String.class
        );
        JSONAssert.assertEquals("[{\"subscriberId\":\"user1\",\"businessEventSubscribtion\":[]," +
                "\"dataEventSubscribtion\":[]},{\"subscriberId\":\"user2\",\"businessEventSubscribtion\":[]," +
                "\"dataEventSubscribtion\":[]},{\"subscriberId\":\"user3\",\"businessEventSubscribtion\":[]," +
                "\"dataEventSubscribtion\":[]},{\"subscriberId\":\"user4\",\"businessEventSubscribtion\":[]," +
                "\"dataEventSubscribtion\":[]}]", response.getBody(), false);

    }

    /**
     * Test that it is possible to find a specific subscribtion
     * @throws Exception
     */
    @Test
    public void testGetSpecificSubscribtions() throws Exception {

        HttpEntity<String> httpEntity = new HttpEntity<String>("", new HttpHeaders());
        dk.magenta.datafordeler.subscribtion.services.TestUserDetails testUserDetails = new dk.magenta.datafordeler.subscribtion.services.TestUserDetails();
        this.applyAccess(testUserDetails);

        ResponseEntity<String> response = restTemplate.exchange(
                "/subscribtionplugin/v1/manager/subscriber/user1",
                HttpMethod.GET,
                httpEntity,
                String.class
        );
        Assert.assertEquals(HttpStatus.OK, response.getStatusCode());
        JSONAssert.assertEquals("{\"subscriberId\":\"user1\",\"businessEventSubscribtion\":[],\"dataEventSubscribtion\":[]}", response.getBody(), false);


        httpEntity = new HttpEntity<String>("", new HttpHeaders());
        testUserDetails = new dk.magenta.datafordeler.subscribtion.services.TestUserDetails();
        this.applyAccess(testUserDetails);

        response = restTemplate.exchange(
                "/subscribtionplugin/v1/manager/subscriber/user2",
                HttpMethod.GET,
                httpEntity,
                String.class
        );
        Assert.assertEquals(HttpStatus.OK, response.getStatusCode());
        JSONAssert.assertEquals("{\"subscriberId\":\"user2\",\"businessEventSubscribtion\":[],\"dataEventSubscribtion\":[]}", response.getBody(), false);

        httpEntity = new HttpEntity<String>("", new HttpHeaders());
        testUserDetails = new dk.magenta.datafordeler.subscribtion.services.TestUserDetails();
        this.applyAccess(testUserDetails);

        //Try fetching with no cpr access rights
        response = restTemplate.exchange(
                "/subscribtionplugin/v1/manager/subscriber/nonExistingUser",
                HttpMethod.GET,
                httpEntity,
                String.class
        );
        Assert.assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());

    }

    /**
     * Test that it is possible to create a new subscribtion
     * @throws Exception
     */
    @Test
    public void testCreateSubscribtion() throws Exception {

        HttpEntity<String> httpEntity = new HttpEntity<String>("", new HttpHeaders());
        dk.magenta.datafordeler.subscribtion.services.TestUserDetails testUserDetails = new dk.magenta.datafordeler.subscribtion.services.TestUserDetails();
        this.applyAccess(testUserDetails);

        ResponseEntity<String> response = restTemplate.exchange(
                "/subscribtionplugin/v1/manager/subscriber/list",
                HttpMethod.GET,
                httpEntity,
                String.class
        );

        JSONAssert.assertEquals("[{\"subscriberId\":\"user1\",\"businessEventSubscribtion\":[]," +
                "\"dataEventSubscribtion\":[]},{\"subscriberId\":\"user2\",\"businessEventSubscribtion\":[]," +
                "\"dataEventSubscribtion\":[]},{\"subscriberId\":\"user3\",\"businessEventSubscribtion\":[]," +
                "\"dataEventSubscribtion\":[]},{\"subscriberId\":\"user4\",\"businessEventSubscribtion\":[]," +
                "\"dataEventSubscribtion\":[]}]", response.getBody(), false);

        httpEntity = new HttpEntity<String>("{\"subscriberId\":\"createdUser1\",\"businessEventSubscribtion\":[],\"dataEventSubscribtion\":[]}", new HttpHeaders());
        testUserDetails = new dk.magenta.datafordeler.subscribtion.services.TestUserDetails();
        this.applyAccess(testUserDetails);

        //Try fetching with no cpr access rights
        response = restTemplate.exchange(
                "/subscribtionplugin/v1/manager/subscriber/create/",
                HttpMethod.POST,
                httpEntity,
                String.class
        );

        response = restTemplate.exchange(
                "/subscribtionplugin/v1/manager/subscriber/list",
                HttpMethod.GET,
                httpEntity,
                String.class
        );

        JSONAssert.assertEquals("[{\"subscriberId\":\"user1\",\"businessEventSubscribtion\":[]," +
                "\"dataEventSubscribtion\":[]},{\"subscriberId\":\"user2\",\"businessEventSubscribtion\":[]," +
                "\"dataEventSubscribtion\":[]},{\"subscriberId\":\"user3\",\"businessEventSubscribtion\":[]," +
                "\"dataEventSubscribtion\":[]},{\"subscriberId\":\"user4\",\"businessEventSubscribtion\":[]," +
                "\"dataEventSubscribtion\":[]},{\"subscriberId\":\"createdUser1\",\"businessEventSubscribtion\":[]," +
                "\"dataEventSubscribtion\":[]}]", response.getBody(), false);



        testUserDetails.setIdentity("myCreatedUser");
        this.applyAccess(testUserDetails);

        //Try fetching with no cpr access rights
        response = restTemplate.exchange(
                "/subscribtionplugin/v1/manager/subscriber/createMy/",
                HttpMethod.POST,
                httpEntity,
                String.class
        );

        response = restTemplate.exchange(
                "/subscribtionplugin/v1/manager/subscriber/list",
                HttpMethod.GET,
                httpEntity,
                String.class
        );

        JSONAssert.assertEquals("[{\"subscriberId\":\"user1\",\"businessEventSubscribtion\":[]," +
                "\"dataEventSubscribtion\":[]},{\"subscriberId\":\"user2\",\"businessEventSubscribtion\":[]," +
                "\"dataEventSubscribtion\":[]},{\"subscriberId\":\"user3\",\"businessEventSubscribtion\":[]," +
                "\"dataEventSubscribtion\":[]},{\"subscriberId\":\"user4\",\"businessEventSubscribtion\":[]," +
                "\"dataEventSubscribtion\":[]},{\"subscriberId\":\"myCreatedUser\",\"businessEventSubscribtion\":[]," +
                "\"dataEventSubscribtion\":[]},{\"subscriberId\":\"createdUser1\",\"businessEventSubscribtion\":[]," +
                "\"dataEventSubscribtion\":[]}]", response.getBody(), false);

    }


    /**
     * Test that it is possible to delete a new subscribtion
     * @throws Exception
     */
    @Test
    public void testDeleteSubscribtion() throws Exception {

        HttpEntity<String> httpEntity = new HttpEntity<String>("", new HttpHeaders());
        dk.magenta.datafordeler.subscribtion.services.TestUserDetails testUserDetails = new dk.magenta.datafordeler.subscribtion.services.TestUserDetails();
        this.applyAccess(testUserDetails);

        ResponseEntity<String> response = restTemplate.exchange(
                "/subscribtionplugin/v1/manager/subscriber/list",
                HttpMethod.GET,
                httpEntity,
                String.class
        );

        JSONAssert.assertEquals("[{\"subscriberId\":\"user1\",\"businessEventSubscribtion\":[]," +
                "\"dataEventSubscribtion\":[]},{\"subscriberId\":\"user2\",\"businessEventSubscribtion\":[]," +
                "\"dataEventSubscribtion\":[]},{\"subscriberId\":\"user3\",\"businessEventSubscribtion\":[]," +
                "\"dataEventSubscribtion\":[]},{\"subscriberId\":\"user4\",\"businessEventSubscribtion\":[]," +
                "\"dataEventSubscribtion\":[]}]", response.getBody(), false);

        httpEntity = new HttpEntity<String>("{\"subscriberId\":\"createdUser1\",\"businessEventSubscribtion\":[],\"dataEventSubscribtion\":[]}", new HttpHeaders());
        testUserDetails = new dk.magenta.datafordeler.subscribtion.services.TestUserDetails();
        this.applyAccess(testUserDetails);

        //Try fetching with no cpr access rights
        response = restTemplate.exchange(
                "/subscribtionplugin/v1/manager/subscriber/delete/user2",
                HttpMethod.DELETE,
                httpEntity,
                String.class
        );
        Assert.assertEquals(HttpStatus.OK, response.getStatusCode());
        JSONAssert.assertEquals("{\"subscriberId\":\"user2\",\"businessEventSubscribtion\":[],\"dataEventSubscribtion\":[]}", response.getBody(), false);

        response = restTemplate.exchange(
                "/subscribtionplugin/v1/manager/subscriber/list",
                HttpMethod.GET,
                httpEntity,
                String.class
        );
        JSONAssert.assertEquals("[{\"subscriberId\":\"user1\",\"businessEventSubscribtion\":[]," +
                "\"dataEventSubscribtion\":[]},{\"subscriberId\":\"user3\",\"businessEventSubscribtion\":[]," +
                "\"dataEventSubscribtion\":[]},{\"subscriberId\":\"user4\",\"businessEventSubscribtion\":[]," +
                "\"dataEventSubscribtion\":[]}]", response.getBody(), false);
    }





    /**
     * Test that it is possible to find a specific subscribtion
     * @throws Exception
     */
    @Test
    public void testGetSubscribtions() throws Exception {

        try(Session session = sessionManager.getSessionFactory().openSession()) {
            Transaction transaction = session.beginTransaction();
            Subscriber subscriber =  new Subscriber("user77");
            subscriber.addBusinessEventSubscribtion(new BusinessEventSubscribtion("subscribtion1"));
            subscriber.addBusinessEventSubscribtion(new BusinessEventSubscribtion("subscribtion2"));
            subscriber.addBusinessEventSubscribtion(new BusinessEventSubscribtion("subscribtion3"));
            session.save(subscriber);
            transaction.commit();
        }




        HttpEntity<String> httpEntity = new HttpEntity<String>("", new HttpHeaders());
        dk.magenta.datafordeler.subscribtion.services.TestUserDetails testUserDetails = new dk.magenta.datafordeler.subscribtion.services.TestUserDetails();
        this.applyAccess(testUserDetails);


        ResponseEntity<String> response = restTemplate.exchange(
                "/subscribtionplugin/v1/manager/subscriber/businessEventSubscribtion/list/user77",
                HttpMethod.GET,
                httpEntity,
                String.class
        );
        Assert.assertEquals(HttpStatus.OK, response.getStatusCode());
        JSONAssert.assertEquals("[{\"cprList\":null,\"businessEventId\":\"subscribtion3\"},{\"cprList\":null,\"businessEventId\":\"subscribtion1\"},{\"cprList\":null,\"businessEventId\":\"subscribtion2\"}]", response.getBody(), false);


    }























    //@Test
    public void testGetSubscribtionListkk() throws Exception {

        HttpEntity<String> httpEntity = new HttpEntity<String>("", new HttpHeaders());
        dk.magenta.datafordeler.subscribtion.services.TestUserDetails testUserDetails = new dk.magenta.datafordeler.subscribtion.services.TestUserDetails();
            this.applyAccess(testUserDetails);

            //Try fetching with no cpr access rights
        ResponseEntity<String> response = restTemplate.exchange(
                    "/subscribtionplugin/v1/manager/subscriber/user1",
                    HttpMethod.GET,
                    httpEntity,
                    String.class
            );

        JSONAssert.assertEquals("{\"subscriberId\":\"user1\",\"businessEventSubscribtion\":[],\"dataEventSubscribtion\":[]}", response.getBody(), false);
            System.out.println(response);







            httpEntity = new HttpEntity<String>("{\"subscriberId\":\"user7\",\"businessEventSubscribtion\":[{\"cprList\":null,\"businessEventIdId\":\"ww\"}],\"dataEventSubscribtion\":[]}", new HttpHeaders());
            testUserDetails = new dk.magenta.datafordeler.subscribtion.services.TestUserDetails();
            this.applyAccess(testUserDetails);


            //Try fetching with no cpr access rights
            response = restTemplate.exchange(
                    "/subscribtionplugin/v1/manager/subscriber/create/",
                    HttpMethod.POST,
                    httpEntity,
                    String.class
            );
            System.out.println(response);



            /*try(Session session = sessionManager.getSessionFactory().openSession()) {

                HttpEntity<String> httpEntity = new HttpEntity<String>("", new HttpHeaders());
                ResponseEntity<String> response = restTemplate.exchange(
                    "/subscribtionplugin/v1/manager/subscriber/list",
                    HttpMethod.GET,
                    httpEntity,
                    String.class
            );
            System.out.println(response);






        } finally {

        }*/
    }
}
