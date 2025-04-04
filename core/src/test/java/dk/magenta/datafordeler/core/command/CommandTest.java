package dk.magenta.datafordeler.core.command;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import dk.magenta.datafordeler.core.Application;
import dk.magenta.datafordeler.core.gapi.GapiTestBase;
import dk.magenta.datafordeler.core.testutil.ExpectorCallback;
import dk.magenta.datafordeler.core.testutil.Order;
import dk.magenta.datafordeler.core.testutil.TestUserDetails;
import dk.magenta.datafordeler.core.user.DafoUserManager;
import dk.magenta.datafordeler.core.user.UserProfile;
import dk.magenta.datafordeler.plugindemo.DemoPlugin;
import dk.magenta.datafordeler.plugindemo.DemoRegisterManager;
import dk.magenta.datafordeler.plugindemo.DemoRolesDefinition;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.*;

import static org.mockito.Mockito.when;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ContextConfiguration(classes = Application.class)
public class CommandTest extends GapiTestBase {

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoSpyBean
    private DafoUserManager dafoUserManager;

    @Autowired
    private DemoPlugin demoPlugin;

    @LocalServerPort
    private int port;

    @BeforeEach
    public void before() {
        //DemoRegisterManager registerManager = (DemoRegisterManager) this.demoPlugin.getRegisterManager();
        //registerManager.setPort(this.port);
        DemoRegisterManager.setPortOnAll(this.port);
    }

    @AfterEach
    public void after() {
        DemoRegisterManager.setPortOnAll(Application.servicePort);
    }

    @Order(order = 1)
    @Test
    public void pullTest() throws IOException, InterruptedException, URISyntaxException {
        UserProfile testUserProfile = new UserProfile("TestProfile", Arrays.asList(
                DemoRolesDefinition.EXECUTE_DEMO_PULL_ROLE.getRoleName(),
                DemoRolesDefinition.READ_DEMO_PULL_ROLE.getRoleName(),
                DemoRolesDefinition.STOP_DEMO_PULL_ROLE.getRoleName()
        ));

        TestUserDetails testUserDetails = new TestUserDetails();
        testUserDetails.addUserProfile(testUserProfile);
        when(dafoUserManager.getFallbackUser()).thenReturn(testUserDetails);

        String checksum = this.hash(UUID.randomUUID().toString());
        String reference = "http://localhost:"+this.port+"/test/get/" + checksum;
        String uuid = UUID.randomUUID().toString();
        String full = this.getPayload("/referencelookuptest.json")
                .replace("%{checksum}", checksum)
                .replace("%{entityid}", uuid)
                .replace("%{sequenceNumber}", "1");

        ExpectorCallback lookupCallback = new ExpectorCallback();
        this.callbackController.addCallbackResponse("/test/get/" + checksum, full, lookupCallback);

        String event1 = this.envelopReference("Postnummer", reference);
        String eventsBody = this.jsonList(Collections.singletonList(event1), "events");
        ExpectorCallback eventCallback = new ExpectorCallback();
        this.callbackController.addCallbackResponse("/test/getNewEvents", eventsBody, eventCallback);

        ExpectorCallback receiptCallback = new ExpectorCallback();
        this.callbackController.addCallbackResponse("/test/receipt", "", receiptCallback);


        String body = "{\"plugin\":\"Demo\",\"foo\":\"bar\"}";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> httpPostEntity = new HttpEntity<String>(body, headers);
        ResponseEntity<String> postResponse = this.restTemplate.exchange("/command/pull", HttpMethod.POST, httpPostEntity, String.class);
        Assertions.assertNotNull(postResponse);
        Assertions.assertEquals(200, postResponse.getStatusCodeValue());
        JsonNode postResponseNode = objectMapper.readTree(postResponse.getBody());

        Assertions.assertEquals("queued", postResponseNode.get("status").asText());
        Assertions.assertEquals("pull", postResponseNode.get("commandName").asText());
        Assertions.assertNotNull(postResponseNode.get("received").asText());
        long id = postResponseNode.get("id").asLong();

        String status = null;
        for (int i=0; i<60 && (status == null || "queued".equals(status)); i++) {
            HttpEntity<String> httpGetEntity = new HttpEntity<String>("", headers);
            ResponseEntity<String> getResponse = this.restTemplate.exchange("/command/" + id, HttpMethod.GET, httpGetEntity, String.class);
            Assertions.assertNotNull(getResponse);
            Assertions.assertEquals(200, getResponse.getStatusCodeValue());
            JsonNode getResponseNode = objectMapper.readTree(getResponse.getBody());
            status = getResponseNode.get("status").asText();
            Assertions.assertEquals("pull", getResponseNode.get("commandName").asText());
            Assertions.assertNotNull(getResponseNode.get("received").asText());
            String commandBody = getResponseNode.get("commandBody").asText();
            Assertions.assertEquals("bar", objectMapper.readTree(commandBody).get("foo").asText());
            Thread.sleep(1000);
        }
        Assertions.assertTrue("running".equals(status) || "successful".equals(status));

        HttpEntity<String> httpDeleteEntity = new HttpEntity<String>("", headers);
        ResponseEntity<String> deleteResponse = this.restTemplate.exchange("/command/"+id, HttpMethod.DELETE, httpDeleteEntity, String.class);
        Assertions.assertNotNull(deleteResponse);
        Assertions.assertEquals(200, deleteResponse.getStatusCodeValue());
        JsonNode deleteResponseNode = objectMapper.readTree(deleteResponse.getBody());
        status = deleteResponseNode.get("status").asText();
        Assertions.assertTrue("cancelled".equals(status) || "successful".equals(status));
        Assertions.assertEquals("pull", deleteResponseNode.get("commandName").asText());
        Assertions.assertNotNull(deleteResponseNode.get("received").asText());
    }


    @Order(order = 2)
    @Test
    public void readStopAccessDeniedTest() throws IOException, InterruptedException {

        UserProfile testUserProfile = new UserProfile("TestProfile", Arrays.asList(
                DemoRolesDefinition.EXECUTE_DEMO_PULL_ROLE.getRoleName()
        ));

        TestUserDetails testUserDetails = new TestUserDetails();
        testUserDetails.addUserProfile(testUserProfile);
        when(dafoUserManager.getFallbackUser()).thenReturn(testUserDetails);

        String checksum = this.hash(UUID.randomUUID().toString());
        String reference = "http://localhost:8444/test/get/" + checksum;
        String uuid = UUID.randomUUID().toString();
        String full = this.getPayload("/referencelookuptest.json")
                .replace("%{checksum}", checksum)
                .replace("%{entityid}", uuid)
                .replace("%{sequenceNumber}", "1");

        ExpectorCallback lookupCallback = new ExpectorCallback();
        this.callbackController.addCallbackResponse("/test/get/" + checksum, full, lookupCallback);

        String event1 = this.envelopReference("Postnummer", reference);
        String eventsBody = this.jsonList(Collections.singletonList(event1), "events");
        ExpectorCallback eventCallback = new ExpectorCallback();
        this.callbackController.addCallbackResponse("/test/getNewEvents", eventsBody, eventCallback);

        ExpectorCallback receiptCallback = new ExpectorCallback();
        this.callbackController.addCallbackResponse("/test/receipt", "", receiptCallback);


        String body = "{\"plugin\":\"Demo\"}";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> httpPostEntity = new HttpEntity<String>(body, headers);
        ResponseEntity<String> postResponse = this.restTemplate.exchange("/command/pull", HttpMethod.POST, httpPostEntity, String.class);
        Assertions.assertNotNull(postResponse);
        Assertions.assertEquals(200, postResponse.getStatusCodeValue());
        JsonNode postResponseNode = objectMapper.readTree(postResponse.getBody());
        Assertions.assertEquals("queued", postResponseNode.get("status").asText());
        Assertions.assertEquals("pull", postResponseNode.get("commandName").asText());
        Assertions.assertNotNull(postResponseNode.get("received").asText());
        long id = postResponseNode.get("id").asLong();

        HttpEntity<String> httpGetEntity = new HttpEntity<String>("", headers);
        ResponseEntity<String> getResponse = this.restTemplate.exchange("/command/" + id, HttpMethod.GET, httpGetEntity, String.class);
        Assertions.assertNotNull(getResponse);
        Assertions.assertEquals(200, getResponse.getStatusCodeValue());

        HttpEntity<String> httpDeleteEntity = new HttpEntity<String>("", headers);
        ResponseEntity<String> deleteResponse = this.restTemplate.exchange("/command/"+id, HttpMethod.DELETE, httpDeleteEntity, String.class);
        Assertions.assertNotNull(deleteResponse);
        Assertions.assertEquals(200, deleteResponse.getStatusCodeValue());
    }


    @Order(order = 3)
    @Test
    public void startAccessDeniedTest1() throws IOException, InterruptedException {
        UserProfile testUserProfile = new UserProfile("TestProfile", Collections.emptyList());
        TestUserDetails testUserDetails = new TestUserDetails();
        testUserDetails.addUserProfile(testUserProfile);
        when(dafoUserManager.getFallbackUser()).thenReturn(testUserDetails);

        String body = "{\"plugin\":\"Demo\"}";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> httpPostEntity = new HttpEntity<String>(body, headers);
        ResponseEntity<String> postResponse = this.restTemplate.exchange("/command/pull", HttpMethod.POST, httpPostEntity, String.class);
        Assertions.assertNotNull(postResponse);
        Assertions.assertEquals(403, postResponse.getStatusCodeValue());
    }

    @Test
    public void startAccessDeniedTest2() throws IOException, InterruptedException {
        String body = "{\"plugin\":\"Demo\"}";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> httpPostEntity = new HttpEntity<String>(body, headers);
        ResponseEntity<String> postResponse = this.restTemplate.exchange("/command/pull", HttpMethod.POST, httpPostEntity, String.class);
        Assertions.assertNotNull(postResponse);
        Assertions.assertEquals(403, postResponse.getStatusCodeValue());
    }

    private String jsonList(List<String> jsonData, String listKey) {
        StringJoiner sj = new StringJoiner(",");
        for (String j : jsonData) {
            sj.add(j);
        }
        return "{\""+listKey+"\":["+sj.toString()+"]}";
    }

}
