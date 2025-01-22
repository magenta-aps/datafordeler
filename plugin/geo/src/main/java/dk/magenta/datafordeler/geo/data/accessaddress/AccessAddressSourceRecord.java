package dk.magenta.datafordeler.geo.data.accessaddress;

import dk.magenta.datafordeler.core.database.DatabaseEntry;
import dk.magenta.datafordeler.geo.GeoPlugin;
import dk.magenta.datafordeler.geo.data.common.SourceRecord;

import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

@Entity
@Table(name = GeoPlugin.DEBUG_TABLE_PREFIX + AccessAddressSourceRecord.TABLE_NAME, indexes = {
        @Index(
                name = GeoPlugin.DEBUG_TABLE_PREFIX + AccessAddressSourceRecord.TABLE_NAME + AccessAddressSourceRecord.DB_FIELD_ENTITY,
                columnList = AccessAddressSourceRecord.DB_FIELD_ENTITY + DatabaseEntry.REF
        ),
})
public class AccessAddressSourceRecord extends SourceRecord<AccessAddressEntity> {

    public static final String TABLE_NAME = "geo_access_address_source";

    public AccessAddressSourceRecord() {
    }

    public AccessAddressSourceRecord(Integer source) {
        super(source);
    }

}
