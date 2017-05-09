package dk.magenta.dafosts.saml.config;

import com.github.ulisesbocchio.spring.boot.security.saml.bean.SAMLConfigurerBean;
import com.github.ulisesbocchio.spring.boot.security.saml.bean.override.DSLSAMLAuthenticationProvider;
import com.github.ulisesbocchio.spring.boot.security.saml.bean.override.DSLWebSSOProfileConsumerHoKImpl;
import dk.magenta.dafosts.library.TokenGeneratorProperties;
import dk.magenta.dafosts.saml.users.DafoSAMLUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.saml.log.SAMLDefaultLogger;


/**
 * Configures SAML SSO Login below the /by_saml_sso/ URL.
 */
@Configuration
@EnableConfigurationProperties(TokenGeneratorProperties.class)
public class SamlWebSecurityConfig extends WebSecurityConfigurerAdapter {

    @Value("${dafo.sts.identity-id}")
    String identityId;

    @Value("${dafo.sts.dafo-idp-metadata-location}")
    String idpMetadataLocation;

    @Value("${dafo.sts.dafo-sts-root-url}")
    String spRootURL;

    private TokenGeneratorProperties tokenGeneratorProperties;
    private DafoWebSSOProfileConsumer dafoWebSSOProfileConsumer;

    public DafoWebSSOProfileConsumer getDafoWebSSOProfileConsumer() {
        return dafoWebSSOProfileConsumer;
    }

    @Autowired
    public void setTokenGeneratorProperties(TokenGeneratorProperties tokenGeneratorProperties) {
        this.tokenGeneratorProperties = tokenGeneratorProperties;
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
    SAMLConfigurerBean saml() {
        return new SAMLConfigurerBean();
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
                        .entityId(this.identityId)
                        .entityBaseURL(spRootURL)
                .and()
                    .sso() //(2)
                    .defaultSuccessURL("/by_saml_sso/get_token")
                    .idpSelectionPageURL("/idpselection")
                .and()
                    .logout() //(3)
                    .defaultTargetURL("/")
                .and()
                    .metadataManager() //(4)
                    .metadataLocations(this.idpMetadataLocation)
                    .refreshCheckInterval(0)
                .and()
                    .extendedMetadata() //(5)
                    // TODO: Remember to change this back
                    .idpDiscoveryEnabled(true)
                .and()
                    .keyManager() //(6)
                    .privateKeyDERLocation(tokenGeneratorProperties.getPrivateKeyDerLocation())
                    .publicKeyPEMLocation(tokenGeneratorProperties.getPublicKeyPemLocation())
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