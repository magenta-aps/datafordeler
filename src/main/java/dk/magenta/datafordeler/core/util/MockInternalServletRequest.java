package dk.magenta.datafordeler.core.util;

import dk.magenta.datafordeler.core.user.DafoUserDetails;
import dk.magenta.datafordeler.core.user.UserProfile;
import org.springframework.mock.web.MockHttpServletRequest;

import java.util.Collection;
import java.util.Collections;

public class MockInternalServletRequest extends MockHttpServletRequest {

    private final String sender;

    private final DafoUserDetails userDetails = new DafoUserDetails(null) {
        @Override
        public String getNameQualifier() {
            return "INTERNAL";
        }

        @Override
        public String getIdentity() {
            return sender;
        }

        @Override
        public String getOnBehalfOf() {
            return null;
        }

        @Override
        public boolean hasSystemRole(String role) {
            return true;
        }

        @Override
        public boolean isAnonymous() {
            return false;
        }

        @Override
        public boolean hasUserProfile(String userProfileName) {
            return true;
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
    };

    public MockInternalServletRequest(String sender, String requestURI) {
        super("GET", requestURI);
        this.sender = sender;
    }

    public DafoUserDetails getUserDetails() {
        return userDetails;
    }
}
