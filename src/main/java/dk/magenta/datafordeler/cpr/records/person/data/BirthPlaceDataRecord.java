package dk.magenta.datafordeler.cpr.records.person.data;

import com.fasterxml.jackson.annotation.JsonProperty;
import dk.magenta.datafordeler.cpr.CprPlugin;
import dk.magenta.datafordeler.cpr.records.CprBitemporalRecord;
import dk.magenta.datafordeler.cpr.records.person.CprBitemporalPersonRecord;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Index;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlElement;
import java.util.Objects;

/**
 * Storage for data on a Person's birth,
 */
@Entity
@Table(name = CprPlugin.DEBUG_TABLE_PREFIX + BirthPlaceDataRecord.TABLE_NAME, indexes = {
        @Index(name = CprPlugin.DEBUG_TABLE_PREFIX + BirthPlaceDataRecord.TABLE_NAME + BirthPlaceDataRecord.DB_FIELD_BIRTH_PLACE_CODE, columnList = BirthPlaceDataRecord.DB_FIELD_BIRTH_PLACE_CODE),
        @Index(name = CprPlugin.DEBUG_TABLE_PREFIX + BirthPlaceDataRecord.TABLE_NAME + CprBitemporalRecord.DB_FIELD_REGISTRATION_FROM, columnList = CprBitemporalRecord.DB_FIELD_REGISTRATION_FROM),
        @Index(name = CprPlugin.DEBUG_TABLE_PREFIX + BirthPlaceDataRecord.TABLE_NAME + CprBitemporalRecord.DB_FIELD_REGISTRATION_TO, columnList = CprBitemporalRecord.DB_FIELD_REGISTRATION_TO),
        @Index(name = CprPlugin.DEBUG_TABLE_PREFIX + BirthPlaceDataRecord.TABLE_NAME + CprBitemporalRecord.DB_FIELD_EFFECT_FROM, columnList = CprBitemporalRecord.DB_FIELD_EFFECT_FROM),
        @Index(name = CprPlugin.DEBUG_TABLE_PREFIX + BirthPlaceDataRecord.TABLE_NAME + CprBitemporalRecord.DB_FIELD_EFFECT_TO, columnList = CprBitemporalRecord.DB_FIELD_EFFECT_TO),
})
public class BirthPlaceDataRecord extends CprBitemporalPersonRecord {

    public static final String TABLE_NAME = "cpr_person_birthplace_record";

    public BirthPlaceDataRecord() {
    }

    public BirthPlaceDataRecord(Integer birthPlaceCode, String birthPlaceName) {
        this.birthPlaceCode = birthPlaceCode;
        this.birthPlaceName = birthPlaceName;
    }

    // Myndighed tekst myndighed
    public static final String DB_FIELD_BIRTH_PLACE_CODE = "birthPlaceCode";
    public static final String IO_FIELD_BIRTH_PLACE_CODE = "cprFødselsregistreringsstedskode";
    @Column(name = DB_FIELD_BIRTH_PLACE_CODE)
    @JsonProperty(value = IO_FIELD_BIRTH_PLACE_CODE)
    @XmlElement(name = IO_FIELD_BIRTH_PLACE_CODE)
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
    @XmlElement(name = IO_FIELD_BIRTH_PLACE_NAME)
    private String birthPlaceName;

    public String getBirthPlaceName() {
        return this.birthPlaceName;
    }

    public void setBirthPlaceName(String birthPlaceName) {
        this.birthPlaceName = birthPlaceName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        BirthPlaceDataRecord that = (BirthPlaceDataRecord) o;
        return Objects.equals(birthPlaceCode, that.birthPlaceCode) &&
                Objects.equals(birthPlaceName, that.birthPlaceName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), birthPlaceCode, birthPlaceName);
    }

    @Override
    protected BirthPlaceDataRecord clone() {
        BirthPlaceDataRecord clone = new BirthPlaceDataRecord();
        clone.birthPlaceCode = this.birthPlaceCode;
        clone.birthPlaceName = this.birthPlaceName;
        CprBitemporalRecord.copy(this, clone);
        return clone;
    }
}
