package dk.magenta.datafordeler.cpr.records.person.data;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import dk.magenta.datafordeler.core.database.DatabaseEntry;
import dk.magenta.datafordeler.cpr.CprPlugin;
import dk.magenta.datafordeler.cpr.records.CprBitemporalRecord;
import dk.magenta.datafordeler.cpr.records.person.CprBitemporalPersonRecord;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

import javax.xml.bind.annotation.XmlElement;
import java.util.Objects;
import java.util.StringJoiner;

/**
 * Storage for data on a Person's address in a foreign country,
 * referenced by {@link dk.magenta.datafordeler.cpr.data.person.data.PersonBaseData}
 */
@Entity
@Table(name = CprPlugin.DEBUG_TABLE_PREFIX + ForeignAddressDataRecord.TABLE_NAME, indexes = {
        @Index(name = CprPlugin.DEBUG_TABLE_PREFIX + ForeignAddressDataRecord.TABLE_NAME + CprBitemporalPersonRecord.DB_FIELD_ENTITY, columnList = CprBitemporalPersonRecord.DB_FIELD_ENTITY + DatabaseEntry.REF),
        @Index(name = CprPlugin.DEBUG_TABLE_PREFIX + ForeignAddressDataRecord.TABLE_NAME + CprBitemporalRecord.DB_FIELD_REGISTRATION_FROM, columnList = CprBitemporalRecord.DB_FIELD_REGISTRATION_FROM),
        @Index(name = CprPlugin.DEBUG_TABLE_PREFIX + ForeignAddressDataRecord.TABLE_NAME + CprBitemporalRecord.DB_FIELD_REGISTRATION_TO, columnList = CprBitemporalRecord.DB_FIELD_REGISTRATION_TO),
        @Index(name = CprPlugin.DEBUG_TABLE_PREFIX + ForeignAddressDataRecord.TABLE_NAME + CprBitemporalRecord.DB_FIELD_EFFECT_FROM, columnList = CprBitemporalRecord.DB_FIELD_EFFECT_FROM),
        @Index(name = CprPlugin.DEBUG_TABLE_PREFIX + ForeignAddressDataRecord.TABLE_NAME + CprBitemporalRecord.DB_FIELD_EFFECT_TO, columnList = CprBitemporalRecord.DB_FIELD_EFFECT_TO),
        @Index(name = CprPlugin.DEBUG_TABLE_PREFIX + ForeignAddressDataRecord.TABLE_NAME + CprBitemporalRecord.DB_FIELD_CORRECTION_OF, columnList = CprBitemporalRecord.DB_FIELD_CORRECTION_OF + DatabaseEntry.REF),
        @Index(name = CprPlugin.DEBUG_TABLE_PREFIX + ForeignAddressDataRecord.TABLE_NAME + CprBitemporalRecord.DB_FIELD_REPLACED_BY, columnList = CprBitemporalRecord.DB_FIELD_REPLACED_BY + DatabaseEntry.REF)
})
public class ForeignAddressDataRecord extends CprBitemporalPersonRecord<ForeignAddressDataRecord> {

    public static final String TABLE_NAME = "cpr_person_foreignaddress_record";

    public ForeignAddressDataRecord() {
    }

    public ForeignAddressDataRecord(String addressLine1, String addressLine2, String addressLine3, String addressLine4, String addressLine5) {
        this.addressLine1 = addressLine1;
        this.addressLine2 = addressLine2;
        this.addressLine3 = addressLine3;
        this.addressLine4 = addressLine4;
        this.addressLine5 = addressLine5;
    }

    @JsonIgnore
    public String getFieldName() {
        return TABLE_NAME;
    }

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


    public String join(String separator) {
        StringJoiner joiner = new StringJoiner(separator);
        if (this.addressLine1 != null && !this.addressLine1.isEmpty()) {
            joiner.add(this.addressLine1);
        }
        if (this.addressLine2 != null && !this.addressLine2.isEmpty()) {
            joiner.add(this.addressLine2);
        }
        if (this.addressLine3 != null && !this.addressLine3.isEmpty()) {
            joiner.add(this.addressLine3);
        }
        if (this.addressLine4 != null && !this.addressLine4.isEmpty()) {
            joiner.add(this.addressLine4);
        }
        if (this.addressLine5 != null && !this.addressLine5.isEmpty()) {
            joiner.add(this.addressLine5);
        }
        return joiner.toString();
    }

    @Override
    public boolean equalData(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equalData(o)) return false;
        ForeignAddressDataRecord that = (ForeignAddressDataRecord) o;
        return Objects.equals(addressLine1, that.addressLine1) &&
                Objects.equals(addressLine2, that.addressLine2) &&
                Objects.equals(addressLine3, that.addressLine3) &&
                Objects.equals(addressLine4, that.addressLine4) &&
                Objects.equals(addressLine5, that.addressLine5);
    }

    @Override
    public boolean hasData() {
        return stringNonEmpty(this.addressLine1) || stringNonEmpty(this.addressLine2) || stringNonEmpty(this.addressLine3) || stringNonEmpty(this.addressLine4) || stringNonEmpty(this.addressLine5);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), addressLine1, addressLine2, addressLine3, addressLine4, addressLine5);
    }

    @Override
    public ForeignAddressDataRecord clone() {
        ForeignAddressDataRecord clone = new ForeignAddressDataRecord();
        clone.addressLine1 = this.addressLine1;
        clone.addressLine2 = this.addressLine2;
        clone.addressLine3 = this.addressLine3;
        clone.addressLine4 = this.addressLine4;
        clone.addressLine5 = this.addressLine5;
        CprBitemporalRecord.copy(this, clone);
        return clone;
    }
}
