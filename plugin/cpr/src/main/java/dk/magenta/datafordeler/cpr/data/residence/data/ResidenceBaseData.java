package dk.magenta.datafordeler.cpr.data.residence.data;

import com.fasterxml.jackson.annotation.JsonProperty;
import dk.magenta.datafordeler.core.database.DataItem;
import dk.magenta.datafordeler.cpr.CprPlugin;
import dk.magenta.datafordeler.cpr.data.CprData;
import dk.magenta.datafordeler.cpr.data.residence.ResidenceEffect;
import org.hibernate.Session;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import javax.xml.bind.annotation.XmlElement;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Base class for Residence data, linking to Effects and delegating storage to referred classes
 */
@Entity
@Table(name = CprPlugin.DEBUG_TABLE_PREFIX + "cpr_residence_data", indexes = {
        @Index(name = CprPlugin.DEBUG_TABLE_PREFIX + "cpr_residence_lastUpdated", columnList = DataItem.DB_FIELD_LAST_UPDATED),
        @Index(name = CprPlugin.DEBUG_TABLE_PREFIX + "cpr_residence_municipality_code", columnList = ResidenceBaseData.DB_FIELD_MUNICIPALITY_CODE),
        @Index(name = CprPlugin.DEBUG_TABLE_PREFIX + "cpr_residence_road_code", columnList = ResidenceBaseData.DB_FIELD_ROAD_CODE),
        @Index(name = CprPlugin.DEBUG_TABLE_PREFIX + "cpr_residence_housenumber", columnList = ResidenceBaseData.DB_FIELD_HOUSENUMBER),
        @Index(name = CprPlugin.DEBUG_TABLE_PREFIX + "cpr_residence_floor", columnList = ResidenceBaseData.DB_FIELD_FLOOR),
        @Index(name = CprPlugin.DEBUG_TABLE_PREFIX + "cpr_residence_door", columnList = ResidenceBaseData.DB_FIELD_DOOR),
})
public class ResidenceBaseData extends CprData<ResidenceEffect, ResidenceBaseData> {

    public static final String DB_FIELD_DAFO_UPDATED = "dafoUpdated";
    public static final String IO_FIELD_DAFO_UPDATED = "dafoOpdateret";

    @Column(name = DB_FIELD_DAFO_UPDATED)
    private OffsetDateTime dafoUpdated = null;

    @JsonProperty(value = IO_FIELD_DAFO_UPDATED)
    public OffsetDateTime getDafoUpdated() {
        return this.dafoUpdated;
    }

    public void setDafoUpdated(OffsetDateTime dafoUpdated) {
        this.dafoUpdated = dafoUpdated;
    }


    public static final String DB_FIELD_MUNICIPALITY_CODE = "municipalityCode";
    public static final String IO_FIELD_MUNICIPALITY_CODE = "kommunekode";
    @Column(name = DB_FIELD_MUNICIPALITY_CODE)
    @JsonProperty(value = IO_FIELD_MUNICIPALITY_CODE)
    @XmlElement(name = IO_FIELD_MUNICIPALITY_CODE)
    private int municipalityCode;

    public int getKommunekode() {
        return this.municipalityCode;
    }

    public void setKommunekode(int kommunekode, OffsetDateTime updateTime) {
        this.municipalityCode = kommunekode;
        this.setDafoUpdated(updateTime);
    }

    public static final String DB_FIELD_ROAD_CODE = "roadCode";
    public static final String IO_FIELD_ROAD_CODE = "vejkode";
    @Column(name = DB_FIELD_ROAD_CODE)
    @JsonProperty(value = IO_FIELD_ROAD_CODE)
    @XmlElement(name = IO_FIELD_ROAD_CODE)
    private int roadCode;

    public int getVejkode() {
        return this.roadCode;
    }

    public void setVejkode(int vejkode, OffsetDateTime updateTime) {
        this.roadCode = vejkode;
        this.setDafoUpdated(updateTime);
    }

    public static final String DB_FIELD_HOUSENUMBER = "houseNumber";
    public static final String IO_FIELD_HOUSENUMBER = "husnummer";
    @Column(name = DB_FIELD_HOUSENUMBER)
    @JsonProperty(value = IO_FIELD_HOUSENUMBER)
    @XmlElement(name = IO_FIELD_HOUSENUMBER)
    private String houseNumber;

    public String getHusnummer() {
        return this.houseNumber;
    }

    public void setHusnummer(String husnummer, OffsetDateTime updateTime) {
        this.houseNumber = husnummer;
        this.setDafoUpdated(updateTime);
    }

    public static final String DB_FIELD_FLOOR = "floor";
    public static final String IO_FIELD_FLOOR = "etage";
    @Column(name = DB_FIELD_FLOOR)
    @JsonProperty(value = IO_FIELD_FLOOR)
    @XmlElement(name = IO_FIELD_FLOOR)
    private String floor;

    public String getEtage() {
        return this.floor;
    }

    public void setEtage(String etage, OffsetDateTime updateTime) {
        this.floor = etage;
        this.setDafoUpdated(updateTime);
    }

    public static final String DB_FIELD_DOOR = "door";
    public static final String IO_FIELD_DOOR = "sidedør";
    @Column(name = DB_FIELD_DOOR)
    @JsonProperty(value = IO_FIELD_DOOR)
    @XmlElement(name = IO_FIELD_DOOR)
    private String door;

    public String getSideDoer() {
        return this.door;
    }

    public void setSideDoer(String sideDoer, OffsetDateTime updateTime) {
        this.door = sideDoer;
        this.setDafoUpdated(updateTime);
    }

    public static final String DB_FIELD_LOCALITY = "locality";
    public static final String IO_FIELD_LOCALITY = "lokalitet";
    @Column(name = DB_FIELD_LOCALITY)
    @JsonProperty(value = IO_FIELD_LOCALITY)
    @XmlElement(name = IO_FIELD_LOCALITY)
    private String locality;

    public String getLokalitet() {
        return this.locality;
    }

    public void setLokalitet(String lokalitet, OffsetDateTime updateTime) {
        this.locality = lokalitet;
        this.setDafoUpdated(updateTime);
    }

    @Override
    public Map<String, Object> asMap() {
        HashMap<String, Object> map = new HashMap<>();
        map.put(IO_FIELD_MUNICIPALITY_CODE, this.municipalityCode);
        map.put(IO_FIELD_ROAD_CODE, this.roadCode);
        map.put(IO_FIELD_HOUSENUMBER, this.houseNumber);
        map.put(IO_FIELD_FLOOR, this.floor);
        map.put(IO_FIELD_DOOR, this.door);
        map.put(IO_FIELD_LOCALITY, this.locality);
        return map;
    }

    @Override
    public void forceLoad(Session session) {

    }

    @Override
    public ResidenceBaseData clone() {
        ResidenceBaseData clone = new ResidenceBaseData();
        clone.municipalityCode = this.municipalityCode;
        clone.locality = this.locality;
        clone.roadCode = this.roadCode;
        clone.houseNumber = this.houseNumber;
        clone.floor = this.floor;
        clone.door = this.door;
        clone.dafoUpdated = this.dafoUpdated;
        return clone;
    }
}
