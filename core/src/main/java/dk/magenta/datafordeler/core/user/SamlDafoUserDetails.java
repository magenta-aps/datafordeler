package dk.magenta.datafordeler.core.user;

import org.opensaml.saml.saml2.core.Assertion;
import org.opensaml.saml.saml2.core.Attribute;
import org.opensaml.saml.saml2.core.AttributeStatement;
import org.opensaml.core.xml.XMLObject;
import org.opensaml.core.xml.schema.XSAny;
import org.opensaml.core.xml.schema.XSString;

import java.util.*;

public class SamlDafoUserDetails extends DafoUserDetails {

    public static String USERPROFILE_CLAIM_URL = "https://data.gl/claims/userprofile";
    public static String ON_BEHALF_OF_CLAIM_URL = "https://data.gl/claims/on-behalf-of";

    private final HashMap<String, UserProfile> userProfiles = new HashMap<>();
    private final HashMap<String, ArrayList<UserProfile>> systemRoles = new HashMap<>();

    private final Assertion sourceAssertion;

    private final String nameQualifier;
    private final String identity;
    private final String onBehalfOf;

    public SamlDafoUserDetails(Assertion assertion) {
        super();

        this.sourceAssertion = assertion;

        this.nameQualifier = assertion.getSubject().getNameID().getNameQualifier();
        this.identity = assertion.getSubject().getNameID().getValue();
        this.onBehalfOf = this.lookupOnBehalfOf();
    }

    public void addUserProfile(UserProfile userprofile) {
        this.userProfiles.put(userprofile.getName(), userprofile);
        for (String systemRole : userprofile.getSystemRoles()) {
            if (systemRoles.containsKey(systemRole)) {
                systemRoles.get(systemRole).add(userprofile);
            } else {
                ArrayList<UserProfile> list = new ArrayList<>();
                list.add(userprofile);
                systemRoles.put(systemRole, list);
            }
        }
    }

    public List<String> getAssertionUserProfileNames() {
        ArrayList<String> result = new ArrayList<>();
        for (AttributeStatement attributeStatement : sourceAssertion.getAttributeStatements()) {
            for (Attribute attribute : attributeStatement.getAttributes()) {
                if (attribute.getName().equals(USERPROFILE_CLAIM_URL)) {
                    for (XMLObject value : attribute.getAttributeValues()) {
                        result.add(getString(value));
                    }
                }
            }
        }
        return result;
    }

    private String lookupOnBehalfOf() {
        for (AttributeStatement attributeStatement : sourceAssertion.getAttributeStatements()) {
            for (Attribute attribute : attributeStatement.getAttributes()) {
                if (attribute.getName().equals(ON_BEHALF_OF_CLAIM_URL)) {
                    for (XMLObject value : attribute.getAttributeValues()) {
                        return getString(value);
                    }
                }
            }
        }

        return null;
    }

    public String getIssueInstant() {
        return sourceAssertion.getIssueInstant().toString();
    }

    private String getString(XMLObject xmlValue) {
        if (xmlValue instanceof XSString) {
            return ((XSString) xmlValue).getValue();
        } else if (xmlValue instanceof XSAny) {
            return ((XSAny) xmlValue).getTextContent();
        } else {
            return null;
        }
    }


    @Override
    public String getNameQualifier() {
        return this.nameQualifier;
    }

    @Override
    public String getIdentity() {
        return this.identity;
    }

    @Override
    public String getOnBehalfOf() {
        return this.onBehalfOf;
    }

    @Override
    public boolean isAnonymous() {
        return false;
    }

    @Override
    public boolean hasSystemRole(String role) {
        return systemRoles.containsKey(role);
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
    public Collection<UserProfile> getUserProfilesForRole(String role) {
        if (systemRoles.containsKey(role)) {
            return systemRoles.get(role);
        } else {
            return Collections.EMPTY_LIST;
        }
    }
}
