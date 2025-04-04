package dk.magenta.datafordeler.geo.data.unitaddress;

import dk.magenta.datafordeler.core.database.DatabaseEntry;
import dk.magenta.datafordeler.geo.GeoPlugin;
import dk.magenta.datafordeler.geo.data.common.UsageRecord;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

@Entity
@Table(name = GeoPlugin.DEBUG_TABLE_PREFIX + UnitAddressUsageRecord.TABLE_NAME, indexes = {
        @Index(
                name = GeoPlugin.DEBUG_TABLE_PREFIX + UnitAddressUsageRecord.TABLE_NAME + UnitAddressUsageRecord.DB_FIELD_ENTITY,
                columnList = UnitAddressUsageRecord.DB_FIELD_ENTITY + DatabaseEntry.REF
        ),
        @Index(
                name = GeoPlugin.DEBUG_TABLE_PREFIX + UnitAddressUsageRecord.TABLE_NAME + UnitAddressUsageRecord.DB_FIELD_USAGE,
                columnList = UnitAddressUsageRecord.DB_FIELD_USAGE
        ),
})
public class UnitAddressUsageRecord extends UsageRecord<UnitAddressEntity> {

    public static final String TABLE_NAME = "geo_unit_address_usage";

    public UnitAddressUsageRecord() {
    }

    public UnitAddressUsageRecord(Integer usage) {
        super(usage);
    }

}
