package dk.magenta.datafordeler.geo.data.municipality;

import com.vividsolutions.jts.geom.MultiPolygon;
import dk.magenta.datafordeler.core.database.DatabaseEntry;
import dk.magenta.datafordeler.geo.GeoPlugin;
import dk.magenta.datafordeler.geo.data.common.AreaRecord;

import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

@Entity
@Table(name = GeoPlugin.DEBUG_TABLE_PREFIX + MunicipalityShapeRecord.TABLE_NAME, indexes = {
        @Index(
                name = GeoPlugin.DEBUG_TABLE_PREFIX + MunicipalityShapeRecord.TABLE_NAME + MunicipalityShapeRecord.DB_FIELD_ENTITY,
                columnList = MunicipalityShapeRecord.DB_FIELD_ENTITY + DatabaseEntry.REF
        )
})
public class MunicipalityShapeRecord extends AreaRecord<GeoMunicipalityEntity> {

    public static final String TABLE_NAME = "geo_municipality_shape";

    public MunicipalityShapeRecord() {
    }

    public MunicipalityShapeRecord(double area, double circumference, MultiPolygon shape) {
        super(area, circumference, shape);
    }

    public MunicipalityShapeRecord(double area, double circumference, org.geojson.MultiPolygon shape) {
        super(area, circumference, shape);
    }
}
