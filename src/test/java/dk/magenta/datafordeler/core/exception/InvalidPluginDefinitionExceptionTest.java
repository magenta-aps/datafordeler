package dk.magenta.datafordeler.core.exception;

import dk.magenta.datafordeler.core.TestConfig;
import dk.magenta.datafordeler.plugindemo.DemoPlugin;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = TestConfig.class)
public class InvalidPluginDefinitionExceptionTest {

    @Autowired
    private DemoPlugin plugin;

    @Test
    public void testInvalidPluginDefinitionException() {
        String message = "testmessage";
        InvalidPluginDefinitionException exception = new InvalidPluginDefinitionException(plugin, message);

        Assert.assertEquals(plugin, exception.getPlugin());
        Assert.assertEquals("datafordeler.plugin.invalid_plugin_definition", exception.getCode());
        Assert.assertEquals("Plugin " + plugin.getClass().getCanonicalName() + " is incorrectly defined: " + message, exception.getMessage());
    }

}
