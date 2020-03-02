package dk.magenta.datafordeler.cvr.records;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import dk.magenta.datafordeler.core.database.DatabaseEntry;
import dk.magenta.datafordeler.cvr.data.company.CompanyBaseData;
import dk.magenta.datafordeler.cvr.data.companyunit.CompanyUnitBaseData;
import dk.magenta.datafordeler.cvr.data.participant.ParticipantBaseData;
import org.hibernate.Session;

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



    @Override
    public void populateBaseData(CompanyBaseData baseData, Session session) {
        baseData.addAttribute(
                this.attribute.getType(),
                this.attribute.getValueType(),
                this.value,
                this.attribute.getSequenceNumber()
        );
    }

    @Override
    public void populateBaseData(CompanyUnitBaseData baseData, Session session) {
        baseData.addAttribute(
                this.attribute.getType(),
                this.attribute.getValueType(),
                this.value,
                this.attribute.getSequenceNumber()
        );
    }

    @Override
    public void populateBaseData(ParticipantBaseData baseData, Session session) {
        baseData.addAttribute(
                this.attribute.getType(),
                this.attribute.getValueType(),
                this.value,
                this.attribute.getSequenceNumber()
        );
    }

}
