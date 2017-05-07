package dk.magenta.dafosts.saml.users;

import com.github.ulisesbocchio.spring.boot.security.saml.user.SAMLUserDetails;
import dk.magenta.dafosts.users.DafoUserData;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.saml.SAMLCredential;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

/**
 * Extended version of SAMLUserDetails that allows access to the SAMLCredentials used to create the object.
 */
public class DafoSAMLUserDetails extends SAMLUserDetails implements DafoUserData {

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

    @Override
    public Collection<String> getUserProfiles() {
        String[] groups = samlCredential.getAttributeAsStringArray("http://wso2.org/claims/groups");
        if(groups != null) {
            return Arrays.asList(groups);
        } else {
            return new ArrayList<>();
        }
    }
}
