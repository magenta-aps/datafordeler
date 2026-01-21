package dk.magenta.datafordeler.cvr.records;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import dk.magenta.datafordeler.cvr.BitemporalSet;
import dk.magenta.datafordeler.cvr.CvrPlugin;
import dk.magenta.datafordeler.cvr.RecordSet;
import jakarta.persistence.*;
import org.hibernate.Session;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

/**
 * Record for one participant on a Company or CompanyUnit
 */
@Entity
@Table(name = CvrPlugin.DEBUG_TABLE_PREFIX + OfficeRelationUnitRecord.TABLE_NAME, indexes = {
        @Index(name = CvrPlugin.DEBUG_TABLE_PREFIX + OfficeRelationUnitRecord.TABLE_NAME + "__unit", columnList = OfficeRelationUnitRecord.DB_FIELD_UNITNUMBER),

        @Index(name = CvrPlugin.DEBUG_TABLE_PREFIX + OfficeRelationUnitRecord.TABLE_NAME + "__" + CvrBitemporalRecord.DB_FIELD_LAST_UPDATED, columnList = CvrBitemporalRecord.DB_FIELD_LAST_UPDATED),
        @Index(name = CvrPlugin.DEBUG_TABLE_PREFIX + OfficeRelationUnitRecord.TABLE_NAME + "__" + CvrRecordPeriod.DB_FIELD_VALID_FROM, columnList = CvrRecordPeriod.DB_FIELD_VALID_FROM),
        @Index(name = CvrPlugin.DEBUG_TABLE_PREFIX + OfficeRelationUnitRecord.TABLE_NAME + "__" + CvrRecordPeriod.DB_FIELD_VALID_TO, columnList = CvrRecordPeriod.DB_FIELD_VALID_TO)
})
@JsonIgnoreProperties(ignoreUnknown = true)
public class OfficeRelationUnitRecord extends CvrBitemporalRecord {

    public static final String TABLE_NAME = OfficeRelationRecord.TABLE_NAME + "_unit";

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

    public void setUnitNumber(long unitNumber) {
        this.unitNumber = unitNumber;
    }

    public static final String DB_FIELD_UNITTYPE = "unitType";
    public static final String IO_FIELD_UNITTYPE = "enhedsType";

    @Column(name = DB_FIELD_UNITTYPE)
    @JsonProperty(value = IO_FIELD_UNITTYPE)
    private String unitType;

    public String getUnitType() {
        return this.unitType;
    }

    public void setUnitType(String unitType) {
        this.unitType = unitType;
    }

    public static final String DB_FIELD_BUSINESS_KEY = "businessKey";
    public static final String IO_FIELD_BUSINESS_KEY = "forretningsnoegle";

    @Column(name = DB_FIELD_BUSINESS_KEY)
    @JsonProperty(value = IO_FIELD_BUSINESS_KEY)
    private long businessKey;

    public long getBusinessKey() {
        return this.businessKey;
    }

    public void setBusinessKey(long businessKey) {
        this.businessKey = businessKey;
    }

    //This field is null for every single input
    public static final String IO_FIELD_ORGANIZATION_TYPE = "organisationstype";

    @Transient
    @JsonProperty(value = IO_FIELD_ORGANIZATION_TYPE)
    public Integer organizationType;

    public Integer getOrganizationType() {
        return this.organizationType;
    }

    public void setOrganizationType(Integer organizationType) {
        this.organizationType = organizationType;
    }

    public static final String IO_FIELD_NAME = "navne";

    @OneToMany(mappedBy = BaseNameRecord.DB_FIELD_OFFICE_UNIT, targetEntity = BaseNameRecord.class, cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonProperty(value = IO_FIELD_NAME)
    private Set<BaseNameRecord> names = new HashSet<>();

    public BitemporalSet<BaseNameRecord, OfficeRelationUnitRecord> getNames() {
        return new BitemporalSet<>(this.names, BaseNameRecord.class, this, BaseNameRecord.DB_FIELD_OFFICE_UNIT);
    }

    public void setNames(Set<BaseNameRecord> names) {
        this.names = names;
        for (BaseNameRecord nameRecord : names) {
            nameRecord.setOfficeUnitRecord(this);
        }
    }

    public void addName(BaseNameRecord record) {
        if (record != null) {
            record.setOfficeUnitRecord(this);
            this.names.add(record);
        }
    }


    public static final String DB_FIELD_LOCATION_ADDRESS = "locationAddress";
    public static final String IO_FIELD_LOCATION_ADDRESS = "beliggenhedsadresse";

    @OneToMany(mappedBy = AddressRecord.DB_FIELD_OFFICE_UNIT, targetEntity = AddressRecord.class, cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonProperty(value = IO_FIELD_LOCATION_ADDRESS)
    private Set<AddressRecord> locationAddress = new HashSet<>();

    public void setLocationAddress(Set<AddressRecord> locationAddress) {
        for (AddressRecord record : locationAddress) {
            record.setType(AddressRecord.TYPE_LOCATION);
            record.setOfficeUnitRecord(this);
        }
        this.locationAddress = locationAddress;
    }

    public void addLocationAddress(AddressRecord record) {
        if (record != null) {
            record.setType(AddressRecord.TYPE_LOCATION);
            record.setOfficeUnitRecord(this);
            this.locationAddress.add(record);
        }
    }

    public BitemporalSet<AddressRecord, OfficeRelationUnitRecord> getLocationAddress() {
        return new BitemporalSet<>(this.locationAddress, AddressRecord.class, this, AddressRecord.DB_FIELD_OFFICE_UNIT);
    }

    // TODO: Postadresse

    public void wire(Session session) {
        for (AddressRecord address : this.locationAddress) {
            address.wire(session);
        }
    }

    public void merge(OfficeRelationUnitRecord other) {
        if (other != null) {
            for (BaseNameRecord name : other.getNames()) {
                this.addName(name);
            }
            for (AddressRecord address : other.getLocationAddress()) {
                this.addLocationAddress(address);
            }
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
    public void traverse(Consumer<RecordSet<? extends CvrRecord, ? extends CvrRecord>> setCallback, Consumer<CvrRecord> itemCallback) {
        super.traverse(setCallback, itemCallback);
        this.getNames().traverse(setCallback, itemCallback);
        this.getLocationAddress().traverse(setCallback, itemCallback);
    }


//    public ArrayList<CvrBitemporalRecord> closeRegistrations() {
//        ArrayList<CvrBitemporalRecord> updated = new ArrayList<>();
//        updated.addAll(CvrBitemporalRecord.closeRegistrations(this.names));
//        updated.addAll(CvrBitemporalRecord.closeRegistrations(this.locationAddress));
//        return updated;
//    }

    @Override
    protected Object clone() throws CloneNotSupportedException {
        OfficeRelationUnitRecord clone = (OfficeRelationUnitRecord) super.clone();
        clone.setBusinessKey(this.businessKey);
        clone.setUnitNumber(this.unitNumber);
        clone.setUnitType(this.unitType);
        clone.setOrganizationType(this.organizationType);

        HashSet<AddressRecord> clonedAddresses = new HashSet<>();
        for (AddressRecord addressRecord : this.locationAddress) {
            clonedAddresses.add((AddressRecord) addressRecord.clone());
        }
        clone.setLocationAddress(clonedAddresses);

        HashSet<BaseNameRecord> clonedNames = new HashSet<>();
        for (BaseNameRecord nameRecord : this.names) {
            clonedNames.add((BaseNameRecord) nameRecord.clone());
        }
        clone.setNames(clonedNames);

        return clone;
    }
}
