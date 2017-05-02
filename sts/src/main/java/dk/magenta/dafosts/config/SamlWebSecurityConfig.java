package dk.magenta.dafosts.config;

import com.github.ulisesbocchio.spring.boot.security.saml.bean.SAMLConfigurerBean;
import com.github.ulisesbocchio.spring.boot.security.saml.bean.override.DSLSAMLAuthenticationProvider;
import com.github.ulisesbocchio.spring.boot.security.saml.bean.override.DSLWebSSOProfileConsumerHoKImpl;
import com.github.ulisesbocchio.spring.boot.security.saml.bean.override.DSLWebSSOProfileConsumerImpl;
import dk.magenta.dafosts.users.DafoSAMLUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.saml.SAMLAuthenticationProvider;
import org.springframework.security.saml.log.SAMLDefaultLogger;
import org.springframework.security.saml.log.SAMLLogger;
import org.springframework.security.saml.metadata.MetadataManager;
import org.springframework.security.saml.websso.WebSSOProfileConsumer;
import org.springframework.security.saml.websso.WebSSOProfileConsumerHoKImpl;
import org.springframework.security.saml.websso.WebSSOProfileConsumerImpl;

import javax.annotation.PostConstruct;


@Configuration
public class SamlWebSecurityConfig extends WebSecurityConfigurerAdapter {

    @Value("${dafo.sts.identity-id}")
    String identityId;

    @Value("${dafo.sts.dafo-idp-metadata-location}")
    String idpMetadataLocation;

    @Value("${dafo.sts.private-key-der-location}")
    String privateKeyLocation;

    @Value("${dafo.sts.public-key-pem-location}")
    String publicKeyLocation;

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
                .antMatcher("/by_saml/**")
                .apply(saml())
                    .serviceProvider()
                        .metadataGenerator() //(1)
                        .entityId(this.identityId)
                        // TODO: This should not be hardcoded
                        .entityBaseURL("http://localhost:8080/by_saml")
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
                    .privateKeyDERLocation(this.privateKeyLocation)
                    .publicKeyPEMLocation(this.publicKeyLocation)
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