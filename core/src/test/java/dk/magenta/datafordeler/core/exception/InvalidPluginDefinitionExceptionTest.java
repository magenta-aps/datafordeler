package dk.magenta.datafordeler.core.exception;

import dk.magenta.datafordeler.core.Application;
import dk.magenta.datafordeler.plugindemo.DemoPlugin;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = Application.class)
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
