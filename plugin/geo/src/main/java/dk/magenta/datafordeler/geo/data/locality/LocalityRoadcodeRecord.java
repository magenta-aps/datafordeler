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
@Table(name = GeoPlugin.DEBUG_TABLE_PREFIX + LocalityRoadcodeRecord.TABLE_NAME, indexes = {
        @Index(
                name = GeoPlugin.DEBUG_TABLE_PREFIX + LocalityRoadcodeRecord.TABLE_NAME + LocalityRoadcodeRecord.DB_FIELD_ENTITY,
                columnList = LocalityRoadcodeRecord.DB_FIELD_ENTITY + DatabaseEntry.REF
        ),
        @Index(
                name = GeoPlugin.DEBUG_TABLE_PREFIX + LocalityRoadcodeRecord.TABLE_NAME + LocalityRoadcodeRecord.DB_FIELD_CODE,
                columnList = LocalityRoadcodeRecord.DB_FIELD_CODE
        ),
})
public class LocalityRoadcodeRecord extends GeoMonotemporalRecord<GeoLocalityEntity> {

    public static final String TABLE_NAME = "geo_locality_roadcode";

    public LocalityRoadcodeRecord() {
    }

    public LocalityRoadcodeRecord(Integer code) {
        this.code = code;
    }

    public static final String DB_FIELD_ENTITY = GeoMonotemporalRecord.DB_FIELD_ENTITY;


    public static final String DB_FIELD_CODE = "code";
    public static final String IO_FIELD_CODE = "lokalitetvejkode";
    @Column(name = DB_FIELD_CODE, nullable = true)
    @JsonProperty(IO_FIELD_CODE)
    private Integer code;

    public Integer getCode() {
        return this.code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public boolean equalData(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equalData(o)) return false;
        LocalityRoadcodeRecord that = (LocalityRoadcodeRecord) o;
        return Objects.equals(this.code, that.code);
    }

}
