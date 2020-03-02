package dk.magenta.datafordeler.cpr.data.person;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import dk.magenta.datafordeler.core.database.Bitemporal;
import dk.magenta.datafordeler.core.database.Identification;
import dk.magenta.datafordeler.core.database.Monotemporal;
import dk.magenta.datafordeler.core.database.Nontemporal;
import dk.magenta.datafordeler.core.util.Equality;
import dk.magenta.datafordeler.cpr.CprPlugin;
import dk.magenta.datafordeler.cpr.data.CprEntity;
import dk.magenta.datafordeler.cpr.records.person.CprBitemporalPersonRecord;
import dk.magenta.datafordeler.cpr.records.person.data.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.Filters;
import org.hibernate.annotations.Where;

import javax.persistence.*;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * An Entity representing a person. Bitemporal data is structured as
 * described in {@link dk.magenta.datafordeler.core.database.Entity}
 */
@javax.persistence.Entity
@Table(name= CprPlugin.DEBUG_TABLE_PREFIX + "cpr_person_entity", indexes = {
        @Index(name = CprPlugin.DEBUG_TABLE_PREFIX + "cpr_person_identification", columnList = "identification_id", unique = true),
        @Index(name = CprPlugin.DEBUG_TABLE_PREFIX + "cpr_person_personnummer", columnList = PersonEntity.DB_FIELD_CPR_NUMBER, unique = true)
})
@XmlAccessorType(XmlAccessType.FIELD)
public class PersonEntity extends CprEntity<PersonEntity, PersonRegistration> {

    public PersonEntity() {
    }

    public PersonEntity(Identification identification) {
        super(identification);
    }

    public PersonEntity(UUID uuid, String domain) {
        super(uuid, domain);
    }

    @Override
    protected PersonRegistration createEmptyRegistration() {
        return new PersonRegistration();
    }

    @JsonTypeInfo(use=JsonTypeInfo.Id.CLASS, include=JsonTypeInfo.As.PROPERTY, property="type")
    public static final String schema = "Person";

    public static final String DB_FIELD_CPR_NUMBER = "personnummer";
    public static final String IO_FIELD_CPR_NUMBER = "personnummer";

    @Column(name = DB_FIELD_CPR_NUMBER)
    @JsonProperty("personnummer")
    @XmlElement(name=("personnummer"))
    private String personnummer;

    public String getPersonnummer() {
        return this.personnummer;
    }

    public void setPersonnummer(String personnummer) {
        this.personnummer = personnummer;
    }

    public static UUID generateUUID(String cprNumber) {
        String uuidInput = "person:"+cprNumber;
        return UUID.nameUUIDFromBytes(uuidInput.getBytes());
    }






    public static final String DB_FIELD_ADDRESS_CONAME = "coname";
    public static final String IO_FIELD_ADDRESS_CONAME = "conavn";
    @OneToMany(mappedBy = CprBitemporalPersonRecord.DB_FIELD_ENTITY, cascade = CascadeType.ALL)
    @Filters({
            @Filter(name = Bitemporal.FILTER_EFFECT_AFTER, condition = Bitemporal.FILTERLOGIC_EFFECT_AFTER),
            @Filter(name = Bitemporal.FILTER_EFFECT_BEFORE, condition = Bitemporal.FILTERLOGIC_EFFECT_BEFORE),
            @Filter(name = Monotemporal.FILTER_REGISTRATION_AFTER, condition = Monotemporal.FILTERLOGIC_REGISTRATION_AFTER),
            @Filter(name = Monotemporal.FILTER_REGISTRATION_BEFORE, condition = Monotemporal.FILTERLOGIC_REGISTRATION_BEFORE),
            @Filter(name = Nontemporal.FILTER_LASTUPDATED_AFTER, condition = Nontemporal.FILTERLOGIC_LASTUPDATED_AFTER),
            @Filter(name = Nontemporal.FILTER_LASTUPDATED_BEFORE, condition = Nontemporal.FILTERLOGIC_LASTUPDATED_BEFORE)
    })
    @JsonProperty(IO_FIELD_ADDRESS_CONAME)
    Set<AddressConameDataRecord> coname = new HashSet<>();

    public Set<AddressConameDataRecord> getConame() {
        return this.coname;
    }

    public static final String DB_FIELD_ADDRESS = "address";
    public static final String IO_FIELD_ADDRESS = "adresse";
    @OneToMany(mappedBy = CprBitemporalPersonRecord.DB_FIELD_ENTITY, cascade = CascadeType.ALL)
    @Filters({
            @Filter(name = Bitemporal.FILTER_EFFECT_AFTER, condition = Bitemporal.FILTERLOGIC_EFFECT_AFTER),
            @Filter(name = Bitemporal.FILTER_EFFECT_BEFORE, condition = Bitemporal.FILTERLOGIC_EFFECT_BEFORE),
            @Filter(name = Monotemporal.FILTER_REGISTRATION_AFTER, condition = Monotemporal.FILTERLOGIC_REGISTRATION_AFTER),
            @Filter(name = Monotemporal.FILTER_REGISTRATION_BEFORE, condition = Monotemporal.FILTERLOGIC_REGISTRATION_BEFORE),
            @Filter(name = Nontemporal.FILTER_LASTUPDATED_AFTER, condition = Nontemporal.FILTERLOGIC_LASTUPDATED_AFTER),
            @Filter(name = Nontemporal.FILTER_LASTUPDATED_BEFORE, condition = Nontemporal.FILTERLOGIC_LASTUPDATED_BEFORE)
    })
    @JsonProperty(IO_FIELD_ADDRESS)
    Set<AddressDataRecord> address = new HashSet<>();

    public Set<AddressDataRecord> getAddress() {
        return this.address;
    }

    public static final String DB_FIELD_ADDRESS_NAME = "addressName";
    public static final String IO_FIELD_ADDRESS_NAME = "addresseringsnavn";
    @OneToMany(mappedBy = CprBitemporalPersonRecord.DB_FIELD_ENTITY, cascade = CascadeType.ALL)
    @Filters({
            @Filter(name = Bitemporal.FILTER_EFFECT_AFTER, condition = Bitemporal.FILTERLOGIC_EFFECT_AFTER),
            @Filter(name = Bitemporal.FILTER_EFFECT_BEFORE, condition = Bitemporal.FILTERLOGIC_EFFECT_BEFORE),
            @Filter(name = Monotemporal.FILTER_REGISTRATION_AFTER, condition = Monotemporal.FILTERLOGIC_REGISTRATION_AFTER),
            @Filter(name = Monotemporal.FILTER_REGISTRATION_BEFORE, condition = Monotemporal.FILTERLOGIC_REGISTRATION_BEFORE),
            @Filter(name = Nontemporal.FILTER_LASTUPDATED_AFTER, condition = Nontemporal.FILTERLOGIC_LASTUPDATED_AFTER),
            @Filter(name = Nontemporal.FILTER_LASTUPDATED_BEFORE, condition = Nontemporal.FILTERLOGIC_LASTUPDATED_BEFORE)
    })
    @JsonProperty(IO_FIELD_ADDRESS_NAME)
    Set<AddressNameDataRecord> addressName = new HashSet<>();

    public Set<AddressNameDataRecord> getAddressName() {
        return this.addressName;
    }

    public static final String DB_FIELD_BIRTHPLACE = "birthPlace";
    public static final String IO_FIELD_BIRTHPLACE = "fødselsted";
    @OneToMany(mappedBy = CprBitemporalPersonRecord.DB_FIELD_ENTITY, cascade = CascadeType.ALL)
    @Filters({
            @Filter(name = Bitemporal.FILTER_EFFECT_AFTER, condition = Bitemporal.FILTERLOGIC_EFFECT_AFTER),
            @Filter(name = Bitemporal.FILTER_EFFECT_BEFORE, condition = Bitemporal.FILTERLOGIC_EFFECT_BEFORE),
            @Filter(name = Monotemporal.FILTER_REGISTRATION_AFTER, condition = Monotemporal.FILTERLOGIC_REGISTRATION_AFTER),
            @Filter(name = Monotemporal.FILTER_REGISTRATION_BEFORE, condition = Monotemporal.FILTERLOGIC_REGISTRATION_BEFORE),
            @Filter(name = Nontemporal.FILTER_LASTUPDATED_AFTER, condition = Nontemporal.FILTERLOGIC_LASTUPDATED_AFTER),
            @Filter(name = Nontemporal.FILTER_LASTUPDATED_BEFORE, condition = Nontemporal.FILTERLOGIC_LASTUPDATED_BEFORE)
    })
    @JsonProperty(IO_FIELD_BIRTHPLACE)
    Set<BirthPlaceDataRecord> birthPlace = new HashSet<>();

    public Set<BirthPlaceDataRecord> getBirthPlace() {
        return this.birthPlace;
    }

    public static final String DB_FIELD_BIRTHPLACE_VERIFICATION = "birthPlaceVerification";
    public static final String IO_FIELD_BIRTHPLACE_VERIFICATION = "fødselssted_verifikation";
    @OneToMany(mappedBy = CprBitemporalPersonRecord.DB_FIELD_ENTITY, cascade = CascadeType.ALL)
    @Filters({
            @Filter(name = Bitemporal.FILTER_EFFECT_AFTER, condition = Bitemporal.FILTERLOGIC_EFFECT_AFTER),
            @Filter(name = Bitemporal.FILTER_EFFECT_BEFORE, condition = Bitemporal.FILTERLOGIC_EFFECT_BEFORE),
            @Filter(name = Monotemporal.FILTER_REGISTRATION_AFTER, condition = Monotemporal.FILTERLOGIC_REGISTRATION_AFTER),
            @Filter(name = Monotemporal.FILTER_REGISTRATION_BEFORE, condition = Monotemporal.FILTERLOGIC_REGISTRATION_BEFORE),
            @Filter(name = Nontemporal.FILTER_LASTUPDATED_AFTER, condition = Nontemporal.FILTERLOGIC_LASTUPDATED_AFTER),
            @Filter(name = Nontemporal.FILTER_LASTUPDATED_BEFORE, condition = Nontemporal.FILTERLOGIC_LASTUPDATED_BEFORE)
    })
    @JsonProperty(IO_FIELD_BIRTHPLACE_VERIFICATION)
    Set<BirthPlaceVerificationDataRecord> birthPlaceVerification = new HashSet<>();

    public Set<BirthPlaceVerificationDataRecord> getBirthPlaceVerification() {
        return this.birthPlaceVerification;
    }

    public static final String DB_FIELD_BIRTHTIME = "birthTime";
    public static final String IO_FIELD_BIRTHTIME = "fødselstidspunkt";
    @OneToMany(mappedBy = CprBitemporalPersonRecord.DB_FIELD_ENTITY, cascade = CascadeType.ALL)
    @Filters({
            @Filter(name = Bitemporal.FILTER_EFFECT_AFTER, condition = Bitemporal.FILTERLOGIC_EFFECT_AFTER),
            @Filter(name = Bitemporal.FILTER_EFFECT_BEFORE, condition = Bitemporal.FILTERLOGIC_EFFECT_BEFORE),
            @Filter(name = Monotemporal.FILTER_REGISTRATION_AFTER, condition = Monotemporal.FILTERLOGIC_REGISTRATION_AFTER),
            @Filter(name = Monotemporal.FILTER_REGISTRATION_BEFORE, condition = Monotemporal.FILTERLOGIC_REGISTRATION_BEFORE),
            @Filter(name = Nontemporal.FILTER_LASTUPDATED_AFTER, condition = Nontemporal.FILTERLOGIC_LASTUPDATED_AFTER),
            @Filter(name = Nontemporal.FILTER_LASTUPDATED_BEFORE, condition = Nontemporal.FILTERLOGIC_LASTUPDATED_BEFORE)
    })
    @JsonProperty(IO_FIELD_BIRTHTIME)
    Set<BirthTimeDataRecord> birthTime = new HashSet<>();

    public Set<BirthTimeDataRecord> getBirthTime() {
        return this.birthTime;
    }

    public static final String DB_FIELD_CHURCH = "churchRelation";
    public static final String IO_FIELD_CHURCH = "folkekirkeoplysning";
    @OneToMany(mappedBy = CprBitemporalPersonRecord.DB_FIELD_ENTITY, cascade = CascadeType.ALL)
    @Filters({
            @Filter(name = Bitemporal.FILTER_EFFECT_AFTER, condition = Bitemporal.FILTERLOGIC_EFFECT_AFTER),
            @Filter(name = Bitemporal.FILTER_EFFECT_BEFORE, condition = Bitemporal.FILTERLOGIC_EFFECT_BEFORE),
            @Filter(name = Monotemporal.FILTER_REGISTRATION_AFTER, condition = Monotemporal.FILTERLOGIC_REGISTRATION_AFTER),
            @Filter(name = Monotemporal.FILTER_REGISTRATION_BEFORE, condition = Monotemporal.FILTERLOGIC_REGISTRATION_BEFORE),
            @Filter(name = Nontemporal.FILTER_LASTUPDATED_AFTER, condition = Nontemporal.FILTERLOGIC_LASTUPDATED_AFTER),
            @Filter(name = Nontemporal.FILTER_LASTUPDATED_BEFORE, condition = Nontemporal.FILTERLOGIC_LASTUPDATED_BEFORE)
    })
    @JsonProperty(IO_FIELD_CHURCH)
    Set<ChurchDataRecord> churchRelation = new HashSet<>();

    public Set<ChurchDataRecord> getChurchRelation() {
        return this.churchRelation;
    }

    public static final String DB_FIELD_CHURCH_VERIFICATION = "churchRelationVerification";
    public static final String IO_FIELD_CHURCH_VERIFICATION = "folkekirkeoplysning_verifikation";
    @OneToMany(mappedBy = CprBitemporalPersonRecord.DB_FIELD_ENTITY, cascade = CascadeType.ALL)
    @Filters({
            @Filter(name = Bitemporal.FILTER_EFFECT_AFTER, condition = Bitemporal.FILTERLOGIC_EFFECT_AFTER),
            @Filter(name = Bitemporal.FILTER_EFFECT_BEFORE, condition = Bitemporal.FILTERLOGIC_EFFECT_BEFORE),
            @Filter(name = Monotemporal.FILTER_REGISTRATION_AFTER, condition = Monotemporal.FILTERLOGIC_REGISTRATION_AFTER),
            @Filter(name = Monotemporal.FILTER_REGISTRATION_BEFORE, condition = Monotemporal.FILTERLOGIC_REGISTRATION_BEFORE),
            @Filter(name = Nontemporal.FILTER_LASTUPDATED_AFTER, condition = Nontemporal.FILTERLOGIC_LASTUPDATED_AFTER),
            @Filter(name = Nontemporal.FILTER_LASTUPDATED_BEFORE, condition = Nontemporal.FILTERLOGIC_LASTUPDATED_BEFORE)
    })
    @JsonProperty(IO_FIELD_CHURCH_VERIFICATION)
    Set<ChurchVerificationDataRecord> churchRelationVerification = new HashSet<>();

    public Set<ChurchVerificationDataRecord> getChurchRelationVerification() {
        return this.churchRelationVerification;
    }

    public static final String DB_FIELD_CITIZENSHIP = "citizenship";
    public static final String IO_FIELD_CITIZENSHIP = "statsborgerskab";
    @OneToMany(mappedBy = CprBitemporalPersonRecord.DB_FIELD_ENTITY, cascade = CascadeType.ALL)
    @Filters({
            @Filter(name = Bitemporal.FILTER_EFFECT_AFTER, condition = Bitemporal.FILTERLOGIC_EFFECT_AFTER),
            @Filter(name = Bitemporal.FILTER_EFFECT_BEFORE, condition = Bitemporal.FILTERLOGIC_EFFECT_BEFORE),
            @Filter(name = Monotemporal.FILTER_REGISTRATION_AFTER, condition = Monotemporal.FILTERLOGIC_REGISTRATION_AFTER),
            @Filter(name = Monotemporal.FILTER_REGISTRATION_BEFORE, condition = Monotemporal.FILTERLOGIC_REGISTRATION_BEFORE),
            @Filter(name = Nontemporal.FILTER_LASTUPDATED_AFTER, condition = Nontemporal.FILTERLOGIC_LASTUPDATED_AFTER),
            @Filter(name = Nontemporal.FILTER_LASTUPDATED_BEFORE, condition = Nontemporal.FILTERLOGIC_LASTUPDATED_BEFORE)
    })
    @JsonProperty(IO_FIELD_CITIZENSHIP)
    Set<CitizenshipDataRecord> citizenship = new HashSet<>();

    public Set<CitizenshipDataRecord> getCitizenship() {
        return this.citizenship;
    }

    public static final String DB_FIELD_CITIZENSHIP_VERIFICATION = "citizenshipVerification";
    public static final String IO_FIELD_CITIZENSHIP_VERIFICATION = "statsborgerskab_verifikation";
    @OneToMany(mappedBy = CprBitemporalPersonRecord.DB_FIELD_ENTITY, cascade = CascadeType.ALL)
    @Filters({
            @Filter(name = Bitemporal.FILTER_EFFECT_AFTER, condition = Bitemporal.FILTERLOGIC_EFFECT_AFTER),
            @Filter(name = Bitemporal.FILTER_EFFECT_BEFORE, condition = Bitemporal.FILTERLOGIC_EFFECT_BEFORE),
            @Filter(name = Monotemporal.FILTER_REGISTRATION_AFTER, condition = Monotemporal.FILTERLOGIC_REGISTRATION_AFTER),
            @Filter(name = Monotemporal.FILTER_REGISTRATION_BEFORE, condition = Monotemporal.FILTERLOGIC_REGISTRATION_BEFORE),
            @Filter(name = Nontemporal.FILTER_LASTUPDATED_AFTER, condition = Nontemporal.FILTERLOGIC_LASTUPDATED_AFTER),
            @Filter(name = Nontemporal.FILTER_LASTUPDATED_BEFORE, condition = Nontemporal.FILTERLOGIC_LASTUPDATED_BEFORE)
    })
    @JsonProperty(IO_FIELD_CITIZENSHIP_VERIFICATION)
    Set<CitizenshipVerificationDataRecord> citizenshipVerification = new HashSet<>();

    public Set<CitizenshipVerificationDataRecord> getCitizenshipVerification() {
        return this.citizenshipVerification;
    }

    public static final String DB_FIELD_CIVILSTATUS = "civilstatus";
    public static final String IO_FIELD_CIVILSTATUS = "civilstatus";
    @OneToMany(mappedBy = CprBitemporalPersonRecord.DB_FIELD_ENTITY, cascade = CascadeType.ALL)
    @Filters({
            @Filter(name = Bitemporal.FILTER_EFFECT_AFTER, condition = Bitemporal.FILTERLOGIC_EFFECT_AFTER),
            @Filter(name = Bitemporal.FILTER_EFFECT_BEFORE, condition = Bitemporal.FILTERLOGIC_EFFECT_BEFORE),
            @Filter(name = Monotemporal.FILTER_REGISTRATION_AFTER, condition = Monotemporal.FILTERLOGIC_REGISTRATION_AFTER),
            @Filter(name = Monotemporal.FILTER_REGISTRATION_BEFORE, condition = Monotemporal.FILTERLOGIC_REGISTRATION_BEFORE),
            @Filter(name = Nontemporal.FILTER_LASTUPDATED_AFTER, condition = Nontemporal.FILTERLOGIC_LASTUPDATED_AFTER),
            @Filter(name = Nontemporal.FILTER_LASTUPDATED_BEFORE, condition = Nontemporal.FILTERLOGIC_LASTUPDATED_BEFORE)
    })
    @JsonProperty(IO_FIELD_CIVILSTATUS)
    Set<CivilStatusDataRecord> civilstatus = new HashSet<>();

    public Set<CivilStatusDataRecord> getCivilstatus() {
        return this.civilstatus;
    }

    public static final String DB_FIELD_CIVILSTATUS_AUTHORITYTEXT = "civilstatusAuthorityText";
    public static final String IO_FIELD_CIVILSTATUS_AUTHORITYTEXT = "civilstatus_autoritetstekst";
    @OneToMany(mappedBy = CprBitemporalPersonRecord.DB_FIELD_ENTITY, cascade = CascadeType.ALL)
    @Filters({
            @Filter(name = Bitemporal.FILTER_EFFECT_AFTER, condition = Bitemporal.FILTERLOGIC_EFFECT_AFTER),
            @Filter(name = Bitemporal.FILTER_EFFECT_BEFORE, condition = Bitemporal.FILTERLOGIC_EFFECT_BEFORE),
            @Filter(name = Monotemporal.FILTER_REGISTRATION_AFTER, condition = Monotemporal.FILTERLOGIC_REGISTRATION_AFTER),
            @Filter(name = Monotemporal.FILTER_REGISTRATION_BEFORE, condition = Monotemporal.FILTERLOGIC_REGISTRATION_BEFORE),
            @Filter(name = Nontemporal.FILTER_LASTUPDATED_AFTER, condition = Nontemporal.FILTERLOGIC_LASTUPDATED_AFTER),
            @Filter(name = Nontemporal.FILTER_LASTUPDATED_BEFORE, condition = Nontemporal.FILTERLOGIC_LASTUPDATED_BEFORE)
    })
    @JsonProperty(IO_FIELD_CIVILSTATUS_AUTHORITYTEXT)
    Set<CivilStatusAuthorityTextDataRecord> civilstatusAuthorityText = new HashSet<>();

    public Set<CivilStatusAuthorityTextDataRecord> getCivilstatusAuthorityText() {
        return this.civilstatusAuthorityText;
    }

    public static final String DB_FIELD_CIVILSTATUS_VERIFICATION = "civilstatusVerification";
    public static final String IO_FIELD_CIVILSTATUS_VERIFICATION = "civilstatus_verifikation";
    @OneToMany(mappedBy = CprBitemporalPersonRecord.DB_FIELD_ENTITY, cascade = CascadeType.ALL)
    @Filters({
            @Filter(name = Bitemporal.FILTER_EFFECT_AFTER, condition = Bitemporal.FILTERLOGIC_EFFECT_AFTER),
            @Filter(name = Bitemporal.FILTER_EFFECT_BEFORE, condition = Bitemporal.FILTERLOGIC_EFFECT_BEFORE),
            @Filter(name = Monotemporal.FILTER_REGISTRATION_AFTER, condition = Monotemporal.FILTERLOGIC_REGISTRATION_AFTER),
            @Filter(name = Monotemporal.FILTER_REGISTRATION_BEFORE, condition = Monotemporal.FILTERLOGIC_REGISTRATION_BEFORE),
            @Filter(name = Nontemporal.FILTER_LASTUPDATED_AFTER, condition = Nontemporal.FILTERLOGIC_LASTUPDATED_AFTER),
            @Filter(name = Nontemporal.FILTER_LASTUPDATED_BEFORE, condition = Nontemporal.FILTERLOGIC_LASTUPDATED_BEFORE)
    })
    @JsonProperty(IO_FIELD_CIVILSTATUS_VERIFICATION)
    Set<CivilStatusVerificationDataRecord> civilstatusVerification = new HashSet<>();

    public Set<CivilStatusVerificationDataRecord> getCivilstatusVerification() {
        return this.civilstatusVerification;
    }

    public static final String DB_FIELD_FOREIGN_ADDRESS = "foreignAddress";
    public static final String IO_FIELD_FOREIGN_ADDRESS = "udlandsadresse";
    @OneToMany(mappedBy = CprBitemporalPersonRecord.DB_FIELD_ENTITY, cascade = CascadeType.ALL)
    @Filters({
            @Filter(name = Bitemporal.FILTER_EFFECT_AFTER, condition = Bitemporal.FILTERLOGIC_EFFECT_AFTER),
            @Filter(name = Bitemporal.FILTER_EFFECT_BEFORE, condition = Bitemporal.FILTERLOGIC_EFFECT_BEFORE),
            @Filter(name = Monotemporal.FILTER_REGISTRATION_AFTER, condition = Monotemporal.FILTERLOGIC_REGISTRATION_AFTER),
            @Filter(name = Monotemporal.FILTER_REGISTRATION_BEFORE, condition = Monotemporal.FILTERLOGIC_REGISTRATION_BEFORE),
            @Filter(name = Nontemporal.FILTER_LASTUPDATED_AFTER, condition = Nontemporal.FILTERLOGIC_LASTUPDATED_AFTER),
            @Filter(name = Nontemporal.FILTER_LASTUPDATED_BEFORE, condition = Nontemporal.FILTERLOGIC_LASTUPDATED_BEFORE)
    })
    @JsonProperty(IO_FIELD_FOREIGN_ADDRESS)
    Set<ForeignAddressDataRecord> foreignAddress = new HashSet<>();

    public Set<ForeignAddressDataRecord> getForeignAddress() {
        return this.foreignAddress;
    }

    public static final String DB_FIELD_FOREIGN_ADDRESS_EMIGRATION = "emigration";
    public static final String IO_FIELD_FOREIGN_ADDRESS_EMIGRATION = "udrejse";
    @OneToMany(mappedBy = CprBitemporalPersonRecord.DB_FIELD_ENTITY, cascade = CascadeType.ALL)
    @Filters({
            @Filter(name = Bitemporal.FILTER_EFFECT_AFTER, condition = Bitemporal.FILTERLOGIC_EFFECT_AFTER),
            @Filter(name = Bitemporal.FILTER_EFFECT_BEFORE, condition = Bitemporal.FILTERLOGIC_EFFECT_BEFORE),
            @Filter(name = Monotemporal.FILTER_REGISTRATION_AFTER, condition = Monotemporal.FILTERLOGIC_REGISTRATION_AFTER),
            @Filter(name = Monotemporal.FILTER_REGISTRATION_BEFORE, condition = Monotemporal.FILTERLOGIC_REGISTRATION_BEFORE),
            @Filter(name = Nontemporal.FILTER_LASTUPDATED_AFTER, condition = Nontemporal.FILTERLOGIC_LASTUPDATED_AFTER),
            @Filter(name = Nontemporal.FILTER_LASTUPDATED_BEFORE, condition = Nontemporal.FILTERLOGIC_LASTUPDATED_BEFORE)
    })
    @JsonProperty(IO_FIELD_FOREIGN_ADDRESS_EMIGRATION)
    Set<ForeignAddressEmigrationDataRecord> emigration = new HashSet<>();

    public Set<ForeignAddressEmigrationDataRecord> getEmigration() {
        return this.emigration;
    }

    public static final String DB_FIELD_MOVE_MUNICIPALITY = "municipalityMove";
    public static final String IO_FIELD_MOVE_MUNICIPALITY = "kommuneflytning";
    @OneToMany(mappedBy = CprBitemporalPersonRecord.DB_FIELD_ENTITY, cascade = CascadeType.ALL)
    @Filters({
            @Filter(name = Bitemporal.FILTER_EFFECT_AFTER, condition = Bitemporal.FILTERLOGIC_EFFECT_AFTER),
            @Filter(name = Bitemporal.FILTER_EFFECT_BEFORE, condition = Bitemporal.FILTERLOGIC_EFFECT_BEFORE),
            @Filter(name = Monotemporal.FILTER_REGISTRATION_AFTER, condition = Monotemporal.FILTERLOGIC_REGISTRATION_AFTER),
            @Filter(name = Monotemporal.FILTER_REGISTRATION_BEFORE, condition = Monotemporal.FILTERLOGIC_REGISTRATION_BEFORE),
            @Filter(name = Nontemporal.FILTER_LASTUPDATED_AFTER, condition = Nontemporal.FILTERLOGIC_LASTUPDATED_AFTER),
            @Filter(name = Nontemporal.FILTER_LASTUPDATED_BEFORE, condition = Nontemporal.FILTERLOGIC_LASTUPDATED_BEFORE)
    })
    @JsonProperty(IO_FIELD_MOVE_MUNICIPALITY)
    Set<MoveMunicipalityDataRecord> municipalityMove = new HashSet<>();

    public Set<MoveMunicipalityDataRecord> getMunicipalityMove() {
        return this.municipalityMove;
    }

    public static final String DB_FIELD_NAME = "name";
    public static final String IO_FIELD_NAME = "navn";
    @OneToMany(mappedBy = CprBitemporalPersonRecord.DB_FIELD_ENTITY, cascade = CascadeType.ALL)
    @Filters({
            @Filter(name = Bitemporal.FILTER_EFFECT_AFTER, condition = Bitemporal.FILTERLOGIC_EFFECT_AFTER),
            @Filter(name = Bitemporal.FILTER_EFFECT_BEFORE, condition = Bitemporal.FILTERLOGIC_EFFECT_BEFORE),
            @Filter(name = Monotemporal.FILTER_REGISTRATION_AFTER, condition = Monotemporal.FILTERLOGIC_REGISTRATION_AFTER),
            @Filter(name = Monotemporal.FILTER_REGISTRATION_BEFORE, condition = Monotemporal.FILTERLOGIC_REGISTRATION_BEFORE),
            @Filter(name = Nontemporal.FILTER_LASTUPDATED_AFTER, condition = Nontemporal.FILTERLOGIC_LASTUPDATED_AFTER),
            @Filter(name = Nontemporal.FILTER_LASTUPDATED_BEFORE, condition = Nontemporal.FILTERLOGIC_LASTUPDATED_BEFORE)
    })
    @JsonProperty(IO_FIELD_NAME)
    Set<NameDataRecord> name = new HashSet<>();

    public Set<NameDataRecord> getName() {
        return this.name;
    }

    public static final String DB_FIELD_NAME_AUTHORITY_TEXT = "nameAuthorityText";
    public static final String IO_FIELD_NAME_AUTHORITY_TEXT = "navn_autoritetstekst";
    @OneToMany(mappedBy = CprBitemporalPersonRecord.DB_FIELD_ENTITY, cascade = CascadeType.ALL)
    @Filters({
            @Filter(name = Bitemporal.FILTER_EFFECT_AFTER, condition = Bitemporal.FILTERLOGIC_EFFECT_AFTER),
            @Filter(name = Bitemporal.FILTER_EFFECT_BEFORE, condition = Bitemporal.FILTERLOGIC_EFFECT_BEFORE),
            @Filter(name = Monotemporal.FILTER_REGISTRATION_AFTER, condition = Monotemporal.FILTERLOGIC_REGISTRATION_AFTER),
            @Filter(name = Monotemporal.FILTER_REGISTRATION_BEFORE, condition = Monotemporal.FILTERLOGIC_REGISTRATION_BEFORE),
            @Filter(name = Nontemporal.FILTER_LASTUPDATED_AFTER, condition = Nontemporal.FILTERLOGIC_LASTUPDATED_AFTER),
            @Filter(name = Nontemporal.FILTER_LASTUPDATED_BEFORE, condition = Nontemporal.FILTERLOGIC_LASTUPDATED_BEFORE)
    })
    @JsonProperty(IO_FIELD_NAME_AUTHORITY_TEXT)
    Set<NameAuthorityTextDataRecord> nameAuthorityText = new HashSet<>();

    public Set<NameAuthorityTextDataRecord> getNameAuthorityText() {
        return this.nameAuthorityText;
    }

    public static final String DB_FIELD_NAME_VERIFICATION = "nameVerification";
    public static final String IO_FIELD_NAME_VERIFICATION = "navn_verifikation";
    @OneToMany(mappedBy = CprBitemporalPersonRecord.DB_FIELD_ENTITY, cascade = CascadeType.ALL)
    @Filters({
            @Filter(name = Bitemporal.FILTER_EFFECT_AFTER, condition = Bitemporal.FILTERLOGIC_EFFECT_AFTER),
            @Filter(name = Bitemporal.FILTER_EFFECT_BEFORE, condition = Bitemporal.FILTERLOGIC_EFFECT_BEFORE),
            @Filter(name = Monotemporal.FILTER_REGISTRATION_AFTER, condition = Monotemporal.FILTERLOGIC_REGISTRATION_AFTER),
            @Filter(name = Monotemporal.FILTER_REGISTRATION_BEFORE, condition = Monotemporal.FILTERLOGIC_REGISTRATION_BEFORE),
            @Filter(name = Nontemporal.FILTER_LASTUPDATED_AFTER, condition = Nontemporal.FILTERLOGIC_LASTUPDATED_AFTER),
            @Filter(name = Nontemporal.FILTER_LASTUPDATED_BEFORE, condition = Nontemporal.FILTERLOGIC_LASTUPDATED_BEFORE)
    })
    @JsonProperty(IO_FIELD_NAME_VERIFICATION)
    Set<NameVerificationDataRecord> nameVerification = new HashSet<>();

    public Set<NameVerificationDataRecord> getNameVerification() {
        return this.nameVerification;
    }

    public static final String DB_FIELD_MOTHER = "mother";
    public static final String IO_FIELD_MOTHER = "mor";
    @OneToMany(mappedBy = CprBitemporalPersonRecord.DB_FIELD_ENTITY, cascade = CascadeType.ALL)
    @Where(clause = ParentDataRecord.DB_FIELD_IS_MOTHER + "=true")
    @Filters({
            @Filter(name = Bitemporal.FILTER_EFFECT_AFTER, condition = Bitemporal.FILTERLOGIC_EFFECT_AFTER),
            @Filter(name = Bitemporal.FILTER_EFFECT_BEFORE, condition = Bitemporal.FILTERLOGIC_EFFECT_BEFORE),
            @Filter(name = Monotemporal.FILTER_REGISTRATION_AFTER, condition = Monotemporal.FILTERLOGIC_REGISTRATION_AFTER),
            @Filter(name = Monotemporal.FILTER_REGISTRATION_BEFORE, condition = Monotemporal.FILTERLOGIC_REGISTRATION_BEFORE),
            @Filter(name = Nontemporal.FILTER_LASTUPDATED_AFTER, condition = Nontemporal.FILTERLOGIC_LASTUPDATED_AFTER),
            @Filter(name = Nontemporal.FILTER_LASTUPDATED_BEFORE, condition = Nontemporal.FILTERLOGIC_LASTUPDATED_BEFORE)
    })
    @JsonProperty(IO_FIELD_MOTHER)
    Set<ParentDataRecord> mother = new HashSet<>();

    public Set<ParentDataRecord> getMother() {
        return this.mother;
    }

    public static final String DB_FIELD_MOTHER_VERIFICATION = "motherVerification";
    public static final String IO_FIELD_MOTHER_VERIFICATION = "mor_verifikation";
    @OneToMany(mappedBy = CprBitemporalPersonRecord.DB_FIELD_ENTITY, cascade = CascadeType.ALL)
    @Where(clause = ParentDataRecord.DB_FIELD_IS_MOTHER + "=true")
    @Filters({
            @Filter(name = Bitemporal.FILTER_EFFECT_AFTER, condition = Bitemporal.FILTERLOGIC_EFFECT_AFTER),
            @Filter(name = Bitemporal.FILTER_EFFECT_BEFORE, condition = Bitemporal.FILTERLOGIC_EFFECT_BEFORE),
            @Filter(name = Monotemporal.FILTER_REGISTRATION_AFTER, condition = Monotemporal.FILTERLOGIC_REGISTRATION_AFTER),
            @Filter(name = Monotemporal.FILTER_REGISTRATION_BEFORE, condition = Monotemporal.FILTERLOGIC_REGISTRATION_BEFORE),
            @Filter(name = Nontemporal.FILTER_LASTUPDATED_AFTER, condition = Nontemporal.FILTERLOGIC_LASTUPDATED_AFTER),
            @Filter(name = Nontemporal.FILTER_LASTUPDATED_BEFORE, condition = Nontemporal.FILTERLOGIC_LASTUPDATED_BEFORE)
    })
    @JsonProperty(IO_FIELD_MOTHER_VERIFICATION)
    Set<ParentVerificationDataRecord> motherVerification = new HashSet<>();

    public Set<ParentVerificationDataRecord> getMotherVerification() {
        return this.motherVerification;
    }

    public static final String DB_FIELD_FATHER = "father";
    public static final String IO_FIELD_FATHER = "far";
    @OneToMany(mappedBy = CprBitemporalPersonRecord.DB_FIELD_ENTITY, cascade = CascadeType.ALL)
    @Where(clause = ParentDataRecord.DB_FIELD_IS_MOTHER + "=false")
    @Filters({
            @Filter(name = Bitemporal.FILTER_EFFECT_AFTER, condition = Bitemporal.FILTERLOGIC_EFFECT_AFTER),
            @Filter(name = Bitemporal.FILTER_EFFECT_BEFORE, condition = Bitemporal.FILTERLOGIC_EFFECT_BEFORE),
            @Filter(name = Monotemporal.FILTER_REGISTRATION_AFTER, condition = Monotemporal.FILTERLOGIC_REGISTRATION_AFTER),
            @Filter(name = Monotemporal.FILTER_REGISTRATION_BEFORE, condition = Monotemporal.FILTERLOGIC_REGISTRATION_BEFORE),
            @Filter(name = Nontemporal.FILTER_LASTUPDATED_AFTER, condition = Nontemporal.FILTERLOGIC_LASTUPDATED_AFTER),
            @Filter(name = Nontemporal.FILTER_LASTUPDATED_BEFORE, condition = Nontemporal.FILTERLOGIC_LASTUPDATED_BEFORE)
    })
    @JsonProperty(IO_FIELD_FATHER)
    Set<ParentDataRecord> father = new HashSet<>();

    public Set<ParentDataRecord> getFather() {
        return this.father;
    }

    public static final String DB_FIELD_FATHER_VERIFICATION = "fatherVerification";
    public static final String IO_FIELD_FATHER_VERIFICATION = "far_verifikation";
    @OneToMany(mappedBy = CprBitemporalPersonRecord.DB_FIELD_ENTITY, cascade = CascadeType.ALL)
    @Where(clause = ParentDataRecord.DB_FIELD_IS_MOTHER + "=false")
    @Filters({
            @Filter(name = Bitemporal.FILTER_EFFECT_AFTER, condition = Bitemporal.FILTERLOGIC_EFFECT_AFTER),
            @Filter(name = Bitemporal.FILTER_EFFECT_BEFORE, condition = Bitemporal.FILTERLOGIC_EFFECT_BEFORE),
            @Filter(name = Monotemporal.FILTER_REGISTRATION_AFTER, condition = Monotemporal.FILTERLOGIC_REGISTRATION_AFTER),
            @Filter(name = Monotemporal.FILTER_REGISTRATION_BEFORE, condition = Monotemporal.FILTERLOGIC_REGISTRATION_BEFORE),
            @Filter(name = Nontemporal.FILTER_LASTUPDATED_AFTER, condition = Nontemporal.FILTERLOGIC_LASTUPDATED_AFTER),
            @Filter(name = Nontemporal.FILTER_LASTUPDATED_BEFORE, condition = Nontemporal.FILTERLOGIC_LASTUPDATED_BEFORE)
    })
    @JsonProperty(IO_FIELD_FATHER_VERIFICATION)
    Set<ParentVerificationDataRecord> fatherVerification = new HashSet<>();

    public Set<ParentVerificationDataRecord> getFatherVerification() {
        return this.fatherVerification;
    }

    public static final String DB_FIELD_CORE = "person";
    public static final String IO_FIELD_CORE = "kernedata";
    @OneToMany(mappedBy = CprBitemporalPersonRecord.DB_FIELD_ENTITY, cascade = CascadeType.ALL)
    @Filters({
            @Filter(name = Bitemporal.FILTER_EFFECT_AFTER, condition = Bitemporal.FILTERLOGIC_EFFECT_AFTER),
            @Filter(name = Bitemporal.FILTER_EFFECT_BEFORE, condition = Bitemporal.FILTERLOGIC_EFFECT_BEFORE),
            @Filter(name = Monotemporal.FILTER_REGISTRATION_AFTER, condition = Monotemporal.FILTERLOGIC_REGISTRATION_AFTER),
            @Filter(name = Monotemporal.FILTER_REGISTRATION_BEFORE, condition = Monotemporal.FILTERLOGIC_REGISTRATION_BEFORE),
            @Filter(name = Nontemporal.FILTER_LASTUPDATED_AFTER, condition = Nontemporal.FILTERLOGIC_LASTUPDATED_AFTER),
            @Filter(name = Nontemporal.FILTER_LASTUPDATED_BEFORE, condition = Nontemporal.FILTERLOGIC_LASTUPDATED_BEFORE)
    })
    @JsonProperty(IO_FIELD_CORE)
    Set<PersonCoreDataRecord> core = new HashSet<>();

    public Set<PersonCoreDataRecord> getCore() {
        return this.core;
    }

    public static final String DB_FIELD_PNR = "personNumber";
    public static final String IO_FIELD_PNR = "historiskPersonnummer";
    @OneToMany(mappedBy = CprBitemporalPersonRecord.DB_FIELD_ENTITY, cascade = CascadeType.ALL)
    @Filters({
            @Filter(name = Bitemporal.FILTER_EFFECT_AFTER, condition = Bitemporal.FILTERLOGIC_EFFECT_AFTER),
            @Filter(name = Bitemporal.FILTER_EFFECT_BEFORE, condition = Bitemporal.FILTERLOGIC_EFFECT_BEFORE),
            @Filter(name = Monotemporal.FILTER_REGISTRATION_AFTER, condition = Monotemporal.FILTERLOGIC_REGISTRATION_AFTER),
            @Filter(name = Monotemporal.FILTER_REGISTRATION_BEFORE, condition = Monotemporal.FILTERLOGIC_REGISTRATION_BEFORE),
            @Filter(name = Nontemporal.FILTER_LASTUPDATED_AFTER, condition = Nontemporal.FILTERLOGIC_LASTUPDATED_AFTER),
            @Filter(name = Nontemporal.FILTER_LASTUPDATED_BEFORE, condition = Nontemporal.FILTERLOGIC_LASTUPDATED_BEFORE)
    })
    @JsonProperty(IO_FIELD_PNR)
    Set<PersonNumberDataRecord> personNumber = new HashSet<>();

    public Set<PersonNumberDataRecord> getPersonNumber() {
        return this.personNumber;
    }

    public static final String DB_FIELD_POSITION = "position";
    public static final String IO_FIELD_POSITION = "stilling";
    @OneToMany(mappedBy = CprBitemporalPersonRecord.DB_FIELD_ENTITY, cascade = CascadeType.ALL)
    @Filters({
            @Filter(name = Bitemporal.FILTER_EFFECT_AFTER, condition = Bitemporal.FILTERLOGIC_EFFECT_AFTER),
            @Filter(name = Bitemporal.FILTER_EFFECT_BEFORE, condition = Bitemporal.FILTERLOGIC_EFFECT_BEFORE),
            @Filter(name = Monotemporal.FILTER_REGISTRATION_AFTER, condition = Monotemporal.FILTERLOGIC_REGISTRATION_AFTER),
            @Filter(name = Monotemporal.FILTER_REGISTRATION_BEFORE, condition = Monotemporal.FILTERLOGIC_REGISTRATION_BEFORE),
            @Filter(name = Nontemporal.FILTER_LASTUPDATED_AFTER, condition = Nontemporal.FILTERLOGIC_LASTUPDATED_AFTER),
            @Filter(name = Nontemporal.FILTER_LASTUPDATED_BEFORE, condition = Nontemporal.FILTERLOGIC_LASTUPDATED_BEFORE)
    })
    @JsonProperty(IO_FIELD_POSITION)
    Set<PersonPositionDataRecord> position = new HashSet<>();

    public Set<PersonPositionDataRecord> getPosition() {
        return this.position;
    }

    public static final String DB_FIELD_STATUS = "status";
    public static final String IO_FIELD_STATUS = "status";
    @OneToMany(mappedBy = CprBitemporalPersonRecord.DB_FIELD_ENTITY, cascade = CascadeType.ALL)
    @Filters({
            @Filter(name = Bitemporal.FILTER_EFFECT_AFTER, condition = Bitemporal.FILTERLOGIC_EFFECT_AFTER),
            @Filter(name = Bitemporal.FILTER_EFFECT_BEFORE, condition = Bitemporal.FILTERLOGIC_EFFECT_BEFORE),
            @Filter(name = Monotemporal.FILTER_REGISTRATION_AFTER, condition = Monotemporal.FILTERLOGIC_REGISTRATION_AFTER),
            @Filter(name = Monotemporal.FILTER_REGISTRATION_BEFORE, condition = Monotemporal.FILTERLOGIC_REGISTRATION_BEFORE),
            @Filter(name = Nontemporal.FILTER_LASTUPDATED_AFTER, condition = Nontemporal.FILTERLOGIC_LASTUPDATED_AFTER),
            @Filter(name = Nontemporal.FILTER_LASTUPDATED_BEFORE, condition = Nontemporal.FILTERLOGIC_LASTUPDATED_BEFORE)
    })
    @JsonProperty(IO_FIELD_STATUS)
    Set<PersonStatusDataRecord> status = new HashSet<>();

    public Set<PersonStatusDataRecord> getStatus() {
        return this.status;
    }

    public static final String DB_FIELD_PROTECTION = "protection";
    public static final String IO_FIELD_PROTECTION = "beskyttelse";
    @OneToMany(mappedBy = CprBitemporalPersonRecord.DB_FIELD_ENTITY, cascade = CascadeType.ALL)
    @Filters({
            @Filter(name = Bitemporal.FILTER_EFFECT_AFTER, condition = Bitemporal.FILTERLOGIC_EFFECT_AFTER),
            @Filter(name = Bitemporal.FILTER_EFFECT_BEFORE, condition = Bitemporal.FILTERLOGIC_EFFECT_BEFORE),
            @Filter(name = Monotemporal.FILTER_REGISTRATION_AFTER, condition = Monotemporal.FILTERLOGIC_REGISTRATION_AFTER),
            @Filter(name = Monotemporal.FILTER_REGISTRATION_BEFORE, condition = Monotemporal.FILTERLOGIC_REGISTRATION_BEFORE),
            @Filter(name = Nontemporal.FILTER_LASTUPDATED_AFTER, condition = Nontemporal.FILTERLOGIC_LASTUPDATED_AFTER),
            @Filter(name = Nontemporal.FILTER_LASTUPDATED_BEFORE, condition = Nontemporal.FILTERLOGIC_LASTUPDATED_BEFORE)
    })
    @JsonProperty(IO_FIELD_PROTECTION)
    Set<ProtectionDataRecord> protection = new HashSet<>();

    public Set<ProtectionDataRecord> getProtection() {
        return this.protection;
    }
	
	public static final String DB_FIELD_GUARDIAN = "guardian";
    public static final String IO_FIELD_GUARDIAN = "værgemål";
    @OneToMany(mappedBy = CprBitemporalPersonRecord.DB_FIELD_ENTITY, cascade = CascadeType.ALL)
    @Filters({
            @Filter(name = Bitemporal.FILTER_EFFECT_AFTER, condition = Bitemporal.FILTERLOGIC_EFFECT_AFTER),
            @Filter(name = Bitemporal.FILTER_EFFECT_BEFORE, condition = Bitemporal.FILTERLOGIC_EFFECT_BEFORE),
            @Filter(name = Monotemporal.FILTER_REGISTRATION_AFTER, condition = Monotemporal.FILTERLOGIC_REGISTRATION_AFTER),
            @Filter(name = Monotemporal.FILTER_REGISTRATION_BEFORE, condition = Monotemporal.FILTERLOGIC_REGISTRATION_BEFORE),
            @Filter(name = Nontemporal.FILTER_LASTUPDATED_AFTER, condition = Nontemporal.FILTERLOGIC_LASTUPDATED_AFTER),
            @Filter(name = Nontemporal.FILTER_LASTUPDATED_BEFORE, condition = Nontemporal.FILTERLOGIC_LASTUPDATED_BEFORE)
    })
    @JsonProperty(IO_FIELD_GUARDIAN)
    Set<GuardianDataRecord> guardian = new HashSet<>();

    public Set<GuardianDataRecord> getGuardian() {
        return this.guardian;
    }

    public void addBitemporalRecord(CprBitemporalPersonRecord record, Session session) {
        boolean added = false;
        if (record instanceof AddressConameDataRecord) {
            added = addItem(this.coname, record, session);
        }
        if (record instanceof AddressDataRecord) {
            added = addItem(this.address, record, session);
        }
        if (record instanceof AddressNameDataRecord) {
            added = addItem(this.addressName, record, session);
        }
        if (record instanceof BirthPlaceDataRecord) {
            added = addItem(this.birthPlace, record, session);
        }
        if (record instanceof BirthPlaceVerificationDataRecord) {
            added = addItem(this.birthPlaceVerification, record, session);
        }
        if (record instanceof BirthTimeDataRecord) {
            added = addItem(this.birthTime, record, session);
        }
        if (record instanceof ChurchDataRecord) {
            added = addItem(this.churchRelation, record, session);
        }
        if (record instanceof ChurchVerificationDataRecord) {
            added = addItem(this.churchRelationVerification, record, session);
        }
        if (record instanceof CitizenshipDataRecord) {
            added = addItem(this.citizenship, record, session);
        }
        if (record instanceof CitizenshipVerificationDataRecord) {
            added = addItem(this.citizenshipVerification, record, session);
        }
        if (record instanceof CivilStatusDataRecord) {
            added = addItem(this.civilstatus, record, session);
        }
        if (record instanceof CivilStatusAuthorityTextDataRecord) {
            added = addItem(this.civilstatusAuthorityText, record, session);
        }
        if (record instanceof CivilStatusVerificationDataRecord) {
            added = addItem(this.civilstatusVerification, record, session);
        }
        if (record instanceof ForeignAddressDataRecord) {
            added = addItem(this.foreignAddress, record, session);
        }
        if (record instanceof ForeignAddressEmigrationDataRecord) {
            added = addItem(this.emigration, record, session);
        }
        if (record instanceof MoveMunicipalityDataRecord) {
            added = addItem(this.municipalityMove, record, session);
        }
        if (record instanceof NameAuthorityTextDataRecord) {
            added = addItem(this.nameAuthorityText, record, session);
        }
        if (record instanceof NameDataRecord) {
            added = addItem(this.name, record, session);
        }
        if (record instanceof NameVerificationDataRecord) {
            added = addItem(this.nameVerification, record, session);
        }
        if (record instanceof ParentDataRecord) {
            ParentDataRecord pRecord = (ParentDataRecord) record;
            if (pRecord.isMother()) {
                added = addItem(this.mother, pRecord, session);
            } else {
                added = addItem(this.father, pRecord, session);
            }
        }
        if (record instanceof ParentVerificationDataRecord) {
            ParentVerificationDataRecord pRecord = (ParentVerificationDataRecord) record;
            if (pRecord.isMother()) {
                added = addItem(this.motherVerification, pRecord, session);
            } else {
                added = addItem(this.fatherVerification, pRecord, session);
            }
        }
        if (record instanceof PersonCoreDataRecord) {
            added = addItem(this.core, record, session);
        }
        if (record instanceof PersonNumberDataRecord) {
            added = addItem(this.personNumber, record, session);
        }
        if (record instanceof PersonPositionDataRecord) {
            added = addItem(this.position, record, session);
        }
        if (record instanceof PersonStatusDataRecord) {
            added = addItem(this.status, record, session);
        }
        if (record instanceof ProtectionDataRecord) {
            added = addItem(this.protection, record, session);
        }
        if (added) {
            record.setEntity(this);
        }
    }

    private static Logger log = LogManager.getLogger("PersonEntity");
    private static <E extends CprBitemporalPersonRecord> boolean addItem(Set<E> set, CprBitemporalPersonRecord newItem, Session session) {
        log.debug("Add "+newItem.getClass().getSimpleName()+"("+newItem.getAuthority()+") at "+newItem.getBitemporality()+" to set with "+set.size()+" preexisting entries");
        if (newItem != null) {
            for (E oldItem : set) {
                if (newItem.equalData(oldItem)) {
                    if (
                            newItem.isHistoric() && !oldItem.isHistoric() &&
                            Equality.equal(newItem.getRegistrationFrom(), oldItem.getRegistrationFrom()) &&
                            Equality.equal(newItem.getEffectFrom(), oldItem.getEffectFrom()) && oldItem.getEffectTo() == null &&
                            !Equality.equal(newItem.getEffectFrom(), newItem.getEffectTo())
                            ) {
                        log.debug("matching item at " + oldItem.getBitemporality() + ", removing preexisting (" + oldItem.getAuthority() + ")");
                        set.remove(oldItem);
                        session.delete(oldItem);
                        return set.add((E) newItem);

                    } else if (newItem.getBitemporality().equals(oldItem.getBitemporality())) {
                        log.debug("matching item with same temporality (" + newItem.getBitemporality() + "), replacing (" + oldItem.getAuthority() + ")");
                        set.remove(oldItem);
                        session.delete(oldItem);
                        return set.add((E) newItem);

                    } else if (
                            Equality.equal(newItem.getRegistrationFrom(), oldItem.getRegistrationFrom()) &&
                            (Equality.equal(newItem.getRegistrationTo(), oldItem.getRegistrationTo()) || newItem.getRegistrationTo() == null) &&
                            Equality.equal(newItem.getEffectFrom(), oldItem.getEffectFrom()) &&
                            newItem.getEffectTo() == null
                            ) {
                        log.debug("matching item with insufficient temporality (" + newItem.getBitemporality() + "), not adding");
                        return false;
                    }
                }

            }
            log.debug("nonmatching item, adding as new");
            return set.add((E) newItem);
        }
        return false;
    }

    @JsonIgnore
    public Set<CprBitemporalPersonRecord> getBitemporalRecords() {
        HashSet<CprBitemporalPersonRecord> records = new HashSet<>();
        records.addAll(this.coname);
        records.addAll(this.address);
        records.addAll(this.addressName);
        records.addAll(this.birthPlace);
        records.addAll(this.birthPlaceVerification);
        records.addAll(this.birthTime);
        records.addAll(this.churchRelation);
        records.addAll(this.churchRelationVerification);
        records.addAll(this.citizenship);
        records.addAll(this.citizenshipVerification);
        records.addAll(this.civilstatus);
        records.addAll(this.civilstatusAuthorityText);
        records.addAll(this.civilstatusVerification);
        records.addAll(this.foreignAddress);
        records.addAll(this.emigration);
        records.addAll(this.municipalityMove);
        records.addAll(this.nameAuthorityText);
        records.addAll(this.nameVerification);
        records.addAll(this.mother);
        records.addAll(this.motherVerification);
        records.addAll(this.father);
        records.addAll(this.fatherVerification);
        records.addAll(this.core);
        records.addAll(this.personNumber);
        records.addAll(this.position);
        records.addAll(this.status);
        records.addAll(this.protection);
        return records;
    }
}
