package dk.magenta.datafordeler.cpr.records.person.data;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import dk.magenta.datafordeler.core.database.DatabaseEntry;
import dk.magenta.datafordeler.cpr.CprPlugin;
import dk.magenta.datafordeler.cpr.records.CprBitemporalRecord;
import dk.magenta.datafordeler.cpr.records.person.CprBitemporalPersonRecord;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import javax.persistence.*;
import javax.xml.bind.annotation.XmlElement;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;


@Entity
@Table(name = CprPlugin.DEBUG_TABLE_PREFIX + ProtectionDataRecord.TABLE_NAME, indexes = {
        @Index(name = CprPlugin.DEBUG_TABLE_PREFIX + ProtectionDataRecord.TABLE_NAME + CprBitemporalPersonRecord.DB_FIELD_ENTITY, columnList = CprBitemporalPersonRecord.DB_FIELD_ENTITY + DatabaseEntry.REF),
        @Index(name = CprPlugin.DEBUG_TABLE_PREFIX + ProtectionDataRecord.TABLE_NAME + CprBitemporalRecord.DB_FIELD_REGISTRATION_FROM, columnList = CprBitemporalRecord.DB_FIELD_REGISTRATION_FROM),
        @Index(name = CprPlugin.DEBUG_TABLE_PREFIX + ProtectionDataRecord.TABLE_NAME + CprBitemporalRecord.DB_FIELD_REGISTRATION_TO, columnList = CprBitemporalRecord.DB_FIELD_REGISTRATION_TO),
        @Index(name = CprPlugin.DEBUG_TABLE_PREFIX + ProtectionDataRecord.TABLE_NAME + CprBitemporalRecord.DB_FIELD_EFFECT_FROM, columnList = CprBitemporalRecord.DB_FIELD_EFFECT_FROM),
        @Index(name = CprPlugin.DEBUG_TABLE_PREFIX + ProtectionDataRecord.TABLE_NAME + CprBitemporalRecord.DB_FIELD_EFFECT_TO, columnList = CprBitemporalRecord.DB_FIELD_EFFECT_TO),
        @Index(name = CprPlugin.DEBUG_TABLE_PREFIX + ProtectionDataRecord.TABLE_NAME + CprBitemporalRecord.DB_FIELD_REPLACED_BY, columnList = CprBitemporalRecord.DB_FIELD_REPLACED_BY + DatabaseEntry.REF)
})
public class ProtectionDataRecord extends CprBitemporalPersonRecord<ProtectionDataRecord> {

    public static final String TABLE_NAME = "cpr_person_protection_record";

    public ProtectionDataRecord() {
    }

    public ProtectionDataRecord(int protectionType, boolean reportMarking, LocalDate deletionDate) {
        this.protectionType = protectionType;
        this.reportMarking = reportMarking;
        this.deletionDate = deletionDate;
    }

    @JsonIgnore
    public String getFieldName() {
        return TABLE_NAME;
    }

    /*
    0001 = Navne- og adressebeskyttelse
    0002 = Lokalvejviserbeskyttelse
    0003 = Markedsføringsbeskyttelse
    * */

    public static final String DB_FIELD_TYPE = "protectionType";
    public static final String IO_FIELD_TYPE = "beskyttelsestype";
    @Column(name = DB_FIELD_TYPE)
    @JsonProperty(value = IO_FIELD_TYPE)
    @XmlElement(name = IO_FIELD_TYPE)
    private int protectionType;

    public int getProtectionType() {
        return protectionType;
    }

    public void setProtectionType(int protectionType) {
        this.protectionType = protectionType;
    }


    public static final String DB_FIELD_REPORTMARKING = "reportMarking";
    public static final String IO_FIELD_REPORTMARKING = "rapportMarkering";
    @Column(name = DB_FIELD_REPORTMARKING)
    @JsonProperty(value = IO_FIELD_REPORTMARKING)
    @XmlElement(name = IO_FIELD_REPORTMARKING)
    private boolean reportMarking;

    public boolean getReportMarking() {
        return reportMarking;
    }

    public void setReportMarking(boolean reportMarking) {
        this.reportMarking = reportMarking;
    }


    public static final String DB_FIELD_DELETION_DATE = "deletionDate";
    public static final String IO_FIELD_DELETION_DATE = "sletteDato";
    @Column(name = DB_FIELD_DELETION_DATE)
    @JsonProperty(value = IO_FIELD_DELETION_DATE)
    @XmlElement(name = IO_FIELD_DELETION_DATE)
    private LocalDate deletionDate;

    public LocalDate getDeletionDate() {
        return this.deletionDate;
    }

    public void setDeletionDate(LocalDate deletionDate) {
        this.deletionDate = deletionDate;
    }


    public static final String DB_FIELD_END_AUTHORITY = "endAuthority";
    public static final String IO_FIELD_END_AUTHORITY = "slutmyndighed";
    @Column(name = DB_FIELD_END_AUTHORITY)
    @JsonProperty(value = IO_FIELD_END_AUTHORITY)
    @XmlElement(name = IO_FIELD_END_AUTHORITY)
    private int endAuthority;

    public int getEndAuthority() {
        return this.endAuthority;
    }

    public ProtectionDataRecord setEndAuthority(int endAuthority) {
        if (endAuthority != 0 || this.endAuthority == 0) {
            this.endAuthority = endAuthority;
        }
        return this;
    }

    public ProtectionDataRecord setAuthority(int authority) {
        super.setAuthority(authority);
        return this;
    }


    @OneToMany(fetch = FetchType.LAZY, mappedBy = DB_FIELD_CORRECTION_OF)
    @OnDelete(action = OnDeleteAction.NO_ACTION)
    private final Set<ProtectionDataRecord> correctors = new HashSet<>();

    public Set<ProtectionDataRecord> getCorrectors() {
        return this.correctors;
    }


    @Override
    public boolean equalData(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equalData(o)) return false;
        ProtectionDataRecord that = (ProtectionDataRecord) o;
        return protectionType == that.protectionType;
    }

    @Override
    public boolean hasData() {
        return this.protectionType != 0 || this.reportMarking || this.deletionDate != null;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), protectionType, reportMarking, deletionDate);
    }

    @Override
    public ProtectionDataRecord clone() {
        ProtectionDataRecord clone = new ProtectionDataRecord();
        clone.protectionType = this.protectionType;
        clone.reportMarking = this.reportMarking;
        CprBitemporalRecord.copy(this, clone);
        return clone;
    }
}
