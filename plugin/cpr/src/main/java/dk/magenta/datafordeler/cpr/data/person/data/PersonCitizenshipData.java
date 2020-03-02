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
 * Storage for data on a Person's church relation,
 * referenced by {@link PersonBaseData}
 */
@Entity
@Table(name = CprPlugin.DEBUG_TABLE_PREFIX + "cpr_person_citizenship")
public class PersonCitizenshipData extends AuthorityDetailData {

    public static final String DB_FIELD_COUNTRY_CODE = "countryCode";
    public static final String IO_FIELD_COUNTRY_CODE = "landekode";
    @Column(name = DB_FIELD_COUNTRY_CODE)
    @JsonProperty(value = IO_FIELD_COUNTRY_CODE)
    @XmlElement(name = IO_FIELD_COUNTRY_CODE)
    private int countryCode;

    public int getCountryCode() {
        return this.countryCode;
    }

    public void setCountryCode(int countryCode) {
        this.countryCode = countryCode;
    }

    @Override
    public Map<String, Object> databaseFields() {
        HashMap<String, Object> map = new HashMap<>();
        map.put(DB_FIELD_COUNTRY_CODE, this.countryCode);
        return map;
    }

    @Override
    protected PersonCitizenshipData clone() {
        PersonCitizenshipData clone = new PersonCitizenshipData();
        clone.countryCode = this.countryCode;
        clone.setAuthority(this.getAuthority());
        clone.setDafoUpdated(this.getDafoUpdated());
        return clone;
    }

}
