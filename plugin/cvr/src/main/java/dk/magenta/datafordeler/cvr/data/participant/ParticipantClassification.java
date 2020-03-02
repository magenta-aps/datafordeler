package dk.magenta.datafordeler.cvr.data.participant;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import dk.magenta.datafordeler.cvr.data.unversioned.UnversionedEntity;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;
import javax.xml.bind.annotation.XmlElement;
import java.util.HashMap;
import java.util.Map;

/**
 * Storage for data on a Participant's classification
 * referenced by {@link dk.magenta.datafordeler.cvr.data.participant.ParticipantBaseData}
 */
@MappedSuperclass
public abstract class ParticipantClassification extends UnversionedEntity {

    public static final String DB_FIELD_NAME = "name";
    public static final String IO_FIELD_NAME = "navn";

    @Column(name = DB_FIELD_NAME, unique = true)
    @JsonProperty(value = IO_FIELD_NAME)
    @XmlElement(name = IO_FIELD_NAME)
    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * Return a map of attributes, including those from the superclass
     * @return
     */
    @JsonIgnore
    public Map<String, Object> databaseFields() {
        HashMap<String, Object> map = new HashMap<>();
        map.put(DB_FIELD_NAME, this.getName());
        return map;
    }
}
