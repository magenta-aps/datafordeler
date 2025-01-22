package dk.magenta.datafordeler.geo.data.common;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import dk.magenta.datafordeler.core.database.Identification;
import dk.magenta.datafordeler.geo.data.GeoEntity;
import dk.magenta.datafordeler.geo.data.WireCache;
import dk.magenta.datafordeler.geo.data.postcode.PostcodeEntity;
import org.hibernate.Session;

import jakarta.persistence.Column;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MappedSuperclass;
import java.util.Objects;

@MappedSuperclass
public class PostcodeReferenceRecord<E extends GeoEntity> extends GeoMonotemporalRecord<E> {

    public static final String DB_FIELD_ENTITY = GeoMonotemporalRecord.DB_FIELD_ENTITY;

    public PostcodeReferenceRecord() {
    }

    public PostcodeReferenceRecord(Integer postcode) {
        this.postcode = postcode;
    }

    public static final String DB_FIELD_CODE = "postcode";
    public static final String IO_FIELD_CODE = "postnummer";
    @Column(name = DB_FIELD_CODE, nullable = true)
    @JsonProperty(IO_FIELD_CODE)
    private Integer postcode;

    public Integer getPostcode() {
        return this.postcode;
    }

    public void setPostcode(Integer postcode) {
        this.postcode = postcode;
    }


    public static final String DB_FIELD_REFERENCE = "reference";
    @ManyToOne
    @JsonIgnore
    private Identification reference;

    public Identification getReference() {
        return this.reference;
    }

    public void wire(Session session, WireCache wireCache) {
        if (this.postcode != null) {
            PostcodeEntity postcodeEntity = wireCache.getPostcode(session, this.postcode);
            if (postcodeEntity != null) {
                this.reference = postcodeEntity.getIdentification();
            }
        }
    }

    public boolean equalData(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equalData(o)) return false;
        PostcodeReferenceRecord that = (PostcodeReferenceRecord) o;
        return this.postcode != null && Objects.equals(this.postcode, that.postcode);
    }


}
