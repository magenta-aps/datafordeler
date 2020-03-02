package dk.magenta.datafordeler.cpr.data.road.data;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import dk.magenta.datafordeler.core.database.DatabaseEntry;
import dk.magenta.datafordeler.cpr.CprPlugin;
import dk.magenta.datafordeler.cpr.data.DetailData;

import javax.persistence.*;
import javax.xml.bind.annotation.XmlElement;
import java.util.HashMap;
import java.util.Map;

import static dk.magenta.datafordeler.cpr.data.road.data.RoadCityData.DB_FIELD_BASEDATA;

/**
 * Storage for data on a Road's notes,
 * referenced by {@link dk.magenta.datafordeler.cpr.data.road.data.RoadBaseData}
 */
@Entity
@Table(name= "cpr_road_memo", indexes = {
        @Index(name = CprPlugin.DEBUG_TABLE_PREFIX + "cpr_road_memo_base", columnList = DB_FIELD_BASEDATA + DatabaseEntry.REF)
})
public class RoadMemoData extends DetailData {

    public static final String DB_FIELD_BASEDATA = "roadBaseData";

    @JsonIgnore
    @ManyToOne(targetEntity = RoadBaseData.class, fetch = FetchType.EAGER)
    @JoinColumn(name = DB_FIELD_BASEDATA + DatabaseEntry.REF)
    private RoadBaseData roadBaseData;

    public RoadBaseData getRoadBaseData() {
        return this.roadBaseData;
    }

    public void setRoadBaseData(RoadBaseData roadBaseData) {
        this.roadBaseData = roadBaseData;
    }

    public static final String DB_FIELD_MEMONUMBER = "memoNumber";
    public static final String IO_FIELD_MEMONUMBER = "notatNummer";
    @Column(name = DB_FIELD_MEMONUMBER)
    @JsonProperty(value = IO_FIELD_MEMONUMBER)
    @XmlElement(name = IO_FIELD_MEMONUMBER)
    private int memoNumber;

    public int getMemoNumber() {
        return memoNumber;
    }

    public void setMemoNumber(int memoNumber) {
        this.memoNumber = memoNumber;
    }

    public static final String DB_FIELD_MEMOLINE = "memoText";
    public static final String IO_FIELD_MEMOLINE = "notatLinie";
    @Column(name = DB_FIELD_MEMOLINE)
    @JsonProperty(value = IO_FIELD_MEMOLINE)
    @XmlElement(name = IO_FIELD_MEMOLINE)
    private String memoText;

    public String getMemoText() {
        return memoText;
    }

    public void setMemoText(String memoText) {
        this.memoText = memoText;
    }


    /**
     * Return a map of attributes, including those from the superclass
     * @return
     */
    @Override
    public Map<String, Object> asMap() {
        HashMap<String, Object> map = new HashMap<>();

        if (this.memoNumber != 0) {
            map.put("memoNumber", this.memoNumber);
        }
        if (this.memoText != null) {
            map.put("memoText", this.memoText);
        }
        return map;
    }


    @JsonIgnore
    public Map<String, Object> databaseFields() {
        HashMap<String, Object> map = new HashMap<>();
        map.put(DB_FIELD_MEMOLINE, this.memoText);
        map.put(DB_FIELD_MEMONUMBER, this.memoNumber);
        return map;
    }

    @Override
    protected RoadMemoData clone() {
        RoadMemoData clone = new RoadMemoData();
        clone.memoText = this.memoText;
        clone.memoNumber = this.memoNumber;
        clone.setDafoUpdated(this.getDafoUpdated());
        return clone;
    }

}
