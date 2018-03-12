package dk.magenta.datafordeler.cvr.records;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import dk.magenta.datafordeler.core.database.DatabaseEntry;

import javax.persistence.*;
import java.util.Objects;

/**
 * Record for Company, CompanyUnit or Participant name.
 */
@Entity
@Table(name = CompanyRegNumberRecord.TABLE_NAME, indexes = {
        @Index(name = CompanyRegNumberRecord.TABLE_NAME + "__company", columnList = CompanyRegNumberRecord.DB_FIELD_COMPANY + DatabaseEntry.REF),
        @Index(name = CompanyRegNumberRecord.TABLE_NAME + "__participant_company_relation", columnList = CompanyRegNumberRecord.DB_FIELD_PARTICIPANT_COMPANY_RELATION + DatabaseEntry.REF),
        @Index(name = CompanyRegNumberRecord.TABLE_NAME + "__data", columnList = CompanyRegNumberRecord.DB_FIELD_REGNUMBER),
})
public class CompanyRegNumberRecord extends CvrBitemporalDataRecord {

    public static final String TABLE_NAME = "cvr_record_company_regnumber";

    public static final String DB_FIELD_REGNUMBER = "regNumber";
    public static final String IO_FIELD_REGNUMBER = "regNummer";

    @Column(name = DB_FIELD_REGNUMBER)
    @JsonProperty(value = IO_FIELD_REGNUMBER)
    private String regNumber;

    public String getRegNumber() {
        return this.regNumber;
    }



    public static final String DB_FIELD_PARTICIPANT_COMPANY_RELATION = "regNumber";
    public static final String IO_FIELD_PARTICIPANT_COMPANY_RELATION = "regNummer";

    @ManyToOne(targetEntity = RelationCompanyRecord.class)
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
}
