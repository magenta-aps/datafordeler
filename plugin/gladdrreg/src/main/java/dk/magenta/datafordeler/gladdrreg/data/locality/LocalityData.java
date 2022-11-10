package dk.magenta.datafordeler.gladdrreg.data.locality;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import dk.magenta.datafordeler.core.database.DatabaseEntry;
import dk.magenta.datafordeler.core.database.Identification;
import dk.magenta.datafordeler.gladdrreg.data.SumiffiikData;

import javax.persistence.*;
import javax.xml.bind.annotation.XmlElement;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by lars on 16-05-17.
 */
@javax.persistence.Entity
@Table(name = "gladdrreg_locality_data", indexes = {
        @Index(name = "gladdrreg_locality_code", columnList = LocalityData.DB_FIELD_CODE),
        @Index(name = "gladdrreg_locality_abbrev", columnList = LocalityData.DB_FIELD_ABBREV),
        @Index(name = "gladdrreg_locality_name", columnList = LocalityData.DB_FIELD_NAME),
        @Index(name = "gladdrreg_locality_type", columnList = LocalityData.DB_FIELD_TYPE),
        @Index(name = "gladdrreg_locality_localityState", columnList = LocalityData.DB_FIELD_STATE),
        @Index(name = "gladdrreg_locality_municipality", columnList = LocalityData.DB_FIELD_MUNICIPALITY + DatabaseEntry.REF),
})
public class LocalityData extends SumiffiikData<LocalityEffect, LocalityData> {

    public static final String DB_FIELD_CODE = "code";
    public static final String IO_FIELD_CODE = "lokalitetskode";

    @JsonProperty
    @XmlElement
    @Column(name = DB_FIELD_CODE)
    private int code;

    public int getCode() {
        return this.code;
    }


    public static final String DB_FIELD_ABBREV = "abbrev";
    public static final String IO_FIELD_ABBREV = "forkortelse";

    @JsonProperty
    @XmlElement
    @Column(name = DB_FIELD_ABBREV)
    private String abbrev;

    public String getAbbrev() {
        return this.abbrev;
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


    public static final String DB_FIELD_TYPE = "type";
    public static final String IO_FIELD_TYPE = "type";

    @JsonProperty
    @XmlElement
    @Column(name = DB_FIELD_TYPE)
    private int type;

    public int getType() {
        return this.type;
    }


    public static final String DB_FIELD_STATE = "localityState";
    public static final String IO_FIELD_STATE = "tilstand";

    @JsonProperty("locality_state")
    @XmlElement
    @Column(name = DB_FIELD_STATE)
    private int localityState;

    public int getLocalityState() {
        return this.localityState;
    }


    public static final String DB_FIELD_MUNICIPALITY = "municipality";
    public static final String IO_FIELD_MUNICIPALITY = "kommune";

    @JsonProperty(required = false)
    @XmlElement
    @ManyToOne
    @JoinColumn(name = DB_FIELD_MUNICIPALITY + DatabaseEntry.REF)
    private Identification municipality;

    public Identification getMunicipality() {
        return this.municipality;
    }


    public static final String DB_FIELD_DISTRICT = "district";
    public static final String IO_FIELD_DISTRICT = "distrikt";

    @JsonProperty(required = false)
    @XmlElement
    @ManyToOne
    @JoinColumn(name = DB_FIELD_DISTRICT + DatabaseEntry.REF)
    private Identification district;

    public Identification getDistrict() {
        return this.district;
    }


    public static final String DB_FIELD_POSTALCODE = "postalCode";
    public static final String IO_FIELD_POSTALCODE = "postnummer";

    @JsonProperty(value = "postal_code", required = false)
    @XmlElement
    @ManyToOne
    @JoinColumn(name = DB_FIELD_POSTALCODE + DatabaseEntry.REF)
    private Identification postalCode;

    public Identification getPostalCode() {
        return this.postalCode;
    }


    @Override
    public Map<String, Object> asMap() {
        HashMap<String, Object> map = new HashMap<>(super.asMap());
        map.put("code", this.code);
        map.put("abbrev", this.abbrev);
        map.put("name", this.name);
        map.put("type", this.type);
        map.put("localityState", this.localityState);
        return map;
    }

    @Override
    @JsonIgnore
    public HashMap<String, Identification> getReferences() {
        HashMap<String, Identification> references = super.getReferences();
        references.put("municipality", this.municipality);
        references.put("district", this.district);
        references.put("postalCode", this.postalCode);
        return references;
    }

    @Override
    public void updateReferences(HashMap<String, Identification> references) {
        super.updateReferences(references);
        if (references.containsKey("municipality")) {
            this.municipality = references.get("municipality");
        }
        if (references.containsKey("district")) {
            this.district = references.get("district");
        }
        if (references.containsKey("postalCode")) {
            this.postalCode = references.get("postalCode");
        }
    }

}
