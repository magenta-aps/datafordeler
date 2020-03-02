package dk.magenta.datafordeler.cpr.records.person.data;

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

/**
 * Storage for data on a Person's historic cpr number,
 * referenced by {@link dk.magenta.datafordeler.cpr.data.person.data.PersonBaseData}
 */
@Entity
@Table(name = CprPlugin.DEBUG_TABLE_PREFIX + PersonNumberDataRecord.TABLE_NAME, indexes = {
        @Index(name = CprPlugin.DEBUG_TABLE_PREFIX + PersonNumberDataRecord.TABLE_NAME + CprBitemporalPersonRecord.DB_FIELD_ENTITY, columnList = CprBitemporalPersonRecord.DB_FIELD_ENTITY + DatabaseEntry.REF),
        @Index(name = CprPlugin.DEBUG_TABLE_PREFIX + PersonNumberDataRecord.TABLE_NAME + CprBitemporalRecord.DB_FIELD_REGISTRATION_FROM, columnList = CprBitemporalRecord.DB_FIELD_REGISTRATION_FROM),
        @Index(name = CprPlugin.DEBUG_TABLE_PREFIX + PersonNumberDataRecord.TABLE_NAME + CprBitemporalRecord.DB_FIELD_REGISTRATION_TO, columnList = CprBitemporalRecord.DB_FIELD_REGISTRATION_TO),
        @Index(name = CprPlugin.DEBUG_TABLE_PREFIX + PersonNumberDataRecord.TABLE_NAME + CprBitemporalRecord.DB_FIELD_EFFECT_FROM, columnList = CprBitemporalRecord.DB_FIELD_EFFECT_FROM),
        @Index(name = CprPlugin.DEBUG_TABLE_PREFIX + PersonNumberDataRecord.TABLE_NAME + CprBitemporalRecord.DB_FIELD_EFFECT_TO, columnList = CprBitemporalRecord.DB_FIELD_EFFECT_TO),
})
public class PersonNumberDataRecord extends CprBitemporalPersonRecord {

    public static final String TABLE_NAME = "cpr_person_number_record";

    public PersonNumberDataRecord() {
    }

    public PersonNumberDataRecord(String cprNumber) {
        this.cprNumber = cprNumber;
    }

    public static final String DB_FIELD_CPR_NUMBER = "cprNumber";
    public static final String IO_FIELD_CPR_NUMBER = "cprNummer";
    @Column(name = DB_FIELD_CPR_NUMBER, length = 10)
    @JsonProperty(value = IO_FIELD_CPR_NUMBER)
    @XmlElement(name = IO_FIELD_CPR_NUMBER)
    private String cprNumber;

    public String getCprNumber() {
        return this.cprNumber;
    }

    public void setCprNumber(String cprNumber) {
        this.cprNumber = cprNumber;
    }

    @Override
    public boolean equalData(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equalData(o)) return false;
        PersonNumberDataRecord that = (PersonNumberDataRecord) o;
        return Objects.equals(cprNumber, that.cprNumber);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), cprNumber);
    }

    @Override
    protected PersonNumberDataRecord clone() {
        PersonNumberDataRecord clone = new PersonNumberDataRecord();
        clone.cprNumber = this.cprNumber;
        CprBitemporalRecord.copy(this, clone);
        return clone;
    }
}
