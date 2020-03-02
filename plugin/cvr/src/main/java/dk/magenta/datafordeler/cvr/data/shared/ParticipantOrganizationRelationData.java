package dk.magenta.datafordeler.cvr.data.shared;

import com.fasterxml.jackson.annotation.JsonProperty;
import dk.magenta.datafordeler.core.database.Identification;
import dk.magenta.datafordeler.cvr.data.DetailData;
import dk.magenta.datafordeler.cvr.data.company.CompanyBaseData;
import dk.magenta.datafordeler.cvr.data.companyunit.CompanyUnitBaseData;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlElement;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

/**
 * Storage for data on a Company's participants,
 * referenced by {@link dk.magenta.datafordeler.cvr.data.company.CompanyBaseData}
 */
@Entity
@Table(name = "cvr_participantrelation")
public abstract class ParticipantOrganizationRelationData extends DetailData {

    @ManyToOne(targetEntity = CompanyBaseData.class, optional = true)
    private CompanyBaseData companyBaseData;

    @ManyToOne(targetEntity = CompanyUnitBaseData.class, optional = true)
    private CompanyUnitBaseData companyUnitBaseData;

    //--------------------------------------------------

    public static final String DB_FIELD_PARTICIPANT = "participant";
    public static final String IO_FIELD_PARTICIPANT = "deltager";

    @ManyToOne(cascade = CascadeType.MERGE)
    private Identification participant;

    public void setParticipant(Identification participant) {
        this.participant = participant;
    }

    @JsonProperty(value = IO_FIELD_PARTICIPANT)
    @XmlElement(name = IO_FIELD_PARTICIPANT)
    public Identification getParticipant() {
        return this.participant;
    }

    //--------------------------------------------


    // Opret en ParticipantRelationData for hver organisation i hver deltager
    // Sæt bitemporalitet først, og populér derefter


    private String organizationName;

    public String getOrganizationName() {
        return this.organizationName;
    }

    public void setOrganizationName(String organizationName) {
        this.organizationName = organizationName;
    }



    private String organizationType;

    public String getOrganizationType() {
        return this.organizationType;
    }

    public void setOrganizationType(String organizationType) {
        this.organizationType = organizationType;
    }



    private long organizationNumber;

    public long getOrganizationNumber() {
        return this.organizationNumber;
    }

    public void setOrganizationNumber(long organizationNumber) {
        this.organizationNumber = organizationNumber;
    }



    private HashSet<AttributeData> organizationAttributes;

    private HashSet<AttributeData> memberAttributes;


    //--------------------------------------------

    @Override
    public Map<String, Object> asMap() {
        HashMap<String, Object> map = new HashMap<>();
        map.put(DB_FIELD_PARTICIPANT, this.participant);
        //map.put(DB_FIELD_ORGANIZATIONS, this.organizations);
        return map;
    }

    /**
     * Return a map of attributes
     * @return
     */
    /*@JsonIgnore
    public Map<String, Object> databaseFields() {
        HashMap<String, Object> map = new HashMap<>();
        map.put(DB_FIELD_PARTICIPANT, this.participant);
        //map.put(DB_FIELD_ORGANIZATIONS, this.organizations);
        return map;
    }*/

    /*public void forceLoad(Session session) {
        Hibernate.initialize(this);
        Hibernate.initialize(this.participant);
        for (Identification organization : this.organizations) {
            Hibernate.initialize(organization);
        }
    }*/
}
