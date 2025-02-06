package dk.magenta.datafordeler.core.role;

import dk.magenta.datafordeler.core.Application;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ContextConfiguration;


@ContextConfiguration(classes = Application.class)
public class RoleTest {

    @Test
    public void testEqual() {
        SystemRole role = new ReadServiceRole(
            "MyService",
            new ReadServiceRoleVersion(0.1f, "For testing only")
        );

        Assertions.assertEquals("ReadMyService", role.getRoleName());
    }
}
