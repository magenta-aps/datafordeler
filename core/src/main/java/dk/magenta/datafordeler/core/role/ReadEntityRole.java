package dk.magenta.datafordeler.core.role;

public class ReadEntityRole extends SystemRole {

    private final String entityName;

    public ReadEntityRole(
            String entityName, ReadServiceRole parent, ReadEntityRoleVersion... versions
    ) {
        super(SystemRoleType.EntityRole, SystemRoleGrant.Read, parent, versions);
        this.entityName = entityName;
    }

    @Override
    public String getTargetName() {
        return this.getParent().getTargetName() + entityName;
    }
}
