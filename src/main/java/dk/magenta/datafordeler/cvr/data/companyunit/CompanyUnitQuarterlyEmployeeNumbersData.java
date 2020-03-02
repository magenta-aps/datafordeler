package dk.magenta.datafordeler.cvr.data.companyunit;

import com.fasterxml.jackson.annotation.JsonIgnore;
import dk.magenta.datafordeler.core.database.DatabaseEntry;
import dk.magenta.datafordeler.cvr.data.shared.QuarterlyEmployeeNumbersData;

import javax.persistence.*;

/**
 * Storage for data on a Company Unit's quarterly employees
 * referenced by {@link dk.magenta.datafordeler.cvr.data.companyunit.CompanyUnitBaseData}
 */
@Entity
@Table(name = "cvr_companyunit_quarterly_employees", indexes = {
        @Index(name = "cvr_companyunit_quarterlyEmployees_year", columnList = CompanyUnitQuarterlyEmployeeNumbersData.DB_FIELD_YEAR),
        @Index(name = "cvr_companyunit_quarterlyEmployees_quarter", columnList = CompanyUnitQuarterlyEmployeeNumbersData.DB_FIELD_QUARTER + ", " + CompanyUnitQuarterlyEmployeeNumbersData.DB_FIELD_YEAR),
        @Index(name = "cvr_companyunit_quarterlyEmployees_base", columnList = CompanyUnitQuarterlyEmployeeNumbersData.DB_FIELD_BASEDATA + DatabaseEntry.REF)
})
public class CompanyUnitQuarterlyEmployeeNumbersData extends QuarterlyEmployeeNumbersData {

    public static final String DB_FIELD_BASEDATA = "companyUnitBaseData";

    @JsonIgnore
    @ManyToOne(targetEntity = CompanyUnitBaseData.class)
    @JoinColumn(name = DB_FIELD_BASEDATA + DatabaseEntry.REF)
    private CompanyUnitBaseData companyUnitBaseData;

    public CompanyUnitBaseData getCompanyUnitBaseData() {
        return this.companyUnitBaseData;
    }

    public void setCompanyUnitBaseData(CompanyUnitBaseData companyUnitBaseData) {
        this.companyUnitBaseData = companyUnitBaseData;
    }
}
