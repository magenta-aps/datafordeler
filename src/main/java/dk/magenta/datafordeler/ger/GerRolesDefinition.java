package dk.magenta.datafordeler.ger;

import dk.magenta.datafordeler.core.plugin.RolesDefinition;
import dk.magenta.datafordeler.core.role.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class GerRolesDefinition extends RolesDefinition {

    public static ReadServiceRole READ_GER_ROLE = new ReadServiceRole(
            "Ger",
            new ReadServiceRoleVersion(
                    1.0f,
                    "Role that gives access to all PERSONNUMMER dk.magenta.datafordeler.ger.data."
            )
    );

    public static ExecuteCommandRole EXECUTE_GER_PULL_ROLE = new ExecuteCommandRole(
            "pull",
            new HashMap<String, Object>() {{
                put("plugin", "ger");
            }},
            new ExecuteCommandRoleVersion(
                    1.0f,
                    "Role that gives access to start and stop the PULL command for Ger dk.magenta.datafordeler.ger.data"
            )
    );

    public static ReadCommandRole READ_GER_PULL_ROLE = new ReadCommandRole(
            "Pull",
            new HashMap<String, Object>() {{
                put("plugin", "ger");
            }},
            new ReadCommandRoleVersion(
                    1.0f,
                    "Role that gives access to read the status of the PULL command for Ger dk.magenta.datafordeler.ger.data"
            )
    );

    public static StopCommandRole STOP_GER_PULL_ROLE = new StopCommandRole(
            "Pull",
            new HashMap<String, Object>() {{
                put("plugin", "ger");
            }},
            new StopCommandRoleVersion(
                    1.0f,
                    "Role that gives access to stop the PULL command for Ger dk.magenta.datafordeler.ger.data"
            )
    );

    @Override
    public List<SystemRole> getRoles() {
        ArrayList<SystemRole> roles = new ArrayList<>();
        roles.add(READ_GER_ROLE);
        roles.add(EXECUTE_GER_PULL_ROLE);
        roles.add(READ_GER_PULL_ROLE);
        roles.add(STOP_GER_PULL_ROLE);
        return roles;
    }

    public ReadServiceRole getDefaultReadRole() {
        return READ_GER_ROLE;
    }
}
