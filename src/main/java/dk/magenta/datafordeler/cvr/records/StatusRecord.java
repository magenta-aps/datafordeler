package dk.magenta.datafordeler.cvr.records;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import dk.magenta.datafordeler.core.database.DatabaseEntry;
import dk.magenta.datafordeler.cvr.data.company.CompanyBaseData;
import dk.magenta.datafordeler.cvr.data.unversioned.CompanyStatus;
import org.hibernate.Session;

import javax.persistence.*;
import java.util.Objects;

/**
 * Record for Company statusText data.
 */
@Entity
@Table(name = StatusRecord.TABLE_NAME, indexes = {
        @Index(name = StatusRecord.TABLE_NAME + "__company", columnList = StatusRecord.DB_FIELD_COMPANY + DatabaseEntry.REF),
        @Index(name = StatusRecord.TABLE_NAME + "__unit", columnList = StatusRecord.DB_FIELD_COMPANYUNIT + DatabaseEntry.REF),
        @Index(name = StatusRecord.TABLE_NAME + "__relation", columnList = StatusRecord.DB_FIELD_PARTICIPANT_COMPANY_RELATION + DatabaseEntry.REF),
})
public class StatusRecord extends CvrBitemporalDataRecord {

    public static final String TABLE_NAME = "cvr_record_status";

    public static final String DB_FIELD_STATUSTEXT = "statusText";
    public static final String IO_FIELD_STATUSTEXT = "statustekst";

    @Column(name = DB_FIELD_STATUSTEXT)
    @JsonProperty(value = IO_FIELD_STATUSTEXT)
    private String statusText;

    public String getStatusText() {
        return this.statusText;
    }

    public void setStatusText(String statusText) {
        this.statusText = statusText;
    }



    public static final String DB_FIELD_STATUSCODE = "statusCode";
    public static final String IO_FIELD_STATUSCODE = "statuskode";

    @Column(name = DB_FIELD_STATUSCODE)
    @JsonProperty(value = IO_FIELD_STATUSCODE)
    private Integer statusCode;

    public Integer getStatusCode() {
        return this.statusCode;
    }

    public void setStatusCode(Integer statusCode) {
        this.statusCode = statusCode;
    }



    public static final String DB_FIELD_CREDITDATATEXT = "creditDataText";
    public static final String IO_FIELD_CREDITDATATEXT = "kreditoplysningtekst";

    @Column(name = DB_FIELD_CREDITDATATEXT)
    @JsonProperty(value = IO_FIELD_CREDITDATATEXT)
    private String creditDataText;

    public String getCreditDataText() {
        return this.creditDataText;
    }

    public void setCreditDataText(String creditDataText) {
        this.creditDataText = creditDataText;
    }



    public static final String DB_FIELD_CREDITDATACODE = "creditDataCode";
    public static final String IO_FIELD_CREDITDATACODE = "kreditoplysningkode";

    @Column(name = DB_FIELD_CREDITDATACODE)
    @JsonProperty(value = IO_FIELD_CREDITDATACODE)
    private Integer creditDataCode;

    public Integer getCreditDataCode() {
        return this.creditDataCode;
    }

    public void setCreditDataCode(Integer creditDataCode) {
        this.creditDataCode = creditDataCode;
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
    public void populateBaseData(CompanyBaseData baseData, Session session) {
        baseData.setStatus(CompanyStatus.getStatus(this.statusText, session));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        StatusRecord that = (StatusRecord) o;
        return Objects.equals(statusCode, that.statusCode) &&
                Objects.equals(creditDataCode, that.creditDataCode) &&
                Objects.equals(statusText, that.statusText) &&
                Objects.equals(creditDataText, that.creditDataText);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), statusText, statusCode, creditDataText, creditDataCode);
    }
}
