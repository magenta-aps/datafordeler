package dk.magenta.datafordeler.cpr.data.person.data;

import com.fasterxml.jackson.annotation.JsonProperty;
import dk.magenta.datafordeler.cpr.CprPlugin;
import dk.magenta.datafordeler.cpr.data.DetailData;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlElement;
import java.util.Collections;
import java.util.Map;

/**
 * Storage for data on a Person's address coname,
 * referenced by {@link dk.magenta.datafordeler.cpr.data.person.data.PersonBaseData}
 */
@Entity
@Table(name = CprPlugin.DEBUG_TABLE_PREFIX + "cpr_person_address_coname")
public class PersonAddressConameData extends DetailData {

    public static final String DB_FIELD_CONAME = "coname";
    public static final String IO_FIELD_CONAME = "conavn";
    @Column(name = DB_FIELD_CONAME)
    @JsonProperty(value = IO_FIELD_CONAME)
    @XmlElement(name = IO_FIELD_CONAME)
    private String coname;

    public String getConame() {
        return this.coname;
    }

    public void setConame(String coname) {
        this.coname = coname;
    }

    @Override
    public Map<String, Object> databaseFields() {
        return Collections.singletonMap(DB_FIELD_CONAME, this.coname);
    }

    @Override
    public Map<String, Object> asMap() {
        return Collections.singletonMap("coname", this.coname);
    }

    @Override
    protected PersonAddressConameData clone() {
        PersonAddressConameData clone = new PersonAddressConameData();
        clone.coname = this.coname;
        clone.setDafoUpdated(this.getDafoUpdated());
        return clone;
    }
}
