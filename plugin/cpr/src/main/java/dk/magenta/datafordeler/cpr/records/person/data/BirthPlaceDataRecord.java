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

import java.util.Objects;

/**
 * Storage for data on a Person's birth,
 */
@Entity
@Table(name = CprPlugin.DEBUG_TABLE_PREFIX + BirthPlaceDataRecord.TABLE_NAME, indexes = {
        @Index(name = CprPlugin.DEBUG_TABLE_PREFIX + BirthPlaceDataRecord.TABLE_NAME + CprBitemporalPersonRecord.DB_FIELD_ENTITY, columnList = CprBitemporalPersonRecord.DB_FIELD_ENTITY + DatabaseEntry.REF),
        @Index(name = CprPlugin.DEBUG_TABLE_PREFIX + BirthPlaceDataRecord.TABLE_NAME + BirthPlaceDataRecord.DB_FIELD_BIRTH_PLACE_CODE, columnList = BirthPlaceDataRecord.DB_FIELD_BIRTH_PLACE_CODE),
        @Index(name = CprPlugin.DEBUG_TABLE_PREFIX + BirthPlaceDataRecord.TABLE_NAME + CprBitemporalRecord.DB_FIELD_REGISTRATION_FROM, columnList = CprBitemporalRecord.DB_FIELD_REGISTRATION_FROM),
        @Index(name = CprPlugin.DEBUG_TABLE_PREFIX + BirthPlaceDataRecord.TABLE_NAME + CprBitemporalRecord.DB_FIELD_REGISTRATION_TO, columnList = CprBitemporalRecord.DB_FIELD_REGISTRATION_TO),
        @Index(name = CprPlugin.DEBUG_TABLE_PREFIX + BirthPlaceDataRecord.TABLE_NAME + CprBitemporalRecord.DB_FIELD_EFFECT_FROM, columnList = CprBitemporalRecord.DB_FIELD_EFFECT_FROM),
        @Index(name = CprPlugin.DEBUG_TABLE_PREFIX + BirthPlaceDataRecord.TABLE_NAME + CprBitemporalRecord.DB_FIELD_REPLACED_BY, columnList = CprBitemporalRecord.DB_FIELD_REPLACED_BY + DatabaseEntry.REF)
})
public class BirthPlaceDataRecord extends CprBitemporalPersonRecord<BirthPlaceDataRecord> {

    public static final String TABLE_NAME = "cpr_person_birthplace_record";

    public BirthPlaceDataRecord() {
    }

    public BirthPlaceDataRecord(Integer birthPlaceCode, String birthPlaceName) {
        this.birthPlaceCode = birthPlaceCode;
        this.birthPlaceName = birthPlaceName;
    }

    @JsonIgnore
    public String getFieldName() {
        return TABLE_NAME;
    }

    // Myndighed tekst myndighed
    public static final String DB_FIELD_BIRTH_PLACE_CODE = "birthPlaceCode";
    public static final String IO_FIELD_BIRTH_PLACE_CODE = "cprFødselsregistreringsstedskode";
    @Column(name = DB_FIELD_BIRTH_PLACE_CODE)
    @JsonProperty(value = IO_FIELD_BIRTH_PLACE_CODE)
    private Integer birthPlaceCode;

    public Integer getBirthPlaceCode() {
        return this.birthPlaceCode;
    }

    public void setBirthPlaceCode(Integer birthPlaceCode) {
        this.birthPlaceCode = birthPlaceCode;
    }


    // Supplerende fødselsregistreringssted tekst
    public static final String DB_FIELD_BIRTH_PLACE_NAME = "birthPlaceName";
    public static final String IO_FIELD_BIRTH_PLACE_NAME = "cprFødselsregistreringsstedsnavn";
    @Column(name = DB_FIELD_BIRTH_PLACE_NAME)
    @JsonProperty(value = IO_FIELD_BIRTH_PLACE_NAME)
    private String birthPlaceName;

    public String getBirthPlaceName() {
        return this.birthPlaceName;
    }

    public void setBirthPlaceName(String birthPlaceName) {
        this.birthPlaceName = birthPlaceName;
    }


    @Override
    public boolean equalData(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equalData(o)) return false;
        BirthPlaceDataRecord that = (BirthPlaceDataRecord) o;
        return Objects.equals(birthPlaceCode, that.birthPlaceCode) &&
                Objects.equals(birthPlaceName, that.birthPlaceName);
    }

    @Override
    public boolean hasData() {
        return intNonZero(this.birthPlaceCode) || stringNonEmpty(this.birthPlaceName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), birthPlaceCode, birthPlaceName);
    }

    @Override
    public BirthPlaceDataRecord clone() {
        BirthPlaceDataRecord clone = new BirthPlaceDataRecord();
        clone.birthPlaceCode = this.birthPlaceCode;
        clone.birthPlaceName = this.birthPlaceName;
        CprBitemporalRecord.copy(this, clone);
        return clone;
    }
}
