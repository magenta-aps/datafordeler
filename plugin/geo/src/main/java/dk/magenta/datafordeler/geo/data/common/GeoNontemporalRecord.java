package dk.magenta.datafordeler.geo.data.common;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import dk.magenta.datafordeler.core.database.DatabaseEntry;
import dk.magenta.datafordeler.core.database.IdentifiedEntity;
import dk.magenta.datafordeler.core.database.Nontemporal;
import dk.magenta.datafordeler.geo.data.GeoEntity;
import jakarta.persistence.Column;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MappedSuperclass;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;
import java.time.OffsetDateTime;

@MappedSuperclass
public abstract class GeoNontemporalRecord<E extends GeoEntity> extends DatabaseEntry implements Nontemporal {

    public static final String DB_FIELD_ENTITY = "entity";

    @JsonIgnore
    @XmlTransient
    @ManyToOne(optional = false)
    private E entity;

    public E getEntity() {
        return this.entity;
    }

    public void setEntity(E entity) {
        this.entity = entity;
    }

    public void setEntity(IdentifiedEntity identifiedEntity) {
        this.setEntity((E) identifiedEntity);
    }


    public static final String DB_FIELD_UPDATED = "dafoUpdated";
    public static final String IO_FIELD_UPDATED = "sidstOpdateret";
    @Column(name = DB_FIELD_UPDATED)
    @JsonProperty(value = IO_FIELD_UPDATED)
    @XmlElement(name = IO_FIELD_UPDATED)
    public OffsetDateTime dafoUpdated;

    public OffsetDateTime getDafoUpdated() {
        return this.dafoUpdated;
    }

    public void setDafoUpdated(OffsetDateTime dafoUpdated) {
        this.dafoUpdated = dafoUpdated;
    }

    protected static void copy(GeoNontemporalRecord from, GeoNontemporalRecord to) {
        to.dafoUpdated = from.dafoUpdated;
    }

    public boolean equalData(Object o) {
        return o != null && (getClass() == o.getClass());
    }

}
