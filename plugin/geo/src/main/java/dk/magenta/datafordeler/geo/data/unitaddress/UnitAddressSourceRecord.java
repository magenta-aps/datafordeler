package dk.magenta.datafordeler.geo.data.unitaddress;

import dk.magenta.datafordeler.core.database.DatabaseEntry;
import dk.magenta.datafordeler.geo.GeoPlugin;
import dk.magenta.datafordeler.geo.data.common.SourceRecord;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

@Entity
@Table(name = GeoPlugin.DEBUG_TABLE_PREFIX + UnitAddressSourceRecord.TABLE_NAME, indexes = {
        @Index(
                name = GeoPlugin.DEBUG_TABLE_PREFIX + UnitAddressSourceRecord.TABLE_NAME + UnitAddressSourceRecord.DB_FIELD_ENTITY,
                columnList = UnitAddressSourceRecord.DB_FIELD_ENTITY + DatabaseEntry.REF
        ),
})
public class UnitAddressSourceRecord extends SourceRecord<UnitAddressEntity> {

    public static final String TABLE_NAME = "geo_unit_address_source";

    public UnitAddressSourceRecord() {
    }

    public UnitAddressSourceRecord(Integer source) {
        super(source);
    }

}
