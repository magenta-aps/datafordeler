package dk.magenta.datafordeler.cvr.records;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import dk.magenta.datafordeler.core.database.DatabaseEntry;
import dk.magenta.datafordeler.cvr.BitemporalSet;
import dk.magenta.datafordeler.cvr.CvrPlugin;
import dk.magenta.datafordeler.cvr.RecordSet;
import org.hibernate.Session;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import javax.persistence.*;
import java.util.*;
import java.util.function.Consumer;

/**
 * Record for one participating organization on a Company or CompanyUnit
 */
@Entity
@Table(name = CvrPlugin.DEBUG_TABLE_PREFIX + OrganizationRecord.TABLE_NAME, indexes = {
        @Index(name = CvrPlugin.DEBUG_TABLE_PREFIX + OrganizationRecord.TABLE_NAME + "__relation", columnList = OrganizationRecord.DB_FIELD_PARTICIPANT_RELATION + DatabaseEntry.REF)
})
@JsonIgnoreProperties(ignoreUnknown = true)
public class OrganizationRecord extends CvrRecord {

    public static final String TABLE_NAME = CompanyParticipantRelationRecord.TABLE_NAME + "_organization";

    public static final String DB_FIELD_PARTICIPANT_RELATION = "companyParticipantRelationRecord";

    @JsonIgnore
    public String getFieldName() {
        return TABLE_NAME;
    }


    @ManyToOne(targetEntity = CompanyParticipantRelationRecord.class, fetch = FetchType.LAZY)
    @JoinColumn(name = DB_FIELD_PARTICIPANT_RELATION + DatabaseEntry.REF)
    @JsonIgnore
    private CompanyParticipantRelationRecord companyParticipantRelationRecord;

    public void setCompanyParticipantRelationRecord(CompanyParticipantRelationRecord companyParticipantRelationRecord) {
        this.companyParticipantRelationRecord = companyParticipantRelationRecord;
    }


    public static final String DB_FIELD_UNIT_NUMBER = "unitNumber";
    public static final String IO_FIELD_UNIT_NUMBER = "enhedsNummerOrganisation";


    @Column(name = DB_FIELD_UNIT_NUMBER)
    @JsonProperty(value = IO_FIELD_UNIT_NUMBER)
    private long unitNumber;

    public long getUnitNumber() {
        return this.unitNumber;
    }


    public static final String DB_FIELD_MAIN_TYPE = "mainType";
    public static final String IO_FIELD_MAIN_TYPE = "hovedtype";

    @Column(name = DB_FIELD_MAIN_TYPE)
    @JsonProperty(value = IO_FIELD_MAIN_TYPE)
    private String mainType;

    public String getMainType() {
        return this.mainType;
    }


    public static final String DB_FIELD_NAME = "names";
    public static final String IO_FIELD_NAME = "organisationsNavn";

    @OneToMany(mappedBy = BaseNameRecord.DB_FIELD_ORGANIZATION, targetEntity = BaseNameRecord.class, cascade = CascadeType.ALL)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JsonProperty(value = IO_FIELD_NAME)
    public Set<BaseNameRecord> names;

    public BitemporalSet<BaseNameRecord> getNames() {
        return new BitemporalSet<>(this.names);
    }

    public void setNames(Set<BaseNameRecord> names) {
        this.names = names;
        for (BaseNameRecord nameRecord : names) {
            nameRecord.setOrganizationRecord(this);
        }
    }

    public void addName(BaseNameRecord name) {
        if (name != null && !this.names.contains(name)) {
            name.setOrganizationRecord(this);
            this.names.add(name);
        }
    }


    public static final String DB_FIELD_ATTRIBUTES = "attributes";
    public static final String IO_FIELD_ATTRIBUTES = "attributter";

    @OneToMany(mappedBy = AttributeRecord.DB_FIELD_ORGANIZATION, targetEntity = AttributeRecord.class, cascade = CascadeType.ALL)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JsonProperty(value = IO_FIELD_ATTRIBUTES)
    public Set<AttributeRecord> attributes;

    public void setAttributes(Set<AttributeRecord> attributes) {
        this.attributes = attributes;
        for (AttributeRecord attributeRecord : attributes) {
            attributeRecord.setOrganizationRecord(this);
        }
    }

    public void addAttribute(AttributeRecord attribute) {
        if (attribute != null && !this.attributes.contains(attribute)) {
            attribute.setOrganizationRecord(this);
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


    public static final String DB_FIELD_MEMBERDATA = "memberData";
    public static final String IO_FIELD_MEMBERDATA = "medlemsData";

    @OneToMany(mappedBy = OrganizationMemberdataRecord.DB_FIELD_ORGANIZATION, targetEntity = OrganizationMemberdataRecord.class, cascade = CascadeType.ALL)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JsonProperty(value = IO_FIELD_MEMBERDATA)
    public Set<OrganizationMemberdataRecord> memberData = new HashSet<>();

    @JsonSetter(value = IO_FIELD_MEMBERDATA)
    public void setMemberData(List<OrganizationMemberdataRecord> memberData) {
        this.memberData.clear();
        this.memberData.addAll(memberData);
        int index = 0;
        for (OrganizationMemberdataRecord memberdataRecord : memberData) {
            memberdataRecord.setOrganizationRecord(this);
            memberdataRecord.setIndex(index);
            index++;
        }
    }

    private void setMemberData(Set<OrganizationMemberdataRecord> memberData) {
        this.memberData = memberData;
        for (OrganizationMemberdataRecord memberdataRecord : memberData) {
            memberdataRecord.setOrganizationRecord(this);
        }
    }

    public RecordSet<OrganizationMemberdataRecord> getMemberData() {
        return new RecordSet<>(this.memberData);
    }


    public void save(Session session) {
        session.save(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OrganizationRecord that = (OrganizationRecord) o;
        return unitNumber == that.unitNumber &&
                Objects.equals(mainType, that.mainType) &&
                Objects.equals(names, that.names) &&
                Objects.equals(attributes, that.attributes) &&
                Objects.equals(memberData, that.memberData);
    }

    @Override
    public int hashCode() {
        return Objects.hash(unitNumber, mainType, names, attributes, memberData);
    }

    public void merge(OrganizationRecord other) {
        for (BaseNameRecord name : other.getNames()) {
            this.addName(name);
        }
        for (AttributeRecord attribute : other.getAttributes()) {
            this.mergeAttribute(attribute);
        }
        for (OrganizationMemberdataRecord memberdata : other.getMemberData()) {
            if (memberdata != null) {
                if (this.memberData.isEmpty()) {
                    this.setMemberData(memberData);
                } else {
                    int otherIndex = memberdata.getIndex();
                    for (OrganizationMemberdataRecord ourMemberData : this.getMemberData()) {
                        if (ourMemberData.getIndex() == otherIndex) {
                            ourMemberData.merge(memberdata);
                        }
                    }
                }
            }
        }
    }

    @Override
    public List<CvrRecord> subs() {
        ArrayList<CvrRecord> subs = new ArrayList<>(super.subs());
        subs.addAll(this.names);
        subs.addAll(this.attributes);
        subs.addAll(this.memberData);
        return subs;
    }





    public void traverse(Consumer<RecordSet> setCallback, Consumer<CvrRecord> itemCallback) {
        super.traverse(setCallback, itemCallback);
        this.getMemberData().traverse(setCallback, itemCallback);
    }
}
