package dk.magenta.datafordeler.subscription.services;

import dk.magenta.datafordeler.core.Application;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.*;
import org.springframework.test.context.ContextConfiguration;

import static org.mockito.Mockito.when;


@ContextConfiguration(classes = Application.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class SubscriptionSequenceTest extends TestBase {


    private void applyAccess(TestUserDetails testUserDetails) {
        when(dafoUserManager.getFallbackUser()).thenReturn(testUserDetails);
    }



    /**
     * Test that it is possible to create a new subscription
     */
    @Test
    public void testSequenceToBePerformedWithCustomer() {

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add("uxp-client", "PITU/GOV/DIA/magenta_services".replaceAll("/", "_"));

        HttpEntity<String> httpEntity = new HttpEntity<String>("", httpHeaders);
        TestUserDetails testUserDetails = new TestUserDetails();
        this.applyAccess(testUserDetails);

        ResponseEntity<String> response = restTemplate.exchange(
                "/subscription/1/manager/subscriber",
                HttpMethod.GET,
                httpEntity,
                String.class
        );

        JSONAssert.assertEquals("[]", response.getBody(), false);

        this.applyAccess(testUserDetails);

        //Try fetching with no cpr access rights
        response = restTemplate.exchange(
                "/subscription/1/manager/subscriber/",
                HttpMethod.POST,
                httpEntity,
                String.class
        );
        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());

        //Try fetching with no cpr access rights
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

        JSONAssert.assertEquals("[{\"subscriberId\":\"PITU_GOV_DIA_magenta_services\",\"businessEventSubscription\":[]," +
                "\"dataEventSubscription\":[]}]", response.getBody(), false);

        response = restTemplate.exchange(
                "/subscription/1/manager/subscriber/subscription/businesseventSubscription",
                HttpMethod.GET,
                httpEntity,
                String.class
        );
        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());

        JSONAssert.assertEquals("[]", response.getBody(), false);


        response = restTemplate.exchange(
                "/subscription/1/manager/subscriber/subscription/businesseventSubscription/?businessEventId=newBusinessEventId&kodeId=A04&cprList=list01",
                HttpMethod.POST,
                httpEntity,
                String.class
        );
        Assertions.assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());

        //Confirm that the CPR-list has two elements
        response = restTemplate.exchange(
                "/subscription/1/manager/subscriber/cprList",
                HttpMethod.GET,
                httpEntity,
                String.class
        );

        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
        JSONAssert.assertEquals("[]", response.getBody(), false);


        //ADD a new CPR-list
        httpEntity = new HttpEntity<String>("", httpHeaders);
        response = restTemplate.exchange(
                "/subscription/1/manager/subscriber/cprList/?cprList=list01",
                HttpMethod.POST,
                httpEntity,
                String.class
        );
        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());

        //ADD an element to the CPR-list
        httpEntity = new HttpEntity<String>("", httpHeaders);
        response = restTemplate.exchange(
                "/subscription/1/manager/subscriber/cprList/?cprList=list02",
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
        JSONAssert.assertEquals("[" +
                "{\"listId\":\"list01\"},{\"listId\":\"list02\"}]", response.getBody(), false);

        //ADD an element to the CPR-list
        httpEntity = new HttpEntity<String>("", httpHeaders);
        response = restTemplate.exchange(
                "/subscription/1/manager/subscriber/cvrList/?cvrList=list01",
                HttpMethod.POST,
                httpEntity,
                String.class
        );
        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());

        //ADD an element to the CPR-list
        httpEntity = new HttpEntity<String>("", httpHeaders);
        response = restTemplate.exchange(
                "/subscription/1/manager/subscriber/cvrList/?cvrList=list02",
                HttpMethod.POST,
                httpEntity,
                String.class
        );
        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());

        //Confirm that the CPR-list has two elements
        response = restTemplate.exchange(
                "/subscription/1/manager/subscriber/cvrList",
                HttpMethod.GET,
                httpEntity,
                String.class
        );

        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
        JSONAssert.assertEquals("[" +
                "{\"listId\":\"list01\"},{\"listId\":\"list02\"}]", response.getBody(), false);

        System.out.println("NOW!");
        //Manage businesseventsubscriptions
        response = restTemplate.exchange(
                "/subscription/1/manager/subscriber/subscription/businesseventSubscription/?businessEventId=newBusinessEventId&kodeId=A04&cprList=list01",
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

        JSONAssert.assertEquals("[" +
                "{\"cprList\":{\"listId\":\"list01\"},\"businessEventId\":\"newBusinessEventId\",\"kodeId\":\"A04\"}]", response.getBody(), false);


        response = restTemplate.exchange(
                "/subscription/1/manager/subscriber/subscription/businesseventSubscription/newBusinessEventId/?kodeId=A05&cprList=list02",
                HttpMethod.PUT,
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

        JSONAssert.assertEquals("[" +
                "{\"cprList\":{\"listId\":\"list02\"},\"businessEventId\":\"newBusinessEventId\",\"kodeId\":\"A05\"}]", response.getBody(), false);

        //Manage dataeventsubscriptions
        response = restTemplate.exchange(
                "/subscription/1/manager/subscriber/subscription/dataeventSubscription/?dataEventId=newDataEventId&kodeId=A04&cprList=list01",
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

        JSONAssert.assertEquals("[" +
                "{\"cprList\":{\"listId\":\"list01\"},\"dataEventId\":\"newDataEventId\",\"kodeId\":\"A04\"}]", response.getBody(), false);


        response = restTemplate.exchange(
                "/subscription/1/manager/subscriber/subscription/dataeventSubscription/newDataEventId/?kodeId=A05&cprList=list02",
                HttpMethod.PUT,
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

        JSONAssert.assertEquals("[" +
                "{\"cprList\":{\"listId\":\"list02\"},\"dataEventId\":\"newDataEventId\",\"kodeId\":\"A05\"}]", response.getBody(), false);

        response = restTemplate.exchange(
                "/subscription/1/manager/subscriber/subscription/dataeventSubscription/newDataEventId/?kodeId=A05&cvrList=list02",
                HttpMethod.PUT,
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

        JSONAssert.assertEquals("[" +
                "{\"cprList\":{\"listId\":\"list02\"},\"cvrList\":{\"listId\":\"list02\"},\"dataEventId\":\"newDataEventId\",\"kodeId\":\"A05\"}]", response.getBody(), false);

    }


}
