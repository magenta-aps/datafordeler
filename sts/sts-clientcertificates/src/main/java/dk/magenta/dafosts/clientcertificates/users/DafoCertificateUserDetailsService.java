package dk.magenta.dafosts.clientcertificates.users;

import dk.magenta.dafosts.DatabaseQueryManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.*;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;
import org.springframework.stereotype.Component;

import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateEncodingException;

@Component
public class DafoCertificateUserDetailsService implements AuthenticationUserDetailsService<PreAuthenticatedAuthenticationToken> {

    @Autowired
    DatabaseQueryManager databaseQueryManager;

    @Override
    public UserDetails loadUserDetails(PreAuthenticatedAuthenticationToken preAuthenticatedAuthenticationToken)
            throws UsernameNotFoundException {
        DafoCertificateUserDetails user = new DafoCertificateUserDetails(
                preAuthenticatedAuthenticationToken,
                databaseQueryManager
        );

        return user;
    }
}
