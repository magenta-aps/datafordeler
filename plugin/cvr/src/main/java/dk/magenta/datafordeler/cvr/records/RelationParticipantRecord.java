package dk.magenta.datafordeler.cvr.records;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import dk.magenta.datafordeler.cvr.CvrPlugin;
import org.hibernate.Session;

import javax.persistence.*;
import java.time.OffsetDateTime;
import java.util.*;

/**
 * Record for one participant on a Company or CompanyUnit
 */
@Entity
@Table(name = CvrPlugin.DEBUG_TABLE_PREFIX + RelationParticipantRecord.TABLE_NAME)
@JsonIgnoreProperties(ignoreUnknown = true)
public class RelationParticipantRecord extends CvrBitemporalRecord {

    public static final String TABLE_NAME = CompanyParticipantRelationRecord.TABLE_NAME + "_participant";

    public static final String DB_FIELD_UNITNUMBER = "unitNumber";
    public static final String IO_FIELD_UNITNUMBER = "enhedsNummer";

    @JsonIgnore
    public String getFieldName() {
        return TABLE_NAME;
    }

    @Column(name = DB_FIELD_UNITNUMBER)
    @JsonProperty(value = IO_FIELD_UNITNUMBER)
    private long unitNumber;

    public long getUnitNumber() {
        return this.unitNumber;
    }



    public static final String DB_FIELD_UNITTYPE = "unitType";
    public static final String IO_FIELD_UNITTYPE = "enhedstype";

    @Column(name = DB_FIELD_UNITTYPE)
    @JsonProperty(value = IO_FIELD_UNITTYPE)
    public String unitType;

    public String getUnitType() {
        return this.unitType;
    }





    public static final String DB_FIELD_BUSINESS_KEY = "businessKey";
    public static final String IO_FIELD_BUSINESS_KEY = "forretningsnoegle";

    @Column(name = DB_FIELD_BUSINESS_KEY)
    @JsonProperty(value = IO_FIELD_BUSINESS_KEY)
    public Integer businessKey;

    public Integer getBusinessKey() {
        return this.businessKey;
    }



    //This field is null for every single input
    public static final String IO_FIELD_ORGANIZATION_TYPE = "organisationstype";

    @Transient
    @JsonProperty(value = IO_FIELD_ORGANIZATION_TYPE)
    public Integer organizationType;

    public Integer getOrganizationType() {
        return this.organizationType;
    }



    public static final String DB_FIELD_NAME = "names";
    public static final String IO_FIELD_NAME = "navne";

    @OneToMany(targetEntity = BaseNameRecord.class, mappedBy = BaseNameRecord.DB_FIELD_PARTICIPANT_RELATION, cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonProperty(value = IO_FIELD_NAME)
    private Set<BaseNameRecord> names = new HashSet<>();

    public void setNames(Set<BaseNameRecord> names) {
        this.names = names;
        for (BaseNameRecord name : names) {
            name.setRelationParticipantRecord(this);
        }
    }

    public void addName(BaseNameRecord record) {
        if (record != null) {
            record.setRelationParticipantRecord(this);
            this.names.add(record);
        }
    }

    public Set<BaseNameRecord> getNames() {
        return this.names;
    }



    public static final String DB_FIELD_LOCATION_ADDRESS = "locationAddress";
    public static final String IO_FIELD_LOCATION_ADDRESS = "beliggenhedsadresse";

    @OneToMany(targetEntity = AddressRecord.class, mappedBy = AddressRecord.DB_FIELD_PARTICIPANT_RELATION, cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonProperty(value = IO_FIELD_LOCATION_ADDRESS)
    private Set<AddressRecord> locationAddress = new HashSet<>();

    public void setLocationAddress(Set<AddressRecord> locationAddress) {
        this.locationAddress = locationAddress;
        for (AddressRecord name : locationAddress) {
            name.setRelationParticipantRecord(this);
        }
    }

    public void addLocationAddress(AddressRecord record) {
        if (record != null) {
            record.setRelationParticipantRecord(this);
            this.locationAddress.add(record);
        }
    }

    public Set<AddressRecord> getLocationAddress() {
        return this.locationAddress;
    }




    @OneToOne(targetEntity = CompanyParticipantRelationRecord.class, mappedBy = CompanyParticipantRelationRecord.DB_FIELD_PARTICIPANT_RELATION)
    @JsonIgnore
    private CompanyParticipantRelationRecord companyParticipantRelationRecord;

    public void setCompanyParticipantRelationRecord(CompanyParticipantRelationRecord companyParticipantRelationRecord) {
        this.companyParticipantRelationRecord = companyParticipantRelationRecord;
    }



    public UUID generateUUID() {
        String uuidInput = "participant:"+this.unitType+"/"+this.unitNumber;
        return UUID.nameUUIDFromBytes(uuidInput.getBytes());
    }

    @Override
    public OffsetDateTime getRegistrationFrom() {
        OffsetDateTime registrationFrom = super.getRegistrationFrom();
        if (registrationFrom == null) {
            registrationFrom = this.getLastUpdated();
        }
        return registrationFrom;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        RelationParticipantRecord that = (RelationParticipantRecord) o;
        return unitNumber == that.unitNumber &&
                Objects.equals(unitType, that.unitType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), unitNumber, unitType);
    }

    public void merge(RelationParticipantRecord other) {
        if (other != null) {
            for (BaseNameRecord name : other.getNames()) {
                this.addName(name);
            }
            for (AddressRecord address : other.getLocationAddress()) {
                this.addLocationAddress(address);
            }
        }
    }

    public void wire(Session session) {
        for (AddressRecord address : this.locationAddress) {
            address.wire(session);
        }
    }

    @Override
    public List<CvrRecord> subs() {
        ArrayList<CvrRecord> subs = new ArrayList<>(super.subs());
        subs.addAll(this.names);
        subs.addAll(this.locationAddress);
        return subs;
    }
}
