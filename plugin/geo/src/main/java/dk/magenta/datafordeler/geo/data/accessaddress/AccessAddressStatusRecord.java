package dk.magenta.datafordeler.geo.data.accessaddress;

import dk.magenta.datafordeler.core.database.DatabaseEntry;
import dk.magenta.datafordeler.geo.GeoPlugin;
import dk.magenta.datafordeler.geo.data.common.ObjectStatusRecord;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

@Entity
@Table(name = GeoPlugin.DEBUG_TABLE_PREFIX + AccessAddressStatusRecord.TABLE_NAME, indexes = {
        @Index(
                name = GeoPlugin.DEBUG_TABLE_PREFIX + AccessAddressStatusRecord.TABLE_NAME + AccessAddressStatusRecord.DB_FIELD_ENTITY,
                columnList = AccessAddressStatusRecord.DB_FIELD_ENTITY + DatabaseEntry.REF
        ),
})
public class AccessAddressStatusRecord extends ObjectStatusRecord<AccessAddressEntity> {

    public static final String TABLE_NAME = "geo_access_address_status";

    public AccessAddressStatusRecord() {
    }

    public AccessAddressStatusRecord(Integer status) {
        super(status);
    }

}
