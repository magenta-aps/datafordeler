package dk.magenta.datafordeler.geo.data.building;

import dk.magenta.datafordeler.core.database.DatabaseEntry;
import dk.magenta.datafordeler.geo.GeoPlugin;
import dk.magenta.datafordeler.geo.data.common.LocalityReferenceRecord;

import javax.persistence.Entity;
import javax.persistence.Index;
import javax.persistence.Table;

@Entity
@Table(name = GeoPlugin.DEBUG_TABLE_PREFIX + BuildingLocalityRecord.TABLE_NAME, indexes = {
        @Index(
                name = GeoPlugin.DEBUG_TABLE_PREFIX + BuildingLocalityRecord.TABLE_NAME + BuildingLocalityRecord.DB_FIELD_ENTITY,
                columnList = BuildingLocalityRecord.DB_FIELD_ENTITY + DatabaseEntry.REF
        ),
        @Index(
                name = GeoPlugin.DEBUG_TABLE_PREFIX + BuildingLocalityRecord.TABLE_NAME + BuildingLocalityRecord.DB_FIELD_CODE,
                columnList = BuildingLocalityRecord.DB_FIELD_CODE
        ),
})
public class BuildingLocalityRecord extends LocalityReferenceRecord<BuildingEntity> {

    public static final String TABLE_NAME = "geo_building_locality";

    public BuildingLocalityRecord() {
    }

    public BuildingLocalityRecord(Integer code) {
        super(code);
    }

}
