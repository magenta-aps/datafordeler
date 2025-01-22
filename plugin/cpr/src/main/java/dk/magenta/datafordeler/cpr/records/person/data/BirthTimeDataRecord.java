package dk.magenta.datafordeler.cpr.records.person.data;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import dk.magenta.datafordeler.core.database.DatabaseEntry;
import dk.magenta.datafordeler.cpr.CprPlugin;
import dk.magenta.datafordeler.cpr.records.CprBitemporalRecord;
import dk.magenta.datafordeler.cpr.records.person.CprBitemporalPersonRecord;

import jakarta.persistence.*;
import javax.xml.bind.annotation.XmlElement;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Storage for data on a Person's birth,
 * referenced by {@link dk.magenta.datafordeler.cpr.data.person.data.PersonBaseData}
 */
@Entity
@Table(name = CprPlugin.DEBUG_TABLE_PREFIX + BirthTimeDataRecord.TABLE_NAME, indexes = {
        @Index(name = CprPlugin.DEBUG_TABLE_PREFIX + BirthTimeDataRecord.TABLE_NAME + CprBitemporalPersonRecord.DB_FIELD_ENTITY, columnList = CprBitemporalPersonRecord.DB_FIELD_ENTITY + DatabaseEntry.REF),
        @Index(name = CprPlugin.DEBUG_TABLE_PREFIX + BirthTimeDataRecord.TABLE_NAME + BirthTimeDataRecord.DB_FIELD_BIRTH_DATETIME, columnList = BirthTimeDataRecord.DB_FIELD_BIRTH_DATETIME),
        @Index(name = CprPlugin.DEBUG_TABLE_PREFIX + BirthTimeDataRecord.TABLE_NAME + CprBitemporalRecord.DB_FIELD_REGISTRATION_FROM, columnList = CprBitemporalRecord.DB_FIELD_REGISTRATION_FROM),
        @Index(name = CprPlugin.DEBUG_TABLE_PREFIX + BirthTimeDataRecord.TABLE_NAME + CprBitemporalRecord.DB_FIELD_REGISTRATION_TO, columnList = CprBitemporalRecord.DB_FIELD_REGISTRATION_TO),
        @Index(name = CprPlugin.DEBUG_TABLE_PREFIX + BirthTimeDataRecord.TABLE_NAME + CprBitemporalRecord.DB_FIELD_EFFECT_FROM, columnList = CprBitemporalRecord.DB_FIELD_EFFECT_FROM),
        @Index(name = CprPlugin.DEBUG_TABLE_PREFIX + BirthTimeDataRecord.TABLE_NAME + CprBitemporalRecord.DB_FIELD_EFFECT_TO, columnList = CprBitemporalRecord.DB_FIELD_EFFECT_TO),
        @Index(name = CprPlugin.DEBUG_TABLE_PREFIX + BirthTimeDataRecord.TABLE_NAME + CprBitemporalRecord.DB_FIELD_REPLACED_BY, columnList = CprBitemporalRecord.DB_FIELD_REPLACED_BY + DatabaseEntry.REF)
})
public class BirthTimeDataRecord extends CprBitemporalPersonRecord<BirthTimeDataRecord> {

    public static final String TABLE_NAME = "cpr_person_birthtime_record";

    public BirthTimeDataRecord() {
    }

    public BirthTimeDataRecord(LocalDateTime birthDatetime, boolean birthDatetimeUncertain, int foedselsraekkefoelge) {
        this.birthDatetime = birthDatetime;
        this.birthDatetimeUncertain = birthDatetimeUncertain;
        this.foedselsraekkefoelge = foedselsraekkefoelge;
    }

    @JsonIgnore
    public String getFieldName() {
        return TABLE_NAME;
    }

    public static final String DB_FIELD_BIRTH_DATETIME = "birthDatetime";
    public static final String IO_FIELD_BIRTH_DATETIME = "fødselsdato";
    @Column(name = DB_FIELD_BIRTH_DATETIME)
    @JsonProperty(value = IO_FIELD_BIRTH_DATETIME)
    @XmlElement(name = IO_FIELD_BIRTH_DATETIME)
    private LocalDateTime birthDatetime;

    public LocalDateTime getBirthDatetime() {
        return this.birthDatetime;
    }

    public void setBirthDatetime(LocalDateTime birthDatetime) {
        this.birthDatetime = birthDatetime;
    }


    public static final String DB_FIELD_BIRTH_DATETIME_UNCERTAIN = "birthDatetimeUncertain";
    public static final String IO_FIELD_BIRTH_DATETIME_UNCERTAIN = "fødselsdatoUsikkerhedsmarkering";
    @Column(name = DB_FIELD_BIRTH_DATETIME_UNCERTAIN)
    @JsonProperty(value = IO_FIELD_BIRTH_DATETIME_UNCERTAIN)
    @XmlElement(name = IO_FIELD_BIRTH_DATETIME_UNCERTAIN)
    private boolean birthDatetimeUncertain;

    public boolean isBirthDatetimeUncertain() {
        return this.birthDatetimeUncertain;
    }

    public void setBirthDatetimeUncertain(boolean birthDatetimeUncertain) {
        this.birthDatetimeUncertain = birthDatetimeUncertain;
    }


    @Transient
    @JsonIgnore
    private int foedselsraekkefoelge;

    public int getFoedselsraekkefoelge() {
        return this.foedselsraekkefoelge;
    }

    public void setFoedselsraekkefoelge(int foedselsraekkefoelge) {
        this.foedselsraekkefoelge = foedselsraekkefoelge;
    }


    @OneToMany(fetch = FetchType.LAZY, mappedBy = DB_FIELD_CORRECTION_OF)
    private final Set<BirthTimeDataRecord> correctors = new HashSet<>();

    public Set<BirthTimeDataRecord> getCorrectors() {
        return this.correctors;
    }


    @Override
    public boolean equalData(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equalData(o)) return false;
        BirthTimeDataRecord that = (BirthTimeDataRecord) o;
        return birthDatetimeUncertain == that.birthDatetimeUncertain &&
                foedselsraekkefoelge == that.foedselsraekkefoelge &&
                Objects.equals(birthDatetime, that.birthDatetime);
    }

    @Override
    public boolean hasData() {
        return this.birthDatetimeUncertain || this.birthDatetime != null || this.foedselsraekkefoelge != 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), birthDatetime, birthDatetimeUncertain, foedselsraekkefoelge);
    }

    @Override
    public BirthTimeDataRecord clone() {
        BirthTimeDataRecord clone = new BirthTimeDataRecord();
        clone.birthDatetime = this.birthDatetime;
        clone.birthDatetimeUncertain = this.birthDatetimeUncertain;
        clone.foedselsraekkefoelge = this.foedselsraekkefoelge;
        CprBitemporalRecord.copy(this, clone);
        return clone;
    }
}
