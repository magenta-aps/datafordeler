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
 * Storage for data on a Person's status code,
 * referenced by {@link dk.magenta.datafordeler.cpr.data.person.data.PersonBaseData}
 */
@Entity
@Table(name = CprPlugin.DEBUG_TABLE_PREFIX + PersonStatusDataRecord.TABLE_NAME, indexes = {
        @Index(name = CprPlugin.DEBUG_TABLE_PREFIX + PersonStatusDataRecord.TABLE_NAME + CprBitemporalPersonRecord.DB_FIELD_ENTITY, columnList = CprBitemporalPersonRecord.DB_FIELD_ENTITY + DatabaseEntry.REF),
        @Index(name = CprPlugin.DEBUG_TABLE_PREFIX + PersonStatusDataRecord.TABLE_NAME + PersonStatusDataRecord.DB_FIELD_STATUS, columnList = PersonStatusDataRecord.DB_FIELD_STATUS),
        @Index(name = CprPlugin.DEBUG_TABLE_PREFIX + PersonStatusDataRecord.TABLE_NAME + CprBitemporalRecord.DB_FIELD_REGISTRATION_FROM, columnList = CprBitemporalRecord.DB_FIELD_REGISTRATION_FROM),
        @Index(name = CprPlugin.DEBUG_TABLE_PREFIX + PersonStatusDataRecord.TABLE_NAME + CprBitemporalRecord.DB_FIELD_REGISTRATION_TO, columnList = CprBitemporalRecord.DB_FIELD_REGISTRATION_TO),
        @Index(name = CprPlugin.DEBUG_TABLE_PREFIX + PersonStatusDataRecord.TABLE_NAME + CprBitemporalRecord.DB_FIELD_EFFECT_FROM, columnList = CprBitemporalRecord.DB_FIELD_EFFECT_FROM),
        @Index(name = CprPlugin.DEBUG_TABLE_PREFIX + PersonStatusDataRecord.TABLE_NAME + CprBitemporalRecord.DB_FIELD_EFFECT_TO, columnList = CprBitemporalRecord.DB_FIELD_EFFECT_TO),
        @Index(name = CprPlugin.DEBUG_TABLE_PREFIX + PersonStatusDataRecord.TABLE_NAME + CprBitemporalRecord.DB_FIELD_REPLACED_BY, columnList = CprBitemporalRecord.DB_FIELD_REPLACED_BY + DatabaseEntry.REF)
})
public class PersonStatusDataRecord extends CprBitemporalPersonRecord<PersonStatusDataRecord> {

    public static final String TABLE_NAME = "cpr_person_status_record";

    public PersonStatusDataRecord() {
    }

    public PersonStatusDataRecord(int status) {
        this.status = status;
    }

    @JsonIgnore
    public String getFieldName() {
        return TABLE_NAME;
    }

    public static final String DB_FIELD_STATUS = "status";
    public static final String IO_FIELD_STATUS = "status";
    @Column(name = DB_FIELD_STATUS)
    @JsonProperty(value = IO_FIELD_STATUS)
    @XmlElement(name = IO_FIELD_STATUS)
    private int status;

    public int getStatus() {
        return this.status;
    }

    public void setStatus(int status) {
        this.status = status;
    }


    @OneToMany(fetch = FetchType.LAZY, mappedBy = DB_FIELD_CORRECTION_OF)
    private final Set<PersonStatusDataRecord> correctors = new HashSet<>();

    public Set<PersonStatusDataRecord> getCorrectors() {
        return this.correctors;
    }


    @Override
    public boolean equalData(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equalData(o)) return false;
        PersonStatusDataRecord that = (PersonStatusDataRecord) o;
        return status == that.status;
    }

    @Override
    public boolean hasData() {
        return this.status != 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), status);
    }

    @Override
    public PersonStatusDataRecord clone() {
        PersonStatusDataRecord clone = new PersonStatusDataRecord();
        clone.status = this.status;
        clone.line = "cloned from" + this.line;
        CprBitemporalRecord.copy(this, clone);
        return clone;
    }

    @Override
    public boolean updateBitemporalityByCloning() {
        return true;
    }
}
