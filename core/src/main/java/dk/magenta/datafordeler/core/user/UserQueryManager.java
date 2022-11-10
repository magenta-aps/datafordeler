package dk.magenta.datafordeler.core.user;

import dk.magenta.datafordeler.core.arearestriction.AreaRestriction;
import dk.magenta.datafordeler.core.arearestriction.AreaRestrictionType;
import dk.magenta.datafordeler.core.role.SystemRole;

import java.util.List;
import java.util.Set;

/**
 * Gives access to data in the DAFO user database.
 */
public abstract class UserQueryManager {

    public static int INVALID_USERPROFILE_ID = -1;

    public UserProfile getUserProfileByName(String name) {
        UserProfile userProfile = null;

        int userProfileId = getUserProfileIdByName(name);
        if (userProfileId != INVALID_USERPROFILE_ID) {
            userProfile = new UserProfile(name);
            userProfile.setDatabaseId(userProfileId);
            userProfile.getSystemRoles().addAll(
                    getSystemRoleNamesByUserProfileId(userProfile.getDatabaseId())
            );
            for (AreaRestriction area : getAreaRestrictionsByUserProfileId(userProfile.getDatabaseId())) {
                userProfile.getAreaRestrictions().add(area);
            }
        }

        return userProfile;
    }

    public abstract int getUserProfileIdByName(String name);

    public abstract List<String> getSystemRoleNamesByUserProfileId(int databaseId);

    public abstract List<AreaRestriction> getAreaRestrictionsByUserProfileId(int databaseId);

    public abstract Set<String> getAllStoredSystemRoleNames();

    public abstract void insertSystemRole(SystemRole systemRole);

    public abstract Set<String> getAllAreaRestrictionTypeLookupNames();

    public abstract Set<String> getAllAreaRestrictionLookupNames();

    public abstract void insertAreaRestrictionType(AreaRestrictionType areaRestrictionType);

    public abstract void insertAreaRestriction(AreaRestriction areaRestriction);

    public abstract void checkConnection();
}
