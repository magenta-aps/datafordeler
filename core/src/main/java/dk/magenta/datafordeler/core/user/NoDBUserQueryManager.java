package dk.magenta.datafordeler.core.user;

import dk.magenta.datafordeler.core.arearestriction.AreaRestriction;
import dk.magenta.datafordeler.core.arearestriction.AreaRestrictionType;
import dk.magenta.datafordeler.core.role.SystemRole;

import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * Implements UserQueryManager without a database connection.
 */
public class NoDBUserQueryManager extends UserQueryManager {

    @Override
    public int getUserProfileIdByName(String name) {
        return UserQueryManager.INVALID_USERPROFILE_ID;
    }

    @Override
    public List<String> getSystemRoleNamesByUserProfileId(int databaseId) {
        return Collections.EMPTY_LIST;
    }

    @Override
    public List<AreaRestriction> getAreaRestrictionsByUserProfileId(int databaseId) {
        return Collections.EMPTY_LIST;
    }

    @Override
    public Set<String> getAllStoredSystemRoleNames() {
        return Collections.EMPTY_SET;
    }

    @Override
    public void insertSystemRole(SystemRole systemRole) {
    }

    @Override
    public Set<String> getAllAreaRestrictionTypeLookupNames() {
        return Collections.EMPTY_SET;
    }

    @Override
    public Set<String> getAllAreaRestrictionLookupNames() {
        return Collections.EMPTY_SET;
    }

    @Override
    public void insertAreaRestrictionType(AreaRestrictionType areaRestrictionType) {
    }

    @Override
    public void insertAreaRestriction(AreaRestriction areaRestriction) {
    }

    @Override
    public void checkConnection() {
    }
}
