package dk.magenta.datafordeler.cvr.records;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import dk.magenta.datafordeler.core.database.Bitemporal;
import dk.magenta.datafordeler.core.database.DatabaseEntry;
import dk.magenta.datafordeler.cvr.CvrPlugin;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Index;
import javax.persistence.Table;
import java.util.Objects;

/**
 * Record for Company and CompanyUnit industry.
 */
@Entity
@Table(name = CvrPlugin.DEBUG_TABLE_PREFIX + CompanyIndustryRecord.TABLE_NAME, indexes = {
        @Index(name = CvrPlugin.DEBUG_TABLE_PREFIX + CompanyIndustryRecord.TABLE_NAME + "__company", columnList = CompanyIndustryRecord.DB_FIELD_COMPANY + DatabaseEntry.REF + "," + CompanyIndustryRecord.DB_FIELD_INDEX),
        @Index(name = CvrPlugin.DEBUG_TABLE_PREFIX + CompanyIndustryRecord.TABLE_NAME + "__companyunit", columnList = CompanyIndustryRecord.DB_FIELD_COMPANYUNIT + DatabaseEntry.REF + "," + CompanyIndustryRecord.DB_FIELD_INDEX),
        @Index(name = CvrPlugin.DEBUG_TABLE_PREFIX + CompanyIndustryRecord.TABLE_NAME + "__companymetadata", columnList = CompanyIndustryRecord.DB_FIELD_COMPANY_METADATA + DatabaseEntry.REF),
        @Index(name = CvrPlugin.DEBUG_TABLE_PREFIX + CompanyIndustryRecord.TABLE_NAME + "__unitmetadata", columnList = CompanyIndustryRecord.DB_FIELD_UNIT_METADATA + DatabaseEntry.REF),
        @Index(name = CvrPlugin.DEBUG_TABLE_PREFIX + CompanyIndustryRecord.TABLE_NAME + "__code", columnList = CompanyIndustryRecord.DB_FIELD_CODE),

        @Index(name = CvrPlugin.DEBUG_TABLE_PREFIX + CompanyIndustryRecord.TABLE_NAME + "__" + CvrBitemporalRecord.DB_FIELD_LAST_UPDATED, columnList = CvrBitemporalRecord.DB_FIELD_LAST_UPDATED),
        @Index(name = CvrPlugin.DEBUG_TABLE_PREFIX + CompanyIndustryRecord.TABLE_NAME + "__" + CvrRecordPeriod.DB_FIELD_VALID_FROM, columnList = CvrRecordPeriod.DB_FIELD_VALID_FROM),
        @Index(name = CvrPlugin.DEBUG_TABLE_PREFIX + CompanyIndustryRecord.TABLE_NAME + "__" + CvrRecordPeriod.DB_FIELD_VALID_TO, columnList = CvrRecordPeriod.DB_FIELD_VALID_TO)
})
@JsonIgnoreProperties(ignoreUnknown = true)
public class CompanyIndustryRecord extends CvrBitemporalDataMetaRecord {

    public static final String TABLE_NAME = "cvr_record_industry";

    public static final String DB_FIELD_INDEX = "industryIndex";

    @Column(name = DB_FIELD_INDEX)
    @JsonIgnore
    private int index;

    public void setIndex(int index) {
        this.index = index;
    }



    public static final String DB_FIELD_CODE = "industryCode";
    public static final String IO_FIELD_CODE = "branchekode";

    @Column(name = DB_FIELD_CODE)
    @JsonProperty(value = IO_FIELD_CODE)
    private String industryCode;

    public String getIndustryCode() {
        return this.industryCode;
    }



    public static final String DB_FIELD_TEXT = "industryText";
    public static final String IO_FIELD_TEXT = "branchetekst";

    @Column(name = DB_FIELD_TEXT)
    @JsonProperty(value = IO_FIELD_TEXT)
    private String industryText;

    public String getIndustryText() {
        return this.industryText;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        CompanyIndustryRecord that = (CompanyIndustryRecord) o;
        return index == that.index &&
                Objects.equals(industryCode, that.industryCode) &&
                Objects.equals(industryText, that.industryText);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), index, industryCode, industryText);
    }

    /*@Override
    public boolean equalData(Object o) {
        if (!super.equalData(o)) return false;
        CompanyIndustryRecord that = (CompanyIndustryRecord) o;
        return index == that.index &&
                Objects.equals(industryCode, that.industryCode) &&
                Objects.equals(industryText, that.industryText);
    }*/
}
