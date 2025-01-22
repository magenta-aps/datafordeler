package dk.magenta.datafordeler.geo.data;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import dk.magenta.datafordeler.core.database.DatabaseEntry;
import dk.magenta.datafordeler.core.database.Identification;
import dk.magenta.datafordeler.core.database.IdentifiedEntity;
import dk.magenta.datafordeler.core.database.Nontemporal;
import dk.magenta.datafordeler.core.util.Equality;
import dk.magenta.datafordeler.geo.data.common.GeoMonotemporalRecord;
import org.hibernate.Session;

import jakarta.persistence.*;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Set;
import java.util.UUID;

@MappedSuperclass
@JsonIgnoreProperties(ignoreUnknown = true)
public abstract class GeoEntity extends DatabaseEntry implements IdentifiedEntity {

    public abstract boolean merge(GeoEntity other);

    public static final String DB_FIELD_IDENTIFICATION = "identification";
    @ManyToOne(fetch = FetchType.EAGER, cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH})
    protected Identification identification;

    @Override
    public Identification getIdentification() {
        return this.identification;
    }

    public void setIdentification(Identification identification) {
        this.identification = identification;
    }

    public UUID getUUID() {
        return this.identification.getUuid();
    }

    public static final String DB_FIELD_CREATOR = "creator";
    @Column(name = DB_FIELD_CREATOR)
    private String creator;

    public String getCreator() {
        return this.creator;
    }

    @JsonProperty(value = "Creator")
    public void setCreator(String creator) {
        this.creator = creator;
    }


    public static final String DB_FIELD_CREATION_DATE = "creationDate";
    @Column(name = DB_FIELD_CREATION_DATE)
    private OffsetDateTime creationDate;

    public OffsetDateTime getCreationDate() {
        return this.creationDate;
    }

    @JsonProperty(value = "CreationDate")
    public void setCreationDate(OffsetDateTime creationDate) {
        this.creationDate = creationDate;
    }

    @JsonProperty(value = "CreationDate")
    public void setCreationDate(long creationDate) {
        this.setCreationDate(Instant.ofEpochMilli(creationDate).atOffset(ZoneOffset.UTC));
    }

    public static final String DB_FIELD_EDIT_DATE = "editDate";
    @Column(name = DB_FIELD_EDIT_DATE)
    private OffsetDateTime editDate;

    public OffsetDateTime getEditDate() {
        return this.editDate;
    }

    @JsonProperty(value = "EditDate")
    public void setEditDate(OffsetDateTime editDate) {
        this.editDate = editDate;
    }

    @JsonProperty(value = "EditDate")
    public void setEditDate(long editDate) {
        this.setEditDate(Instant.ofEpochMilli(editDate).atOffset(ZoneOffset.UTC));
    }

    public static final String DB_FIELD_DAFO_UPDATED = Nontemporal.DB_FIELD_UPDATED;
    public static final String IO_FIELD_DAFO_UPDATED = "dafoOpdateret";

    @Column(name = DB_FIELD_DAFO_UPDATED)
    private OffsetDateTime dafoUpdated = null;

    @JsonProperty(value = IO_FIELD_DAFO_UPDATED)
    public OffsetDateTime getDafoUpdated() {
        return this.dafoUpdated;
    }

    public void setDafoUpdated(OffsetDateTime dafoUpdated) {
        this.dafoUpdated = dafoUpdated;
    }


    @Override
    public void forceLoad(Session session) {
    }

    public void update(RawData rawData, OffsetDateTime timestamp) {
        for (GeoMonotemporalRecord record : rawData.getMonotemporalRecords()) {
            record.setDafoUpdated(timestamp);
            this.addMonotemporalRecord(record);
            if (timestamp != null && (this.dafoUpdated == null || timestamp.isAfter(this.dafoUpdated))) {
                this.dafoUpdated = timestamp;
            }
        }
    }

    protected static <E extends GeoMonotemporalRecord> boolean addItem(Set<E> set, GeoMonotemporalRecord newItem) {
        if (newItem != null) {
            for (E oldItem : set) {
                if (newItem.equalData(oldItem) && Equality.equal(newItem.getRegistrationFrom(), oldItem.getRegistrationFrom())) {
                    return false;
                }
            }
            return set.add((E) newItem);
        }
        return false;
    }

    public abstract void addMonotemporalRecord(GeoMonotemporalRecord record);

    public void wire(Session session, WireCache wireCache) {
        for (Set<? extends GeoMonotemporalRecord> set : this.getAllRecords()) {
            for (GeoMonotemporalRecord record : set) {
                record.wire(session, wireCache);
            }
        }
    }

    public abstract Set<Set<? extends GeoMonotemporalRecord>> getAllRecords();
}
