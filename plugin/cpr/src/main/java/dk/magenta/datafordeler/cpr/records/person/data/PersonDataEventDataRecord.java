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

import static dk.magenta.datafordeler.core.database.Bitemporal.fixOffsetIn;
import static dk.magenta.datafordeler.core.database.Bitemporal.fixOffsetOut;


@Entity
@Table(name = CprPlugin.DEBUG_TABLE_PREFIX + PersonDataEventDataRecord.TABLE_NAME, indexes = {
        @Index(name = CprPlugin.DEBUG_TABLE_PREFIX + PersonDataEventDataRecord.TABLE_NAME + PersonDataEventDataRecord.DB_FIELD_ENTITY, columnList = PersonDataEventDataRecord.DB_FIELD_ENTITY + DatabaseEntry.REF),
        @Index(name = CprPlugin.DEBUG_TABLE_PREFIX + PersonDataEventDataRecord.TABLE_NAME + PersonDataEventDataRecord.DB_FIELD_FIELD, columnList = PersonDataEventDataRecord.DB_FIELD_FIELD),
        @Index(name = CprPlugin.DEBUG_TABLE_PREFIX + PersonDataEventDataRecord.TABLE_NAME + PersonDataEventDataRecord.DB_FIELD_TIMESTAMP, columnList = PersonDataEventDataRecord.DB_FIELD_TIMESTAMP)
})
public class PersonDataEventDataRecord extends CprRecordEntity {


    public static final String TABLE_NAME = "cpr_person_data_event_record";
    public static final String DB_FIELD_ENTITY = "entity";

    public PersonDataEventDataRecord() {
    }

    public PersonDataEventDataRecord(OffsetDateTime timestamp, String field, Long oldItem, String text) {
        this.setDafoUpdated(OffsetDateTime.now());
        this.timestamp = fixOffsetIn(timestamp);
        this.field = field;
        this.oldItem = oldItem;
        this.text = text;
    }

    @JsonIgnore
    public String getFieldName() {
        return TABLE_NAME;
    }

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = DB_FIELD_ENTITY + DatabaseEntry.REF)
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


    public OffsetDateTime getTimestamp() {
        return fixOffsetOut(timestamp);
    }

    public static final String DB_FIELD_FIELD = "field";
    @Column(name = DB_FIELD_FIELD)
    @JsonIgnore
    private String field;

    public String getField() {
        return field;
    }


    public static final String DB_FIELD_TEXT = "text";
    @Column(name = DB_FIELD_TEXT)
    @JsonIgnore
    private String text;


    public static final String DB_FIELD_TIMESTAMP = "timestamp";
    @Column(name = DB_FIELD_TIMESTAMP, columnDefinition = "datetime2")
    @JsonIgnore
    private OffsetDateTime timestamp;


    public static final String DB_FIELD_OLD_ITEM = "oldItem";
    @Column(name = DB_FIELD_OLD_ITEM)
    @JsonIgnore
    private Long oldItem;

    public Long getOldItem() {
        return oldItem;
    }

    @JsonProperty(value = "id")
    public Long getId() {
        return super.getId();
    }


    @Override
    public IdentifiedEntity getNewest(Collection<IdentifiedEntity> collection) {
        return null;
    }
}
