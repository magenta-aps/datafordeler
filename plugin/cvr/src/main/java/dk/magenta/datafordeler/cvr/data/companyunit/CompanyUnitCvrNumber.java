package dk.magenta.datafordeler.cvr.data.companyunit;

import dk.magenta.datafordeler.core.database.DatabaseEntry;
import dk.magenta.datafordeler.cvr.data.shared.SingleData;

import javax.persistence.*;

@Entity
@Table(name = "cvr_companyunit_cvr", indexes = {
        @Index(name = "cvr_companyunit_cvr_data", columnList = CompanyUnitCvrNumber.DB_FIELD_VALUE),
        @Index(name = "cvr_companyunit_cvr_base", columnList = CompanyUnitCvrNumber.DB_FIELD_BASEDATA + DatabaseEntry.REF)
})
public class CompanyUnitCvrNumber extends SingleData<Long> {

    public static final String DB_FIELD_BASEDATA = "companyUnitBaseData";

    @ManyToOne(targetEntity = CompanyUnitBaseData.class, optional = false)
    @JoinColumn(name = DB_FIELD_BASEDATA + DatabaseEntry.REF)
    private CompanyUnitBaseData companyUnitBaseData;

    public void setCompanyUnitBaseData(CompanyUnitBaseData companyUnitBaseData) {
        this.companyUnitBaseData = companyUnitBaseData;
    }
}
