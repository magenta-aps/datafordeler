package dk.magenta.datafordeler.geo.data.common;

import com.fasterxml.jackson.annotation.JsonProperty;
import dk.magenta.datafordeler.geo.data.GeoEntity;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import java.util.Objects;

@MappedSuperclass
public class UsageRecord<E extends GeoEntity> extends GeoMonotemporalRecord<E> {

    public UsageRecord() {
    }

    public UsageRecord(Integer usage) {
        this.usage = usage;
    }


    public static final String DB_FIELD_USAGE = "usage";
    public static final String IO_FIELD_USAGE = "anvendelsesKode";
    @Column(name = DB_FIELD_USAGE)
    @JsonProperty(value = IO_FIELD_USAGE)
    private Integer usage;

    public Integer getUsage() {
        return this.usage;
    }

    public void setUsage(Integer usage) {
        this.usage = usage;
    }

    public boolean equalData(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equalData(o)) return false;
        UsageRecord that = (UsageRecord) o;
        return Objects.equals(this.usage, that.usage);
    }
}
