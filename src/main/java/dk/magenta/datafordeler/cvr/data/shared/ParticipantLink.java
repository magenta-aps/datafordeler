package dk.magenta.datafordeler.cvr.data.shared;

import com.fasterxml.jackson.annotation.JsonProperty;
import dk.magenta.datafordeler.cvr.data.company.CompanyBaseData;
import dk.magenta.datafordeler.cvr.data.companyunit.CompanyUnitBaseData;

import javax.persistence.Entity;
import javax.persistence.ManyToMany;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlElement;
import java.util.HashSet;
import java.util.Set;

/**
 * Storage for data on a Company's or CompanyUnit's participants
 * referenced by {@link dk.magenta.datafordeler.cvr.data.company.CompanyBaseData}
 * and {@link dk.magenta.datafordeler.cvr.data.companyunit.CompanyUnitBaseData}
 */
@Entity
@Table(name = "cvr_company_participantlink")
public class ParticipantLink extends SingleData<Integer> {

    public static final String IO_FIELD_NUMBER = "deltagernummer";

    @JsonProperty(value = IO_FIELD_NUMBER)
    @XmlElement(name = IO_FIELD_NUMBER)
    public int getParticipantNumber() {
        return this.getValue();
    }

    public static final String DB_FIELD_COMPANYBASES = "companyBases";
    @ManyToMany
    private Set<CompanyBaseData> companyBases = new HashSet<>();

    @ManyToMany
    private Set<CompanyUnitBaseData> companyUnitBases = new HashSet<>();

}
