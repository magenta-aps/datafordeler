package dk.magenta.datafordeler.cvr.records;

import com.fasterxml.jackson.annotation.JsonFilter;
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
import dk.magenta.datafordeler.cvr.service.ParticipantRecordService;
import jakarta.persistence.*;
import org.hibernate.Session;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.Filters;
import org.hibernate.annotations.SQLRestriction;
import org.hibernate.annotations.Where;

import java.util.*;
import java.util.function.Consumer;

/**
 * Base record for Participant data, parsed from JSON into a tree of objects
 * with this class at the base.
 */
@Entity
@Table(name = CvrPlugin.DEBUG_TABLE_PREFIX + ParticipantRecord.TABLE_NAME, indexes = {
        @Index(name = CvrPlugin.DEBUG_TABLE_PREFIX + ParticipantRecord.TABLE_NAME + "__unitNumber", columnList = ParticipantRecord.DB_FIELD_UNIT_NUMBER, unique = true),
        @Index(name = CvrPlugin.DEBUG_TABLE_PREFIX + ParticipantRecord.TABLE_NAME + "__" + ParticipantRecord.DB_FIELD_DAFO_UPDATED, columnList = ParticipantRecord.DB_FIELD_DAFO_UPDATED)
})

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonFilter("ParticipantRecordFilter")  // Keeps businessKey from being outputted in normal outputwrappers
public class ParticipantRecord extends CvrEntityRecord {

    public static final String TABLE_NAME = "cvr_record_participant";

    public static final String schema = "deltager";

    @JsonIgnore
    public String getFieldName() {
        return TABLE_NAME;
    }

    @Override
    @JsonIgnore
    protected String getDomain() {
        return ParticipantRecordService.getDomain();
    }

    public static final String DB_FIELD_UNIT_NUMBER = "unitNumber";
    public static final String IO_FIELD_UNIT_NUMBER = "enhedsNummer";

    @Column(name = DB_FIELD_UNIT_NUMBER)
    @JsonProperty(value = IO_FIELD_UNIT_NUMBER)
    private long unitNumber;

    public long getUnitNumber() {
        return this.unitNumber;
    }

    @JsonIgnore
    public Map<String, Object> getIdentifyingFilter() {
        return Collections.singletonMap(DB_FIELD_UNIT_NUMBER, this.unitNumber);
    }


    public static final String DB_FIELD_UNIT_TYPE = "unitType";
    public static final String IO_FIELD_UNIT_TYPE = "enhedstype";

    @Column(name = DB_FIELD_UNIT_TYPE)
    @JsonProperty(value = IO_FIELD_UNIT_TYPE)
    private String unitType;

    public String getUnitType() {
        return this.unitType;
    }


    public static final String DB_FIELD_POSITION = "position";
    public static final String IO_FIELD_POSITION = "stilling";

    @Column(name = DB_FIELD_POSITION)
    @JsonProperty(value = IO_FIELD_POSITION)
    private String position;

    public String getPosition() {
        return this.position;
    }


    public static final String DB_FIELD_CONFIDENTIAL_ENRICHED = "confidentialEnriched";
    public static final String IO_FIELD_CONFIDENTIAL_ENRICHED = "fortroligBeriget";

    @Column(name = DB_FIELD_CONFIDENTIAL_ENRICHED)
    private Boolean confidentialEnriched;

    @JsonIgnore
    public Boolean getConfidentialEnriched() {
        return this.confidentialEnriched;
    }

    @JsonProperty(value = IO_FIELD_CONFIDENTIAL_ENRICHED)
    public void setConfidentialEnriched(Boolean confidentialEnriched) {
        this.confidentialEnriched = confidentialEnriched;
    }

    public static final String DB_FIELD_BUSINESS_KEY = "businessKey";
    public static final String IO_FIELD_BUSINESS_KEY = "forretningsnoegle";

    @Column(name = DB_FIELD_BUSINESS_KEY)
    @JsonProperty(value = IO_FIELD_BUSINESS_KEY)
    private Long businessKey;

    public void setBusinessKey(Long businessKey) {
        this.businessKey = businessKey;
    }

    public Long getBusinessKey() {
        return this.businessKey;
    }


    public static final String DB_FIELD_STATUS_CODE = "statusCode";
    public static final String IO_FIELD_STATUS_CODE = "statusKode";

    @Column(name = DB_FIELD_STATUS_CODE)
    @JsonProperty(value = IO_FIELD_STATUS_CODE)
    private Long statusCode;

    public Long getStatusCode() {
        return this.statusCode;
    }


    public static final String DB_FIELD_NAMES = "names";
    public static final String IO_FIELD_NAMES = "navne";

    @OneToMany(mappedBy = SecNameRecord.DB_FIELD_PARTICIPANT, targetEntity = SecNameRecord.class, cascade = CascadeType.ALL)
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
    public Set<SecNameRecord> names = new HashSet<>();

    public void setNames(Set<SecNameRecord> names) {
        this.names = (names == null) ? new HashSet<>() : new HashSet<>(names);
        for (SecNameRecord record : this.names) {
            record.setParticipantRecord(this);
        }
    }

    public void addName(SecNameRecord record) {
        if (record != null && !this.names.contains(record)) {
            record.setSecondary(false);
            record.setParticipantRecord(this);
            this.names.add(record);
        }
    }

    public BitemporalSet<SecNameRecord, ParticipantRecord> getNames() {
        return new BitemporalSet<>(this.names, SecNameRecord.class, this, SecNameRecord.DB_FIELD_PARTICIPANT);
    }


    public static final String DB_FIELD_LOCATION_ADDRESS = "locationAddress";
    public static final String IO_FIELD_LOCATION_ADDRESS = "beliggenhedsadresse";
    public static final String CLAUSE_ADDRESS_LOCATION = AddressRecord.DB_FIELD_TYPE + "=" + AddressRecord.TYPE_LOCATION;


    @OneToMany(mappedBy = AddressRecord.DB_FIELD_PARTICIPANT, targetEntity = AddressRecord.class, cascade = CascadeType.ALL)
    @SQLRestriction(CLAUSE_ADDRESS_LOCATION)
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
    public Set<AddressRecord> locationAddress = new HashSet<>();

    public void setLocationAddress(Set<AddressRecord> locationAddress) {
        this.locationAddress = (locationAddress == null) ? new HashSet<>() : new HashSet<>(locationAddress);
        for (AddressRecord record : this.locationAddress) {
            record.setType(AddressRecord.TYPE_LOCATION);
            record.setParticipantRecord(this);
        }
    }

    public void addLocationAddress(AddressRecord record) {
        if (record != null && !this.locationAddress.contains(record)) {
            record.setType(AddressRecord.TYPE_LOCATION);
            record.setParticipantRecord(this);
            this.locationAddress.add(record);
        }
    }

    public BitemporalSet<AddressRecord, ParticipantRecord> getLocationAddress() {
        return new BitemporalSet<>(this.locationAddress, AddressRecord.class, this, AddressRecord.DB_FIELD_PARTICIPANT, CLAUSE_ADDRESS_LOCATION);
    }


    public static final String DB_FIELD_POSTAL_ADDRESS = "postalAddress";
    public static final String IO_FIELD_POSTAL_ADDRESS = "postadresse";
    public static final String CLAUSE_ADDRESS_POSTAL = AddressRecord.DB_FIELD_TYPE + "=" + AddressRecord.TYPE_POSTAL;

    @OneToMany(mappedBy = AddressRecord.DB_FIELD_PARTICIPANT, targetEntity = AddressRecord.class, cascade = CascadeType.ALL)
    @SQLRestriction(CLAUSE_ADDRESS_POSTAL)
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
    public Set<AddressRecord> postalAddress = new HashSet<>();

    public void setPostalAddress(Set<AddressRecord> postalAddress) {
        this.postalAddress = (postalAddress == null) ? new HashSet<>() : new HashSet<>(postalAddress);
        for (AddressRecord record : this.postalAddress) {
            record.setType(AddressRecord.TYPE_POSTAL);
            record.setParticipantRecord(this);
        }
    }

    public void addPostalAddress(AddressRecord record) {
        if (record != null && !this.postalAddress.contains(record)) {
            record.setType(AddressRecord.TYPE_POSTAL);
            record.setParticipantRecord(this);
            this.postalAddress.add(record);
        }
    }

    public BitemporalSet<AddressRecord, ParticipantRecord> getPostalAddress() {
        return new BitemporalSet<>(this.postalAddress, AddressRecord.class, this, AddressRecord.DB_FIELD_PARTICIPANT, CLAUSE_ADDRESS_POSTAL);
    }


    public static final String DB_FIELD_BUSINESS_ADDRESS = "businessAddress";
    public static final String IO_FIELD_BUSINESS_ADDRESS = "forretningsadresse";
    public static final String CLAUSE_ADDRESS_BUSINESS = AddressRecord.DB_FIELD_TYPE + "=" + AddressRecord.TYPE_BUSINESS;


    @OneToMany(mappedBy = AddressRecord.DB_FIELD_PARTICIPANT, targetEntity = AddressRecord.class, cascade = CascadeType.ALL)
    @SQLRestriction(CLAUSE_ADDRESS_BUSINESS)
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
    @JsonProperty(value = IO_FIELD_BUSINESS_ADDRESS)
    public Set<AddressRecord> businessAddress = new HashSet<>();

    public void setBusinessAddress(Set<AddressRecord> businessAddress) {
        this.businessAddress = (businessAddress == null) ? new HashSet<>() : new HashSet<>(businessAddress);
        for (AddressRecord record : this.businessAddress) {
            record.setType(AddressRecord.TYPE_BUSINESS);
            record.setParticipantRecord(this);
        }
    }

    public void addBusinessAddress(AddressRecord record) {
        if (record != null && !this.businessAddress.contains(record)) {
            record.setType(AddressRecord.TYPE_BUSINESS);
            record.setParticipantRecord(this);
            this.businessAddress.add(record);
        }
    }

    public BitemporalSet<AddressRecord, ParticipantRecord> getBusinessAddress() {
        return new BitemporalSet<>(this.businessAddress, AddressRecord.class, this, AddressRecord.DB_FIELD_PARTICIPANT, CLAUSE_ADDRESS_BUSINESS);
    }


    public static final String DB_FIELD_PHONE = "phoneNumber";
    public static final String IO_FIELD_PHONE = "telefonNummer";
    public static final String CLAUSE_CONTACT_PHONE = ContactRecord.DB_FIELD_TYPE + "=" + ContactRecord.TYPE_TELEFONNUMMER;

    @OneToMany(mappedBy = ContactRecord.DB_FIELD_PARTICIPANT, targetEntity = ContactRecord.class, cascade = CascadeType.ALL)
    @SQLRestriction(CLAUSE_CONTACT_PHONE)
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
    public Set<ContactRecord> phoneNumber = new HashSet<>();

    public void setPhoneNumber(Set<ContactRecord> phoneNumber) {
        this.phoneNumber = (phoneNumber == null) ? new HashSet<>() : new HashSet<>(phoneNumber);
        for (ContactRecord record : this.phoneNumber) {
            record.setType(ContactRecord.TYPE_TELEFONNUMMER);
            record.setParticipantRecord(this);
        }
    }

    public void addPhoneNumber(ContactRecord record) {
        if (record != null && !this.phoneNumber.contains(record)) {
            record.setType(ContactRecord.TYPE_TELEFONNUMMER);
            record.setParticipantRecord(this);
            record.setSecondary(false);
            this.phoneNumber.add(record);
        }
    }

    public BitemporalSet<ContactRecord, ParticipantRecord> getPhoneNumber() {
        return new BitemporalSet<>(this.phoneNumber, ContactRecord.class, this, ContactRecord.DB_FIELD_PARTICIPANT, CLAUSE_CONTACT_PHONE);
    }


    public static final String DB_FIELD_FAX = "faxNumber";
    public static final String IO_FIELD_FAX = "telefaxNummer";
    public static final String CLAUSE_CONTACT_FAX = ContactRecord.DB_FIELD_TYPE + "=" + ContactRecord.TYPE_TELEFAXNUMMER;

    @OneToMany(mappedBy = ContactRecord.DB_FIELD_PARTICIPANT, targetEntity = ContactRecord.class, cascade = CascadeType.ALL)
    @SQLRestriction(CLAUSE_CONTACT_FAX)
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
    public Set<ContactRecord> faxNumber = new HashSet<>();

    public void setFaxNumber(Set<ContactRecord> faxNumber) {
        this.faxNumber = (faxNumber == null) ? new HashSet<>() : new HashSet<>(faxNumber);
        for (ContactRecord record : this.faxNumber) {
            record.setType(ContactRecord.TYPE_TELEFAXNUMMER);
            record.setParticipantRecord(this);
        }
    }

    public void addFaxNumber(ContactRecord record) {
        if (record != null && !this.faxNumber.contains(record)) {
            record.setType(ContactRecord.TYPE_TELEFAXNUMMER);
            record.setParticipantRecord(this);
            record.setSecondary(false);
            this.faxNumber.add(record);
        }
    }

    public BitemporalSet<ContactRecord, ParticipantRecord> getFaxNumber() {
        return new BitemporalSet<>(this.faxNumber, ContactRecord.class, this, ContactRecord.DB_FIELD_PARTICIPANT, CLAUSE_CONTACT_FAX);
    }


    public static final String DB_FIELD_EMAIL = "emailAddress";
    public static final String IO_FIELD_EMAIL = "elektroniskPost";
    public static final String CLAUSE_CONTACT_EMAIL = ContactRecord.DB_FIELD_TYPE + "=" + ContactRecord.TYPE_EMAILADRESSE;

    @OneToMany(mappedBy = ContactRecord.DB_FIELD_PARTICIPANT, targetEntity = ContactRecord.class, cascade = CascadeType.ALL)
    @SQLRestriction(CLAUSE_CONTACT_EMAIL)
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
    public Set<ContactRecord> emailAddress = new HashSet<>();

    public void setEmailAddress(Set<ContactRecord> emailAddress) {
        this.emailAddress = (emailAddress == null) ? new HashSet<>() : new HashSet<>(emailAddress);
        for (ContactRecord record : this.emailAddress) {
            record.setType(ContactRecord.TYPE_EMAILADRESSE);
            record.setParticipantRecord(this);
        }

    }

    public void addEmailAddress(ContactRecord record) {
        if (record != null && !this.emailAddress.contains(record)) {
            record.setType(ContactRecord.TYPE_EMAILADRESSE);
            record.setParticipantRecord(this);
            this.emailAddress.add(record);
        }
    }

    public BitemporalSet<ContactRecord, ParticipantRecord> getEmailAddress() {
        return new BitemporalSet<>(this.emailAddress, ContactRecord.class, this, ContactRecord.DB_FIELD_PARTICIPANT, CLAUSE_CONTACT_EMAIL);
    }


    public static final String DB_FIELD_ATTRIBUTES = "attributes";
    public static final String IO_FIELD_ATTRIBUTES = "attributter";

    @OneToMany(mappedBy = AttributeRecord.DB_FIELD_PARTICIPANT, targetEntity = AttributeRecord.class, cascade = CascadeType.ALL)
    @JsonProperty(value = IO_FIELD_ATTRIBUTES)
    public Set<AttributeRecord> attributes = new HashSet<>();

    public void setAttributes(Set<AttributeRecord> attributes) {
        this.attributes = attributes;
        if (attributes != null) {
            for (AttributeRecord attributeRecord : attributes) {
                attributeRecord.setParticipantRecord(this);
            }
        }
    }

    public void addAttribute(AttributeRecord record) {
        if (record != null && !this.attributes.contains(record)) {
            record.setParticipantRecord(this);
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

    public AttributeRecordSet<ParticipantRecord> getAttributes() {
        return new AttributeRecordSet(this.attributes, this, AttributeRecord.DB_FIELD_PARTICIPANT);
    }


    public static final String DB_FIELD_META = "metadata";
    public static final String IO_FIELD_META = "deltagerpersonMetadata";

    @OneToOne(targetEntity = ParticipantMetadataRecord.class, mappedBy = ParticipantMetadataRecord.DB_FIELD_PARTICIPANT, cascade = CascadeType.ALL)
    @JoinColumn(name = DB_FIELD_META + DatabaseEntry.REF)
    @JsonProperty(value = IO_FIELD_META)
    private ParticipantMetadataRecord metadata;

    public void setMetadata(ParticipantMetadataRecord metadata) {
        this.metadata = metadata;
        if (this.metadata != null) {
            this.metadata.setParticipantRecord(this);
        }
    }

    public ParticipantMetadataRecord getMetadata() {
        return this.metadata;
    }


    public static final String DB_FIELD_COMPANY_RELATION = "companyRelation";
    public static final String IO_FIELD_COMPANY_RELATION = "virksomhedSummariskRelation";

    @OneToMany(targetEntity = CompanyParticipantRelationRecord.class, mappedBy = CompanyParticipantRelationRecord.DB_FIELD_PARTICIPANT, cascade = CascadeType.ALL)
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
    @JsonProperty(value = IO_FIELD_COMPANY_RELATION)
    private Set<CompanyParticipantRelationRecord> companyRelation = new HashSet<>();

    public void setCompanyRelation(Set<CompanyParticipantRelationRecord> companyRelation) {
        this.companyRelation = (companyRelation == null) ? new HashSet<>() : new HashSet<>(companyRelation);
        for (CompanyParticipantRelationRecord companyParticipantRelationRecord : this.companyRelation) {
            companyParticipantRelationRecord.setParticipantRecord(this);
        }
    }

    public void addCompanyRelation(CompanyParticipantRelationRecord record) {
        if (record != null && !this.companyRelation.contains(record)) {
            record.setParticipantRecord(this);
            this.companyRelation.add(record);
        }
    }

    public void mergeCompanyRelation(CompanyParticipantRelationRecord otherRecord) {
        Long otherCompanyId = otherRecord.getCompanyUnitNumber();
        if (otherCompanyId != null) {
            for (CompanyParticipantRelationRecord ourRecord : this.companyRelation) {
                Long ourCompanyId = ourRecord.getCompanyUnitNumber();
                if (otherCompanyId.equals(ourCompanyId)) {
                    ourRecord.merge(otherRecord);
                    return;
                }
            }
        }
        this.addCompanyRelation(otherRecord);
    }

    public BitemporalSet<CompanyParticipantRelationRecord, ParticipantRecord> getCompanyRelation() {
        return new BitemporalSet<>(this.companyRelation, CompanyParticipantRelationRecord.class, this, CompanyParticipantRelationRecord.DB_FIELD_PARTICIPANT);
    }


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
        if (this.businessAddress != null) {
            list.addAll(this.businessAddress);
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
        if (this.companyRelation != null) {
            list.addAll(this.companyRelation);
        }
        if (this.attributes != null) {
            for (AttributeRecord attributeRecord : this.attributes) {
                list.addAll(attributeRecord.getValues());
            }
        }
        return list;
    }

    public UUID generateUUID() {
        return ParticipantRecord.generateUUID(this.unitType, this.unitNumber);
    }

    public static UUID generateUUID(String unitType, long unitNumber) {
        String uuidInput = "participant:" + unitType + "/" + unitNumber;
        return UUID.nameUUIDFromBytes(uuidInput.getBytes());
    }


    @Override
    public void save(Session session) {
        this.wire(session);
        super.save(session);
    }

    public void wire(Session session) {
        for (AddressRecord address : this.locationAddress) {
            address.wire(session);
        }
        for (AddressRecord address : this.postalAddress) {
            address.wire(session);
        }
        for (AddressRecord address : this.businessAddress) {
            address.wire(session);
        }
        for (CompanyParticipantRelationRecord companyParticipantRelationRecord : this.companyRelation) {
            companyParticipantRelationRecord.wire(session);
        }
        if (this.metadata != null) {
            this.metadata.wire(session);
        }
        // TODO: wire on traverse
    }

    @Override
    public boolean merge(CvrEntityRecord other) {
        if (other != null && !Objects.equals(this.getId(), other.getId()) && other instanceof ParticipantRecord) {
            ParticipantRecord otherRecord = (ParticipantRecord) other;
            if (this.getBusinessKey() == null && otherRecord.getBusinessKey() != null) {
                this.setBusinessKey(otherRecord.getBusinessKey());
            }
            for (SecNameRecord nameRecord : otherRecord.getNames()) {
                this.addName(nameRecord);
            }
            for (AddressRecord addressRecord : otherRecord.getLocationAddress()) {
                this.addLocationAddress(addressRecord);
            }
            for (AddressRecord addressRecord : otherRecord.getPostalAddress()) {
                this.addPostalAddress(addressRecord);
            }
            for (AddressRecord addressRecord : otherRecord.getBusinessAddress()) {
                this.addBusinessAddress(addressRecord);
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
            for (AttributeRecord attributeRecord : otherRecord.getAttributes()) {
                this.mergeAttribute(attributeRecord);
            }
            for (CompanyParticipantRelationRecord companyParticipantRelationRecord : otherRecord.getCompanyRelation()) {
                this.mergeCompanyRelation(companyParticipantRelationRecord);
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
        subs.addAll(this.businessAddress);
        subs.addAll(this.phoneNumber);
        subs.addAll(this.faxNumber);
        subs.addAll(this.emailAddress);
        subs.addAll(this.attributes);
        subs.addAll(this.companyRelation);
        if (this.metadata != null) {
            subs.add(this.metadata);
        }
        return subs;
    }


    @Override
    public void traverse(Consumer<RecordSet<? extends CvrRecord, ? extends CvrRecord>> setCallback, Consumer<CvrRecord> itemCallback, boolean grouped) {
        super.traverse(setCallback, itemCallback, grouped);
        this.getNames().traverse(setCallback, itemCallback);
        this.getLocationAddress().traverse(setCallback, itemCallback);
        this.getPostalAddress().traverse(setCallback, itemCallback);
        this.getBusinessAddress().traverse(setCallback, itemCallback);
        this.getPhoneNumber().traverse(setCallback, itemCallback);
        this.getFaxNumber().traverse(setCallback, itemCallback);
        this.getEmailAddress().traverse(setCallback, itemCallback);
        this.getAttributes().traverse(setCallback, itemCallback);
        this.getCompanyRelation().traverse(setCallback, itemCallback);
        this.getMetadata().traverse(setCallback, itemCallback);
    }

}
