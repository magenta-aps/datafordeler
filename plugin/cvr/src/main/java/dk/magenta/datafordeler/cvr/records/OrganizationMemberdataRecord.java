package dk.magenta.datafordeler.cvr.records;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import dk.magenta.datafordeler.core.database.DatabaseEntry;
import dk.magenta.datafordeler.cvr.CvrPlugin;
import dk.magenta.datafordeler.cvr.RecordSet;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import javax.persistence.*;
import java.util.*;
import java.util.function.Consumer;


@Entity
@Table(name = CvrPlugin.DEBUG_TABLE_PREFIX + OrganizationMemberdataRecord.TABLE_NAME, indexes = {
        @Index(name = CvrPlugin.DEBUG_TABLE_PREFIX + OrganizationMemberdataRecord.TABLE_NAME + "__organization", columnList = OrganizationMemberdataRecord.DB_FIELD_ORGANIZATION + DatabaseEntry.REF)
})
@JsonIgnoreProperties(ignoreUnknown = true)
public class OrganizationMemberdataRecord extends CvrRecord {

    public static final String TABLE_NAME = OrganizationRecord.TABLE_NAME + "_memberdata";

    public static final String DB_FIELD_ORGANIZATION = "organizationRecord";

    @JsonIgnore
    public String getFieldName() {
        return TABLE_NAME;
    }

    @ManyToOne(targetEntity = OrganizationRecord.class, fetch = FetchType.LAZY)
    @JoinColumn(name = DB_FIELD_ORGANIZATION + DatabaseEntry.REF)
    @JsonIgnore
    private OrganizationRecord organizationRecord;

    public void setOrganizationRecord(OrganizationRecord organizationRecord) {
        this.organizationRecord = organizationRecord;
    }


    public static final String DB_FIELD_INDEX = "recordIndex";

    @Column(name = DB_FIELD_INDEX)
    @JsonIgnore
    private int index;

    public int getIndex() {
        return this.index;
    }

    public void setIndex(int index) {
        this.index = index;
    }


    public static final String DB_FIELD_ATTRIBUTES = "attributes";
    public static final String IO_FIELD_ATTRIBUTES = "attributter";

    @OneToMany(mappedBy = AttributeRecord.DB_FIELD_ORGANIZATION_MEMBERDATA, targetEntity = AttributeRecord.class, cascade = CascadeType.ALL)
    @JsonProperty(value = IO_FIELD_ATTRIBUTES)
    public Set<AttributeRecord> attributes = new HashSet<>();

    public void setAttributes(Set<AttributeRecord> attributes) {
        this.attributes = attributes;
        for (AttributeRecord attributeRecord : attributes) {
            attributeRecord.setOrganizationMemberdataRecord(this);
        }
    }

    public void addAttribute(AttributeRecord attribute) {
        if (attribute != null && !this.attributes.contains(attribute)) {
            attribute.setOrganizationMemberdataRecord(this);
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


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OrganizationMemberdataRecord that = (OrganizationMemberdataRecord) o;
        return Objects.equals(attributes, that.attributes);
    }

    @Override
    public int hashCode() {
        return Objects.hash(attributes);
    }

    public void merge(OrganizationMemberdataRecord other) {
        if (other != null) {
            for (AttributeRecord attribute : other.getAttributes()) {
                this.mergeAttribute(attribute);
            }
        }
    }

    @Override
    public List<CvrRecord> subs() {
        ArrayList<CvrRecord> subs = new ArrayList<>(super.subs());
        subs.addAll(this.attributes);
        return subs;
    }

    public void traverse(Consumer<RecordSet<? extends CvrRecord>> setCallback, Consumer<CvrRecord> itemCallback) {
        this.getAttributes().traverse(setCallback, itemCallback);
        super.traverse(setCallback, itemCallback);
    }
}
