package dk.magenta.datafordeler.core.exception;

import dk.magenta.datafordeler.core.Application;
import dk.magenta.datafordeler.plugindemo.DemoPlugin;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;

@SpringBootTest
@ContextConfiguration(classes = Application.class)
public class InvalidPluginDefinitionExceptionTest {

    @Autowired
    private DemoPlugin plugin;

    @Test
    public void testInvalidPluginDefinitionException() {
        String message = "testmessage";
        InvalidPluginDefinitionException exception = new InvalidPluginDefinitionException(plugin, message);

        Assertions.assertEquals(plugin, exception.getPlugin());
        Assertions.assertEquals("datafordeler.plugin.invalid_plugin_definition", exception.getCode());
        Assertions.assertEquals("Plugin " + plugin.getClass().getCanonicalName() + " is incorrectly defined: " + message, exception.getMessage());
    }

}
