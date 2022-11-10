package dk.magenta.datafordeler.core.role;

import java.util.ArrayList;
import java.util.List;

public abstract class SystemRole {
    private final SystemRole parent;
    private final SystemRoleType type;
    private final SystemRoleGrant grantType;
    private final List<SystemRole> children;
    private final List<SystemRoleVersion> versions;
    private SystemRoleVersion currentVersion;

    protected SystemRole(
            SystemRoleType type, SystemRoleGrant grantType, SystemRole parent,
            SystemRoleVersion... versions
    ) {
        if (versions.length < 1) {
            throw new IllegalArgumentException("You must supply at least one version");
        }

        this.type = type;
        this.grantType = grantType;
        this.parent = parent;
        this.versions = new ArrayList<>();
        this.children = new ArrayList<>();

        if (parent != null) {
            parent.addChild(this);
        }

        for (SystemRoleVersion v : versions) {
            this.addVersion(v);
        }
    }

    public String getGrantType() {
        return grantType.name();
    }

    public SystemRoleType getType() {
        return type;
    }

    private void addChild(SystemRole newChild) {
        children.add(newChild);
    }

    private void addVersion(SystemRoleVersion version) {
        versions.add(version);
        if (currentVersion != null && version.getVersion() > currentVersion.getVersion()) {
            currentVersion = version;
        }
    }

    public SystemRole getParent() {
        return parent;
    }

    public abstract String getTargetName();

    public String getRoleName() {
        return this.getGrantType() + this.getTargetName();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof SystemRole)) {
            return false;
        }

        SystemRole that = (SystemRole) o;

        // Roles are represented by their name, so equals should just match those
        return this.getRoleName().equals(that.getRoleName());
    }

    public boolean equals(String that) {
        return this.getRoleName().equals(that);
    }

}
