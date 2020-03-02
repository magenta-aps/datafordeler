package dk.magenta.datafordeler.geo.data.locality;

import dk.magenta.datafordeler.core.database.DatabaseEntry;
import dk.magenta.datafordeler.geo.GeoPlugin;
import dk.magenta.datafordeler.geo.data.common.NameRecord;

import javax.persistence.Entity;
import javax.persistence.Index;
import javax.persistence.Table;

@Entity
@Table(name = GeoPlugin.DEBUG_TABLE_PREFIX + LocalityNameRecord.TABLE_NAME, indexes = {
        @Index(
                name = GeoPlugin.DEBUG_TABLE_PREFIX + LocalityNameRecord.TABLE_NAME + LocalityNameRecord.DB_FIELD_ENTITY,
                columnList = LocalityNameRecord.DB_FIELD_ENTITY + DatabaseEntry.REF
        ),
        @Index(
                name = GeoPlugin.DEBUG_TABLE_PREFIX + LocalityNameRecord.TABLE_NAME + LocalityNameRecord.DB_FIELD_NAME,
                columnList = LocalityNameRecord.DB_FIELD_NAME
        ),
})
public class LocalityNameRecord extends NameRecord<LocalityEntity> {

    public static final String TABLE_NAME = "geo_locality_name";

    public LocalityNameRecord() {
    }

    public LocalityNameRecord(String name) {
        super(name);
    }
}
