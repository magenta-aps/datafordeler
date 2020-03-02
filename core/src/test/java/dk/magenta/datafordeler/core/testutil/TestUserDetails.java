package dk.magenta.datafordeler.core.testutil;

import dk.magenta.datafordeler.core.user.DafoUserDetails;
import dk.magenta.datafordeler.core.user.UserProfile;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;

public class TestUserDetails extends DafoUserDetails {

    private HashMap<String, UserProfile> userProfiles = new HashMap<>();
    private HashMap<String, Collection<UserProfile>> systemRoles = new HashMap<>();

    public void addUserProfile(UserProfile userprofile) {
        this.userProfiles.put(userprofile.getName(), userprofile);
        for (String systemRole : userprofile.getSystemRoles()) {
            if (systemRoles.containsKey(systemRole)) {
                systemRoles.get(systemRole).add(userprofile);
            } else {
                systemRoles.put(systemRole, Collections.singletonList(userprofile));
            }
        }
    }

    @Override
    public String getNameQualifier() {
        return "testing";
    }

    @Override
    public String getIdentity() {
        return "tester";
    }

    @Override
    public String getOnBehalfOf() {
        return null;
    }

    @Override
    public boolean isAnonymous() {
        return false;
    }

    @Override
    public boolean hasUserProfile(String userProfileName) {
        return userProfiles.containsKey(userProfileName);
    }

    @Override
    public Collection<String> getUserProfiles() {
        return userProfiles.keySet();
    }

    @Override
    public Collection<String> getSystemRoles() {
        return systemRoles.keySet();
    }

    @Override
    public boolean hasSystemRole(String role) {
        return systemRoles.containsKey(role);
    }

    @Override
    public Collection<UserProfile> getUserProfilesForRole(String role) {
        return systemRoles.getOrDefault(role, Collections.EMPTY_LIST);
    }
}
