package dk.magenta.datafordeler.cpr.records.person.data;

import com.fasterxml.jackson.annotation.JsonIgnore;
import dk.magenta.datafordeler.core.database.DatabaseEntry;
import dk.magenta.datafordeler.cpr.CprPlugin;
import dk.magenta.datafordeler.cpr.records.CprBitemporalRecord;
import dk.magenta.datafordeler.cpr.records.person.CprBitemporalPersonRecord;

import jakarta.persistence.*;
import javax.xml.bind.annotation.XmlTransient;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Storage for data on a Person's parentage verification,
 * referenced by {@link dk.magenta.datafordeler.cpr.data.person.data.PersonBaseData}
 */
@Entity
@Table(name = CprPlugin.DEBUG_TABLE_PREFIX + ParentVerificationDataRecord.TABLE_NAME, indexes = {
        @Index(name = CprPlugin.DEBUG_TABLE_PREFIX + ParentVerificationDataRecord.TABLE_NAME + CprBitemporalPersonRecord.DB_FIELD_ENTITY, columnList = CprBitemporalPersonRecord.DB_FIELD_ENTITY + DatabaseEntry.REF),
        @Index(name = CprPlugin.DEBUG_TABLE_PREFIX + ParentVerificationDataRecord.TABLE_NAME + CprBitemporalRecord.DB_FIELD_REGISTRATION_FROM, columnList = CprBitemporalRecord.DB_FIELD_REGISTRATION_FROM),
        @Index(name = CprPlugin.DEBUG_TABLE_PREFIX + ParentVerificationDataRecord.TABLE_NAME + CprBitemporalRecord.DB_FIELD_REGISTRATION_TO, columnList = CprBitemporalRecord.DB_FIELD_REGISTRATION_TO),
        @Index(name = CprPlugin.DEBUG_TABLE_PREFIX + ParentVerificationDataRecord.TABLE_NAME + CprBitemporalRecord.DB_FIELD_EFFECT_FROM, columnList = CprBitemporalRecord.DB_FIELD_EFFECT_FROM),
        @Index(name = CprPlugin.DEBUG_TABLE_PREFIX + ParentVerificationDataRecord.TABLE_NAME + CprBitemporalRecord.DB_FIELD_EFFECT_TO, columnList = CprBitemporalRecord.DB_FIELD_EFFECT_TO),
        @Index(name = CprPlugin.DEBUG_TABLE_PREFIX + ParentVerificationDataRecord.TABLE_NAME + CprBitemporalRecord.DB_FIELD_REPLACED_BY, columnList = CprBitemporalRecord.DB_FIELD_REPLACED_BY + DatabaseEntry.REF)
})
public class ParentVerificationDataRecord extends VerificationDataRecord<ParentVerificationDataRecord> {

    public static final String TABLE_NAME = "cpr_person_parent_verification_record";

    public ParentVerificationDataRecord() {
    }

    public ParentVerificationDataRecord(boolean verified, boolean isMother) {
        super(verified);
        this.isMother = isMother;
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


    @OneToMany(fetch = FetchType.LAZY, mappedBy = DB_FIELD_CORRECTION_OF)
    private final Set<ParentVerificationDataRecord> correctors = new HashSet<>();

    public Set<ParentVerificationDataRecord> getCorrectors() {
        return this.correctors;
    }


    @Override
    public boolean equalData(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equalData(o)) return false;
        ParentVerificationDataRecord that = (ParentVerificationDataRecord) o;
        return isMother == that.isMother;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), isMother);
    }

    @Override
    public ParentVerificationDataRecord clone() {
        ParentVerificationDataRecord clone = new ParentVerificationDataRecord();
        clone.isMother = this.isMother;
        VerificationDataRecord.copy(this, clone);
        return clone;
    }
}
