package dk.magenta.datafordeler.geo.data.accessaddress;

import com.fasterxml.jackson.annotation.JsonProperty;
import dk.magenta.datafordeler.core.database.DatabaseEntry;
import dk.magenta.datafordeler.geo.GeoPlugin;
import dk.magenta.datafordeler.geo.data.common.GeoMonotemporalRecord;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Index;
import javax.persistence.Table;
import java.util.Objects;

@Entity
@Table(name = GeoPlugin.DEBUG_TABLE_PREFIX + AccessAddressHouseNumberRecord.TABLE_NAME, indexes = {
        @Index(
                name = GeoPlugin.DEBUG_TABLE_PREFIX + AccessAddressHouseNumberRecord.TABLE_NAME + AccessAddressHouseNumberRecord.DB_FIELD_ENTITY,
                columnList = AccessAddressHouseNumberRecord.DB_FIELD_ENTITY + DatabaseEntry.REF
        ),
        @Index(
                name = GeoPlugin.DEBUG_TABLE_PREFIX + AccessAddressHouseNumberRecord.TABLE_NAME + AccessAddressHouseNumberRecord.DB_FIELD_NUMBER,
                columnList = AccessAddressHouseNumberRecord.DB_FIELD_NUMBER
        )
})
public class AccessAddressHouseNumberRecord extends GeoMonotemporalRecord<AccessAddressEntity> {

    public static final String TABLE_NAME = "geo_access_address_house_number";

    public AccessAddressHouseNumberRecord() {
    }

    public AccessAddressHouseNumberRecord(String number) {
        this.number = number;
    }


    public static final String DB_FIELD_NUMBER = "number";
    public static final String IO_FIELD_NUMBER = "husNummer";
    @Column(name = DB_FIELD_NUMBER)
    @JsonProperty(value = IO_FIELD_NUMBER)
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
        AccessAddressHouseNumberRecord that = (AccessAddressHouseNumberRecord) o;
        return Objects.equals(this.number, that.number);
    }
}
