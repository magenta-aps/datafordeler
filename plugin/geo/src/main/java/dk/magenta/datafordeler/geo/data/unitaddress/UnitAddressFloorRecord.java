package dk.magenta.datafordeler.geo.data.unitaddress;

import com.fasterxml.jackson.annotation.JsonProperty;
import dk.magenta.datafordeler.core.database.DatabaseEntry;
import dk.magenta.datafordeler.geo.GeoPlugin;
import dk.magenta.datafordeler.geo.data.common.GeoMonotemporalRecord;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import java.util.Objects;

@Entity
@Table(name = GeoPlugin.DEBUG_TABLE_PREFIX + UnitAddressFloorRecord.TABLE_NAME, indexes = {
        @Index(
                name = GeoPlugin.DEBUG_TABLE_PREFIX + UnitAddressFloorRecord.TABLE_NAME + UnitAddressFloorRecord.DB_FIELD_ENTITY,
                columnList = UnitAddressFloorRecord.DB_FIELD_ENTITY + DatabaseEntry.REF
        ),
        @Index(
                name = GeoPlugin.DEBUG_TABLE_PREFIX + UnitAddressFloorRecord.TABLE_NAME + UnitAddressFloorRecord.DB_FIELD_FLOOR,
                columnList = UnitAddressFloorRecord.DB_FIELD_FLOOR
        ),
})
public class UnitAddressFloorRecord extends GeoMonotemporalRecord<UnitAddressEntity> {

    public static final String TABLE_NAME = "geo_unit_address_floor";

    public UnitAddressFloorRecord() {
    }

    public UnitAddressFloorRecord(String floor) {
        this.floor = floor;
    }


    public static final String DB_FIELD_FLOOR = "floor";
    public static final String IO_FIELD_FLOOR = "etage";
    @Column(name = DB_FIELD_FLOOR)
    @JsonProperty(value = IO_FIELD_FLOOR)
    private String floor;

    public String getFloor() {
        return this.floor;
    }

    public void setFloor(String floor) {
        this.floor = floor;
    }

    public boolean equalData(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equalData(o)) return false;
        UnitAddressFloorRecord that = (UnitAddressFloorRecord) o;
        return Objects.equals(this.floor, that.floor);
    }
}
