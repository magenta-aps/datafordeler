package dk.magenta.datafordeler.cvr.records;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import dk.magenta.datafordeler.core.database.DatabaseEntry;
import dk.magenta.datafordeler.cvr.CvrPlugin;
import dk.magenta.datafordeler.cvr.RecordSet;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.hibernate.annotations.Where;

import javax.persistence.*;
import java.util.*;
import java.util.function.Consumer;

/**
 * Record for Company status data.
 */
@Entity
@Table(name = CvrPlugin.DEBUG_TABLE_PREFIX + FusionSplitRecord.TABLE_NAME, indexes = {
        @Index(name = CvrPlugin.DEBUG_TABLE_PREFIX + FusionSplitRecord.TABLE_NAME + "__company", columnList = FusionSplitRecord.DB_FIELD_COMPANY + DatabaseEntry.REF + "," + FusionSplitRecord.DB_FIELD_SPLIT),
})
@JsonIgnoreProperties(ignoreUnknown = true)
public class FusionSplitRecord extends CvrNontemporalDataRecord {

    public static final String TABLE_NAME = "cvr_record_fusion";

    public static final String DB_FIELD_SPLIT = "split";

    @JsonIgnore
    public String getFieldName() {
        return TABLE_NAME;
    }

    @Column(name = DB_FIELD_SPLIT)
    @JsonIgnore
    private boolean split;

    public void setSplit(boolean split) {
        this.split = split;
    }

    public boolean isSplit() {
        return this.split;
    }

    public static final String DB_FIELD_ORGANIZATION_UNITNUMBER = "organizationUnitNumber";
    public static final String IO_FIELD_ORGANIZATION_UNITNUMBER = "enhedsNummerOrganisation";

    @Column(name = DB_FIELD_ORGANIZATION_UNITNUMBER)
    @JsonProperty(value = IO_FIELD_ORGANIZATION_UNITNUMBER)
    private long organizationUnitNumber;

    public long getOrganizationUnitNumber() {
        return this.organizationUnitNumber;
    }

    public void setOrganizationUnitNumber(long organizationUnitNumber) {
        this.organizationUnitNumber = organizationUnitNumber;
    }


    public static final String DB_FIELD_ORGANIZATION_NAME = "name";
    public static final String IO_FIELD_ORGANIZATION_NAME = "organisationsNavn";

    @OneToMany(mappedBy = BaseNameRecord.DB_FIELD_FUSION, targetEntity = BaseNameRecord.class, cascade = CascadeType.ALL)
        @JsonProperty(value = IO_FIELD_ORGANIZATION_NAME)
    private Set<BaseNameRecord> name;

    public RecordSet<BaseNameRecord> getName() {
        return new RecordSet<>(this.name);
    }

    public void setName(Set<BaseNameRecord> name) {
        this.name = name;
        for (BaseNameRecord nameRecord : name) {
            nameRecord.setFusionSplitRecord(this);
        }
    }

    public void addName(BaseNameRecord name) {
        if (name != null && !this.name.contains(name)) {
            name.setFusionSplitRecord(this);
            this.name.add(name);
        }
    }


    public static final String DB_FIELD_INCOMING = "incoming";
    public static final String IO_FIELD_INCOMING = "indgaaende";

    @OneToMany(mappedBy = AttributeRecord.DB_FIELD_FUSION, targetEntity = AttributeRecord.class, cascade = CascadeType.ALL)
    @Where(clause = AttributeRecord.DB_FIELD_FUSION_OUTGOING + "=false")
    @JsonProperty(value = IO_FIELD_INCOMING)
    private Set<AttributeRecord> incoming = new HashSet<>();

    public void setIncoming(Set<AttributeRecord> incoming) {
        this.incoming = incoming;
        for (AttributeRecord attributeRecord : incoming) {
            attributeRecord.setFusionSplitRecord(this);
            attributeRecord.setFusionOutgoing(false);
        }
    }

    public void addIncoming(AttributeRecord attribute) {
        if (attribute != null && !this.incoming.contains(attribute)) {
            attribute.setFusionSplitRecord(this);
            attribute.setFusionOutgoing(false);
            this.incoming.add(attribute);
        }
    }

    public void mergeIncoming(AttributeRecord otherRecord) {
        if (otherRecord != null) {
            String otherType = otherRecord.getType();
            String otherValueType = otherRecord.getValueType();
            int otherSequenceNumber = otherRecord.getSequenceNumber();
            for (AttributeRecord attributeRecord : this.incoming) {
                if (Objects.equals(attributeRecord.getType(), otherType) && Objects.equals(attributeRecord.getValueType(), otherValueType) && attributeRecord.getSequenceNumber() == otherSequenceNumber) {
                    attributeRecord.merge(otherRecord);
                    return;
                }
            }
            this.addIncoming(otherRecord);
        }
    }

    public AttributeRecordSet getIncoming() {
        return new AttributeRecordSet(this.incoming);
    }


    public static final String DB_FIELD_OUTGOING = "outgoing";
    public static final String IO_FIELD_OUTGOING = "udgaaende";

    @OneToMany(mappedBy = AttributeRecord.DB_FIELD_FUSION, targetEntity = AttributeRecord.class, cascade = CascadeType.ALL)
    @Where(clause = AttributeRecord.DB_FIELD_FUSION_OUTGOING + "=true")
    @JsonProperty(value = IO_FIELD_OUTGOING)
    private Set<AttributeRecord> outgoing = new HashSet<>();

    public void setOutgoing(Set<AttributeRecord> outgoing) {
        this.outgoing = outgoing;
        for (AttributeRecord attributeRecord : outgoing) {
            attributeRecord.setFusionSplitRecord(this);
            attributeRecord.setFusionOutgoing(true);
        }
    }

    public void addOutgoing(AttributeRecord attribute) {
        if (attribute != null && !this.outgoing.contains(attribute)) {
            attribute.setFusionSplitRecord(this);
            attribute.setFusionOutgoing(true);
            this.outgoing.add(attribute);
        }
    }

    public void mergeOutgoing(AttributeRecord otherRecord) {
        if (otherRecord != null) {
            String otherType = otherRecord.getType();
            String otherValueType = otherRecord.getValueType();
            int otherSequenceNumber = otherRecord.getSequenceNumber();
            for (AttributeRecord attributeRecord : this.outgoing) {
                if (Objects.equals(attributeRecord.getType(), otherType) && Objects.equals(attributeRecord.getValueType(), otherValueType) && attributeRecord.getSequenceNumber() == otherSequenceNumber) {
                    attributeRecord.merge(otherRecord);
                    return;
                }
            }
            this.addOutgoing(otherRecord);
        }
    }

    public AttributeRecordSet getOutgoing() {
        return new AttributeRecordSet(this.outgoing);
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FusionSplitRecord that = (FusionSplitRecord) o;
        return split == that.split &&
                organizationUnitNumber == that.organizationUnitNumber &&
                Objects.equals(name, that.name) &&
                Objects.equals(incoming, that.incoming) &&
                Objects.equals(outgoing, that.outgoing);
    }

    @Override
    public int hashCode() {
        return Objects.hash(split, organizationUnitNumber, name, incoming, outgoing);
    }

    public boolean merge(FusionSplitRecord otherRecord) {
        if (otherRecord != null && !Objects.equals(this.getId(), otherRecord.getId())) {
            for (BaseNameRecord nameRecord : otherRecord.getName()) {
                this.addName(nameRecord);
            }
            for (AttributeRecord attributeRecord : otherRecord.getIncoming()) {
                this.mergeIncoming(attributeRecord);
            }
            for (AttributeRecord attributeRecord : otherRecord.getOutgoing()) {
                this.mergeOutgoing(attributeRecord);
            }
            return true;
        }
        return false;
    }

    @Override
    public List<CvrRecord> subs() {
        ArrayList<CvrRecord> subs = new ArrayList<>(super.subs());
        subs.addAll(this.name);
        subs.addAll(this.incoming);
        subs.addAll(this.outgoing);
        return subs;
    }

    @Override
    public void traverse(Consumer<RecordSet<? extends CvrRecord>> setCallback, Consumer<CvrRecord> itemCallback) {
        this.getIncoming().traverse(setCallback, itemCallback);
        this.getOutgoing().traverse(setCallback, itemCallback);
        this.getName().traverse(setCallback, itemCallback);
        super.traverse(setCallback, itemCallback);
    }
}
