package dk.magenta.datafordeler.geo.data.common;

import com.fasterxml.jackson.annotation.JsonProperty;
import dk.magenta.datafordeler.geo.data.GeoEntity;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import java.util.Objects;

@MappedSuperclass
public class ObjectStatusRecord<E extends GeoEntity> extends GeoMonotemporalRecord<E> {

    public ObjectStatusRecord() {
    }

    public ObjectStatusRecord(Integer status) {
        this.status = status;
    }


    public static final String DB_FIELD_STATUS = "status";
    public static final String IO_FIELD_STATUS = "objektStatus";
    @Column(name = DB_FIELD_STATUS)
    @JsonProperty(value = IO_FIELD_STATUS)
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
        ObjectStatusRecord that = (ObjectStatusRecord) o;
        return Objects.equals(this.status, that.status);
    }
}
