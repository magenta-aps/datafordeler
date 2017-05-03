package dk.magenta.dafosts.config;


import dk.magenta.dafosts.users.DafoCertificateUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

import java.security.Principal;

@Configuration
@Order(1)
public class CertificateWebSecurityContext extends WebSecurityConfigurerAdapter {
    @Autowired
    private DafoCertificateUserDetailsService dafoCertificateUserDetailsService;


    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .antMatcher("/by_certificate/**")
                    .authorizeRequests()
                        .anyRequest().hasRole("CERTIFICATE_USER")
                    .and()
                        .x509()
                        .subjectPrincipalRegex("CN=(.*?)(?:,|$)")
                        .userDetailsService(dafoCertificateUserDetailsService);
    }
}
