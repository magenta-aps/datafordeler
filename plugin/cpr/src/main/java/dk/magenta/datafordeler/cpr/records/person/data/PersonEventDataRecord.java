package dk.magenta.datafordeler.cpr.records.person.data;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import dk.magenta.datafordeler.core.database.DatabaseEntry;
import dk.magenta.datafordeler.core.database.IdentifiedEntity;
import dk.magenta.datafordeler.cpr.CprPlugin;
import dk.magenta.datafordeler.cpr.data.CprRecordEntity;
import dk.magenta.datafordeler.cpr.data.person.PersonEntity;
import jakarta.persistence.*;

import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.List;

import static dk.magenta.datafordeler.core.database.Bitemporal.fixOffsetIn;
import static dk.magenta.datafordeler.core.database.Bitemporal.fixOffsetOut;

/**
 * Storage for data on a Person's eventhistory
 * referenced by {@link dk.magenta.datafordeler.cpr.records.person.data}
 */
@Entity
@Table(name = CprPlugin.DEBUG_TABLE_PREFIX + "cpr_person_event_record", indexes = {
        @Index(name = CprPlugin.DEBUG_TABLE_PREFIX + "cpr_person_event_record" + PersonEventDataRecord.DB_FIELD_ENTITY, columnList = PersonEventDataRecord.DB_FIELD_ENTITY + DatabaseEntry.REF)
})
public class PersonEventDataRecord extends CprRecordEntity {

    public PersonEventDataRecord() {
    }

    public PersonEventDataRecord(OffsetDateTime timestamp, String eventId, String derived) {
        this.setDafoUpdated(fixOffsetIn(OffsetDateTime.now()));
        this.timestamp = fixOffsetIn(timestamp);
        this.eventId = eventId;
        this.derived = derived;
        this.timestampNew = timestamp;
    }


    public static final String DB_FIELD_ENTITY = "entity";

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = PersonEventDataRecord.DB_FIELD_ENTITY + DatabaseEntry.REF)
    @JsonIgnore
    private PersonEntity entity;

    public PersonEntity getEntity() {
        return this.entity;
    }

    public void setEntity(PersonEntity entity) {
        this.entity = entity;
    }

    public void setEntity(IdentifiedEntity entity) {
        this.entity = (PersonEntity) entity;
    }


    public String getEventId() {
        return eventId;
    }

    public OffsetDateTime getTimestamp() {
        return fixOffsetOut(timestamp);
    }

    public static final String DB_FIELD_TIMESTAMP = "timestamp";
    @Column(name = DB_FIELD_TIMESTAMP, columnDefinition = "datetime2")
    @JsonIgnore
    private OffsetDateTime timestamp;

    @JsonIgnore
    @Column(name = DB_FIELD_TIMESTAMP+"_new")
    private OffsetDateTime timestampNew;

    public void setTimestamp(OffsetDateTime timestamp) {
        this.timestamp = fixOffsetIn(timestamp);
        this.timestampNew = timestamp;
    }

    public static final String DB_FIELD_EVENT = "eventId";
    public static final String IO_FIELD_EVENT = "eventId";
    @Column(name = DB_FIELD_EVENT)
    @JsonIgnore
    private String eventId;

    public static final String DB_FIELD_DERIVED = "derived";
    @Column(name = DB_FIELD_DERIVED)
    @JsonIgnore
    private String derived;

    @JsonProperty(value = "id")
    public Long getId() {
        return super.getId();
    }


    @Override
    public IdentifiedEntity getNewest(Collection<IdentifiedEntity> collection) {
        return null;
    }

    public void updateTimestamp() {
        this.timestampNew = this.getTimestamp();
    }

    public static List<String> updateFields() {
        return List.of(DB_FIELD_TIMESTAMP);
    }
}
