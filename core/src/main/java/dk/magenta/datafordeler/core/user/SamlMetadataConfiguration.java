package dk.magenta.datafordeler.core.user;

import dk.magenta.datafordeler.core.exception.ConfigurationException;
import net.shibboleth.shared.component.ComponentInitializationException;
import net.shibboleth.shared.primitive.TimerSupport;
import net.shibboleth.shared.resolver.ResolverException;
import net.shibboleth.shared.xml.ParserPool;
import net.shibboleth.shared.xml.impl.BasicParserPool;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
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
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Timer;

@org.springframework.context.annotation.Configuration
@EnableConfigurationProperties(TokenConfigProperties.class)
@PropertySource("classpath:/application.properties")
public class SamlMetadataConfiguration {

    private final Logger log = LogManager.getLogger(SamlMetadataConfiguration.class.getCanonicalName());

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
        FilesystemMetadataResolver resolver = new FilesystemMetadataResolver(metadataFile);
        resolver.setMinRefreshDelay(Duration.of(4, ChronoUnit.HOURS));
        resolver.setRequireValidMetadata(false);
        resolver.setParserPool(parserPool);
        resolver.setId("sts-metadata");
        resolver.initialize();
        log.info("STS Metadata expiration: {}, path: {}", resolver.getExpirationTime(), path);
        return resolver;
    }

    @Bean
    public PredicateRoleDescriptorResolver predicateRoleDescriptorResolver(MetadataResolver metadataResolver) throws ComponentInitializationException {
        PredicateRoleDescriptorResolver predicateRoleDescriptorResolver = new PredicateRoleDescriptorResolver(metadataResolver);
        predicateRoleDescriptorResolver.setRequireValidMetadata(false);  // Allow expired STS signing certificate (for now)
        predicateRoleDescriptorResolver.initialize();
        return predicateRoleDescriptorResolver;
    }

    @Bean
    public ExplicitKeySignatureTrustEngine trustEngine(RoleDescriptorResolver roleDescriptorResolver) throws ComponentInitializationException {
        KeyInfoCredentialResolver keyInfoCredResolver = DefaultSecurityConfigurationBootstrap.buildBasicInlineKeyInfoCredentialResolver();
        MetadataCredentialResolver metadataCredentialResolver = new MetadataCredentialResolver();
        metadataCredentialResolver.setRoleDescriptorResolver(roleDescriptorResolver);
        metadataCredentialResolver.setKeyInfoCredentialResolver(keyInfoCredResolver);
        metadataCredentialResolver.initialize();
        return new ExplicitKeySignatureTrustEngine(
                metadataCredentialResolver,
                keyInfoCredResolver
        );
    }

}
