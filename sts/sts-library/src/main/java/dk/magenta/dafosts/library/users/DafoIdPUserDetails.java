package dk.magenta.dafosts.library.users;

import dk.magenta.dafosts.library.DatabaseQueryManager;
import org.opensaml.saml2.core.Assertion;
import org.opensaml.saml2.core.Attribute;
import org.opensaml.saml2.core.AttributeStatement;
import org.opensaml.xml.XMLObject;
import org.opensaml.xml.schema.XSAny;
import org.opensaml.xml.schema.XSString;

import java.util.*;

import static dk.magenta.dafosts.library.DafoTokenGenerator.USERPROFILE_CLAIM_URL;

/**
 * Created by row on 07-09-2017.
 */
public class DafoIdPUserDetails implements DafoUserData {

    private int accessAccountId;
    private Assertion sourceAssertion;
    private DatabaseQueryManager databaseQueryManager;

    @Override
    public String getUsername() {
        return sourceAssertion.getSubject().getNameID().getValue();
    }

    @Override
    public int getAccessAccountId() {
        return accessAccountId;
    }

    @Override
    public String getOnBehalfOf() {
        return null;
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
    public Collection<String> getUserProfiles() {
        Set<String> allowedUserProfiles = new HashSet<>();
        List<String> result = new ArrayList<>();
        allowedUserProfiles.addAll(databaseQueryManager.getUserProfiles(accessAccountId));
        for(AttributeStatement attributeStatement : sourceAssertion.getAttributeStatements()) {
            for(Attribute attribute : attributeStatement.getAttributes()) {
                if(attribute.getName().equals(USERPROFILE_CLAIM_URL)) {
                    for(XMLObject value : attribute.getAttributeValues()) {
                        String strValue = getString(value);
                        if(allowedUserProfiles.contains(strValue)) {
                            result.add(strValue);
                        }
                    }
                }
            }
        }

        return result;
    }

    @Override
    public String getNameQualifier() {
        return sourceAssertion.getIssuer().getValue();
    }
}
