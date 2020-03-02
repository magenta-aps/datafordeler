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
 * Storage for data on a Person's historic cpr number,
 * referenced by {@link dk.magenta.datafordeler.cpr.data.person.data.PersonBaseData}
 */
@Entity
@Table(name = CprPlugin.DEBUG_TABLE_PREFIX + "cpr_person_cprnumber")
public class PersonNumberData extends AuthorityDetailData {

    public static final String DB_FIELD_CPR_NUMBER = "cprNumber";
    public static final String IO_FIELD_CPR_NUMBER = "cprNummer";
    @Column(name = DB_FIELD_CPR_NUMBER)
    @JsonProperty(value = IO_FIELD_CPR_NUMBER)
    @XmlElement(name = IO_FIELD_CPR_NUMBER)
    private String cprNumber;

    public String getCprNumber() {
        return this.cprNumber;
    }

    public void setCprNumber(String cprNumber) {
        this.cprNumber = cprNumber;
    }

    @Override
    public Map<String, Object> databaseFields() {
        HashMap<String, Object> map = new HashMap<>();
        map.put(DB_FIELD_CPR_NUMBER, this.cprNumber);
        return map;
    }

    @Override
    public Map<String, Object> asMap() {
        HashMap<String, Object> map = new HashMap<>();
        map.put("cprNumber", this.cprNumber);
        return map;
    }

    @Override
    protected PersonNumberData clone() {
        PersonNumberData clone = new PersonNumberData();
        clone.cprNumber = this.cprNumber;
        clone.setAuthority(this.getAuthority());
        clone.setDafoUpdated(this.getDafoUpdated());
        return clone;
    }
}
