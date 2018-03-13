package dk.magenta.datafordeler.cvr.records;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.hibernate.Session;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

/**
 * Record for one participant on a Company or CompanyUnit
 */
@Entity
@Table(name = OfficeRelationUnitRecord.TABLE_NAME, indexes = {
        @Index(name = OfficeRelationUnitRecord.TABLE_NAME + "__unit", columnList = OfficeRelationUnitRecord.DB_FIELD_UNITNUMBER)
})
public class OfficeRelationUnitRecord extends CvrBitemporalRecord {

    public static final String TABLE_NAME = OfficeRelationRecord.TABLE_NAME + "_unit";

    public static final String DB_FIELD_UNITNUMBER = "unitNumber";
    public static final String IO_FIELD_UNITNUMBER = "enhedsNummer";

    @Column(name = DB_FIELD_UNITNUMBER)
    @JsonProperty(value = IO_FIELD_UNITNUMBER)
    private long unitNumber;

    public long getUnitNumber() {
        return this.unitNumber;
    }



    public static final String DB_FIELD_UNITTYPE = "unitType";
    public static final String IO_FIELD_UNITTYPE = "enhedsType";

    @Column(name = DB_FIELD_UNITTYPE)
    @JsonProperty(value = IO_FIELD_UNITTYPE)
    private String unitType;

    public String getUnitType() {
        return this.unitType;
    }



    public static final String DB_FIELD_BUSINESS_KEY = "businessKey";
    public static final String IO_FIELD_BUSINESS_KEY = "forretningsnoegle";

    @Column(name = DB_FIELD_BUSINESS_KEY)
    @JsonProperty(value = IO_FIELD_BUSINESS_KEY)
    private long businessKey;

    public long getBusinessKey() {
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



    public static final String IO_FIELD_NAME = "navne";

    @OneToMany(mappedBy = BaseNameRecord.DB_FIELD_OFFICE_UNIT, targetEntity = BaseNameRecord.class, cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JsonProperty(value = IO_FIELD_NAME)
    private Set<BaseNameRecord> names = new HashSet<>();

    public Set<BaseNameRecord> getNames() {
        return this.names;
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

    @OneToMany(mappedBy = AddressRecord.DB_FIELD_OFFICE_UNIT, targetEntity = AddressRecord.class, cascade = CascadeType.ALL, fetch = FetchType.EAGER)
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

    public Set<AddressRecord> getLocationAddress() {
        return this.locationAddress;
    }

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
}
