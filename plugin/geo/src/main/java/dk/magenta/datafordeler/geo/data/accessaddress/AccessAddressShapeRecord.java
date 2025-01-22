package dk.magenta.datafordeler.geo.data.accessaddress;

import com.vividsolutions.jts.geom.Point;
import dk.magenta.datafordeler.core.database.DatabaseEntry;
import dk.magenta.datafordeler.geo.GeoPlugin;
import dk.magenta.datafordeler.geo.data.common.PointRecord;

import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

@Entity
@Table(name = GeoPlugin.DEBUG_TABLE_PREFIX + AccessAddressShapeRecord.TABLE_NAME, indexes = {
        @Index(
                name = GeoPlugin.DEBUG_TABLE_PREFIX + AccessAddressShapeRecord.TABLE_NAME + AccessAddressShapeRecord.DB_FIELD_ENTITY,
                columnList = AccessAddressShapeRecord.DB_FIELD_ENTITY + DatabaseEntry.REF
        ),
})
public class AccessAddressShapeRecord extends PointRecord<AccessAddressEntity> {

    public static final String TABLE_NAME = "geo_access_address_shape";

    public AccessAddressShapeRecord() {
    }

    public AccessAddressShapeRecord(Point shape) {
        super(shape);
    }

    public AccessAddressShapeRecord(org.geojson.Point shape) {
        super(shape);
    }
}
