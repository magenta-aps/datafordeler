package dk.magenta.datafordeler.cvr.records;

import com.fasterxml.jackson.annotation.JsonIgnore;
import dk.magenta.datafordeler.core.database.DatabaseEntry;

import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MappedSuperclass;

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


    public static final String DB_FIELD_UNIT_METADATA = "unitMetadataRecord";

    @ManyToOne(targetEntity = CompanyUnitMetadataRecord.class, fetch = FetchType.LAZY)
    @JoinColumn(name = DB_FIELD_UNIT_METADATA + DatabaseEntry.REF)
    @JsonIgnore
    private MetadataRecord unitMetadataRecord;

    public void setUnitMetadataRecord(CompanyUnitMetadataRecord unitMetadataRecord) {
        this.unitMetadataRecord = unitMetadataRecord;
    }


    public static final String DB_FIELD_PARTICIPANT_METADATA = "participantMetadataRecord";

    @ManyToOne(targetEntity = ParticipantMetadataRecord.class, fetch = FetchType.LAZY)
    @JoinColumn(name = DB_FIELD_PARTICIPANT_METADATA + DatabaseEntry.REF)
    @JsonIgnore
    private ParticipantMetadataRecord participantMetadataRecord;

    public void setParticipantMetadataRecord(ParticipantMetadataRecord participantMetadataRecord) {
        this.participantMetadataRecord = participantMetadataRecord;
    }

}
