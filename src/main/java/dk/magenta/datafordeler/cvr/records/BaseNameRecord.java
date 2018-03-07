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
@Table(name = BaseNameRecord.TABLE_NAME, indexes = {
        @Index(name = BaseNameRecord.TABLE_NAME + "__companymetadata", columnList = BaseNameRecord.DB_FIELD_COMPANY_METADATA + DatabaseEntry.REF),
        @Index(name = BaseNameRecord.TABLE_NAME + "__unitmetadata", columnList = BaseNameRecord.DB_FIELD_UNIT_METADATA + DatabaseEntry.REF),
        @Index(name = BaseNameRecord.TABLE_NAME + "__company_participant_relation", columnList = BaseNameRecord.DB_FIELD_PARTICIPANT_RELATION + DatabaseEntry.REF),
        @Index(name = BaseNameRecord.TABLE_NAME + "__organization", columnList = BaseNameRecord.DB_FIELD_ORGANIZATION + DatabaseEntry.REF),
        @Index(name = BaseNameRecord.TABLE_NAME + "__office", columnList = BaseNameRecord.DB_FIELD_OFFICE_UNIT + DatabaseEntry.REF),
        @Index(name = BaseNameRecord.TABLE_NAME + "__fusion", columnList = BaseNameRecord.DB_FIELD_FUSION + DatabaseEntry.REF),
        @Index(name = BaseNameRecord.TABLE_NAME + "__participant_company_relation", columnList = BaseNameRecord.DB_FIELD_PARTICIPANT_COMPANY_RELATION + DatabaseEntry.REF),
        @Index(name = BaseNameRecord.TABLE_NAME + "__data", columnList = BaseNameRecord.DB_FIELD_NAME),
})
public class BaseNameRecord extends CvrBitemporalMetaRecord {

    public static final String TABLE_NAME = "cvr_record_name1";

    public static final String DB_FIELD_NAME = "name";
    public static final String IO_FIELD_NAME = "navn";

    @Column(name = DB_FIELD_NAME)
    @JsonProperty(value = IO_FIELD_NAME)
    private String name;

    public String getName() {
        return this.name;
    }



    public static final String DB_FIELD_PARTICIPANT_RELATION = "companyParticipantRelationRecord";

    @ManyToOne(targetEntity = ParticipantRelationRecord.class)
    @JoinColumn(name = DB_FIELD_PARTICIPANT_RELATION + DatabaseEntry.REF)
    @JsonIgnore
    private ParticipantRelationRecord companyParticipantRelationRecord;

    public void setCompanyParticipantRelationRecord(ParticipantRelationRecord companyParticipantRelationRecord) {
        this.companyParticipantRelationRecord = companyParticipantRelationRecord;
    }



    public static final String DB_FIELD_ORGANIZATION = "organizationRecord";

    @JsonIgnore
    @ManyToOne(targetEntity = OrganizationRecord.class)
    @JoinColumn(name = DB_FIELD_ORGANIZATION + DatabaseEntry.REF)
    private OrganizationRecord organizationRecord;

    public void setOrganizationRecord(OrganizationRecord organizationRecord) {
        this.organizationRecord = organizationRecord;
    }



    public static final String DB_FIELD_OFFICE_UNIT = "officeUnitRecord";

    @JsonIgnore
    @ManyToOne(targetEntity = OfficeRelationUnitRecord.class)
    @JoinColumn(name = DB_FIELD_OFFICE_UNIT + DatabaseEntry.REF)
    private OfficeRelationUnitRecord officeUnitRecord;

    public void setOfficeUnitRecord(OfficeRelationUnitRecord officeUnitRecord) {
        this.officeUnitRecord = officeUnitRecord;
    }



    public static final String DB_FIELD_FUSION = "fusionSplitRecord";

    @JsonIgnore
    @ManyToOne(targetEntity = FusionSplitRecord.class)
    @JoinColumn(name = DB_FIELD_FUSION + DatabaseEntry.REF)
    private FusionSplitRecord fusionSplitRecord;

    public void setFusionSplitRecord(FusionSplitRecord fusionSplitRecord) {
        this.fusionSplitRecord = fusionSplitRecord;
    }



    public static final String DB_FIELD_PARTICIPANT_COMPANY_RELATION = "participantCompanyRelationRecord";

    @JsonIgnore
    @ManyToOne(targetEntity = CompanyRelationRecord.class)
    @JoinColumn(name = DB_FIELD_PARTICIPANT_COMPANY_RELATION + DatabaseEntry.REF)
    private CompanyRelationRecord participantCompanyRelationRecord;

    public void setParticipantCompanyRelationRecord(CompanyRelationRecord participantCompanyRelationRecord) {
        this.participantCompanyRelationRecord = participantCompanyRelationRecord;
    }



    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        BaseNameRecord that = (BaseNameRecord) o;
        return Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {

        return Objects.hash(super.hashCode(), name);
    }
}
