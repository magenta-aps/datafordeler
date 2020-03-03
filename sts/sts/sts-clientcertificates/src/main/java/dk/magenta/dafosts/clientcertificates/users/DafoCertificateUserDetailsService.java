package dk.magenta.dafosts.clientcertificates.users;

import dk.magenta.dafosts.library.DatabaseQueryManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.*;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;
import org.springframework.stereotype.Component;

@Component
public class DafoCertificateUserDetailsService
        implements AuthenticationUserDetailsService<PreAuthenticatedAuthenticationToken> {

    @Autowired
    DatabaseQueryManager databaseQueryManager;

    @Override
    public UserDetails loadUserDetails(PreAuthenticatedAuthenticationToken preAuthenticatedAuthenticationToken)
            throws UsernameNotFoundException {
        DafoCertificateUserDetailsImpl user = new DafoCertificateUserDetailsImpl(
                preAuthenticatedAuthenticationToken,
                databaseQueryManager
        );

        return user;
    }
}
