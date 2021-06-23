package dk.magenta.datafordeler.geo.data.locality;

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
@Table(name = GeoPlugin.DEBUG_TABLE_PREFIX + LocalityBetegnRecord.TABLE_NAME, indexes = {
        @Index(
                name = GeoPlugin.DEBUG_TABLE_PREFIX + LocalityBetegnRecord.TABLE_NAME + LocalityBetegnRecord.DB_FIELD_ENTITY,
                columnList = LocalityBetegnRecord.DB_FIELD_ENTITY + DatabaseEntry.REF
        ),
        @Index(
                name = GeoPlugin.DEBUG_TABLE_PREFIX + LocalityBetegnRecord.TABLE_NAME + LocalityBetegnRecord.DB_FIELD_TYPE,
                columnList = LocalityBetegnRecord.DB_FIELD_TYPE
        ),
})
public class LocalityBetegnRecord extends GeoMonotemporalRecord<GeoLocalityEntity> {

    public static final String TABLE_NAME = "geo_locality_betegn";

    public LocalityBetegnRecord() {
    }

    public LocalityBetegnRecord(Integer betegn) {
        this.betegn = betegn;
    }



    public static final String DB_FIELD_TYPE = "betegn";
    public static final String IO_FIELD_TYPE = "betegn";
    @Column(name = DB_FIELD_TYPE)
    @JsonProperty(IO_FIELD_TYPE)
    private Integer betegn;

    public Integer getType() {
        return this.betegn;
    }

    public void setType(Integer betegn) {
        this.betegn = betegn;
    }

    public boolean equalData(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equalData(o)) return false;
        LocalityBetegnRecord that = (LocalityBetegnRecord) o;
        return Objects.equals(this.betegn, that.betegn);
    }
}
