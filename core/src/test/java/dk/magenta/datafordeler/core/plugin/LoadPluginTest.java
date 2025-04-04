package dk.magenta.datafordeler.core.plugin;

import dk.magenta.datafordeler.core.Application;
import dk.magenta.datafordeler.core.PluginManager;
import dk.magenta.datafordeler.core.database.SessionManager;
import dk.magenta.datafordeler.core.testutil.Order;
import dk.magenta.datafordeler.plugindemo.DemoEntityManager;
import dk.magenta.datafordeler.plugindemo.DemoPlugin;
import dk.magenta.datafordeler.plugindemo.configuration.DemoConfiguration;
import dk.magenta.datafordeler.plugindemo.model.DemoEntityRecord;
import org.hibernate.Session;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;

import java.util.List;
@SpringBootTest
@ContextConfiguration(classes = Application.class)
public class LoadPluginTest {

    @Autowired
    PluginManager pluginManager;

    @Autowired
    SessionManager sessionManager;

    @Test
    @Order(order=1)
    public void listDemoPlugin() {
        List<Plugin> plugins = pluginManager.getPlugins();
        Assertions.assertNotNull(plugins);
        Assertions.assertFalse(plugins.isEmpty(), "There should be loaded at least one plugin ("+plugins.size()+" loaded)");
        int foundDemo = 0;
        for (Plugin plugin : plugins) {
            if (plugin.getClass().getName().equals("dk.magenta.datafordeler.plugindemo.DemoPlugin")) {
                foundDemo++;
            }
        }
        Assertions.assertEquals(1, foundDemo, "The demo plugin should be loaded exactly once (loaded " + foundDemo + ")");
    }

    @Test
    @Order(order=2)
    public void failPluginTest() {
        Assertions.assertNull(pluginManager.getPluginForSchema("foobar"));
    }


    @Test
    @Order(order=3)
    public void findDemoPluginTest1() {
        String testSchema = DemoEntityRecord.schema;
        Plugin foundPlugin = this.pluginManager.getPluginForSchema(testSchema);
        Assertions.assertEquals(DemoPlugin.class, foundPlugin.getClass());

        EntityManager foundEntityManager = foundPlugin.getEntityManager(testSchema);
        Assertions.assertEquals(DemoEntityManager.class, foundEntityManager.getClass());
    }


    @Test
    @Order(order=4)
    public void configurationTest() {
        Plugin plugin = this.pluginManager.getPluginForSchema(DemoEntityRecord.schema);
        Session session = sessionManager.getSessionFactory().openSession();
        try {
            DemoConfiguration configuration = (DemoConfiguration) plugin.getConfigurationManager().getConfiguration();
            Assertions.assertNotNull(configuration.getPullCronSchedule());
            Assertions.assertEquals("0 0 0 * * ?", configuration.getPullCronSchedule());
        } finally {
            session.close();
        }
    }



}
