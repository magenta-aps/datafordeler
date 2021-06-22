package dk.magenta.datafordeler.cvr.records;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import dk.magenta.datafordeler.core.database.DatabaseEntry;
import dk.magenta.datafordeler.cvr.CvrPlugin;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Index;
import javax.persistence.Table;
import java.util.Objects;

/**
 * Record for Company and CompanyUnit monthly employee numbers.
 */
@Entity
@Table(name = CvrPlugin.DEBUG_TABLE_PREFIX + CompanyMonthlyNumbersRecord.TABLE_NAME, indexes = {
        @Index(name = CvrPlugin.DEBUG_TABLE_PREFIX + CompanyMonthlyNumbersRecord.TABLE_NAME + "__company", columnList = CompanyMonthlyNumbersRecord.DB_FIELD_COMPANY + DatabaseEntry.REF),
        @Index(name = CvrPlugin.DEBUG_TABLE_PREFIX + CompanyMonthlyNumbersRecord.TABLE_NAME + "__unit", columnList = CompanyMonthlyNumbersRecord.DB_FIELD_COMPANYUNIT + DatabaseEntry.REF),
        @Index(name = CvrPlugin.DEBUG_TABLE_PREFIX + CompanyMonthlyNumbersRecord.TABLE_NAME + "__year", columnList = CompanyMonthlyNumbersRecord.DB_FIELD_YEAR),
        @Index(name = CvrPlugin.DEBUG_TABLE_PREFIX + CompanyMonthlyNumbersRecord.TABLE_NAME + "__month", columnList = CompanyMonthlyNumbersRecord.DB_FIELD_MONTH),

        @Index(name = CvrPlugin.DEBUG_TABLE_PREFIX + CompanyMonthlyNumbersRecord.TABLE_NAME + "__" + CvrBitemporalRecord.DB_FIELD_LAST_UPDATED, columnList = CvrBitemporalRecord.DB_FIELD_LAST_UPDATED),
        @Index(name = CvrPlugin.DEBUG_TABLE_PREFIX + CompanyMonthlyNumbersRecord.TABLE_NAME + "__" + CvrRecordPeriod.DB_FIELD_VALID_FROM, columnList = CvrRecordPeriod.DB_FIELD_VALID_FROM),
        @Index(name = CvrPlugin.DEBUG_TABLE_PREFIX + CompanyMonthlyNumbersRecord.TABLE_NAME + "__" + CvrRecordPeriod.DB_FIELD_VALID_TO, columnList = CvrRecordPeriod.DB_FIELD_VALID_TO)
})
@JsonIgnoreProperties(ignoreUnknown = true)
public class CompanyMonthlyNumbersRecord extends CompanyNumbersRecord {

    public static final String TABLE_NAME = "cvr_record_monthly_numbers";

    public static final String DB_FIELD_YEAR = "year";
    public static final String IO_FIELD_YEAR = "aar";

    @JsonIgnore
    public String getFieldName() {
        return TABLE_NAME;
    }

    @Column(name = DB_FIELD_YEAR)
    @JsonProperty(value = IO_FIELD_YEAR)
    private int year;



    public static final String DB_FIELD_MONTH = "month";
    public static final String IO_FIELD_MONTH = "maaned";

    @Column(name = DB_FIELD_MONTH)
    @JsonProperty(value = IO_FIELD_MONTH)
    private int month;


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        CompanyMonthlyNumbersRecord that = (CompanyMonthlyNumbersRecord) o;
        return year == that.year &&
                month == that.month;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), year, month);
    }


    /*@Override
    public boolean equalData(Object o) {
        if (!super.equalData(o)) return false;
        CompanyMonthlyNumbersRecord that = (CompanyMonthlyNumbersRecord) o;
        return year == that.year && month == that.month;
    }*/
}
