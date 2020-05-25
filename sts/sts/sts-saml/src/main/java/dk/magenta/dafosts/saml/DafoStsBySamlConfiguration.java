package dk.magenta.dafosts.saml;

import dk.magenta.dafosts.library.TokenGeneratorProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

/**
 * Specification of external configuration for the dafo-sts-saml application.
 * <p>
 *  This file contains all the application specific properties that can be configured using
 *  {@code application.properties} or {@code application.yml} files.
 * </p>
 */
@ConfigurationProperties(prefix="dafo.sts")
@EnableConfigurationProperties(TokenGeneratorProperties.class)
public class DafoStsBySamlConfiguration {
    @Autowired
    private TokenGeneratorProperties tokenGeneratorProperties;
    private String identityId = "dafo-sts-default-entity-id";
    private String idpMetadataLocation = "classpath:/saml/dafo-idp.xml";
    private String serverRootURL = "https://localhost:7443";

    /**
     * The configuration used when issuing tokens from the STS.
     * <p>
     * Configurations values for token generation are documented in the
     * {@link dk.magenta.dafosts.library.TokenGeneratorProperties} class.
     * </p>
     * @return A configured instance of {@link dk.magenta.dafosts.library.TokenGeneratorProperties}.
     */
    public TokenGeneratorProperties getTokenGeneratorProperties() {
        return tokenGeneratorProperties;
    }

    /**
     * The SAML identity id used by the STS when acting as an IdP and issuing tokens. This IdentityID is also used
     * when acting as a service provider in regards to single-sign-on with external IdPs.
     * <p>
     *  Configuration property: {@code dafo.sts.identity-id}
     * </p>
     * <p>
     *  Default value: "dafo-sts-default-entity-id"
     * </p>
     * @return The value configured by the {@code dafo.sts.identity-id} property
     */
    public String getIdentityId() {
        return identityId;
    }

    public void setIdentityId(String identityId) {
        this.identityId = identityId;
    }

    /**
     * The location of the metadata for the default primary IdP.
     * <p>
     *  Configuration property: {@code dafo.sts.idp-metadata-location}
     * </p>
     * <p>
     *  Default value: "classpath:/saml/dafo-idp.xml"
     * </p>
     * @return The value configured by the {@code dafo.sts.idp-metadata-location} property
     */
    public String getIdpMetadataLocation() {
        return idpMetadataLocation;
    }

    public void setIdpMetadataLocation(String idpMetadataLocation) {
        this.idpMetadataLocation = idpMetadataLocation;
    }

    /**
     * The root URL of the server that can be used to access it from the outside.
     * <p>
     *  Configuration property: {@code dafo.sts.server-root-url}
     * </p>
     * <p>
     *  Default value: "https://localhost:7443"
     * </p>
     * <p>
     * If the server is running behind a reverse proxy the URL of that reverse proxy should be specified here.
     * The URL should be specified without an ending slash.
     * </p>
     * @return The value configured by the {@code dafo.sts.server-root-url} property
     */
    public String getServerRootURL() {
        return serverRootURL;
    }

    public void setServerRootURL(String serverRootURL) {
        this.serverRootURL = serverRootURL;
    }
}
