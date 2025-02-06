package dk.magenta.datafordeler.core.role;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.HashMap;

public class RolesTest {
    private static ReadServiceRole serviceRole;
    private static ReadEntityRole entityRole;
    private static ReadAttributeRole attributeRole;
    private static ExecuteCommandRole executeCommandRole;

    @BeforeAll
    public static void setUp() {
        serviceRole = new ReadServiceRole(
                "MyService",
                new ReadServiceRoleVersion(0.1f, "First version of MyService role"),
                new ReadServiceRoleVersion(0.2f, "Second version MyService role")
        );
        entityRole = new ReadEntityRole(
                "MyEntity", serviceRole,
                new ReadEntityRoleVersion(0.1f, "First version of MyEntity role")
        );
        attributeRole = new ReadAttributeRole(
                "MyAttribute", entityRole,
                new ReadAttributeRoleVersion(0.1f, "First version of MyAttribute role")
        );
        executeCommandRole = new ExecuteCommandRole(
                "BogusCommand",
                new HashMap<String, Object>() {{
                    put("foo", 42);
                }},
                new ExecuteCommandRoleVersion(0.1f, "First version of BogusCommand role")
        );
    }

    @Test
    public void testNameofServiceRole() {
        Assertions.assertTrue(
                serviceRole.getRoleName().equals("ReadMyService"),
                "Name of service role should be ReadMyService"
        );
    }

    @Test
    public void testNameOfEntityRole() {
        Assertions.assertTrue(
                entityRole.getRoleName().equals("ReadMyServiceMyEntity"),
                "Name of entity role should be ReadMyServiceMyEntity"
        );
    }

    @Test
    public void testNameOfAttributeRole() {
        Assertions.assertTrue(
                attributeRole.getRoleName().equals("ReadMyServiceMyEntityMyAttribute"),
                "Name of attribute role should be ReadMyServiceMyEntityMyAttribute"
        );
    }

    @Test
    public void testNameOfExecuteCommandRole() {
        Assertions.assertTrue(
                executeCommandRole.getRoleName().equals("ExecuteBogusCommand{foo=42}"),
                "Name of execute role should be ExecuteBogusCommand{foo=42}"
        );
    }
}
