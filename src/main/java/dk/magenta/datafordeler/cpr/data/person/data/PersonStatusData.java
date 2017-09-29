package dk.magenta.datafordeler.cpr.data.person.data;

import com.fasterxml.jackson.annotation.JsonProperty;
import dk.magenta.datafordeler.cpr.data.DetailData;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlElement;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by lars on 21-06-17.
 */
@Entity
@Table(name = "cpr_person_status")
public class PersonStatusData extends DetailData {

    public static final String DB_FIELD_STATUS = "status";
    public static final String IO_FIELD_STATUS = "status";
    @Column(name = DB_FIELD_STATUS)
    @JsonProperty(value = IO_FIELD_STATUS)
    @XmlElement(name = IO_FIELD_STATUS)
    private String status;

    public String getStatus() {
        return this.status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public Map<String, Object> databaseFields() {
        return Collections.singletonMap(DB_FIELD_STATUS, this.status);
    }

    @Override
    public Map<String, Object> asMap() {
        HashMap<String, Object> map = new HashMap<>();
        if (this.status != null) {
            map.put("status", this.status);
        }
        return map;
    }
}
