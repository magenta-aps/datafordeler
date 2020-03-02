package dk.magenta.datafordeler.cvr.records;

import com.fasterxml.jackson.annotation.JsonProperty;
import dk.magenta.datafordeler.core.database.DatabaseEntry;
import dk.magenta.datafordeler.core.exception.ParseException;
import dk.magenta.datafordeler.cvr.data.company.CompanyBaseData;
import dk.magenta.datafordeler.cvr.data.companyunit.CompanyUnitBaseData;
import org.hibernate.Session;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Index;
import javax.persistence.Table;
import java.util.Objects;

/**
 * Record for Company and CompanyUnit quarterly employee numbers.
 */
@Entity
@Table(name = CompanyQuarterlyNumbersRecord.TABLE_NAME, indexes = {
        @Index(name = CompanyQuarterlyNumbersRecord.TABLE_NAME + "__company", columnList = CompanyQuarterlyNumbersRecord.DB_FIELD_COMPANY + DatabaseEntry.REF),
        @Index(name = CompanyQuarterlyNumbersRecord.TABLE_NAME + "__unit", columnList = CompanyQuarterlyNumbersRecord.DB_FIELD_COMPANYUNIT + DatabaseEntry.REF),
        @Index(name = CompanyQuarterlyNumbersRecord.TABLE_NAME + "__year", columnList = CompanyQuarterlyNumbersRecord.DB_FIELD_YEAR),
        @Index(name = CompanyQuarterlyNumbersRecord.TABLE_NAME + "__quarter", columnList = CompanyQuarterlyNumbersRecord.DB_FIELD_QUARTER),
})
public class CompanyQuarterlyNumbersRecord extends CompanyNumbersRecord {

    public static final String TABLE_NAME = "cvr_record_quarterly_numbers";

    public static final String DB_FIELD_YEAR = "year";
    public static final String IO_FIELD_YEAR = "aar";

    @Column(name = DB_FIELD_YEAR)
    @JsonProperty(value = IO_FIELD_YEAR)
    private int year;

    public static final String DB_FIELD_QUARTER = "quarter";
    public static final String IO_FIELD_QUARTER = "kvartal";

    @Column(name = DB_FIELD_QUARTER)
    @JsonProperty(value = IO_FIELD_QUARTER)
    private int quarter;

    @Override
    public void populateBaseData(CompanyBaseData baseData, Session session) throws ParseException {
        baseData.setQuarterlyEmployeeNumbers(
                this.year,
                this.quarter,
                this.getEmployeeLow(),
                this.getEmployeeHigh(),
                this.getFulltimeEquivalentLow(),
                this.getFulltimeEquivalentHigh(),
                this.getIncludingOwnersLow(),
                this.getIncludingOwnersHigh()
        );
    }

    @Override
    public void populateBaseData(CompanyUnitBaseData baseData, Session session) throws ParseException {
        baseData.setQuarterlyEmployeeNumbers(
                this.year,
                this.quarter,
                this.getEmployeeLow(),
                this.getEmployeeHigh(),
                this.getFulltimeEquivalentLow(),
                this.getFulltimeEquivalentHigh(),
                this.getIncludingOwnersLow(),
                this.getIncludingOwnersHigh()
        );
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
}
