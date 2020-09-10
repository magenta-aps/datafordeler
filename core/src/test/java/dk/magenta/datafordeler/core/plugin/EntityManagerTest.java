package dk.magenta.datafordeler.core.plugin;

import dk.magenta.datafordeler.core.Application;
import dk.magenta.datafordeler.core.testutil.CallbackController;
import dk.magenta.datafordeler.plugindemo.DemoRegisterManager;
import dk.magenta.datafordeler.plugindemo.model.DemoEntityRecord;
import org.junit.*;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.net.URI;
import java.net.URISyntaxException;

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
    public void testExpandBaseUri() throws URISyntaxException {
        Assert.assertEquals(new URI("http://localhost/foo/bar"), EntityManager.expandBaseURI(new URI("http://localhost"), "/foo/bar"));
        Assert.assertEquals(new URI("http://localhost/foo/bar?bat=42#hey"), EntityManager.expandBaseURI(new URI("http://localhost"), "/foo/bar", "bat=42", "hey"));
    }

}
