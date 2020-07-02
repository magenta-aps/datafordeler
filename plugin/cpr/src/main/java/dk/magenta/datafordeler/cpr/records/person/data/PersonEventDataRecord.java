package dk.magenta.datafordeler.cpr.records.person.data;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import dk.magenta.datafordeler.core.database.DatabaseEntry;
import dk.magenta.datafordeler.core.database.IdentifiedEntity;
import dk.magenta.datafordeler.cpr.CprPlugin;
import dk.magenta.datafordeler.cpr.data.CprRecordEntity;
import dk.magenta.datafordeler.cpr.data.person.PersonEntity;

import javax.persistence.*;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;
import java.time.OffsetDateTime;
import java.util.Collection;

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
        this.timestamp = timestamp;
        this.eventId = eventId;
        this.derived = derived;
    }


    public static final String DB_FIELD_ENTITY = "entity";

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = PersonEventDataRecord.DB_FIELD_ENTITY + DatabaseEntry.REF)
    @JsonIgnore
    @XmlTransient
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
        return timestamp;
    }

    public static final String DB_FIELD_TIMESTAMP = "timestamp";
    @Column(name = DB_FIELD_TIMESTAMP)
    @JsonIgnore
    @XmlElement(name = DB_FIELD_TIMESTAMP)
    private OffsetDateTime timestamp;


    public static final String DB_FIELD_EVENT = "eventId";
    @Column(name = DB_FIELD_EVENT)
    @JsonIgnore
    @XmlTransient
    private String eventId;

    public static final String DB_FIELD_DERIVED = "derived";
    @Column(name = DB_FIELD_DERIVED)
    @JsonIgnore
    @XmlTransient
    private String derived;

    @JsonProperty(value = "id")
    public Long getId() {
        return super.getId();
    }


    @Override
    public IdentifiedEntity getNewest(Collection<IdentifiedEntity> collection) {
        return null;
    }
}
