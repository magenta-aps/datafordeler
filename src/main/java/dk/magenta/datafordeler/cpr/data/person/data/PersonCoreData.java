package dk.magenta.datafordeler.cpr.data.person.data;

import com.fasterxml.jackson.annotation.JsonProperty;
import dk.magenta.datafordeler.cpr.CprPlugin;
import org.apache.commons.lang.StringUtils;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlElement;
import java.util.HashMap;
import java.util.Map;

/**
 * Storage for data on a Person's core data (gender, current cpr number),
 * referenced by {@link dk.magenta.datafordeler.cpr.data.person.data.PersonBaseData}
 */
@Entity
@Table(name = CprPlugin.DEBUG_TABLE_PREFIX + "cpr_person_core")
public class PersonCoreData extends AuthorityDetailData {

    public enum Koen {
        MAND,
        KVINDE
    }

    public static final String DB_FIELD_CPR_NUMBER = "cprNumber";
    public static final String IO_FIELD_CPR_NUMBER = "personnummer";
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



    public static final String DB_FIELD_GENDER = "gender";
    public static final String IO_FIELD_GENDER = "køn";
    @Column(name = DB_FIELD_GENDER)
    @JsonProperty(value = IO_FIELD_GENDER)
    @XmlElement(name = IO_FIELD_GENDER)
    private Koen gender;

    public Koen getGender() {
        return this.gender;
    }

    public void setGender(Koen gender) {
        this.gender = gender;
    }

    public void setKoen(String koen) {
        if (koen != null) {
            if (koen.equalsIgnoreCase("M")) {
                this.setGender(Koen.MAND);
            } else if (koen.equalsIgnoreCase("K")) {
                this.setGender(Koen.KVINDE);
            }
        }
    }

    @Override
    public Map<String, Object> databaseFields() {
        HashMap<String, Object> map = new HashMap<>();
        map.put(DB_FIELD_CPR_NUMBER, this.cprNumber);
        map.put(DB_FIELD_GENDER, this.gender);
        return map;
    }

    @Override
    public Map<String, Object> asMap() {
        HashMap<String, Object> map = new HashMap<>(super.asMap());
        if (StringUtils.isNotEmpty(cprNumber)) {
            map.put("cprNumber", this.cprNumber);
        }
        if (this.gender != null) {
            map.put("gender", this.gender);
        }
        return map;
    }

    @Override
    protected PersonCoreData clone() {
        PersonCoreData clone = new PersonCoreData();
        clone.cprNumber = this.cprNumber;
        clone.gender = this.gender;
        clone.setAuthority(this.getAuthority());
        clone.setDafoUpdated(this.getDafoUpdated());
        return clone;
    }
}
