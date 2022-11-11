package dk.magenta.datafordeler.geo.data.accessaddress;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import dk.magenta.datafordeler.core.database.DatabaseEntry;
import dk.magenta.datafordeler.core.database.Identification;
import dk.magenta.datafordeler.core.exception.InvalidClientInputException;
import dk.magenta.datafordeler.geo.GeoPlugin;
import dk.magenta.datafordeler.geo.data.WireCache;
import dk.magenta.datafordeler.geo.data.common.GeoMonotemporalRecord;
import dk.magenta.datafordeler.geo.data.road.GeoRoadEntity;
import org.hibernate.Session;

import javax.persistence.*;
import java.util.Objects;

@Entity
@Table(name = GeoPlugin.DEBUG_TABLE_PREFIX + AccessAddressRoadRecord.TABLE_NAME, indexes = {
        @Index(
                name = GeoPlugin.DEBUG_TABLE_PREFIX + AccessAddressRoadRecord.TABLE_NAME + AccessAddressRoadRecord.DB_FIELD_ENTITY,
                columnList = AccessAddressRoadRecord.DB_FIELD_ENTITY + DatabaseEntry.REF
        ),
        @Index(
                name = GeoPlugin.DEBUG_TABLE_PREFIX + AccessAddressRoadRecord.TABLE_NAME + AccessAddressRoadRecord.DB_FIELD_ROAD_CODE,
                columnList = AccessAddressRoadRecord.DB_FIELD_ROAD_CODE
        ),
        @Index(
                name = GeoPlugin.DEBUG_TABLE_PREFIX + AccessAddressRoadRecord.TABLE_NAME + AccessAddressRoadRecord.DB_FIELD_MUNICIPALITY_CODE,
                columnList = AccessAddressRoadRecord.DB_FIELD_MUNICIPALITY_CODE
        ),
        @Index(
                name = GeoPlugin.DEBUG_TABLE_PREFIX + AccessAddressRoadRecord.TABLE_NAME + AccessAddressRoadRecord.DB_FIELD_MUNICIPALITY_CODE + AccessAddressRoadRecord.DB_FIELD_ROAD_CODE,
                columnList = AccessAddressRoadRecord.DB_FIELD_MUNICIPALITY_CODE + "," + AccessAddressRoadRecord.DB_FIELD_ROAD_CODE
        )
})
public class AccessAddressRoadRecord extends GeoMonotemporalRecord<AccessAddressEntity> {

    public static final String TABLE_NAME = "geo_access_address_road";

    public AccessAddressRoadRecord() {
    }

    public AccessAddressRoadRecord(Integer municipalityCode, Integer roadCode) {
        this.municipalityCode = municipalityCode;
        this.roadCode = roadCode;
    }


    public static final String DB_FIELD_MUNICIPALITY_CODE = "municipalityCode";
    public static final String IO_FIELD_MUNICIPALITY_CODE = "kommunekode";
    @Column(name = DB_FIELD_MUNICIPALITY_CODE, nullable = true)
    @JsonProperty(value = IO_FIELD_MUNICIPALITY_CODE)
    private Integer municipalityCode;

    public Integer getMunicipalityCode() {
        return this.municipalityCode;
    }

    public void setMunicipalityCode(Integer municipalityCode) {
        this.municipalityCode = municipalityCode;
    }


    public static final String DB_FIELD_ROAD_CODE = "roadCode";
    public static final String IO_FIELD_ROAD_CODE = "vejkode";
    @Column(name = DB_FIELD_ROAD_CODE)
    @JsonProperty(value = IO_FIELD_ROAD_CODE)
    private Integer roadCode;

    public Integer getRoadCode() {
        return this.roadCode;
    }

    public void setRoadCode(Integer roadCode) {
        this.roadCode = roadCode;
    }


    public static final String DB_FIELD_ROAD_REFERENCE = "reference";
    public static final String IO_FIELD_ROAD_REFERENCE = "reference";
    @ManyToOne
    @JsonIgnore
    private Identification reference;

    public Identification getReference() {
        return this.reference;
    }


    public void wire(Session session, WireCache wireCache) {
        if (this.reference == null && this.municipalityCode != null && this.roadCode != null) {
            try {
                for (GeoRoadEntity road : wireCache.getRoad(session, this.municipalityCode, this.roadCode)) {
                    this.reference = road.getIdentification();
                    return;
                }
            } catch (InvalidClientInputException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public boolean equalData(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equalData(o)) return false;
        AccessAddressRoadRecord that = (AccessAddressRoadRecord) o;
        return Objects.equals(this.roadCode, that.roadCode);
    }
}
