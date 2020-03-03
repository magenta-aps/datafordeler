package dk.magenta.dafosts.clientcertificates.users;

import dk.magenta.dafosts.library.users.DafoCertificateUserDetails;
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
import java.util.Collection;

import static dk.magenta.dafosts.library.DatabaseQueryManager.IDENTIFICATION_MODE_INVALID;
import static dk.magenta.dafosts.library.DatabaseQueryManager.INVALID_USER_ID;

public class DafoCertificateUserDetailsImpl implements UserDetails, DafoCertificateUserDetails {
    private String username;
    private X509CertImpl certificate;
    int accessAccountId = INVALID_USER_ID;
    int identificationMode = IDENTIFICATION_MODE_INVALID;
    String onBehalfOf = null;

    private final static char[] hexArray = "0123456789ABCDEF".toCharArray();

    DatabaseQueryManager databaseQueryManager;

    public DafoCertificateUserDetailsImpl(
            PreAuthenticatedAuthenticationToken preAuthenticatedAuthenticationToken,
            DatabaseQueryManager databaseQueryManager ) throws UsernameNotFoundException {

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

        this.databaseQueryManager = databaseQueryManager;

        this.accessAccountId = databaseQueryManager.getUserIdByCertificateData(fingerprint);
        this.username = databaseQueryManager.getUserIdentificationByAccountId(this.accessAccountId);

        if(this.accessAccountId == INVALID_USER_ID) {
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
    public int getIdentificationMode() {
        return databaseQueryManager.getCertUserIdentificationMode(accessAccountId);
    }

    @Override
    public int getAccessAccountId() {
        return accessAccountId;
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
        return databaseQueryManager.getUserProfiles(accessAccountId);
    }

    @Override
    public String getOnBehalfOf() {
        return onBehalfOf;
    }

    @Override
    public void setOnBehalfOf(String onBehalfOf) {
        this.onBehalfOf = onBehalfOf;
    }

    public X509CertImpl getCertificate() {
        return certificate;
    }

    @Override
    public String getNameQualifier() {
        return "<none>";
    }
}
