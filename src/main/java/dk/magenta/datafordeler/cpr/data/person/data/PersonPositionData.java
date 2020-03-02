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
 * Storage for data on a Person's job position,
 * referenced by {@link dk.magenta.datafordeler.cpr.data.person.data.PersonBaseData}
 */
@Entity
@Table(name = CprPlugin.DEBUG_TABLE_PREFIX + "cpr_person_position")
public class PersonPositionData extends AuthorityDetailData {


    public static final String DB_FIELD_POSITION = "position";
    public static final String IO_FIELD_POSITION = "stilling";
    @Column(name = DB_FIELD_POSITION)
    @JsonProperty(value = IO_FIELD_POSITION)
    @XmlElement(name = IO_FIELD_POSITION)
    private String position;

    public String getPosition() {
        return this.position;
    }

    public void setPosition(String position) {
        this.position = position;
    }


    @Override
    public Map<String, Object> databaseFields() {
        HashMap<String, Object> map = new HashMap<>(super.databaseFields());
        map.put(DB_FIELD_POSITION, this.position);
        return map;
    }

    @Override
    public Map<String, Object> asMap() {
        HashMap<String, Object> map = new HashMap<>(super.asMap());
        map.put("position", this.position);
        return map;
    }

    @Override
    protected PersonPositionData clone() {
        PersonPositionData clone = new PersonPositionData();
        clone.position = this.position;
        clone.setAuthority(this.getAuthority());
        clone.setDafoUpdated(this.getDafoUpdated());
        return clone;
    }
}
