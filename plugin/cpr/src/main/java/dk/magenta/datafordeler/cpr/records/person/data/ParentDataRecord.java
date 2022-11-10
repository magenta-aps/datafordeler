package dk.magenta.datafordeler.cpr.records.person.data;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import dk.magenta.datafordeler.core.database.DatabaseEntry;
import dk.magenta.datafordeler.cpr.CprPlugin;
import dk.magenta.datafordeler.cpr.records.CprBitemporalRecord;
import dk.magenta.datafordeler.cpr.records.person.CprBitemporalPersonRecord;

import javax.persistence.*;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Storage for data on a Person's parentage,
 * referenced by {@link dk.magenta.datafordeler.cpr.data.person.data.PersonBaseData}
 */
@Entity
@Table(name = CprPlugin.DEBUG_TABLE_PREFIX + ParentDataRecord.TABLE_NAME, indexes = {
        @Index(name = CprPlugin.DEBUG_TABLE_PREFIX + ParentDataRecord.TABLE_NAME + CprBitemporalPersonRecord.DB_FIELD_ENTITY, columnList = CprBitemporalPersonRecord.DB_FIELD_ENTITY + DatabaseEntry.REF),
        @Index(name = CprPlugin.DEBUG_TABLE_PREFIX + ParentDataRecord.TABLE_NAME + CprBitemporalRecord.DB_FIELD_REGISTRATION_FROM, columnList = CprBitemporalRecord.DB_FIELD_REGISTRATION_FROM),
        @Index(name = CprPlugin.DEBUG_TABLE_PREFIX + ParentDataRecord.TABLE_NAME + CprBitemporalRecord.DB_FIELD_REGISTRATION_TO, columnList = CprBitemporalRecord.DB_FIELD_REGISTRATION_TO),
        @Index(name = CprPlugin.DEBUG_TABLE_PREFIX + ParentDataRecord.TABLE_NAME + CprBitemporalRecord.DB_FIELD_EFFECT_FROM, columnList = CprBitemporalRecord.DB_FIELD_EFFECT_FROM),
        @Index(name = CprPlugin.DEBUG_TABLE_PREFIX + ParentDataRecord.TABLE_NAME + CprBitemporalRecord.DB_FIELD_EFFECT_TO, columnList = CprBitemporalRecord.DB_FIELD_EFFECT_TO),
        @Index(name = CprPlugin.DEBUG_TABLE_PREFIX + ParentDataRecord.TABLE_NAME + CprBitemporalRecord.DB_FIELD_REPLACED_BY, columnList = CprBitemporalRecord.DB_FIELD_REPLACED_BY + DatabaseEntry.REF)
})
public class ParentDataRecord extends CprBitemporalPersonRecord<ParentDataRecord> {

    public static final String TABLE_NAME = "cpr_person_parent_record";

    public ParentDataRecord() {
    }

    public ParentDataRecord(boolean isMother, String cprNumber, LocalDate birthDate, boolean birthDateUncertain, String name, boolean nameMarking) {
        this.isMother = isMother;
        this.cprNumber = cprNumber;
        this.birthDate = birthDate;
        this.birthDateUncertain = birthDateUncertain;
        this.name = name;
        this.nameMarking = nameMarking;
    }

    @JsonIgnore
    public String getFieldName() {
        return TABLE_NAME;
    }

    public static final String DB_FIELD_IS_MOTHER = "isMother";
    @Column(name = DB_FIELD_IS_MOTHER)
    @JsonIgnore
    @XmlTransient
    private boolean isMother;

    @JsonIgnore
    @XmlTransient
    public boolean isMother() {
        return this.isMother;
    }

    public void setMother(boolean mother) {
        isMother = mother;
    }


    public static final String DB_FIELD_CPR_NUMBER = "cprNumber";
    public static final String IO_FIELD_CPR_NUMBER = "personnummer";
    @Column(name = DB_FIELD_CPR_NUMBER)
    @JsonProperty(value = IO_FIELD_CPR_NUMBER)
    @XmlElement(name = IO_FIELD_CPR_NUMBER)
    private String cprNumber;

    public String getCprNumber() {
        return this.cprNumber;
    }

    public void setCprNumber(String cprNumber) {
        this.cprNumber = cprNumber;
    }


    public static final String DB_FIELD_BIRTHDATE = "birthDate";
    public static final String IO_FIELD_BIRTHDATE = "foedselsdato";
    @Column(name = DB_FIELD_BIRTHDATE)
    @JsonProperty(value = IO_FIELD_BIRTHDATE)
    @XmlElement(name = IO_FIELD_BIRTHDATE)
    private LocalDate birthDate;

    public LocalDate getBirthDate() {
        return this.birthDate;
    }

    public void setBirthDate(LocalDate birthDate) {
        this.birthDate = birthDate;
    }


    public static final String DB_FIELD_BIRTHDATE_UNCERTAIN = "birthDateUncertain";
    public static final String IO_FIELD_BIRTHDATE_UNCERTAIN = "foedselsdatoUsikker";
    @Column(name = DB_FIELD_BIRTHDATE_UNCERTAIN)
    @JsonProperty(value = IO_FIELD_BIRTHDATE_UNCERTAIN)
    @XmlElement(name = IO_FIELD_BIRTHDATE_UNCERTAIN)
    private boolean birthDateUncertain;

    public boolean isBirthDateUncertain() {
        return this.birthDateUncertain;
    }

    public void setBirthDateUncertain(boolean birthDateUncertain) {
        this.birthDateUncertain = birthDateUncertain;
    }


    public static final String DB_FIELD_NAME = "name";
    public static final String IO_FIELD_NAME = "navn";
    @Column(name = DB_FIELD_NAME)
    @JsonProperty(value = IO_FIELD_NAME)
    @XmlElement(name = IO_FIELD_NAME)
    private String name;

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }


    public static final String DB_FIELD_NAME_MARKING = "nameMarking";
    public static final String IO_FIELD_NAME_MARKING = "navneMarkering";
    @Column(name = DB_FIELD_NAME_MARKING)
    @JsonProperty(value = IO_FIELD_NAME_MARKING)
    @XmlElement(name = IO_FIELD_NAME_MARKING)
    private boolean nameMarking;

    public boolean hasNameMarking() {
        return this.nameMarking;
    }

    public void setNameMarking(boolean nameMarking) {
        this.nameMarking = nameMarking;
    }


    @OneToMany(fetch = FetchType.LAZY, mappedBy = DB_FIELD_CORRECTION_OF)
    private final Set<ParentDataRecord> correctors = new HashSet<>();

    public Set<ParentDataRecord> getCorrectors() {
        return this.correctors;
    }


    @Override
    public boolean equalData(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equalData(o)) return false;
        ParentDataRecord that = (ParentDataRecord) o;
        return isMother == that.isMother &&
                birthDateUncertain == that.birthDateUncertain &&
                nameMarking == that.nameMarking &&
                Objects.equals(cprNumber, that.cprNumber) &&
                Objects.equals(birthDate, that.birthDate) &&
                Objects.equals(name, that.name);
    }

    @Override
    public boolean hasData() {
        return this.birthDateUncertain || this.nameMarking || stringNonEmpty(this.cprNumber) || this.birthDate != null || stringNonEmpty(this.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), isMother, cprNumber, birthDate, birthDateUncertain, name, nameMarking);
    }

    @Override
    public ParentDataRecord clone() {
        ParentDataRecord clone = new ParentDataRecord();
        clone.cprNumber = this.cprNumber;
        clone.birthDate = this.birthDate;
        clone.birthDateUncertain = this.birthDateUncertain;
        clone.isMother = this.isMother;
        clone.name = this.name;
        clone.nameMarking = this.nameMarking;
        CprBitemporalRecord.copy(this, clone);
        return clone;
    }
}
