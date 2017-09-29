package dk.magenta.datafordeler.cpr.data.person.data;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlElement;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by lars on 22-06-17.
 */
@Entity
@Table(name = "cpr_person_addressname")
public class PersonAddressNameData extends AuthorityDetailData {

    public static final String DB_FIELD_ADDRESS_NAME = "addressName";
    public static final String IO_FIELD_ADDRESS_NAME = "adressenavn";
    @Column(name = DB_FIELD_ADDRESS_NAME)
    @JsonProperty(value = IO_FIELD_ADDRESS_NAME)
    @XmlElement(name = IO_FIELD_ADDRESS_NAME)
    private String addressName;

    public String getAddressName() {
        return this.addressName;
    }

    public void setAddressName(String addressName) {
        this.addressName = addressName;
    }

    @Override
    public Map<String, Object> databaseFields() {
        HashMap<String, Object> map = new HashMap<>(super.databaseFields());
        map.put(DB_FIELD_ADDRESS_NAME, this.addressName);
        return map;
    }

    @Override
    public Map<String, Object> asMap() {
        HashMap<String, Object> map = new HashMap<>(super.asMap());
        map.put("addressName", this.addressName);
        return map;
    }
}
