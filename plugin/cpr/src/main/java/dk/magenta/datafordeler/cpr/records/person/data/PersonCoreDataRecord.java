package dk.magenta.datafordeler.cpr.records.person.data;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import dk.magenta.datafordeler.core.database.DatabaseEntry;
import dk.magenta.datafordeler.cpr.CprPlugin;
import dk.magenta.datafordeler.cpr.records.CprBitemporalRecord;
import dk.magenta.datafordeler.cpr.records.person.CprBitemporalPersonRecord;
import jakarta.persistence.*;

import javax.xml.bind.annotation.XmlElement;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Storage for data on a Person's core data (gender, current cpr number),
 * referenced by {@link dk.magenta.datafordeler.cpr.data.person.data.PersonBaseData}
 */
@Entity
@Table(name = CprPlugin.DEBUG_TABLE_PREFIX + PersonCoreDataRecord.TABLE_NAME, indexes = {
        @Index(name = CprPlugin.DEBUG_TABLE_PREFIX + PersonCoreDataRecord.TABLE_NAME + CprBitemporalPersonRecord.DB_FIELD_ENTITY, columnList = CprBitemporalPersonRecord.DB_FIELD_ENTITY + DatabaseEntry.REF),
        @Index(name = CprPlugin.DEBUG_TABLE_PREFIX + PersonCoreDataRecord.TABLE_NAME + CprBitemporalRecord.DB_FIELD_REGISTRATION_FROM, columnList = CprBitemporalRecord.DB_FIELD_REGISTRATION_FROM),
        @Index(name = CprPlugin.DEBUG_TABLE_PREFIX + PersonCoreDataRecord.TABLE_NAME + CprBitemporalRecord.DB_FIELD_REGISTRATION_TO, columnList = CprBitemporalRecord.DB_FIELD_REGISTRATION_TO),
        @Index(name = CprPlugin.DEBUG_TABLE_PREFIX + PersonCoreDataRecord.TABLE_NAME + CprBitemporalRecord.DB_FIELD_EFFECT_FROM, columnList = CprBitemporalRecord.DB_FIELD_EFFECT_FROM),
        @Index(name = CprPlugin.DEBUG_TABLE_PREFIX + PersonCoreDataRecord.TABLE_NAME + CprBitemporalRecord.DB_FIELD_EFFECT_TO, columnList = CprBitemporalRecord.DB_FIELD_EFFECT_TO),
        @Index(name = CprPlugin.DEBUG_TABLE_PREFIX + PersonCoreDataRecord.TABLE_NAME + CprBitemporalRecord.DB_FIELD_REPLACED_BY, columnList = CprBitemporalRecord.DB_FIELD_REPLACED_BY + DatabaseEntry.REF)
})
public class PersonCoreDataRecord extends CprBitemporalPersonRecord<PersonCoreDataRecord> {

    public static final String TABLE_NAME = "cpr_person_core_record";

    public enum Koen {
        MAND,
        KVINDE
    }

    public PersonCoreDataRecord() {
    }

    public PersonCoreDataRecord(Koen gender) {
        this.gender = gender;
    }

    public PersonCoreDataRecord(String gender) {
        this.setGender(gender);
    }

    @JsonIgnore
    public String getFieldName() {
        return TABLE_NAME;
    }

    public static final String DB_FIELD_GENDER = "gender";
    public static final String IO_FIELD_GENDER = "køn";
    @Column(name = DB_FIELD_GENDER)
    @JsonProperty(value = IO_FIELD_GENDER)
    @XmlElement(name = IO_FIELD_GENDER)
    private Koen gender;

    public Koen getGender() {
        return this.gender;
    }

    public void setGender(Koen gender) {
        this.gender = gender;
    }

    public void setGender(String gender) {
        if (gender != null) {
            if (gender.equalsIgnoreCase("M")) {
                this.setGender(Koen.MAND);
            } else if (gender.equalsIgnoreCase("K")) {
                this.setGender(Koen.KVINDE);
            }
        }
    }


    @OneToMany(fetch = FetchType.LAZY, mappedBy = DB_FIELD_CORRECTION_OF)
    private final Set<PersonCoreDataRecord> correctors = new HashSet<>();

    public Set<PersonCoreDataRecord> getCorrectors() {
        return this.correctors;
    }


    @Override
    public boolean equalData(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equalData(o)) return false;
        PersonCoreDataRecord that = (PersonCoreDataRecord) o;
        return gender == that.gender;
    }

    @Override
    public boolean hasData() {
        return true;
    }

    @Override
    public int hashCode() {

        return Objects.hash(super.hashCode(), gender);
    }

    @Override
    public PersonCoreDataRecord clone() {
        PersonCoreDataRecord clone = new PersonCoreDataRecord();
        clone.gender = this.gender;
        CprBitemporalRecord.copy(this, clone);
        return clone;
    }
}
