package dk.magenta.datafordeler.core.plugin;

import dk.magenta.datafordeler.core.Application;
import dk.magenta.datafordeler.core.testutil.CallbackController;
import dk.magenta.datafordeler.plugindemo.DemoRegisterManager;
import dk.magenta.datafordeler.plugindemo.model.DemoEntityRecord;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ContextConfiguration;

import java.net.URI;
import java.net.URISyntaxException;

@ContextConfiguration(classes = Application.class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
public class EntityManagerTest extends PluginTestBase {

    @Autowired
    protected TestRestTemplate restTemplate;

    @Autowired
    protected CallbackController callbackController;

    @LocalServerPort
    private int port;


    @BeforeEach
    public void before() {
        DemoRegisterManager registerManager = (DemoRegisterManager) this.plugin.getRegisterManager();
        registerManager.setPort(this.port);
    }

    @AfterEach
    public void after() {
        DemoRegisterManager registerManager = (DemoRegisterManager) this.plugin.getRegisterManager();
        registerManager.setPort(Application.servicePort);
    }

    @Test
    public void testLinks() {
        EntityManager entityManager = this.plugin.getEntityManager(DemoEntityRecord.schema);
        Assertions.assertTrue(entityManager.getRegisterManager() instanceof DemoRegisterManager);
        Assertions.assertEquals(entityManager, entityManager.getRegisterManager().getEntityManager(DemoEntityRecord.schema));
    }

    @Test
    public void testExpandBaseUri() throws URISyntaxException {
        Assertions.assertEquals(new URI("http://localhost/foo/bar"), EntityManager.expandBaseURI(new URI("http://localhost"), "/foo/bar"));
        Assertions.assertEquals(new URI("http://localhost/foo/bar?bat=42#hey"), EntityManager.expandBaseURI(new URI("http://localhost"), "/foo/bar", "bat=42", "hey"));
    }

}
