package dk.magenta.datafordeler.core.role;

import java.util.Map;

public class ReadCommandRole extends CommandRole {

    public ReadCommandRole(String commandName, Map<String, Object> details, SystemRoleVersion... versions) {
        super(SystemRoleType.ReadCommandRole, SystemRoleGrant.Read, commandName, details, null, versions);
    }

}
