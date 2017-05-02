package dk.magenta.dafosts.users;

import com.github.ulisesbocchio.spring.boot.security.saml.user.SAMLUserDetails;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.saml.SAMLCredential;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Extended version of SAMLUserDetails that allows access to the SAMLCredentials used to create the object.
 */
public class DafoSAMLUserDetails extends SAMLUserDetails {

    private SAMLCredential samlCredential;
    private Collection<SimpleGrantedAuthority> authorities = new ArrayList<SimpleGrantedAuthority>();

    public DafoSAMLUserDetails(SAMLCredential samlCredential) {
        super(samlCredential);
        this.samlCredential = samlCredential;
        this.authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
        this.authorities.add(new SimpleGrantedAuthority("ROLE_SAML_USER"));
    }

    public SAMLCredential getSAMLCredential() {
        return samlCredential;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return this.authorities;
    }
}
