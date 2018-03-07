package dk.magenta.datafordeler.cvr.records;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import dk.magenta.datafordeler.core.database.DatabaseEntry;
import org.hibernate.Session;

import javax.persistence.*;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * Record for one participating organization on a Company or CompanyUnit
 */
@Entity
@Table(name = OrganizationRecord.TABLE_NAME, indexes = {
        @Index(name = OrganizationRecord.TABLE_NAME + "__relation", columnList = OrganizationRecord.DB_FIELD_PARTICIPANT_RELATION + DatabaseEntry.REF)
})
public class OrganizationRecord extends DatabaseEntry {

    public static final String TABLE_NAME = CompanyParticipantRelationRecord.TABLE_NAME + "_organization";

    public static final String DB_FIELD_PARTICIPANT_RELATION = "companyParticipantRelationRecord";


    @ManyToOne(targetEntity = CompanyParticipantRelationRecord.class)
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
    @JsonProperty(value = IO_FIELD_NAME)
    public Set<BaseNameRecord> names;

    public Set<BaseNameRecord> getNames() {
        return this.names;
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

    public Set<AttributeRecord> getAttributes() {
        return this.attributes;
    }



    public static final String DB_FIELD_MEMBERDATA = "memberData";
    public static final String IO_FIELD_MEMBERDATA = "medlemsData";

    @OneToMany(mappedBy = OrganizationMemberdataRecord.DB_FIELD_ORGANIZATION, targetEntity = OrganizationMemberdataRecord.class, cascade = CascadeType.ALL)
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

    public Set<OrganizationMemberdataRecord> getMemberData() {
        return this.memberData;
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
            this.addAttribute(attribute);
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
}
