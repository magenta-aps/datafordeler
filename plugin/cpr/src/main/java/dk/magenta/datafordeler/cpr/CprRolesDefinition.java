package dk.magenta.datafordeler.cpr;

import dk.magenta.datafordeler.core.plugin.RolesDefinition;
import dk.magenta.datafordeler.core.role.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class CprRolesDefinition extends RolesDefinition {

    public static ReadServiceRole READ_CPR_ROLE = new ReadServiceRole(
            "Cpr",
            new ReadServiceRoleVersion(
                    1.0f,
                    "Role that gives access to all PERSONNUMMER data."
            )
    );

    public static ExecuteCommandRole EXECUTE_CPR_PULL_ROLE = new ExecuteCommandRole(
            "pull",
            new HashMap<String, Object>() {{
                put("plugin", "cpr");
            }},
            new ExecuteCommandRoleVersion(
                    1.0f,
                    "Role that gives access to start and stop the PULL command for Cpr data"
            )
    );

    public static ReadCommandRole READ_CPR_PULL_ROLE = new ReadCommandRole(
            "Pull",
            new HashMap<String, Object>() {{
                put("plugin", "cpr");
            }},
            new ReadCommandRoleVersion(
                    1.0f,
                    "Role that gives access to read the status of the PULL command for Cpr data"
            )
    );

    public static StopCommandRole STOP_CPR_PULL_ROLE = new StopCommandRole(
            "Pull",
            new HashMap<String, Object>() {{
                put("plugin", "cpr");
            }},
            new StopCommandRoleVersion(
                    1.0f,
                    "Role that gives access to stop the PULL command for Cpr data"
            )
    );

    @Override
    public List<SystemRole> getRoles() {
        ArrayList<SystemRole> roles = new ArrayList<>();
        roles.add(READ_CPR_ROLE);
        roles.add(EXECUTE_CPR_PULL_ROLE);
        roles.add(READ_CPR_PULL_ROLE);
        roles.add(STOP_CPR_PULL_ROLE);
        return roles;
    }

    public ReadServiceRole getDefaultReadRole() {
        return READ_CPR_ROLE;
    }
}
