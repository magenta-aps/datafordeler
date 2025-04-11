package dk.magenta.datafordeler.cpr.records.person.data;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import dk.magenta.datafordeler.core.database.Bitemporal;
import dk.magenta.datafordeler.core.database.DatabaseEntry;
import dk.magenta.datafordeler.cpr.CprPlugin;
import dk.magenta.datafordeler.cpr.records.CprBitemporalRecord;
import dk.magenta.datafordeler.cpr.records.person.CprBitemporalPersonRecord;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

import javax.xml.bind.annotation.XmlElement;
import java.time.OffsetDateTime;
import java.util.Objects;

/**
 * Storage for data on a Person's country code,
 * referenced by {@link dk.magenta.datafordeler.cpr.data.person.data.PersonBaseData}
 */
@Entity
@Table(name = CprPlugin.DEBUG_TABLE_PREFIX + ForeignAddressEmigrationDataRecord.TABLE_NAME, indexes = {
        @Index(name = CprPlugin.DEBUG_TABLE_PREFIX + ForeignAddressEmigrationDataRecord.TABLE_NAME + CprBitemporalPersonRecord.DB_FIELD_ENTITY, columnList = CprBitemporalPersonRecord.DB_FIELD_ENTITY + DatabaseEntry.REF),
        @Index(name = CprPlugin.DEBUG_TABLE_PREFIX + ForeignAddressEmigrationDataRecord.TABLE_NAME + CprBitemporalRecord.DB_FIELD_REGISTRATION_FROM, columnList = CprBitemporalRecord.DB_FIELD_REGISTRATION_FROM),
        @Index(name = CprPlugin.DEBUG_TABLE_PREFIX + ForeignAddressEmigrationDataRecord.TABLE_NAME + CprBitemporalRecord.DB_FIELD_REGISTRATION_TO, columnList = CprBitemporalRecord.DB_FIELD_REGISTRATION_TO),
        @Index(name = CprPlugin.DEBUG_TABLE_PREFIX + ForeignAddressEmigrationDataRecord.TABLE_NAME + CprBitemporalRecord.DB_FIELD_EFFECT_FROM, columnList = CprBitemporalRecord.DB_FIELD_EFFECT_FROM),
        @Index(name = CprPlugin.DEBUG_TABLE_PREFIX + ForeignAddressEmigrationDataRecord.TABLE_NAME + CprBitemporalRecord.DB_FIELD_EFFECT_TO, columnList = CprBitemporalRecord.DB_FIELD_EFFECT_TO),
        @Index(name = CprPlugin.DEBUG_TABLE_PREFIX + ForeignAddressEmigrationDataRecord.TABLE_NAME + CprBitemporalRecord.DB_FIELD_CORRECTION_OF, columnList = CprBitemporalRecord.DB_FIELD_CORRECTION_OF + DatabaseEntry.REF),
        @Index(name = CprPlugin.DEBUG_TABLE_PREFIX + ForeignAddressEmigrationDataRecord.TABLE_NAME + CprBitemporalRecord.DB_FIELD_REPLACED_BY, columnList = CprBitemporalRecord.DB_FIELD_REPLACED_BY + DatabaseEntry.REF)
})
public class ForeignAddressEmigrationDataRecord extends CprBitemporalPersonRecord<ForeignAddressEmigrationDataRecord> {

    public static final String TABLE_NAME = "cpr_person_foreignaddress_migration_record";

    public ForeignAddressEmigrationDataRecord() {
    }

    public ForeignAddressEmigrationDataRecord(int immigrationCountryCode, int emigrationCountryCode, OffsetDateTime emigrationRegistration, OffsetDateTime immigrationRegistration) {
        this.immigrationCountryCode = immigrationCountryCode;
        this.emigrationCountryCode = emigrationCountryCode;
        this.emigrationRegistration = emigrationRegistration;
        this.immigrationRegistration = immigrationRegistration;
    }

    public ForeignAddressEmigrationDataRecord(int emigrationCountryCode) {
        this.immigrationCountryCode = 0;
        this.emigrationCountryCode = emigrationCountryCode;
    }

    @JsonIgnore
    public String getFieldName() {
        return TABLE_NAME;
    }

    public static final String DB_FIELD_IN_COUNTRY_CODE = "immigrationCountryCode";
    public static final String IO_FIELD_IN_COUNTRY_CODE = "indrejseLandekode";
    @Column(name = DB_FIELD_IN_COUNTRY_CODE)
    @JsonProperty(value = IO_FIELD_IN_COUNTRY_CODE)
    @XmlElement(name = IO_FIELD_IN_COUNTRY_CODE)
    private int immigrationCountryCode;

    public int getImmigrationCountryCode() {
        return this.immigrationCountryCode;
    }

    public ForeignAddressEmigrationDataRecord setImmigrationCountryCode(int immigrationCountryCode) {
        this.immigrationCountryCode = immigrationCountryCode;
        return this;
    }


    public static final String DB_FIELD_OUT_COUNTRY_CODE = "emigrationCountryCode";
    public static final String IO_FIELD_OUT_COUNTRY_CODE = "udrejseLandekode";
    @Column(name = DB_FIELD_OUT_COUNTRY_CODE)
    @JsonProperty(value = IO_FIELD_OUT_COUNTRY_CODE)
    @XmlElement(name = IO_FIELD_OUT_COUNTRY_CODE)
    private int emigrationCountryCode;

    public int getEmigrationCountryCode() {
        return this.emigrationCountryCode;
    }

    public ForeignAddressEmigrationDataRecord setEmigrationCountryCode(int emigrationCountryCode) {
        this.emigrationCountryCode = emigrationCountryCode;
        return this;
    }


    public static final String DB_FIELD_EXIT_REGISTRATION = "emigrationRegistration";
    public static final String IO_FIELD_EXIT_REGISTRATION = "udrejseRegistrering";
    @Column(name = DB_FIELD_EXIT_REGISTRATION, columnDefinition = "datetime2")
    @JsonProperty(value = IO_FIELD_EXIT_REGISTRATION)
    @XmlElement(name = IO_FIELD_EXIT_REGISTRATION)
    private OffsetDateTime emigrationRegistration;


    public static final String DB_FIELD_RETURN_REGISTRATION = "immigrationRegistration";
    public static final String IO_FIELD_RETURN_REGISTRATION = "indrejseRegistrering";
    @Column(name = DB_FIELD_RETURN_REGISTRATION, columnDefinition = "datetime2")
    @XmlElement(name = IO_FIELD_RETURN_REGISTRATION)
    private OffsetDateTime immigrationRegistration;

    @JsonProperty(value = IO_FIELD_RETURN_REGISTRATION)
    public OffsetDateTime getImmigrationRegistration() {
        return Bitemporal.fixOffsetOut(this.immigrationRegistration);
    }

    @JsonProperty(value = IO_FIELD_RETURN_REGISTRATION)
    public void setImmigrationRegistration(OffsetDateTime immigrationRegistration) {
        this.immigrationRegistration = Bitemporal.fixOffsetIn(immigrationRegistration);
    }


    @Override
    public boolean equalData(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equalData(o)) return false;
        ForeignAddressEmigrationDataRecord that = (ForeignAddressEmigrationDataRecord) o;
        return immigrationCountryCode == that.immigrationCountryCode &&
                emigrationCountryCode == that.emigrationCountryCode &&
                Objects.equals(emigrationRegistration, that.emigrationRegistration) &&
                Objects.equals(immigrationRegistration, that.immigrationRegistration);
    }

    @Override
    public boolean hasData() {
        return this.immigrationCountryCode != 0 || this.emigrationCountryCode != 0 || this.immigrationRegistration != null || this.emigrationRegistration != null;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), immigrationCountryCode, emigrationCountryCode);
    }

    @Override
    public ForeignAddressEmigrationDataRecord clone() {
        ForeignAddressEmigrationDataRecord clone = new ForeignAddressEmigrationDataRecord();
        clone.immigrationCountryCode = this.immigrationCountryCode;
        clone.emigrationCountryCode = this.emigrationCountryCode;
        CprBitemporalRecord.copy(this, clone);
        return clone;
    }
}
