package dk.magenta.datafordeler.cvr.data.company;

import com.fasterxml.jackson.annotation.JsonIgnore;
import dk.magenta.datafordeler.core.database.DatabaseEntry;
import dk.magenta.datafordeler.cvr.data.shared.YearlyEmployeeNumbersData;

import javax.persistence.*;

/**
 * Storage for data on a Company's yearly employees
 * referenced by {@link dk.magenta.datafordeler.cvr.data.company.CompanyBaseData}
 */
@Entity
@Table(name = "cvr_company_yearly_employees", indexes = {
        @Index(name = "cvr_company_yearlyEmployees_year", columnList = CompanyYearlyEmployeeNumbersData.DB_FIELD_YEAR),
        @Index(name = "cvr_company_yearlyEmployees_base", columnList = CompanyYearlyEmployeeNumbersData.DB_FIELD_BASEDATA + DatabaseEntry.REF)
})
public class CompanyYearlyEmployeeNumbersData extends YearlyEmployeeNumbersData {

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
