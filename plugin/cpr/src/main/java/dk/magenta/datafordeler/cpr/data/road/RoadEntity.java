package dk.magenta.datafordeler.cpr.data.road;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import dk.magenta.datafordeler.core.database.Identification;
import dk.magenta.datafordeler.cpr.CprPlugin;
import dk.magenta.datafordeler.cpr.data.CprEntity;

import javax.persistence.Column;
import javax.persistence.Index;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import java.util.UUID;

import static dk.magenta.datafordeler.cpr.data.road.RoadEntity.DB_FIELD_MUNICIPALITYCODE;
import static dk.magenta.datafordeler.cpr.data.road.RoadEntity.DB_FIELD_ROADCODE;

/**
 * An Entity representing a road. Bitemporal data is structured as described
 * in {@link dk.magenta.datafordeler.core.database.Entity}
 */
@javax.persistence.Entity
@Table(
    name= "cpr_road_entity",
    indexes = {
            @Index(name = CprPlugin.DEBUG_TABLE_PREFIX + "cpr_road_identification", columnList = "identification_id", unique = true),
            @Index(name = CprPlugin.DEBUG_TABLE_PREFIX + "cpr_road_municipality_code", columnList = DB_FIELD_MUNICIPALITYCODE),
            @Index(name = CprPlugin.DEBUG_TABLE_PREFIX + "cpr_road_code", columnList = DB_FIELD_ROADCODE)
    }
)
@XmlAccessorType(XmlAccessType.FIELD)
public class RoadEntity extends CprEntity<RoadEntity, RoadRegistration> {

    public RoadEntity() {
    }

    public RoadEntity(Identification identification) {
        super(identification);
    }

    public RoadEntity(UUID uuid, String domain) {
        super(uuid, domain);
    }

    @Override
    protected RoadRegistration createEmptyRegistration() {
        return new RoadRegistration();
    }

    @JsonTypeInfo(use=JsonTypeInfo.Id.CLASS, include=JsonTypeInfo.As.PROPERTY, property="type")
    public static final String schema = "Vej";



    public static final String DB_FIELD_MUNICIPALITYCODE = "municipalityCode";
    public static final String IO_FIELD_MUNICIPALITYCODE = "kommunekode";

    @Column(name = DB_FIELD_MUNICIPALITYCODE)
    @JsonProperty(value = IO_FIELD_MUNICIPALITYCODE)
    @XmlElement(name = IO_FIELD_MUNICIPALITYCODE)
    private int municipalityCode;

    public int getKommunekode() {
        return this.municipalityCode;
    }

    public void setKommunekode(int kommunekode) {
        this.municipalityCode = kommunekode;
    }


    public static final String DB_FIELD_ROADCODE = "roadCode";
    public static final String IO_FIELD_ROADCODE = "vejkode";
    @Column(name = DB_FIELD_ROADCODE)
    @JsonProperty(value = IO_FIELD_ROADCODE)
    @XmlElement(name = IO_FIELD_ROADCODE)
    private int roadCode;

    public int getVejkode() {
        return this.roadCode;
    }

    public void setVejkode(int vejkode) {
        this.roadCode = vejkode;
    }

    public static UUID generateUUID(int municipalityCode, int roadCode) {
        String uuidInput = "road:"+municipalityCode+"/"+roadCode;
        return UUID.nameUUIDFromBytes(uuidInput.getBytes());
    }
}
