package dk.magenta.datafordeler.geo.data.building;

import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.MultiPolygon;
import dk.magenta.datafordeler.core.database.DatabaseEntry;
import dk.magenta.datafordeler.geo.GeoPlugin;
import dk.magenta.datafordeler.geo.data.common.AreaRecord;
import dk.magenta.datafordeler.geo.data.common.LineRecord;

import javax.persistence.Entity;
import javax.persistence.Index;
import javax.persistence.Table;

@Entity
@Table(name = GeoPlugin.DEBUG_TABLE_PREFIX + BuildingShapeRecord.TABLE_NAME, indexes = {
        @Index(
                name = GeoPlugin.DEBUG_TABLE_PREFIX + BuildingShapeRecord.TABLE_NAME + BuildingShapeRecord.DB_FIELD_ENTITY,
                columnList = BuildingShapeRecord.DB_FIELD_ENTITY + DatabaseEntry.REF
        ),
})
public class BuildingShapeRecord extends AreaRecord<BuildingEntity> {

    public static final String TABLE_NAME = "geo_building_shape";

    public BuildingShapeRecord() {
    }

    public BuildingShapeRecord(double area, double length, MultiPolygon shape) {
        super(area, length, shape);
    }

    public BuildingShapeRecord(double area, double length, org.geojson.MultiPolygon shape) {
        super(area, length, shape);
    }
}
