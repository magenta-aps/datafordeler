package dk.magenta.datafordeler.core.plugin;

import dk.magenta.datafordeler.core.Application;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ContextConfiguration;


@ContextConfiguration(classes = Application.class)
public class RolesDefintionTest extends PluginTestBase {

    @Test
    public void testGetRoles() {
        //RolesDefinition rolesDefinition = this.plugin.getRolesDefinition();
        //List<SystemRole> roles = rolesDefinition.getRoles();
    }

}
