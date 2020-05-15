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
@Table(name = GeoPlugin.DEBUG_TABLE_PREFIX + AccessAddressNumberRecord.TABLE_NAME, indexes = {
        @Index(
                name = GeoPlugin.DEBUG_TABLE_PREFIX + AccessAddressNumberRecord.TABLE_NAME + AccessAddressNumberRecord.DB_FIELD_ENTITY,
                columnList = AccessAddressNumberRecord.DB_FIELD_ENTITY + DatabaseEntry.REF
        ),
        @Index(
                name = GeoPlugin.DEBUG_TABLE_PREFIX + AccessAddressNumberRecord.TABLE_NAME + AccessAddressNumberRecord.DB_FIELD_NUMBER,
                columnList = AccessAddressNumberRecord.DB_FIELD_NUMBER
        ),
})
public class AccessAddressNumberRecord extends GeoMonotemporalRecord<AccessAddressEntity> {

    public static final String TABLE_NAME = "geo_access_address_number";

    public AccessAddressNumberRecord() {
    }

    public AccessAddressNumberRecord(String number) {
        this.number = number;
    }


    public static final String DB_FIELD_NUMBER = "number";
    @Column(name = DB_FIELD_NUMBER)
    private String number;

    public String getNumber() {
        return this.number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public boolean equalData(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equalData(o)) return false;
        AccessAddressNumberRecord that = (AccessAddressNumberRecord) o;
        return Objects.equals(this.number, that.number);
    }
}
