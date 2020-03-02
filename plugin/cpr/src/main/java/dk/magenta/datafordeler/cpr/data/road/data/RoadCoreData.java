package dk.magenta.datafordeler.cpr.data.road.data;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import dk.magenta.datafordeler.cpr.CprPlugin;
import dk.magenta.datafordeler.cpr.data.DetailData;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Index;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlElement;
import java.util.HashMap;
import java.util.Map;

import static dk.magenta.datafordeler.cpr.data.road.data.RoadCoreData.DB_FIELD_ROAD_NAME;

/**
 * Storage for data on a Road's core data (name, connections),
 * referenced by {@link dk.magenta.datafordeler.cpr.data.road.data.RoadBaseData}
 */
@Entity
@Table(name= "cpr_road_core", indexes = {
        @Index(name = CprPlugin.DEBUG_TABLE_PREFIX + "cpr_road_name", columnList = DB_FIELD_ROAD_NAME),
})
public class RoadCoreData extends DetailData {

    public static final String DB_FIELD_TO_MUNICIPALITY = "toMunicipality";
    public static final String IO_FIELD_TO_MUNICIPALITY = "tilKommunekode";
    @Column(name = DB_FIELD_TO_MUNICIPALITY)
    @JsonProperty(value = IO_FIELD_TO_MUNICIPALITY)
    @XmlElement(name = IO_FIELD_TO_MUNICIPALITY)
    private int toMunicipality;

    public int getToMunicipality() {
        return this.toMunicipality;
    }

    public void setToMunicipality(int toMunicipality) {
        this.toMunicipality = toMunicipality;
    }


    public static final String DB_FIELD_TO_ROAD = "toRoad";
    public static final String IO_FIELD_TO_ROAD = "tilVejkode";
    @Column(name = DB_FIELD_TO_ROAD)
    @JsonProperty(value = IO_FIELD_TO_ROAD)
    @XmlElement(name = IO_FIELD_TO_ROAD)
    private int toRoad;

    public int getToRoad() {
        return this.toRoad;
    }

    public void setToRoad(int toRoad) {
        this.toRoad = toRoad;
    }

    public static final String DB_FIELD_FROM_MUNICIPALITY = "fromMunicipality";
    public static final String IO_FIELD_FROM_MUNICIPALITY = "fraKommunekode";
    @Column(name = DB_FIELD_FROM_MUNICIPALITY)
    @JsonProperty(value = IO_FIELD_FROM_MUNICIPALITY)
    @XmlElement(name = IO_FIELD_FROM_MUNICIPALITY)
    private int fromMunicipality;

    public int getFromMunicipality() {
        return this.fromMunicipality;
    }

    public void setFromMunicipality(int fromMunicipality) {
        this.fromMunicipality = fromMunicipality;
    }

    public static final String DB_FIELD_FROM_ROAD = "fromRoad";
    public static final String IO_FIELD_FROM_ROAD = "fraVejkode";
    @Column(name = DB_FIELD_FROM_ROAD)
    @JsonProperty(value = IO_FIELD_FROM_ROAD)
    @XmlElement(name = IO_FIELD_FROM_ROAD)
    private int fromRoad;

    public int getFromRoad() {
        return this.fromRoad;
    }

    public void setFromRoad(int fromRoad) {
        this.fromRoad = fromRoad;
    }

    public static final String DB_FIELD_ADDRESS_NAME = "addressingName";
    public static final String IO_FIELD_ADDRESS_NAME = "adresseringsnavn";
    @Column(name = DB_FIELD_ADDRESS_NAME)
    @JsonProperty(value = IO_FIELD_ADDRESS_NAME)
    @XmlElement(name = IO_FIELD_ADDRESS_NAME)
    private String addressingName;

    public String getAddressingName() {
        return this.addressingName;
    }

    public void setAddressingName(String addressingName) {
        this.addressingName = addressingName;
    }

    public static final String DB_FIELD_ROAD_NAME = "name";
    public static final String IO_FIELD_ROAD_NAME = "vejnavn";
    @Column(name = DB_FIELD_ROAD_NAME)
    @JsonProperty(value = IO_FIELD_ROAD_NAME)
    @XmlElement(name = IO_FIELD_ROAD_NAME)
    private String name;

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * Return a map of attributes, including those from the superclass
     * @return
     */
    @Override
    public Map<String, Object> asMap() {
        HashMap<String, Object> map = new HashMap<>();

        if (this.toMunicipality != 0) {
            map.put(IO_FIELD_TO_MUNICIPALITY, this.toMunicipality);
        }
        if (this.toRoad != 0) {
            map.put(IO_FIELD_TO_ROAD, this.toRoad);
        }
        if (this.fromMunicipality != 0) {
            map.put(IO_FIELD_FROM_MUNICIPALITY, this.fromMunicipality);
        }
        if (this.fromRoad != 0) {
            map.put(IO_FIELD_FROM_ROAD, this.fromRoad);
        }
        if (this.addressingName != null) {
            map.put(IO_FIELD_ADDRESS_NAME, this.addressingName);
        }
        if (this.name != null) {
            map.put(IO_FIELD_ROAD_NAME, this.name);
        }
        return map;
    }

    @JsonIgnore
    public Map<String, Object> databaseFields() {
        HashMap<String, Object> map = new HashMap<>();
        map.put(DB_FIELD_FROM_MUNICIPALITY, this.fromMunicipality);
        map.put(DB_FIELD_FROM_ROAD, this.fromRoad);
        map.put(DB_FIELD_TO_MUNICIPALITY, this.toMunicipality);
        map.put(DB_FIELD_TO_ROAD, this.toRoad);
        map.put(DB_FIELD_ADDRESS_NAME, this.addressingName);
        map.put(DB_FIELD_ROAD_NAME, this.name);
        return map;
    }

    @Override
    protected RoadCoreData clone() {
        RoadCoreData clone = new RoadCoreData();
        clone.fromMunicipality = this.fromMunicipality;
        clone.fromRoad = this.fromRoad;
        clone.toMunicipality = this.toMunicipality;
        clone.toRoad = this.toRoad;
        clone.addressingName = this.addressingName;
        clone.name = this.name;
        clone.setDafoUpdated(this.getDafoUpdated());
        return clone;
    }
}
