package dk.magenta.datafordeler.cvr.records;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import dk.magenta.datafordeler.core.database.DatabaseEntry;
import dk.magenta.datafordeler.cvr.CvrPlugin;
import dk.magenta.datafordeler.cvr.records.unversioned.CompanyForm;
import jakarta.persistence.*;
import org.hibernate.Session;

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
public class FormRecord extends CvrBitemporalDataRecord implements Cloneable {

    public static final String TABLE_NAME = "cvr_record_form";

    public static final String DB_FIELD_COMPANY_METADATA = "companyMetadataRecord";

    @JsonIgnore
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
    private String companyFormCode;

    @JsonProperty(value = IO_FIELD_CODE)
    public String getCompanyFormCode() {
        if (this.companyForm != null) {
            return this.companyForm.getCompanyFormCode();
        }
        return this.companyFormCode;
    }

    @JsonProperty(value = IO_FIELD_CODE)
    public void setCompanyFormCode(String companyFormCode) {
        this.companyFormCode = companyFormCode;
    }

    public static final String IO_FIELD_SHORT_DESCRIPTION = "kortBeskrivelse";

    @Transient
    private String shortDescription;

    @JsonProperty(value = IO_FIELD_SHORT_DESCRIPTION)
    public String getShortDescription() {
        if (this.companyForm != null) {
            return this.companyForm.getShortDescription();
        }
        return this.shortDescription;
    }

    @JsonProperty(value = IO_FIELD_SHORT_DESCRIPTION)
    public void setShortDescription(String shortDescription) {
        this.shortDescription = shortDescription;
    }

    public static final String IO_FIELD_LONG_DESCRIPTION = "langBeskrivelse";

    @Transient
    private String longDescription;

    @JsonProperty(value = IO_FIELD_LONG_DESCRIPTION)
    public String getLongDescription() {
        if (this.companyForm != null) {
            return this.companyForm.getLongDescription();
        }
        return this.longDescription;
    }

    @JsonProperty(value = IO_FIELD_LONG_DESCRIPTION)
    public void setLongDescription(String longDescription) {
        this.longDescription = longDescription;
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

    public CompanyForm getCompanyForm() {
        return this.companyForm;
    }

    public void setCompanyForm(CompanyForm companyForm) {
        this.companyForm = companyForm;
    }

    public static final String DB_FIELD_PARTICIPANT_COMPANY_RELATION = "relationCompanyRecord";

    @ManyToOne(targetEntity = RelationCompanyRecord.class, fetch = FetchType.LAZY, cascade = CascadeType.ALL)
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
        return Objects.equals(this.getCompanyFormCode(), that.getCompanyFormCode()) &&
                Objects.equals(this.getShortDescription(), that.getShortDescription()) &&
                Objects.equals(this.getLongDescription(), that.getLongDescription()) &&
                Objects.equals(this.getResponsibleDatasource(), that.getResponsibleDatasource());
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), this.getCompanyFormCode(), this.getShortDescription(), this.getLongDescription(), this.getResponsibleDatasource());
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

    @Override
    protected Object clone() throws CloneNotSupportedException {
        FormRecord clone = (FormRecord) super.clone();
        clone.setCompanyForm(this.companyForm);
        clone.setCompanyMetadataRecord(this.companyMetadataRecord);
        clone.setRelationCompanyRecord(this.relationCompanyRecord);
        return clone;
    }
}
