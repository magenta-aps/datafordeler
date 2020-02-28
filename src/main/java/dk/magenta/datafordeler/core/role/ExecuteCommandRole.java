package dk.magenta.datafordeler.core.role;

import java.util.Map;

public class ExecuteCommandRole extends CommandRole {

    public ExecuteCommandRole(String commandName, Map<String, Object> details, SystemRoleVersion... versions) {
        super(SystemRoleType.ExecuteCommandRole, SystemRoleGrant.Execute, commandName, details, null, versions);
    }

}
