package dk.magenta.datafordeler.cvr.data.shared;

import com.fasterxml.jackson.annotation.JsonIgnore;
import dk.magenta.datafordeler.cvr.data.DetailData;
import dk.magenta.datafordeler.cvr.data.unversioned.Industry;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.util.HashMap;
import java.util.Map;

/**
 * Class linking companies & units to industries
 */
@Entity
@Table(name="cvr_industrylink")
public class IndustryData extends DetailData {

    public IndustryData() {
        this(false);
    }
    public IndustryData(boolean primary) {
        this.isPrimary = primary;
    }



    public static final String DB_FIELD_INDUSTRY = "industry";
    public static final String IO_FIELD_INDUSTRY = "branche";
    @ManyToOne
    @Cascade(value = CascadeType.SAVE_UPDATE)
    private Industry industry;

    public Industry getIndustry() {
        return this.industry;
    }

    public void setIndustry(Industry industry) {
        this.industry = industry;
    }



    public static final String DB_FIELD_PRIMARY = "isPrimary";
    public static final String IO_FIELD_PRIMARY = "primaer";
    @Column
    private boolean isPrimary;


    public boolean isPrimary() {
        return this.isPrimary;
    }

    public void setPrimary(boolean primary) {
        this.isPrimary = primary;
    }



    public Map<String, Object> asMap() {
        HashMap<String, Object> map = new HashMap<>();
        map.put("industry", this.industry);
        map.put("primary", this.isPrimary);
        return map;
    }

    /**
     * Return a map of attributes
     * @return
     */
    @JsonIgnore
    public Map<String, Object> databaseFields() {
        HashMap<String, Object> map = new HashMap<>();
        map.put(DB_FIELD_INDUSTRY, this.industry);
        map.put(DB_FIELD_PRIMARY, this.isPrimary);
        return map;
    }

}
