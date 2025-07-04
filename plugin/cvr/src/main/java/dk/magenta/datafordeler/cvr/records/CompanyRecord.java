package dk.magenta.datafordeler.cvr.records;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import dk.magenta.datafordeler.core.database.Bitemporal;
import dk.magenta.datafordeler.core.database.DatabaseEntry;
import dk.magenta.datafordeler.core.database.Monotemporal;
import dk.magenta.datafordeler.core.database.Nontemporal;
import dk.magenta.datafordeler.core.fapi.BaseQuery;
import dk.magenta.datafordeler.cvr.BitemporalSet;
import dk.magenta.datafordeler.cvr.BitemporalSetGroup;
import dk.magenta.datafordeler.cvr.CvrPlugin;
import dk.magenta.datafordeler.cvr.RecordSet;
import dk.magenta.datafordeler.cvr.service.CompanyRecordService;
import jakarta.persistence.*;
import org.hibernate.Session;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.Filters;
import org.hibernate.annotations.Where;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Base record for Company data, parsed from JSON into a tree of objects
 * with this class at the base.
 */
@Entity
@Table(name = CvrPlugin.DEBUG_TABLE_PREFIX + CompanyRecord.TABLE_NAME, indexes = {
        @Index(name = CvrPlugin.DEBUG_TABLE_PREFIX + CompanyRecord.TABLE_NAME + "__cvrnumber", columnList = CompanyRecord.DB_FIELD_CVR_NUMBER, unique = true),
        @Index(name = CvrPlugin.DEBUG_TABLE_PREFIX + CompanyRecord.TABLE_NAME + "__advertprotection", columnList = CompanyRecord.DB_FIELD_ADVERTPROTECTION),
        @Index(name = CvrPlugin.DEBUG_TABLE_PREFIX + CompanyRecord.TABLE_NAME + "__" + CompanyRecord.DB_FIELD_DAFO_UPDATED, columnList = CompanyRecord.DB_FIELD_DAFO_UPDATED)
})
@JsonIgnoreProperties(ignoreUnknown = true)
public class CompanyRecord extends CvrEntityRecord {

    public static final String TABLE_NAME = "cvr_record_company";

    public static final String schema = "virksomhed";

    @JsonIgnore
    public String getFieldName() {
        return TABLE_NAME;
    }

    @Override
    @JsonIgnore
    protected String getDomain() {
        return CompanyRecordService.getDomain();
    }


    public static final String DB_FIELD_CVR_NUMBER = "cvrNumber";
    public static final String IO_FIELD_CVR_NUMBER = "cvrNummer";

    @Column(name = DB_FIELD_CVR_NUMBER)
    @JsonProperty(value = IO_FIELD_CVR_NUMBER)
    private int cvrNumber;

    public int getCvrNumber() {
        return this.cvrNumber;
    }

    public String getCvrNumberString() {
        return String.format("%08d", this.cvrNumber);
    }

    @JsonIgnore
    public Map<String, Object> getIdentifyingFilter() {
        return Collections.singletonMap(DB_FIELD_CVR_NUMBER, this.cvrNumber);
    }


    public static final String DB_FIELD_REG_NUMBER = "regNumber";
    public static final String IO_FIELD_REG_NUMBER = "regNummer";

    @OneToMany(targetEntity = CompanyRegNumberRecord.class, mappedBy = CompanyRegNumberRecord.DB_FIELD_COMPANY, cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Filters({
            @Filter(name = Bitemporal.FILTER_EFFECTFROM_AFTER, condition = CvrBitemporalRecord.FILTERLOGIC_EFFECTFROM_AFTER),
            @Filter(name = Bitemporal.FILTER_EFFECTFROM_BEFORE, condition = CvrBitemporalRecord.FILTERLOGIC_EFFECTFROM_BEFORE),
            @Filter(name = Bitemporal.FILTER_EFFECTTO_AFTER, condition = CvrBitemporalRecord.FILTERLOGIC_EFFECTTO_AFTER),
            @Filter(name = Bitemporal.FILTER_EFFECTTO_BEFORE, condition = CvrBitemporalRecord.FILTERLOGIC_EFFECTTO_BEFORE),
            @Filter(name = Monotemporal.FILTER_REGISTRATIONFROM_AFTER, condition = CvrBitemporalRecord.FILTERLOGIC_REGISTRATIONFROM_AFTER),
            @Filter(name = Monotemporal.FILTER_REGISTRATIONFROM_BEFORE, condition = CvrBitemporalRecord.FILTERLOGIC_REGISTRATIONFROM_BEFORE),
            // @Filter(name = Monotemporal.FILTER_REGISTRATIONTO_AFTER, condition = Monotemporal.FILTERLOGIC_REGISTRATIONTO_AFTER),
            // @Filter(name = Monotemporal.FILTER_REGISTRATIONTO_BEFORE, condition = Monotemporal.FILTERLOGIC_REGISTRATIONTO_BEFORE),
            @Filter(name = Nontemporal.FILTER_LASTUPDATED_AFTER, condition = CvrNontemporalRecord.FILTERLOGIC_LASTUPDATED_AFTER),
            @Filter(name = Nontemporal.FILTER_LASTUPDATED_BEFORE, condition = CvrNontemporalRecord.FILTERLOGIC_LASTUPDATED_BEFORE)
    })
    @JsonProperty(value = IO_FIELD_REG_NUMBER)
    private Set<CompanyRegNumberRecord> regNumber = new HashSet<>();

    public void setRegNumber(Set<CompanyRegNumberRecord> regNumber) {
        this.regNumber = (regNumber == null) ? new HashSet<>() : new HashSet<>(regNumber);
        for (CompanyRegNumberRecord regNumberRecord : this.regNumber) {
            regNumberRecord.setCompanyRecord(this);
        }
    }

    public void addRegNumber(CompanyRegNumberRecord record) {
        if (record != null) {
            record.setCompanyRecord(this);
            if (!regNumber.isEmpty()) {
                this.addDataEventRecord(new CompanyDataEventRecord(record.getLastUpdated(), record.getFieldName(), this.regNumber.stream().reduce((first, second) -> second).get().getId()));
            }
            this.regNumber.add(record);
        }
    }

    public BitemporalSet<CompanyRegNumberRecord, CompanyRecord> getRegNumber() {
        return new BitemporalSet<>(this.regNumber, CompanyRegNumberRecord.class, this, CompanyRegNumberRecord.DB_FIELD_COMPANY);
    }


    public static final String DB_FIELD_ADVERTPROTECTION = "advertProtection";
    public static final String IO_FIELD_ADVERTPROTECTION = "reklamebeskyttet";

    @Column(name = DB_FIELD_ADVERTPROTECTION)
    @JsonProperty(value = IO_FIELD_ADVERTPROTECTION)
    private boolean advertProtection;

    public boolean getAdvertProtection() {
        return this.advertProtection;
    }


    public static final String DB_FIELD_UNITNUMBER = "unitNumber";
    public static final String IO_FIELD_UNITNUMBER = "enhedsNummer";

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
    private String unitType;

    public String getUnitType() {
        return this.unitType;
    }


    public static final String DB_FIELD_INDUSTRY_RESPONSIBILITY_CODE = "industryResponsibilityCode";
    public static final String IO_FIELD_INDUSTRY_RESPONSIBILITY_CODE = "brancheAnsvarskode";

    @Column(name = DB_FIELD_INDUSTRY_RESPONSIBILITY_CODE, nullable = true)
    @JsonProperty(value = IO_FIELD_INDUSTRY_RESPONSIBILITY_CODE)
    private Integer industryResponsibilityCode;

    public Integer getIndustryResponsibilityCode() {
        return this.industryResponsibilityCode;
    }


    public static final String DB_FIELD_NAMES = "names";
    public static final String IO_FIELD_NAMES = "navne";
    public static final String CLAUSE_NAME_PRIMARY = SecNameRecord.DB_FIELD_SECONDARY + "=false";

    @OneToMany(targetEntity = SecNameRecord.class, mappedBy = SecNameRecord.DB_FIELD_COMPANY, cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Where(clause = CLAUSE_NAME_PRIMARY)
    @Filters({
            @Filter(name = Bitemporal.FILTER_EFFECTFROM_AFTER, condition = CvrBitemporalRecord.FILTERLOGIC_EFFECTFROM_AFTER),
            @Filter(name = Bitemporal.FILTER_EFFECTFROM_BEFORE, condition = CvrBitemporalRecord.FILTERLOGIC_EFFECTFROM_BEFORE),
            @Filter(name = Bitemporal.FILTER_EFFECTTO_AFTER, condition = CvrBitemporalRecord.FILTERLOGIC_EFFECTTO_AFTER),
            @Filter(name = Bitemporal.FILTER_EFFECTTO_BEFORE, condition = CvrBitemporalRecord.FILTERLOGIC_EFFECTTO_BEFORE),
            @Filter(name = Monotemporal.FILTER_REGISTRATIONFROM_AFTER, condition = CvrBitemporalRecord.FILTERLOGIC_REGISTRATIONFROM_AFTER),
            @Filter(name = Monotemporal.FILTER_REGISTRATIONFROM_BEFORE, condition = CvrBitemporalRecord.FILTERLOGIC_REGISTRATIONFROM_BEFORE),
            // @Filter(name = Monotemporal.FILTER_REGISTRATIONTO_AFTER, condition = Monotemporal.FILTERLOGIC_REGISTRATIONTO_AFTER),
            // @Filter(name = Monotemporal.FILTER_REGISTRATIONTO_BEFORE, condition = Monotemporal.FILTERLOGIC_REGISTRATIONTO_BEFORE),
            @Filter(name = Nontemporal.FILTER_LASTUPDATED_AFTER, condition = CvrNontemporalRecord.FILTERLOGIC_LASTUPDATED_AFTER),
            @Filter(name = Nontemporal.FILTER_LASTUPDATED_BEFORE, condition = CvrNontemporalRecord.FILTERLOGIC_LASTUPDATED_BEFORE)
    })
    @JsonProperty(value = IO_FIELD_NAMES)
    private Set<SecNameRecord> names = new HashSet<>();

    public BitemporalSet<SecNameRecord, CompanyRecord> getNames() {
        return new BitemporalSet<>(this.names, SecNameRecord.class, this, SecNameRecord.DB_FIELD_COMPANY, CLAUSE_NAME_PRIMARY);
    }

    public void setNames(Set<SecNameRecord> names) {
        this.names = (names == null) ? new HashSet<>() : new HashSet<>(names);
        for (SecNameRecord record : this.names) {
            record.setSecondary(false);
            record.setCompanyRecord(this);
        }
    }

    public void addName(SecNameRecord record) {
        if (record != null) {
            record.setSecondary(false);
            record.setCompanyRecord(this);
            if (!names.isEmpty()) {
                this.addDataEventRecord(new CompanyDataEventRecord(record.getLastUpdated(), record.getFieldName(), this.names.stream().reduce((first, second) -> second).get().getId()));
            }
            this.names.add(record);
        }
    }


    public static final String DB_FIELD_SECONDARY_NAMES = "secondaryNames";
    public static final String IO_FIELD_SECONDARY_NAMES = "binavne";
    public static final String CLAUSE_NAME_SECONDARY = SecNameRecord.DB_FIELD_SECONDARY + "=true";

    @OneToMany(targetEntity = SecNameRecord.class, mappedBy = SecNameRecord.DB_FIELD_COMPANY, cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Where(clause = CLAUSE_NAME_SECONDARY)
    @Filters({
            @Filter(name = Bitemporal.FILTER_EFFECTFROM_AFTER, condition = CvrBitemporalRecord.FILTERLOGIC_EFFECTFROM_AFTER),
            @Filter(name = Bitemporal.FILTER_EFFECTFROM_BEFORE, condition = CvrBitemporalRecord.FILTERLOGIC_EFFECTFROM_BEFORE),
            @Filter(name = Bitemporal.FILTER_EFFECTTO_AFTER, condition = CvrBitemporalRecord.FILTERLOGIC_EFFECTTO_AFTER),
            @Filter(name = Bitemporal.FILTER_EFFECTTO_BEFORE, condition = CvrBitemporalRecord.FILTERLOGIC_EFFECTTO_BEFORE),
            @Filter(name = Monotemporal.FILTER_REGISTRATIONFROM_AFTER, condition = CvrBitemporalRecord.FILTERLOGIC_REGISTRATIONFROM_AFTER),
            @Filter(name = Monotemporal.FILTER_REGISTRATIONFROM_BEFORE, condition = CvrBitemporalRecord.FILTERLOGIC_REGISTRATIONFROM_BEFORE),
            // @Filter(name = Monotemporal.FILTER_REGISTRATIONTO_AFTER, condition = Monotemporal.FILTERLOGIC_REGISTRATIONTO_AFTER),
            // @Filter(name = Monotemporal.FILTER_REGISTRATIONTO_BEFORE, condition = Monotemporal.FILTERLOGIC_REGISTRATIONTO_BEFORE),
            @Filter(name = Nontemporal.FILTER_LASTUPDATED_AFTER, condition = CvrNontemporalRecord.FILTERLOGIC_LASTUPDATED_AFTER),
            @Filter(name = Nontemporal.FILTER_LASTUPDATED_BEFORE, condition = CvrNontemporalRecord.FILTERLOGIC_LASTUPDATED_BEFORE)
    })
    @JsonProperty(value = IO_FIELD_SECONDARY_NAMES)
    private Set<SecNameRecord> secondaryNames = new HashSet<>();

    public BitemporalSet<SecNameRecord, CompanyRecord> getSecondaryNames() {
        return new BitemporalSet<>(this.secondaryNames, SecNameRecord.class, this, SecNameRecord.DB_FIELD_COMPANY, CLAUSE_NAME_SECONDARY);
    }

    public void setSecondaryNames(Set<SecNameRecord> secondaryNames) {
        this.secondaryNames = (secondaryNames == null) ? new HashSet<>() : new HashSet<>(secondaryNames);
        for (SecNameRecord record : this.secondaryNames) {
            record.setSecondary(true);
            record.setCompanyRecord(this);
        }
    }

    public void addSecondaryName(SecNameRecord record) {
        if (record != null) {
            record.setSecondary(true);
            record.setCompanyRecord(this);
            if (!secondaryNames.isEmpty()) {
                this.addDataEventRecord(new CompanyDataEventRecord(record.getLastUpdated(), record.getFieldName(), this.secondaryNames.stream().reduce((first, second) -> second).get().getId()));
            }
            this.secondaryNames.add(record);
        }
    }


    public static final String DB_FIELD_LOCATION_ADDRESS = "locationAddress";
    public static final String IO_FIELD_LOCATION_ADDRESS = "beliggenhedsadresse";
    public static final String CLAUSE_ADDRESS_LOCATION = AddressRecord.DB_FIELD_TYPE + "=" + AddressRecord.TYPE_LOCATION;

    @OneToMany(targetEntity = AddressRecord.class, mappedBy = AddressRecord.DB_FIELD_COMPANY, cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Where(clause = CLAUSE_ADDRESS_LOCATION)
    @Filters({
            @Filter(name = Bitemporal.FILTER_EFFECTFROM_AFTER, condition = CvrBitemporalRecord.FILTERLOGIC_EFFECTFROM_AFTER),
            @Filter(name = Bitemporal.FILTER_EFFECTFROM_BEFORE, condition = CvrBitemporalRecord.FILTERLOGIC_EFFECTFROM_BEFORE),
            @Filter(name = Bitemporal.FILTER_EFFECTTO_AFTER, condition = CvrBitemporalRecord.FILTERLOGIC_EFFECTTO_AFTER),
            @Filter(name = Bitemporal.FILTER_EFFECTTO_BEFORE, condition = CvrBitemporalRecord.FILTERLOGIC_EFFECTTO_BEFORE),
            @Filter(name = Monotemporal.FILTER_REGISTRATIONFROM_AFTER, condition = CvrBitemporalRecord.FILTERLOGIC_REGISTRATIONFROM_AFTER),
            @Filter(name = Monotemporal.FILTER_REGISTRATIONFROM_BEFORE, condition = CvrBitemporalRecord.FILTERLOGIC_REGISTRATIONFROM_BEFORE),
            // @Filter(name = Monotemporal.FILTER_REGISTRATIONTO_AFTER, condition = Monotemporal.FILTERLOGIC_REGISTRATIONTO_AFTER),
            // @Filter(name = Monotemporal.FILTER_REGISTRATIONTO_BEFORE, condition = Monotemporal.FILTERLOGIC_REGISTRATIONTO_BEFORE),
            @Filter(name = Nontemporal.FILTER_LASTUPDATED_AFTER, condition = CvrNontemporalRecord.FILTERLOGIC_LASTUPDATED_AFTER),
            @Filter(name = Nontemporal.FILTER_LASTUPDATED_BEFORE, condition = CvrNontemporalRecord.FILTERLOGIC_LASTUPDATED_BEFORE)
    })
    @JsonProperty(value = IO_FIELD_LOCATION_ADDRESS)
    private Set<AddressRecord> locationAddress = new HashSet<>();

    public void setLocationAddress(Set<AddressRecord> locationAddress) {
        this.locationAddress = (locationAddress == null) ? new HashSet<>() : new HashSet<>(locationAddress);
        for (AddressRecord record : this.locationAddress) {
            record.setType(AddressRecord.TYPE_LOCATION);
            record.setCompanyRecord(this);
        }
    }

    public void addLocationAddress(AddressRecord record) {
        if (record != null) {
            record.setType(AddressRecord.TYPE_LOCATION);
            record.setCompanyRecord(this);
            if (!locationAddress.isEmpty()) {
                this.addDataEventRecord(new CompanyDataEventRecord(record.getLastUpdated(), record.getFieldName(), this.locationAddress.stream().reduce((first, second) -> second).get().getId()));
            }
            this.locationAddress.add(record);
        }
    }

    public BitemporalSet<AddressRecord, CompanyRecord> getLocationAddress() {
        return new BitemporalSet<>(this.locationAddress, AddressRecord.class, this, AddressRecord.DB_FIELD_COMPANY, CLAUSE_ADDRESS_LOCATION);
    }


    public static final String DB_FIELD_POSTAL_ADDRESS = "postalAddress";
    public static final String IO_FIELD_POSTAL_ADDRESS = "postadresse";
    public static final String CLAUSE_ADDRESS_POSTAL = AddressRecord.DB_FIELD_TYPE + "=" + AddressRecord.TYPE_POSTAL;


    @OneToMany(targetEntity = AddressRecord.class, mappedBy = AddressRecord.DB_FIELD_COMPANY, cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Where(clause = CLAUSE_ADDRESS_POSTAL)
    @Filters({
            @Filter(name = Bitemporal.FILTER_EFFECTFROM_AFTER, condition = CvrBitemporalRecord.FILTERLOGIC_EFFECTFROM_AFTER),
            @Filter(name = Bitemporal.FILTER_EFFECTFROM_BEFORE, condition = CvrBitemporalRecord.FILTERLOGIC_EFFECTFROM_BEFORE),
            @Filter(name = Bitemporal.FILTER_EFFECTTO_AFTER, condition = CvrBitemporalRecord.FILTERLOGIC_EFFECTTO_AFTER),
            @Filter(name = Bitemporal.FILTER_EFFECTTO_BEFORE, condition = CvrBitemporalRecord.FILTERLOGIC_EFFECTTO_BEFORE),
            @Filter(name = Monotemporal.FILTER_REGISTRATIONFROM_AFTER, condition = CvrBitemporalRecord.FILTERLOGIC_REGISTRATIONFROM_AFTER),
            @Filter(name = Monotemporal.FILTER_REGISTRATIONFROM_BEFORE, condition = CvrBitemporalRecord.FILTERLOGIC_REGISTRATIONFROM_BEFORE),
            // @Filter(name = Monotemporal.FILTER_REGISTRATIONTO_AFTER, condition = Monotemporal.FILTERLOGIC_REGISTRATIONTO_AFTER),
            // @Filter(name = Monotemporal.FILTER_REGISTRATIONTO_BEFORE, condition = Monotemporal.FILTERLOGIC_REGISTRATIONTO_BEFORE),
            @Filter(name = Nontemporal.FILTER_LASTUPDATED_AFTER, condition = CvrNontemporalRecord.FILTERLOGIC_LASTUPDATED_AFTER),
            @Filter(name = Nontemporal.FILTER_LASTUPDATED_BEFORE, condition = CvrNontemporalRecord.FILTERLOGIC_LASTUPDATED_BEFORE)
    })
    @JsonProperty(value = IO_FIELD_POSTAL_ADDRESS)
    private Set<AddressRecord> postalAddress = new HashSet<>();

    public void setPostalAddress(Set<AddressRecord> postalAddress) {
        this.postalAddress = (postalAddress == null) ? new HashSet<>() : new HashSet<>(postalAddress);
        for (AddressRecord record : this.postalAddress) {
            record.setType(AddressRecord.TYPE_POSTAL);
            record.setCompanyRecord(this);
        }
    }

    public void addPostalAddress(AddressRecord record) {
        if (record != null) {
            record.setType(AddressRecord.TYPE_POSTAL);
            record.setCompanyRecord(this);
            if (!postalAddress.isEmpty()) {
                this.addDataEventRecord(new CompanyDataEventRecord(record.getLastUpdated(), record.getFieldName(), this.postalAddress.stream().reduce((first, second) -> second).get().getId()));
            }
            this.postalAddress.add(record);
        }
    }

    public BitemporalSet<AddressRecord, CompanyRecord> getPostalAddress() {
        return new BitemporalSet<>(this.postalAddress, AddressRecord.class, this, AddressRecord.DB_FIELD_COMPANY, CLAUSE_ADDRESS_POSTAL);
    }


    public static final String DB_FIELD_PHONE = "phoneNumber";
    public static final String IO_FIELD_PHONE = "telefonNummer";
    public static final String CLAUSE_CONTACT_PHONE_PRIMARY = ContactRecord.DB_FIELD_TYPE + "=" + ContactRecord.TYPE_TELEFONNUMMER + " AND " + ContactRecord.DB_FIELD_SECONDARY + "=false";

    @OneToMany(targetEntity = ContactRecord.class, mappedBy = ContactRecord.DB_FIELD_COMPANY, cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Where(clause = CLAUSE_CONTACT_PHONE_PRIMARY)
    @Filters({
            @Filter(name = Bitemporal.FILTER_EFFECTFROM_AFTER, condition = CvrBitemporalRecord.FILTERLOGIC_EFFECTFROM_AFTER),
            @Filter(name = Bitemporal.FILTER_EFFECTFROM_BEFORE, condition = CvrBitemporalRecord.FILTERLOGIC_EFFECTFROM_BEFORE),
            @Filter(name = Bitemporal.FILTER_EFFECTTO_AFTER, condition = CvrBitemporalRecord.FILTERLOGIC_EFFECTTO_AFTER),
            @Filter(name = Bitemporal.FILTER_EFFECTTO_BEFORE, condition = CvrBitemporalRecord.FILTERLOGIC_EFFECTTO_BEFORE),
            @Filter(name = Monotemporal.FILTER_REGISTRATIONFROM_AFTER, condition = CvrBitemporalRecord.FILTERLOGIC_REGISTRATIONFROM_AFTER),
            @Filter(name = Monotemporal.FILTER_REGISTRATIONFROM_BEFORE, condition = CvrBitemporalRecord.FILTERLOGIC_REGISTRATIONFROM_BEFORE),
            // @Filter(name = Monotemporal.FILTER_REGISTRATIONTO_AFTER, condition = Monotemporal.FILTERLOGIC_REGISTRATIONTO_AFTER),
            // @Filter(name = Monotemporal.FILTER_REGISTRATIONTO_BEFORE, condition = Monotemporal.FILTERLOGIC_REGISTRATIONTO_BEFORE),
            @Filter(name = Nontemporal.FILTER_LASTUPDATED_AFTER, condition = CvrNontemporalRecord.FILTERLOGIC_LASTUPDATED_AFTER),
            @Filter(name = Nontemporal.FILTER_LASTUPDATED_BEFORE, condition = CvrNontemporalRecord.FILTERLOGIC_LASTUPDATED_BEFORE)
    })
    @JsonProperty(value = IO_FIELD_PHONE)
    private Set<ContactRecord> phoneNumber = new HashSet<>();

    public void setPhoneNumber(Set<ContactRecord> phoneNumber) {
        this.phoneNumber = (phoneNumber == null) ? new HashSet<>() : new HashSet<>(phoneNumber);
        for (ContactRecord record : this.phoneNumber) {
            record.setType(ContactRecord.TYPE_TELEFONNUMMER);
            record.setSecondary(false);
            record.setCompanyRecord(this);
        }
    }

    public void addPhoneNumber(ContactRecord record) {
        this.addContact(record, this.getPhoneNumber(), ContactRecord.TYPE_TELEFONNUMMER, false);
    }

    public BitemporalSet<ContactRecord, CompanyRecord> getPhoneNumber() {
        return new BitemporalSet<>(this.phoneNumber, ContactRecord.class, this, ContactRecord.DB_FIELD_COMPANY, CLAUSE_CONTACT_PHONE_PRIMARY);
    }


    public static final String DB_FIELD_PHONE_SECONDARY = "secondaryPhoneNumber";
    public static final String IO_FIELD_PHONE_SECONDARY = "sekundaertTelefonNummer";
    public static final String CLAUSE_CONTACT_PHONE_SECONDARY = ContactRecord.DB_FIELD_TYPE + "=" + ContactRecord.TYPE_TELEFONNUMMER + " AND " + ContactRecord.DB_FIELD_SECONDARY + "=true";

    @OneToMany(targetEntity = ContactRecord.class, mappedBy = ContactRecord.DB_FIELD_COMPANY, cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Where(clause = CLAUSE_CONTACT_PHONE_SECONDARY)
    @Filters({
            @Filter(name = Bitemporal.FILTER_EFFECTFROM_AFTER, condition = CvrBitemporalRecord.FILTERLOGIC_EFFECTFROM_AFTER),
            @Filter(name = Bitemporal.FILTER_EFFECTFROM_BEFORE, condition = CvrBitemporalRecord.FILTERLOGIC_EFFECTFROM_BEFORE),
            @Filter(name = Bitemporal.FILTER_EFFECTTO_AFTER, condition = CvrBitemporalRecord.FILTERLOGIC_EFFECTTO_AFTER),
            @Filter(name = Bitemporal.FILTER_EFFECTTO_BEFORE, condition = CvrBitemporalRecord.FILTERLOGIC_EFFECTTO_BEFORE),
            @Filter(name = Monotemporal.FILTER_REGISTRATIONFROM_AFTER, condition = CvrBitemporalRecord.FILTERLOGIC_REGISTRATIONFROM_AFTER),
            @Filter(name = Monotemporal.FILTER_REGISTRATIONFROM_BEFORE, condition = CvrBitemporalRecord.FILTERLOGIC_REGISTRATIONFROM_BEFORE),
            // @Filter(name = Monotemporal.FILTER_REGISTRATIONTO_AFTER, condition = Monotemporal.FILTERLOGIC_REGISTRATIONTO_AFTER),
            // @Filter(name = Monotemporal.FILTER_REGISTRATIONTO_BEFORE, condition = Monotemporal.FILTERLOGIC_REGISTRATIONTO_BEFORE),
            @Filter(name = Nontemporal.FILTER_LASTUPDATED_AFTER, condition = CvrNontemporalRecord.FILTERLOGIC_LASTUPDATED_AFTER),
            @Filter(name = Nontemporal.FILTER_LASTUPDATED_BEFORE, condition = CvrNontemporalRecord.FILTERLOGIC_LASTUPDATED_BEFORE)
    })
    @JsonProperty(value = IO_FIELD_PHONE_SECONDARY)
    private Set<ContactRecord> secondaryPhoneNumber = new HashSet<>();

    public void setSecondaryPhoneNumber(Set<ContactRecord> secondaryPhoneNumber) {
        this.secondaryPhoneNumber = (secondaryPhoneNumber == null) ? new HashSet<>() : new HashSet<>(secondaryPhoneNumber);
        for (ContactRecord record : this.secondaryPhoneNumber) {
            record.setType(ContactRecord.TYPE_TELEFONNUMMER);
            record.setSecondary(true);
            record.setCompanyRecord(this);
        }
    }

    public void addSecondaryPhoneNumber(ContactRecord record) {
        this.addContact(record, this.getSecondaryPhoneNumber(), ContactRecord.TYPE_TELEFONNUMMER, true);
    }

    public BitemporalSet<ContactRecord, CompanyRecord> getSecondaryPhoneNumber() {
        return new BitemporalSet<>(this.secondaryPhoneNumber, ContactRecord.class, this, ContactRecord.DB_FIELD_COMPANY, CLAUSE_CONTACT_PHONE_SECONDARY);
    }


    public static final String DB_FIELD_FAX = "faxNumber";
    public static final String IO_FIELD_FAX = "telefaxNummer";
    public static final String CLAUSE_CONTACT_FAX_PRIMARY = ContactRecord.DB_FIELD_TYPE + "=" + ContactRecord.TYPE_TELEFAXNUMMER + " AND " + ContactRecord.DB_FIELD_SECONDARY + "=false";

    @OneToMany(targetEntity = ContactRecord.class, mappedBy = ContactRecord.DB_FIELD_COMPANY, cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Where(clause = CLAUSE_CONTACT_FAX_PRIMARY)
    @Filters({
            @Filter(name = Bitemporal.FILTER_EFFECTFROM_AFTER, condition = CvrBitemporalRecord.FILTERLOGIC_EFFECTFROM_AFTER),
            @Filter(name = Bitemporal.FILTER_EFFECTFROM_BEFORE, condition = CvrBitemporalRecord.FILTERLOGIC_EFFECTFROM_BEFORE),
            @Filter(name = Bitemporal.FILTER_EFFECTTO_AFTER, condition = CvrBitemporalRecord.FILTERLOGIC_EFFECTTO_AFTER),
            @Filter(name = Bitemporal.FILTER_EFFECTTO_BEFORE, condition = CvrBitemporalRecord.FILTERLOGIC_EFFECTTO_BEFORE),
            @Filter(name = Monotemporal.FILTER_REGISTRATIONFROM_AFTER, condition = CvrBitemporalRecord.FILTERLOGIC_REGISTRATIONFROM_AFTER),
            @Filter(name = Monotemporal.FILTER_REGISTRATIONFROM_BEFORE, condition = CvrBitemporalRecord.FILTERLOGIC_REGISTRATIONFROM_BEFORE),
            // @Filter(name = Monotemporal.FILTER_REGISTRATIONTO_AFTER, condition = Monotemporal.FILTERLOGIC_REGISTRATIONTO_AFTER),
            // @Filter(name = Monotemporal.FILTER_REGISTRATIONTO_BEFORE, condition = Monotemporal.FILTERLOGIC_REGISTRATIONTO_BEFORE),
            @Filter(name = Nontemporal.FILTER_LASTUPDATED_AFTER, condition = CvrNontemporalRecord.FILTERLOGIC_LASTUPDATED_AFTER),
            @Filter(name = Nontemporal.FILTER_LASTUPDATED_BEFORE, condition = CvrNontemporalRecord.FILTERLOGIC_LASTUPDATED_BEFORE)
    })
    @JsonProperty(value = IO_FIELD_FAX)
    private Set<ContactRecord> faxNumber = new HashSet<>();

    public void setFaxNumber(Set<ContactRecord> faxNumber) {
        this.faxNumber = (faxNumber == null) ? new HashSet<>() : new HashSet<>(faxNumber);
        for (ContactRecord record : this.faxNumber) {
            record.setType(ContactRecord.TYPE_TELEFAXNUMMER);
            record.setSecondary(false);
            record.setCompanyRecord(this);
        }
    }

    public void addFaxNumber(ContactRecord record) {
        this.addContact(record, this.getFaxNumber(), ContactRecord.TYPE_TELEFAXNUMMER, false);
    }

    public BitemporalSet<ContactRecord, CompanyRecord> getFaxNumber() {
        return new BitemporalSet<>(this.faxNumber, ContactRecord.class, this, ContactRecord.DB_FIELD_COMPANY, CLAUSE_CONTACT_FAX_PRIMARY);
    }


    public static final String DB_FIELD_FAX_SECONDARY = "secondaryFaxNumber";
    public static final String IO_FIELD_FAX_SECONDARY = "sekundaertTelefaxNummer";
    public static final String CLAUSE_CONTACT_FAX_SECONDARY = ContactRecord.DB_FIELD_TYPE + "=" + ContactRecord.TYPE_TELEFAXNUMMER + " AND " + ContactRecord.DB_FIELD_SECONDARY + "=true";

    @OneToMany(targetEntity = ContactRecord.class, mappedBy = ContactRecord.DB_FIELD_COMPANY, cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Where(clause = CLAUSE_CONTACT_FAX_SECONDARY)
    @Filters({
            @Filter(name = Bitemporal.FILTER_EFFECTFROM_AFTER, condition = CvrBitemporalRecord.FILTERLOGIC_EFFECTFROM_AFTER),
            @Filter(name = Bitemporal.FILTER_EFFECTFROM_BEFORE, condition = CvrBitemporalRecord.FILTERLOGIC_EFFECTFROM_BEFORE),
            @Filter(name = Bitemporal.FILTER_EFFECTTO_AFTER, condition = CvrBitemporalRecord.FILTERLOGIC_EFFECTTO_AFTER),
            @Filter(name = Bitemporal.FILTER_EFFECTTO_BEFORE, condition = CvrBitemporalRecord.FILTERLOGIC_EFFECTTO_BEFORE),
            @Filter(name = Monotemporal.FILTER_REGISTRATIONFROM_AFTER, condition = CvrBitemporalRecord.FILTERLOGIC_REGISTRATIONFROM_AFTER),
            @Filter(name = Monotemporal.FILTER_REGISTRATIONFROM_BEFORE, condition = CvrBitemporalRecord.FILTERLOGIC_REGISTRATIONFROM_BEFORE),
            // @Filter(name = Monotemporal.FILTER_REGISTRATIONTO_AFTER, condition = Monotemporal.FILTERLOGIC_REGISTRATIONTO_AFTER),
            // @Filter(name = Monotemporal.FILTER_REGISTRATIONTO_BEFORE, condition = Monotemporal.FILTERLOGIC_REGISTRATIONTO_BEFORE),
            @Filter(name = Nontemporal.FILTER_LASTUPDATED_AFTER, condition = CvrNontemporalRecord.FILTERLOGIC_LASTUPDATED_AFTER),
            @Filter(name = Nontemporal.FILTER_LASTUPDATED_BEFORE, condition = CvrNontemporalRecord.FILTERLOGIC_LASTUPDATED_BEFORE)
    })
    @JsonProperty(value = IO_FIELD_FAX_SECONDARY)
    private Set<ContactRecord> secondaryFaxNumber = new HashSet<>();

    public void setSecondaryFaxNumber(Set<ContactRecord> secondaryFaxNumber) {
        this.secondaryFaxNumber = (secondaryFaxNumber == null) ? new HashSet<>() : new HashSet<>(secondaryFaxNumber);
        for (ContactRecord record : this.secondaryFaxNumber) {
            record.setType(ContactRecord.TYPE_TELEFAXNUMMER);
            record.setSecondary(true);
            record.setCompanyRecord(this);
        }
    }

    public void addSecondaryFaxNumber(ContactRecord record) {
        this.addContact(record, this.getSecondaryFaxNumber(), ContactRecord.TYPE_TELEFAXNUMMER, true);
    }

    public BitemporalSet<ContactRecord, CompanyRecord> getSecondaryFaxNumber() {
        return new BitemporalSet<>(this.secondaryFaxNumber, ContactRecord.class, this, ContactRecord.DB_FIELD_COMPANY, CLAUSE_CONTACT_FAX_SECONDARY);
    }


    public static final String DB_FIELD_EMAIL = "emailAddress";
    public static final String IO_FIELD_EMAIL = "elektroniskPost";
    public static final String CLAUSE_CONTACT_EMAIL = ContactRecord.DB_FIELD_TYPE + "=" + ContactRecord.TYPE_EMAILADRESSE;

    @OneToMany(targetEntity = ContactRecord.class, mappedBy = ContactRecord.DB_FIELD_COMPANY, cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Where(clause = CLAUSE_CONTACT_EMAIL)
    @Filters({
            @Filter(name = Bitemporal.FILTER_EFFECTFROM_AFTER, condition = CvrBitemporalRecord.FILTERLOGIC_EFFECTFROM_AFTER),
            @Filter(name = Bitemporal.FILTER_EFFECTFROM_BEFORE, condition = CvrBitemporalRecord.FILTERLOGIC_EFFECTFROM_BEFORE),
            @Filter(name = Bitemporal.FILTER_EFFECTTO_AFTER, condition = CvrBitemporalRecord.FILTERLOGIC_EFFECTTO_AFTER),
            @Filter(name = Bitemporal.FILTER_EFFECTTO_BEFORE, condition = CvrBitemporalRecord.FILTERLOGIC_EFFECTTO_BEFORE),
            @Filter(name = Monotemporal.FILTER_REGISTRATIONFROM_AFTER, condition = CvrBitemporalRecord.FILTERLOGIC_REGISTRATIONFROM_AFTER),
            @Filter(name = Monotemporal.FILTER_REGISTRATIONFROM_BEFORE, condition = CvrBitemporalRecord.FILTERLOGIC_REGISTRATIONFROM_BEFORE),
            // @Filter(name = Monotemporal.FILTER_REGISTRATIONTO_AFTER, condition = Monotemporal.FILTERLOGIC_REGISTRATIONTO_AFTER),
            // @Filter(name = Monotemporal.FILTER_REGISTRATIONTO_BEFORE, condition = Monotemporal.FILTERLOGIC_REGISTRATIONTO_BEFORE),
            @Filter(name = Nontemporal.FILTER_LASTUPDATED_AFTER, condition = CvrNontemporalRecord.FILTERLOGIC_LASTUPDATED_AFTER),
            @Filter(name = Nontemporal.FILTER_LASTUPDATED_BEFORE, condition = CvrNontemporalRecord.FILTERLOGIC_LASTUPDATED_BEFORE)
    })
    @JsonProperty(value = IO_FIELD_EMAIL)
    private Set<ContactRecord> emailAddress = new HashSet<>();

    public void setEmailAddress(Set<ContactRecord> emailAddress) {
        this.emailAddress = (emailAddress == null) ? new HashSet<>() : new HashSet<>(emailAddress);
        for (ContactRecord record : this.emailAddress) {
            record.setType(ContactRecord.TYPE_EMAILADRESSE);
            record.setCompanyRecord(this);
        }
    }

    public void addEmailAddress(ContactRecord record) {
        this.addContact(record, this.getEmailAddress(), ContactRecord.TYPE_EMAILADRESSE, false);
    }

    public BitemporalSet<ContactRecord, CompanyRecord> getEmailAddress() {
        return new BitemporalSet<>(this.emailAddress, ContactRecord.class, this, ContactRecord.DB_FIELD_COMPANY, CLAUSE_CONTACT_EMAIL);
    }


    public static final String DB_FIELD_HOMEPAGE = "homepage";
    public static final String IO_FIELD_HOMEPAGE = "hjemmeside";
    public static final String CLAUSE_CONTACT_HOMEPAGE = ContactRecord.DB_FIELD_TYPE + "=" + ContactRecord.TYPE_HJEMMESIDE;

    @OneToMany(targetEntity = ContactRecord.class, mappedBy = ContactRecord.DB_FIELD_COMPANY, cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Where(clause = CLAUSE_CONTACT_HOMEPAGE)
    @Filters({
            @Filter(name = Bitemporal.FILTER_EFFECTFROM_AFTER, condition = CvrBitemporalRecord.FILTERLOGIC_EFFECTFROM_AFTER),
            @Filter(name = Bitemporal.FILTER_EFFECTFROM_BEFORE, condition = CvrBitemporalRecord.FILTERLOGIC_EFFECTFROM_BEFORE),
            @Filter(name = Bitemporal.FILTER_EFFECTTO_AFTER, condition = CvrBitemporalRecord.FILTERLOGIC_EFFECTTO_AFTER),
            @Filter(name = Bitemporal.FILTER_EFFECTTO_BEFORE, condition = CvrBitemporalRecord.FILTERLOGIC_EFFECTTO_BEFORE),
            @Filter(name = Monotemporal.FILTER_REGISTRATIONFROM_AFTER, condition = CvrBitemporalRecord.FILTERLOGIC_REGISTRATIONFROM_AFTER),
            @Filter(name = Monotemporal.FILTER_REGISTRATIONFROM_BEFORE, condition = CvrBitemporalRecord.FILTERLOGIC_REGISTRATIONFROM_BEFORE),
            // @Filter(name = Monotemporal.FILTER_REGISTRATIONTO_AFTER, condition = Monotemporal.FILTERLOGIC_REGISTRATIONTO_AFTER),
            // @Filter(name = Monotemporal.FILTER_REGISTRATIONTO_BEFORE, condition = Monotemporal.FILTERLOGIC_REGISTRATIONTO_BEFORE),
            @Filter(name = Nontemporal.FILTER_LASTUPDATED_AFTER, condition = CvrNontemporalRecord.FILTERLOGIC_LASTUPDATED_AFTER),
            @Filter(name = Nontemporal.FILTER_LASTUPDATED_BEFORE, condition = CvrNontemporalRecord.FILTERLOGIC_LASTUPDATED_BEFORE)
    })
    @JsonProperty(value = IO_FIELD_HOMEPAGE)
    private Set<ContactRecord> homepage = new HashSet<>();

    public void setHomepage(Set<ContactRecord> homepage) {
        this.homepage = (homepage == null) ? new HashSet<>() : new HashSet<>(homepage);
        for (ContactRecord record : this.homepage) {
            record.setType(ContactRecord.TYPE_HJEMMESIDE);
            record.setCompanyRecord(this);
        }
    }

    public void addHomepage(ContactRecord record) {
        this.addContact(record, this.getHomepage(), ContactRecord.TYPE_HJEMMESIDE, false);
    }

    public BitemporalSet<ContactRecord, CompanyRecord> getHomepage() {
        return new BitemporalSet<>(this.homepage, ContactRecord.class, this, ContactRecord.DB_FIELD_COMPANY, CLAUSE_CONTACT_HOMEPAGE);
    }


    public static final String DB_FIELD_MANDATORY_EMAIL = "mandatoryEmailAddress";
    public static final String IO_FIELD_MANDATORY_EMAIL = "obligatoriskEmail";
    public static final String CLAUSE_CONTACT_MANDATORY_EMAIL = ContactRecord.DB_FIELD_TYPE + "=" + ContactRecord.TYPE_OBLIGATORISK_EMAILADRESSE;

    @OneToMany(targetEntity = ContactRecord.class, mappedBy = ContactRecord.DB_FIELD_COMPANY, cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Where(clause = CLAUSE_CONTACT_MANDATORY_EMAIL)
    @Filters({
            @Filter(name = Bitemporal.FILTER_EFFECTFROM_AFTER, condition = CvrBitemporalRecord.FILTERLOGIC_EFFECTFROM_AFTER),
            @Filter(name = Bitemporal.FILTER_EFFECTFROM_BEFORE, condition = CvrBitemporalRecord.FILTERLOGIC_EFFECTFROM_BEFORE),
            @Filter(name = Bitemporal.FILTER_EFFECTTO_AFTER, condition = CvrBitemporalRecord.FILTERLOGIC_EFFECTTO_AFTER),
            @Filter(name = Bitemporal.FILTER_EFFECTTO_BEFORE, condition = CvrBitemporalRecord.FILTERLOGIC_EFFECTTO_BEFORE),
            @Filter(name = Monotemporal.FILTER_REGISTRATIONFROM_AFTER, condition = CvrBitemporalRecord.FILTERLOGIC_REGISTRATIONFROM_AFTER),
            @Filter(name = Monotemporal.FILTER_REGISTRATIONFROM_BEFORE, condition = CvrBitemporalRecord.FILTERLOGIC_REGISTRATIONFROM_BEFORE),
            // @Filter(name = Monotemporal.FILTER_REGISTRATIONTO_AFTER, condition = Monotemporal.FILTERLOGIC_REGISTRATIONTO_AFTER),
            // @Filter(name = Monotemporal.FILTER_REGISTRATIONTO_BEFORE, condition = Monotemporal.FILTERLOGIC_REGISTRATIONTO_BEFORE),
            @Filter(name = Nontemporal.FILTER_LASTUPDATED_AFTER, condition = CvrNontemporalRecord.FILTERLOGIC_LASTUPDATED_AFTER),
            @Filter(name = Nontemporal.FILTER_LASTUPDATED_BEFORE, condition = CvrNontemporalRecord.FILTERLOGIC_LASTUPDATED_BEFORE)
    })
    @JsonProperty(value = IO_FIELD_MANDATORY_EMAIL)
    private Set<ContactRecord> mandatoryEmailAddress = new HashSet<>();

    public void setMandatoryEmailAddress(Set<ContactRecord> mandatoryEmailAddress) {
        this.mandatoryEmailAddress = (mandatoryEmailAddress == null) ? new HashSet<>() : new HashSet<>(mandatoryEmailAddress);
        for (ContactRecord record : this.mandatoryEmailAddress) {
            record.setType(ContactRecord.TYPE_OBLIGATORISK_EMAILADRESSE);
            record.setCompanyRecord(this);
        }
    }

    public void addMandatoryEmailAddress(ContactRecord record) {
        this.addContact(record, this.getMandatoryEmailAddress(), ContactRecord.TYPE_OBLIGATORISK_EMAILADRESSE, false);
    }

    public BitemporalSet<ContactRecord, CompanyRecord> getMandatoryEmailAddress() {
        return new BitemporalSet<>(this.mandatoryEmailAddress, ContactRecord.class, this, ContactRecord.DB_FIELD_COMPANY, CLAUSE_CONTACT_MANDATORY_EMAIL);
    }

    private void addContact(ContactRecord record, RecordSet<ContactRecord, CompanyRecord> set, int type, boolean secondary) {
        if (record != null) {
            record.setType(type);
            record.setSecondary(secondary);
            if (!set.contains(record)) {
                record.setCompanyRecord(this);
                if (!set.isEmpty()) {
                    this.addDataEventRecord(
                            new CompanyDataEventRecord(
                                    record.getLastUpdated(),
                                    record.getFieldName(),
                                    set.stream().reduce((first, second) -> second).get().getId()
                            )
                    );
                }
                set.add(record);
            }
        }
    }


    public static final String DB_FIELD_LIFECYCLE = "lifecycle";
    public static final String IO_FIELD_LIFECYCLE = "livsforloeb";

    @OneToMany(targetEntity = LifecycleRecord.class, mappedBy = LifecycleRecord.DB_FIELD_COMPANY, cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Filters({
            @Filter(name = Bitemporal.FILTER_EFFECTFROM_AFTER, condition = CvrBitemporalRecord.FILTERLOGIC_EFFECTFROM_AFTER),
            @Filter(name = Bitemporal.FILTER_EFFECTFROM_BEFORE, condition = CvrBitemporalRecord.FILTERLOGIC_EFFECTFROM_BEFORE),
            @Filter(name = Bitemporal.FILTER_EFFECTTO_AFTER, condition = CvrBitemporalRecord.FILTERLOGIC_EFFECTTO_AFTER),
            @Filter(name = Bitemporal.FILTER_EFFECTTO_BEFORE, condition = CvrBitemporalRecord.FILTERLOGIC_EFFECTTO_BEFORE),
            @Filter(name = Monotemporal.FILTER_REGISTRATIONFROM_AFTER, condition = CvrBitemporalRecord.FILTERLOGIC_REGISTRATIONFROM_AFTER),
            @Filter(name = Monotemporal.FILTER_REGISTRATIONFROM_BEFORE, condition = CvrBitemporalRecord.FILTERLOGIC_REGISTRATIONFROM_BEFORE),
            // @Filter(name = Monotemporal.FILTER_REGISTRATIONTO_AFTER, condition = Monotemporal.FILTERLOGIC_REGISTRATIONTO_AFTER),
            // @Filter(name = Monotemporal.FILTER_REGISTRATIONTO_BEFORE, condition = Monotemporal.FILTERLOGIC_REGISTRATIONTO_BEFORE),
            @Filter(name = Nontemporal.FILTER_LASTUPDATED_AFTER, condition = CvrNontemporalRecord.FILTERLOGIC_LASTUPDATED_AFTER),
            @Filter(name = Nontemporal.FILTER_LASTUPDATED_BEFORE, condition = CvrNontemporalRecord.FILTERLOGIC_LASTUPDATED_BEFORE)
    })
    @JsonProperty(value = IO_FIELD_LIFECYCLE)
    private Set<LifecycleRecord> lifecycle = new HashSet<>();

    public void setLifecycle(Set<LifecycleRecord> lifecycle) {
        this.lifecycle = (lifecycle == null) ? new HashSet<>() : new HashSet<>(lifecycle);
        for (LifecycleRecord lifecycleRecord : this.lifecycle) {
            lifecycleRecord.setCompanyRecord(this);
        }
    }

    public void addLifecycle(LifecycleRecord record) {
        if (record != null) {
            record.setCompanyRecord(this);
            if (!lifecycle.isEmpty()) {
                this.addDataEventRecord(new CompanyDataEventRecord(record.getLastUpdated(), record.getFieldName(), this.lifecycle.stream().reduce((first, second) -> second).get().getId()));
            }
            this.lifecycle.add(record);
        }
    }

    public BitemporalSet<LifecycleRecord, CompanyRecord> getLifecycle() {
        return new BitemporalSet<>(this.lifecycle, LifecycleRecord.class, this, LifecycleRecord.DB_FIELD_COMPANY);
    }


    public static final String DB_FIELD_PRIMARY_INDUSTRY = "primaryIndustry";
    public static final String IO_FIELD_PRIMARY_INDUSTRY = "hovedbranche";
    public static final String CLAUSE_PRIMARY_INDUSTRY = CompanyIndustryRecord.DB_FIELD_INDEX + "=0";

    @OneToMany(targetEntity = CompanyIndustryRecord.class, mappedBy = CompanyIndustryRecord.DB_FIELD_COMPANY, cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Where(clause = CLAUSE_PRIMARY_INDUSTRY)
    @Filters({
            @Filter(name = Bitemporal.FILTER_EFFECTFROM_AFTER, condition = CvrBitemporalRecord.FILTERLOGIC_EFFECTFROM_AFTER),
            @Filter(name = Bitemporal.FILTER_EFFECTFROM_BEFORE, condition = CvrBitemporalRecord.FILTERLOGIC_EFFECTFROM_BEFORE),
            @Filter(name = Bitemporal.FILTER_EFFECTTO_AFTER, condition = CvrBitemporalRecord.FILTERLOGIC_EFFECTTO_AFTER),
            @Filter(name = Bitemporal.FILTER_EFFECTTO_BEFORE, condition = CvrBitemporalRecord.FILTERLOGIC_EFFECTTO_BEFORE),
            @Filter(name = Monotemporal.FILTER_REGISTRATIONFROM_AFTER, condition = CvrBitemporalRecord.FILTERLOGIC_REGISTRATIONFROM_AFTER),
            @Filter(name = Monotemporal.FILTER_REGISTRATIONFROM_BEFORE, condition = CvrBitemporalRecord.FILTERLOGIC_REGISTRATIONFROM_BEFORE),
            // @Filter(name = Monotemporal.FILTER_REGISTRATIONTO_AFTER, condition = Monotemporal.FILTERLOGIC_REGISTRATIONTO_AFTER),
            // @Filter(name = Monotemporal.FILTER_REGISTRATIONTO_BEFORE, condition = Monotemporal.FILTERLOGIC_REGISTRATIONTO_BEFORE),
            @Filter(name = Nontemporal.FILTER_LASTUPDATED_AFTER, condition = CvrNontemporalRecord.FILTERLOGIC_LASTUPDATED_AFTER),
            @Filter(name = Nontemporal.FILTER_LASTUPDATED_BEFORE, condition = CvrNontemporalRecord.FILTERLOGIC_LASTUPDATED_BEFORE)
    })
    @JsonProperty(value = IO_FIELD_PRIMARY_INDUSTRY)
    private Set<CompanyIndustryRecord> primaryIndustry = new HashSet<>();

    public void setPrimaryIndustry(Set<CompanyIndustryRecord> primaryIndustry) {
        this.primaryIndustry = (primaryIndustry == null) ? new HashSet<>() : new HashSet<>(primaryIndustry);
        for (CompanyIndustryRecord record : this.primaryIndustry) {
            record.setIndex(0);
            record.setCompanyRecord(this);
        }
    }

    public void addPrimaryIndustry(CompanyIndustryRecord record) {
        if (record != null) {
            record.setIndex(0);
            record.setCompanyRecord(this);
            if (!primaryIndustry.isEmpty()) {
                this.addDataEventRecord(new CompanyDataEventRecord(record.getLastUpdated(), record.getFieldName(), this.primaryIndustry.stream().reduce((first, second) -> second).get().getId()));
            }
            this.primaryIndustry.add(record);
        }
    }

    public BitemporalSet<CompanyIndustryRecord, CompanyRecord> getPrimaryIndustry() {
        return new BitemporalSet<>(this.primaryIndustry, CompanyIndustryRecord.class, this, CompanyIndustryRecord.DB_FIELD_COMPANY, "index=0");
    }


    public static final String DB_FIELD_SECONDARY_INDUSTRY1 = "secondaryIndustry1";
    public static final String IO_FIELD_SECONDARY_INDUSTRY1 = "bibranche1";
    public static final String CLAUSE_SECONDARY_INDUSTRY1 = CompanyIndustryRecord.DB_FIELD_INDEX + "=1";

    @OneToMany(targetEntity = CompanyIndustryRecord.class, mappedBy = CompanyIndustryRecord.DB_FIELD_COMPANY, cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Where(clause = CLAUSE_SECONDARY_INDUSTRY1)
    @Filters({
            @Filter(name = Bitemporal.FILTER_EFFECTFROM_AFTER, condition = CvrBitemporalRecord.FILTERLOGIC_EFFECTFROM_AFTER),
            @Filter(name = Bitemporal.FILTER_EFFECTFROM_BEFORE, condition = CvrBitemporalRecord.FILTERLOGIC_EFFECTFROM_BEFORE),
            @Filter(name = Bitemporal.FILTER_EFFECTTO_AFTER, condition = CvrBitemporalRecord.FILTERLOGIC_EFFECTTO_AFTER),
            @Filter(name = Bitemporal.FILTER_EFFECTTO_BEFORE, condition = CvrBitemporalRecord.FILTERLOGIC_EFFECTTO_BEFORE),
            @Filter(name = Monotemporal.FILTER_REGISTRATIONFROM_AFTER, condition = CvrBitemporalRecord.FILTERLOGIC_REGISTRATIONFROM_AFTER),
            @Filter(name = Monotemporal.FILTER_REGISTRATIONFROM_BEFORE, condition = CvrBitemporalRecord.FILTERLOGIC_REGISTRATIONFROM_BEFORE),
            // @Filter(name = Monotemporal.FILTER_REGISTRATIONTO_AFTER, condition = Monotemporal.FILTERLOGIC_REGISTRATIONTO_AFTER),
            // @Filter(name = Monotemporal.FILTER_REGISTRATIONTO_BEFORE, condition = Monotemporal.FILTERLOGIC_REGISTRATIONTO_BEFORE),
            @Filter(name = Nontemporal.FILTER_LASTUPDATED_AFTER, condition = CvrNontemporalRecord.FILTERLOGIC_LASTUPDATED_AFTER),
            @Filter(name = Nontemporal.FILTER_LASTUPDATED_BEFORE, condition = CvrNontemporalRecord.FILTERLOGIC_LASTUPDATED_BEFORE)
    })
    @JsonProperty(value = IO_FIELD_SECONDARY_INDUSTRY1)
    private Set<CompanyIndustryRecord> secondaryIndustry1 = new HashSet<>();

    public void setSecondaryIndustry1(Set<CompanyIndustryRecord> secondaryIndustryRecords) {
        this.secondaryIndustry1 = (secondaryIndustryRecords == null) ? new HashSet<>() : new HashSet<>(secondaryIndustryRecords);
        for (CompanyIndustryRecord record : this.secondaryIndustry1) {
            record.setIndex(1);
            record.setCompanyRecord(this);
        }
    }

    public void addSecondaryIndustry1(CompanyIndustryRecord record) {
        if (record != null) {
            record.setIndex(1);
            record.setCompanyRecord(this);
            if (!secondaryIndustry1.isEmpty()) {
                this.addDataEventRecord(new CompanyDataEventRecord(record.getLastUpdated(), record.getFieldName(), this.secondaryIndustry1.stream().reduce((first, second) -> second).get().getId()));
            }
            this.secondaryIndustry1.add(record);
        }
    }

    public BitemporalSet<CompanyIndustryRecord, CompanyRecord> getSecondaryIndustry1() {
        return new BitemporalSet<>(this.secondaryIndustry1, CompanyIndustryRecord.class, this, CompanyIndustryRecord.DB_FIELD_COMPANY, "index=1");
    }


    public static final String DB_FIELD_SECONDARY_INDUSTRY2 = "secondaryIndustry2";
    public static final String IO_FIELD_SECONDARY_INDUSTRY2 = "bibranche2";
    public static final String CLAUSE_SECONDARY_INDUSTRY2 = CompanyIndustryRecord.DB_FIELD_INDEX + "=2";

    @OneToMany(targetEntity = CompanyIndustryRecord.class, mappedBy = CompanyIndustryRecord.DB_FIELD_COMPANY, cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Where(clause = CLAUSE_SECONDARY_INDUSTRY2)
    @Filters({
            @Filter(name = Bitemporal.FILTER_EFFECTFROM_AFTER, condition = CvrBitemporalRecord.FILTERLOGIC_EFFECTFROM_AFTER),
            @Filter(name = Bitemporal.FILTER_EFFECTFROM_BEFORE, condition = CvrBitemporalRecord.FILTERLOGIC_EFFECTFROM_BEFORE),
            @Filter(name = Bitemporal.FILTER_EFFECTTO_AFTER, condition = CvrBitemporalRecord.FILTERLOGIC_EFFECTTO_AFTER),
            @Filter(name = Bitemporal.FILTER_EFFECTTO_BEFORE, condition = CvrBitemporalRecord.FILTERLOGIC_EFFECTTO_BEFORE),
            @Filter(name = Monotemporal.FILTER_REGISTRATIONFROM_AFTER, condition = CvrBitemporalRecord.FILTERLOGIC_REGISTRATIONFROM_AFTER),
            @Filter(name = Monotemporal.FILTER_REGISTRATIONFROM_BEFORE, condition = CvrBitemporalRecord.FILTERLOGIC_REGISTRATIONFROM_BEFORE),
            // @Filter(name = Monotemporal.FILTER_REGISTRATIONTO_AFTER, condition = Monotemporal.FILTERLOGIC_REGISTRATIONTO_AFTER),
            // @Filter(name = Monotemporal.FILTER_REGISTRATIONTO_BEFORE, condition = Monotemporal.FILTERLOGIC_REGISTRATIONTO_BEFORE),
            @Filter(name = Nontemporal.FILTER_LASTUPDATED_AFTER, condition = CvrNontemporalRecord.FILTERLOGIC_LASTUPDATED_AFTER),
            @Filter(name = Nontemporal.FILTER_LASTUPDATED_BEFORE, condition = CvrNontemporalRecord.FILTERLOGIC_LASTUPDATED_BEFORE)
    })
    @JsonProperty(value = IO_FIELD_SECONDARY_INDUSTRY2)
    private Set<CompanyIndustryRecord> secondaryIndustry2 = new HashSet<>();

    public void setSecondaryIndustry2(Set<CompanyIndustryRecord> secondaryIndustryRecords) {
        this.secondaryIndustry2 = (secondaryIndustryRecords == null) ? new HashSet<>() : new HashSet<>(secondaryIndustryRecords);
        for (CompanyIndustryRecord record : this.secondaryIndustry2) {
            record.setIndex(2);
            record.setCompanyRecord(this);
        }
    }

    public void addSecondaryIndustry2(CompanyIndustryRecord record) {
        if (record != null) {
            record.setIndex(2);
            record.setCompanyRecord(this);
            if (!secondaryIndustry2.isEmpty()) {
                this.addDataEventRecord(new CompanyDataEventRecord(record.getLastUpdated(), record.getFieldName(), this.secondaryIndustry2.stream().reduce((first, second) -> second).get().getId()));
            }
            this.secondaryIndustry2.add(record);
        }
    }

    public BitemporalSet<CompanyIndustryRecord, CompanyRecord> getSecondaryIndustry2() {
        return new BitemporalSet<>(this.secondaryIndustry2, CompanyIndustryRecord.class, this, CompanyIndustryRecord.DB_FIELD_COMPANY, "index=2");
    }


    public static final String DB_FIELD_SECONDARY_INDUSTRY3 = "secondaryIndustry3";
    public static final String IO_FIELD_SECONDARY_INDUSTRY3 = "bibranche3";
    public static final String CLAUSE_SECONDARY_INDUSTRY3 = CompanyIndustryRecord.DB_FIELD_INDEX + "=3";

    @OneToMany(targetEntity = CompanyIndustryRecord.class, mappedBy = CompanyIndustryRecord.DB_FIELD_COMPANY, cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Where(clause = CLAUSE_SECONDARY_INDUSTRY3)
    @Filters({
            @Filter(name = Bitemporal.FILTER_EFFECTFROM_AFTER, condition = CvrBitemporalRecord.FILTERLOGIC_EFFECTFROM_AFTER),
            @Filter(name = Bitemporal.FILTER_EFFECTFROM_BEFORE, condition = CvrBitemporalRecord.FILTERLOGIC_EFFECTFROM_BEFORE),
            @Filter(name = Bitemporal.FILTER_EFFECTTO_AFTER, condition = CvrBitemporalRecord.FILTERLOGIC_EFFECTTO_AFTER),
            @Filter(name = Bitemporal.FILTER_EFFECTTO_BEFORE, condition = CvrBitemporalRecord.FILTERLOGIC_EFFECTTO_BEFORE),
            @Filter(name = Monotemporal.FILTER_REGISTRATIONFROM_AFTER, condition = CvrBitemporalRecord.FILTERLOGIC_REGISTRATIONFROM_AFTER),
            @Filter(name = Monotemporal.FILTER_REGISTRATIONFROM_BEFORE, condition = CvrBitemporalRecord.FILTERLOGIC_REGISTRATIONFROM_BEFORE),
            // @Filter(name = Monotemporal.FILTER_REGISTRATIONTO_AFTER, condition = Monotemporal.FILTERLOGIC_REGISTRATIONTO_AFTER),
            // @Filter(name = Monotemporal.FILTER_REGISTRATIONTO_BEFORE, condition = Monotemporal.FILTERLOGIC_REGISTRATIONTO_BEFORE),
            @Filter(name = Nontemporal.FILTER_LASTUPDATED_AFTER, condition = CvrNontemporalRecord.FILTERLOGIC_LASTUPDATED_AFTER),
            @Filter(name = Nontemporal.FILTER_LASTUPDATED_BEFORE, condition = CvrNontemporalRecord.FILTERLOGIC_LASTUPDATED_BEFORE)
    })
    @JsonProperty(value = IO_FIELD_SECONDARY_INDUSTRY3)
    private Set<CompanyIndustryRecord> secondaryIndustry3 = new HashSet<>();

    public void setSecondaryIndustry3(Set<CompanyIndustryRecord> secondaryIndustryRecords) {
        this.secondaryIndustry3 = (secondaryIndustryRecords == null) ? new HashSet<>() : new HashSet<>(secondaryIndustryRecords);
        for (CompanyIndustryRecord record : this.secondaryIndustry3) {
            record.setIndex(3);
            record.setCompanyRecord(this);
        }
    }

    public void addSecondaryIndustry3(CompanyIndustryRecord record) {
        if (record != null) {
            record.setIndex(3);
            record.setCompanyRecord(this);
            if (!secondaryIndustry3.isEmpty()) {
                this.addDataEventRecord(new CompanyDataEventRecord(record.getLastUpdated(), record.getFieldName(), this.secondaryIndustry3.stream().reduce((first, second) -> second).get().getId()));
            }
            this.secondaryIndustry3.add(record);
        }
    }

    public BitemporalSet<CompanyIndustryRecord, CompanyRecord> getSecondaryIndustry3() {
        return new BitemporalSet<>(this.secondaryIndustry3, CompanyIndustryRecord.class, this, CompanyIndustryRecord.DB_FIELD_COMPANY, "index=3");
    }


    public static final String DB_FIELD_STATUS = "status";
    public static final String IO_FIELD_STATUS = "status";

    @OneToMany(targetEntity = StatusRecord.class, mappedBy = CompanyStatusRecord.DB_FIELD_COMPANY, cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Filters({
            @Filter(name = Bitemporal.FILTER_EFFECTFROM_AFTER, condition = CvrBitemporalRecord.FILTERLOGIC_EFFECTFROM_AFTER),
            @Filter(name = Bitemporal.FILTER_EFFECTFROM_BEFORE, condition = CvrBitemporalRecord.FILTERLOGIC_EFFECTFROM_BEFORE),
            @Filter(name = Bitemporal.FILTER_EFFECTTO_AFTER, condition = CvrBitemporalRecord.FILTERLOGIC_EFFECTTO_AFTER),
            @Filter(name = Bitemporal.FILTER_EFFECTTO_BEFORE, condition = CvrBitemporalRecord.FILTERLOGIC_EFFECTTO_BEFORE),
            @Filter(name = Monotemporal.FILTER_REGISTRATIONFROM_AFTER, condition = CvrBitemporalRecord.FILTERLOGIC_REGISTRATIONFROM_AFTER),
            @Filter(name = Monotemporal.FILTER_REGISTRATIONFROM_BEFORE, condition = CvrBitemporalRecord.FILTERLOGIC_REGISTRATIONFROM_BEFORE),
            // @Filter(name = Monotemporal.FILTER_REGISTRATIONTO_AFTER, condition = Monotemporal.FILTERLOGIC_REGISTRATIONTO_AFTER),
            // @Filter(name = Monotemporal.FILTER_REGISTRATIONTO_BEFORE, condition = Monotemporal.FILTERLOGIC_REGISTRATIONTO_BEFORE),
            @Filter(name = Nontemporal.FILTER_LASTUPDATED_AFTER, condition = CvrNontemporalRecord.FILTERLOGIC_LASTUPDATED_AFTER),
            @Filter(name = Nontemporal.FILTER_LASTUPDATED_BEFORE, condition = CvrNontemporalRecord.FILTERLOGIC_LASTUPDATED_BEFORE)
    })
    @JsonProperty(value = IO_FIELD_STATUS)
    private Set<StatusRecord> status = new HashSet<>();

    public void setStatus(Set<StatusRecord> status) {
        this.status = (status == null) ? new HashSet<>() : new HashSet<>(status);
        for (StatusRecord statusRecord : this.status) {
            statusRecord.setCompanyRecord(this);
        }
    }

    public void addStatus(StatusRecord record) {
        if (record != null) {
            record.setCompanyRecord(this);
            if (!status.isEmpty()) {
                this.addDataEventRecord(new CompanyDataEventRecord(record.getLastUpdated(), record.getFieldName(), this.status.stream().reduce((first, second) -> second).get().getId()));
            }
            this.status.add(record);
        }
    }

    public BitemporalSet<StatusRecord, CompanyRecord> getStatus() {
        return new BitemporalSet<>(this.status, StatusRecord.class, this, StatusRecord.DB_FIELD_COMPANY);
    }


    public static final String DB_FIELD_COMPANYSTATUS = "companyStatus";
    public static final String IO_FIELD_COMPANYSTATUS = "virksomhedsstatus";

    @OneToMany(targetEntity = CompanyStatusRecord.class, mappedBy = CompanyStatusRecord.DB_FIELD_COMPANY, cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Filters({
            @Filter(name = Bitemporal.FILTER_EFFECTFROM_AFTER, condition = CvrBitemporalRecord.FILTERLOGIC_EFFECTFROM_AFTER),
            @Filter(name = Bitemporal.FILTER_EFFECTFROM_BEFORE, condition = CvrBitemporalRecord.FILTERLOGIC_EFFECTFROM_BEFORE),
            @Filter(name = Bitemporal.FILTER_EFFECTTO_AFTER, condition = CvrBitemporalRecord.FILTERLOGIC_EFFECTTO_AFTER),
            @Filter(name = Bitemporal.FILTER_EFFECTTO_BEFORE, condition = CvrBitemporalRecord.FILTERLOGIC_EFFECTTO_BEFORE),
            @Filter(name = Monotemporal.FILTER_REGISTRATIONFROM_AFTER, condition = CvrBitemporalRecord.FILTERLOGIC_REGISTRATIONFROM_AFTER),
            @Filter(name = Monotemporal.FILTER_REGISTRATIONFROM_BEFORE, condition = CvrBitemporalRecord.FILTERLOGIC_REGISTRATIONFROM_BEFORE),
            // @Filter(name = Monotemporal.FILTER_REGISTRATIONTO_AFTER, condition = Monotemporal.FILTERLOGIC_REGISTRATIONTO_AFTER),
            // @Filter(name = Monotemporal.FILTER_REGISTRATIONTO_BEFORE, condition = Monotemporal.FILTERLOGIC_REGISTRATIONTO_BEFORE),
            @Filter(name = Nontemporal.FILTER_LASTUPDATED_AFTER, condition = CvrNontemporalRecord.FILTERLOGIC_LASTUPDATED_AFTER),
            @Filter(name = Nontemporal.FILTER_LASTUPDATED_BEFORE, condition = CvrNontemporalRecord.FILTERLOGIC_LASTUPDATED_BEFORE)
    })
    @JsonProperty(value = IO_FIELD_COMPANYSTATUS)
    private Set<CompanyStatusRecord> companyStatus = new HashSet<>();


    public void setCompanyStatus(Set<CompanyStatusRecord> companyStatus) {
        this.companyStatus = (companyStatus == null) ? new HashSet<>() : new HashSet<>(companyStatus);
        for (CompanyStatusRecord statusRecord : this.companyStatus) {
            statusRecord.setCompanyRecord(this);
        }
    }

    public void addCompanyStatus(CompanyStatusRecord record) {
        if (record != null) {
            record.setCompanyRecord(this);
            if (!companyStatus.isEmpty()) {
                this.addDataEventRecord(new CompanyDataEventRecord(record.getLastUpdated(), record.getFieldName(), this.companyStatus.stream().reduce((first, second) -> second).get().getId()));
            }
            this.companyStatus.add(record);
        }
    }

    public BitemporalSet<CompanyStatusRecord, CompanyRecord> getCompanyStatus() {
        return new BitemporalSet<>(this.companyStatus, CompanyStatusRecord.class, this, CompanyStatusRecord.DB_FIELD_COMPANY);
    }


    public static final String DB_FIELD_FORM = "companyForm";
    public static final String IO_FIELD_FORM = "virksomhedsform";

    @OneToMany(targetEntity = FormRecord.class, mappedBy = FormRecord.DB_FIELD_COMPANY, cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Filters({
            @Filter(name = Bitemporal.FILTER_EFFECTFROM_AFTER, condition = CvrBitemporalRecord.FILTERLOGIC_EFFECTFROM_AFTER),
            @Filter(name = Bitemporal.FILTER_EFFECTFROM_BEFORE, condition = CvrBitemporalRecord.FILTERLOGIC_EFFECTFROM_BEFORE),
            @Filter(name = Bitemporal.FILTER_EFFECTTO_AFTER, condition = CvrBitemporalRecord.FILTERLOGIC_EFFECTTO_AFTER),
            @Filter(name = Bitemporal.FILTER_EFFECTTO_BEFORE, condition = CvrBitemporalRecord.FILTERLOGIC_EFFECTTO_BEFORE),
            @Filter(name = Monotemporal.FILTER_REGISTRATIONFROM_AFTER, condition = CvrBitemporalRecord.FILTERLOGIC_REGISTRATIONFROM_AFTER),
            @Filter(name = Monotemporal.FILTER_REGISTRATIONFROM_BEFORE, condition = CvrBitemporalRecord.FILTERLOGIC_REGISTRATIONFROM_BEFORE),
            // @Filter(name = Monotemporal.FILTER_REGISTRATIONTO_AFTER, condition = Monotemporal.FILTERLOGIC_REGISTRATIONTO_AFTER),
            // @Filter(name = Monotemporal.FILTER_REGISTRATIONTO_BEFORE, condition = Monotemporal.FILTERLOGIC_REGISTRATIONTO_BEFORE),
            @Filter(name = Nontemporal.FILTER_LASTUPDATED_AFTER, condition = CvrNontemporalRecord.FILTERLOGIC_LASTUPDATED_AFTER),
            @Filter(name = Nontemporal.FILTER_LASTUPDATED_BEFORE, condition = CvrNontemporalRecord.FILTERLOGIC_LASTUPDATED_BEFORE)
    })
    @JsonProperty(value = IO_FIELD_FORM)
    private Set<FormRecord> companyForm = new HashSet<>();

    public void setCompanyForm(Set<FormRecord> companyForm) {
        this.companyForm = (companyForm == null) ? new HashSet<>() : new HashSet<>(companyForm);
        for (FormRecord formRecord : this.companyForm) {
            formRecord.setCompanyRecord(this);
        }
    }

    public void addCompanyForm(FormRecord record) {
        if (record != null) {
            record.setCompanyRecord(this);
            if (!companyForm.isEmpty()) {
                this.addDataEventRecord(new CompanyDataEventRecord(record.getLastUpdated(), record.getFieldName(), this.companyForm.stream().reduce((first, second) -> second).get().getId()));
            }
            this.companyForm.add(record);
        }
    }

    public BitemporalSet<FormRecord, CompanyRecord> getCompanyForm() {
        return new BitemporalSet<>(this.companyForm, FormRecord.class, this, FormRecord.DB_FIELD_COMPANY);
    }


    public static final String DB_FIELD_YEARLY_NUMBERS = "yearlyNumbers";
    public static final String IO_FIELD_YEARLY_NUMBERS = "aarsbeskaeftigelse";

    @OneToMany(targetEntity = CompanyYearlyNumbersRecord.class, mappedBy = CompanyYearlyNumbersRecord.DB_FIELD_COMPANY, cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Filters({
            @Filter(name = Bitemporal.FILTER_EFFECTFROM_AFTER, condition = CvrBitemporalRecord.FILTERLOGIC_EFFECTFROM_AFTER),
            @Filter(name = Bitemporal.FILTER_EFFECTFROM_BEFORE, condition = CvrBitemporalRecord.FILTERLOGIC_EFFECTFROM_BEFORE),
            @Filter(name = Bitemporal.FILTER_EFFECTTO_AFTER, condition = CvrBitemporalRecord.FILTERLOGIC_EFFECTTO_AFTER),
            @Filter(name = Bitemporal.FILTER_EFFECTTO_BEFORE, condition = CvrBitemporalRecord.FILTERLOGIC_EFFECTTO_BEFORE),
            @Filter(name = Monotemporal.FILTER_REGISTRATIONFROM_AFTER, condition = CvrBitemporalRecord.FILTERLOGIC_REGISTRATIONFROM_AFTER),
            @Filter(name = Monotemporal.FILTER_REGISTRATIONFROM_BEFORE, condition = CvrBitemporalRecord.FILTERLOGIC_REGISTRATIONFROM_BEFORE),
            // @Filter(name = Monotemporal.FILTER_REGISTRATIONTO_AFTER, condition = Monotemporal.FILTERLOGIC_REGISTRATIONTO_AFTER),
            // @Filter(name = Monotemporal.FILTER_REGISTRATIONTO_BEFORE, condition = Monotemporal.FILTERLOGIC_REGISTRATIONTO_BEFORE),
            @Filter(name = Nontemporal.FILTER_LASTUPDATED_AFTER, condition = CvrNontemporalRecord.FILTERLOGIC_LASTUPDATED_AFTER),
            @Filter(name = Nontemporal.FILTER_LASTUPDATED_BEFORE, condition = CvrNontemporalRecord.FILTERLOGIC_LASTUPDATED_BEFORE)
    })
    @JsonProperty(value = IO_FIELD_YEARLY_NUMBERS)
    private Set<CompanyYearlyNumbersRecord> yearlyNumbers = new HashSet<>();

    public void setYearlyNumbers(Set<CompanyYearlyNumbersRecord> yearlyNumbers) {
        this.yearlyNumbers = (yearlyNumbers == null) ? new HashSet<>() : new HashSet<>(yearlyNumbers);
        for (CompanyYearlyNumbersRecord yearlyNumbersRecord : this.yearlyNumbers) {
            yearlyNumbersRecord.setCompanyRecord(this);
        }
    }

    public void addYearlyNumbers(CompanyYearlyNumbersRecord record) {
        if (record != null) {
            record.setCompanyRecord(this);
            this.yearlyNumbers.add(record);
        }
    }

    public BitemporalSet<CompanyYearlyNumbersRecord, CompanyRecord> getYearlyNumbers() {
        return new BitemporalSet<>(this.yearlyNumbers, CompanyYearlyNumbersRecord.class, this, CompanyYearlyNumbersRecord.DB_FIELD_COMPANY);
    }

    @JsonIgnore
    public BitemporalSetGroup<CompanyYearlyNumbersRecord, CompanyRecord> getYearlyNumbersGrouped() {
        BitemporalSetGroup<CompanyYearlyNumbersRecord, CompanyRecord> groups = new BitemporalSetGroup<>();
        for (CompanyYearlyNumbersRecord record : this.yearlyNumbers) {
            int year = record.getYear();
            if (!groups.containsKey(year)) {
                groups.put(
                        year, 0,
                        new BitemporalSet<>(
                                new HashSet<>(),
                                CompanyYearlyNumbersRecord.class,
                                this,
                                CompanyYearlyNumbersRecord.DB_FIELD_COMPANY,
                                CompanyYearlyNumbersRecord.DB_FIELD_YEAR + "=" + year,
                                this.getYearlyNumbers()
                        )
                );
            }
            groups.get(year, 0).add(record);
        }
        return groups;
    }


    public static final String DB_FIELD_QUARTERLY_NUMBERS = "quarterlyNumbers";
    public static final String IO_FIELD_QUARTERLY_NUMBERS = "kvartalsbeskaeftigelse";

    @OneToMany(targetEntity = CompanyQuarterlyNumbersRecord.class, mappedBy = CompanyQuarterlyNumbersRecord.DB_FIELD_COMPANY, cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Filters({
            @Filter(name = Bitemporal.FILTER_EFFECTFROM_AFTER, condition = CvrBitemporalRecord.FILTERLOGIC_EFFECTFROM_AFTER),
            @Filter(name = Bitemporal.FILTER_EFFECTFROM_BEFORE, condition = CvrBitemporalRecord.FILTERLOGIC_EFFECTFROM_BEFORE),
            @Filter(name = Bitemporal.FILTER_EFFECTTO_AFTER, condition = CvrBitemporalRecord.FILTERLOGIC_EFFECTTO_AFTER),
            @Filter(name = Bitemporal.FILTER_EFFECTTO_BEFORE, condition = CvrBitemporalRecord.FILTERLOGIC_EFFECTTO_BEFORE),
            @Filter(name = Monotemporal.FILTER_REGISTRATIONFROM_AFTER, condition = CvrBitemporalRecord.FILTERLOGIC_REGISTRATIONFROM_AFTER),
            @Filter(name = Monotemporal.FILTER_REGISTRATIONFROM_BEFORE, condition = CvrBitemporalRecord.FILTERLOGIC_REGISTRATIONFROM_BEFORE),
            // @Filter(name = Monotemporal.FILTER_REGISTRATIONTO_AFTER, condition = Monotemporal.FILTERLOGIC_REGISTRATIONTO_AFTER),
            // @Filter(name = Monotemporal.FILTER_REGISTRATIONTO_BEFORE, condition = Monotemporal.FILTERLOGIC_REGISTRATIONTO_BEFORE),
            @Filter(name = Nontemporal.FILTER_LASTUPDATED_AFTER, condition = CvrNontemporalRecord.FILTERLOGIC_LASTUPDATED_AFTER),
            @Filter(name = Nontemporal.FILTER_LASTUPDATED_BEFORE, condition = CvrNontemporalRecord.FILTERLOGIC_LASTUPDATED_BEFORE)
    })
    @JsonProperty(value = IO_FIELD_QUARTERLY_NUMBERS)
    private Set<CompanyQuarterlyNumbersRecord> quarterlyNumbers = new HashSet<>();

    public void setQuarterlyNumbers(Set<CompanyQuarterlyNumbersRecord> quarterlyNumbers) {
        this.quarterlyNumbers = (quarterlyNumbers == null) ? new HashSet<>() : new HashSet<>(quarterlyNumbers);
        for (CompanyQuarterlyNumbersRecord quarterlyNumbersRecord : this.quarterlyNumbers) {
            quarterlyNumbersRecord.setCompanyRecord(this);
        }
    }

    public void addQuarterlyNumbers(CompanyQuarterlyNumbersRecord record) {
        if (record != null) {
            record.setCompanyRecord(this);
            this.quarterlyNumbers.add(record);
        }
    }

    public BitemporalSet<CompanyQuarterlyNumbersRecord, CompanyRecord> getQuarterlyNumbers() {
        return new BitemporalSet<>(this.quarterlyNumbers, CompanyQuarterlyNumbersRecord.class, this, CompanyQuarterlyNumbersRecord.DB_FIELD_COMPANY);
    }

    @JsonIgnore
    public BitemporalSetGroup<CompanyQuarterlyNumbersRecord, CompanyRecord> getQuarterlyNumbersGrouped() {
        BitemporalSetGroup<CompanyQuarterlyNumbersRecord, CompanyRecord> groups = new BitemporalSetGroup<>();
        for (CompanyQuarterlyNumbersRecord record : this.quarterlyNumbers) {
            int year = record.getYear();
            int quarter = record.getQuarter();
            if (!groups.containsKey(year, quarter)) {
                groups.put(year, quarter, new BitemporalSet<>(
                        new HashSet<>(),
                        CompanyQuarterlyNumbersRecord.class,
                        this,
                        CompanyQuarterlyNumbersRecord.DB_FIELD_COMPANY,
                        CompanyQuarterlyNumbersRecord.DB_FIELD_YEAR + "=" + year + " AND " + CompanyQuarterlyNumbersRecord.DB_FIELD_QUARTER + "=" + quarter,
                        this.getQuarterlyNumbers()
                ));
            }
            groups.get(year, quarter).add(record);
        }
        return groups;
    }

    public static final String DB_FIELD_MONTHLY_NUMBERS = "monthlyNumbers";
    public static final String IO_FIELD_MONTHLY_NUMBERS = "maanedsbeskaeftigelse";

    @OneToMany(targetEntity = CompanyMonthlyNumbersRecord.class, mappedBy = CompanyMonthlyNumbersRecord.DB_FIELD_COMPANY, cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Filters({
            @Filter(name = Bitemporal.FILTER_EFFECTFROM_AFTER, condition = CvrBitemporalRecord.FILTERLOGIC_EFFECTFROM_AFTER),
            @Filter(name = Bitemporal.FILTER_EFFECTFROM_BEFORE, condition = CvrBitemporalRecord.FILTERLOGIC_EFFECTFROM_BEFORE),
            @Filter(name = Bitemporal.FILTER_EFFECTTO_AFTER, condition = CvrBitemporalRecord.FILTERLOGIC_EFFECTTO_AFTER),
            @Filter(name = Bitemporal.FILTER_EFFECTTO_BEFORE, condition = CvrBitemporalRecord.FILTERLOGIC_EFFECTTO_BEFORE),
            @Filter(name = Monotemporal.FILTER_REGISTRATIONFROM_AFTER, condition = CvrBitemporalRecord.FILTERLOGIC_REGISTRATIONFROM_AFTER),
            @Filter(name = Monotemporal.FILTER_REGISTRATIONFROM_BEFORE, condition = CvrBitemporalRecord.FILTERLOGIC_REGISTRATIONFROM_BEFORE),
            // @Filter(name = Monotemporal.FILTER_REGISTRATIONTO_AFTER, condition = Monotemporal.FILTERLOGIC_REGISTRATIONTO_AFTER),
            // @Filter(name = Monotemporal.FILTER_REGISTRATIONTO_BEFORE, condition = Monotemporal.FILTERLOGIC_REGISTRATIONTO_BEFORE),
            @Filter(name = Nontemporal.FILTER_LASTUPDATED_AFTER, condition = CvrNontemporalRecord.FILTERLOGIC_LASTUPDATED_AFTER),
            @Filter(name = Nontemporal.FILTER_LASTUPDATED_BEFORE, condition = CvrNontemporalRecord.FILTERLOGIC_LASTUPDATED_BEFORE)
    })
    @JsonProperty(value = IO_FIELD_MONTHLY_NUMBERS)
    private Set<CompanyMonthlyNumbersRecord> monthlyNumbers = new HashSet<>();

    public void setMonthlyNumbers(Set<CompanyMonthlyNumbersRecord> monthlyNumbers) {
        this.monthlyNumbers = (monthlyNumbers == null) ? new HashSet<>() : new HashSet<>(monthlyNumbers);
        for (CompanyMonthlyNumbersRecord monthlyNumbersRecord : this.monthlyNumbers) {
            monthlyNumbersRecord.setCompanyRecord(this);
        }
    }

    public void addMonthlyNumbers(CompanyMonthlyNumbersRecord record) {
        if (record != null) {
            record.setCompanyRecord(this);
            this.monthlyNumbers.add(record);
        }
    }

    public BitemporalSet<CompanyMonthlyNumbersRecord, CompanyRecord> getMonthlyNumbers() {
        return new BitemporalSet<>(this.monthlyNumbers, CompanyMonthlyNumbersRecord.class, this, CompanyMonthlyNumbersRecord.DB_FIELD_COMPANY);
    }

    @JsonIgnore
    public BitemporalSetGroup<CompanyMonthlyNumbersRecord, CompanyRecord> getMonthlyNumbersGrouped() {
        BitemporalSetGroup<CompanyMonthlyNumbersRecord, CompanyRecord> groups = new BitemporalSetGroup<>();
        for (CompanyMonthlyNumbersRecord record : this.monthlyNumbers) {
            int year = record.getYear();
            int month = record.getMonth();
            if (!groups.containsKey(year, month)) {
                groups.put(year, month, new BitemporalSet<>(
                        new HashSet<>(),
                        CompanyMonthlyNumbersRecord.class,
                        this,
                        CompanyMonthlyNumbersRecord.DB_FIELD_COMPANY,
                        CompanyMonthlyNumbersRecord.DB_FIELD_YEAR + "=" + year + " AND " + CompanyMonthlyNumbersRecord.DB_FIELD_MONTH + "=" + month,
                        this.getMonthlyNumbers()
                ));
            }
            groups.get(year, month).add(record);
        }
        return groups;
    }


    public static final String DB_FIELD_ATTRIBUTES = "attributes";
    public static final String IO_FIELD_ATTRIBUTES = "attributter";

    @OneToMany(targetEntity = AttributeRecord.class, mappedBy = AttributeRecord.DB_FIELD_COMPANY, cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonProperty(value = IO_FIELD_ATTRIBUTES)
    private Set<AttributeRecord> attributes = new HashSet<>();

    public void setAttributes(Set<AttributeRecord> attributes) {
        this.attributes = (attributes == null) ? new HashSet<>() : new HashSet<>(attributes);
        for (AttributeRecord attributeRecord : this.attributes) {
            attributeRecord.setCompanyRecord(this);
        }
    }

    public void addAttribute(AttributeRecord record) {
        if (record != null) {
            record.setCompanyRecord(this);
            this.attributes.add(record);
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

    public AttributeRecordSet<CompanyRecord> getAttributes() {
        return new AttributeRecordSet<>(this.attributes, this, AttributeRecord.DB_FIELD_COMPANY);
    }

    public static final String DB_FIELD_P_UNITS = "productionUnits";
    public static final String IO_FIELD_P_UNITS = "penheder";

    @OneToMany(targetEntity = CompanyUnitLinkRecord.class, mappedBy = CompanyUnitLinkRecord.DB_FIELD_COMPANY, cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Filters({
            @Filter(name = Bitemporal.FILTER_EFFECTFROM_AFTER, condition = CvrBitemporalRecord.FILTERLOGIC_EFFECTFROM_AFTER),
            @Filter(name = Bitemporal.FILTER_EFFECTFROM_BEFORE, condition = CvrBitemporalRecord.FILTERLOGIC_EFFECTFROM_BEFORE),
            @Filter(name = Bitemporal.FILTER_EFFECTTO_AFTER, condition = CvrBitemporalRecord.FILTERLOGIC_EFFECTTO_AFTER),
            @Filter(name = Bitemporal.FILTER_EFFECTTO_BEFORE, condition = CvrBitemporalRecord.FILTERLOGIC_EFFECTTO_BEFORE),
            @Filter(name = Monotemporal.FILTER_REGISTRATIONFROM_AFTER, condition = CvrBitemporalRecord.FILTERLOGIC_REGISTRATIONFROM_AFTER),
            @Filter(name = Monotemporal.FILTER_REGISTRATIONFROM_BEFORE, condition = CvrBitemporalRecord.FILTERLOGIC_REGISTRATIONFROM_BEFORE),
            // @Filter(name = Monotemporal.FILTER_REGISTRATIONTO_AFTER, condition = Monotemporal.FILTERLOGIC_REGISTRATIONTO_AFTER),
            // @Filter(name = Monotemporal.FILTER_REGISTRATIONTO_BEFORE, condition = Monotemporal.FILTERLOGIC_REGISTRATIONTO_BEFORE),
            @Filter(name = Nontemporal.FILTER_LASTUPDATED_AFTER, condition = CvrNontemporalRecord.FILTERLOGIC_LASTUPDATED_AFTER),
            @Filter(name = Nontemporal.FILTER_LASTUPDATED_BEFORE, condition = CvrNontemporalRecord.FILTERLOGIC_LASTUPDATED_BEFORE)
    })
    @JsonProperty(value = IO_FIELD_P_UNITS)
    private Set<CompanyUnitLinkRecord> productionUnits = new HashSet<>();

    public void setProductionUnits(Set<CompanyUnitLinkRecord> productionUnits) {
        this.productionUnits = (productionUnits == null) ? new HashSet<>() : new HashSet<>(productionUnits);
        for (CompanyUnitLinkRecord unitLinkRecord : this.productionUnits) {
            unitLinkRecord.setCompanyRecord(this);
        }
    }

    public void addProductionUnit(CompanyUnitLinkRecord record) {
        if (record != null) {
            record.setCompanyRecord(this);
            if (!productionUnits.isEmpty()) {
                this.addDataEventRecord(new CompanyDataEventRecord(record.getLastUpdated(), record.getFieldName(), this.productionUnits.stream().reduce((first, second) -> second).get().getId()));
            }
            this.productionUnits.add(record);
        }
    }

    public BitemporalSet<CompanyUnitLinkRecord, CompanyRecord> getProductionUnits() {
        return new BitemporalSet<>(this.productionUnits, CompanyUnitLinkRecord.class, this, CompanyUnitLinkRecord.DB_FIELD_COMPANY);
    }


    public static final String DB_FIELD_PARTICIPANTS = "participants";
    public static final String IO_FIELD_PARTICIPANTS = "deltagerRelation";

    @OneToMany(targetEntity = CompanyParticipantRelationRecord.class, mappedBy = CompanyParticipantRelationRecord.DB_FIELD_COMPANY, cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Filters({
            @Filter(name = Bitemporal.FILTER_EFFECTFROM_AFTER, condition = CvrBitemporalRecord.FILTERLOGIC_EFFECTFROM_AFTER),
            @Filter(name = Bitemporal.FILTER_EFFECTFROM_BEFORE, condition = CvrBitemporalRecord.FILTERLOGIC_EFFECTFROM_BEFORE),
            @Filter(name = Bitemporal.FILTER_EFFECTTO_AFTER, condition = CvrBitemporalRecord.FILTERLOGIC_EFFECTTO_AFTER),
            @Filter(name = Bitemporal.FILTER_EFFECTTO_BEFORE, condition = CvrBitemporalRecord.FILTERLOGIC_EFFECTTO_BEFORE),
            @Filter(name = Monotemporal.FILTER_REGISTRATIONFROM_AFTER, condition = CvrBitemporalRecord.FILTERLOGIC_REGISTRATIONFROM_AFTER),
            @Filter(name = Monotemporal.FILTER_REGISTRATIONFROM_BEFORE, condition = CvrBitemporalRecord.FILTERLOGIC_REGISTRATIONFROM_BEFORE),
            // @Filter(name = Monotemporal.FILTER_REGISTRATIONTO_AFTER, condition = Monotemporal.FILTERLOGIC_REGISTRATIONTO_AFTER),
            // @Filter(name = Monotemporal.FILTER_REGISTRATIONTO_BEFORE, condition = Monotemporal.FILTERLOGIC_REGISTRATIONTO_BEFORE),
            @Filter(name = Nontemporal.FILTER_LASTUPDATED_AFTER, condition = CvrNontemporalRecord.FILTERLOGIC_LASTUPDATED_AFTER),
            @Filter(name = Nontemporal.FILTER_LASTUPDATED_BEFORE, condition = CvrNontemporalRecord.FILTERLOGIC_LASTUPDATED_BEFORE)
    })
    @JsonProperty(value = IO_FIELD_PARTICIPANTS)
    private Set<CompanyParticipantRelationRecord> participants = new HashSet<>();

    public void setParticipants(Set<CompanyParticipantRelationRecord> participants) {
        this.participants = (participants == null) ? new HashSet<>() : new HashSet<>(participants);
        for (CompanyParticipantRelationRecord participantRelationRecord : this.participants) {
            participantRelationRecord.setCompanyRecord(this);
        }
    }

    public void addParticipant(CompanyParticipantRelationRecord record) {
        if (record != null) {
            record.setCompanyRecord(this);
            if (!participants.isEmpty()) {
                this.addDataEventRecord(new CompanyDataEventRecord(record.getLastUpdated(), record.getFieldName(), this.participants.stream().reduce((first, second) -> second).get().getId()));
            }
            this.participants.add(record);
        }
    }

    public void mergeParticipant(CompanyParticipantRelationRecord otherRecord) {
        Long otherParticipantId = otherRecord.getParticipantUnitNumber();
        if (otherParticipantId != null) {
            for (CompanyParticipantRelationRecord ourRecord : this.participants) {
                Long ourParticipantId = ourRecord.getParticipantUnitNumber();
                if (otherParticipantId.equals(ourParticipantId)) {
                    ourRecord.merge(otherRecord);
                    return;
                }
            }
        }
        this.addParticipant(otherRecord);
    }

    public BitemporalSet<CompanyParticipantRelationRecord, CompanyRecord> getParticipants() {
        return new BitemporalSet<>(this.participants, CompanyParticipantRelationRecord.class, this, CompanyParticipantRelationRecord.DB_FIELD_COMPANY);
    }


    public static final String DB_FIELD_FUSIONS = "fusions";
    public static final String IO_FIELD_FUSIONS = "fusioner";
    public static final String CLAUSE_FUSIONS = FusionSplitRecord.DB_FIELD_SPLIT + "=false";

    @OneToMany(targetEntity = FusionSplitRecord.class, mappedBy = FusionSplitRecord.DB_FIELD_COMPANY, cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Where(clause = CLAUSE_FUSIONS)
    @JsonProperty(value = IO_FIELD_FUSIONS)
    private Set<FusionSplitRecord> fusions = new HashSet<>();

    public RecordSet<FusionSplitRecord, CompanyRecord> getFusions() {
        return new RecordSet<>(this.fusions, FusionSplitRecord.class, this, FusionSplitRecord.DB_FIELD_COMPANY, CLAUSE_FUSIONS);
    }

    public void setFusions(Set<FusionSplitRecord> fusions) {
        this.fusions = (fusions == null) ? new HashSet<>() : new HashSet<>(fusions);
        for (FusionSplitRecord fusionSplitRecord : this.fusions) {
            fusionSplitRecord.setCompanyRecord(this);
            fusionSplitRecord.setSplit(false);
        }
    }

    public void addFusion(FusionSplitRecord record) {
        if (record != null) {
            record.setCompanyRecord(this);
            record.setSplit(false);
            this.fusions.add(record);
        }
    }

    public void mergeFusion(FusionSplitRecord otherRecord) {
        long otherOrganizationId = otherRecord.getOrganizationUnitNumber();
        for (FusionSplitRecord ourRecord : this.fusions) {
            Long ourOrganizationId = ourRecord.getOrganizationUnitNumber();
            if (otherOrganizationId == ourOrganizationId) {
                ourRecord.merge(otherRecord);
                return;
            }
        }
        this.addFusion(otherRecord);
    }


    public static final String DB_FIELD_SPLITS = "splits";
    public static final String IO_FIELD_SPLITS = "spaltninger";
    public static final String CLAUSE_SPLITS = FusionSplitRecord.DB_FIELD_SPLIT + "=true";

    @OneToMany(targetEntity = FusionSplitRecord.class, mappedBy = FusionSplitRecord.DB_FIELD_COMPANY, cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Where(clause = CLAUSE_SPLITS)
    @JsonProperty(value = IO_FIELD_SPLITS)
    private Set<FusionSplitRecord> splits = new HashSet<>();

    public RecordSet<FusionSplitRecord, CompanyRecord> getSplits() {
        return new RecordSet<>(this.splits, FusionSplitRecord.class, this, FusionSplitRecord.DB_FIELD_COMPANY, CLAUSE_SPLITS);
    }

    public void setSplits(Set<FusionSplitRecord> splits) {
        this.splits = (splits == null) ? new HashSet<>() : new HashSet<>(splits);
        for (FusionSplitRecord fusionSplitRecord : this.splits) {
            fusionSplitRecord.setCompanyRecord(this);
            fusionSplitRecord.setSplit(true);
        }
    }

    public void addSplit(FusionSplitRecord record) {
        if (record != null) {
            record.setCompanyRecord(this);
            record.setSplit(true);
            this.fusions.add(record);
        }
    }

    public void mergeSplit(FusionSplitRecord otherRecord) {
        long otherOrganizationId = otherRecord.getOrganizationUnitNumber();
        for (FusionSplitRecord ourRecord : this.splits) {
            Long ourOrganizationId = ourRecord.getOrganizationUnitNumber();
            if (otherOrganizationId == ourOrganizationId) {
                ourRecord.merge(otherRecord);
                return;
            }
        }
        this.addSplit(otherRecord);
    }


    public static final String DB_FIELD_META = "metadata";
    public static final String IO_FIELD_META = "virksomhedMetadata";

    @OneToOne(targetEntity = CompanyMetadataRecord.class, mappedBy = CompanyMetadataRecord.DB_FIELD_COMPANY, cascade = CascadeType.ALL)
    @JoinColumn(name = DB_FIELD_META + DatabaseEntry.REF)
    @JsonProperty(value = IO_FIELD_META)
    private CompanyMetadataRecord metadata;

    public void setMetadata(CompanyMetadataRecord metadata) {
        this.metadata = metadata;
        if (this.metadata != null) {
            this.metadata.setCompanyRecord(this);
        }
    }

    public CompanyMetadataRecord getMetadata() {
        return this.metadata;
    }


    public static final String DB_FIELD_DATAEVENT = "dataevent";
    public static final String IO_FIELD_DATAEVENT = "dataevent";

    @OneToMany(targetEntity = CompanyDataEventRecord.class, mappedBy = CompanyDataEventRecord.DB_FIELD_COMPANY, cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonProperty(value = IO_FIELD_DATAEVENT)
    private Set<CompanyDataEventRecord> dataevent = new HashSet<>();

    public void setDataEventRecord(Set<CompanyDataEventRecord> dataevent) {
        this.dataevent = (dataevent == null) ? new HashSet<>() : new HashSet<>(dataevent);
        for (CompanyDataEventRecord record : this.dataevent) {
            record.setCompanyRecord(this);
        }
    }

    public void addDataEventRecord(CompanyDataEventRecord record) {
        if (record != null) {
            record.setCompanyRecord(this);
            this.dataevent.add(record);
        }
    }

    public Set<CompanyDataEventRecord> getDataevent() {
        return this.dataevent;
    }

    @JsonIgnore
    @Override
    public List<CvrRecord> getAll() {
        ArrayList<CvrRecord> list = new ArrayList<>();
        if (this.regNumber != null) {
            list.addAll(this.regNumber);
        }
        if (this.names != null) {
            list.addAll(this.names);
        }
        if (this.locationAddress != null) {
            list.addAll(this.locationAddress);
        }
        if (this.postalAddress != null) {
            list.addAll(this.postalAddress);
        }
        if (this.phoneNumber != null) {
            list.addAll(this.phoneNumber);
        }
        if (this.faxNumber != null) {
            list.addAll(this.faxNumber);
        }
        if (this.emailAddress != null) {
            list.addAll(this.emailAddress);
        }
        if (this.homepage != null) {
            list.addAll(this.homepage);
        }
        if (this.mandatoryEmailAddress != null) {
            list.addAll(this.mandatoryEmailAddress);
        }
        if (this.lifecycle != null) {
            list.addAll(this.lifecycle);
        }
        if (this.primaryIndustry != null) {
            list.addAll(this.primaryIndustry);
        }
        if (this.secondaryIndustry1 != null) {
            list.addAll(this.secondaryIndustry1);
        }
        if (this.secondaryIndustry2 != null) {
            list.addAll(this.secondaryIndustry2);
        }
        if (this.secondaryIndustry3 != null) {
            list.addAll(this.secondaryIndustry3);
        }
        if (this.companyStatus != null) {
            list.addAll(this.companyStatus);
        }
        if (this.companyForm != null) {
            list.addAll(this.companyForm);
        }
        if (this.yearlyNumbers != null) {
            list.addAll(this.yearlyNumbers);
        }
        if (this.quarterlyNumbers != null) {
            list.addAll(this.quarterlyNumbers);
        }
        if (this.monthlyNumbers != null) {
            list.addAll(this.monthlyNumbers);
        }
        if (this.attributes != null) {
            for (AttributeRecord attributeRecord : this.attributes) {
                list.addAll(attributeRecord.getValues());
            }
        }
        if (this.productionUnits != null && !this.productionUnits.isEmpty()) {
            list.addAll(this.productionUnits);
        }
        if (this.participants != null) {
            list.addAll(this.participants);
        }
        if (this.metadata != null) {
            list.addAll(this.metadata.extractRecords(this, true));
        }
        return list;
    }

    @Override
    public UUID generateUUID() {
        return CompanyRecord.generateUUID(this.cvrNumber);
    }

    public static UUID generateUUID(int cvrNumber) {
        String uuidInput = "company:" + cvrNumber;
        return UUID.nameUUIDFromBytes(uuidInput.getBytes());
    }

    @Override
    public void save(Session session) {
        for (AddressRecord address : this.locationAddress) {
            address.wire(session);
        }
        for (AddressRecord address : this.postalAddress) {
            address.wire(session);
        }
        for (FormRecord form : this.companyForm) {
            form.wire(session);
        }
        for (CompanyParticipantRelationRecord participant : this.participants) {
            participant.wire(session);
        }
        if (this.metadata != null) {
            this.metadata.wire(session);
        }
        super.save(session);
    }

    @Override
    public boolean merge(CvrEntityRecord other) {
        if (other != null && !Objects.equals(this.getId(), other.getId()) && other instanceof CompanyRecord) {
            CompanyRecord otherRecord = (CompanyRecord) other;
            for (CompanyRegNumberRecord regNumberRecord : otherRecord.getRegNumber()) {
                this.addRegNumber(regNumberRecord);
            }
            for (SecNameRecord nameRecord : otherRecord.getNames()) {
                this.addName(nameRecord);
            }
            for (SecNameRecord nameRecord : otherRecord.getSecondaryNames()) {
                this.addSecondaryName(nameRecord);
            }
            for (AddressRecord addressRecord : otherRecord.getLocationAddress()) {
                this.addLocationAddress(addressRecord);
            }
            for (AddressRecord addressRecord : otherRecord.getPostalAddress()) {
                this.addPostalAddress(addressRecord);
            }
            for (ContactRecord contactRecord : otherRecord.getPhoneNumber()) {
                this.addPhoneNumber(contactRecord);
            }
            for (ContactRecord contactRecord : otherRecord.getSecondaryPhoneNumber()) {
                this.addSecondaryPhoneNumber(contactRecord);
            }
            for (ContactRecord contactRecord : otherRecord.getFaxNumber()) {
                this.addFaxNumber(contactRecord);
            }
            for (ContactRecord contactRecord : otherRecord.getSecondaryFaxNumber()) {
                this.addSecondaryFaxNumber(contactRecord);
            }
            for (ContactRecord contactRecord : otherRecord.getEmailAddress()) {
                this.addEmailAddress(contactRecord);
            }
            for (ContactRecord contactRecord : otherRecord.getHomepage()) {
                this.addHomepage(contactRecord);
            }
            for (ContactRecord contactRecord : otherRecord.getMandatoryEmailAddress()) {
                this.addMandatoryEmailAddress(contactRecord);
            }
            for (LifecycleRecord lifecycleRecord : otherRecord.getLifecycle()) {
                this.addLifecycle(lifecycleRecord);
            }
            for (CompanyIndustryRecord companyIndustryRecord : otherRecord.getPrimaryIndustry()) {
                this.addPrimaryIndustry(companyIndustryRecord);
            }
            for (CompanyIndustryRecord companyIndustryRecord : otherRecord.getSecondaryIndustry1()) {
                this.addSecondaryIndustry1(companyIndustryRecord);
            }
            for (CompanyIndustryRecord companyIndustryRecord : otherRecord.getSecondaryIndustry2()) {
                this.addSecondaryIndustry2(companyIndustryRecord);
            }
            for (CompanyIndustryRecord companyIndustryRecord : otherRecord.getSecondaryIndustry3()) {
                this.addSecondaryIndustry3(companyIndustryRecord);
            }
            for (StatusRecord statusRecord : otherRecord.getStatus()) {
                this.addStatus(statusRecord);
            }
            for (CompanyStatusRecord statusRecord : otherRecord.getCompanyStatus()) {
                this.addCompanyStatus(statusRecord);
            }
            for (FormRecord formRecord : otherRecord.getCompanyForm()) {
                this.addCompanyForm(formRecord);
            }
            for (CompanyYearlyNumbersRecord yearlyNumbersRecord : otherRecord.getYearlyNumbers()) {
                this.addYearlyNumbers(yearlyNumbersRecord);
            }
            for (CompanyQuarterlyNumbersRecord quarterlyNumbersRecord : otherRecord.getQuarterlyNumbers()) {
                this.addQuarterlyNumbers(quarterlyNumbersRecord);
            }
            for (CompanyMonthlyNumbersRecord monthlyNumbersRecord : otherRecord.getMonthlyNumbers()) {
                this.addMonthlyNumbers(monthlyNumbersRecord);
            }
            for (AttributeRecord attributeRecord : otherRecord.getAttributes()) {
                this.mergeAttribute(attributeRecord);
            }
            for (CompanyUnitLinkRecord companyUnitLinkRecord : otherRecord.getProductionUnits()) {
                this.addProductionUnit(companyUnitLinkRecord);
            }
            for (CompanyParticipantRelationRecord participantRelationRecord : otherRecord.getParticipants()) {
                this.mergeParticipant(participantRelationRecord);
            }
            for (FusionSplitRecord fusionSplitRecord : otherRecord.getFusions()) {
                this.mergeFusion(fusionSplitRecord);
            }
            for (FusionSplitRecord fusionSplitRecord : otherRecord.getSplits()) {
                this.mergeSplit(fusionSplitRecord);
            }
            this.metadata.merge(otherRecord.getMetadata());
            return true;
        }
        return false;
    }

    @Override
    public List<CvrRecord> subs() {
        ArrayList<CvrRecord> subs = new ArrayList<>(super.subs());
        subs.addAll(this.names);
        subs.addAll(this.secondaryNames);
        subs.addAll(this.locationAddress);
        subs.addAll(this.postalAddress);
        subs.addAll(this.phoneNumber);
        subs.addAll(this.secondaryPhoneNumber);
        subs.addAll(this.faxNumber);
        subs.addAll(this.secondaryFaxNumber);
        subs.addAll(this.emailAddress);
        subs.addAll(this.homepage);
        subs.addAll(this.mandatoryEmailAddress);
        subs.addAll(this.lifecycle);
        subs.addAll(this.primaryIndustry);
        subs.addAll(this.secondaryIndustry1);
        subs.addAll(this.secondaryIndustry2);
        subs.addAll(this.secondaryIndustry3);
        subs.addAll(this.status);
        subs.addAll(this.companyStatus);
        subs.addAll(this.companyForm);
        subs.addAll(this.yearlyNumbers);
        subs.addAll(this.quarterlyNumbers);
        subs.addAll(this.monthlyNumbers);
        subs.addAll(this.attributes);
        subs.addAll(this.productionUnits);
        subs.addAll(this.participants);
        subs.addAll(this.fusions);
        subs.addAll(this.splits);
        if (this.metadata != null) {
            subs.add(this.metadata);
        }
        return subs;
    }

    public List<BaseQuery> getAssoc() {
        ArrayList<BaseQuery> queries = new ArrayList<>();
        queries.addAll(this.locationAddress.stream().map(a -> a.getAssoc()).flatMap(x -> x.stream()).collect(Collectors.toList()));
        return queries;
    }

    @Override
    public void traverse(Consumer<RecordSet<? extends CvrRecord, ? extends CvrRecord>> setCallback, Consumer<CvrRecord> itemCallback, boolean grouped) {
        super.traverse(setCallback, itemCallback, grouped);
        this.getNames().traverse(setCallback, itemCallback);
        this.getSecondaryNames().traverse(setCallback, itemCallback);
        this.getLocationAddress().traverse(setCallback, itemCallback);
        this.getPostalAddress().traverse(setCallback, itemCallback);
        this.getPhoneNumber().traverse(setCallback, itemCallback);
        this.getSecondaryPhoneNumber().traverse(setCallback, itemCallback);
        this.getFaxNumber().traverse(setCallback, itemCallback);
        this.getSecondaryFaxNumber().traverse(setCallback, itemCallback);
        this.getEmailAddress().traverse(setCallback, itemCallback);
        this.getMandatoryEmailAddress().traverse(setCallback, itemCallback);
        this.getHomepage().traverse(setCallback, itemCallback);
        this.getLifecycle().traverse(setCallback, itemCallback);
        this.getPrimaryIndustry().traverse(setCallback, itemCallback);
        this.getSecondaryIndustry1().traverse(setCallback, itemCallback);
        this.getSecondaryIndustry2().traverse(setCallback, itemCallback);
        this.getSecondaryIndustry3().traverse(setCallback, itemCallback);
        this.getStatus().traverse(setCallback, itemCallback);
        this.getCompanyStatus().traverse(setCallback, itemCallback);
        this.getCompanyForm().traverse(setCallback, itemCallback);
        if (grouped) {
            this.getYearlyNumbersGrouped().traverse(setCallback, itemCallback);
            this.getQuarterlyNumbersGrouped().traverse(setCallback, itemCallback);
            this.getMonthlyNumbersGrouped().traverse(setCallback, itemCallback);
        } else {
            this.getYearlyNumbers().traverse(setCallback, itemCallback);
            this.getQuarterlyNumbers().traverse(setCallback, itemCallback);
            this.getMonthlyNumbers().traverse(setCallback, itemCallback);
        }
        this.getAttributes().traverse(setCallback, itemCallback);
        this.getProductionUnits().traverse(setCallback, itemCallback);
        this.getParticipants().traverse(setCallback, itemCallback);
        this.getFusions().traverse(setCallback, itemCallback);
        this.getSplits().traverse(setCallback, itemCallback);
        this.getMetadata().traverse(setCallback, itemCallback);
    }

}
