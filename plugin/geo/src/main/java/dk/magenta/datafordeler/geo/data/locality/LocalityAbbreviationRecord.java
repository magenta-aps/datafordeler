package dk.magenta.datafordeler.geo.data.locality;

import dk.magenta.datafordeler.core.database.DatabaseEntry;
import dk.magenta.datafordeler.geo.GeoPlugin;
import dk.magenta.datafordeler.geo.data.common.NameRecord;

import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

@Entity
@Table(name = GeoPlugin.DEBUG_TABLE_PREFIX + LocalityAbbreviationRecord.TABLE_NAME, indexes = {
        @Index(
                name = GeoPlugin.DEBUG_TABLE_PREFIX + LocalityAbbreviationRecord.TABLE_NAME + LocalityAbbreviationRecord.DB_FIELD_ENTITY,
                columnList = LocalityAbbreviationRecord.DB_FIELD_ENTITY + DatabaseEntry.REF
        ),
        @Index(
                name = GeoPlugin.DEBUG_TABLE_PREFIX + LocalityAbbreviationRecord.TABLE_NAME + LocalityAbbreviationRecord.DB_FIELD_NAME,
                columnList = LocalityAbbreviationRecord.DB_FIELD_NAME
        ),
})
public class LocalityAbbreviationRecord extends NameRecord<GeoLocalityEntity> {

    public static final String TABLE_NAME = "geo_locality_abbreviation";

    public LocalityAbbreviationRecord() {
    }

    public LocalityAbbreviationRecord(String name) {
        super(name);
    }
}
