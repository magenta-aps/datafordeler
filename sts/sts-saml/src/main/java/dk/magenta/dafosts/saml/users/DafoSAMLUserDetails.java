package dk.magenta.dafosts.saml.users;

import com.github.ulisesbocchio.spring.boot.security.saml.user.SAMLUserDetails;
import dk.magenta.dafosts.library.DatabaseQueryManager;
import dk.magenta.dafosts.library.users.DafoUserData;
import dk.magenta.dafosts.saml.metadata.DafoCachingMetadataManager;
import dk.magenta.dafosts.saml.metadata.DafoMetadataProvider;
import org.opensaml.saml2.core.Attribute;
import org.opensaml.saml2.core.AttributeStatement;
import org.opensaml.xml.XMLObject;
import org.opensaml.xml.schema.XSAny;
import org.opensaml.xml.schema.XSString;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.saml.SAMLCredential;

import java.util.*;

import static dk.magenta.dafosts.library.DafoTokenGenerator.USERPROFILE_CLAIM_URL;
import static dk.magenta.dafosts.library.DatabaseQueryManager.INVALID_USER_ID;
import static dk.magenta.dafosts.saml.metadata.DafoMetadataProvider.*;

/**
 * Extended version of SAMLUserDetails that allows access to the SAMLCredentials used to create the object.
 */
public class DafoSAMLUserDetails extends SAMLUserDetails implements DafoUserData {

    private SAMLCredential samlCredential;
    private Collection<SimpleGrantedAuthority> authorities = new ArrayList<SimpleGrantedAuthority>();
    private DatabaseQueryManager databaseQueryManager;
    private int idpType = IDP_TYPE_SECONDARY;
    private int accessAccountId = INVALID_USER_ID;
    private String nameQualifier;
    private int userProfileAttributeFormat = USERPROFILE_FORMAT_MULTIVALUE;;
    private String userProfileAttributeName = "http://wso2.org/claims/groups";
    private int userProfileFilterType = USERPROFILE_FILTER_NONE;
    private String userProfileFilterValue = "";
    private DafoMetadataProvider dafoMetadataProvider;

    public DafoSAMLUserDetails(
            SAMLCredential samlCredential,
            DafoCachingMetadataManager metadataManager
    ) {
        super(samlCredential);
        this.samlCredential = samlCredential;
        this.authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
        this.authorities.add(new SimpleGrantedAuthority("ROLE_SAML_USER"));
        this.databaseQueryManager = metadataManager.getDatabaseQueryManager();
        String remoteEntityID = samlCredential.getRemoteEntityID();
        if(metadataManager.isDefaultMetadataProvider(remoteEntityID)) {
            nameQualifier = "<none>";
            this.idpType = IDP_TYPE_PRIMARY;
            this.userProfileFilterType = USERPROFILE_FILTER_REMOVE_PREFIX;
            // TODO: Find right value for this
            this.userProfileFilterValue = "DafoSTS";
        } else {
            this.dafoMetadataProvider = metadataManager.getDafoMetadataProvider(remoteEntityID);
            if(this.dafoMetadataProvider != null) {
                this.idpType = this.dafoMetadataProvider.getIdpType();
                this.userProfileAttributeFormat = this.dafoMetadataProvider.getUserprofileAttributeFormat();
                this.userProfileAttributeName = this.dafoMetadataProvider.getUserprofileAttribute();
                this.userProfileFilterType = this.dafoMetadataProvider.getUserprofileAdjustmentFilterType();
                this.userProfileFilterValue = this.dafoMetadataProvider.getUserprofileAdjustmentFilterValue();
            }
            this.accessAccountId = databaseQueryManager.getAccountIdByEntityId(remoteEntityID);
            this.nameQualifier = databaseQueryManager.getUserIdentificationByAccountId(accessAccountId);
        }

    }

    public SAMLCredential getSAMLCredential() {
        return samlCredential;
    }


    @Override
    public int getAccessAccountId() {
        return accessAccountId;
    }

    @Override
    public String getOnBehalfOf() {
        return null;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return this.authorities;
    }

    @Override
    public Collection<String> getUserProfiles() {
        Set<String> allowedUserProfiles;
        if(this.idpType == IDP_TYPE_PRIMARY) {
            // allow everything
            allowedUserProfiles = null;
        } else if(this.idpType == IDP_TYPE_SECONDARY) {
            // Allow only userprofiles associated with the user
            allowedUserProfiles = new HashSet<>();
            allowedUserProfiles.addAll(databaseQueryManager.getUserProfiles(accessAccountId));
        } else {
            // Allow nothing
            allowedUserProfiles = new HashSet<>();
        }

        // Read userprofiles from token
        List<String> groupsFromToken = new ArrayList<>();
        if(this.userProfileAttributeFormat == USERPROFILE_FORMAT_MULTIVALUE) {
            String[] groups = samlCredential.getAttributeAsStringArray(this.userProfileAttributeName);
            if(groups != null) {
                groupsFromToken.addAll(Arrays.asList(groups));
            }
        } else if(this.userProfileAttributeFormat == USERPROFILE_FORMAT_COMMASEPARATED) {
            String valueFromToken = samlCredential.getAttributeAsString(this.userProfileAttributeName);
            if(valueFromToken != null && !valueFromToken.isEmpty()) {
                groupsFromToken.addAll(Arrays.asList(valueFromToken.split("\\s*,\\s*")));
            }
        }
        // Adjust the userprofile values, if needed
        List<String> result = new ArrayList<>();
        for(String value : groupsFromToken) {
            if(this.userProfileFilterValue != null && !this.userProfileFilterValue.isEmpty()) {
                int length = this.userProfileFilterValue.length();
                if(this.userProfileFilterType == USERPROFILE_FILTER_REMOVE_PREFIX) {
                    if(value.length() > length && value.substring(0, length).equals(this.userProfileFilterValue)) {
                        value = value.substring(this.userProfileFilterValue.length());
                    }
                } else if(this.userProfileFilterType == USERPROFILE_FILTER_REMOVE_POSTFIX) {
                    int postfixIndex = value.length() - length;
                    if(postfixIndex >= 0 && value.substring(postfixIndex).equals(this.userProfileFilterValue)) {
                        value = value.substring(0, postfixIndex);
                    }
                }

            }
            if(allowedUserProfiles == null || allowedUserProfiles.contains(value)) {
                result.add(value);
            }
        }

        return result;
    }

    @Override
    public String getNameQualifier() {
        return nameQualifier;
    }
}
