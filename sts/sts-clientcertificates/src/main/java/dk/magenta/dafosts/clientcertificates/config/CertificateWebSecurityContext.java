package dk.magenta.dafosts.clientcertificates.config;

import dk.magenta.dafosts.library.DafoTokenGenerator;
import dk.magenta.dafosts.library.TokenGeneratorProperties;
import dk.magenta.dafosts.clientcertificates.users.DafoCertificateUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
@EnableConfigurationProperties(TokenGeneratorProperties.class)
public class CertificateWebSecurityContext extends WebSecurityConfigurerAdapter {
    @Autowired
    private DafoCertificateUserDetailsService dafoCertificateUserDetailsService;

    @Bean
    public DafoTokenGenerator dafoTokenGenerator(TokenGeneratorProperties properties) throws Exception {
        return new DafoTokenGenerator(properties);
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
            .authorizeRequests()
                .anyRequest().hasRole("CERTIFICATE_USER")
            .and()
                .x509()
                .subjectPrincipalRegex("CN=(.*?)(?:,|$)")
                .authenticationUserDetailsService(dafoCertificateUserDetailsService);
    }
}
