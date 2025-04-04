package dk.magenta.datafordeler.geo.data.road;

import com.vividsolutions.jts.geom.MultiLineString;
import dk.magenta.datafordeler.core.database.DatabaseEntry;
import dk.magenta.datafordeler.geo.GeoPlugin;
import dk.magenta.datafordeler.geo.data.common.LineRecord;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

@Entity
@Table(name = GeoPlugin.DEBUG_TABLE_PREFIX + RoadShapeRecord.TABLE_NAME, indexes = {
        @Index(
                name = GeoPlugin.DEBUG_TABLE_PREFIX + RoadShapeRecord.TABLE_NAME + RoadShapeRecord.DB_FIELD_ENTITY,
                columnList = RoadShapeRecord.DB_FIELD_ENTITY + DatabaseEntry.REF
        ),
})
public class RoadShapeRecord extends LineRecord<GeoRoadEntity> {

    public static final String TABLE_NAME = "geo_road_shape";

    public RoadShapeRecord() {
    }

    public RoadShapeRecord(double length, MultiLineString shape) {
        super(length, shape);
    }

    public RoadShapeRecord(double length, org.geojson.MultiLineString shape) {
        super(length, shape);
    }
}
