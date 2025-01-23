package dk.magenta.datafordeler.geo.data.road;

import com.fasterxml.jackson.annotation.JsonProperty;
import dk.magenta.datafordeler.core.database.DatabaseEntry;
import dk.magenta.datafordeler.geo.GeoPlugin;
import dk.magenta.datafordeler.geo.data.common.NameRecord;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

@Entity
@Table(name = GeoPlugin.DEBUG_TABLE_PREFIX + RoadNameRecord.TABLE_NAME, indexes = {
        @Index(
                name = GeoPlugin.DEBUG_TABLE_PREFIX + RoadNameRecord.TABLE_NAME + RoadNameRecord.DB_FIELD_ENTITY,
                columnList = RoadNameRecord.DB_FIELD_ENTITY + DatabaseEntry.REF
        ),
        @Index(
                name = GeoPlugin.DEBUG_TABLE_PREFIX + RoadNameRecord.TABLE_NAME + RoadNameRecord.DB_FIELD_NAME,
                columnList = RoadNameRecord.DB_FIELD_NAME
        ),
        @Index(
                name = GeoPlugin.DEBUG_TABLE_PREFIX + RoadNameRecord.TABLE_NAME + RoadNameRecord.DB_FIELD_ADDRESSING_NAME,
                columnList = RoadNameRecord.DB_FIELD_ADDRESSING_NAME
        ),
})
public class RoadNameRecord extends NameRecord<GeoRoadEntity> {

    public static final String TABLE_NAME = "geo_road_name";

    public RoadNameRecord() {
    }

    public RoadNameRecord(String name, String addressingName) {
        super(name);
        this.addressingName = addressingName;
    }


    public static final String DB_FIELD_ADDRESSING_NAME = "addressingName";
    public static final String IO_FIELD_ADDRESSING_NAME = "addresseringsNavn";
    @Column(name = DB_FIELD_ADDRESSING_NAME)
    @JsonProperty(IO_FIELD_ADDRESSING_NAME)
    private String addressingName;

    public String getAddressingName() {
        return this.addressingName;
    }

    public void setAddressingName(String addressingName) {
        this.addressingName = addressingName;
    }
}
