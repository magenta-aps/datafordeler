package dk.magenta.datafordeler.cvr.records;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
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
 * Record for Company and CompanyUnit monthly employee numbers.
 */
@Entity
@Table(name = CompanyMonthlyNumbersRecord.TABLE_NAME, indexes = {
        @Index(name = CompanyMonthlyNumbersRecord.TABLE_NAME + "__company", columnList = CompanyMonthlyNumbersRecord.DB_FIELD_COMPANY + DatabaseEntry.REF),
        @Index(name = CompanyMonthlyNumbersRecord.TABLE_NAME + "__unit", columnList = CompanyMonthlyNumbersRecord.DB_FIELD_COMPANYUNIT + DatabaseEntry.REF),
        @Index(name = CompanyMonthlyNumbersRecord.TABLE_NAME + "__year", columnList = CompanyMonthlyNumbersRecord.DB_FIELD_YEAR),
        @Index(name = CompanyMonthlyNumbersRecord.TABLE_NAME + "__month", columnList = CompanyMonthlyNumbersRecord.DB_FIELD_MONTH),
})
@JsonIgnoreProperties(ignoreUnknown = true)
public class CompanyMonthlyNumbersRecord extends CompanyNumbersRecord {

    public static final String TABLE_NAME = "cvr_record_monthly_numbers";

    public static final String DB_FIELD_YEAR = "year";
    public static final String IO_FIELD_YEAR = "aar";

    @Column(name = DB_FIELD_YEAR)
    @JsonProperty(value = IO_FIELD_YEAR)
    private int year;



    public static final String DB_FIELD_MONTH = "month";
    public static final String IO_FIELD_MONTH = "maaned";

    @Column(name = DB_FIELD_MONTH)
    @JsonProperty(value = IO_FIELD_MONTH)
    private int month;



    @Override
    public void populateBaseData(CompanyBaseData baseData, Session session) throws ParseException {
        baseData.setMonthlyEmployeeNumbers(
                this.year,
                this.month,
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
        baseData.setMonthlyEmployeeNumbers(
                this.year,
                this.month,
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
        CompanyMonthlyNumbersRecord that = (CompanyMonthlyNumbersRecord) o;
        return year == that.year &&
                month == that.month;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), year, month);
    }
}
