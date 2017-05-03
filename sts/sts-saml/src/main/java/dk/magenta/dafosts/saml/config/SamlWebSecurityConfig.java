package dk.magenta.dafosts.saml.config;

import com.github.ulisesbocchio.spring.boot.security.saml.bean.SAMLConfigurerBean;
import com.github.ulisesbocchio.spring.boot.security.saml.bean.override.DSLSAMLAuthenticationProvider;
import com.github.ulisesbocchio.spring.boot.security.saml.bean.override.DSLWebSSOProfileConsumerHoKImpl;
import com.github.ulisesbocchio.spring.boot.security.saml.bean.override.DSLWebSSOProfileConsumerImpl;
import dk.magenta.dafosts.TokenGeneratorProperties;
import dk.magenta.dafosts.saml.users.DafoSAMLUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.saml.log.SAMLDefaultLogger;


@Configuration
@EnableConfigurationProperties(TokenGeneratorProperties.class)
public class SamlWebSecurityConfig extends WebSecurityConfigurerAdapter {

    @Value("${dafo.sts.identity-id}")
    String identityId;

    @Value("${dafo.sts.dafo-idp-metadata-location}")
    String idpMetadataLocation;

    private TokenGeneratorProperties tokenGeneratorProperties;

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
        dslsamlAuthenticationProvider.setConsumer(new DSLWebSSOProfileConsumerImpl());
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


    @Autowired
    private ApplicationContext context;

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
                        // TODO: This should not be hardcoded
                        .entityBaseURL("https://localhost:7443")
                .and()
                    .sso() //(2)
                    .defaultSuccessURL("/")
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
                    .idpDiscoveryEnabled(false)
                .and()
                    .keyManager() //(6)
                    .privateKeyDERLocation(tokenGeneratorProperties.getPrivateKeyDerLocation())
                    .publicKeyPEMLocation(tokenGeneratorProperties.getPublicKeyPemLocation())
                .and()
            .http()
                .authorizeRequests()
                .requestMatchers(saml().endpointsMatcher())
                .permitAll()
            .and()
                .authorizeRequests()
                .anyRequest().authenticated();
        // @formatter:on

    }
}