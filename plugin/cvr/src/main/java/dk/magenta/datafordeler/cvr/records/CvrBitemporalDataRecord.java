package dk.magenta.datafordeler.cvr.records;

import com.fasterxml.jackson.annotation.JsonIgnore;
import dk.magenta.datafordeler.core.database.DatabaseEntry;

import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MappedSuperclass;

@MappedSuperclass
public abstract class CvrBitemporalDataRecord extends CvrBitemporalRecord {


    public static final String DB_FIELD_COMPANY = "companyRecord";

    @JsonIgnore
    @ManyToOne(targetEntity = CompanyRecord.class, fetch = FetchType.LAZY)
    @JoinColumn(name = DB_FIELD_COMPANY + DatabaseEntry.REF)
    private CompanyRecord companyRecord;

    public void setCompanyRecord(CompanyRecord companyRecord) {
        this.companyRecord = companyRecord;
    }

    public CompanyRecord getCompanyRecord() {
        return this.companyRecord;
    }


    public static final String DB_FIELD_COMPANYUNIT = "companyUnitRecord";

    @JsonIgnore
    @ManyToOne(targetEntity = CompanyUnitRecord.class, fetch = FetchType.LAZY)
    @JoinColumn(name = DB_FIELD_COMPANYUNIT + DatabaseEntry.REF)
    private CompanyUnitRecord companyUnitRecord;

    public void setCompanyUnitRecord(CompanyUnitRecord companyUnitRecord) {
        this.companyUnitRecord = companyUnitRecord;
    }

    public CompanyUnitRecord getCompanyUnitRecord() {
        return this.companyUnitRecord;
    }

    public static final String DB_FIELD_PARTICIPANT = "participantRecord";

    @JsonIgnore
    @ManyToOne(targetEntity = ParticipantRecord.class, fetch = FetchType.LAZY)
    @JoinColumn(name = DB_FIELD_PARTICIPANT + DatabaseEntry.REF)
    private ParticipantRecord participantRecord;

    public void setParticipantRecord(ParticipantRecord participantRecord) {
        this.participantRecord = participantRecord;
    }

    public ParticipantRecord getParticipantRecord() {
        return this.participantRecord;
    }

}
