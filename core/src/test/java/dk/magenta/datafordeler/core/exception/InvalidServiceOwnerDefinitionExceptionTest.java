package dk.magenta.datafordeler.core.exception;

import dk.magenta.datafordeler.core.Application;
import dk.magenta.datafordeler.plugindemo.DemoPlugin;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.regex.Pattern;


@RunWith(SpringJUnit4ClassRunner.class)
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

        Assert.assertEquals("Plugin " + plugin.getClass().getCanonicalName() + " is incorrectly defined: \"" + ownerDefinition + "\" is not a valid owner definition, must conform to regex /" + validationRegex.pattern() + "/", exception.getMessage());
        Assert.assertEquals("datafordeler.plugin.invalid_owner_definition", exception.getCode());
    }

}
