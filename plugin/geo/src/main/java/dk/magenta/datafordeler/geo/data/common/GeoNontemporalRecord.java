package dk.magenta.datafordeler.geo.data.common;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import dk.magenta.datafordeler.core.database.DatabaseEntry;
import dk.magenta.datafordeler.core.database.IdentifiedEntity;
import dk.magenta.datafordeler.core.database.Nontemporal;
import dk.magenta.datafordeler.core.migration.MigrateModel;
import dk.magenta.datafordeler.geo.data.GeoEntity;
import jakarta.persistence.Column;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MappedSuperclass;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static dk.magenta.datafordeler.core.database.Bitemporal.fixOffsetIn;
import static dk.magenta.datafordeler.core.database.Bitemporal.fixOffsetOut;

@MappedSuperclass
public abstract class GeoNontemporalRecord<E extends GeoEntity> extends DatabaseEntry implements Nontemporal, MigrateModel {

    public static final String DB_FIELD_ENTITY = "entity";

    @JsonIgnore
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
    @Column(name = DB_FIELD_UPDATED, columnDefinition = "datetime2")
    public OffsetDateTime dafoUpdated;
    @JsonIgnore
    @Column(name = DB_FIELD_UPDATED+"_new")
    public OffsetDateTime dafoUpdatedNew;

    @JsonProperty(value = IO_FIELD_UPDATED)
    public OffsetDateTime getDafoUpdated() {
        return fixOffsetOut(this.dafoUpdated);
    }

    @JsonProperty(value = IO_FIELD_UPDATED)
    public void setDafoUpdated(OffsetDateTime dafoUpdated) {
        this.dafoUpdated = fixOffsetIn(dafoUpdated);
        this.dafoUpdatedNew = dafoUpdated;
    }

    protected static void copy(GeoNontemporalRecord from, GeoNontemporalRecord to) {
        to.dafoUpdated = from.dafoUpdated;
        to.dafoUpdatedNew = from.dafoUpdatedNew;
    }

    public boolean equalData(Object o) {
        return o != null && (getClass() == o.getClass());
    }


    public void updateTimestamp() {
        this.dafoUpdatedNew = this.getDafoUpdated();
    }

    public static List<String> updateFields() {
        return List.of(DB_FIELD_UPDATED);
    }

}
