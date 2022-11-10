package dk.magenta.datafordeler.core.role;

public class ReadServiceRole extends SystemRole {

    private final String serviceName;

    // Note that service name should be rather static, as changing it will change the name of all
    // associated roles, potentially breaking access to services,
    public ReadServiceRole(String serviceName, ReadServiceRoleVersion... versions) {
        super(SystemRoleType.ServiceRole, SystemRoleGrant.Read, null, versions);
        this.serviceName = serviceName;
    }

    @Override
    public String getTargetName() {
        return serviceName;
    }
}
