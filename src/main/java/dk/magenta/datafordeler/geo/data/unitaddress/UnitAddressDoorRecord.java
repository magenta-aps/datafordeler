package dk.magenta.datafordeler.geo.data.unitaddress;

import dk.magenta.datafordeler.core.database.DatabaseEntry;
import dk.magenta.datafordeler.geo.GeoPlugin;
import dk.magenta.datafordeler.geo.data.common.GeoMonotemporalRecord;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Index;
import javax.persistence.Table;
import java.util.Objects;

@Entity
@Table(name = GeoPlugin.DEBUG_TABLE_PREFIX + UnitAddressDoorRecord.TABLE_NAME, indexes = {
        @Index(
                name = GeoPlugin.DEBUG_TABLE_PREFIX + UnitAddressDoorRecord.TABLE_NAME + UnitAddressDoorRecord.DB_FIELD_ENTITY,
                columnList = UnitAddressDoorRecord.DB_FIELD_ENTITY + DatabaseEntry.REF
        ),
})
public class UnitAddressDoorRecord extends GeoMonotemporalRecord<UnitAddressEntity> {

    public static final String TABLE_NAME = "geo_unit_address_door";

    public UnitAddressDoorRecord() {
    }

    public UnitAddressDoorRecord(String door) {
        this.door = door;
    }


    public static final String DB_FIELD_DOOR = "door";
    @Column(name = DB_FIELD_DOOR)
    private String door;

    public String getDoor() {
        return this.door;
    }

    public void setDoor(String door) {
        this.door = door;
    }

    public boolean equalData(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equalData(o)) return false;
        UnitAddressDoorRecord that = (UnitAddressDoorRecord) o;
        return Objects.equals(this.door, that.door);
    }
}
