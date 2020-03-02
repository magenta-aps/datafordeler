package dk.magenta.datafordeler.cpr.data.person.data;

import com.fasterxml.jackson.annotation.JsonProperty;
import dk.magenta.datafordeler.cpr.CprPlugin;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlElement;
import java.util.HashMap;
import java.util.Map;

/**
 * Storage for data on a Person's name verification,
 * referenced by {@link dk.magenta.datafordeler.cpr.data.person.data.PersonBaseData}
 */
@Entity
@Table(name = CprPlugin.DEBUG_TABLE_PREFIX + "cpr_person_name_verification")
public class PersonNameVerificationData extends AuthorityDetailData {


    public static final String DB_FIELD_VERIFIED = "verified";
    public static final String IO_FIELD_VERIFIED = "verificeret";
    @Column(name = DB_FIELD_VERIFIED)
    @JsonProperty(value = IO_FIELD_VERIFIED)
    @XmlElement(name = IO_FIELD_VERIFIED)
    private boolean verified;

    public boolean isVerified() {
        return this.verified;
    }

    public void setVerified(boolean verified) {
        this.verified = verified;
    }


    @Override
    public Map<String, Object> databaseFields() {
        HashMap<String, Object> map = new HashMap<>(super.databaseFields());
        map.put(DB_FIELD_VERIFIED, this.verified);
        return map;
    }

    @Override
    public Map<String, Object> asMap() {
        HashMap<String, Object> map = new HashMap<>(super.asMap());
        map.put("verified", this.verified);
        return map;
    }

    @Override
    protected PersonNameVerificationData clone() {
        PersonNameVerificationData clone = new PersonNameVerificationData();
        clone.verified = this.verified;
        clone.setAuthority(this.getAuthority());
        clone.setDafoUpdated(this.getDafoUpdated());
        return clone;
    }
}
