package dk.magenta.dafosts.config;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

@Configuration
@Order(1)
public class CertificateWebSecurityContext extends WebSecurityConfigurerAdapter {
    protected void configure(HttpSecurity http) throws Exception {
        http
                .antMatcher("/by_certificate/**")
                    .authorizeRequests()
                    .antMatchers("/by_certificate/", "/by_certificate/home")
                    .permitAll()
                    .anyRequest().hasRole("CERTIFICATE_USER")
                .and()
                    .formLogin()
                        .loginPage("/by_certificate/login")
                        .permitAll()
                .and()
                    .logout()
                        .permitAll();
    }

    @Autowired
    public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
        auth
                .inMemoryAuthentication()
                .withUser("user").password("password").roles("USER", "CERTIFICATE_USER");
    }
}
