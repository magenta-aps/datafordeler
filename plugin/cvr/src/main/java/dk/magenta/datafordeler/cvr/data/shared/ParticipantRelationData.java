package dk.magenta.datafordeler.cvr.data.shared;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import dk.magenta.datafordeler.core.database.Identification;
import dk.magenta.datafordeler.cvr.data.DetailData;
import org.hibernate.Hibernate;
import org.hibernate.Session;

import javax.persistence.*;
import javax.xml.bind.annotation.XmlElement;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Storage for data on a Company's participants,
 * referenced by {@link dk.magenta.datafordeler.cvr.data.company.CompanyBaseData}
 */
@Entity
@Table(name = "cvr_participantrelation")
public class ParticipantRelationData extends DetailData implements Comparable<ParticipantRelationData> {

    @Override
    public int compareTo(ParticipantRelationData o) {
        Identification i1 = this.participant;
        Identification i2 = o == null ? null : o.participant;
        if (i1 == null && i2 == null) return 0;
        if (i1 == null) return -1;
        if (i2 == null) return 1;
        return i1.getUuid().compareTo(i2.getUuid());
    }

    public static class Comparator implements java.util.Comparator<ParticipantRelationData> {
        @Override
        public int compare(ParticipantRelationData o1, ParticipantRelationData o2) {
            if (o1 == null && o2 == null) return 0;
            if (o1 == null) return -1;
            return o1.compareTo(o2);
        }
    }

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

    //--------------------------------------------------

    public static final String DB_FIELD_ORGANIZATIONS = "organizations";
    public static final String IO_FIELD_ORGANIZATIONS = "organisationer";

    @ManyToMany(cascade = CascadeType.ALL)
    private Set<Identification> organizations = new HashSet<>();

    public void addOrganization(Identification organization) {
        this.organizations.add(organization);
    }

    @JsonProperty(value = IO_FIELD_ORGANIZATIONS)
    @XmlElement(name = IO_FIELD_ORGANIZATIONS)
    public Set<Identification> getOrganizations() {
        return this.organizations;
    }

    //--------------------------------------------

    @Override
    public Map<String, Object> asMap() {
        HashMap<String, Object> map = new HashMap<>();
        map.put(DB_FIELD_PARTICIPANT, this.participant);
        map.put(DB_FIELD_ORGANIZATIONS, this.organizations);
        return map;
    }

    /**
     * Return a map of attributes
     * @return
     */
    @JsonIgnore
    public Map<String, Object> databaseFields() {
        HashMap<String, Object> map = new HashMap<>();
        map.put(DB_FIELD_PARTICIPANT, this.participant);
        map.put(DB_FIELD_ORGANIZATIONS, this.organizations);
        return map;
    }

    public void forceLoad(Session session) {
        Hibernate.initialize(this);
        Hibernate.initialize(this.participant);
        for (Identification organization : this.organizations) {
            Hibernate.initialize(organization);
        }
    }
}
