package dk.magenta.datafordeler.cvr.records;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import dk.magenta.datafordeler.core.database.DatabaseEntry;
import dk.magenta.datafordeler.cvr.CvrPlugin;

import javax.persistence.*;

/**
 * Record for Company, CompanyUnit or Participant attribute values.
 */
@Entity
@Table(name = CvrPlugin.DEBUG_TABLE_PREFIX + AttributeValueRecord.TABLE_NAME, indexes = {
        @Index(name = CvrPlugin.DEBUG_TABLE_PREFIX + AttributeValueRecord.TABLE_NAME + "__attribute", columnList = AttributeValueRecord.DB_FIELD_ATTRIBUTE + DatabaseEntry.REF),
        @Index(name = CvrPlugin.DEBUG_TABLE_PREFIX + AttributeValueRecord.TABLE_NAME + "__" + CvrBitemporalRecord.DB_FIELD_LAST_UPDATED, columnList = CvrBitemporalRecord.DB_FIELD_LAST_UPDATED),
        @Index(name = CvrPlugin.DEBUG_TABLE_PREFIX + AttributeValueRecord.TABLE_NAME + "__" + CvrRecordPeriod.DB_FIELD_VALID_FROM, columnList = CvrRecordPeriod.DB_FIELD_VALID_FROM),
        @Index(name = CvrPlugin.DEBUG_TABLE_PREFIX + AttributeValueRecord.TABLE_NAME + "__" + CvrRecordPeriod.DB_FIELD_VALID_TO, columnList = CvrRecordPeriod.DB_FIELD_VALID_TO)
})
@JsonIgnoreProperties(ignoreUnknown = true)
public class AttributeValueRecord extends BaseAttributeValueRecord {

    public static final String TABLE_NAME = "cvr_record_attribute_value";

    public static final String DB_FIELD_ATTRIBUTE = "attribute";

    @JsonIgnore
    public String getFieldName() {
        return TABLE_NAME;
    }

    @ManyToOne(targetEntity = AttributeRecord.class, fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = DB_FIELD_ATTRIBUTE + DatabaseEntry.REF)
    @JsonIgnore
    private AttributeRecord attribute;

    public void setAttribute(AttributeRecord attribute) {
        this.attribute = attribute;
    }

}
