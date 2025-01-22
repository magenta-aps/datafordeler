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
@Table(name = GeoPlugin.DEBUG_TABLE_PREFIX + LocalityTypeRecord.TABLE_NAME, indexes = {
        @Index(
                name = GeoPlugin.DEBUG_TABLE_PREFIX + LocalityTypeRecord.TABLE_NAME + LocalityTypeRecord.DB_FIELD_ENTITY,
                columnList = LocalityTypeRecord.DB_FIELD_ENTITY + DatabaseEntry.REF
        ),
        @Index(
                name = GeoPlugin.DEBUG_TABLE_PREFIX + LocalityTypeRecord.TABLE_NAME + LocalityTypeRecord.DB_FIELD_TYPE,
                columnList = LocalityTypeRecord.DB_FIELD_TYPE
        ),
})
public class LocalityTypeRecord extends GeoMonotemporalRecord<GeoLocalityEntity> {

    public static final String TABLE_NAME = "geo_locality_type";

    public LocalityTypeRecord() {
    }

    public LocalityTypeRecord(Integer type) {
        this.type = type;
    }


    public static final String DB_FIELD_TYPE = "type";
    public static final String IO_FIELD_TYPE = "type";
    @Column(name = DB_FIELD_TYPE)
    @JsonProperty(IO_FIELD_TYPE)
    private Integer type;

    public Integer getType() {
        return this.type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public boolean equalData(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equalData(o)) return false;
        LocalityTypeRecord that = (LocalityTypeRecord) o;
        return Objects.equals(this.type, that.type);
    }
}
