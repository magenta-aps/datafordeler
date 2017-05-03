package dk.magenta.dafosts.saml.users;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.saml.SAMLCredential;
import org.springframework.security.saml.userdetails.SAMLUserDetailsService;
import org.springframework.stereotype.Service;

@Service
public class DafoSAMLUserDetailsService implements SAMLUserDetailsService {

    // Logger
    private static final Logger LOG = LoggerFactory.getLogger(DafoSAMLUserDetailsService.class);

    public Object loadUserBySAML(SAMLCredential credential) throws UsernameNotFoundException {
        String userID = credential.getNameID().getValue();
        LOG.info(userID + " is logged in");
        return new DafoSAMLUserDetails(credential);
    }

}
