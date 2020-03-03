package dk.magenta.dafosts.saml.users;

import dk.magenta.dafosts.library.DatabaseQueryManager;
import dk.magenta.dafosts.saml.metadata.DafoCachingMetadataManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.saml.SAMLCredential;
import org.springframework.security.saml.userdetails.SAMLUserDetailsService;
import org.springframework.stereotype.Service;

@Service
public class DafoSAMLUserDetailsService implements SAMLUserDetailsService {
    // Logger
    private static final Logger LOG = LoggerFactory.getLogger(DafoSAMLUserDetailsService.class);

    DafoCachingMetadataManager metadataManager;

    public Object loadUserBySAML(SAMLCredential credential) throws UsernameNotFoundException {
        String userID = credential.getNameID().getValue();
        LOG.info(userID + " is logged in");
        return new DafoSAMLUserDetails(credential, metadataManager);
    }

    public void setMetadataManager(DafoCachingMetadataManager metadataManager) {
        this.metadataManager = metadataManager;
    }
}
