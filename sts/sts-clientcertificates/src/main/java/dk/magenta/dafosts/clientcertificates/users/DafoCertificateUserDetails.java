package dk.magenta.dafosts.clientcertificates.users;

import dk.magenta.dafosts.library.users.DafoUserData;
import dk.magenta.dafosts.library.DatabaseQueryManager;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;
import sun.security.x509.X509CertImpl;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateEncodingException;
import java.util.ArrayList;
import java.util.Collection;

public class DafoCertificateUserDetails implements UserDetails, DafoUserData {
    private String username;
    private X509CertImpl certificate;
    int databaseId;

    private final static char[] hexArray = "0123456789ABCDEF".toCharArray();

    DatabaseQueryManager databaseQueryManager;


    public DafoCertificateUserDetails(
            PreAuthenticatedAuthenticationToken preAuthenticatedAuthenticationToken,
            DatabaseQueryManager databaseQueryManager ) throws UsernameNotFoundException {

        this.databaseQueryManager = databaseQueryManager;
        this.username = preAuthenticatedAuthenticationToken.getPrincipal().toString();

        // TODO: Validate the certificate against the database using issuer and serialnumber
        this.certificate = (X509CertImpl)preAuthenticatedAuthenticationToken.getCredentials();

        // Validate certificate agains the user database
        String fingerprint;
        try {
            fingerprint = getCertificateFingerprint();
        }
        catch(NoSuchAlgorithmException e) {
            throw new UsernameNotFoundException("No SHA-256 digest for certificate fingerprint");
        }
        catch(CertificateEncodingException e) {
            throw new UsernameNotFoundException("Could not encode certificate for fingerprint");
        }
        this.databaseId = databaseQueryManager.getUserIdByCertificateData(fingerprint);

        if(this.databaseId == DatabaseQueryManager.INVALID_USER_ID) {
            throw new UsernameNotFoundException("No user found for the given certificate");
        }
    }

    public String getCertificateFingerprint() throws NoSuchAlgorithmException, CertificateEncodingException {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        byte[] der = certificate.getEncoded();
        md.update(der);
        byte[] digest = md.digest();

        // Size is two hex chars and a colon for each input byte, minus the last colon
        char[] hexChars = new char[digest.length * 3 - 1];
        for (int j = 0; j < digest.length; j++) {
            int v = digest[j] & 0xFF;
            hexChars[j * 3] = hexArray[v >>> 4];
            hexChars[j * 3 + 1] = hexArray[v & 0x0F];
            if(j < digest.length - 1) {
                hexChars[j * 3 + 2] = ':';
            }
        }

        return new String(hexChars).toLowerCase();
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
        if(databaseId == DatabaseQueryManager.INVALID_USER_ID) {
            return new ArrayList<>();
        } else {
            return databaseQueryManager.getUserProfiles(databaseId);
        }
    }

    public X509CertImpl getCertificate() {
        return certificate;
    }
}
