package dk.magenta.datafordeler.cvr.records;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import dk.magenta.datafordeler.core.database.DatabaseEntry;
import dk.magenta.datafordeler.cvr.CvrPlugin;
import dk.magenta.datafordeler.cvr.records.unversioned.CompanyForm;
import org.hibernate.Session;

import javax.persistence.*;
import java.util.Objects;

/**
 * Record for Company form.
 */
@Entity
@Table(name = CvrPlugin.DEBUG_TABLE_PREFIX + FormRecord.TABLE_NAME, indexes = {
        @Index(name = CvrPlugin.DEBUG_TABLE_PREFIX + FormRecord.TABLE_NAME + "__company", columnList = FormRecord.DB_FIELD_COMPANY + DatabaseEntry.REF),
        @Index(name = CvrPlugin.DEBUG_TABLE_PREFIX + FormRecord.TABLE_NAME + "__unit", columnList = FormRecord.DB_FIELD_COMPANYUNIT + DatabaseEntry.REF),
        @Index(name = CvrPlugin.DEBUG_TABLE_PREFIX + FormRecord.TABLE_NAME + "__companymeta", columnList = FormRecord.DB_FIELD_COMPANY_METADATA + DatabaseEntry.REF),
        @Index(name = CvrPlugin.DEBUG_TABLE_PREFIX + FormRecord.TABLE_NAME + "__participant_company_relation", columnList = FormRecord.DB_FIELD_PARTICIPANT_COMPANY_RELATION + DatabaseEntry.REF),

        @Index(name = CvrPlugin.DEBUG_TABLE_PREFIX + FormRecord.TABLE_NAME + "__" + CvrBitemporalRecord.DB_FIELD_LAST_UPDATED, columnList = CvrBitemporalRecord.DB_FIELD_LAST_UPDATED),
        @Index(name = CvrPlugin.DEBUG_TABLE_PREFIX + FormRecord.TABLE_NAME + "__" + CvrRecordPeriod.DB_FIELD_VALID_FROM, columnList = CvrRecordPeriod.DB_FIELD_VALID_FROM),
        @Index(name = CvrPlugin.DEBUG_TABLE_PREFIX + FormRecord.TABLE_NAME + "__" + CvrRecordPeriod.DB_FIELD_VALID_TO, columnList = CvrRecordPeriod.DB_FIELD_VALID_TO)
})
@JsonIgnoreProperties(ignoreUnknown = true)
public class FormRecord extends CvrBitemporalDataRecord {

    public static final String TABLE_NAME = "cvr_record_form";

    public static final String DB_FIELD_COMPANY_METADATA = "companyMetadataRecord";

    public String getFieldName() {
        return TABLE_NAME;
    }

    @ManyToOne(targetEntity = CompanyMetadataRecord.class, fetch = FetchType.LAZY)
    @JoinColumn(name = DB_FIELD_COMPANY_METADATA + DatabaseEntry.REF)
    @JsonIgnore
    private CompanyMetadataRecord companyMetadataRecord;

    public void setCompanyMetadataRecord(CompanyMetadataRecord companyMetadataRecord) {
        this.companyMetadataRecord = companyMetadataRecord;
    }



    public static final String IO_FIELD_CODE = "virksomhedsformkode";

    @Transient
    @JsonProperty(value = IO_FIELD_CODE)
    private String companyFormCode;

    public String getCompanyFormCode() {
        if (this.companyForm != null) {
            return this.companyForm.getCompanyFormCode();
        }
        return this.companyFormCode;
    }

    public static final String IO_FIELD_SHORT_DESCRIPTION = "kortBeskrivelse";

    @Transient
    @JsonProperty(value = IO_FIELD_SHORT_DESCRIPTION)
    private String shortDescription;

    public String getShortDescription() {
        if (this.companyForm != null) {
            return this.companyForm.getShortDescription();
        }
        return this.shortDescription;
    }

    public static final String IO_FIELD_LONG_DESCRIPTION = "langBeskrivelse";

    @Transient
    @JsonProperty(value = IO_FIELD_LONG_DESCRIPTION)
    private String longDescription;

    public String getLongDescription() {
        if (this.companyForm != null) {
            return this.companyForm.getLongDescription();
        }
        return this.longDescription;
    }

    public static final String IO_FIELD_RESPONSIBLE_DATASOURCE = "ansvarligDataleverandør";

    @Transient
    @JsonProperty(value = IO_FIELD_RESPONSIBLE_DATASOURCE)
    private String responsibleDatasource;

    public String getResponsibleDatasource() {
        if (this.companyForm != null) {
            return this.companyForm.getResponsibleDataSource();
        }
        return this.responsibleDatasource;
    }

    public static final String DB_FIELD_FORM = "companyForm";

    @ManyToOne(targetEntity = CompanyForm.class)
    @JsonIgnore
    private CompanyForm companyForm;



    public static final String DB_FIELD_PARTICIPANT_COMPANY_RELATION = "relationCompanyRecord";

    @ManyToOne(targetEntity = RelationCompanyRecord.class, fetch = FetchType.LAZY)
    @JoinColumn(name = DB_FIELD_PARTICIPANT_COMPANY_RELATION + DatabaseEntry.REF)
    @JsonIgnore
    private RelationCompanyRecord relationCompanyRecord;

    public void setRelationCompanyRecord(RelationCompanyRecord relationCompanyRecord) {
        this.relationCompanyRecord = relationCompanyRecord;
    }


    public void wire(Session session) {
        if (this.companyFormCode != null && (this.companyForm == null || !this.companyFormCode.equals(this.companyForm.getCompanyFormCode()))) {
            this.companyForm = CompanyForm.getForm(this.companyFormCode, this.shortDescription, this.longDescription, this.responsibleDatasource, session);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        FormRecord that = (FormRecord) o;
        return Objects.equals(companyFormCode, that.companyFormCode) &&
                Objects.equals(shortDescription, that.shortDescription) &&
                Objects.equals(longDescription, that.longDescription) &&
                Objects.equals(responsibleDatasource, that.responsibleDatasource) &&
                Objects.equals(companyForm, that.companyForm);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), companyFormCode, shortDescription, longDescription, responsibleDatasource, companyForm);
    }
/*
    @Override
    public boolean equalData(Object o) {
        if (!super.equalData(o)) return false;
        FormRecord that = (FormRecord) o;
        return Objects.equals(companyFormCode, that.companyFormCode) &&
                Objects.equals(shortDescription, that.shortDescription) &&
                Objects.equals(longDescription, that.longDescription) &&
                Objects.equals(responsibleDatasource, that.responsibleDatasource) &&
                Objects.equals(companyForm, that.companyForm);
    }*/
}
