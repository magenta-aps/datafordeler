package dk.magenta.datafordeler.core.plugin;

import dk.magenta.datafordeler.core.Application;
import dk.magenta.datafordeler.core.io.Receipt;
import dk.magenta.datafordeler.core.testutil.CallbackController;
import dk.magenta.datafordeler.core.testutil.ExpectorCallback;
import dk.magenta.datafordeler.plugindemo.DemoEntityManager;
import dk.magenta.datafordeler.plugindemo.DemoRegisterManager;
import dk.magenta.datafordeler.plugindemo.model.DemoEntityRecord;
import org.junit.*;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Map;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = Application.class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
public class EntityManagerTest extends PluginTestBase {

    @Autowired
    protected TestRestTemplate restTemplate;

    @Autowired
    protected CallbackController callbackController;

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @LocalServerPort
    private int port;


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
    public void testLinks() {
        EntityManager entityManager = this.plugin.getEntityManager(DemoEntityRecord.schema);
        Assert.assertTrue(entityManager.getRegisterManager() instanceof DemoRegisterManager);
        Assert.assertEquals(entityManager, entityManager.getRegisterManager().getEntityManager(DemoEntityRecord.schema));
    }

    @Test
    public void testReceipts() throws IOException {
        DemoEntityManager entityManager = (DemoEntityManager) this.plugin.getEntityManager(DemoEntityRecord.schema);
        Receipt receipt1 = new Receipt("Testing receipt 1", OffsetDateTime.now());
        Receipt receipt2 = new Receipt("Testing receipt 2", OffsetDateTime.now());
        ExpectorCallback receiptResponder = new ExpectorCallback();
        this.callbackController.addCallbackResponse("/test/receipt", "response body", receiptResponder);
        try {
            entityManager.setBaseEndpoint(new URI("http", null, "localhost", this.port, "/test", null, null));
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        ArrayList<Receipt> receipts = new ArrayList<>();
        receipts.add(receipt1);
        receipts.add(receipt2);
        Map<Receipt, Integer> responses = entityManager.sendReceipts(receipts);
        Assert.assertNotNull(responses.get(receipt1));
        Assert.assertNotNull(responses.get(receipt2));
        Assert.assertEquals(200, responses.get(receipt1).intValue());
        Assert.assertEquals(200, responses.get(receipt2).intValue());
    }

    @Test
    public void testExpandBaseUri() throws URISyntaxException {
        Assert.assertEquals(new URI("http://localhost/foo/bar"), EntityManager.expandBaseURI(new URI("http://localhost"), "/foo/bar"));
        Assert.assertEquals(new URI("http://localhost/foo/bar?bat=42#hey"), EntityManager.expandBaseURI(new URI("http://localhost"), "/foo/bar", "bat=42", "hey"));
    }

}
