package dk.magenta.datafordeler.cvr.data.company;

import com.fasterxml.jackson.annotation.JsonProperty;
import dk.magenta.datafordeler.core.database.DatabaseEntry;
import dk.magenta.datafordeler.cvr.data.DetailData;
import dk.magenta.datafordeler.cvr.data.unversioned.CompanyForm;

import javax.persistence.*;
import javax.xml.bind.annotation.XmlElement;
import java.util.HashMap;
import java.util.Map;

/**
 * Storage for data on a Company's form
 * referenced by {@link dk.magenta.datafordeler.cvr.data.company.CompanyBaseData}
 */
@Entity
@Table(name="cvr_company_form", indexes = {
        @Index(name = "cvr_company_form_form", columnList = CompanyFormData.DB_FIELD_FORM + DatabaseEntry.REF)
})
public class CompanyFormData extends DetailData {

    /**
     * Return a map of attributes, including those from the superclass
     * @return
     */
    @Override
    public Map<String, Object> asMap() {
        HashMap<String, Object> map = new HashMap<>();
        map.put("companyForm", this.companyForm);
        return map;
    }


    public static final String DB_FIELD_FORM = "companyForm";
    public static final String IO_FIELD_FORM = "virksomhedsform";
    @ManyToOne
    @JsonProperty(value = IO_FIELD_FORM)
    @XmlElement(name = IO_FIELD_FORM)
    @JoinColumn(name = DB_FIELD_FORM + DatabaseEntry.REF)
    private CompanyForm companyForm;

    public CompanyForm getCompanyForm() {
        return this.companyForm;
    }

    public void setCompanyForm(CompanyForm companyForm) {
        this.companyForm = companyForm;
    }

}
