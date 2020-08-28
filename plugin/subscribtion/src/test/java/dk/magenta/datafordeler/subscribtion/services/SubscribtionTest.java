package dk.magenta.datafordeler.subscribtion.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import dk.magenta.datafordeler.core.Application;
import dk.magenta.datafordeler.core.database.QueryManager;
import dk.magenta.datafordeler.core.database.SessionManager;
import dk.magenta.datafordeler.core.user.DafoUserManager;

import dk.magenta.datafordeler.subscribtion.data.subscribtionModel.BusinessEventSubscribtion;
import dk.magenta.datafordeler.subscribtion.data.subscribtionModel.DataEventSubscribtion;
import dk.magenta.datafordeler.subscribtion.data.subscribtionModel.Subscriber;
import org.hibernate.Session;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import java.util.*;


@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = Application.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class SubscribtionTest {

    @Autowired
    TestRestTemplate restTemplate;


    @Autowired
    private SessionManager sessionManager;

    @Autowired
    private ObjectMapper objectMapper;
    @SpyBean
    private DafoUserManager dafoUserManager;





    @Test
    public void testSubscribtion() throws Exception {

        try(Session session = sessionManager.getSessionFactory().openSession()) {

            session.save(new Subscriber("testing"));

            List<Subscriber> subscriptions = QueryManager.getAllItems(session, Subscriber.class);

            subscriptions.add(new Subscriber("ww"));

            subscriptions.get(0).addBusinessEventSubscribtion(new BusinessEventSubscribtion("ww"));
            subscriptions.get(1).addDataEventSubscribtion(new DataEventSubscribtion("ww"));

            //There is 5 fathers in the test-file
            //One should not be added to subscribtion becrause the child is more than 18 years old.
            //One person should not be added becrause the father allready exists as a person.
            Assert.assertEquals(2, subscriptions.size());
            Assert.assertEquals(1, subscriptions.get(0).getBusinessEventSubscribtion().size());
            Assert.assertEquals(0, subscriptions.get(1).getBusinessEventSubscribtion().size());

            Assert.assertEquals(0, subscriptions.get(0).getDataEventSubscribtion().size());
            Assert.assertEquals(1, subscriptions.get(1).getDataEventSubscribtion().size());






            HttpEntity<String> httpEntity = new HttpEntity<String>("", new HttpHeaders());

            TestUserDetails testUserDetails = new TestUserDetails();
            //this.applyAccess(testUserDetails);

            //Try fetching with no cpr access rights
            ResponseEntity<String> response = restTemplate.exchange(
                    "/subscribtion/v1/manager",
                    HttpMethod.GET,
                    httpEntity,
                    String.class
            );


            System.out.println(response);












        } finally {

        }
    }
}
