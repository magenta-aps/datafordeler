package dk.magenta.datafordeler.core.plugin;

import dk.magenta.datafordeler.core.Application;
import dk.magenta.datafordeler.plugindemo.DemoRegisterManager;
import dk.magenta.datafordeler.plugindemo.DemoRolesDefinition;
import dk.magenta.datafordeler.plugindemo.model.DemoEntityRecord;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ContextConfiguration;

import java.net.URI;
import java.net.URISyntaxException;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ContextConfiguration(classes = Application.class)
public class SinglePluginTest extends PluginTestBase {

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
    public void testGetVersion() {
        Assertions.assertEquals(1L, this.plugin.getVersion());
    }

    @Test
    public void testGetEntityManager() throws URISyntaxException {
        URI uri = new URI("http://localhost:" + this.port);
        Assertions.assertTrue(this.plugin.getRegisterManager() instanceof RegisterManager);
        Assertions.assertTrue(this.plugin.getRegisterManager() instanceof DemoRegisterManager);
        Assertions.assertEquals(this.plugin.getEntityManager(DemoEntityRecord.schema), this.plugin.getRegisterManager().getEntityManager(DemoEntityRecord.class));
        Assertions.assertEquals(this.plugin.getEntityManager(uri), this.plugin.getEntityManager(DemoEntityRecord.schema));
    }


    @Test
    public void testHandlesSchema() throws URISyntaxException {
        Assertions.assertTrue(this.plugin.handlesSchema(DemoEntityRecord.schema));
        Assertions.assertFalse(this.plugin.handlesSchema("foobar"));
    }


    @Test
    public void testGetRolesDefinition() throws URISyntaxException {
        Assertions.assertTrue(this.plugin.getRolesDefinition() instanceof DemoRolesDefinition);
    }
}
