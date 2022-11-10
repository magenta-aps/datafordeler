package dk.magenta.datafordeler.core.role;

public class ReadAttributeRole extends SystemRole {
    private final String attributeName;

    public ReadAttributeRole(
            String attributeName, ReadEntityRole parent, ReadAttributeRoleVersion... versions

    ) {
        super(SystemRoleType.AttributeRole, SystemRoleGrant.Read, parent, versions);
        this.attributeName = attributeName;
    }

    @Override
    public String getTargetName() {
        return this.getParent().getTargetName() + attributeName;
    }
}
