package dk.magenta.dafosts.saml;

import dk.magenta.dafosts.library.TokenGeneratorProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

/**
 * Specification of external configuration for the dafo-sts-saml application.
 * <p>
 *  This file contains all the application specific properties that can be configured using
 *  {@code application.properties} or {@code application.yml} files.
 * </p>
 */
@ConfigurationProperties("dafo.sts")
@EnableConfigurationProperties(TokenGeneratorProperties.class)
public class DafoStsBySamlConfiguration {

    @Autowired
    private TokenGeneratorProperties tokenGeneratorProperties;
    String identityId = "dafo-sts-default-entity-id";
    String idpMetadataLocation = "classpath:/saml/dafo-idp.xml";
    String serverRootURL = "https://localhost:7443";

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
     * dafo.sts.identity-id -  The SAML identity id used by the STS when acting as an IdP and issuing tokens.
     * <p>
     *  Default value is "dafo-sts-default-entity-id"
     * </p>
     * @return The configured value
     */
    public String getIdentityId() {
        return identityId;
    }

    /**
     * dafo.sts.idp-metadata-location - The location of the metadata for the default primary IdP.
     * <p>
     *  Default value is "classpath:/saml/dafo-idp.xml"
     * </p>
     * @return The configured value
     */
    public String getIdpMetadataLocation() {
        return idpMetadataLocation;
    }

    /**
     * dafo.sts.server-root-url - The root URL of the server that can be used to access it from the outside.
     * <p>
     * If the server is running behind a reverse proxy the URL of that reverse proxy should be specified here.
     * The URL should be specified without an ending slash.
     * </p>
     * <p>
     *  Default value: "https://localhost:7443"
     * </p>
     * @return The configured value
     */
    public String getServerRootURL() {
        return serverRootURL;
    }
}
