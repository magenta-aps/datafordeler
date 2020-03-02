package dk.magenta.datafordeler.cvr.data.companyunit;

import com.fasterxml.jackson.annotation.JsonIgnore;
import dk.magenta.datafordeler.core.database.DatabaseEntry;
import dk.magenta.datafordeler.cvr.data.shared.MonthlyEmployeeNumbersData;

import javax.persistence.*;

/**
 * Storage for data on a Company Unit's monthly employees
 * referenced by {@link dk.magenta.datafordeler.cvr.data.companyunit.CompanyUnitBaseData}
 */
@Entity
@Table(name = "cvr_companyunit_monthly_employees", indexes = {
        @Index(name = "cvr_companyunit_monthlyEmployess_year", columnList = CompanyUnitMonthlyEmployeeNumbersData.DB_FIELD_YEAR),
        @Index(name = "cvr_companyunit_monthlyEmployees_month", columnList = CompanyUnitMonthlyEmployeeNumbersData.DB_FIELD_MONTH + ", " + CompanyUnitMonthlyEmployeeNumbersData.DB_FIELD_YEAR),
        @Index(name = "cvr_companyunit_monthlyEmployees_base", columnList = CompanyUnitMonthlyEmployeeNumbersData.DB_FIELD_BASEDATA + DatabaseEntry.REF)
})
public class CompanyUnitMonthlyEmployeeNumbersData extends MonthlyEmployeeNumbersData {

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
