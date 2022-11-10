package dk.magenta.datafordeler.geo.data.common;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import dk.magenta.datafordeler.core.database.Identification;
import dk.magenta.datafordeler.geo.data.GeoEntity;
import dk.magenta.datafordeler.geo.data.WireCache;
import dk.magenta.datafordeler.geo.data.locality.GeoLocalityEntity;
import org.hibernate.Session;

import javax.persistence.Column;
import javax.persistence.ManyToOne;
import javax.persistence.MappedSuperclass;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@MappedSuperclass
public class LocalityReferenceRecord<E extends GeoEntity> extends GeoMonotemporalRecord<E> {

    public LocalityReferenceRecord() {
    }

    public LocalityReferenceRecord(String code) {
        this.code = code;
    }

    public LocalityReferenceRecord(UUID uuid) {
        this.uuid = uuid;
    }

    public static final String DB_FIELD_CODE = "code";
    public static final String IO_FIELD_CODE = "lokalitetskode";
    @Column(name = DB_FIELD_CODE)
    @JsonProperty(IO_FIELD_CODE)
    private String code;

    public String getCode() {
        return this.code;
    }

    public void setCode(String code) {
        this.code = code;
    }


    public static final String DB_FIELD_UUID = "uuid";
    @Column(name = DB_FIELD_UUID)
    @JsonIgnore
    private UUID uuid;

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }


    public static final String DB_FIELD_REFERENCE = "reference";
    @ManyToOne
    @JsonIgnore
    private Identification reference;

    public Identification getReference() {
        return this.reference;
    }

    public void wire(Session session, WireCache wireCache) {
        if (this.reference == null && this.code != null) {
            List<GeoLocalityEntity> localityEntities = wireCache.getLocality(session, this.code);
            for (GeoLocalityEntity locality : localityEntities) {
                this.reference = locality.getIdentification();
                break;
            }
        }
        if (this.reference == null && this.uuid != null) {
            GeoLocalityEntity geoLocalityEntity = wireCache.getLocality(session, this.uuid);
            if (geoLocalityEntity != null) {
                this.reference = geoLocalityEntity.getIdentification();
                this.code = geoLocalityEntity.getCode();
            }
        }
    }

    public boolean equalData(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equalData(o)) return false;
        LocalityReferenceRecord that = (LocalityReferenceRecord) o;
        return (this.code != null && Objects.equals(this.code, that.code)) || (this.uuid != null && Objects.equals(this.uuid, that.uuid));
    }

}
