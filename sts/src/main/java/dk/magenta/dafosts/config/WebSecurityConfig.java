package dk.magenta.dafosts.config;

import com.github.ulisesbocchio.spring.boot.security.saml.bean.SAMLConfigurerBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

@Configuration
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

    @Value("${dafo.sts.identity-id}")
    String identityId;

    @Value("${dafo.sts.dafo-idp-metadata-location}")
    String idpMetadataLocation;

    @Value("${dafo.sts.private-key-der-location}")
    String privateKeyLocation;

    @Value("${dafo.sts.public-key-pem-location}")
    String publicKeyLocation;

    @Bean
    SAMLConfigurerBean saml() {
        return new SAMLConfigurerBean();
    }

    @Bean
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }

    @Override
    public void configure(HttpSecurity http) throws Exception {
        // @formatter:off
        http.httpBasic()
            .disable()
            .csrf()
            .disable()
            .anonymous()
        .and()
            .apply(saml()).serviceProvider()
                .metadataGenerator() //(1)
                .entityId(this.identityId)
                // TODO: This should not be hardcoded
                .entityBaseURL("http://localhost:8080")
            .and()
                .sso() //(2)
                .defaultSuccessURL("/get_token")
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
            .anyRequest()
            .authenticated();
        // @formatter:on
    }
}