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
@Table(name = CprPlugin.DEBUG_TABLE_PREFIX + "cpr_person_children_record", indexes = {
        @Index(name = CprPlugin.DEBUG_TABLE_PREFIX + "cpr_person_children_record" + PersonEventDataRecord.DB_FIELD_ENTITY, columnList = PersonEventDataRecord.DB_FIELD_ENTITY + DatabaseEntry.REF)
})
public class ChildrenDataRecord extends CprBitemporalPersonRecord<ChildrenDataRecord> {

    public static final String TABLE_NAME = "cpr_person_children_record";

    public ChildrenDataRecord() {
    }

    public ChildrenDataRecord(String cprNumber, int status) {
        this.cprNumber = cprNumber;
        this.status = status;
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


    public static final String DB_FIELD_IS_MOTHER = "status";
    @Column(name = DB_FIELD_IS_MOTHER)
    @JsonIgnore
    @XmlTransient
    private int status;

    @JsonIgnore
    @XmlTransient
    public int getStatus() {
        return this.status;
    }

    public void setStatus(int status) {
        status = status;
    }


    @Override
    public boolean equalData(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equalData(o)) return false;
        ChildrenDataRecord that = (ChildrenDataRecord) o;
        return Objects.equals(cprNumber, that.cprNumber) && Objects.equals(status, that.status);
    }

    @Override
    public boolean hasData() {
        return this.cprNumber != null || this.status != 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), cprNumber, status);
    }

    @Override
    public ChildrenDataRecord clone() {
        ChildrenDataRecord clone = new ChildrenDataRecord();
        clone.cprNumber = this.cprNumber;
        clone.status = this.status;
        CprBitemporalRecord.copy(this, clone);
        return clone;
    }
}
