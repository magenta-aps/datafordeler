package dk.magenta.datafordeler.geo.data.locality;

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
@Table(name = GeoPlugin.DEBUG_TABLE_PREFIX + LocalityStatusRecord.TABLE_NAME, indexes = {
        @Index(
                name = GeoPlugin.DEBUG_TABLE_PREFIX + LocalityStatusRecord.TABLE_NAME + LocalityStatusRecord.DB_FIELD_ENTITY,
                columnList = LocalityStatusRecord.DB_FIELD_ENTITY + DatabaseEntry.REF
        ),
        @Index(
                name = GeoPlugin.DEBUG_TABLE_PREFIX + LocalityStatusRecord.TABLE_NAME + LocalityStatusRecord.DB_FIELD_TYPE,
                columnList = LocalityStatusRecord.DB_FIELD_TYPE
        ),
})
public class LocalityStatusRecord extends GeoMonotemporalRecord<GeoLocalityEntity> {

    public static final String TABLE_NAME = "geo_locality_status";

    public LocalityStatusRecord() {
    }

    public LocalityStatusRecord(Integer status) {
        this.status = status;
    }


    public static final String DB_FIELD_TYPE = "status";
    public static final String IO_FIELD_TYPE = "status";
    @Column(name = DB_FIELD_TYPE)
    @JsonProperty(IO_FIELD_TYPE)
    private Integer status;

    public Integer getStatus() {
        return this.status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public boolean equalData(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equalData(o)) return false;
        LocalityStatusRecord that = (LocalityStatusRecord) o;
        return Objects.equals(this.status, that.status);
    }
}
