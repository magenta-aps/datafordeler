package dk.magenta.datafordeler.geo.data.postcode;

import com.vividsolutions.jts.geom.MultiPolygon;
import dk.magenta.datafordeler.core.database.DatabaseEntry;
import dk.magenta.datafordeler.geo.GeoPlugin;
import dk.magenta.datafordeler.geo.data.common.AreaRecord;

import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

@Entity
@Table(name = GeoPlugin.DEBUG_TABLE_PREFIX + PostcodeShapeRecord.TABLE_NAME, indexes = {
        @Index(
                name = GeoPlugin.DEBUG_TABLE_PREFIX + PostcodeShapeRecord.TABLE_NAME + PostcodeShapeRecord.DB_FIELD_ENTITY,
                columnList = PostcodeShapeRecord.DB_FIELD_ENTITY + DatabaseEntry.REF
        )
})
public class PostcodeShapeRecord extends AreaRecord<PostcodeEntity> {

    public static final String TABLE_NAME = "geo_postcode_shape";

    public PostcodeShapeRecord() {
    }

    public PostcodeShapeRecord(double area, double circumference, MultiPolygon shape) {
        super(area, circumference, shape);
    }

    public PostcodeShapeRecord(double area, double circumference, org.geojson.MultiPolygon shape) {
        super(area, circumference, shape);
    }
}
