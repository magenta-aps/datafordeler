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
@Table(name = CvrPlugin.DEBUG_TABLE_PREFIX + BaseNameRecord.TABLE_NAME, indexes = {
        @Index(name = CvrPlugin.DEBUG_TABLE_PREFIX + BaseNameRecord.TABLE_NAME + "__companymetadata", columnList = BaseNameRecord.DB_FIELD_COMPANY_METADATA + DatabaseEntry.REF),
        @Index(name = CvrPlugin.DEBUG_TABLE_PREFIX + BaseNameRecord.TABLE_NAME + "__unitmetadata", columnList = BaseNameRecord.DB_FIELD_UNIT_METADATA + DatabaseEntry.REF),
        @Index(name = CvrPlugin.DEBUG_TABLE_PREFIX + BaseNameRecord.TABLE_NAME + "__company_participant_relation", columnList = BaseNameRecord.DB_FIELD_PARTICIPANT_RELATION + DatabaseEntry.REF),
        @Index(name = CvrPlugin.DEBUG_TABLE_PREFIX + BaseNameRecord.TABLE_NAME + "__organization", columnList = BaseNameRecord.DB_FIELD_ORGANIZATION + DatabaseEntry.REF),
        @Index(name = CvrPlugin.DEBUG_TABLE_PREFIX + BaseNameRecord.TABLE_NAME + "__office", columnList = BaseNameRecord.DB_FIELD_OFFICE_UNIT + DatabaseEntry.REF),
        @Index(name = CvrPlugin.DEBUG_TABLE_PREFIX + BaseNameRecord.TABLE_NAME + "__fusion", columnList = BaseNameRecord.DB_FIELD_FUSION + DatabaseEntry.REF),
        @Index(name = CvrPlugin.DEBUG_TABLE_PREFIX + BaseNameRecord.TABLE_NAME + "__participant_company_relation", columnList = BaseNameRecord.DB_FIELD_COMPANY_RELATION + DatabaseEntry.REF),
        @Index(name = CvrPlugin.DEBUG_TABLE_PREFIX + BaseNameRecord.TABLE_NAME + "__data", columnList = BaseNameRecord.DB_FIELD_NAME),

        @Index(name = CvrPlugin.DEBUG_TABLE_PREFIX + BaseNameRecord.TABLE_NAME + "__" + CvrBitemporalRecord.DB_FIELD_LAST_UPDATED, columnList = CvrBitemporalRecord.DB_FIELD_LAST_UPDATED),
        @Index(name = CvrPlugin.DEBUG_TABLE_PREFIX + BaseNameRecord.TABLE_NAME + "__" + CvrRecordPeriod.DB_FIELD_VALID_FROM, columnList = CvrRecordPeriod.DB_FIELD_VALID_FROM),
        @Index(name = CvrPlugin.DEBUG_TABLE_PREFIX + BaseNameRecord.TABLE_NAME + "__" + CvrRecordPeriod.DB_FIELD_VALID_TO, columnList = CvrRecordPeriod.DB_FIELD_VALID_TO)
})
@JsonIgnoreProperties(ignoreUnknown = true)
public class BaseNameRecord extends CvrBitemporalMetaRecord {

    public static final String TABLE_NAME = "cvr_record_name1";

    public static final String DB_FIELD_NAME = "name";
    public static final String IO_FIELD_NAME = "navn";

    public String getFieldName() {
        return TABLE_NAME;
    }

    @Column(name = DB_FIELD_NAME, length = 900)
    @JsonProperty(value = IO_FIELD_NAME)
    private String name;

    public String getName() {
        return this.name;
    }
    
    public void setName(String name) {
        if (name != null && name.length() > 900) {
            name = name.substring(0, 900);
        }
        this.name = name;
    }



    public static final String DB_FIELD_ORGANIZATION = "organizationRecord";

    @JsonIgnore
    @ManyToOne(targetEntity = OrganizationRecord.class, fetch = FetchType.LAZY)
    @JoinColumn(name = DB_FIELD_ORGANIZATION + DatabaseEntry.REF)
    private OrganizationRecord organizationRecord;

    public void setOrganizationRecord(OrganizationRecord organizationRecord) {
        this.organizationRecord = organizationRecord;
    }



    public static final String DB_FIELD_OFFICE_UNIT = "officeUnitRecord";

    @JsonIgnore
    @ManyToOne(targetEntity = OfficeRelationUnitRecord.class, fetch = FetchType.LAZY)
    @JoinColumn(name = DB_FIELD_OFFICE_UNIT + DatabaseEntry.REF)
    private OfficeRelationUnitRecord officeUnitRecord;

    public void setOfficeUnitRecord(OfficeRelationUnitRecord officeUnitRecord) {
        this.officeUnitRecord = officeUnitRecord;
    }



    public static final String DB_FIELD_FUSION = "fusionSplitRecord";

    @JsonIgnore
    @ManyToOne(targetEntity = FusionSplitRecord.class, fetch = FetchType.LAZY)
    @JoinColumn(name = DB_FIELD_FUSION + DatabaseEntry.REF)
    private FusionSplitRecord fusionSplitRecord;

    public void setFusionSplitRecord(FusionSplitRecord fusionSplitRecord) {
        this.fusionSplitRecord = fusionSplitRecord;
    }



    public static final String DB_FIELD_PARTICIPANT_RELATION = "relationParticipantRecord";

    @ManyToOne(targetEntity = RelationParticipantRecord.class, fetch = FetchType.LAZY)
    @JoinColumn(name = DB_FIELD_PARTICIPANT_RELATION + DatabaseEntry.REF)
    @JsonIgnore
    private RelationParticipantRecord relationParticipantRecord;

    public void setRelationParticipantRecord(RelationParticipantRecord relationParticipantRecord) {
        this.relationParticipantRecord = relationParticipantRecord;
    }



    public static final String DB_FIELD_COMPANY_RELATION = "relationCompanyRecord";

    @JsonIgnore
    @ManyToOne(targetEntity = RelationCompanyRecord.class, fetch = FetchType.LAZY)
    @JoinColumn(name = DB_FIELD_COMPANY_RELATION + DatabaseEntry.REF)
    private RelationCompanyRecord relationCompanyRecord;

    public void setRelationCompanyRecord(RelationCompanyRecord relationCompanyRecord) {
        this.relationCompanyRecord = relationCompanyRecord;
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
