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
import java.util.*;

@Entity
@Table(name = CprPlugin.DEBUG_TABLE_PREFIX + ChildrenDataRecord.TABLE_NAME, indexes = {
        @Index(name = CprPlugin.DEBUG_TABLE_PREFIX + ChildrenDataRecord.TABLE_NAME + CprBitemporalPersonRecord.DB_FIELD_ENTITY, columnList = CprBitemporalPersonRecord.DB_FIELD_ENTITY + DatabaseEntry.REF),
        @Index(name = CprPlugin.DEBUG_TABLE_PREFIX + ChildrenDataRecord.TABLE_NAME + CprBitemporalRecord.DB_FIELD_REGISTRATION_FROM, columnList = CprBitemporalRecord.DB_FIELD_REGISTRATION_FROM),
        @Index(name = CprPlugin.DEBUG_TABLE_PREFIX + ChildrenDataRecord.TABLE_NAME + CprBitemporalRecord.DB_FIELD_REGISTRATION_TO, columnList = CprBitemporalRecord.DB_FIELD_REGISTRATION_TO),
        @Index(name = CprPlugin.DEBUG_TABLE_PREFIX + ChildrenDataRecord.TABLE_NAME + CprBitemporalRecord.DB_FIELD_EFFECT_FROM, columnList = CprBitemporalRecord.DB_FIELD_EFFECT_FROM),
        @Index(name = CprPlugin.DEBUG_TABLE_PREFIX + ChildrenDataRecord.TABLE_NAME + CprBitemporalRecord.DB_FIELD_EFFECT_TO, columnList = CprBitemporalRecord.DB_FIELD_EFFECT_TO),
        @Index(name = CprPlugin.DEBUG_TABLE_PREFIX + ChildrenDataRecord.TABLE_NAME + CprBitemporalRecord.DB_FIELD_CORRECTION_OF, columnList = CprBitemporalRecord.DB_FIELD_CORRECTION_OF + DatabaseEntry.REF),
        @Index(name = CprPlugin.DEBUG_TABLE_PREFIX + ChildrenDataRecord.TABLE_NAME + CprBitemporalRecord.DB_FIELD_REPLACED_BY, columnList = CprBitemporalRecord.DB_FIELD_REPLACED_BY + DatabaseEntry.REF)
})
public class ChildrenDataRecord extends CprBitemporalPersonRecord<ChildrenDataRecord> {

    public static final String TABLE_NAME = "cpr_person_children_record";

    public ChildrenDataRecord() {
    }

    public ChildrenDataRecord(String childCprNumber, int status) {
        this.childCprNumber = childCprNumber;
        this.status = status;
    }


    public static final String DB_FIELD_CHILD_CPR_NUMBER = "childCprNumber";
    public static final String IO_FIELD_CHILD_CPR_NUMBER = "barnpersonnummer";
    @Column(name = DB_FIELD_CHILD_CPR_NUMBER)
    @JsonProperty(value = IO_FIELD_CHILD_CPR_NUMBER)
    @XmlElement(name = IO_FIELD_CHILD_CPR_NUMBER)
    private String childCprNumber;

    public String getChildCprNumber() {
        return this.childCprNumber;
    }

    public void setChildCprNumber(String childCprNumber) {
        this.childCprNumber = childCprNumber;
    }


    public static final String DB_FIELD_STATUS = "status";
    @Column(name = DB_FIELD_STATUS)
    @JsonIgnore
    @XmlTransient
    private int status;

    @JsonIgnore
    @XmlTransient
    public int getStatus() {
        return this.status;
    }

    public void setStatus(int status) {
        this.status = status;
    }


    @Override
    public boolean equalData(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equalData(o)) return false;
        ChildrenDataRecord that = (ChildrenDataRecord) o;
        return Objects.equals(childCprNumber, that.childCprNumber) && Objects.equals(status, that.status);
    }

    @Override
    public boolean hasData() {
        return this.childCprNumber != null || this.status != 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), childCprNumber, status);
    }

    @Override
    public ChildrenDataRecord clone() {
        ChildrenDataRecord clone = new ChildrenDataRecord();
        clone.childCprNumber = this.childCprNumber;
        clone.status = this.status;
        CprBitemporalRecord.copy(this, clone);
        return clone;
    }
}
