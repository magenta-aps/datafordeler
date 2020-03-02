package dk.magenta.datafordeler.cvr.data.shared;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import dk.magenta.datafordeler.core.database.DatabaseEntry;
import dk.magenta.datafordeler.cvr.data.DetailData;
import dk.magenta.datafordeler.cvr.data.unversioned.Address;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;

import javax.persistence.Entity;
import javax.persistence.Index;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlElement;
import java.util.HashMap;
import java.util.Map;

import static dk.magenta.datafordeler.cvr.data.shared.AddressData.DB_FIELD_ADDRESS;

/**
 * Class for Address data
 */
@Entity
@Table(name="cvr_company_address", indexes = {
        @Index(name = "cvr_company_address_address", columnList = DB_FIELD_ADDRESS + DatabaseEntry.REF)
})
public class AddressData extends DetailData {

    public static final String DB_FIELD_ADDRESS = "address";
    public static final String IO_FIELD_ADDRESS = "adresse";

    @JsonProperty(value = IO_FIELD_ADDRESS)
    @XmlElement(name = IO_FIELD_ADDRESS)
    @ManyToOne
    @Cascade(value = CascadeType.ALL)
    private Address address;

    public Address getAddress() {
        return this.address;
    }

    public void setAddress(Address address) {
        this.address = address;
    }

    public Map<String, Object> asMap() {
        HashMap<String, Object> map = new HashMap<>();
        map.put("address", this.address);
        return map;
    }

    /**
     * Return a map of attributes
     * @return
     */
    @JsonIgnore
    public Map<String, Object> databaseFields() {
        HashMap<String, Object> map = new HashMap<>();
        map.put(DB_FIELD_ADDRESS, this.address);
        return map;
    }

}
