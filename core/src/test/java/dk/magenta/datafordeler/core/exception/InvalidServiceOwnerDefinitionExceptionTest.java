package dk.magenta.datafordeler.core.exception;

import dk.magenta.datafordeler.core.Application;
import dk.magenta.datafordeler.plugindemo.DemoPlugin;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;

import java.util.regex.Pattern;



@ContextConfiguration(classes = Application.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class InvalidServiceOwnerDefinitionExceptionTest extends InvalidPluginDefinitionExceptionTest {

    @Autowired
    private DemoPlugin plugin;

    @Test
    public void testInvalidServiceOwnerDefinitionException() {
        String ownerDefinition = "test@";
        Pattern validationRegex = Pattern.compile("^[a-zA-Z0-9_]+$");
        InvalidServiceOwnerDefinitionException exception = new InvalidServiceOwnerDefinitionException(plugin, ownerDefinition, validationRegex);

        Assertions.assertEquals("Plugin " + plugin.getClass().getCanonicalName() + " is incorrectly defined: \"" + ownerDefinition + "\" is not a valid owner definition, must conform to regex /" + validationRegex.pattern() + "/", exception.getMessage());
        Assertions.assertEquals("datafordeler.plugin.invalid_owner_definition", exception.getCode());
    }

}
