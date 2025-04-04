package dk.magenta.datafordeler.gladdrreg.data.address;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
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
@Entity
@Table(name = "gladdrreg_address_data", indexes = {
        @Index(name = "gladdrreg_address_road", columnList = AddressData.DB_FIELD_ROAD + DatabaseEntry.REF),
        @Index(name = "gladdrreg_address_housenumber", columnList = AddressData.DB_FIELD_HOUSENUMBER),
        @Index(name = "gladdrreg_address_floor", columnList = AddressData.DB_FIELD_FLOOR),
        @Index(name = "gladdrreg_address_room", columnList = AddressData.DB_FIELD_ROOM),
        @Index(name = "gladdrreg_address_bnr", columnList = AddressData.DB_FIELD_BNUMBER + DatabaseEntry.REF)
})
public class AddressData extends SumiffiikData<AddressEffect, AddressData> {

    public static final String DB_FIELD_HOUSENUMBER = "houseNumber";
    public static final String IO_FIELD_HOUSENUMBER = "husnummer";

    @Column(name = DB_FIELD_HOUSENUMBER)
    @JsonProperty("house_number")
    @XmlElement
    private String houseNumber;

    public String getHouseNumber() {
        return this.houseNumber;
    }


    public static final String DB_FIELD_FLOOR = "floor";
    public static final String IO_FIELD_FLOOR = "etage";

    @JsonProperty
    @XmlElement
    @Column(name = DB_FIELD_FLOOR)
    private String floor;

    public String getFloor() {
        return this.floor;
    }


    public static final String DB_FIELD_ROOM = "room";
    public static final String IO_FIELD_ROOM = "værelse";

    @JsonProperty
    @XmlElement
    @Column(name = DB_FIELD_ROOM)
    private String room;

    public String getRoom() {
        return this.room;
    }


    public static final String DB_FIELD_BNUMBER = "bNumber";
    public static final String IO_FIELD_BNUMBER = "bNummer";

    @JsonProperty("b_number")
    @XmlElement
    @ManyToOne
    @JoinColumn(name = DB_FIELD_BNUMBER + DatabaseEntry.REF)
    private Identification bNumber;

    public Identification getbNumber() {
        return this.bNumber;
    }


    public static final String DB_FIELD_ROAD = "road";
    public static final String IO_FIELD_ROAD = "vej";

    @JsonProperty
    @XmlElement
    @ManyToOne
    @JoinColumn(name = DB_FIELD_ROAD + DatabaseEntry.REF)
    private Identification road;

    public Identification getRoad() {
        return this.road;
    }


    public static final String DB_FIELD_MUNICIPALITY = "municipality";
    public static final String IO_FIELD_MUNICIPALITY = "kommune";

    @JsonProperty
    @XmlElement
    @ManyToOne
    @JoinColumn(name = DB_FIELD_MUNICIPALITY + DatabaseEntry.REF)
    private Identification municipality;

    public Identification getMunicipality() {
        return this.municipality;
    }


    public static final String DB_FIELD_RESIDENCE = "residence";
    public static final String IO_FIELD_RESIDENCE = "bolig";

    @JsonProperty("residence")
    @Column(name = DB_FIELD_RESIDENCE, nullable = true)
    private Boolean residence;

    public Boolean getResidence() {
        return this.residence;
    }

    public void setResidence(Boolean residence) {
        this.residence = residence;
    }


    @Override
    public Map<String, Object> asMap() {
        HashMap<String, Object> map = new HashMap<>(super.asMap());
        map.put("houseNumber", this.houseNumber);
        map.put("floor", this.floor);
        map.put("room", this.room);
        map.put("bNumber", this.bNumber);
        map.put("road", this.road);
        map.put("municipality", this.municipality);
        map.put("residence", this.residence);
        return map;
    }


    @Override
    @JsonIgnore
    public HashMap<String, Identification> getReferences() {
        HashMap<String, Identification> references = super.getReferences();
        references.put("bNumber", this.bNumber);
        references.put("road", this.road);
        references.put("municipality", this.municipality);
        return references;
    }

    @Override
    public void updateReferences(HashMap<String, Identification> references) {
        super.updateReferences(references);
        if (references.containsKey("bNumber")) {
            this.bNumber = references.get("bNumber");
        }
        if (references.containsKey("road")) {
            this.road = references.get("road");
        }
        if (references.containsKey("municipality")) {
            this.municipality = references.get("municipality");
        }
    }

}
