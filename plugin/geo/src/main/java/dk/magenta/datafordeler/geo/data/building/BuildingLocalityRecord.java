package dk.magenta.datafordeler.geo.data.building;

import dk.magenta.datafordeler.core.database.DatabaseEntry;
import dk.magenta.datafordeler.geo.GeoPlugin;
import dk.magenta.datafordeler.geo.data.common.LocalityReferenceRecord;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

import java.util.UUID;

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
        @Index(
                name = GeoPlugin.DEBUG_TABLE_PREFIX + BuildingLocalityRecord.TABLE_NAME + BuildingLocalityRecord.DB_FIELD_REFERENCE,
                columnList = BuildingLocalityRecord.DB_FIELD_REFERENCE + DatabaseEntry.REF
        )
})
public class BuildingLocalityRecord extends LocalityReferenceRecord<BuildingEntity> {

    public static final String TABLE_NAME = "geo_building_locality";

    public BuildingLocalityRecord() {
    }

    public BuildingLocalityRecord(String code) {
        super(code);
    }

    public BuildingLocalityRecord(UUID uuid) {
        super(uuid);
    }
}
