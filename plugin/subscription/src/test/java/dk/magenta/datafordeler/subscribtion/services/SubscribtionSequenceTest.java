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

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;


@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = Application.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class SubscribtionSequenceTest {

    @Autowired
    TestRestTemplate restTemplate;


    @Autowired
    private SessionManager sessionManager;

    @Autowired
    private ObjectMapper objectMapper;
    @SpyBean
    private DafoUserManager dafoUserManager;

    MockMvc mvc;





    private void applyAccess(TestUserDetails testUserDetails) {
        when(dafoUserManager.getFallbackUser()).thenReturn(testUserDetails);
    }





    /**
     * Test that it is possible to create a new subscription
     * @throws Exception
     */
    @Test
    public void testSequenceToBePerformedWithCustomer() throws Exception {

        HttpEntity<String> httpEntity = new HttpEntity<String>("", new HttpHeaders());
        TestUserDetails testUserDetails = new TestUserDetails();
        this.applyAccess(testUserDetails);

        ResponseEntity<String> response = restTemplate.exchange(
                "/subscriptionplugin/v1/manager/subscriber/list",
                HttpMethod.GET,
                httpEntity,
                String.class
        );

        JSONAssert.assertEquals("[]", response.getBody(), false);

        testUserDetails.setIdentity("myCreatedUser");
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
                "/subscriptionplugin/v1/manager/subscriber/list",
                HttpMethod.GET,
                httpEntity,
                String.class
        );

        JSONAssert.assertEquals("[{\"subscriberId\":\"myCreatedUser\",\"businessEventSubscription\":[]," +
                "\"dataEventSubscription\":[]}]", response.getBody(), false);

        response = restTemplate.exchange(
                "/subscriptionplugin/v1/manager/subscriber/businessEventSubscribtion/list",
                HttpMethod.GET,
                httpEntity,
                String.class
        );
        Assert.assertEquals(HttpStatus.OK, response.getStatusCode());

        JSONAssert.assertEquals("[]", response.getBody(), false);


        response = restTemplate.exchange(
                "/subscriptionplugin/v1/manager/subscriber/businessEventSubscribtion/?businessEventId=newBusinessEventId&kodeId=A04&cprList=list01",
                HttpMethod.POST,
                httpEntity,
                String.class
        );
        Assert.assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());


        //ADD an element to the CPR-list
        httpEntity = new HttpEntity<String>("list01", new HttpHeaders());
        response = restTemplate.exchange(
                "/cprlistplugin/v1/manager/subscriber/cprList/",
                HttpMethod.POST,
                httpEntity,
                String.class
        );
        Assert.assertEquals(HttpStatus.OK, response.getStatusCode());

        //ADD an element to the CPR-list
        httpEntity = new HttpEntity<String>("list02", new HttpHeaders());
        response = restTemplate.exchange(
                "/cprlistplugin/v1/manager/subscriber/cprList/",
                HttpMethod.POST,
                httpEntity,
                String.class
        );
        Assert.assertEquals(HttpStatus.OK, response.getStatusCode());

        //Confirm that the CPR-list has two elements
        response = restTemplate.exchange(
                "/cprlistplugin/v1/manager/subscriber/cprList/list",
                HttpMethod.GET,
                httpEntity,
                String.class
        );

        Assert.assertEquals(HttpStatus.OK, response.getStatusCode());
        JSONAssert.assertEquals("[" +
                "{\"listId\":\"list01\"},{\"listId\":\"list02\"}]", response.getBody(), false);

        //ADD an element to the CPR-list
        httpEntity = new HttpEntity<String>("list01", new HttpHeaders());
        response = restTemplate.exchange(
                "/cvrlistplugin/v1/manager/subscriber/cvrList/",
                HttpMethod.POST,
                httpEntity,
                String.class
        );
        Assert.assertEquals(HttpStatus.OK, response.getStatusCode());

        //ADD an element to the CPR-list
        httpEntity = new HttpEntity<String>("list02", new HttpHeaders());
        response = restTemplate.exchange(
                "/cvrlistplugin/v1/manager/subscriber/cvrList/",
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
        JSONAssert.assertEquals("[" +
                "{\"listId\":\"list01\"},{\"listId\":\"list02\"}]", response.getBody(), false);

        //Manage businesseventsubscribtions
        response = restTemplate.exchange(
                "/subscriptionplugin/v1/manager/subscriber/businessEventSubscribtion/?businessEventId=newBusinessEventId&kodeId=A04&cprList=list01",
                HttpMethod.POST,
                httpEntity,
                String.class
        );
        Assert.assertEquals(HttpStatus.OK, response.getStatusCode());


        response = restTemplate.exchange(
                "/subscriptionplugin/v1/manager/subscriber/businessEventSubscribtion/list",
                HttpMethod.GET,
                httpEntity,
                String.class
        );
        Assert.assertEquals(HttpStatus.OK, response.getStatusCode());

        JSONAssert.assertEquals("[" +
                "{\"cprList\":{\"listId\":\"list01\"},\"businessEventId\":\"newBusinessEventId\",\"kodeId\":\"A04\"}]", response.getBody(), false);


        response = restTemplate.exchange(
                "/subscriptionplugin/v1/manager/subscriber/businessEventSubscribtion/?businessEventId=newBusinessEventId&kodeId=A05&cprList=list02",
                HttpMethod.PUT,
                httpEntity,
                String.class
        );
        Assert.assertEquals(HttpStatus.OK, response.getStatusCode());

        response = restTemplate.exchange(
                "/subscriptionplugin/v1/manager/subscriber/businessEventSubscribtion/list",
                HttpMethod.GET,
                httpEntity,
                String.class
        );
        Assert.assertEquals(HttpStatus.OK, response.getStatusCode());

        JSONAssert.assertEquals("[" +
                "{\"cprList\":{\"listId\":\"list02\"},\"businessEventId\":\"newBusinessEventId\",\"kodeId\":\"A05\"}]", response.getBody(), false);

        //Manage dataeventsubscribtions
        response = restTemplate.exchange(
                "/subscriptionplugin/v1/manager/subscriber/dataEventSubscribtion/?dataEventId=newDataEventId&kodeId=A04&cprList=list01",
                HttpMethod.POST,
                httpEntity,
                String.class
        );
        Assert.assertEquals(HttpStatus.OK, response.getStatusCode());


        response = restTemplate.exchange(
                "/subscriptionplugin/v1/manager/subscriber/dataEventSubscribtion/list",
                HttpMethod.GET,
                httpEntity,
                String.class
        );
        Assert.assertEquals(HttpStatus.OK, response.getStatusCode());

        JSONAssert.assertEquals("[" +
                "{\"cprList\":{\"listId\":\"list01\"},\"dataEventId\":\"newDataEventId\",\"kodeId\":\"A04\"}]", response.getBody(), false);


        response = restTemplate.exchange(
                "/subscriptionplugin/v1/manager/subscriber/dataEventSubscribtion/?dataEventId=newDataEventId&kodeId=A05&cprList=list02",
                HttpMethod.PUT,
                httpEntity,
                String.class
        );
        Assert.assertEquals(HttpStatus.OK, response.getStatusCode());

        response = restTemplate.exchange(
                "/subscriptionplugin/v1/manager/subscriber/dataEventSubscribtion/list",
                HttpMethod.GET,
                httpEntity,
                String.class
        );
        Assert.assertEquals(HttpStatus.OK, response.getStatusCode());

        JSONAssert.assertEquals("[" +
                "{\"cprList\":{\"listId\":\"list02\"},\"dataEventId\":\"newDataEventId\",\"kodeId\":\"A05\"}]", response.getBody(), false);

        response = restTemplate.exchange(
                "/subscriptionplugin/v1/manager/subscriber/dataEventSubscribtion/?dataEventId=newDataEventId&kodeId=A05&cvrList=list02",
                HttpMethod.PUT,
                httpEntity,
                String.class
        );
        Assert.assertEquals(HttpStatus.OK, response.getStatusCode());

        response = restTemplate.exchange(
                "/subscriptionplugin/v1/manager/subscriber/dataEventSubscribtion/list",
                HttpMethod.GET,
                httpEntity,
                String.class
        );
        Assert.assertEquals(HttpStatus.OK, response.getStatusCode());

        JSONAssert.assertEquals("[" +
                "{\"cprList\":{\"listId\":\"list02\"},\"cvrList\":{\"listId\":\"list02\"},\"dataEventId\":\"newDataEventId\",\"kodeId\":\"A05\"}]", response.getBody(), false);

    }


}
