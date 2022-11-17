package dk.magenta.datafordeler.cvr.records;

import com.fasterxml.jackson.annotation.JsonIgnore;
import dk.magenta.datafordeler.core.database.DatabaseEntry;
import dk.magenta.datafordeler.cvr.CvrPlugin;

import javax.persistence.*;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;
import java.time.OffsetDateTime;
import java.util.Objects;


@Entity
@Table(
        name = CvrPlugin.DEBUG_TABLE_PREFIX + CompanyDataEventRecord.TABLE_NAME,
        indexes = {
            @Index(name = CvrPlugin.DEBUG_TABLE_PREFIX + CompanyDataEventRecord.TABLE_NAME + CompanyDataEventRecord.DB_FIELD_ENTITY, columnList = CompanyDataEventRecord.DB_FIELD_ENTITY + DatabaseEntry.REF)
        }
)
public class CompanyDataEventRecord extends CvrNontemporalRecord {

    public static final String TABLE_NAME = "cvr_record_company_data_event_record";
    public static final String DB_FIELD_ENTITY = "companyRecord";

    public CompanyDataEventRecord() {
    }

    public CompanyDataEventRecord(OffsetDateTime timestamp, String field, Long oldItem) {
        this.setDafoUpdated(OffsetDateTime.now());
        this.timestamp = timestamp;
        this.field = field;
        this.oldItem = oldItem;
    }

    @JsonIgnore
    public String getFieldName() {
        return TABLE_NAME;
    }


    public static final String DB_FIELD_COMPANY = "companyRecord";

    @JsonIgnore
    @ManyToOne(targetEntity = CompanyRecord.class, fetch = FetchType.LAZY)
    @JoinColumn(name = DB_FIELD_COMPANY + DatabaseEntry.REF)
    private CompanyRecord companyRecord;

    public void setCompanyRecord(CompanyRecord companyRecord) {
        this.companyRecord = companyRecord;
    }

    public CompanyRecord getCompanyRecord() {
        return this.companyRecord;
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

    public String getField() {
        return field;
    }

    public void setField(String field) {
        this.field = field;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public void setTimestamp(OffsetDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public Long getOldItem() {
        return oldItem;
    }

    public void setOldItem(Long oldItem) {
        this.oldItem = oldItem;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        CompanyDataEventRecord that = (CompanyDataEventRecord) o;
        return Objects.equals(field, that.field) &&
                Objects.equals(text, that.text) &&
                Objects.equals(oldItem, that.oldItem) &&
                Objects.equals(timestamp, that.timestamp) &&
                Objects.equals(getDafoUpdated(), that.getDafoUpdated());
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), field, text, oldItem, timestamp, getDafoUpdated());
    }
}
