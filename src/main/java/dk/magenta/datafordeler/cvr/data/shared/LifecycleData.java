package dk.magenta.datafordeler.cvr.data.shared;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import dk.magenta.datafordeler.cvr.data.DetailData;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlElement;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Storage for data on a Company's lifecycle data,
 * referenced by {@link dk.magenta.datafordeler.cvr.data.company.CompanyBaseData}
 */
@Entity
@Table(name = "cvr_company_lifecycle")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class LifecycleData extends DetailData {

    public static final String DB_FIELD_START = "startDate";
    public static final String IO_FIELD_START = "startDato";

    @JsonProperty(value = IO_FIELD_START)
    @XmlElement(name = IO_FIELD_START)
    @Column(name = DB_FIELD_START, nullable = true)
    private OffsetDateTime startDate;

    public OffsetDateTime getStartDate() {
        return startDate;
    }

    public void setStartDate(OffsetDateTime startDate) {
        this.startDate = startDate;
    }

    //-------------------------------------------

    public static final String DB_FIELD_END = "endDate";
    public static final String IO_FIELD_END = "slutDato";

    @JsonProperty(value = IO_FIELD_END)
    @XmlElement(name = IO_FIELD_END)
    @Column(name = DB_FIELD_END, nullable = true)
    private OffsetDateTime endDate;

    public OffsetDateTime getEndDate() {
        return endDate;
    }

    public void setEndDate(OffsetDateTime endDate) {
        this.endDate = endDate;
    }

    //-------------------------------------------

    @Override
    public Map<String, Object> asMap() {
        HashMap<String, Object> map = new HashMap<>();
        if (this.startDate != null) {
            map.put("startDato", this.startDate);
        }
        if (this.endDate != null) {
            map.put("slutDato", this.endDate);
        }
        return map;
    }


    @JsonIgnore
    public Map<String, Object> databaseFields() {
        HashMap<String, Object> map = new HashMap<>();
        map.put(DB_FIELD_START, this.startDate);
        map.put(DB_FIELD_END, this.endDate);
        return map;
    }
}
