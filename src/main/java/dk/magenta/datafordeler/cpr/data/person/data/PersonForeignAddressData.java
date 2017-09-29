package dk.magenta.datafordeler.cpr.data.person.data;

import com.fasterxml.jackson.annotation.JsonInclude;
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
@Table(name = "cpr_person_foreign_address")
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class PersonForeignAddressData extends AuthorityDetailData {


    public static final String DB_FIELD_ADDRESS_LINE1 = "addressLine1";
    public static final String IO_FIELD_ADDRESS_LINE1 = "adresselinie1";
    @Column(name = DB_FIELD_ADDRESS_LINE1)
    @JsonProperty(value = IO_FIELD_ADDRESS_LINE1)
    @XmlElement(name = IO_FIELD_ADDRESS_LINE1)
    private String addressLine1;

    public String getAddressLine1() {
        return addressLine1;
    }

    public void setAddressLine1(String addressLine1) {
        this.addressLine1 = addressLine1;
    }



    public static final String DB_FIELD_ADDRESS_LINE2 = "addressLine2";
    public static final String IO_FIELD_ADDRESS_LINE2 = "adresselinie2";
    @Column(name = DB_FIELD_ADDRESS_LINE2)
    @JsonProperty(value = IO_FIELD_ADDRESS_LINE2)
    @XmlElement(name = IO_FIELD_ADDRESS_LINE2)
    private String addressLine2;

    public String getAddressLine2() {
        return addressLine2;
    }

    public void setAddressLine2(String addressLine2) {
        this.addressLine2 = addressLine2;
    }



    public static final String DB_FIELD_ADDRESS_LINE3 = "addressLine3";
    public static final String IO_FIELD_ADDRESS_LINE3 = "adresselinie3";
    @Column(name = DB_FIELD_ADDRESS_LINE3)
    @JsonProperty(value = IO_FIELD_ADDRESS_LINE3)
    @XmlElement(name = IO_FIELD_ADDRESS_LINE3)
    private String addressLine3;

    public String getAddressLine3() {
        return addressLine3;
    }

    public void setAddressLine3(String addressLine3) {
        this.addressLine3 = addressLine3;
    }



    public static final String DB_FIELD_ADDRESS_LINE4 = "addressLine4";
    public static final String IO_FIELD_ADDRESS_LINE4 = "adresselinie4";
    @Column(name = DB_FIELD_ADDRESS_LINE4)
    @JsonProperty(value = IO_FIELD_ADDRESS_LINE4)
    @XmlElement(name = IO_FIELD_ADDRESS_LINE4)
    private String addressLine4;

    public String getAddressLine4() {
        return addressLine4;
    }

    public void setAddressLine4(String addressLine4) {
        this.addressLine4 = addressLine4;
    }



    public static final String DB_FIELD_ADDRESS_LINE5 = "addressLine5";
    public static final String IO_FIELD_ADDRESS_LINE5 = "adresselinie5";
    @Column(name = DB_FIELD_ADDRESS_LINE5)
    @JsonProperty(value = IO_FIELD_ADDRESS_LINE5)
    @XmlElement(name = IO_FIELD_ADDRESS_LINE5)
    private String addressLine5;

    public String getAddressLine5() {
        return addressLine5;
    }

    public void setAddressLine5(String addressLine5) {
        this.addressLine5 = addressLine5;
    }


    @Override
    public Map<String, Object> databaseFields() {
        HashMap<String, Object> map = new HashMap<>(super.databaseFields());
        map.put(DB_FIELD_ADDRESS_LINE1, this.addressLine1);
        map.put(DB_FIELD_ADDRESS_LINE2, this.addressLine2);
        map.put(DB_FIELD_ADDRESS_LINE3, this.addressLine3);
        map.put(DB_FIELD_ADDRESS_LINE4, this.addressLine4);
        map.put(DB_FIELD_ADDRESS_LINE5, this.addressLine5);
        return map;
    }

    @Override
    public Map<String, Object> asMap() {
        HashMap<String, Object> map = new HashMap<>(super.asMap());
        map.put("addressLine1", this.addressLine1);
        map.put("addressLine2", this.addressLine2);
        map.put("addressLine3", this.addressLine3);
        map.put("addressLine4", this.addressLine4);
        map.put("addressLine5", this.addressLine5);
        return map;
    }
}
