package dk.magenta.datafordeler.cvr.records;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import dk.magenta.datafordeler.core.database.Bitemporal;
import dk.magenta.datafordeler.core.database.DatabaseEntry;
import dk.magenta.datafordeler.core.database.Monotemporal;
import dk.magenta.datafordeler.core.database.Nontemporal;
import dk.magenta.datafordeler.cvr.BitemporalSet;
import dk.magenta.datafordeler.cvr.CvrPlugin;
import dk.magenta.datafordeler.cvr.RecordSet;
import dk.magenta.datafordeler.cvr.service.CompanyUnitRecordService;
import org.hibernate.Session;
import org.hibernate.annotations.*;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Index;
import javax.persistence.Table;
import javax.persistence.*;
import java.util.*;
import java.util.function.Consumer;

/**
 * Base record for CompanyUnit data, parsed from JSON into a tree of objects
 * with this class at the base.
 */
@Entity
@Table(name = CvrPlugin.DEBUG_TABLE_PREFIX + CompanyUnitRecord.TABLE_NAME, indexes = {
        @Index(name = CvrPlugin.DEBUG_TABLE_PREFIX + CompanyUnitRecord.TABLE_NAME + "__pnumber", columnList = CompanyUnitRecord.DB_FIELD_P_NUMBER, unique = true),
        @Index(name = CvrPlugin.DEBUG_TABLE_PREFIX + CompanyUnitRecord.TABLE_NAME + "__unitnumber", columnList = CompanyUnitRecord.DB_FIELD_UNITNUMBER, unique = true),
        @Index(name = CvrPlugin.DEBUG_TABLE_PREFIX + CompanyUnitRecord.TABLE_NAME + "__" + CompanyUnitRecord.DB_FIELD_DAFO_UPDATED, columnList = CompanyUnitRecord.DB_FIELD_DAFO_UPDATED)
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
public class CompanyUnitRecord extends CvrEntityRecord {

    public static final String TABLE_NAME = "cvr_record_unit";

    public static final String schema = "produktionsenhed";

    @JsonIgnore
    public String getFieldName() {
        return TABLE_NAME;
    }

    @Override
    @JsonIgnore
    protected String getDomain() {
        return CompanyUnitRecordService.getDomain();
    }

    public static final String DB_FIELD_P_NUMBER = "pNumber";
    public static final String IO_FIELD_P_NUMBER = "pNummer";

    @Column(name = DB_FIELD_P_NUMBER)
    @JsonProperty(value = IO_FIELD_P_NUMBER)
    private int pNumber;

    public int getpNumber() {
        return this.pNumber;
    }

    @JsonIgnore
    public Map<String, Object> getIdentifyingFilter() {
        return Collections.singletonMap(DB_FIELD_P_NUMBER, this.pNumber);
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

    @OneToMany(mappedBy = SecNameRecord.DB_FIELD_COMPANYUNIT, targetEntity = SecNameRecord.class, cascade = CascadeType.ALL)
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
            record.setCompanyUnitRecord(this);
        }
    }

    public void addName(SecNameRecord record) {
        if (!this.names.contains(record)) {
            record.setSecondary(false);
            record.setCompanyUnitRecord(this);
            this.names.add(record);
        }
    }


    public static final String DB_FIELD_LOCATION_ADDRESS = "locationAddress";
    public static final String IO_FIELD_LOCATION_ADDRESS = "beliggenhedsadresse";

    @OneToMany(mappedBy = AddressRecord.DB_FIELD_COMPANYUNIT, targetEntity = AddressRecord.class, cascade = CascadeType.ALL)
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
            record.setCompanyUnitRecord(this);
        }
    }

    public void addLocationAddress(AddressRecord record) {
        if (!this.locationAddress.contains(record)) {
            record.setType(AddressRecord.TYPE_LOCATION);
            record.setCompanyUnitRecord(this);
            this.locationAddress.add(record);
        }
    }

    public BitemporalSet<AddressRecord> getLocationAddress() {
        return new BitemporalSet<>(this.locationAddress);
    }


    public static final String DB_FIELD_POSTAL_ADDRESS = "postalAddress";
    public static final String IO_FIELD_POSTAL_ADDRESS = "postadresse";

    @OneToMany(mappedBy = AddressRecord.DB_FIELD_COMPANYUNIT, targetEntity = AddressRecord.class, cascade = CascadeType.ALL)
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
            record.setCompanyUnitRecord(this);
        }

    }

    public void addPostalAddress(AddressRecord record) {
        if (!this.postalAddress.contains(record)) {
            record.setType(AddressRecord.TYPE_POSTAL);
            record.setCompanyUnitRecord(this);
            this.postalAddress.add(record);
        }
    }

    public BitemporalSet<AddressRecord> getPostalAddress() {
        return new BitemporalSet<>(this.postalAddress);
    }


    public static final String DB_FIELD_PHONE = "phoneNumber";
    public static final String IO_FIELD_PHONE = "telefonNummer";

    @OneToMany(mappedBy = ContactRecord.DB_FIELD_COMPANYUNIT, targetEntity = ContactRecord.class, cascade = CascadeType.ALL)
    @Where(clause = ContactRecord.DB_FIELD_TYPE + "=" + ContactRecord.TYPE_TELEFONNUMMER)
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
            record.setCompanyUnitRecord(this);
        }
    }

    public void addPhoneNumber(ContactRecord record) {
        if (!this.phoneNumber.contains(record)) {
            record.setType(ContactRecord.TYPE_TELEFONNUMMER);
            record.setCompanyUnitRecord(this);
            record.setSecondary(false);
            this.phoneNumber.add(record);
        }
    }

    public BitemporalSet<ContactRecord> getPhoneNumber() {
        return new BitemporalSet<>(this.phoneNumber);
    }


    public static final String DB_FIELD_FAX = "faxNumber";
    public static final String IO_FIELD_FAX = "telefaxNummer";

    @OneToMany(mappedBy = ContactRecord.DB_FIELD_COMPANYUNIT, targetEntity = ContactRecord.class, cascade = CascadeType.ALL)
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
            record.setCompanyUnitRecord(this);
        }
    }

    public void addFaxNumber(ContactRecord record) {
        if (!this.faxNumber.contains(record)) {
            record.setType(ContactRecord.TYPE_TELEFAXNUMMER);
            record.setCompanyUnitRecord(this);
            record.setSecondary(false);
            this.faxNumber.add(record);
        }
    }

    public BitemporalSet<ContactRecord> getFaxNumber() {
        return new BitemporalSet<>(this.faxNumber);
    }


    public static final String DB_FIELD_EMAIL = "emailAddress";
    public static final String IO_FIELD_EMAIL = "elektroniskPost";

    @OneToMany(mappedBy = ContactRecord.DB_FIELD_COMPANYUNIT, targetEntity = ContactRecord.class, cascade = CascadeType.ALL)
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
            record.setCompanyUnitRecord(this);
        }
    }

    public void addEmailAddress(ContactRecord record) {
        if (!this.emailAddress.contains(record)) {
            record.setType(ContactRecord.TYPE_EMAILADRESSE);
            record.setCompanyUnitRecord(this);
            this.emailAddress.add(record);
        }
    }

    public BitemporalSet<ContactRecord> getEmailAddress() {
        return new BitemporalSet<>(this.emailAddress);
    }


    public static final String DB_FIELD_LIFECYCLE = "lifecycle";
    public static final String IO_FIELD_LIFECYCLE = "livsforloeb";

    @OneToMany(mappedBy = LifecycleRecord.DB_FIELD_COMPANYUNIT, targetEntity = LifecycleRecord.class, cascade = CascadeType.ALL)
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
            lifecycleRecord.setCompanyUnitRecord(this);
        }
    }

    public void addLifecycle(LifecycleRecord record) {
        if (!this.lifecycle.contains(record)) {
            record.setCompanyUnitRecord(this);
            this.lifecycle.add(record);
        }
    }

    public BitemporalSet<LifecycleRecord> getLifecycle() {
        return new BitemporalSet<>(this.lifecycle);
    }


    public static final String DB_FIELD_PRIMARY_INDUSTRY = "primaryIndustry";
    public static final String IO_FIELD_PRIMARY_INDUSTRY = "hovedbranche";

    @OneToMany(mappedBy = CompanyIndustryRecord.DB_FIELD_COMPANYUNIT, targetEntity = CompanyIndustryRecord.class, cascade = CascadeType.ALL)
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
            record.setCompanyUnitRecord(this);
        }
    }

    public void addPrimaryIndustry(CompanyIndustryRecord record) {
        if (!this.primaryIndustry.contains(record)) {
            record.setIndex(0);
            record.setCompanyUnitRecord(this);
            this.primaryIndustry.add(record);
        }
    }

    public BitemporalSet<CompanyIndustryRecord> getPrimaryIndustry() {
        return new BitemporalSet<>(this.primaryIndustry);
    }


    public static final String DB_FIELD_SECONDARY_INDUSTRY1 = "secondaryIndustry1";
    public static final String IO_FIELD_SECONDARY_INDUSTRY1 = "bibranche1";

    @OneToMany(mappedBy = CompanyIndustryRecord.DB_FIELD_COMPANYUNIT, targetEntity = CompanyIndustryRecord.class, cascade = CascadeType.ALL)
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
            record.setCompanyUnitRecord(this);
        }
    }

    public void addSecondaryIndustry1(CompanyIndustryRecord record) {
        if (!this.secondaryIndustry1.contains(record)) {
            record.setIndex(1);
            record.setCompanyUnitRecord(this);
            this.secondaryIndustry1.add(record);
        }
    }

    public BitemporalSet<CompanyIndustryRecord> getSecondaryIndustry1() {
        return new BitemporalSet<>(this.secondaryIndustry1);
    }


    public static final String DB_FIELD_SECONDARY_INDUSTRY2 = "secondaryIndustry2";
    public static final String IO_FIELD_SECONDARY_INDUSTRY2 = "bibranche2";

    @OneToMany(mappedBy = CompanyIndustryRecord.DB_FIELD_COMPANYUNIT, targetEntity = CompanyIndustryRecord.class, cascade = CascadeType.ALL)
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
            record.setCompanyUnitRecord(this);
        }
    }

    public void addSecondaryIndustry2(CompanyIndustryRecord record) {
        if (!this.secondaryIndustry2.contains(record)) {
            record.setIndex(2);
            record.setCompanyUnitRecord(this);
            this.secondaryIndustry2.add(record);
        }
    }

    public BitemporalSet<CompanyIndustryRecord> getSecondaryIndustry2() {
        return new BitemporalSet<>(this.secondaryIndustry2);
    }


    public static final String DB_FIELD_SECONDARY_INDUSTRY3 = "secondaryIndustry3";
    public static final String IO_FIELD_SECONDARY_INDUSTRY3 = "bibranche3";

    @OneToMany(mappedBy = CompanyIndustryRecord.DB_FIELD_COMPANYUNIT, targetEntity = CompanyIndustryRecord.class, cascade = CascadeType.ALL)
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
            record.setCompanyUnitRecord(this);
        }
    }

    public void addSecondaryIndustry3(CompanyIndustryRecord record) {
        if (!this.secondaryIndustry3.contains(record)) {
            record.setIndex(3);
            record.setCompanyUnitRecord(this);
            this.secondaryIndustry3.add(record);
        }
    }

    public BitemporalSet<CompanyIndustryRecord> getSecondaryIndustry3() {
        return new BitemporalSet<>(this.secondaryIndustry3);
    }


    public static final String DB_FIELD_YEARLY_NUMBERS = "yearlyNumbers";
    public static final String IO_FIELD_YEARLY_NUMBERS = "aarsbeskaeftigelse";

    @OneToMany(mappedBy = CompanyYearlyNumbersRecord.DB_FIELD_COMPANYUNIT, targetEntity = CompanyYearlyNumbersRecord.class, cascade = CascadeType.ALL)
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
            yearlyNumbersRecord.setCompanyUnitRecord(this);
        }
    }

    public void addYearlyNumbers(CompanyYearlyNumbersRecord record) {
        if (!this.yearlyNumbers.contains(record)) {
            record.setCompanyUnitRecord(this);
            this.yearlyNumbers.add(record);
        }
    }

    public BitemporalSet<CompanyYearlyNumbersRecord> getYearlyNumbers() {
        return new BitemporalSet<>(this.yearlyNumbers);
    }


    public static final String DB_FIELD_QUARTERLY_NUMBERS = "quarterlyNumbers";
    public static final String IO_FIELD_QUARTERLY_NUMBERS = "kvartalsbeskaeftigelse";

    @OneToMany(mappedBy = CompanyQuarterlyNumbersRecord.DB_FIELD_COMPANYUNIT, targetEntity = CompanyQuarterlyNumbersRecord.class, cascade = CascadeType.ALL)
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
            quarterlyNumbersRecord.setCompanyUnitRecord(this);
        }
    }

    public void addQuarterlyNumbers(CompanyQuarterlyNumbersRecord record) {
        if (!this.quarterlyNumbers.contains(record)) {
            record.setCompanyUnitRecord(this);
            this.quarterlyNumbers.add(record);
        }
    }

    public BitemporalSet<CompanyQuarterlyNumbersRecord> getQuarterlyNumbers() {
        return new BitemporalSet<>(this.quarterlyNumbers);
    }


    //public static final String DB_FIELD_MONTHLY_NUMBERS = "monthlyNumbers";
    //public static final String IO_FIELD_MONTHLY_NUMBERS = "maanedsbeskaeftigelse";

    //@OneToMany(mappedBy = CompanyMonthlyNumbersRecord.DB_FIELD_COMPANY, cascade = CascadeType.ALL)
    //@JsonProperty(value = IO_FIELD_MONTHLY_NUMBERS)
    //private Set<CompanyMonthlyNumbersRecord> monthlyNumbers;


    public static final String DB_FIELD_ATTRIBUTES = "attributes";
    public static final String IO_FIELD_ATTRIBUTES = "attributter";

    @OneToMany(mappedBy = AttributeRecord.DB_FIELD_COMPANYUNIT, targetEntity = AttributeRecord.class, cascade = CascadeType.ALL)
        @JsonProperty(value = IO_FIELD_ATTRIBUTES)
    private Set<AttributeRecord> attributes = new HashSet<>();

    public void setAttributes(Set<AttributeRecord> attributes) {
        this.attributes = (attributes == null) ? new HashSet<>() : new HashSet<>(attributes);
        for (AttributeRecord attributeRecord : this.attributes) {
            attributeRecord.setCompanyUnitRecord(this);
        }
    }

    public void addAttribute(AttributeRecord record) {
        if (!this.attributes.contains(record)) {
            record.setCompanyUnitRecord(this);
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

    public AttributeRecordSet getAttributes() {
        return new AttributeRecordSet(this.attributes);
    }


    public static final String DB_FIELD_PARTICIPANTS = "participants";
    public static final String IO_FIELD_PARTICIPANTS = "deltagerRelation";

    @OneToMany(mappedBy = CompanyParticipantRelationRecord.DB_FIELD_COMPANYUNIT, targetEntity = CompanyParticipantRelationRecord.class, cascade = CascadeType.ALL)
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
    private Set<CompanyParticipantRelationRecord> participantRelations = new HashSet<>();

    public void setParticipantRelations(Set<CompanyParticipantRelationRecord> participantRelations) {
        this.participantRelations = (participantRelations == null) ? new HashSet<>() : new HashSet<>(participantRelations);
        for (CompanyParticipantRelationRecord participantRelationRecord : this.participantRelations) {
            participantRelationRecord.setCompanyUnitRecord(this);
        }
    }

    public void addParticipant(CompanyParticipantRelationRecord record) {
        if (!this.participantRelations.contains(record)) {
            record.setCompanyUnitRecord(this);
            this.participantRelations.add(record);
        }
    }

    public void mergeParticipant(CompanyParticipantRelationRecord otherRecord) {
        Long otherParticipantId = otherRecord.getParticipantUnitNumber();
        if (otherParticipantId != null) {
            for (CompanyParticipantRelationRecord ourRecord : this.participantRelations) {
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
        return new BitemporalSet<>(this.participantRelations);
    }


    public static final String DB_FIELD_COMPANY_LINK = "companyLinkRecords";
    public static final String IO_FIELD_COMPANY_LINK = "virksomhedsrelation";

    @OneToMany(mappedBy = CompanyLinkRecord.DB_FIELD_COMPANYUNIT, targetEntity = CompanyLinkRecord.class, cascade = CascadeType.ALL)
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
    @JsonProperty(value = IO_FIELD_COMPANY_LINK)
    private Set<CompanyLinkRecord> companyLinkRecords = new HashSet<>();

    public void setCompanyLinkRecords(Set<CompanyLinkRecord> companyLinkRecords) {
        this.companyLinkRecords = (companyLinkRecords == null) ? new HashSet<>() : new HashSet<>(companyLinkRecords);
        for (CompanyLinkRecord companyLinkRecord : this.companyLinkRecords) {
            companyLinkRecord.setCompanyUnitRecord(this);
        }
    }

    public void addCompanyLinkRecord(CompanyLinkRecord record) {
        if (!this.companyLinkRecords.contains(record)) {
            record.setCompanyUnitRecord(this);
            this.companyLinkRecords.add(record);
        }
    }

    public BitemporalSet<CompanyLinkRecord> getCompanyLinkRecords() {
        return new BitemporalSet<>(this.companyLinkRecords);
    }


    public static final String DB_FIELD_META = "metadata";
    public static final String IO_FIELD_META = "produktionsEnhedMetadata";

    @OneToOne(mappedBy = CompanyUnitMetadataRecord.DB_FIELD_COMPANYUNIT, targetEntity = CompanyUnitMetadataRecord.class, cascade = CascadeType.ALL)
    @JoinColumn(name = DB_FIELD_META + DatabaseEntry.REF)
        @JsonProperty(value = IO_FIELD_META)
    private CompanyUnitMetadataRecord metadata;

    public void setMetadata(CompanyUnitMetadataRecord metadata) {
        this.metadata = metadata;
        if (this.metadata != null) {
            this.metadata.setCompanyUnitRecord(this);
        }
    }

    public CompanyUnitMetadataRecord getMetadata() {
        return this.metadata;
    }


    // enhedstype
    // dataAdgang
    // unitNumber
    // advertProtection
    // deltagere
    // virkningsAktoer
    // brancheAnsvarskode
    // naermesteFremtidigeDato
    // samtId
    @JsonIgnore
    public List<CvrRecord> getAll() {
        ArrayList<CvrRecord> list = new ArrayList<>();
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
        if (this.yearlyNumbers != null) {
            list.addAll(this.yearlyNumbers);
        }
        if (this.quarterlyNumbers != null) {
            list.addAll(this.quarterlyNumbers);
        }
        if (this.attributes != null) {
            for (AttributeRecord attributeRecord : this.attributes) {
                list.addAll(attributeRecord.getValues());
            }
        }
        if (this.participantRelations != null) {
            list.addAll(this.participantRelations);
        }
        if (this.companyLinkRecords != null) {
            list.addAll(this.companyLinkRecords);
        }
        return list;
    }


    public UUID generateUUID() {
        return CompanyUnitRecord.generateUUID(this.pNumber);
    }

    public static UUID generateUUID(int pNumber) {
        String uuidInput = "companyunit:" + pNumber;
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
        if (this.metadata != null) {
            this.metadata.wire(session);
        }
        super.save(session);
    }


    @Override
    public boolean merge(CvrEntityRecord other) {
        if (other != null && !Objects.equals(this.getId(), other.getId()) && other instanceof CompanyUnitRecord) {
            CompanyUnitRecord otherRecord = (CompanyUnitRecord) other;
            for (SecNameRecord nameRecord : otherRecord.getNames()) {
                this.addName(nameRecord);
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
            for (ContactRecord contactRecord : otherRecord.getFaxNumber()) {
                this.addFaxNumber(contactRecord);
            }
            for (ContactRecord contactRecord : otherRecord.getEmailAddress()) {
                this.addEmailAddress(contactRecord);
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
            for (CompanyYearlyNumbersRecord yearlyNumbersRecord : otherRecord.getYearlyNumbers()) {
                this.addYearlyNumbers(yearlyNumbersRecord);
            }
            for (CompanyQuarterlyNumbersRecord quarterlyNumbersRecord : otherRecord.getQuarterlyNumbers()) {
                this.addQuarterlyNumbers(quarterlyNumbersRecord);
            }
            for (AttributeRecord attributeRecord : otherRecord.getAttributes()) {
                this.mergeAttribute(attributeRecord);
            }
            for (CompanyParticipantRelationRecord participantRelationRecord : otherRecord.getParticipants()) {
                //this.addParticipant(participantRelationRecord);
                this.mergeParticipant(participantRelationRecord);
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
        subs.addAll(this.locationAddress);
        subs.addAll(this.postalAddress);
        subs.addAll(this.phoneNumber);
        subs.addAll(this.faxNumber);
        subs.addAll(this.emailAddress);
        subs.addAll(this.lifecycle);
        subs.addAll(this.primaryIndustry);
        subs.addAll(this.secondaryIndustry1);
        subs.addAll(this.secondaryIndustry2);
        subs.addAll(this.secondaryIndustry3);
        subs.addAll(this.yearlyNumbers);
        subs.addAll(this.quarterlyNumbers);
        subs.addAll(this.attributes);
        subs.addAll(this.participantRelations);
        subs.addAll(this.companyLinkRecords);
        if (this.metadata != null) {
            subs.add(this.metadata);
        }
        return subs;
    }

    @Override
    public void traverse(Consumer<RecordSet<? extends CvrRecord>> setCallback, Consumer<CvrRecord> itemCallback) {
        this.getNames().traverse(setCallback, itemCallback);
        this.getLocationAddress().traverse(setCallback, itemCallback);
        this.getPostalAddress().traverse(setCallback, itemCallback);
        this.getPhoneNumber().traverse(setCallback, itemCallback);
        this.getFaxNumber().traverse(setCallback, itemCallback);
        this.getEmailAddress().traverse(setCallback, itemCallback);
        this.getLifecycle().traverse(setCallback, itemCallback);
        this.getPrimaryIndustry().traverse(setCallback, itemCallback);
        this.getSecondaryIndustry1().traverse(setCallback, itemCallback);
        this.getSecondaryIndustry2().traverse(setCallback, itemCallback);
        this.getSecondaryIndustry3().traverse(setCallback, itemCallback);
        this.getYearlyNumbers().traverse(setCallback, itemCallback);
        this.getQuarterlyNumbers().traverse(setCallback, itemCallback);
        this.getAttributes().traverse(setCallback, itemCallback);
        this.getParticipants().traverse(setCallback, itemCallback);
        this.getCompanyLinkRecords().traverse(setCallback, itemCallback);
        this.getMetadata().traverse(setCallback, itemCallback);
        super.traverse(setCallback, itemCallback);
    }

}
