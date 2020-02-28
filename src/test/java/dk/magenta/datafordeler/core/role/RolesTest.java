package dk.magenta.datafordeler.core.role;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.HashMap;

public class RolesTest {
  private static ReadServiceRole serviceRole;
  private static ReadEntityRole entityRole;
  private static ReadAttributeRole attributeRole;
  private static ExecuteCommandRole executeCommandRole;

  @BeforeClass
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
    Assert.assertTrue(
        "Name of service role should be ReadMyService",
        serviceRole.getRoleName().equals("ReadMyService")
    );
  }

  @Test
  public void testNameOfEntityRole() {
    Assert.assertTrue(
        "Name of entity role should be ReadMyServiceMyEntity",
        entityRole.getRoleName().equals("ReadMyServiceMyEntity")
    );
  }

  @Test
  public void testNameOfAttributeRole() {
    Assert.assertTrue(
        "Name of attribute role should be ReadMyServiceMyEntityMyAttribute",
        attributeRole.getRoleName().equals("ReadMyServiceMyEntityMyAttribute")
    );
  }

  @Test
  public void testNameOfExecuteCommandRole() {
    Assert.assertTrue(
            "Name of execute role should be ExecuteBogusCommand{foo=42}",
            executeCommandRole.getRoleName().equals("ExecuteBogusCommand{foo=42}")
    );
  }
}
