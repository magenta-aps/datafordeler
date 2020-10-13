package dk.magenta.datafordeler.cvr.records;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import dk.magenta.datafordeler.core.database.*;
import dk.magenta.datafordeler.core.fapi.BaseQuery;
import dk.magenta.datafordeler.cvr.BitemporalSet;
import dk.magenta.datafordeler.cvr.CvrPlugin;
import dk.magenta.datafordeler.cvr.service.CompanyRecordService;
import org.hibernate.Session;
import org.hibernate.annotations.*;

import javax.persistence.*;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Index;
import javax.persistence.Table;
import java.time.OffsetDateTime;
import java.util.*;
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
@FilterDefs({
        @FilterDef(name = Bitemporal.FILTER_EFFECTFROM_AFTER, parameters = @ParamDef(name = Bitemporal.FILTERPARAM_EFFECTFROM_AFTER, type = CvrBitemporalRecord.FILTERPARAMTYPE_EFFECTFROM)),
        @FilterDef(name = Bitemporal.FILTER_EFFECTFROM_BEFORE, parameters = @ParamDef(name = Bitemporal.FILTERPARAM_EFFECTFROM_BEFORE, type = CvrBitemporalRecord.FILTERPARAMTYPE_EFFECTFROM)),
        @FilterDef(name = Bitemporal.FILTER_EFFECTTO_AFTER, parameters = @ParamDef(name = Bitemporal.FILTERPARAM_EFFECTTO_AFTER, type = CvrBitemporalRecord.FILTERPARAMTYPE_EFFECTTO)),
        @FilterDef(name = Bitemporal.FILTER_EFFECTTO_BEFORE, parameters = @ParamDef(name = Bitemporal.FILTERPARAM_EFFECTTO_BEFORE, type = CvrBitemporalRecord.FILTERPARAMTYPE_EFFECTTO)),
        @FilterDef(name = Monotemporal.FILTER_REGISTRATIONFROM_AFTER, parameters = @ParamDef(name = Monotemporal.FILTERPARAM_REGISTRATIONFROM_AFTER, type = CvrBitemporalRecord.FILTERPARAMTYPE_REGISTRATIONFROM)),
        @FilterDef(name = Monotemporal.FILTER_REGISTRATIONFROM_BEFORE, parameters = @ParamDef(name = Monotemporal.FILTERPARAM_REGISTRATIONFROM_BEFORE, type = CvrBitemporalRecord.FILTERPARAMTYPE_REGISTRATIONFROM)),
        //@FilterDef(name = Monotemporal.FILTER_REGISTRATIONTO_AFTER, parameters = @ParamDef(name = Monotemporal.FILTERPARAM_REGISTRATIONTO_AFTER, type = CvrBitemporalRecord.FILTERPARAMTYPE_REGISTRATIONTO)),
        //@FilterDef(name = Monotemporal.FILTER_REGISTRATIONTO_BEFORE, parameters = @ParamDef(name = Monotemporal.FILTERPARAM_REGISTRATIONTO_BEFORE, type = CvrBitemporalRecord.FILTERPARAMTYPE_REGISTRATIONTO)),
        @FilterDef(name = Nontemporal.FILTER_LASTUPDATED_AFTER, parameters = @ParamDef(name = Nontemporal.FILTERPARAM_LASTUPDATED_AFTER, type = CvrBitemporalRecord.FILTERPARAMTYPE_LASTUPDATED)),
        @FilterDef(name = Nontemporal.FILTER_LASTUPDATED_BEFORE, parameters = @ParamDef(name = Nontemporal.FILTERPARAM_LASTUPDATED_BEFORE, type = CvrBitemporalRecord.FILTERPARAMTYPE_LASTUPDATED))
})
@JsonIgnoreProperties(ignoreUnknown = true)
public class CompanyRecord extends CvrEntityRecord {

    public static final String TABLE_NAME = "cvr_record_company";

    public static final String schema = "virksomhed";

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
            this.regNumber.add(record);
        }
    }

    public BitemporalSet<CompanyRegNumberRecord> getRegNumber() {
        return new BitemporalSet<>(this.regNumber);
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

    @OneToMany(targetEntity = SecNameRecord.class, mappedBy = SecNameRecord.DB_FIELD_COMPANY, cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Where(clause = SecNameRecord.DB_FIELD_SECONDARY + "=false")
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

    public BitemporalSet<SecNameRecord> getNames() {
        return new BitemporalSet<>(this.names);
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
            this.names.add(record);
        }
    }


    public static final String DB_FIELD_SECONDARY_NAMES = "secondaryNames";
    public static final String IO_FIELD_SECONDARY_NAMES = "binavne";

    @OneToMany(targetEntity = SecNameRecord.class, mappedBy = SecNameRecord.DB_FIELD_COMPANY, cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Where(clause = SecNameRecord.DB_FIELD_SECONDARY + "=true")
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

    public BitemporalSet<SecNameRecord> getSecondaryNames() {
        return new BitemporalSet<>(this.secondaryNames);
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
            this.secondaryNames.add(record);
        }
    }


    public static final String DB_FIELD_LOCATION_ADDRESS = "locationAddress";
    public static final String IO_FIELD_LOCATION_ADDRESS = "beliggenhedsadresse";

    @OneToMany(targetEntity = AddressRecord.class, mappedBy = AddressRecord.DB_FIELD_COMPANY, cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Where(clause = AddressRecord.DB_FIELD_TYPE + "=" + AddressRecord.TYPE_LOCATION)
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
            this.locationAddress.add(record);
        }
    }

    public BitemporalSet<AddressRecord> getLocationAddress() {
        return new BitemporalSet<>(this.locationAddress);
    }


    public static final String DB_FIELD_POSTAL_ADDRESS = "postalAddress";
    public static final String IO_FIELD_POSTAL_ADDRESS = "postadresse";

    @OneToMany(targetEntity = AddressRecord.class, mappedBy = AddressRecord.DB_FIELD_COMPANY, cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Where(clause = AddressRecord.DB_FIELD_TYPE + "=" + AddressRecord.TYPE_POSTAL)
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
            this.postalAddress.add(record);
        }
    }

    public BitemporalSet<AddressRecord> getPostalAddress() {
        return new BitemporalSet<>(this.postalAddress);
    }


    public static final String DB_FIELD_PHONE = "phoneNumber";
    public static final String IO_FIELD_PHONE = "telefonNummer";

    @OneToMany(targetEntity = ContactRecord.class, mappedBy = ContactRecord.DB_FIELD_COMPANY, cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Where(clause = ContactRecord.DB_FIELD_TYPE + "=" + ContactRecord.TYPE_TELEFONNUMMER + " AND " + ContactRecord.DB_FIELD_SECONDARY + "=false")
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
        if (record != null) {
            record.setType(ContactRecord.TYPE_TELEFONNUMMER);
            record.setCompanyRecord(this);
            record.setSecondary(false);
            this.phoneNumber.add(record);
        }
    }

    public BitemporalSet<ContactRecord> getPhoneNumber() {
        return new BitemporalSet<>(this.phoneNumber);
    }


    public static final String DB_FIELD_PHONE_SECONDARY = "secondaryPhoneNumber";
    public static final String IO_FIELD_PHONE_SECONDARY = "sekundaertTelefonNummer";

    @OneToMany(targetEntity = ContactRecord.class, mappedBy = ContactRecord.DB_FIELD_COMPANY, cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Where(clause = ContactRecord.DB_FIELD_TYPE + "=" + ContactRecord.TYPE_TELEFONNUMMER + " AND " + ContactRecord.DB_FIELD_SECONDARY + "=true")
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
        if (record != null) {
            record.setType(ContactRecord.TYPE_TELEFONNUMMER);
            record.setCompanyRecord(this);
            record.setSecondary(true);
            this.phoneNumber.add(record);
        }
    }

    public BitemporalSet<ContactRecord> getSecondaryPhoneNumber() {
        return new BitemporalSet<>(this.secondaryPhoneNumber);
    }


    public static final String DB_FIELD_FAX = "faxNumber";
    public static final String IO_FIELD_FAX = "telefaxNummer";

    @OneToMany(targetEntity = ContactRecord.class, mappedBy = ContactRecord.DB_FIELD_COMPANY, cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Where(clause = ContactRecord.DB_FIELD_TYPE + "=" + ContactRecord.TYPE_TELEFAXNUMMER)
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
        if (record != null) {
            record.setType(ContactRecord.TYPE_TELEFAXNUMMER);
            record.setCompanyRecord(this);
            record.setSecondary(false);
            this.faxNumber.add(record);
        }
    }

    public BitemporalSet<ContactRecord> getFaxNumber() {
        return new BitemporalSet<>(this.faxNumber);
    }


    public static final String DB_FIELD_FAX_SECONDARY = "secondaryFaxNumber";
    public static final String IO_FIELD_FAX_SECONDARY = "sekundaertTelefaxNummer";

    @OneToMany(targetEntity = ContactRecord.class, mappedBy = ContactRecord.DB_FIELD_COMPANY, cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Where(clause = ContactRecord.DB_FIELD_TYPE + "=" + ContactRecord.TYPE_TELEFAXNUMMER + " AND " + ContactRecord.DB_FIELD_SECONDARY + "=true")
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
        if (record != null) {
            record.setType(ContactRecord.TYPE_TELEFAXNUMMER);
            record.setCompanyRecord(this);
            record.setSecondary(true);
            this.faxNumber.add(record);
        }
    }

    public BitemporalSet<ContactRecord> getSecondaryFaxNumber() {
        return new BitemporalSet<>(this.secondaryFaxNumber);
    }


    public static final String DB_FIELD_EMAIL = "emailAddress";
    public static final String IO_FIELD_EMAIL = "elektroniskPost";

    @OneToMany(targetEntity = ContactRecord.class, mappedBy = ContactRecord.DB_FIELD_COMPANY, cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Where(clause = ContactRecord.DB_FIELD_TYPE + "=" + ContactRecord.TYPE_EMAILADRESSE)
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
        if (record != null) {
            record.setType(ContactRecord.TYPE_EMAILADRESSE);
            record.setCompanyRecord(this);
            this.emailAddress.add(record);
        }
    }

    public BitemporalSet<ContactRecord> getEmailAddress() {
        return new BitemporalSet<>(this.emailAddress);
    }


    public static final String DB_FIELD_HOMEPAGE = "homepage";
    public static final String IO_FIELD_HOMEPAGE = "hjemmeside";

    @OneToMany(targetEntity = ContactRecord.class, mappedBy = ContactRecord.DB_FIELD_COMPANY, cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Where(clause = ContactRecord.DB_FIELD_TYPE + "=" + ContactRecord.TYPE_HJEMMESIDE)
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
        if (record != null) {
            record.setType(ContactRecord.TYPE_HJEMMESIDE);
            record.setCompanyRecord(this);
            this.homepage.add(record);
        }
    }

    public BitemporalSet<ContactRecord> getHomepage() {
        return new BitemporalSet<>(this.homepage);
    }


    public static final String DB_FIELD_MANDATORY_EMAIL = "mandatoryEmailAddress";
    public static final String IO_FIELD_MANDATORY_EMAIL = "obligatoriskEmail";

    @OneToMany(targetEntity = ContactRecord.class, mappedBy = ContactRecord.DB_FIELD_COMPANY, cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Where(clause = ContactRecord.DB_FIELD_TYPE + "=" + ContactRecord.TYPE_OBLIGATORISK_EMAILADRESSE)
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
        if (record != null) {
            record.setType(ContactRecord.TYPE_OBLIGATORISK_EMAILADRESSE);
            record.setCompanyRecord(this);
            this.mandatoryEmailAddress.add(record);
        }
    }

    public BitemporalSet<ContactRecord> getMandatoryEmailAddress() {
        return new BitemporalSet<>(this.mandatoryEmailAddress);
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
            this.lifecycle.add(record);
        }
    }

    public BitemporalSet<LifecycleRecord> getLifecycle() {
        return new BitemporalSet<>(this.lifecycle);
    }


    public static final String DB_FIELD_PRIMARY_INDUSTRY = "primaryIndustry";
    public static final String IO_FIELD_PRIMARY_INDUSTRY = "hovedbranche";

    @OneToMany(targetEntity = CompanyIndustryRecord.class, mappedBy = CompanyIndustryRecord.DB_FIELD_COMPANY, cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Where(clause = CompanyIndustryRecord.DB_FIELD_INDEX + "=0")
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
            this.primaryIndustry.add(record);
        }
    }

    public BitemporalSet<CompanyIndustryRecord> getPrimaryIndustry() {
        return new BitemporalSet<>(this.primaryIndustry);
    }


    public static final String DB_FIELD_SECONDARY_INDUSTRY1 = "secondaryIndustry1";
    public static final String IO_FIELD_SECONDARY_INDUSTRY1 = "bibranche1";

    @OneToMany(targetEntity = CompanyIndustryRecord.class, mappedBy = CompanyIndustryRecord.DB_FIELD_COMPANY, cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Where(clause = CompanyIndustryRecord.DB_FIELD_INDEX + "=1")
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
            this.secondaryIndustry1.add(record);
        }
    }

    public BitemporalSet<CompanyIndustryRecord> getSecondaryIndustry1() {
        return new BitemporalSet<>(this.secondaryIndustry1);
    }


    public static final String DB_FIELD_SECONDARY_INDUSTRY2 = "secondaryIndustry2";
    public static final String IO_FIELD_SECONDARY_INDUSTRY2 = "bibranche2";

    @OneToMany(targetEntity = CompanyIndustryRecord.class, mappedBy = CompanyIndustryRecord.DB_FIELD_COMPANY, cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Where(clause = CompanyIndustryRecord.DB_FIELD_INDEX + "=2")
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
            this.secondaryIndustry2.add(record);
        }
    }

    public BitemporalSet<CompanyIndustryRecord> getSecondaryIndustry2() {
        return new BitemporalSet<>(this.secondaryIndustry2);
    }


    public static final String DB_FIELD_SECONDARY_INDUSTRY3 = "secondaryIndustry3";
    public static final String IO_FIELD_SECONDARY_INDUSTRY3 = "bibranche3";

    @OneToMany(targetEntity = CompanyIndustryRecord.class, mappedBy = CompanyIndustryRecord.DB_FIELD_COMPANY, cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Where(clause = CompanyIndustryRecord.DB_FIELD_INDEX + "=3")
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
            this.secondaryIndustry3.add(record);
        }
    }

    public BitemporalSet<CompanyIndustryRecord> getSecondaryIndustry3() {
        return new BitemporalSet<>(this.secondaryIndustry3);
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
            this.status.add(record);
        }
    }

    public BitemporalSet<StatusRecord> getStatus() {
        return new BitemporalSet<>(this.status);
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
            this.companyStatus.add(record);
        }
    }

    public BitemporalSet<CompanyStatusRecord> getCompanyStatus() {
        return new BitemporalSet<>(this.companyStatus);
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
            this.companyForm.add(record);
        }
    }

    public BitemporalSet<FormRecord> getCompanyForm() {
        return new BitemporalSet<>(this.companyForm);
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

    public BitemporalSet<CompanyYearlyNumbersRecord> getYearlyNumbers() {
        return new BitemporalSet<>(this.yearlyNumbers);
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

    public BitemporalSet<CompanyQuarterlyNumbersRecord> getQuarterlyNumbers() {
        return new BitemporalSet<>(this.quarterlyNumbers);
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

    public BitemporalSet<CompanyMonthlyNumbersRecord> getMonthlyNumbers() {
        return new BitemporalSet<>(this.monthlyNumbers);
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

    public Set<AttributeRecord> getAttributes() {
        return this.attributes;
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
            this.productionUnits.add(record);
        }
    }

    public BitemporalSet<CompanyUnitLinkRecord> getProductionUnits() {
        return new BitemporalSet<>(this.productionUnits);
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

    public BitemporalSet<CompanyParticipantRelationRecord> getParticipants() {
        return new BitemporalSet<>(this.participants);
    }


    public static final String DB_FIELD_FUSIONS = "fusions";
    public static final String IO_FIELD_FUSIONS = "fusioner";

    @OneToMany(targetEntity = FusionSplitRecord.class, mappedBy = FusionSplitRecord.DB_FIELD_COMPANY, cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Where(clause = FusionSplitRecord.DB_FIELD_SPLIT + "=false")
    @JsonProperty(value = IO_FIELD_FUSIONS)
    private Set<FusionSplitRecord> fusions = new HashSet<>();

    public Set<FusionSplitRecord> getFusions() {
        return this.fusions;
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

    @OneToMany(targetEntity = FusionSplitRecord.class, mappedBy = FusionSplitRecord.DB_FIELD_COMPANY, cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Where(clause = FusionSplitRecord.DB_FIELD_SPLIT + "=true")
    @JsonProperty(value = IO_FIELD_SPLITS)
    private Set<FusionSplitRecord> splits = new HashSet<>();

    public Set<FusionSplitRecord> getSplits() {
        return this.splits;
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
        String uuidInput = "company:"+cvrNumber;
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
                //this.addParticipant(participantRelationRecord);
                this.mergeParticipant(participantRelationRecord);
            }
            for (FusionSplitRecord fusionSplitRecord : otherRecord.getFusions()) {
                //this.addFusion(fusionSplitRecord);
                this.mergeFusion(fusionSplitRecord);
            }
            for (FusionSplitRecord fusionSplitRecord : otherRecord.getSplits()) {
                //this.addSplit(fusionSplitRecord);
                this.mergeSplit(fusionSplitRecord);
            }

            this.addDataEventRecord(new CompanyDataEventRecord(otherRecord.getRegistrationFrom(), otherRecord.getFieldName(), otherRecord.getId(), otherRecord.getId()));
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

}
