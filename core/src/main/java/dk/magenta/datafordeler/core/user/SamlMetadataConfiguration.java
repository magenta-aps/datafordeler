package dk.magenta.datafordeler.core.user;

import dk.magenta.datafordeler.core.exception.ConfigurationException;
import org.opensaml.Configuration;
import org.opensaml.saml2.metadata.provider.FilesystemMetadataProvider;
import org.opensaml.saml2.metadata.provider.MetadataProvider;
import org.opensaml.security.MetadataCredentialResolver;
import org.opensaml.xml.parse.BasicParserPool;
import org.opensaml.xml.security.keyinfo.KeyInfoCredentialResolver;
import org.opensaml.xml.signature.impl.ExplicitKeySignatureTrustEngine;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.PropertySource;


import java.io.File;

@org.springframework.context.annotation.Configuration
@EnableConfigurationProperties(TokenConfigProperties.class)
@PropertySource("classpath:/application.properties")
public class SamlMetadataConfiguration {

    @Bean
    public MetadataProvider samlMetadataProvider(TokenConfigProperties config)
            throws Exception {
        String path = config.getIssuerMetadataPath();
        if (path == null) {
            throw new ConfigurationException("SAML Issuer metadata path is null");
        }
        File metadataFile = new File(path);
        FilesystemMetadataProvider provider = new FilesystemMetadataProvider(metadataFile);
        provider.setRequireValidMetadata(false);
        provider.setParserPool(new BasicParserPool());
        provider.initialize();
        return provider;
    }

    @Bean
    public ExplicitKeySignatureTrustEngine trustEngine(MetadataProvider metadataProvider) {
        MetadataProvider mdProvider = metadataProvider;
        MetadataCredentialResolver mdCredResolver = new MetadataCredentialResolver(mdProvider);
        KeyInfoCredentialResolver keyInfoCredResolver =
                Configuration.getGlobalSecurityConfiguration().getDefaultKeyInfoCredentialResolver();
        return new ExplicitKeySignatureTrustEngine(mdCredResolver, keyInfoCredResolver);
    }

}
