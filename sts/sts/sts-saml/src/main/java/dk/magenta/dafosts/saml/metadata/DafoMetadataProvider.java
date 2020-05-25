package dk.magenta.dafosts.saml.metadata;

import dk.magenta.dafosts.library.DafoIdPData;
import org.opensaml.saml2.metadata.EntityDescriptor;
import org.opensaml.saml2.metadata.IDPSSODescriptor;
import org.opensaml.saml2.metadata.SingleSignOnService;
import org.opensaml.saml2.metadata.provider.AbstractMetadataProvider;
import org.opensaml.saml2.metadata.provider.MetadataProviderException;
import org.opensaml.xml.XMLObject;
import org.opensaml.xml.io.UnmarshallingException;
import org.opensaml.xml.parse.ParserPool;

import java.io.ByteArrayInputStream;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;

import static org.opensaml.common.xml.SAMLConstants.SAML20P_NS;
import static org.opensaml.common.xml.SAMLConstants.SAML2_POST_BINDING_URI;

/**
 * A simple metadataprovider that represents and access-giving IdP Stored in the DAFO-admin database.
 */
public class DafoMetadataProvider extends AbstractMetadataProvider {
    // These values should correspond to the values used in django/dafousers/model_constants.py in the DAFO-admin
    // project.
    public static final int USERPROFILE_FORMAT_MULTIVALUE = 1;
    public static final int USERPROFILE_FORMAT_COMMASEPARATED = 2;

    public static final int USERPROFILE_FILTER_NONE = 1;
    public static final int USERPROFILE_FILTER_REMOVE_PREFIX = 2;
    public static final int USERPROFILE_FILTER_REMOVE_POSTFIX = 3;

    public static final int IDP_TYPE_PRIMARY = 1;
    public static final int IDP_TYPE_SECONDARY = 2;

    private DafoIdPData dafoIdpData;
    private XMLObject cachedMetadata;
    private String cachedSSOLocation;


    public DafoMetadataProvider(DafoIdPData dafoIdpData, ParserPool parser) {
        this.dafoIdpData = dafoIdpData;
        this.setParserPool(parser);
    }

    public String getName() {
        return dafoIdpData.getName();
    }

    public String getEntityId() {
        return dafoIdpData.getEntityId();
    }

    public int getIdpType() {
        return dafoIdpData.getIdpType();
    }

    public String getXmlMetadataString() {
        return dafoIdpData.getMetadataXml();
    }

    public String getUserprofileAttribute() {
        return dafoIdpData.getUserprofileAttribute();
    }

    public int getUserprofileAttributeFormat() {
        return dafoIdpData.getUserprofileAttributeFormat();
    }

    public int getUserprofileAdjustmentFilterType() {
        return dafoIdpData.getUserprofileAdjustmentFilterType();
    }

    public String getUserprofileAdjustmentFilterValue() {
        return dafoIdpData.getUserprofileAdjustmentFilterValue();
    }

    public LocalDateTime getLastUpdate() {
        return dafoIdpData.getUpdated();
    }

    /** {@inheritDoc} */
    @Override
    protected XMLObject doGetMetadata() throws MetadataProviderException {
        if(cachedMetadata == null && getXmlMetadataString() != null) {
            try {
                cachedMetadata = unmarshallMetadata(new ByteArrayInputStream(getXmlMetadataString().getBytes()));
            } catch (UnmarshallingException e) {
                throw new MetadataProviderException("Failed to parse metadata", e);
            }
        }

        return cachedMetadata;
    }

    public void setDafoIdpData(DafoIdPData dafoIdpData) {
        this.dafoIdpData = dafoIdpData;
        this.cachedMetadata = null;
        this.cachedSSOLocation = null;
    }

    /**
     * Gets the location of the IdP's SingleSignOn service that uses HTTP-POST and SAML 2.0.
     * @return A location URL or null if no matching service was found.
     */
    public String getSSOLocation() {
        if(cachedSSOLocation != null) {
            if(cachedSSOLocation.equals("")) {
                return null;
            } else {
                return cachedSSOLocation;
            }
        }
        try {
            EntityDescriptor entityDescriptor = getEntityDescriptor(this.dafoIdpData.getEntityId());
            if(entityDescriptor != null) {
                IDPSSODescriptor idpssoDescriptor = entityDescriptor.getIDPSSODescriptor(SAML20P_NS);
                if(idpssoDescriptor != null) {
                    for(SingleSignOnService ssoService : idpssoDescriptor.getSingleSignOnServices()) {
                        if(ssoService.getBinding().equals(SAML2_POST_BINDING_URI)) {
                            cachedSSOLocation = ssoService.getLocation();
                            return cachedSSOLocation;
                        }
                    }
                }
            }
        }
        catch(Exception e) {
            e.printStackTrace();
        }
        // Set cachedSSOLocation to the empty string to indicate no SSO location was found.
        cachedSSOLocation = "";
        return null;
    }

}
