package dk.magenta.datafordeler.cvr.records;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import dk.magenta.datafordeler.core.database.DatabaseEntry;

import javax.persistence.*;

/**
 * Record for Company and CompanyUnit lifecycle data.
 */
@Entity
@Table(name = LifecycleRecord.TABLE_NAME, indexes = {
        @Index(name = LifecycleRecord.TABLE_NAME + "__company", columnList = LifecycleRecord.DB_FIELD_COMPANY + DatabaseEntry.REF),
        @Index(name = LifecycleRecord.TABLE_NAME + "__companyunit", columnList = LifecycleRecord.DB_FIELD_COMPANYUNIT + DatabaseEntry.REF),
        @Index(name = LifecycleRecord.TABLE_NAME + "__participant", columnList = LifecycleRecord.DB_FIELD_PARTICIPANT + DatabaseEntry.REF),
        @Index(name = LifecycleRecord.TABLE_NAME + "__participant_company_relation", columnList = LifecycleRecord.DB_FIELD_PARTICIPANT_COMPANY_RELATION + DatabaseEntry.REF),
})
@JsonIgnoreProperties(ignoreUnknown = true)
public class LifecycleRecord extends CvrBitemporalDataRecord {

    public static final String TABLE_NAME = "cvr_record_lifecycle";



    public static final String DB_FIELD_PARTICIPANT_COMPANY_RELATION = "relationCompanyRecord";

    @ManyToOne(targetEntity = RelationCompanyRecord.class, fetch = FetchType.LAZY)
    @JoinColumn(name = DB_FIELD_PARTICIPANT_COMPANY_RELATION + DatabaseEntry.REF)
    @JsonIgnore
    private RelationCompanyRecord relationCompanyRecord;

    public void setRelationCompanyRecord(RelationCompanyRecord relationCompanyRecord) {
        this.relationCompanyRecord = relationCompanyRecord;
    }

}
