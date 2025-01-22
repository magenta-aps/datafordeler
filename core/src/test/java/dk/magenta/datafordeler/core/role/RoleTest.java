package dk.magenta.datafordeler.core.role;

import dk.magenta.datafordeler.core.Application;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = Application.class)
public class RoleTest {

    @Test
    public void testEqual() {
        SystemRole role = new ReadServiceRole(
            "MyService",
            new ReadServiceRoleVersion(0.1f, "For testing only")
        );

        Assert.assertEquals("ReadMyService", role.getRoleName());
    }
}
