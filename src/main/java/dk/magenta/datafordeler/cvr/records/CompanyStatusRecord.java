package dk.magenta.datafordeler.cvr.records;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import dk.magenta.datafordeler.core.database.Bitemporal;
import dk.magenta.datafordeler.core.database.DatabaseEntry;

import javax.persistence.*;
import java.util.Objects;

/**
 * Record for Company status data.
 */
@Entity
@Table(name = CompanyStatusRecord.TABLE_NAME, indexes = {
        @Index(name = CompanyStatusRecord.TABLE_NAME + "__company", columnList = CompanyStatusRecord.DB_FIELD_COMPANY + DatabaseEntry.REF),
        @Index(name = CompanyStatusRecord.TABLE_NAME + "__unit", columnList = CompanyStatusRecord.DB_FIELD_COMPANYUNIT + DatabaseEntry.REF),
        @Index(name = CompanyStatusRecord.TABLE_NAME + "__participant_company_relation", columnList = CompanyStatusRecord.DB_FIELD_PARTICIPANT_COMPANY_RELATION + DatabaseEntry.REF),
})
@JsonIgnoreProperties(ignoreUnknown = true)
public class CompanyStatusRecord extends CvrBitemporalDataRecord implements Bitemporal {

    public static final String TABLE_NAME = "cvr_record_company_status";

    public static final String DB_FIELD_STATUS = "status";
    public static final String IO_FIELD_STATUS = "status";

    @Column(name = DB_FIELD_STATUS)
    @JsonProperty(value = IO_FIELD_STATUS)
    private String status;

    public String getStatus() {
        return this.status;
    }

    public void setStatus(String status) {
        this.status = status;
    }



    public static final String DB_FIELD_PARTICIPANT_COMPANY_RELATION = "relationCompanyRecord";

    @ManyToOne(targetEntity = RelationCompanyRecord.class, fetch = FetchType.LAZY)
    @JoinColumn(name = DB_FIELD_PARTICIPANT_COMPANY_RELATION + DatabaseEntry.REF)
    @JsonIgnore
    private RelationCompanyRecord relationCompanyRecord;

    public void setRelationCompanyRecord(RelationCompanyRecord relationCompanyRecord) {
        this.relationCompanyRecord = relationCompanyRecord;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        CompanyStatusRecord that = (CompanyStatusRecord) o;
        return Objects.equals(status, that.status);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), status);
    }

    /*
    public boolean equalData(Object o) {
        if (!super.equalData(o)) return false;
        CompanyStatusRecord that = (CompanyStatusRecord) o;
        return Objects.equals(status, that.status);
    }
    */
}
