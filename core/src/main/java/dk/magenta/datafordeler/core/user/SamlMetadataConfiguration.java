package dk.magenta.datafordeler.core.user;

/*
//import dk.magenta.datafordeler.core.exception.ConfigurationException;
//import org.opensaml.Configuration;
//import org.opensaml.saml2.metadata.provider.FilesystemMetadataProvider;
//import org.opensaml.saml2.metadata.provider.MetadataProvider;
//import org.opensaml.security.MetadataCredentialResolver;
//import org.opensaml.xml.parse.BasicParserPool;
//import org.opensaml.xml.security.keyinfo.KeyInfoCredentialResolver;
//import org.opensaml.xml.signature.impl.ExplicitKeySignatureTrustEngine;
import dk.magenta.datafordeler.core.exception.ConfigurationException;
import org.opensaml.Configuration;
import org.opensaml.core.config.InitializationService;
import org.opensaml.saml.saml2.core.Assertion;
import org.opensaml.saml.security.impl.MetadataCredentialResolver;
import org.opensaml.saml2.metadata.provider.FilesystemMetadataProvider;
import org.opensaml.saml2.metadata.provider.MetadataProvider;
import org.opensaml.xml.parse.BasicParserPool;
import org.opensaml.xmlsec.config.impl.DefaultSecurityConfigurationBootstrap;
import org.opensaml.xmlsec.keyinfo.KeyInfoCredentialResolver;
import org.opensaml.xmlsec.signature.support.impl.ExplicitKeySignatureTrustEngine;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.PropertySource;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.saml2.provider.service.authentication.OpenSaml4AuthenticationProvider;
import org.springframework.security.web.SecurityFilterChain;
//import org.springframework.security.saml2.provider.*;
import java.io.File;
*/

import dk.magenta.datafordeler.core.exception.ConfigurationException;
import net.shibboleth.utilities.java.support.component.ComponentInitializationException;
import net.shibboleth.utilities.java.support.resolver.CriteriaSet;
import net.shibboleth.utilities.java.support.resolver.ResolverException;
import org.opensaml.core.config.InitializationException;
import org.opensaml.core.config.InitializationService;
import org.opensaml.saml.metadata.resolver.FilesystemMetadataResolver;
import org.opensaml.saml.metadata.resolver.impl.PredicateRoleDescriptorResolver;
import org.opensaml.saml.security.impl.MetadataCredentialResolver;
import org.opensaml.saml2.metadata.provider.FilesystemMetadataProvider;
import org.opensaml.saml2.metadata.provider.MetadataProvider;
import org.opensaml.security.credential.Credential;
import org.opensaml.security.credential.CredentialResolver;
import org.opensaml.security.credential.impl.PredicateRoleCredentialResolver;
import org.opensaml.security.criteria.EntityIDCriteria;
import org.opensaml.security.x509.X509Credential;
import org.opensaml.xml.parse.BasicParserPool;
import org.opensaml.xmlsec.config.impl.DefaultSecurityConfigurationBootstrap;
import org.opensaml.xmlsec.keyinfo.KeyInfoCredentialResolver;
import org.opensaml.xmlsec.keyinfo.impl.KeyInfoProvider;
import org.opensaml.xmlsec.keyinfo.impl.provider.DEREncodedKeyValueProvider;
import org.opensaml.xmlsec.keyinfo.impl.provider.InlineX509DataProvider;
import org.opensaml.xmlsec.keyinfo.impl.provider.RSAKeyValueProvider;
import org.opensaml.xmlsec.signature.support.SignatureTrustEngine;
import org.opensaml.xmlsec.signature.support.impl.ExplicitKeySignatureTrustEngine;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.PropertySource;

import java.io.File;

import static org.springframework.security.config.Customizer.withDefaults;

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
        MetadataCredentialResolver metadataCredentialResolver = new MetadataCredentialResolver(/*metadataProvider*/);
        // TODO: brug metadataProvider i metadataCredentialResolver på en måde
        return new ExplicitKeySignatureTrustEngine(
                metadataCredentialResolver,
                DefaultSecurityConfigurationBootstrap.buildBasicInlineKeyInfoCredentialResolver()
        );
    }


}
