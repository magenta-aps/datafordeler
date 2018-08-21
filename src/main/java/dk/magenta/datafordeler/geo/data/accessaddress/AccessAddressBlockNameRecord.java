package dk.magenta.datafordeler.geo.data.accessaddress;

import dk.magenta.datafordeler.core.database.DatabaseEntry;
import dk.magenta.datafordeler.geo.GeoPlugin;
import dk.magenta.datafordeler.geo.data.common.GeoMonotemporalRecord;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Index;
import javax.persistence.Table;
import java.util.Objects;

@Entity
@Table(name = GeoPlugin.DEBUG_TABLE_PREFIX + AccessAddressBlockNameRecord.TABLE_NAME, indexes = {
        @Index(
                name = GeoPlugin.DEBUG_TABLE_PREFIX + AccessAddressBlockNameRecord.TABLE_NAME + AccessAddressBlockNameRecord.DB_FIELD_ENTITY,
                columnList = AccessAddressBlockNameRecord.DB_FIELD_ENTITY + DatabaseEntry.REF
        ),
})
public class AccessAddressBlockNameRecord extends GeoMonotemporalRecord<AccessAddressEntity> {

    public static final String TABLE_NAME = "geo_access_address_block_name";

    public AccessAddressBlockNameRecord() {
    }

    public AccessAddressBlockNameRecord(String name) {
        this.name = name;
    }


    public static final String DB_FIELD_NAME = "name";
    @Column(name = DB_FIELD_NAME)
    private String name;

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean equalData(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equalData(o)) return false;
        AccessAddressBlockNameRecord that = (AccessAddressBlockNameRecord) o;
        return Objects.equals(this.name, that.name);
    }
}
