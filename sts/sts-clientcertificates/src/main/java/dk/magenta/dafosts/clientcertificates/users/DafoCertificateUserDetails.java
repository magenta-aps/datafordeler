package dk.magenta.dafosts.clientcertificates.users;

import dk.magenta.dafosts.DafoUserData;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;
import sun.security.x509.X509CertImpl;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Created by jubk on 03-05-2017.
 */
public class DafoCertificateUserDetails implements UserDetails, DafoUserData {
    private String username;

    public DafoCertificateUserDetails(PreAuthenticatedAuthenticationToken preAuthenticatedAuthenticationToken) {

        this.username = preAuthenticatedAuthenticationToken.getPrincipal().toString();

        // TODO: Validate the certificate against the database using issuer and serialnumber
        X509CertImpl certificate = (X509CertImpl)preAuthenticatedAuthenticationToken.getCredentials();
        String serialNumber = certificate.getSerialNumber().toString();
        String issuer = certificate.getIssuerX500Principal().toString();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return AuthorityUtils.commaSeparatedStringToAuthorityList("ROLE_USER,ROLE_CERTIFICATE_USER");
    }

    @Override
    public String getPassword() {
        return "";
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        // TODO: This should actually check this
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public Collection<String> getUserProfiles() {
        // TODO: Lookup UserProfiles in the database
        return new ArrayList<>();
    }
}
