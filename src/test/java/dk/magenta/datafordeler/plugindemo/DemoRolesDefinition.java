package dk.magenta.datafordeler.plugindemo;

import dk.magenta.datafordeler.core.plugin.RolesDefinition;
import dk.magenta.datafordeler.core.role.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class DemoRolesDefinition extends RolesDefinition {

    public static ReadServiceRole READ_SERVICE_ROLE = new ReadServiceRole(
        "DemoService",
        new ReadServiceRoleVersion(1.0f, "Initial version")
    );

    public static ReadEntityRole READ_DEMO_ENTITY_ROLE = new ReadEntityRole(
        "DemoEntity",
        READ_SERVICE_ROLE,
        new ReadEntityRoleVersion(1.0f, "Initial version")
    );

    public static ExecuteCommandRole EXECUTE_DEMO_PULL_ROLE = new ExecuteCommandRole(
            "Pull",
            new HashMap<String, Object>() {{
                put("plugin", "Demo");
            }},
            new ExecuteCommandRoleVersion(
                    1.0f,
                    "Role that gives access to start the PULL command for Demo data"
            )
    );

    public static ReadCommandRole READ_DEMO_PULL_ROLE = new ReadCommandRole(
            "Pull",
            new HashMap<String, Object>() {{
                put("plugin", "Demo");
            }},
            new ReadCommandRoleVersion(
                    1.0f,
                    "Role that gives access to read the status of the PULL command for Demo data"
            )
    );

    public static StopCommandRole STOP_DEMO_PULL_ROLE = new StopCommandRole(
            "Pull",
            new HashMap<String, Object>() {{
                put("plugin", "Demo");
            }},
            new StopCommandRoleVersion(
                    1.0f,
                    "Role that gives access to stop the PULL command for Demo data"
            )
    );

    public List<SystemRole> getRoles() {
        ArrayList<SystemRole> roles = new ArrayList<>();
        roles.add(READ_SERVICE_ROLE);
        roles.add(READ_DEMO_ENTITY_ROLE);
        roles.add(EXECUTE_DEMO_PULL_ROLE);
        roles.add(READ_DEMO_PULL_ROLE);
        roles.add(STOP_DEMO_PULL_ROLE);
        return roles;
    }

    @Override
    public ReadServiceRole getDefaultReadRole() {
        return READ_SERVICE_ROLE;
    }
}
