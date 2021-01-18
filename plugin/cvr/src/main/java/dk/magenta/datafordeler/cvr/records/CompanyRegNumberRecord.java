package dk.magenta.datafordeler.cvr.records;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import dk.magenta.datafordeler.core.database.DatabaseEntry;
import dk.magenta.datafordeler.cvr.CvrPlugin;

import javax.persistence.*;
import java.util.Objects;

/**
 * Record for Company, CompanyUnit or Participant name.
 */
@Entity
@Table(name = CvrPlugin.DEBUG_TABLE_PREFIX + CompanyRegNumberRecord.TABLE_NAME, indexes = {
        @Index(name = CvrPlugin.DEBUG_TABLE_PREFIX + CompanyRegNumberRecord.TABLE_NAME + "__company", columnList = CompanyRegNumberRecord.DB_FIELD_COMPANY + DatabaseEntry.REF),
        @Index(name = CvrPlugin.DEBUG_TABLE_PREFIX + CompanyRegNumberRecord.TABLE_NAME + "__participant_company_relation", columnList = CompanyRegNumberRecord.DB_FIELD_PARTICIPANT_COMPANY_RELATION + DatabaseEntry.REF),
        @Index(name = CvrPlugin.DEBUG_TABLE_PREFIX + CompanyRegNumberRecord.TABLE_NAME + "__data", columnList = CompanyRegNumberRecord.DB_FIELD_REGNUMBER),

        @Index(name = CvrPlugin.DEBUG_TABLE_PREFIX + CompanyRegNumberRecord.TABLE_NAME + "__" + CvrBitemporalRecord.DB_FIELD_LAST_UPDATED, columnList = CvrBitemporalRecord.DB_FIELD_LAST_UPDATED),
        @Index(name = CvrPlugin.DEBUG_TABLE_PREFIX + CompanyRegNumberRecord.TABLE_NAME + "__" + CvrRecordPeriod.DB_FIELD_VALID_FROM, columnList = CvrRecordPeriod.DB_FIELD_VALID_FROM),
        @Index(name = CvrPlugin.DEBUG_TABLE_PREFIX + CompanyRegNumberRecord.TABLE_NAME + "__" + CvrRecordPeriod.DB_FIELD_VALID_TO, columnList = CvrRecordPeriod.DB_FIELD_VALID_TO)
})
@JsonIgnoreProperties(ignoreUnknown = true)
public class CompanyRegNumberRecord extends CvrBitemporalDataRecord {

    public static final String TABLE_NAME = "cvr_record_company_regnumber";

    public static final String DB_FIELD_REGNUMBER = "regNumber";
    public static final String IO_FIELD_REGNUMBER = "regNummer";

    @JsonIgnore
    public String getFieldName() {
        return TABLE_NAME;
    }

    @Column(name = DB_FIELD_REGNUMBER)
    @JsonProperty(value = IO_FIELD_REGNUMBER)
    private String regNumber;

    public String getRegNumber() {
        return this.regNumber;
    }



    public static final String DB_FIELD_PARTICIPANT_COMPANY_RELATION = "participantRelationCompanyRecord";

    @ManyToOne(targetEntity = RelationCompanyRecord.class, fetch = FetchType.LAZY)
    @JoinColumn(name = DB_FIELD_PARTICIPANT_COMPANY_RELATION + DatabaseEntry.REF)
    @JsonIgnore
    private RelationCompanyRecord participantRelationCompanyRecord;

    public void setParticipantRelationCompanyRecord(RelationCompanyRecord participantRelationCompanyRecord) {
        this.participantRelationCompanyRecord = participantRelationCompanyRecord;
    }



    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        CompanyRegNumberRecord that = (CompanyRegNumberRecord) o;
        return Objects.equals(regNumber, that.regNumber);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), regNumber);
    }

    /*@Override
    public boolean equalData(Object o) {
        if (!super.equalData(o)) return false;
        CompanyRegNumberRecord that = (CompanyRegNumberRecord) o;
        return Objects.equals(regNumber, that.regNumber);
    }*/
}
