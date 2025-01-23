package dk.magenta.datafordeler.geo.data.accessaddress;

import dk.magenta.datafordeler.core.database.DatabaseEntry;
import dk.magenta.datafordeler.geo.GeoPlugin;
import dk.magenta.datafordeler.geo.data.common.PostcodeReferenceRecord;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

@Entity
@Table(name = GeoPlugin.DEBUG_TABLE_PREFIX + AccessAddressPostcodeRecord.TABLE_NAME, indexes = {
        @Index(
                name = GeoPlugin.DEBUG_TABLE_PREFIX + AccessAddressPostcodeRecord.TABLE_NAME + AccessAddressPostcodeRecord.DB_FIELD_ENTITY,
                columnList = AccessAddressPostcodeRecord.DB_FIELD_ENTITY + DatabaseEntry.REF
        ),
        @Index(
                name = GeoPlugin.DEBUG_TABLE_PREFIX + AccessAddressPostcodeRecord.TABLE_NAME + AccessAddressPostcodeRecord.DB_FIELD_CODE,
                columnList = AccessAddressPostcodeRecord.DB_FIELD_CODE
        ),
        @Index(
                name = GeoPlugin.DEBUG_TABLE_PREFIX + AccessAddressPostcodeRecord.TABLE_NAME + AccessAddressPostcodeRecord.DB_FIELD_REFERENCE,
                columnList = AccessAddressPostcodeRecord.DB_FIELD_REFERENCE + DatabaseEntry.REF
        )
})
public class AccessAddressPostcodeRecord extends PostcodeReferenceRecord<AccessAddressEntity> {

    public static final String TABLE_NAME = "geo_access_address_postcode";

    public AccessAddressPostcodeRecord() {
    }

    public AccessAddressPostcodeRecord(int code) {
        super(code);
    }

}
