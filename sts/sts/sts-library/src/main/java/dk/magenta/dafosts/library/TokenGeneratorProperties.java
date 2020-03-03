package dk.magenta.dafosts.library;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Definition of properties used for generating DAFO tokens.
 */
@ConfigurationProperties("dafo.sts")
public class TokenGeneratorProperties {
    /**
     * The location of the private key used for signing tokens, in DER format.
     */
    String privateKeyDerLocation = "classpath:/saml/dafo_sts.key.der";

    /**
     * The location of the public key used for signing tokens, in PEM format.
     */
    String publicKeyPemLocation = "classpath:/saml/dafo_sts.pem";;

    public String getPrivateKeyDerLocation() {
        return privateKeyDerLocation;
    }

    public void setPrivateKeyDerLocation(String privateKeyDerLocation) {
        this.privateKeyDerLocation = privateKeyDerLocation;
    }

    public String getPublicKeyPemLocation() {
        return publicKeyPemLocation;
    }

    public void setPublicKeyPemLocation(String publicKeyPemLocation) {
        this.publicKeyPemLocation = publicKeyPemLocation;
    }
}
