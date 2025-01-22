package dk.magenta.datafordeler.cvr.records;

import com.fasterxml.jackson.annotation.JsonIgnore;
import dk.magenta.datafordeler.core.database.DatabaseEntry;

import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MappedSuperclass;

@MappedSuperclass
public abstract class CvrBitemporalDataMetaRecord extends CvrBitemporalDataRecord {

    public void setMetadataRecord(MetadataRecord metadataRecord) {
        if (metadataRecord instanceof CompanyMetadataRecord) {
            this.setCompanyMetadataRecord((CompanyMetadataRecord) metadataRecord);
        }
        if (metadataRecord instanceof CompanyUnitMetadataRecord) {
            this.setUnitMetadataRecord((CompanyUnitMetadataRecord) metadataRecord);
        }
    }


    public static final String DB_FIELD_COMPANY_METADATA = "companyMetadataRecord";

    @ManyToOne(targetEntity = CompanyMetadataRecord.class, fetch = FetchType.LAZY)
    @JoinColumn(name = DB_FIELD_COMPANY_METADATA + DatabaseEntry.REF)
    @JsonIgnore
    private MetadataRecord companyMetadataRecord;

    public void setCompanyMetadataRecord(CompanyMetadataRecord companyMetadataRecord) {
        this.companyMetadataRecord = companyMetadataRecord;
    }

    public MetadataRecord getCompanyMetadataRecord() {
        return this.companyMetadataRecord;
    }

    public static final String DB_FIELD_UNIT_METADATA = "unitMetadataRecord";

    @ManyToOne(targetEntity = CompanyUnitMetadataRecord.class, fetch = FetchType.LAZY)
    @JoinColumn(name = DB_FIELD_UNIT_METADATA + DatabaseEntry.REF)
    @JsonIgnore
    private MetadataRecord unitMetadataRecord;

    public void setUnitMetadataRecord(CompanyUnitMetadataRecord unitMetadataRecord) {
        this.unitMetadataRecord = unitMetadataRecord;
    }

    public MetadataRecord getUnitMetadataRecord() {
        return this.unitMetadataRecord;
    }

    public static final String DB_FIELD_PARTICIPANT_METADATA = "participantMetadataRecord";

    @ManyToOne(targetEntity = ParticipantMetadataRecord.class, fetch = FetchType.LAZY)
    @JoinColumn(name = DB_FIELD_PARTICIPANT_METADATA + DatabaseEntry.REF)
    @JsonIgnore
    private ParticipantMetadataRecord participantMetadataRecord;

    public void setParticipantMetadataRecord(ParticipantMetadataRecord participantMetadataRecord) {
        this.participantMetadataRecord = participantMetadataRecord;
    }

    public ParticipantMetadataRecord getParticipantMetadataRecord() {
        return this.participantMetadataRecord;
    }
}
