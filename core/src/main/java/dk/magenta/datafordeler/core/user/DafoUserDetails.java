package dk.magenta.datafordeler.core.user;

import dk.magenta.datafordeler.core.arearestriction.AreaRestriction;
import dk.magenta.datafordeler.core.exception.AccessDeniedException;
import dk.magenta.datafordeler.core.role.SystemRole;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collection;

public abstract class DafoUserDetails {

    private final OffsetDateTime creationTime;

    public DafoUserDetails(OffsetDateTime creationTime) {
        this.creationTime = creationTime;
    }

    public DafoUserDetails() {
        this.creationTime = OffsetDateTime.now();
    }

    public abstract String getNameQualifier();

    public abstract String getIdentity();

    public abstract String getOnBehalfOf();

    public abstract boolean hasSystemRole(String role);

    public abstract boolean isAnonymous();

    public boolean hasSystemRole(SystemRole role) {
        return hasSystemRole(role.getRoleName());
    }

    public void checkHasSystemRole(String role) throws AccessDeniedException {
        System.out.println(this.getClass().getCanonicalName());
        if (!hasSystemRole(role)) {
            System.out.println("!hasSystemRole("+role+")");
            throw new AccessDeniedException(
                    "User " + this + " does not have access to " + role
            );
        }
        else {
            System.out.println("hasSystemRole("+role+")");
        }
    }

    public void checkHasSystemRole(SystemRole role) throws AccessDeniedException {
        checkHasSystemRole(role.getRoleName());
    }

    public abstract boolean hasUserProfile(String userProfileName);

    public void checkHasUserProfile(String userProfileName) throws AccessDeniedException {
        if (!hasUserProfile(userProfileName)) {
            throw new AccessDeniedException(
                    "User " + this + " does not have the UserProfile " + userProfileName
            );
        }
    }

    public abstract Collection<String> getUserProfiles();

    public abstract Collection<String> getSystemRoles();

    public abstract Collection<UserProfile> getUserProfilesForRole(String role);

    public Collection<UserProfile> getUserProfilesForRole(SystemRole role) {
        return getUserProfilesForRole(role.getRoleName());
    }

    public Collection<AreaRestriction> getAreaRestrictionsForRole(String role) {
        ArrayList<AreaRestriction> result = new ArrayList<>();

        for (UserProfile userProfile : getUserProfilesForRole(role)) {
            result.addAll(userProfile.getAreaRestrictions());
        }

        return result;
    }

    public Collection<AreaRestriction> getAreaRestrictionsForRole(SystemRole role) {
        return getAreaRestrictionsForRole(role.getRoleName());
    }

    @Override
    public String toString() {
        if (getOnBehalfOf() != null) {
            return "[" + getIdentity() + "<" + getOnBehalfOf() + ">]@[" + getNameQualifier() + "]";

        } else {
            return "[" + getIdentity() + "]@[" + getNameQualifier() + "]";
        }
    }

    public OffsetDateTime getCreationTime() {
        return creationTime;
    }
}
