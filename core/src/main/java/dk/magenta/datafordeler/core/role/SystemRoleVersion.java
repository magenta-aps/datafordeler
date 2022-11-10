package dk.magenta.datafordeler.core.role;

public abstract class SystemRoleVersion {
    private final float version;
    private final String comment;

    public SystemRoleVersion(float version, String comment) {
        this.version = version;
        this.comment = comment;
    }

    public float getVersion() {
        return version;
    }

    public String getComment() {
        return comment;
    }
}
