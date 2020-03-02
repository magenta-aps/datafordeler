package dk.magenta.datafordeler.cvr.data.company;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import dk.magenta.datafordeler.core.database.Identification;
import dk.magenta.datafordeler.cvr.data.DetailData;

import javax.persistence.*;
import javax.xml.bind.annotation.XmlElement;
import java.util.*;

/**
 * Storage for data on a Company's units
 * referenced by {@link dk.magenta.datafordeler.cvr.data.company.CompanyBaseData}
 */
@Entity
@Table(name = "cvr_company_unitlink")
public class CompanyUnitLink extends DetailData {

    public static final String DB_FIELD_PNUMBER = "pNumber";
    public static final String IO_FIELD_PNUMBER = "pnummer";

    @JsonProperty(value = IO_FIELD_PNUMBER)
    @XmlElement(name = IO_FIELD_PNUMBER)
    @Column(name = DB_FIELD_PNUMBER, insertable = true, updatable = false, nullable = false)
    private int pNumber;

    public int getpNumber() {
        return this.pNumber;
    }

    public void setpNumber(int pNumber) {
        this.pNumber = pNumber;
    }

    //------------------------------------------------------------

    public static final String DB_FIELD_IDENTIFICATION = "identification";
    public static final String IO_FIELD_IDENTIFICATION = "identifikation";

    @ManyToOne(cascade = {CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH})
    @JsonProperty(value = IO_FIELD_IDENTIFICATION)
    @XmlElement(name = IO_FIELD_IDENTIFICATION)
    private Identification identification;

    public void setIdentification(Identification identification) {
        this.identification = identification;
    }

    public Identification getIdentification() {
        return this.identification;
    }


    //------------------------------------------------------------

    public static final String DB_FIELD_COMPANYBASES = "companyBases";
    @ManyToMany(mappedBy = CompanyBaseData.DB_FIELD_UNITS)
    private Set<CompanyBaseData> companyBases = new HashSet<>();

    @Override
    public Map<String, Object> asMap() {
        return Collections.singletonMap("pNumber", this.pNumber);
    }

    /**
     * Return a map of attributes, including those from the superclass
     * @return
     */
    @JsonIgnore
    public Map<String, Object> databaseFields() {
        HashMap<String, Object> map = new HashMap<>();
        map.put(DB_FIELD_PNUMBER, this.pNumber);
        return map;
    }
}
