package dk.magenta.datafordeler.core.user;

import dk.magenta.datafordeler.core.arearestriction.AreaRestriction;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;

public class UserProfile {
    public static int INVALID_DATABASE_ID = -1;

    private int databaseId = INVALID_DATABASE_ID;
    private final String name;
    private final HashSet<String> systemRoles = new HashSet<>();
    private final HashSet<AreaRestriction> areaRestrictions = new HashSet<>();

    public UserProfile(String name) {
        this(name, null, null);
    }

    public UserProfile(String name, List<String> systemRoles) {
        this(name, systemRoles, null);
    }

    public UserProfile(
            String name, List<String> systemRoles, List<AreaRestriction> areaRestrictions
    ) {
        this.name = name;
        if (systemRoles != null) {
            this.systemRoles.addAll(systemRoles);
        }
        if (areaRestrictions != null) {
            this.areaRestrictions.addAll(areaRestrictions);
        }
    }

    public int getDatabaseId() {
        return databaseId;
    }

    public void setDatabaseId(int databaseId) {
        this.databaseId = databaseId;
    }

    public String getName() {
        return name;
    }

    public HashSet<String> getSystemRoles() {
        return systemRoles;
    }

    public HashSet<AreaRestriction> getAreaRestrictions() {
        return areaRestrictions;
    }

    public void addSystemRoles(Collection<String> systemRoles) {
        this.systemRoles.addAll(systemRoles);
    }

    public void addAreaRestrictions(Collection<AreaRestriction> areaRestrictions) {
        this.areaRestrictions.addAll(areaRestrictions);
    }
}
