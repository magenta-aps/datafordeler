package dk.magenta.datafordeler.core.user;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import org.opensaml.saml2.core.Assertion;
import org.opensaml.saml2.core.Attribute;
import org.opensaml.saml2.core.AttributeStatement;
import org.opensaml.xml.XMLObject;
import org.opensaml.xml.schema.XSAny;
import org.opensaml.xml.schema.XSString;

/**
 * Created by jubk on 13-06-2017.
 */
public class SamlDafoUserDetails extends DafoUserDetails {

  public static String USERPROFILE_CLAIM_URL = "https://data.gl/claims/userprofile";
  public static String ON_BEHALF_OF_CLAIM_URL = "https://data.gl/claims/on-behalf-of";

  private HashMap<String, UserProfile> userProfiles = new HashMap<>();
  private HashMap<String, Collection<UserProfile>> systemRoles = new HashMap<>();

  private Assertion sourceAssertion;

  private String nameQualifier;
  private String identity;
  private String onBehalfOf;

  public SamlDafoUserDetails(Assertion assertion) {
    super();

    this.sourceAssertion = assertion;

    this.nameQualifier = assertion.getSubject().getNameID().getNameQualifier();
    this.identity = assertion.getSubject().getNameID().getValue();
    this.onBehalfOf = this.lookupOnBehalfOf();
  }

  public void addUserProfile(UserProfile userprofile) {
    this.userProfiles.put(userprofile.getName(), userprofile);
    for(String systemRole : userprofile.getSystemRoles()) {
      if(systemRoles.containsKey(systemRole)) {
        systemRoles.get(systemRole).add(userprofile);
      } else {
        systemRoles.put(systemRole, Collections.singletonList(userprofile));
      }
    }
  }

  public List<String> getAssertionUserProfileNames() {
    ArrayList<String> result = new ArrayList<>();
    for(AttributeStatement attributeStatement : sourceAssertion.getAttributeStatements()) {
      for(Attribute attribute : attributeStatement.getAttributes()) {
        if(attribute.getName().equals(USERPROFILE_CLAIM_URL)) {
          for(XMLObject value : attribute.getAttributeValues()) {
            result.add(getString(value));
          }
        }
      }
    }
    return result;
  }

  private String lookupOnBehalfOf() {
    for(AttributeStatement attributeStatement : sourceAssertion.getAttributeStatements()) {
      for(Attribute attribute : attributeStatement.getAttributes()) {
        if(attribute.getName().equals(ON_BEHALF_OF_CLAIM_URL)) {
          for(XMLObject value : attribute.getAttributeValues()) {
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
    if(systemRoles.containsKey(role)) {
      return systemRoles.get(role);
    } else {
      return Collections.EMPTY_LIST;
    }
  }
}
