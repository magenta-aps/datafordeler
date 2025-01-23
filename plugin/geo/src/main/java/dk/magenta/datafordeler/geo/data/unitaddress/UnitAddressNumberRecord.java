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
@Table(name = GeoPlugin.DEBUG_TABLE_PREFIX + UnitAddressNumberRecord.TABLE_NAME, indexes = {
        @Index(
                name = GeoPlugin.DEBUG_TABLE_PREFIX + UnitAddressNumberRecord.TABLE_NAME + UnitAddressNumberRecord.DB_FIELD_ENTITY,
                columnList = UnitAddressNumberRecord.DB_FIELD_ENTITY + DatabaseEntry.REF
        ),
        @Index(
                name = GeoPlugin.DEBUG_TABLE_PREFIX + UnitAddressNumberRecord.TABLE_NAME + UnitAddressNumberRecord.DB_FIELD_NUMBER,
                columnList = UnitAddressNumberRecord.DB_FIELD_NUMBER
        ),
})
public class UnitAddressNumberRecord extends GeoMonotemporalRecord<UnitAddressEntity> {

    public static final String TABLE_NAME = "geo_unit_address_number";

    public UnitAddressNumberRecord() {
    }

    public UnitAddressNumberRecord(String number) {
        this.number = number;
    }


    public static final String DB_FIELD_NUMBER = "number";
    public static final String IO_FIELD_NUMBER = "nummer";
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
        UnitAddressNumberRecord that = (UnitAddressNumberRecord) o;
        return Objects.equals(this.number, that.number);
    }
}
