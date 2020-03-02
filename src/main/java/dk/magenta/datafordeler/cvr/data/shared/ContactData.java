package dk.magenta.datafordeler.cvr.data.shared;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Index;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;
import java.util.HashMap;
import java.util.Map;

import static dk.magenta.datafordeler.cvr.data.shared.ContactData.DB_FIELD_TYPE;
import static dk.magenta.datafordeler.cvr.data.shared.SingleData.DB_FIELD_VALUE;

/**
 * Storage for data on a Company's contact data,
 * referenced by {@link dk.magenta.datafordeler.cvr.data.company.CompanyBaseData}
 */
@Entity
@Table(name = "cvr_company_contact", indexes = {
        @Index(name = "cvr_contact_type", columnList = DB_FIELD_TYPE),
        @Index(name = "cvr_contact_value", columnList = DB_FIELD_VALUE),
        @Index(name = "cvr_contact_data", columnList = DB_FIELD_TYPE + ", " + DB_FIELD_VALUE)
})
public class ContactData extends SingleData<String> {

    public enum Type {
        NAVN,
        EMAILADRESSE,
        TELEFONNUMMER,
        TELEFAXNUMMER,
        HJEMMESIDE,
        OBLIGATORISK_EMAILADRESSE
    }

    public ContactData() {
    }

    public ContactData(Type type) {
        this.setType(type);
    }

    //----------------------------------------------------

    public static final String DB_FIELD_TYPE = "type";
    public static final String IO_FIELD_TYPE = "type";

    @JsonIgnore
    @XmlTransient
    @Column(name = DB_FIELD_TYPE)
    private Type type;

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    //----------------------------------------------------

    public static final String DB_FIELD_SECRET = "secret";
    public static final String IO_FIELD_SECRET = "hemmelig";

    @Column(name = DB_FIELD_SECRET)
    @JsonProperty(value = IO_FIELD_SECRET)
    @XmlElement(name = IO_FIELD_SECRET)
    private boolean secret;

    public boolean isSecret() {
        return this.secret;
    }

    public void setSecret(boolean secret) {
        this.secret = secret;
    }

    //---------------------------------------------------

    public Map<String, Object> asMap() {
        HashMap<String, Object> fields = new HashMap<>();
        fields.put(IO_FIELD_VALUE, this.getValue());
        fields.put(IO_FIELD_SECRET, this.secret);
        fields.put(IO_FIELD_TYPE, this.type);
        return fields;
    }

    /**
     * Return a map of attributes, including those from the superclass
     * @return
     */
    @JsonIgnore
    public Map<String, Object> databaseFields() {
        HashMap<String, Object> map = new HashMap<>(super.databaseFields());
        map.put(DB_FIELD_SECRET, this.secret);
        map.put(DB_FIELD_TYPE, this.type);
        return map;
    }


}
