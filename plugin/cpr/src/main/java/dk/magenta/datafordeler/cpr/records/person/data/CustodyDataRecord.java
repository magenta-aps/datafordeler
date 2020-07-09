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
import java.time.LocalDate;
import java.util.Objects;

/**
 * Storage for data on a Person's custody status,
 */
@Entity
@Table(name = CprPlugin.DEBUG_TABLE_PREFIX + CustodyDataRecord.TABLE_NAME, indexes = {
        @Index(name = CprPlugin.DEBUG_TABLE_PREFIX + CustodyDataRecord.TABLE_NAME + CprBitemporalPersonRecord.DB_FIELD_ENTITY, columnList = CprBitemporalPersonRecord.DB_FIELD_ENTITY + DatabaseEntry.REF),
        @Index(name = CprPlugin.DEBUG_TABLE_PREFIX + CustodyDataRecord.TABLE_NAME + CprBitemporalRecord.DB_FIELD_REGISTRATION_FROM, columnList = CprBitemporalRecord.DB_FIELD_REGISTRATION_FROM),
        @Index(name = CprPlugin.DEBUG_TABLE_PREFIX + CustodyDataRecord.TABLE_NAME + CprBitemporalRecord.DB_FIELD_REGISTRATION_TO, columnList = CprBitemporalRecord.DB_FIELD_REGISTRATION_TO),
        @Index(name = CprPlugin.DEBUG_TABLE_PREFIX + CustodyDataRecord.TABLE_NAME + CprBitemporalRecord.DB_FIELD_EFFECT_FROM, columnList = CprBitemporalRecord.DB_FIELD_EFFECT_FROM),
        @Index(name = CprPlugin.DEBUG_TABLE_PREFIX + CustodyDataRecord.TABLE_NAME + CprBitemporalRecord.DB_FIELD_EFFECT_TO, columnList = CprBitemporalRecord.DB_FIELD_EFFECT_TO),
})
public class CustodyDataRecord extends CprBitemporalPersonRecord<CustodyDataRecord> {

    public static final String TABLE_NAME = "cpr_person_custody_record";

    public CustodyDataRecord() {
    }

    public CustodyDataRecord(int relationType, int startAuthoritycodeCustody, int relationAuthority, String relationPnr, LocalDate relationPnrStart) {
        this.relationType = relationType;
        this.startAuthoritycodeCustody = startAuthoritycodeCustody;
        this.relationAuthority = relationAuthority;
        this.relationPnr = relationPnr;
        this.relationPnrStart = relationPnrStart;
    }


    public static final String DB_FIELD_RELATION_TYPE = "relationType";
    public static final String IO_FIELD_RELATION_TYPE = "relationsType";
    @Column(name = DB_FIELD_RELATION_TYPE)
    @JsonProperty(value = IO_FIELD_RELATION_TYPE)
    @XmlElement(name = IO_FIELD_RELATION_TYPE)
    private int relationType;

    public int getRelationType() {
        return this.relationType;
    }

    public void setRelationType(int relationType) {
        this.relationType = relationType;
    }



    public static final String DB_FIELD_AUTHORITY_CODE_START = "startAuthoritycodeCustody";
    public static final String IO_FIELD_AUTHORITY_CODE_START = "startAuthoritycodeCustody";
    @Column(name = DB_FIELD_AUTHORITY_CODE_START)
    @JsonProperty(value = IO_FIELD_AUTHORITY_CODE_START)
    @XmlElement(name = IO_FIELD_AUTHORITY_CODE_START)
    private int startAuthoritycodeCustody;

    public int getStartAuthoritycodeCustody() {
        return this.startAuthoritycodeCustody;
    }

    public void setStartAuthoritycodeCustody(int startAuthoritycodeCustody) {
        this.startAuthoritycodeCustody = startAuthoritycodeCustody;
    }

    public static final String DB_FIELD_RELATION_AUTHORITY = "relationAuthority";
    public static final String IO_FIELD_RELATION_AUTHORITY = "relationsMyndighed";
    @Column(name = DB_FIELD_RELATION_AUTHORITY)
    @JsonProperty(value = IO_FIELD_RELATION_AUTHORITY)
    @XmlElement(name = IO_FIELD_RELATION_AUTHORITY)
    private int relationAuthority;

    public int getRelationAuthority() {
        return this.relationAuthority;
    }

    public void setRelationAuthority(int relationAuthority) {
        this.relationAuthority = relationAuthority;
    }

    public static final String DB_FIELD_RELATION_PNR = "relationPnr";
    public static final String IO_FIELD_RELATION_PNR = "relationsPnr";
    @Column(name = DB_FIELD_RELATION_PNR)
    @JsonProperty(value = IO_FIELD_RELATION_PNR)
    @XmlElement(name = IO_FIELD_RELATION_PNR)
    private String relationPnr;


    public String getRelationPnr() {
        return this.relationPnr;
    }

    public void setRelationPnr(String relationPnr) {
        this.relationPnr = relationPnr;
    }

    public static final String DB_FIELD_RELATION_PNR_START = "relationPnrStart";
    public static final String IO_FIELD_RELATION_PNR_START = "relationsPnrStart";
    @Column(name = DB_FIELD_RELATION_PNR_START)
    @JsonProperty(value = IO_FIELD_RELATION_PNR_START)
    @XmlElement(name = IO_FIELD_RELATION_PNR_START)
    private LocalDate relationPnrStart;

    public LocalDate getRelationPnrStart() {
        return this.relationPnrStart;
    }

    public void setRelationPnrStart(LocalDate relationPnrStart) {
        this.relationPnrStart = relationPnrStart;
    }



    @Override
    public boolean equalData(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equalData(o)) return false;
        CustodyDataRecord that = (CustodyDataRecord) o;
        return Objects.equals(this.relationType, that.relationType) &&
                Objects.equals(this.startAuthoritycodeCustody, that.startAuthoritycodeCustody) &&
                Objects.equals(this.relationAuthority, that.relationAuthority) &&
                Objects.equals(this.relationPnr, that.relationPnr) &&
                Objects.equals(this.relationPnrStart, that.relationPnrStart);
    }

    @Override
    public boolean hasData() {
        return this.relationType != 0
                || this.startAuthoritycodeCustody != 0
                || this.relationAuthority != 0
                || this.relationPnrStart != null
                || stringNonEmpty(this.relationPnr);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), relationType, startAuthoritycodeCustody, relationAuthority, relationPnr, relationPnrStart);
    }

    @Override
    public CustodyDataRecord clone() {
        CustodyDataRecord clone = new CustodyDataRecord();
        clone.relationType = this.relationType;
        clone.startAuthoritycodeCustody = this.startAuthoritycodeCustody;
        clone.relationAuthority = this.relationAuthority;
        clone.relationPnr = this.relationPnr;
        clone.relationPnrStart = this.relationPnrStart;
        CprBitemporalRecord.copy(this, clone);
        return clone;
    }
}
