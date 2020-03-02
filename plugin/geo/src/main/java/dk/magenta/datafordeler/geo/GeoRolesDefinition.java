package dk.magenta.datafordeler.geo;

import dk.magenta.datafordeler.core.plugin.RolesDefinition;
import dk.magenta.datafordeler.core.role.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class GeoRolesDefinition extends RolesDefinition {

    public static ReadServiceRole READ_GEO_ROLE = new ReadServiceRole(
            "Geo",
            new ReadServiceRoleVersion(
                    1.0f,
                    "Role that gives access to all PERSONNUMMER data."
            )
    );

    public static ExecuteCommandRole EXECUTE_GEO_PULL_ROLE = new ExecuteCommandRole(
            "pull",
            new HashMap<String, Object>() {{
                put("plugin", "geo");
            }},
            new ExecuteCommandRoleVersion(
                    1.0f,
                    "Role that gives access to start and stop the PULL command for Geo data"
            )
    );

    public static ReadCommandRole READ_GEO_PULL_ROLE = new ReadCommandRole(
            "Pull",
            new HashMap<String, Object>() {{
                put("plugin", "geo");
            }},
            new ReadCommandRoleVersion(
                    1.0f,
                    "Role that gives access to read the status of the PULL command for Geo data"
            )
    );

    public static StopCommandRole STOP_GEO_PULL_ROLE = new StopCommandRole(
            "Pull",
            new HashMap<String, Object>() {{
                put("plugin", "geo");
            }},
            new StopCommandRoleVersion(
                    1.0f,
                    "Role that gives access to stop the PULL command for Geo data"
            )
    );

    @Override
    public List<SystemRole> getRoles() {
        ArrayList<SystemRole> roles = new ArrayList<>();
        roles.add(READ_GEO_ROLE);
        roles.add(EXECUTE_GEO_PULL_ROLE);
        roles.add(READ_GEO_PULL_ROLE);
        roles.add(STOP_GEO_PULL_ROLE);
        return roles;
    }

    public ReadServiceRole getDefaultReadRole() {
        return READ_GEO_ROLE;
    }
}
