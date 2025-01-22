package dk.magenta.datafordeler.gladdrreg.data.road;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import dk.magenta.datafordeler.core.database.DatabaseEntry;
import dk.magenta.datafordeler.core.database.Identification;
import dk.magenta.datafordeler.gladdrreg.data.SumiffiikData;

import jakarta.persistence.*;
import javax.xml.bind.annotation.XmlElement;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by lars on 16-05-17.
 */
@javax.persistence.Entity
@Table(name = "gladdrreg_road_data", indexes = {
        @Index(name = "gladdrreg_road_code", columnList = RoadData.DB_FIELD_CODE),
        @Index(name = "gladdrreg_road_name", columnList = RoadData.DB_FIELD_NAME),
        @Index(name = "gladdrreg_road_shortName", columnList = RoadData.DB_FIELD_SHORTNAME),
        @Index(name = "gladdrreg_road_alternateName", columnList = RoadData.DB_FIELD_ALTNAME),
        @Index(name = "gladdrreg_road_cprName", columnList = RoadData.DB_FIELD_CPRNAME),
        @Index(name = "gladdrreg_road_location", columnList = RoadData.DB_FIELD_LOCATION + DatabaseEntry.REF),
        @Index(name = "gladdrreg_road_municipality", columnList = RoadData.DB_FIELD_MUNICIPALITY + DatabaseEntry.REF)
})
public class RoadData extends SumiffiikData<RoadEffect, RoadData> {

    public static final String DB_FIELD_CODE = "code";
    public static final String IO_FIELD_CODE = "vejkode";

    @JsonProperty
    @XmlElement
    @Column(name = DB_FIELD_CODE)
    private int code;

    public int getCode() {
        return this.code;
    }


    public static final String DB_FIELD_NAME = "name";
    public static final String IO_FIELD_NAME = "navn";

    @JsonProperty
    @XmlElement
    @Column(name = DB_FIELD_NAME)
    private String name;

    public String getName() {
        return this.name;
    }


    public static final String DB_FIELD_SHORTNAME = "shortName";
    public static final String IO_FIELD_SHORTNAME = "kortNavn";

    @JsonProperty("shortname")
    @XmlElement
    @Column(name = DB_FIELD_SHORTNAME)
    private String shortName;

    public String getShortName() {
        return this.shortName;
    }


    public static final String DB_FIELD_ALTNAME = "alternateName";
    public static final String IO_FIELD_ALTNAME = "alternativtNavn";

    @JsonProperty("alternate_name")
    @XmlElement
    @Column(name = DB_FIELD_ALTNAME)
    private String alternateName;

    public String getAlternateName() {
        return this.alternateName;
    }


    public static final String DB_FIELD_CPRNAME = "cprName";
    public static final String IO_FIELD_CPRNAME = "cprNavn";

    @JsonProperty("cpr_name")
    @XmlElement
    @Column(name = DB_FIELD_CPRNAME)
    private String cprName;

    public String getCprName() {
        return this.cprName;
    }


    public static final String DB_FIELD_LOCATION = "location";
    public static final String IO_FIELD_LOCATION = "lokation";

    @JsonProperty
    @XmlElement
    @ManyToOne
    @JoinColumn(name = DB_FIELD_LOCATION + DatabaseEntry.REF)
    private Identification location;


    public static final String DB_FIELD_MUNICIPALITY = "municipality";
    public static final String IO_FIELD_MUNICIPALITY = "kommune";

    public Identification getLocation() {
        return this.location;
    }

    @JsonProperty
    @XmlElement
    @ManyToOne
    @JoinColumn(name = DB_FIELD_MUNICIPALITY + DatabaseEntry.REF)
    private Identification municipality;

    public Identification getMunicipality() {
        return this.municipality;
    }


    @Override
    public Map<String, Object> asMap() {
        HashMap<String, Object> map = new HashMap<>(super.asMap());
        map.put("code", this.code);
        map.put("name", this.name);
        map.put("shortName", this.shortName);
        map.put("alternateName", this.alternateName);
        map.put("cprName", this.cprName);
        return map;
    }


    @Override
    @JsonIgnore
    public HashMap<String, Identification> getReferences() {
        HashMap<String, Identification> references = super.getReferences();
        references.put("municipality", this.municipality);
        references.put("location", this.location);
        return references;
    }

    @Override
    public void updateReferences(HashMap<String, Identification> references) {
        super.updateReferences(references);
        if (references.containsKey("municipality")) {
            this.municipality = references.get("municipality");
        }
        if (references.containsKey("location")) {
            this.location = references.get("location");
        }
    }

}
