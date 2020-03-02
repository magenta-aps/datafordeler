package dk.magenta.datafordeler.cvr.data.company;

import com.fasterxml.jackson.annotation.JsonIgnore;
import dk.magenta.datafordeler.core.database.DatabaseEntry;
import dk.magenta.datafordeler.cvr.data.shared.QuarterlyEmployeeNumbersData;

import javax.persistence.*;

/**
 * Storage for data on a Company's quarterly employees
 * referenced by {@link dk.magenta.datafordeler.cvr.data.company.CompanyBaseData}
 */
@Entity
@Table(name = "cvr_company_quarterly_employees", indexes = {
        @Index(name = "cvr_company_quarterlyEmployees_year", columnList = CompanyQuarterlyEmployeeNumbersData.DB_FIELD_YEAR),
        @Index(name = "cvr_company_quarterlyEmployees_quarter", columnList = CompanyQuarterlyEmployeeNumbersData.DB_FIELD_QUARTER + ", " + CompanyQuarterlyEmployeeNumbersData.DB_FIELD_YEAR),
        @Index(name = "cvr_company_quarterlyEmployees_base", columnList = CompanyQuarterlyEmployeeNumbersData.DB_FIELD_BASEDATA + DatabaseEntry.REF)
})
public class CompanyQuarterlyEmployeeNumbersData extends QuarterlyEmployeeNumbersData {

    public static final String DB_FIELD_BASEDATA = "companyBaseData";

    @JsonIgnore
    @ManyToOne(targetEntity = CompanyBaseData.class)
    @JoinColumn(name = DB_FIELD_BASEDATA + DatabaseEntry.REF)
    private CompanyBaseData companyBaseData;

    public CompanyBaseData getCompanyBaseData() {
        return this.companyBaseData;
    }

    public void setCompanyBaseData(CompanyBaseData companyBaseData) {
        this.companyBaseData = companyBaseData;
    }
}
