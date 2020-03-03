package dk.magenta.dafosts.saml.config;

import com.github.ulisesbocchio.spring.boot.security.saml.bean.SAMLConfigurerBean;
import com.github.ulisesbocchio.spring.boot.security.saml.bean.override.DSLSAMLAuthenticationProvider;
import com.github.ulisesbocchio.spring.boot.security.saml.bean.override.DSLSAMLContextProviderImpl;
import com.github.ulisesbocchio.spring.boot.security.saml.bean.override.DSLWebSSOProfileConsumerHoKImpl;
import com.github.ulisesbocchio.spring.boot.security.saml.resource.KeystoreFactory;
import dk.magenta.dafosts.library.DatabaseQueryManager;
import dk.magenta.dafosts.library.TokenGeneratorProperties;
import dk.magenta.dafosts.saml.DafoStsBySamlConfiguration;
import dk.magenta.dafosts.saml.metadata.DafoCachingMetadataManager;
import dk.magenta.dafosts.saml.users.DafoSAMLUserDetailsService;
import org.opensaml.saml2.metadata.provider.MetadataProviderException;
import org.opensaml.util.resource.ResourceException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.saml.context.SAMLContextProvider;
import org.springframework.security.saml.key.JKSKeyManager;
import org.springframework.security.saml.key.KeyManager;
import org.springframework.security.saml.log.SAMLDefaultLogger;
import org.springframework.security.saml.storage.EmptyStorageFactory;

import java.security.KeyStore;
import java.util.Collections;
import java.util.Map;


/**
 * Configures SAML SSO Login below the /by_saml_sso/ URL.
 */
@Configuration
public class SamlWebSecurityConfig extends WebSecurityConfigurerAdapter {

    private DafoStsBySamlConfiguration config;
    private TokenGeneratorProperties tokenGeneratorProperties;
    private DafoWebSSOProfileConsumer dafoWebSSOProfileConsumer;

    public DafoWebSSOProfileConsumer getDafoWebSSOProfileConsumer() {
        return dafoWebSSOProfileConsumer;
    }

    @Autowired
    public void setConfigProperties(DafoStsBySamlConfiguration config) {
        this.config = config;
        this.tokenGeneratorProperties = config.getTokenGeneratorProperties();
    }

    @Bean
    public SAMLDefaultLogger samlLogger() {
        return new SAMLDefaultLogger();
    }

    @Bean
    public DSLSAMLAuthenticationProvider dslsamlAuthenticationProvider() {
        DSLSAMLAuthenticationProvider dslsamlAuthenticationProvider = new DSLSAMLAuthenticationProvider();
        dslsamlAuthenticationProvider.setUserDetails(dafoSAMLUserDetailsService);
        dafoWebSSOProfileConsumer = new DafoWebSSOProfileConsumer();
        dslsamlAuthenticationProvider.setConsumer(dafoWebSSOProfileConsumer);
        dslsamlAuthenticationProvider.setHokConsumer(new DSLWebSSOProfileConsumerHoKImpl());
        return dslsamlAuthenticationProvider;
    }

    @Bean
    public KeyManager keyManager() {
        DefaultResourceLoader loader = new DefaultResourceLoader();
        KeystoreFactory keystoreFactory = new KeystoreFactory(loader);
        String publicKeyPEMLocation = tokenGeneratorProperties.getPublicKeyPemLocation();
        String privateKeyDERLocation = tokenGeneratorProperties.getPrivateKeyDerLocation();
        // This uses the same defaults as defined in ...spring.boot.security.saml.properties.KeyManagerProperties
        String defaultKey = "localhost";
        String keyPassword = "";
        Map<String, String> keyPasswords = Collections.singletonMap("localhost", keyPassword);
        KeyStore keyStore = keystoreFactory.loadKeystore(
                publicKeyPEMLocation, privateKeyDERLocation, defaultKey, ""
        );
        return new JKSKeyManager(keyStore, keyPasswords, defaultKey);
    }

    @Bean
    @Qualifier("metadata")
    public DafoCachingMetadataManager metadata(
            KeyManager keyManager,
            DatabaseQueryManager queryManager,
            DafoSAMLUserDetailsService samlUserDetailsService
    ) throws MetadataProviderException, ResourceException {
        DafoCachingMetadataManager manager = new DafoCachingMetadataManager();

        manager.initialize(config.getIdpMetadataLocation(), queryManager);

        samlUserDetailsService.setMetadataManager(manager);
        return manager;
    }

    @Bean
    SAMLConfigurerBean saml() {
        return new SAMLConfigurerBean();
    }

    @Bean
    SAMLContextProvider samlContextProvider() {
        DSLSAMLContextProviderImpl provider = new DSLSAMLContextProviderImpl();
        // Disable in-response-to checking
        // TODO: Make this configurable
        provider.setStorageFactory(new EmptyStorageFactory());
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }

    @Autowired
    DafoSAMLUserDetailsService dafoSAMLUserDetailsService;

    @Override
    public void configure(HttpSecurity http) throws Exception {
        // @formatter:off
        http
            .httpBasic()
                .disable().csrf()
                .disable().anonymous()
            .and()
                .apply(saml())
                    .serviceProvider()
                        .metadataGenerator() //(1)
                        .entityId(config.getIdentityId())
                        .entityBaseURL(config.getServerRootURL())
                .and()
                    .sso() //(2)
                    .defaultSuccessURL("/by_saml_sso/get_token")
                    .idpSelectionPageURL("/idpselection")
                .and()
                    .logout() //(3)
                    .defaultTargetURL("/")
                .and()
                    .extendedMetadata() //(5)
                    .idpDiscoveryEnabled(true)
                .and()
                    .ssoProfileConsumer(dafoWebSSOProfileConsumer)
                    .sso()
                .and()
            .http()
                .authorizeRequests()
                .antMatchers("/by_saml_sso/**").authenticated()
            .and()
                .authorizeRequests()
                .antMatchers("/").permitAll()
                .antMatchers("/error").permitAll()
                .requestMatchers(saml().endpointsMatcher()).permitAll();
        // @formatter:on
    }
}