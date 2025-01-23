package dk.magenta.datafordeler.geo.data.unitaddress;

import dk.magenta.datafordeler.core.database.DatabaseEntry;
import dk.magenta.datafordeler.geo.GeoPlugin;
import dk.magenta.datafordeler.geo.data.common.ObjectStatusRecord;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

@Entity
@Table(name = GeoPlugin.DEBUG_TABLE_PREFIX + UnitAddressStatusRecord.TABLE_NAME, indexes = {
        @Index(
                name = GeoPlugin.DEBUG_TABLE_PREFIX + UnitAddressStatusRecord.TABLE_NAME + UnitAddressStatusRecord.DB_FIELD_ENTITY,
                columnList = UnitAddressStatusRecord.DB_FIELD_ENTITY + DatabaseEntry.REF
        ),
})
public class UnitAddressStatusRecord extends ObjectStatusRecord<UnitAddressEntity> {

    public static final String TABLE_NAME = "geo_unit_address_status";

    public UnitAddressStatusRecord() {
    }

    public UnitAddressStatusRecord(Integer status) {
        super(status);
    }

}
