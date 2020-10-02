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


@Entity
@Table(name = CprPlugin.DEBUG_TABLE_PREFIX + PersonDataEventDataRecord.TABLE_NAME, indexes = {
        @Index(name = CprPlugin.DEBUG_TABLE_PREFIX + PersonDataEventDataRecord.TABLE_NAME + PersonDataEventDataRecord.DB_FIELD_ENTITY, columnList = PersonDataEventDataRecord.DB_FIELD_ENTITY + DatabaseEntry.REF)
})
public class PersonDataEventDataRecord extends CprRecordEntity {


    public static final String TABLE_NAME = "cpr_person_data_event_record";
    public static final String DB_FIELD_ENTITY = "entity";

    public PersonDataEventDataRecord() {
    }

    public PersonDataEventDataRecord(OffsetDateTime timestamp, String field, Long oldItem, Long newItem) {
        this.setDafoUpdated(OffsetDateTime.now());
        this.timestamp = timestamp;
        this.field = field;
        this.oldItem = oldItem;
        this.newItem = newItem;
    }


    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = DB_FIELD_ENTITY + DatabaseEntry.REF)
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


    public OffsetDateTime getTimestamp() {
        return timestamp;
    }

    public static final String DB_FIELD_FIELD = "field";
    @Column(name = DB_FIELD_FIELD)
    @JsonIgnore
    @XmlElement(name = DB_FIELD_FIELD)
    private String field;


    public static final String DB_FIELD_TEXT = "text";
    @Column(name = DB_FIELD_TEXT)
    @JsonIgnore
    @XmlElement(name = DB_FIELD_TEXT)
    private String text;


    public static final String DB_FIELD_TIMESTAMP = "timestamp";
    @Column(name = DB_FIELD_TIMESTAMP)
    @JsonIgnore
    @XmlElement(name = DB_FIELD_TIMESTAMP)
    private OffsetDateTime timestamp;


    public static final String DB_FIELD_OLD_ITEM = "oldItem";
    @Column(name = DB_FIELD_OLD_ITEM)
    @JsonIgnore
    @XmlTransient
    private Long oldItem;

    public static final String DB_FIELD_NEW_ITEM = "newItem";
    @Column(name = DB_FIELD_NEW_ITEM)
    @JsonIgnore
    @XmlTransient
    private Long newItem;


    @JsonProperty(value = "id")
    public Long getId() {
        return super.getId();
    }


    @Override
    public IdentifiedEntity getNewest(Collection<IdentifiedEntity> collection) {
        return null;
    }
}
