package dk.magenta.datafordeler.cpr.records.person.data;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import dk.magenta.datafordeler.core.database.DatabaseEntry;
import dk.magenta.datafordeler.cpr.CprPlugin;
import dk.magenta.datafordeler.cpr.records.CprBitemporalRecord;
import dk.magenta.datafordeler.cpr.records.person.CprBitemporalPersonRecord;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Index;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlElement;
import java.util.Objects;

@Entity
@Table(name = CprPlugin.DEBUG_TABLE_PREFIX + CitizenshipDataRecord.TABLE_NAME, indexes = {
        @Index(name = CprPlugin.DEBUG_TABLE_PREFIX + CitizenshipDataRecord.TABLE_NAME + CprBitemporalPersonRecord.DB_FIELD_ENTITY, columnList = CprBitemporalPersonRecord.DB_FIELD_ENTITY + DatabaseEntry.REF),
        @Index(name = CprPlugin.DEBUG_TABLE_PREFIX + CitizenshipDataRecord.TABLE_NAME + CprBitemporalRecord.DB_FIELD_REGISTRATION_FROM, columnList = CprBitemporalRecord.DB_FIELD_REGISTRATION_FROM),
        @Index(name = CprPlugin.DEBUG_TABLE_PREFIX + CitizenshipDataRecord.TABLE_NAME + CprBitemporalRecord.DB_FIELD_REGISTRATION_TO, columnList = CprBitemporalRecord.DB_FIELD_REGISTRATION_TO),
        @Index(name = CprPlugin.DEBUG_TABLE_PREFIX + CitizenshipDataRecord.TABLE_NAME + CprBitemporalRecord.DB_FIELD_EFFECT_FROM, columnList = CprBitemporalRecord.DB_FIELD_EFFECT_FROM),
        @Index(name = CprPlugin.DEBUG_TABLE_PREFIX + CitizenshipDataRecord.TABLE_NAME + CprBitemporalRecord.DB_FIELD_EFFECT_TO, columnList = CprBitemporalRecord.DB_FIELD_EFFECT_TO),
        @Index(name = CprPlugin.DEBUG_TABLE_PREFIX + CitizenshipDataRecord.TABLE_NAME + CprBitemporalRecord.DB_FIELD_CORRECTION_OF, columnList = CprBitemporalRecord.DB_FIELD_CORRECTION_OF + DatabaseEntry.REF),
        @Index(name = CprPlugin.DEBUG_TABLE_PREFIX + CitizenshipDataRecord.TABLE_NAME + CprBitemporalRecord.DB_FIELD_REPLACED_BY, columnList = CprBitemporalRecord.DB_FIELD_REPLACED_BY + DatabaseEntry.REF)
})
public class CitizenshipDataRecord extends CprBitemporalPersonRecord<CitizenshipDataRecord> {

    public static final String TABLE_NAME = "cpr_person_citizenship_record";

    public CitizenshipDataRecord() {
    }

    public CitizenshipDataRecord(int countryCode) {
        this.countryCode = countryCode;
    }

    @JsonIgnore
    public String getFieldName() {
        return TABLE_NAME;
    }

    public static final String DB_FIELD_COUNTRY_CODE = "countryCode";
    public static final String IO_FIELD_COUNTRY_CODE = "landekode";
    @Column(name = DB_FIELD_COUNTRY_CODE)
    @JsonProperty(value = IO_FIELD_COUNTRY_CODE)
    @XmlElement(name = IO_FIELD_COUNTRY_CODE)
    private int countryCode;

    public int getCountryCode() {
        return this.countryCode;
    }

    public void setCountryCode(int countryCode) {
        this.countryCode = countryCode;
    }


    @Override
    public boolean equalData(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equalData(o)) return false;
        CitizenshipDataRecord that = (CitizenshipDataRecord) o;
        return countryCode == that.countryCode;
    }

    @Override
    public boolean hasData() {
        return this.countryCode != 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), countryCode);
    }

    @Override
    public CitizenshipDataRecord clone() {
        CitizenshipDataRecord clone = new CitizenshipDataRecord();
        clone.countryCode = this.countryCode;
        CprBitemporalRecord.copy(this, clone);
        return clone;
    }

}
