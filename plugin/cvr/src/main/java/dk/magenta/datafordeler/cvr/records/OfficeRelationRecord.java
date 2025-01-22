package dk.magenta.datafordeler.cvr.records;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import dk.magenta.datafordeler.core.database.DatabaseEntry;
import dk.magenta.datafordeler.cvr.CvrPlugin;
import dk.magenta.datafordeler.cvr.RecordSet;
import jakarta.persistence.*;
import org.hibernate.Session;

import java.util.*;
import java.util.function.Consumer;

/**
 * Record for one participant on a Company or CompanyUnit
 */
@Entity
@Table(name = CvrPlugin.DEBUG_TABLE_PREFIX + OfficeRelationRecord.TABLE_NAME, indexes = {
        @Index(name = CvrPlugin.DEBUG_TABLE_PREFIX + OfficeRelationRecord.TABLE_NAME + "__relation", columnList = OfficeRelationRecord.DB_FIELD_COMPANY_RELATION + DatabaseEntry.REF),
        @Index(name = CvrPlugin.DEBUG_TABLE_PREFIX + OfficeRelationRecord.TABLE_NAME + "__unit", columnList = OfficeRelationRecord.DB_FIELD_UNIT + DatabaseEntry.REF),
})
@JsonIgnoreProperties(ignoreUnknown = true)
public class OfficeRelationRecord extends CvrNontemporalRecord {

    public static final String TABLE_NAME = CompanyParticipantRelationRecord.TABLE_NAME + "_office";

    public static final String DB_FIELD_COMPANY_RELATION = "companyParticipantRelationRecord";

    @JsonIgnore
    public String getFieldName() {
        return TABLE_NAME;
    }

    @ManyToOne(targetEntity = CompanyParticipantRelationRecord.class, fetch = FetchType.LAZY)
    @JoinColumn(name = DB_FIELD_COMPANY_RELATION + DatabaseEntry.REF)
    @JsonIgnore
    private CompanyParticipantRelationRecord companyParticipantRelationRecord;

    public void setCompanyParticipantRelationRecord(CompanyParticipantRelationRecord companyParticipantRelationRecord) {
        this.companyParticipantRelationRecord = companyParticipantRelationRecord;
    }


    public static final String DB_FIELD_UNIT = "officeRelationUnitRecord";
    public static final String IO_FIELD_UNIT = "penhed";

    @OneToOne(targetEntity = OfficeRelationUnitRecord.class, cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinColumn(name = DB_FIELD_UNIT + DatabaseEntry.REF)
        @JsonProperty(value = IO_FIELD_UNIT)
    private OfficeRelationUnitRecord officeRelationUnitRecord;

    public OfficeRelationUnitRecord getOfficeRelationUnitRecord() {
        return this.officeRelationUnitRecord;
    }

    public void setOfficeRelationUnitRecord(OfficeRelationUnitRecord officeRelationUnitRecord) {
        this.officeRelationUnitRecord = officeRelationUnitRecord;
    }

    @JsonIgnore
    public Long getOfficeUnitNumber() {
        return this.officeRelationUnitRecord != null ? this.officeRelationUnitRecord.getUnitNumber() : null;
    }


    public static final String DB_FIELD_ATTRIBUTES = "attributes";
    public static final String IO_FIELD_ATTRIBUTES = "attributter";

    @OneToMany(targetEntity = AttributeRecord.class, mappedBy = AttributeRecord.DB_FIELD_OFFICE, cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
        @JsonProperty(value = IO_FIELD_ATTRIBUTES)
    private Set<AttributeRecord> attributes = new HashSet<>();

    public void setAttributes(Set<AttributeRecord> attributes) {
        this.attributes = attributes;
        for (AttributeRecord attributeRecord : attributes) {
            attributeRecord.setOfficeRelationRecord(this);
        }
    }

    public void addAttribute(AttributeRecord attribute) {
        if (attribute != null) {
            attribute.setOfficeRelationRecord(this);
            this.attributes.add(attribute);
        }
    }

    public void mergeAttribute(AttributeRecord otherRecord) {
        if (otherRecord != null) {
            String otherType = otherRecord.getType();
            String otherValueType = otherRecord.getValueType();
            int otherSequenceNumber = otherRecord.getSequenceNumber();
            for (AttributeRecord attributeRecord : this.attributes) {
                if (Objects.equals(attributeRecord.getType(), otherType) && Objects.equals(attributeRecord.getValueType(), otherValueType) && attributeRecord.getSequenceNumber() == otherSequenceNumber) {
                    attributeRecord.merge(otherRecord);
                    return;
                }
            }
            this.addAttribute(otherRecord);
        }
    }

    public AttributeRecordSet getAttributes() {
        return new AttributeRecordSet(this.attributes);
    }


    public void wire(Session session) {
        if (this.officeRelationUnitRecord != null) {
            this.officeRelationUnitRecord.wire(session);
        }
    }

    public void merge(OfficeRelationRecord other) {
        if (other != null) {
            if (this.officeRelationUnitRecord != null) {
                this.officeRelationUnitRecord.merge(other.getOfficeRelationUnitRecord());
            } else {
                this.setOfficeRelationUnitRecord(other.getOfficeRelationUnitRecord());
            }
            for (AttributeRecord attribute : other.getAttributes()) {
                this.mergeAttribute(attribute);
            }
        }
    }

    @Override
    public List<CvrRecord> subs() {
        ArrayList<CvrRecord> subs = new ArrayList<>(super.subs());
        subs.addAll(this.attributes);
        if (this.officeRelationUnitRecord != null) {
            subs.add(this.officeRelationUnitRecord);
        }
        return subs;
    }

    @Override
    public void traverse(Consumer<RecordSet<? extends CvrRecord>> setCallback, Consumer<CvrRecord> itemCallback) {
        this.getAttributes().traverse(setCallback, itemCallback);
        if (this.officeRelationUnitRecord != null) {
            this.officeRelationUnitRecord.traverse(setCallback, itemCallback);
        }
        super.traverse(setCallback, itemCallback);
    }
}
