package dk.magenta.datafordeler.cvr.records;

import com.fasterxml.jackson.annotation.JsonFilter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import dk.magenta.datafordeler.core.database.Bitemporal;
import dk.magenta.datafordeler.core.database.Monotemporal;
import dk.magenta.datafordeler.core.database.Nontemporal;
import dk.magenta.datafordeler.cvr.BitemporalSet;
import dk.magenta.datafordeler.cvr.CvrPlugin;
import dk.magenta.datafordeler.cvr.RecordSet;
import org.hibernate.Session;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.Filters;

import javax.persistence.*;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.function.Consumer;

/**
 * Record for one participant on a Company or CompanyUnit
 */
@Entity
@Table(name = CvrPlugin.DEBUG_TABLE_PREFIX + RelationParticipantRecord.TABLE_NAME)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonFilter("ParticipantRecordFilter")  // Keeps businessKey from being outputted in normal outputwrappers
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
    private Long businessKey;

    public Long getBusinessKey() {
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
    @Filters({
            @Filter(name = Bitemporal.FILTER_EFFECTFROM_AFTER, condition = CvrBitemporalRecord.FILTERLOGIC_EFFECTFROM_AFTER),
            @Filter(name = Bitemporal.FILTER_EFFECTFROM_BEFORE, condition = CvrBitemporalRecord.FILTERLOGIC_EFFECTFROM_BEFORE),
            @Filter(name = Bitemporal.FILTER_EFFECTTO_AFTER, condition = CvrBitemporalRecord.FILTERLOGIC_EFFECTTO_AFTER),
            @Filter(name = Bitemporal.FILTER_EFFECTTO_BEFORE, condition = CvrBitemporalRecord.FILTERLOGIC_EFFECTTO_BEFORE),
            @Filter(name = Monotemporal.FILTER_REGISTRATIONFROM_AFTER, condition = CvrBitemporalRecord.FILTERLOGIC_REGISTRATIONFROM_AFTER),
            @Filter(name = Monotemporal.FILTER_REGISTRATIONFROM_BEFORE, condition = CvrBitemporalRecord.FILTERLOGIC_REGISTRATIONFROM_BEFORE),
            @Filter(name = Monotemporal.FILTER_REGISTRATIONTO_AFTER, condition = CvrBitemporalRecord.FILTERLOGIC_REGISTRATIONTO_AFTER),
            @Filter(name = Monotemporal.FILTER_REGISTRATIONTO_BEFORE, condition = CvrBitemporalRecord.FILTERLOGIC_REGISTRATIONTO_BEFORE),
            @Filter(name = Nontemporal.FILTER_LASTUPDATED_AFTER, condition = CvrNontemporalRecord.FILTERLOGIC_LASTUPDATED_AFTER),
            @Filter(name = Nontemporal.FILTER_LASTUPDATED_BEFORE, condition = CvrNontemporalRecord.FILTERLOGIC_LASTUPDATED_BEFORE)
    })
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

    public BitemporalSet<BaseNameRecord> getNames() {
        return new BitemporalSet<>(this.names);
    }


    public static final String DB_FIELD_LOCATION_ADDRESS = "locationAddress";
    public static final String IO_FIELD_LOCATION_ADDRESS = "beliggenhedsadresse";

    @OneToMany(targetEntity = AddressRecord.class, mappedBy = AddressRecord.DB_FIELD_PARTICIPANT_RELATION, cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @Filters({
            @Filter(name = Bitemporal.FILTER_EFFECTFROM_AFTER, condition = CvrBitemporalRecord.FILTERLOGIC_EFFECTFROM_AFTER),
            @Filter(name = Bitemporal.FILTER_EFFECTFROM_BEFORE, condition = CvrBitemporalRecord.FILTERLOGIC_EFFECTFROM_BEFORE),
            @Filter(name = Bitemporal.FILTER_EFFECTTO_AFTER, condition = CvrBitemporalRecord.FILTERLOGIC_EFFECTTO_AFTER),
            @Filter(name = Bitemporal.FILTER_EFFECTTO_BEFORE, condition = CvrBitemporalRecord.FILTERLOGIC_EFFECTTO_BEFORE),
            @Filter(name = Monotemporal.FILTER_REGISTRATIONFROM_AFTER, condition = CvrBitemporalRecord.FILTERLOGIC_REGISTRATIONFROM_AFTER),
            @Filter(name = Monotemporal.FILTER_REGISTRATIONFROM_BEFORE, condition = CvrBitemporalRecord.FILTERLOGIC_REGISTRATIONFROM_BEFORE),
            //@Filter(name = Monotemporal.FILTER_REGISTRATIONTO_AFTER, condition = CvrBitemporalRecord.FILTERLOGIC_REGISTRATIONTO_AFTER),
            //@Filter(name = Monotemporal.FILTER_REGISTRATIONTO_BEFORE, condition = CvrBitemporalRecord.FILTERLOGIC_REGISTRATIONTO_BEFORE),
            @Filter(name = Nontemporal.FILTER_LASTUPDATED_AFTER, condition = CvrNontemporalRecord.FILTERLOGIC_LASTUPDATED_AFTER),
            @Filter(name = Nontemporal.FILTER_LASTUPDATED_BEFORE, condition = CvrNontemporalRecord.FILTERLOGIC_LASTUPDATED_BEFORE)
    })
    @JsonProperty(value = IO_FIELD_LOCATION_ADDRESS)
    private Set<AddressRecord> locationAddress = new HashSet<>();

    public void setLocationAddress(Set<AddressRecord> locationAddress) {
        this.locationAddress = locationAddress;
        for (AddressRecord address : locationAddress) {
            address.setRelationParticipantRecord(this);
        }
    }

    public void addLocationAddress(AddressRecord record) {
        if (record != null) {
            record.setRelationParticipantRecord(this);
            this.locationAddress.add(record);
        }
    }

    public BitemporalSet<AddressRecord> getLocationAddress() {
        return new BitemporalSet<>(this.locationAddress);
    }


    @OneToOne(targetEntity = CompanyParticipantRelationRecord.class, mappedBy = CompanyParticipantRelationRecord.DB_FIELD_PARTICIPANT_RELATION)
    @JsonIgnore
    private CompanyParticipantRelationRecord companyParticipantRelationRecord;

    public void setCompanyParticipantRelationRecord(CompanyParticipantRelationRecord companyParticipantRelationRecord) {
        this.companyParticipantRelationRecord = companyParticipantRelationRecord;
    }


    public UUID generateUUID() {
        String uuidInput = "participant:" + this.unitType + "/" + this.unitNumber;
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


    @Override
    public void traverse(Consumer<RecordSet<? extends CvrRecord>> setCallback, Consumer<CvrRecord> itemCallback) {
        this.getNames().traverse(setCallback, itemCallback);
        this.getLocationAddress().traverse(setCallback, itemCallback);
        super.traverse(setCallback, itemCallback);
    }
}
