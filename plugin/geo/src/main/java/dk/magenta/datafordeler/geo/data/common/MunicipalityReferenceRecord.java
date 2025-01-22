package dk.magenta.datafordeler.geo.data.common;

import com.fasterxml.jackson.annotation.JsonProperty;
import dk.magenta.datafordeler.geo.data.GeoEntity;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import java.util.Objects;

@MappedSuperclass
public class MunicipalityReferenceRecord<E extends GeoEntity> extends GeoMonotemporalRecord<E> {

    public static final String DB_FIELD_ENTITY = GeoMonotemporalRecord.DB_FIELD_ENTITY;

    public MunicipalityReferenceRecord() {
    }

    public MunicipalityReferenceRecord(Integer code) {
        this.code = code;
    }

    public static final String DB_FIELD_CODE = "code";
    public static final String IO_FIELD_CODE = "kommunekode";
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
        MunicipalityReferenceRecord that = (MunicipalityReferenceRecord) o;
        return Objects.equals(this.code, that.code);
    }

}
