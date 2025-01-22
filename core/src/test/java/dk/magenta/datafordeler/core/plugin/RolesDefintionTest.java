package dk.magenta.datafordeler.core.plugin;

import dk.magenta.datafordeler.core.Application;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = Application.class)
public class RolesDefintionTest extends PluginTestBase {

    @Test
    public void testGetRoles() {
        //RolesDefinition rolesDefinition = this.plugin.getRolesDefinition();
        //List<SystemRole> roles = rolesDefinition.getRoles();
    }

}
