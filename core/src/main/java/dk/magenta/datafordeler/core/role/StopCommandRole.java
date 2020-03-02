package dk.magenta.datafordeler.core.role;

import java.util.Map;

public class StopCommandRole extends CommandRole {

    public StopCommandRole(String commandName, Map<String, Object> details, SystemRoleVersion... versions) {
        super(SystemRoleType.StopCommandRole, SystemRoleGrant.Stop, commandName, details, null, versions);
    }

}
