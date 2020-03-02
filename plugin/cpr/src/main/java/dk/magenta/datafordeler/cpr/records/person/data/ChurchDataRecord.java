package dk.magenta.datafordeler.cpr.records.person.data;

import com.fasterxml.jackson.annotation.JsonProperty;
import dk.magenta.datafordeler.core.database.DatabaseEntry;
import dk.magenta.datafordeler.cpr.CprPlugin;
import dk.magenta.datafordeler.cpr.data.person.data.PersonBaseData;
import dk.magenta.datafordeler.cpr.records.CprBitemporalRecord;
import dk.magenta.datafordeler.cpr.records.person.CprBitemporalPersonRecord;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Index;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlElement;
import java.util.Objects;

/**
 * Storage for data on a Person's church relation,
 * referenced by {@link PersonBaseData}
 */
@Entity
@Table(name = CprPlugin.DEBUG_TABLE_PREFIX + ChurchDataRecord.TABLE_NAME, indexes = {
        @Index(name = CprPlugin.DEBUG_TABLE_PREFIX + ChurchDataRecord.TABLE_NAME + CprBitemporalPersonRecord.DB_FIELD_ENTITY, columnList = CprBitemporalPersonRecord.DB_FIELD_ENTITY + DatabaseEntry.REF),
        @Index(name = CprPlugin.DEBUG_TABLE_PREFIX + ChurchDataRecord.TABLE_NAME + CprBitemporalRecord.DB_FIELD_REGISTRATION_FROM, columnList = CprBitemporalRecord.DB_FIELD_REGISTRATION_FROM),
        @Index(name = CprPlugin.DEBUG_TABLE_PREFIX + ChurchDataRecord.TABLE_NAME + CprBitemporalRecord.DB_FIELD_REGISTRATION_TO, columnList = CprBitemporalRecord.DB_FIELD_REGISTRATION_TO),
        @Index(name = CprPlugin.DEBUG_TABLE_PREFIX + ChurchDataRecord.TABLE_NAME + CprBitemporalRecord.DB_FIELD_EFFECT_FROM, columnList = CprBitemporalRecord.DB_FIELD_EFFECT_FROM),
        @Index(name = CprPlugin.DEBUG_TABLE_PREFIX + ChurchDataRecord.TABLE_NAME + CprBitemporalRecord.DB_FIELD_EFFECT_TO, columnList = CprBitemporalRecord.DB_FIELD_EFFECT_TO),
})
public class ChurchDataRecord extends CprBitemporalPersonRecord {

    public static final String TABLE_NAME = "cpr_person_church_record";

    public ChurchDataRecord() {
    }

    public ChurchDataRecord(Character churchRelation) {
        this.churchRelation = churchRelation;
    }

    public static final String DB_FIELD_CHURCH_RELATION = "churchRelation";
    public static final String IO_FIELD_CHURCH_RELATION = "folkekirkeforhold";
    @Column(name = DB_FIELD_CHURCH_RELATION)
    @JsonProperty(value = IO_FIELD_CHURCH_RELATION)
    @XmlElement(name = IO_FIELD_CHURCH_RELATION)
    private Character churchRelation;

    public Character getChurchRelation() {
        return this.churchRelation;
    }

    public void setChurchRelation(Character churchRelation) {
        this.churchRelation = churchRelation;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), churchRelation);
    }

    @Override
    public boolean equalData(Object o) {
        if (o==null || (getClass() != o.getClass())) return false;
        ChurchDataRecord that = (ChurchDataRecord) o;
        return Objects.equals(this.churchRelation, that.churchRelation);
    }

    @Override
    protected ChurchDataRecord clone() {
        ChurchDataRecord clone = new ChurchDataRecord();
        clone.churchRelation = this.churchRelation;
        CprBitemporalRecord.copy(this, clone);
        return clone;
    }

}
