package dk.magenta.datafordeler.geo.data.building;

import com.vividsolutions.jts.geom.MultiPolygon;
import dk.magenta.datafordeler.core.database.DatabaseEntry;
import dk.magenta.datafordeler.geo.GeoPlugin;
import dk.magenta.datafordeler.geo.data.common.AreaRecord;

import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

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
