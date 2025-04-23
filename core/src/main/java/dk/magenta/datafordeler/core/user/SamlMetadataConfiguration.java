package dk.magenta.datafordeler.core.user;

import dk.magenta.datafordeler.core.exception.ConfigurationException;
import net.shibboleth.shared.component.ComponentInitializationException;
import net.shibboleth.shared.resolver.ResolverException;
import net.shibboleth.shared.xml.ParserPool;
import net.shibboleth.shared.xml.impl.BasicParserPool;
import org.opensaml.core.config.InitializationException;
import org.opensaml.core.config.InitializationService;
import org.opensaml.saml.metadata.resolver.MetadataResolver;
import org.opensaml.saml.metadata.resolver.RoleDescriptorResolver;
import org.opensaml.saml.metadata.resolver.impl.FilesystemMetadataResolver;
import org.opensaml.saml.metadata.resolver.impl.PredicateRoleDescriptorResolver;
import org.opensaml.saml.security.impl.MetadataCredentialResolver;
import org.opensaml.xmlsec.config.impl.DefaultSecurityConfigurationBootstrap;
import org.opensaml.xmlsec.keyinfo.KeyInfoCredentialResolver;
import org.opensaml.xmlsec.signature.support.impl.ExplicitKeySignatureTrustEngine;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.PropertySource;

import java.io.File;

@org.springframework.context.annotation.Configuration
@EnableConfigurationProperties(TokenConfigProperties.class)
@PropertySource("classpath:/application.properties")
public class SamlMetadataConfiguration {

    static {
        try {
            InitializationService.initialize();
        } catch (InitializationException e) {
            throw new RuntimeException(e);
        }
    }

    @Bean
    public ParserPool parserPool() throws ComponentInitializationException {
        BasicParserPool parserPool = new BasicParserPool();
        parserPool.setMaxPoolSize(100);
        parserPool.setCoalescing(true);
        parserPool.setIgnoreComments(true);
        parserPool.setIgnoreElementContentWhitespace(true);
        parserPool.setNamespaceAware(true);
        parserPool.setExpandEntityReferences(false);
        parserPool.setXincludeAware(false);
        parserPool.initialize();
        return parserPool;
    }

    @Bean
    public MetadataResolver filesystemMetadataResolver(TokenConfigProperties config, ParserPool parserPool) throws ConfigurationException, ResolverException, ComponentInitializationException {
        String path = config.getIssuerMetadataPath();
        if (path == null) {
            throw new ConfigurationException("SAML Issuer metadata path is null");
        }
        File metadataFile = new File(path);
        System.out.println("metadataFile: "+metadataFile.getAbsolutePath());
        FilesystemMetadataResolver resolver = new DebugMetadataResolver(metadataFile);
        resolver.setRequireValidMetadata(false);
        resolver.setParserPool(parserPool);
        resolver.setId("sts-metadata");
        resolver.initialize();
        return resolver;
    }

    @Bean
    public PredicateRoleDescriptorResolver predicateRoleDescriptorResolver(MetadataResolver metadataResolver) throws ComponentInitializationException {
        PredicateRoleDescriptorResolver predicateRoleDescriptorResolver = new PredicateRoleDescriptorResolver(metadataResolver);
//        predicateRoleDescriptorResolver.setRequireValidMetadata(true);
        predicateRoleDescriptorResolver.setRequireValidMetadata(false);
        predicateRoleDescriptorResolver.initialize();
        return predicateRoleDescriptorResolver;
    }

    @Bean
    public ExplicitKeySignatureTrustEngine trustEngine(RoleDescriptorResolver roleDescriptorResolver) throws ComponentInitializationException {
        KeyInfoCredentialResolver keyInfoCredResolver = DefaultSecurityConfigurationBootstrap.buildBasicInlineKeyInfoCredentialResolver();
        MetadataCredentialResolver metadataCredentialResolver = new DebugCredentialResolver();
        metadataCredentialResolver.setRoleDescriptorResolver(roleDescriptorResolver);
        metadataCredentialResolver.setKeyInfoCredentialResolver(keyInfoCredResolver);
        metadataCredentialResolver.initialize();
        // return new ExplicitKeySignatureTrustEngine(
        return new DebugTrustEngine(
                metadataCredentialResolver,
                keyInfoCredResolver
        );
    }

}
