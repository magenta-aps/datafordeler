package dk.magenta.datafordeler.cvr.records;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import dk.magenta.datafordeler.core.database.DatabaseEntry;

import javax.persistence.*;

/**
 * Record for Company, CompanyUnit or Participant attribute values.
 */
@Entity
@Table(name = AttributeValueRecord.TABLE_NAME, indexes = {
        @Index(name = AttributeValueRecord.TABLE_NAME + "__attribute", columnList = AttributeValueRecord.DB_FIELD_ATTRIBUTE + DatabaseEntry.REF)
})
@JsonIgnoreProperties(ignoreUnknown = true)
public class AttributeValueRecord extends BaseAttributeValueRecord {

    public static final String TABLE_NAME = "cvr_record_attribute_value";

    public static final String DB_FIELD_ATTRIBUTE = "attribute";

    @ManyToOne(targetEntity = AttributeRecord.class, fetch = FetchType.LAZY)
    @JoinColumn(name = DB_FIELD_ATTRIBUTE + DatabaseEntry.REF)
    @JsonIgnore
    private AttributeRecord attribute;

    public void setAttribute(AttributeRecord attribute) {
        this.attribute = attribute;
    }

}
