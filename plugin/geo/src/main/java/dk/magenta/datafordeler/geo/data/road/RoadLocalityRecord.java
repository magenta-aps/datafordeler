package dk.magenta.datafordeler.geo.data.road;

import dk.magenta.datafordeler.core.database.DatabaseEntry;
import dk.magenta.datafordeler.geo.GeoPlugin;
import dk.magenta.datafordeler.geo.data.common.LocalityReferenceRecord;

import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

@Entity
@Table(name = GeoPlugin.DEBUG_TABLE_PREFIX + RoadLocalityRecord.TABLE_NAME, indexes = {
        @Index(
                name = GeoPlugin.DEBUG_TABLE_PREFIX + RoadLocalityRecord.TABLE_NAME + RoadLocalityRecord.DB_FIELD_ENTITY,
                columnList = RoadLocalityRecord.DB_FIELD_ENTITY + DatabaseEntry.REF
        ),
        @Index(
                name = GeoPlugin.DEBUG_TABLE_PREFIX + RoadLocalityRecord.TABLE_NAME + RoadLocalityRecord.DB_FIELD_CODE,
                columnList = RoadLocalityRecord.DB_FIELD_CODE
        ),
        @Index(
                name = GeoPlugin.DEBUG_TABLE_PREFIX + RoadLocalityRecord.TABLE_NAME + RoadLocalityRecord.DB_FIELD_REFERENCE,
                columnList = RoadLocalityRecord.DB_FIELD_REFERENCE + DatabaseEntry.REF
        )
})
public class RoadLocalityRecord extends LocalityReferenceRecord<GeoRoadEntity> {

    public static final String TABLE_NAME = "geo_road_locality";

    public RoadLocalityRecord() {
    }

    public RoadLocalityRecord(String code) {
        super(code);
    }

}
