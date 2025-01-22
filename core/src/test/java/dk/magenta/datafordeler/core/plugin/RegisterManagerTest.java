package dk.magenta.datafordeler.core.plugin;

import dk.magenta.datafordeler.core.Application;
import dk.magenta.datafordeler.core.database.Entity;
import dk.magenta.datafordeler.core.database.Registration;
import dk.magenta.datafordeler.core.exception.DataFordelerException;
import dk.magenta.datafordeler.core.io.PluginSourceData;
import dk.magenta.datafordeler.core.testutil.CallbackController;
import dk.magenta.datafordeler.core.testutil.ExpectorCallback;
import dk.magenta.datafordeler.core.util.ItemInputStream;
import dk.magenta.datafordeler.plugindemo.DemoPlugin;
import dk.magenta.datafordeler.plugindemo.DemoRegisterManager;
import dk.magenta.datafordeler.plugindemo.model.DemoEntityRecord;
import org.junit.*;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = Application.class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
public class RegisterManagerTest extends PluginTestBase {

    @LocalServerPort
    private int port;

    @Autowired
    protected TestRestTemplate restTemplate;

    @Autowired
    protected CallbackController callbackController;

    @Autowired
    private DemoPlugin plugin;

    @Rule
    public ExpectedException exception = ExpectedException.none();

    private class OtherEntity extends Entity {
        @Override
        protected Registration createEmptyRegistration() {
            return null;
        }
    }

    @Before
    public void before() {
        DemoRegisterManager registerManager = (DemoRegisterManager) this.plugin.getRegisterManager();
        registerManager.setPort(this.port);
    }

    @After
    public void after() {
        DemoRegisterManager registerManager = (DemoRegisterManager) this.plugin.getRegisterManager();
        registerManager.setPort(Application.servicePort);
    }

    @Test
    public void testLinks() throws URISyntaxException {
        EntityManager entityManager = this.plugin.getEntityManager(DemoEntityRecord.schema);
        DemoRegisterManager registerManager = (DemoRegisterManager) this.plugin.getRegisterManager();

        Assert.assertEquals(entityManager, registerManager.getEntityManager(DemoEntityRecord.schema));
        Assert.assertEquals(entityManager, registerManager.getEntityManager(new URI("http://localhost:" + this.port)));
        Assert.assertEquals(entityManager, registerManager.getEntityManager(new URI("http://localhost:" + this.port + "/foo")));
        Assert.assertNull(registerManager.getEntityManager(new URI("http://localhost:" + (this.port + 1))));

        Assert.assertNotEquals(entityManager, registerManager.getEntityManager(OtherEntity.class));
    }

    @Test
    public void testHandlesSchema() {
        Assert.assertTrue(this.plugin.handlesSchema(DemoEntityRecord.schema));
        Assert.assertFalse(this.plugin.handlesSchema("foobar"));
        Assert.assertFalse(this.plugin.handlesSchema(DemoEntityRecord.schema+"a"));
    }

    @Test
    public void testGetHandledURISubstrings() {
        DemoRegisterManager registerManager = (DemoRegisterManager) this.plugin.getRegisterManager();
        Assert.assertTrue(registerManager.getHandledURISubstrings().contains("http://localhost:" + this.port));
        Assert.assertFalse(registerManager.getHandledURISubstrings().contains("http://localhost:" + (this.port + 1)));
        Assert.assertFalse(registerManager.getHandledURISubstrings().contains("http://localhost:" + this.port + "/foobar"));
    }

    @Test
    public void testExpandBaseUri() throws URISyntaxException {
        Assert.assertEquals(new URI("http://localhost/foo/bar"), RegisterManager.expandBaseURI(new URI("http://localhost"), "/foo/bar"));
        Assert.assertEquals(new URI("http://localhost/foo/bar?bat=42#hey"), RegisterManager.expandBaseURI(new URI("http://localhost"), "/foo/bar", "bat=42", "hey"));
        Assert.assertEquals(new URI("http://localhost/base/foo/bar?bat=42#hey"), RegisterManager.expandBaseURI(new URI("http://localhost/base"), "/foo/bar", "bat=42", "hey"));
        Assert.assertEquals(new URI("http://localhost/foo/bar?bat=42#hey"), RegisterManager.expandBaseURI(new URI("http", null, "localhost", -1, null, null, null), "/foo/bar", "bat=42", "hey"));
    }


    @Test
    public void testPullEvents() throws DataFordelerException, IOException, InterruptedException, ExecutionException, TimeoutException {

        String checksum = this.hash(UUID.randomUUID().toString());
        String reference = "http://localhost:" + port + "/test/get/" + checksum;
        String uuid = UUID.randomUUID().toString();
        String full = this.getPayload("/referencelookuptest.json")
                .replace("%{checksum}", checksum)
                .replace("%{entityid}", uuid)
                .replace("%{sequenceNumber}", "1");

        String event1 = this.envelopReference("Postnummer", reference);
        String body = this.jsonList(Collections.singletonList(event1), "events");
        ExpectorCallback eventCallback = new ExpectorCallback();
        this.callbackController.addCallbackResponse("/test/getNewEvents", body, eventCallback);

        ItemInputStream<? extends PluginSourceData> dataStream = this.plugin.getRegisterManager().pullEvents(null);

        PluginSourceData data;
        int eventCounter = 0;
        while ((data = dataStream.next()) != null) {
            eventCounter++;
            Assert.assertEquals("msgid", data.getId());
            Assert.assertEquals(reference, data.getReference());
        }
        Assert.assertEquals(1, eventCounter);

        this.callbackController.removeCallback("/test/get/" + checksum);
        this.callbackController.removeCallback("/test/getNewEvents");
        this.callbackController.removeCallback("/test/receipt");
    }

}
