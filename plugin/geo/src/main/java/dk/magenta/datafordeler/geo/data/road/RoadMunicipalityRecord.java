package dk.magenta.datafordeler.geo.data.road;

import dk.magenta.datafordeler.core.database.DatabaseEntry;
import dk.magenta.datafordeler.geo.GeoPlugin;
import dk.magenta.datafordeler.geo.data.common.MunicipalityReferenceRecord;

import javax.persistence.Entity;
import javax.persistence.Index;
import javax.persistence.Table;

@Entity
@Table(name = GeoPlugin.DEBUG_TABLE_PREFIX + RoadMunicipalityRecord.TABLE_NAME, indexes = {
        @Index(
                name = GeoPlugin.DEBUG_TABLE_PREFIX + RoadMunicipalityRecord.TABLE_NAME + RoadMunicipalityRecord.DB_FIELD_ENTITY,
                columnList = RoadMunicipalityRecord.DB_FIELD_ENTITY + DatabaseEntry.REF
        ),
        @Index(
                name = GeoPlugin.DEBUG_TABLE_PREFIX + RoadMunicipalityRecord.TABLE_NAME + RoadMunicipalityRecord.DB_FIELD_CODE,
                columnList = RoadMunicipalityRecord.DB_FIELD_CODE
        )
})
public class RoadMunicipalityRecord extends MunicipalityReferenceRecord<RoadEntity> {

    public static final String TABLE_NAME = "geo_road_municipality";

    public RoadMunicipalityRecord() {
    }

    public RoadMunicipalityRecord(Integer code) {
        super(code);
    }

}
