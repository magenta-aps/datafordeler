package dk.magenta.datafordeler.geo.data;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import dk.magenta.datafordeler.core.database.DatabaseEntry;
import dk.magenta.datafordeler.core.database.Identification;
import dk.magenta.datafordeler.core.database.IdentifiedEntity;
import dk.magenta.datafordeler.geo.data.common.GeoMonotemporalRecord;
import org.hibernate.Session;

import javax.persistence.*;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Objects;
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

    @Override
    public void forceLoad(Session session) {
    }

    protected static <E extends GeoMonotemporalRecord> boolean addItem(Set<E> set, GeoMonotemporalRecord newItem) {
        if (newItem != null) {
            for (E oldItem : set) {
                if (newItem.equalData(oldItem) && Objects.equals(newItem.getRegistrationFrom(), oldItem.getRegistrationFrom())) {
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
