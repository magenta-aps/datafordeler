package dk.magenta.datafordeler.cvr.data.participant;

import com.fasterxml.jackson.annotation.JsonProperty;
import dk.magenta.datafordeler.core.database.DataItem;
import dk.magenta.datafordeler.core.database.DatabaseEntry;
import dk.magenta.datafordeler.core.database.LookupDefinition;
import dk.magenta.datafordeler.cvr.data.CvrData;
import dk.magenta.datafordeler.cvr.data.DetailData;
import dk.magenta.datafordeler.cvr.data.shared.AddressData;
import dk.magenta.datafordeler.cvr.data.shared.AttributeData;
import dk.magenta.datafordeler.cvr.data.shared.ContactData;
import dk.magenta.datafordeler.cvr.data.unversioned.Address;
import org.hibernate.Hibernate;
import org.hibernate.Session;

import javax.persistence.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Base class for Participant data, linking to Effects and delegating storage to referred classes
 */
@javax.persistence.Entity
@Table(name="cvr_participant_data", indexes = {
        @Index(name = "cvr_participant_lastUpdated", columnList = DataItem.DB_FIELD_LAST_UPDATED),
        @Index(name = "cvr_participant_unitnumber", columnList = ParticipantBaseData.DB_FIELD_UNIT_NUMBER),
        @Index(name = "cvr_participant_location", columnList = ParticipantBaseData.DB_FIELD_LOCATION_ADDRESS + DatabaseEntry.REF)
})
public class ParticipantBaseData extends CvrData<ParticipantEffect, ParticipantBaseData> {

    public static final String DB_FIELD_NAMES = "names";
    public static final String IO_FIELD_NAMES = "navn";

    @OneToMany(cascade = CascadeType.ALL, targetEntity = ParticipantName.class, mappedBy = ParticipantName.DB_FIELD_BASEDATA)
    private Set<ParticipantName> names;

    public Set<String> getNames() {
        HashSet<String> nameSet = new HashSet<>();
        if (this.names != null) {
            for (ParticipantName nameItem : this.names) {
                nameSet.add(nameItem.getValue());
            }
        }
        return nameSet;
    }

    public void addName(String name) {
        if (this.names == null) {
            this.names = new HashSet<>();
        }
        for (ParticipantName nameItem : this.names) {
            if (name.equals(nameItem.getValue())) {
                return;
            }
        }
        ParticipantName participantName = new ParticipantName();
        participantName.setParticipantBaseData(this);
        participantName.setValue(name);
        this.names.add(participantName);
    }

    //--------------------------------------------------------------------------


    public static final String DB_FIELD_PHONENUMBER = "phoneNumber";
    public static final String IO_FIELD_PHONENUMBER = "telefon";

    @OneToOne(optional = true, cascade = CascadeType.ALL)
    private ContactData phoneNumber;

    @JsonProperty(value = IO_FIELD_PHONENUMBER)
    public String getPhoneNumber() {
        if (phoneNumber != null) {
            return phoneNumber.getValue();
        } else {
            return null;
        }
    }

    public void setPhoneNumber(String phone, boolean secret) {
        if (this.phoneNumber == null) {
            this.phoneNumber = new ContactData(ContactData.Type.TELEFONNUMMER);
        }
        this.phoneNumber.setValue(phone);
        this.phoneNumber.setSecret(secret);
    }


    //--------------------------------------------------------------------------


    public static final String DB_FIELD_EMAIL = "emailAddress";
    public static final String IO_FIELD_EMAIL = "email";

    @OneToOne(optional = true, cascade = CascadeType.ALL)
    private ContactData emailAddress;

    @JsonProperty(value = IO_FIELD_EMAIL)
    public String getEmailAddress() {
        if (emailAddress != null) {
            return emailAddress.getValue();
        } else {
            return null;
        }
    }

    public void setEmailAddress(String email, boolean secret) {
        if (this.emailAddress == null) {
            this.emailAddress = new ContactData(ContactData.Type.EMAILADRESSE);
        }
        this.emailAddress.setValue(email);
        this.emailAddress.setSecret(secret);
    }


    //--------------------------------------------------------------------------


    public static final String DB_FIELD_FAXNUMBER = "faxNumber";
    public static final String IO_FIELD_FAXNUMBER = "telefax";

    @OneToOne(optional = true, cascade = CascadeType.ALL)
    private ContactData faxNumber;

    @JsonProperty(IO_FIELD_FAXNUMBER)
    public String getFaxNumber() {
        if (faxNumber != null) {
            return faxNumber.getValue();
        } else {
            return null;
        }
    }

    public void setFaxNumber(String fax, boolean secret) {
        if (this.faxNumber == null) {
            this.faxNumber = new ContactData(ContactData.Type.TELEFAXNUMMER);
        }
        this.faxNumber.setValue(fax);
        this.faxNumber.setSecret(secret);
    }


    //--------------------------------------------------------------------------


    public static final String DB_FIELD_LOCATION_ADDRESS = "locationaddress";
    public static final String IO_FIELD_LOCATION_ADDRESS = "beliggenhedsadresse";

    @OneToOne(optional = true, cascade = CascadeType.ALL, targetEntity = AddressData.class)
    private AddressData locationaddress;

    @JsonProperty(value = IO_FIELD_LOCATION_ADDRESS)
    public Address getLocationAddress() {
        if (locationaddress != null) {
            return locationaddress.getAddress();
        } else {
            return null;
        }
    }

    public void setLocationAddress(Address address) {
        if (this.locationaddress == null) {
            this.locationaddress = new AddressData();
        }
        this.locationaddress.setAddress(address);
    }


    //--------------------------------------------------------------------------


    public static final String DB_FIELD_POSTAL_ADDRESS = "postalAddress";
    public static final String IO_FIELD_POSTAL_ADDRESS = "postadresse";

    @OneToOne(optional = true, cascade = CascadeType.ALL)
    private AddressData postalAddress;

    @JsonProperty(value = IO_FIELD_POSTAL_ADDRESS)
    public Address getPostalAddress() {
        if (postalAddress != null) {
            return postalAddress.getAddress();
        } else {
            return null;
        }
    }
    public void setPostalAddress(Address address) {
        if (this.postalAddress == null) {
            this.postalAddress = new AddressData();
        }
        this.postalAddress.setAddress(address);
    }


    //--------------------------------------------------------------------------


    @OneToOne(optional = true, cascade = CascadeType.ALL)
    private AddressData businessAddress;

    public static final String DB_FIELD_BUSINESS_ADDRESS = "businessAddress";
    public static final String IO_FIELD_BUSINESS_ADDRESS = "forretningsadresse";

    public Address getBusinessAddress() {
        if (businessAddress != null) {
            return businessAddress.getAddress();
        } else {
            return null;
        }
    }

    public void setBusinessAddress(Address address) {
        if (this.businessAddress == null) {
            this.businessAddress = new AddressData();
        }
        this.businessAddress.setAddress(address);
    }


    //--------------------------------------------------------------------------

    public static final String DB_FIELD_UNIT_NUMBER = "unitNumber";
    public static final String IO_FIELD_UNIT_NUMBER = "deltagernummer";

    @Column(nullable = true, name = DB_FIELD_UNIT_NUMBER)
    private Long unitNumber;

    public Long getUnitNumber() {
        return this.unitNumber;
    }

    public void setUnitNumber(long unitNumber) {
        this.unitNumber = unitNumber;
    }

    //--------------------------------------------------------------------------


    public static final String DB_FIELD_UNIT_TYPE = "unitType";
    public static final String IO_FIELD_UNIT_TYPE = "enhedstype";

    @ManyToOne(optional = true)
    private ParticipantType unitType;

    public String getUnitType() {
        if (unitType != null) {
            return unitType.getName();
        } else {
            return null;
        }
    }

    public void setUnitType(ParticipantType enhedstype) {
        this.unitType = enhedstype;
    }


    //--------------------------------------------------------------------------


    public static final String DB_FIELD_ROLE = "role";
    public static final String IO_FIELD_ROLE = "rolle";

    @ManyToOne(optional = true)
    private ParticipantRole role;

    public String getRole() {
        if (role != null) {
            return role.getName();
        } else {
            return null;
        }
    }

    public void setRole(ParticipantRole role) {
        this.role = role;
    }


    //--------------------------------------------------------------------------


    public static final String DB_FIELD_STATUS = "status";
    public static final String IO_FIELD_STATUS = "status";

    @ManyToOne(optional = true)
    private ParticipantStatus status;

    public String getStatus() {
        if (status != null) {
            return status.getName();
        } else {
            return null;
        }
    }

    public void setStatus(ParticipantStatus status) {
        this.status = status;
    }


    //--------------------------------------------------------------------------


    public static final String DB_FIELD_ATTRIBUTES = "attributeData";
    public static final String IO_FIELD_ATTRIBUTES = "attributter";

    @OneToMany(cascade = CascadeType.ALL, mappedBy = AttributeData.DB_FIELD_PARTICIPANTBASE)
    private Set<AttributeData> attributeData = new HashSet<>();

    public Set<AttributeData> getAttributeData() {
        return attributeData;
    }

    @JsonProperty(value = IO_FIELD_ATTRIBUTES)
    public Set<AttributeData> getAttributes() {
        if (attributeData.isEmpty()) {
            return null;
        }
        return attributeData;
    }

    public void addAttribute(String type, String valueType, String value, int sequenceNumber) {
        AttributeData attributeData = new AttributeData();
        attributeData.setType(type);
        attributeData.setValueType(valueType);
        attributeData.setValue(value);
        attributeData.setSequenceNumber(sequenceNumber);
        this.addAttribute(attributeData);
    }

    public void addAttribute(AttributeData attributeData) {
        attributeData.setParticipantBaseData(this);
        this.attributeData.add(attributeData);
    }

    //--------------------------------------------------------------------------

    public static final String DB_FIELD_POSITION = "position";
    public static final String IO_FIELD_POSITION = "stilling";

    @Column(name = DB_FIELD_POSITION, nullable = true)
    private String position;

    public String getPosition() {
        return this.position;
    }

    @JsonProperty(value = IO_FIELD_POSITION)
    public void setPosition(String position) {
        this.position = position;
    }

    //--------------------------------------------------------------------------



    /**
     * Return a map of attributes, including those from the superclass
     * @return
     */
    @Override
    public Map<String, Object> asMap() {
        HashMap<String, Object> map = new HashMap<>();

        if (this.names != null) {
            map.put("names", this.getNames());
        }
        if (this.phoneNumber != null) {
            map.put("phoneNumber", this.phoneNumber.getValue());
        }
        if (this.emailAddress != null) {
            map.put("emailAddress", this.emailAddress.getValue());
        }
        if (this.faxNumber != null) {
            map.put("faxNumber", this.faxNumber.getValue());
        }
        if (this.locationaddress != null) {
            map.put("locationaddress", this.locationaddress.getAddress());
        }
        if (this.postalAddress != null) {
            map.put("postalAddress", this.postalAddress.getAddress());
        }
        if (this.businessAddress != null) {
            map.put("businessAddress", this.businessAddress.getAddress());
        }
        if (this.unitNumber != null) {
            map.put("unitNumber", this.unitNumber);
        }
        if (this.unitType != null) {
            map.put("unitType", this.unitType.getName());
        }
        if (this.role != null) {
            map.put("role", this.role.getName());
        }
        if (this.status != null) {
            map.put("status", this.status.getName());
        }
        if (this.getAttributes() != null) {
            map.put("attributes", this.getAttributes());
        }
        return map;
    }


    @Override
    public LookupDefinition getLookupDefinition() {
        LookupDefinition lookupDefinition = new LookupDefinition(ParticipantBaseData.class);
        lookupDefinition.setMatchNulls(true);
        if (this.names != null) {
            lookupDefinition.putAll(DB_FIELD_NAMES, DetailData.listDatabaseFields(this.names));
        }
        if (this.phoneNumber != null) {
            lookupDefinition.putAll(DB_FIELD_PHONENUMBER, this.phoneNumber.databaseFields());
        }
        if (this.emailAddress != null) {
            lookupDefinition.putAll(DB_FIELD_EMAIL, this.emailAddress.databaseFields());
        }
        if (this.faxNumber != null) {
            lookupDefinition.putAll(DB_FIELD_FAXNUMBER, this.faxNumber.databaseFields());
        }
        if (this.locationaddress != null) {
            lookupDefinition.putAll(DB_FIELD_LOCATION_ADDRESS, this.locationaddress.databaseFields());
        }
        if (this.postalAddress != null) {
            lookupDefinition.putAll(DB_FIELD_LOCATION_ADDRESS, this.postalAddress.databaseFields());
        }
        if (this.businessAddress != null) {
            lookupDefinition.putAll(DB_FIELD_BUSINESS_ADDRESS, this.businessAddress.databaseFields());
        }
        if (this.unitNumber != null) {
            lookupDefinition.put(DB_FIELD_UNIT_NUMBER, this.unitNumber);
        }
        if (this.status != null) {
            lookupDefinition.putAll(DB_FIELD_STATUS, this.status.databaseFields());
        }
        if (this.unitType != null) {
            lookupDefinition.putAll(DB_FIELD_UNIT_TYPE, this.unitType.databaseFields());
        }

        return lookupDefinition;
    }

    public void forceLoad(Session session) {
        Hibernate.initialize(this.attributeData);
    }
}
