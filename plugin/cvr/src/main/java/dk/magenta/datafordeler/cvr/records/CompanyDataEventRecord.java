package dk.magenta.datafordeler.cvr.records;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import dk.magenta.datafordeler.core.database.DatabaseEntry;
import dk.magenta.datafordeler.core.database.IdentifiedEntity;

import dk.magenta.datafordeler.cpr.data.CprRecordEntity;
//import dk.magenta.datafordeler.cpr.data.person.PersonEntity;
import dk.magenta.datafordeler.cvr.CvrPlugin;


import javax.persistence.*;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;
import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;


@Entity
@Table(name = CvrPlugin.DEBUG_TABLE_PREFIX + CompanyDataEventRecord.TABLE_NAME, indexes = {
        @Index(name = CvrPlugin.DEBUG_TABLE_PREFIX + CompanyDataEventRecord.TABLE_NAME + CompanyDataEventRecord.DB_FIELD_ENTITY, columnList = CompanyDataEventRecord.DB_FIELD_ENTITY + DatabaseEntry.REF)
})
public class CompanyDataEventRecord extends CvrBitemporalDataRecord {


    public static final String TABLE_NAME = "company_data_event_record";
    public static final String DB_FIELD_ENTITY = "entity";

    public CompanyDataEventRecord() {
    }

    public CompanyDataEventRecord(OffsetDateTime timestamp, String field, Long oldItem, Long newItem) {
        this.setDafoUpdated(OffsetDateTime.now());
        this.timestamp = timestamp;
        this.field = field;
        this.oldItem = oldItem;
        this.newItem = newItem;
    }

    public String getFieldName() {
        return TABLE_NAME;
    }

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = DB_FIELD_ENTITY + DatabaseEntry.REF)
    @JsonIgnore
    @XmlTransient
    private CompanyRecord entity;

    public CompanyRecord getEntity() {
        return this.entity;
    }

    public void setEntity(CompanyRecord entity) {
        this.entity = entity;
    }

    public void setEntity(IdentifiedEntity entity) {
        this.entity = (CompanyRecord) entity;
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




}
