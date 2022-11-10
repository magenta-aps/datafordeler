package dk.magenta.datafordeler.core.user;

import java.util.Collection;
import java.util.Collections;

public class AnonymousDafoUserDetails extends DafoUserDetails {

    @Override
    public String getNameQualifier() {
        return "anonymous";
    }

    @Override
    public String getIdentity() {
        return "anonymous";
    }

    @Override
    public String getOnBehalfOf() {
        return null;
    }

    @Override
    public boolean isAnonymous() {
        return true;
    }

    @Override
    public boolean hasSystemRole(String role) {
        return false;
    }

    @Override
    public boolean hasUserProfile(String userProfileName) {
        return false;
    }

    @Override
    public Collection<String> getUserProfiles() {
        return Collections.EMPTY_LIST;
    }

    @Override
    public Collection<String> getSystemRoles() {
        return Collections.EMPTY_LIST;
    }

    @Override
    public Collection<UserProfile> getUserProfilesForRole(String role) {
        return Collections.EMPTY_LIST;
    }
}
