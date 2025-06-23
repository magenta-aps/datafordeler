package dk.magenta.datafordeler.cvr.access;

import dk.magenta.datafordeler.core.plugin.RolesDefinition;
import dk.magenta.datafordeler.core.role.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class CvrRolesDefinition extends RolesDefinition {

    public static ReadServiceRole READ_CVR_ROLE = new ReadServiceRole(
            "Cvr",
            new ReadServiceRoleVersion(1.0f, "Gives access to Cvr related services")
    );

    public static ExecuteCommandRole EXECUTE_CVR_PULL_ROLE = new ExecuteCommandRole(
            "pull",
            new HashMap<String, Object>() {{
                put("plugin", "cvr");
            }},
            new ExecuteCommandRoleVersion(
                    1.0f,
                    "Role that gives access to start and stop the PULL command for Cvr data"
            )
    );

    public static ReadCommandRole READ_CVR_PULL_ROLE = new ReadCommandRole(
            "Pull",
            new HashMap<String, Object>() {{
                put("plugin", "cvr");
            }},
            new ReadCommandRoleVersion(
                    1.0f,
                    "Role that gives access to read the status of the PULL command for Cvr data"
            )
    );

    public static StopCommandRole STOP_CVR_PULL_ROLE = new StopCommandRole(
            "Pull",
            new HashMap<String, Object>() {{
                put("plugin", "cvr");
            }},
            new StopCommandRoleVersion(
                    1.0f,
                    "Role that gives access to stop the PULL command for Cvr data"
            )
    );

    public static ExecuteCommandRole EXECUTE_CVR_CLOSE_BITEMP_ROLE = new ExecuteCommandRole(
            "close_bitemp",
            new HashMap<String, Object>() {{
                put("type", "ids");
            }},
            new ExecuteCommandRoleVersion(
                    1.0f,
                    "Role that gives access to close bitemporality for Cvr data"
            )
    );


    @Override
    public List<SystemRole> getRoles() {
        ArrayList<SystemRole> roles = new ArrayList<>();

        roles.add(READ_CVR_ROLE);
        roles.add(EXECUTE_CVR_PULL_ROLE);
        roles.add(READ_CVR_PULL_ROLE);
        roles.add(STOP_CVR_PULL_ROLE);
        roles.add(EXECUTE_CVR_CLOSE_BITEMP_ROLE);

        return roles;
    }


    public ReadServiceRole getDefaultReadRole() {
        return READ_CVR_ROLE;
    }
}
