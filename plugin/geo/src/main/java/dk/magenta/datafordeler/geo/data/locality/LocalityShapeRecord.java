package dk.magenta.datafordeler.geo.data.locality;

import com.vividsolutions.jts.geom.MultiPolygon;
import dk.magenta.datafordeler.core.database.DatabaseEntry;
import dk.magenta.datafordeler.geo.GeoPlugin;
import dk.magenta.datafordeler.geo.data.common.AreaRecord;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

@Entity
@Table(name = GeoPlugin.DEBUG_TABLE_PREFIX + LocalityShapeRecord.TABLE_NAME, indexes = {
        @Index(
                name = GeoPlugin.DEBUG_TABLE_PREFIX + LocalityShapeRecord.TABLE_NAME + LocalityShapeRecord.DB_FIELD_ENTITY,
                columnList = LocalityShapeRecord.DB_FIELD_ENTITY + DatabaseEntry.REF
        ),
})
public class LocalityShapeRecord extends AreaRecord<GeoLocalityEntity> {

    public static final String TABLE_NAME = "geo_locality_shape";

    public LocalityShapeRecord() {
    }

    public LocalityShapeRecord(double area, double circumference, MultiPolygon shape) {
        super(area, circumference, shape);
    }

    public LocalityShapeRecord(double area, double circumference, org.geojson.MultiPolygon shape) {
        super(area, circumference, shape);
    }
}
