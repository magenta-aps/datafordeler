package dk.magenta.datafordeler.gladdrreg;

import dk.magenta.datafordeler.core.plugin.RolesDefinition;
import dk.magenta.datafordeler.core.role.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by lars on 12-01-17.
 */
public class GladdrregRolesDefinition extends RolesDefinition {

    public static ReadServiceRole READ_GLADDRREG_ROLE = new ReadServiceRole(
            "Gladdrreg",
            new ReadServiceRoleVersion(1.0f, "Initial version")
    );

    public static ExecuteCommandRole EXECUTE_GLADDRREG_PULL_ROLE = new ExecuteCommandRole(
            "pull",
            new HashMap<String, Object>() {{
                put("plugin", "gladdrreg");
            }},
            new ExecuteCommandRoleVersion(
                    1.0f,
                    "Role that gives access to start and stop the PULL command for Gladdrreg data"
            )
    );

    public static ReadCommandRole READ_GLADDRREG_PULL_ROLE = new ReadCommandRole(
            "Pull",
            new HashMap<String, Object>() {{
                put("plugin", "gladdrreg");
            }},
            new ReadCommandRoleVersion(
                    1.0f,
                    "Role that gives access to read the status of the PULL command for Gladdrreg data"
            )
    );

    public static StopCommandRole STOP_GLADDRREG_PULL_ROLE = new StopCommandRole(
            "Pull",
            new HashMap<String, Object>() {{
                put("plugin", "gladdrreg");
            }},
            new StopCommandRoleVersion(
                    1.0f,
                    "Role that gives access to stop the PULL command for Gladdrreg data"
            )
    );


    public List<SystemRole> getRoles() {
        ArrayList<SystemRole> roles = new ArrayList<>();

        roles.add(READ_GLADDRREG_ROLE);
        roles.add(EXECUTE_GLADDRREG_PULL_ROLE);
        roles.add(READ_GLADDRREG_PULL_ROLE);
        roles.add(STOP_GLADDRREG_PULL_ROLE);

        return roles;
    }


    public ReadServiceRole getDefaultReadRole() {
        return READ_GLADDRREG_ROLE;
    }
}
