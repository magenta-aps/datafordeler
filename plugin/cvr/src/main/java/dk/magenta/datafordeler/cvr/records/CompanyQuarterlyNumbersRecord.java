package dk.magenta.datafordeler.cvr.records;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import dk.magenta.datafordeler.core.database.DatabaseEntry;
import dk.magenta.datafordeler.cvr.CvrPlugin;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

import java.util.Objects;

/**
 * Record for Company and CompanyUnit quarterly employee numbers.
 */
@Entity
@Table(name = CvrPlugin.DEBUG_TABLE_PREFIX + CompanyQuarterlyNumbersRecord.TABLE_NAME, indexes = {
        @Index(name = CvrPlugin.DEBUG_TABLE_PREFIX + CompanyQuarterlyNumbersRecord.TABLE_NAME + "__company", columnList = CompanyQuarterlyNumbersRecord.DB_FIELD_COMPANY + DatabaseEntry.REF),
        @Index(name = CvrPlugin.DEBUG_TABLE_PREFIX + CompanyQuarterlyNumbersRecord.TABLE_NAME + "__unit", columnList = CompanyQuarterlyNumbersRecord.DB_FIELD_COMPANYUNIT + DatabaseEntry.REF),
        @Index(name = CvrPlugin.DEBUG_TABLE_PREFIX + CompanyQuarterlyNumbersRecord.TABLE_NAME + "__year", columnList = CompanyQuarterlyNumbersRecord.DB_FIELD_YEAR),
        @Index(name = CvrPlugin.DEBUG_TABLE_PREFIX + CompanyQuarterlyNumbersRecord.TABLE_NAME + "__quarter", columnList = CompanyQuarterlyNumbersRecord.DB_FIELD_QUARTER),

        @Index(name = CvrPlugin.DEBUG_TABLE_PREFIX + CompanyQuarterlyNumbersRecord.TABLE_NAME + "__" + CvrBitemporalRecord.DB_FIELD_LAST_UPDATED, columnList = CvrBitemporalRecord.DB_FIELD_LAST_UPDATED),
        @Index(name = CvrPlugin.DEBUG_TABLE_PREFIX + CompanyQuarterlyNumbersRecord.TABLE_NAME + "__" + CvrRecordPeriod.DB_FIELD_VALID_FROM, columnList = CvrRecordPeriod.DB_FIELD_VALID_FROM),
        @Index(name = CvrPlugin.DEBUG_TABLE_PREFIX + CompanyQuarterlyNumbersRecord.TABLE_NAME + "__" + CvrRecordPeriod.DB_FIELD_VALID_TO, columnList = CvrRecordPeriod.DB_FIELD_VALID_TO)
})
public class CompanyQuarterlyNumbersRecord extends CompanyNumbersRecord {

    public static final String TABLE_NAME = "cvr_record_quarterly_numbers";

    public static final String DB_FIELD_YEAR = "year";
    public static final String IO_FIELD_YEAR = "aar";

    @JsonIgnore
    public String getFieldName() {
        return TABLE_NAME;
    }

    @Column(name = DB_FIELD_YEAR)
    @JsonProperty(value = IO_FIELD_YEAR)
    private int year;

    public int getYear() {
        return this.year;
    }

    public static final String DB_FIELD_QUARTER = "quarter";
    public static final String IO_FIELD_QUARTER = "kvartal";

    @Column(name = DB_FIELD_QUARTER)
    @JsonProperty(value = IO_FIELD_QUARTER)
    private int quarter;

    public int getQuarter() {
        return this.quarter;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        CompanyQuarterlyNumbersRecord that = (CompanyQuarterlyNumbersRecord) o;
        return year == that.year &&
                quarter == that.quarter;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), year, quarter);
    }

    /*@Override
    public boolean equalData(Object o) {
        if (!super.equalData(o)) return false;
        CompanyQuarterlyNumbersRecord that = (CompanyQuarterlyNumbersRecord) o;
        return year == that.year && quarter == that.quarter;
    }*/
}
