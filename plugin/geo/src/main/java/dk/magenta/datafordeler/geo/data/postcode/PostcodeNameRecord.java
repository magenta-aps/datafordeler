package dk.magenta.datafordeler.geo.data.postcode;

import dk.magenta.datafordeler.core.database.DatabaseEntry;
import dk.magenta.datafordeler.geo.GeoPlugin;
import dk.magenta.datafordeler.geo.data.common.NameRecord;

import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

@Entity
@Table(name = GeoPlugin.DEBUG_TABLE_PREFIX + PostcodeNameRecord.TABLE_NAME, indexes = {
        @Index(
                name = GeoPlugin.DEBUG_TABLE_PREFIX + PostcodeNameRecord.TABLE_NAME + PostcodeNameRecord.DB_FIELD_ENTITY,
                columnList = PostcodeNameRecord.DB_FIELD_ENTITY + DatabaseEntry.REF
        ),
        @Index(
                name = GeoPlugin.DEBUG_TABLE_PREFIX + PostcodeNameRecord.TABLE_NAME + PostcodeNameRecord.DB_FIELD_NAME,
                columnList = PostcodeNameRecord.DB_FIELD_NAME
        ),
})
public class PostcodeNameRecord extends NameRecord<PostcodeEntity> {

    public static final String TABLE_NAME = "geo_postcode_name";

    public PostcodeNameRecord() {
    }

    public PostcodeNameRecord(String name) {
        super(name);
    }

}
