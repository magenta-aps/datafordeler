package dk.magenta.datafordeler.core.plugin;

import dk.magenta.datafordeler.core.Application;
import dk.magenta.datafordeler.plugindemo.DemoRegisterManager;
import dk.magenta.datafordeler.plugindemo.DemoRolesDefinition;
import dk.magenta.datafordeler.plugindemo.model.DemoEntityRecord;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.net.URI;
import java.net.URISyntaxException;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = Application.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class SinglePluginTest extends PluginTestBase {

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
    public void testGetVersion() {
        Assert.assertEquals(1L, this.plugin.getVersion());
    }

    @Test
    public void testGetEntityManager() throws URISyntaxException {
        URI uri = new URI("http://localhost:" + this.port);
        Assert.assertTrue(this.plugin.getRegisterManager() instanceof RegisterManager);
        Assert.assertTrue(this.plugin.getRegisterManager() instanceof DemoRegisterManager);
        Assert.assertEquals(this.plugin.getEntityManager(DemoEntityRecord.schema), this.plugin.getRegisterManager().getEntityManager(DemoEntityRecord.class));
        Assert.assertEquals(this.plugin.getEntityManager(uri), this.plugin.getEntityManager(DemoEntityRecord.schema));
    }


    @Test
    public void testHandlesSchema() throws URISyntaxException {
        Assert.assertTrue(this.plugin.handlesSchema(DemoEntityRecord.schema));
        Assert.assertFalse(this.plugin.handlesSchema("foobar"));
    }


    @Test
    public void testGetRolesDefinition() throws URISyntaxException {
        Assert.assertTrue(this.plugin.getRolesDefinition() instanceof DemoRolesDefinition);
    }
}
