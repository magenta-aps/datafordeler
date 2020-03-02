package dk.magenta.datafordeler.cpr.data.person.data;

import com.fasterxml.jackson.annotation.JsonProperty;
import dk.magenta.datafordeler.core.database.DataItem;
import dk.magenta.datafordeler.core.database.DatabaseEntry;
import dk.magenta.datafordeler.core.database.LookupDefinition;
import dk.magenta.datafordeler.cpr.CprPlugin;
import dk.magenta.datafordeler.cpr.data.CprData;
import dk.magenta.datafordeler.cpr.data.DetailData;
import dk.magenta.datafordeler.cpr.data.person.PersonEffect;
import org.hibernate.Session;

import javax.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.*;

/**
 * Base class for Person data, linking to Effects and delegating storage to referred classes
 */
@Entity
@Table(name = PersonBaseData.TABLE_NAME, indexes = {
        @Index(name = PersonBaseData.TABLE_NAME + "__lastUpdated", columnList = DataItem.DB_FIELD_LAST_UPDATED),
        @Index(name = PersonBaseData.TABLE_NAME + "__name", columnList = PersonBaseData.DB_FIELD_NAME + DatabaseEntry.REF),
        @Index(name = PersonBaseData.TABLE_NAME + "__address", columnList = PersonBaseData.DB_FIELD_ADDRESS + DatabaseEntry.REF),
        @Index(name = PersonBaseData.TABLE_NAME + "__status", columnList = PersonBaseData.DB_FIELD_STATUS + DatabaseEntry.REF),
        @Index(name = PersonBaseData.TABLE_NAME + "__birth", columnList = PersonBaseData.DB_FIELD_BIRTH + DatabaseEntry.REF)
})
public class PersonBaseData extends CprData<PersonEffect, PersonBaseData> {

    public static final String TABLE_NAME = CprPlugin.DEBUG_TABLE_PREFIX + "cpr_person_data";

    public static final String DB_FIELD_CORE = "coreData";
    @OneToOne(optional = true, cascade = CascadeType.ALL)
    @JoinColumn(name = DB_FIELD_CORE + DatabaseEntry.REF)
    private PersonCoreData coreData;

    public static final String DB_FIELD_STATUS = "status";
    @OneToOne(optional = true, cascade = CascadeType.ALL)
    @JoinColumn(name = DB_FIELD_STATUS + DatabaseEntry.REF)
    private PersonStatusData status;

    public static final String DB_FIELD_MOTHER = "mother";
    public static final String IO_FIELD_MOTHER = "mor";
    @OneToOne(optional = true, cascade = CascadeType.ALL)
    @JoinColumn(name = DB_FIELD_MOTHER + DatabaseEntry.REF)
    private PersonParentData mother;

    public static final String DB_FIELD_FATHER = "father";
    public static final String IO_FIELD_FATHER = "far";
    @OneToOne(optional = true, cascade = CascadeType.ALL)
    @JoinColumn(name = DB_FIELD_FATHER + DatabaseEntry.REF)
    private PersonParentData father;

    public static final String DB_FIELD_MOTHERVERIFICATION = "motherVerification";
    public static final String IO_FIELD_MOTHERVERIFICATION = "morVerification";
    @OneToOne(optional = true, cascade = CascadeType.ALL)
    @JoinColumn(name = DB_FIELD_MOTHERVERIFICATION + DatabaseEntry.REF)
    private PersonParentVerificationData motherVerification;

    public static final String DB_FIELD_FATHERVERIFICATION = "fatherVerification";
    public static final String IO_FIELD_FATHERVERIFICATION = "farVerification";
    @OneToOne(optional = true, cascade = CascadeType.ALL)
    @JoinColumn(name = DB_FIELD_FATHERVERIFICATION + DatabaseEntry.REF)
    private PersonParentVerificationData fatherVerification;

    public static final String DB_FIELD_POSITION = "position";
    @OneToOne(optional = true, cascade = CascadeType.ALL)
    @JoinColumn(name = DB_FIELD_POSITION + DatabaseEntry.REF)
    private PersonPositionData position;

    public static final String DB_FIELD_BIRTH = "birth";
    public static final String IO_FIELD_BIRTH = "fødsel";
    @OneToOne(optional = true, cascade = CascadeType.ALL)
    @JoinColumn(name = DB_FIELD_BIRTH + DatabaseEntry.REF)
    private PersonBirthData birth;

    public static final String DB_FIELD_BIRTH_VERIFICATION = "birthVerification";
    @OneToOne(optional = true, cascade = CascadeType.ALL)
    @JoinColumn(name = DB_FIELD_BIRTH_VERIFICATION + DatabaseEntry.REF)
    private PersonBirthVerificationData birthVerification;

    public static final String DB_FIELD_CHURCH = "church";
    public static final String IO_FIELD_CHURCH = "folkekirke";
    @OneToOne(optional = true, cascade = CascadeType.ALL)
    @JoinColumn(name = DB_FIELD_CHURCH + DatabaseEntry.REF)
    private PersonChurchData church;

    public static final String DB_FIELD_CHURCH_VERIFICATION = "churchVerification";
    @OneToOne(optional = true, cascade = CascadeType.ALL)
    @JoinColumn(name = DB_FIELD_CHURCH_VERIFICATION + DatabaseEntry.REF)
    private PersonChurchVerificationData churchVerification;


    public static final String DB_FIELD_CITIZENSHIP = "citizenship";
    public static final String IO_FIELD_CITIZENSHIP = "statsborgerskab";
    @OneToOne(optional = true, cascade = CascadeType.ALL)
    @JoinColumn(name = DB_FIELD_CITIZENSHIP + DatabaseEntry.REF)
    private PersonCitizenshipData citizenship;

    public static final String DB_FIELD_CITIZENSHIP_VERIFICATION = "citizenshipVerification";
    @OneToOne(optional = true, cascade = CascadeType.ALL)
    @JoinColumn(name = DB_FIELD_CITIZENSHIP_VERIFICATION + DatabaseEntry.REF)
    private PersonCitizenshipVerificationData citizenshipVerification;



    public static final String DB_FIELD_ADDRESS = "address";
    public static final String IO_FIELD_ADDRESS = "cpradresse";
    @OneToOne(optional = true, cascade = CascadeType.ALL)
    @JoinColumn(name = DB_FIELD_ADDRESS + DatabaseEntry.REF)
    private PersonAddressData address;

    public static final String DB_FIELD_CONAME = "coname";
    @OneToOne(optional = true, cascade = CascadeType.ALL)
    @JoinColumn(name = DB_FIELD_CONAME + DatabaseEntry.REF)
    private PersonAddressConameData coname;

    public static final String DB_FIELD_MOVEMUNICIPALITY = "moveMunicipality";
    @OneToOne(optional = true, cascade = CascadeType.ALL)
    @JoinColumn(name = DB_FIELD_MOVEMUNICIPALITY + DatabaseEntry.REF)
    private PersonMoveMunicipalityData moveMunicipality;

    public static final String DB_FIELD_NAME = "name";
    public static final String IO_FIELD_NAME = "navn";
    @OneToOne(optional = true, cascade = CascadeType.ALL)
    @JoinColumn(name = DB_FIELD_NAME + DatabaseEntry.REF)
    private PersonNameData name;

    public static final String DB_FIELD_ADDRESSING_NAME = "addressingName";
    public static final String IO_FIELD_ADDRESSING_NAME = "adresseringsnavn";
    @OneToOne(optional = true, cascade = CascadeType.ALL)
    @JoinColumn(name = DB_FIELD_ADDRESSING_NAME + DatabaseEntry.REF)
    private PersonAddressNameData addressingName;

    public static final String DB_FIELD_NAME_VERIFICATION = "nameVerification";
    @OneToOne(optional = true, cascade = CascadeType.ALL)
    @JoinColumn(name = DB_FIELD_NAME_VERIFICATION + DatabaseEntry.REF)
    private PersonNameVerificationData nameVerification;

    public static final String DB_FIELD_NAME_AUTHORITY = "nameAuthority";
    public static final String IO_FIELD_NAME_AUTHORITY = "navnemyndighed";
    @OneToOne(optional = true, cascade = CascadeType.ALL)
    @JoinColumn(name = DB_FIELD_NAME_AUTHORITY + DatabaseEntry.REF)
    private PersonNameAuthorityTextData nameAuthority;

    public static final String DB_FIELD_PROTECTION = "protection";
    public static final String IO_FIELD_PROTECTION = "beskyttelse";
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "personBaseData")
    private Set<PersonProtectionData> protection = new HashSet<>();

    public static final String DB_FIELD_MIGRATION = "migration";
    @JsonProperty(value = DB_FIELD_MIGRATION)
    @OneToOne(optional = true, cascade = CascadeType.ALL)
    @JoinColumn(name = DB_FIELD_MIGRATION + DatabaseEntry.REF)
    private PersonEmigrationData migration;

    public static final String DB_FIELD_FOREIGN_ADDRESS = "foreignAddress";
    @OneToOne(optional = true, cascade = CascadeType.ALL)
    @JoinColumn(name = DB_FIELD_FOREIGN_ADDRESS + DatabaseEntry.REF)
    private PersonForeignAddressData foreignAddress;

    public static final String DB_FIELD_CIVIL_STATUS = "civilStatus";
    @OneToOne(optional = true, cascade = CascadeType.ALL)
    @JoinColumn(name = DB_FIELD_CIVIL_STATUS + DatabaseEntry.REF)
    private PersonCivilStatusData civilStatus;

    public static final String DB_FIELD_CIVIL_STATUS_VERIFICATION = "civilStatusVerification";
    @OneToOne(optional = true, cascade = CascadeType.ALL)
    @JoinColumn(name = DB_FIELD_CIVIL_STATUS_VERIFICATION + DatabaseEntry.REF)
    private PersonCivilStatusVerificationData civilStatusVerification;

    public static final String DB_FIELD_CIVIL_STATUS_AUTHORITY = "civilStatusAuthority";
    @OneToOne(optional = true, cascade = CascadeType.ALL)
    @JoinColumn(name = DB_FIELD_CIVIL_STATUS_AUTHORITY + DatabaseEntry.REF)
    private PersonCivilStatusAuthorityTextData civilStatusAuthority;

    public static final String DB_FIELD_CPRNUMBER = "cprNumber";
    @OneToOne(optional = true, cascade = CascadeType.ALL)
    @JoinColumn(name = DB_FIELD_CPRNUMBER + DatabaseEntry.REF)
    private PersonNumberData cprNumber;



    public PersonCoreData getCoreData() {
        return coreData;
    }

    public PersonStatusData getStatus() {
        return status;
    }

    public PersonParentData getMother() {
        return mother;
    }

    public PersonParentData getFather() {
        return father;
    }

    public PersonParentVerificationData getMotherVerification() {
        return motherVerification;
    }

    public PersonParentVerificationData getFatherVerification() {
        return fatherVerification;
    }

    public PersonPositionData getPosition() {
        return position;
    }

    public PersonBirthData getBirth() {
        return birth;
    }

    public PersonBirthVerificationData getBirthVerification() {
        return this.birthVerification;
    }

    public PersonChurchData getChurch() {
        return this.church;
    }

    public PersonChurchVerificationData getChurchVerification() {
        return this.churchVerification;
    }

    public PersonCitizenshipData getCitizenship() {
        return this.citizenship;
    }

    public PersonCitizenshipVerificationData getCitizenshipVerification() {
        return this.citizenshipVerification;
    }

    public PersonAddressData getAddress() {
        return address;
    }

    public PersonAddressConameData getConame() {
        return coname;
    }

    public PersonMoveMunicipalityData getMoveMunicipality() {
        return moveMunicipality;
    }

    public PersonNameData getName() {
        return name;
    }

    public PersonAddressNameData getAddressingName() {
        return addressingName;
    }

    public PersonNameVerificationData getNameVerification() {
        return nameVerification;
    }

    public PersonNameAuthorityTextData getNameAuthority() {
        return nameAuthority;
    }

    public Collection<PersonProtectionData> getProtection() {
        return protection;
    }

    public PersonEmigrationData getMigration() {
        return migration;
    }

    public PersonForeignAddressData getForeignAddress() {
        return foreignAddress;
    }

    public PersonCivilStatusData getCivilStatus() {
        return this.civilStatus;
    }

    public PersonCivilStatusVerificationData getCivilStatusVerification() {
        return this.civilStatusVerification;
    }

    public PersonCivilStatusAuthorityTextData getCivilStatusAuthority() {
        return this.civilStatusAuthority;
    }

    public PersonNumberData getCprNumber() {
        return this.cprNumber;
    }

    public void setPersonnummer(String personnummer, OffsetDateTime updateTime) {
        if (this.coreData == null) {
            this.coreData = new PersonCoreData();
        }
        this.coreData.setCprNumber(personnummer);
        this.coreData.setDafoUpdated(updateTime);
    }

    public void setKoen(String koen, OffsetDateTime updateTime) {
        if (this.coreData == null) {
            this.coreData = new PersonCoreData();
        }
        this.coreData.setKoen(koen);
        this.coreData.setDafoUpdated(updateTime);
    }

    public void setKoen(PersonCoreData.Koen koen, OffsetDateTime updateTime) {
        if (this.coreData == null) {
            this.coreData = new PersonCoreData();
        }
        this.coreData.setGender(koen);
        this.coreData.setDafoUpdated(updateTime);
    }

    public void setStartAuthority(int authority, OffsetDateTime updateTime) {
        if (this.coreData == null) {
            this.coreData = new PersonCoreData();
        }
        this.coreData.setAuthority(authority);
        this.coreData.setDafoUpdated(updateTime);
    }

    public void setStatus(int status, OffsetDateTime updateTime) {
        if (this.status == null) {
            this.status = new PersonStatusData();
        }
        this.status.setStatus(status);
        this.status.setDafoUpdated(updateTime);
    }

    public void setMother(String name, boolean nameMarking, String cprNumber, LocalDate birthDate, boolean birthDateUncertain, int authorityCode, OffsetDateTime updateTime) {
        if (this.mother == null) {
            this.mother = new PersonParentData();
            this.mother.setMother(true);
        }
        this.mother.setName(name);
        this.mother.setNameMarking(nameMarking);
        this.mother.setCprNumber(cprNumber);
        this.mother.setBirthDate(birthDate);
        this.mother.setBirthDateUncertain(birthDateUncertain);
        this.mother.setAuthority(authorityCode);
        this.mother.setDafoUpdated(updateTime);
    }

    public void setFather(String name, boolean nameMarking, String cprNumber, LocalDate birthDate, boolean birthDateUncertain, int authorityCode, OffsetDateTime updateTime) {
        if (this.father == null) {
            this.father = new PersonParentData();
            this.father.setMother(false);
        }
        this.father.setName(name);
        this.father.setNameMarking(nameMarking);
        this.father.setCprNumber(cprNumber);
        this.father.setBirthDate(birthDate);
        this.father.setBirthDateUncertain(birthDateUncertain);
        this.father.setAuthority(authorityCode);
        this.father.setDafoUpdated(updateTime);
    }

    public void setMotherVerification(int authorityCode, boolean verified, OffsetDateTime updateTime) {
        if (this.motherVerification == null) {
            this.motherVerification = new PersonParentVerificationData();
            this.motherVerification.setMother(true);
        }
        this.motherVerification.setAuthority(authorityCode);
        this.motherVerification.setVerified(verified);
        this.motherVerification.setDafoUpdated(updateTime);
    }

    public void setFatherVerification(int authorityCode, boolean verified, OffsetDateTime updateTime) {
        if (this.fatherVerification == null) {
            this.fatherVerification = new PersonParentVerificationData();
            this.fatherVerification.setMother(false);
        }
        this.fatherVerification.setAuthority(authorityCode);
        this.fatherVerification.setVerified(verified);
        this.fatherVerification.setDafoUpdated(updateTime);
    }

    public void setPosition(int authorityCode, String position, OffsetDateTime updateTime) {
        if (this.position == null) {
            this.position = new PersonPositionData();
        }
        this.position.setAuthority(authorityCode);
        this.position.setPosition(position);
        this.position.setDafoUpdated(updateTime);
    }

    public void setBirth(LocalDateTime foedselsdato, boolean foedselsdatoUsikkerhedsmarkering, int foedselsraekkefoelge, OffsetDateTime updateTime) {
        if (this.birth == null) {
            this.birth = new PersonBirthData();
        }
        this.birth.setBirthDatetime(foedselsdato);
        this.birth.setBirthDatetimeUncertain(foedselsdatoUsikkerhedsmarkering);
        this.birth.setFoedselsraekkefoelge(foedselsraekkefoelge);
        this.birth.setDafoUpdated(updateTime);
    }

    public void setBirth(int authority, int authorityText, String supplementalText, OffsetDateTime updateTime) {
        if (this.birth == null) {
            this.birth = new PersonBirthData();
        }
        this.birth.setBirthPlaceCode(authority);
        this.birth.setBirthAuthorityText(authorityText);
        this.birth.setBirthSupplementalText(supplementalText);
        this.birth.setDafoUpdated(updateTime);
    }

    public void setBirthVerification(int authority, boolean verification, OffsetDateTime updateTime) {
        if (this.birthVerification == null) {
            this.birthVerification = new PersonBirthVerificationData();
        }
        this.birthVerification.setAuthority(authority);
        this.birthVerification.setVerified(verification);
        this.birthVerification.setDafoUpdated(updateTime);
    }

    public void setChurch(int authority, Character churchRelation, OffsetDateTime updateTime) {
        if (this.church == null) {
            this.church = new PersonChurchData();
        }
        this.church.setAuthority(authority);
        this.church.setChurchRelation(churchRelation);
        this.church.setDafoUpdated(updateTime);
    }

    public void clearChurch(Session session) {
        if (this.church != null) {
            session.delete(this.church);
            this.church = null;
        }
    }

    public void setChurchVerification(int authority, boolean verified, OffsetDateTime updateTime) {
        if (this.churchVerification == null) {
            this.churchVerification = new PersonChurchVerificationData();
        }
        this.churchVerification.setAuthority(authority);
        this.churchVerification.setVerified(verified);
        this.churchVerification.setDafoUpdated(updateTime);
    }

    public void clearChurchVerification(Session session) {
        if (this.churchVerification != null) {
            session.delete(this.churchVerification);
            this.churchVerification = null;
        }
    }

    public void setCitizenship(int authority, int countryCode, OffsetDateTime updateTime) {
        if (this.citizenship == null) {
            this.citizenship = new PersonCitizenshipData();
        }
        this.citizenship.setAuthority(authority);
        this.citizenship.setCountryCode(countryCode);
        this.citizenship.setDafoUpdated(updateTime);
    }

    public void clearCitizenship(Session session) {
        if (this.citizenship != null) {
            session.delete(this.citizenship);
            this.citizenship = null;
        }
    }

    public void setCitizenshipVerification(int authority, boolean verified, OffsetDateTime updateTime) {
        if (this.citizenshipVerification == null) {
            this.citizenshipVerification = new PersonCitizenshipVerificationData();
        }
        this.citizenshipVerification.setAuthority(authority);
        this.citizenshipVerification.setVerified(verified);
        this.citizenshipVerification.setDafoUpdated(updateTime);
    }

    public void clearCitizenshipVerification(Session session) {
        if (this.citizenshipVerification != null) {
            session.delete(this.citizenshipVerification);
            this.citizenshipVerification = null;
        }
    }

    public void setAddress(int authority, String bygningsnummer, String bynavn, int cprKommunekode,
                           String cprKommunenavn, int cprVejkode, String darAdresse, String etage,
                           String husnummer, String postdistrikt, String postnummer, String sideDoer,
                           String adresselinie1, String adresselinie2, String adresselinie3, String adresselinie4,
                           String adresselinie5, int addressTextType, int startAuthority, OffsetDateTime updateTime) {
        if (this.address == null) {
            this.address = new PersonAddressData();
        }
        this.address.setAuthority(authority);
        this.address.setBuildingNumber(bygningsnummer);
        this.address.setCityName(bynavn);
        this.address.setMunicipalityCode(cprKommunekode);
        this.address.setMunicipalityName(cprKommunenavn);
        this.address.setRoadCode(cprVejkode);
        this.address.setDarAddress(darAdresse);
        this.address.setFloor(etage);
        this.address.setHouseNumber(husnummer);
        this.address.setPostalDistrict(postdistrikt);
        this.address.setPostalCode(postnummer);
        this.address.setDoor(sideDoer);

        this.address.setRoadAddressLine1(adresselinie1);
        this.address.setRoadAddressLine2(adresselinie2);
        this.address.setRoadAddressLine3(adresselinie3);
        this.address.setRoadAddressLine4(adresselinie4);
        this.address.setRoadAddressLine5(adresselinie5);

        this.address.setAdressetekststype(addressTextType);
        this.address.setStartautoritet(startAuthority);
        this.address.setDafoUpdated(updateTime);
    }

    public void clearAddress(Session session) {
        if (this.address != null) {
            session.delete(this.address);
            this.address = null;
        }
    }

    public void setCoName(String coName, OffsetDateTime updateTime) {
        if (this.coname == null) {
            this.coname = new PersonAddressConameData();
        }
        this.coname.setConame(coName);
    }

    public void clearCoName(Session session) {
        if (this.coname != null) {
            session.delete(this.coname);
            this.coname = null;
        }
    }

    public void setMoveMunicipality(int authority, LocalDateTime fraflytningsdatoKommune,
                                    boolean fraflytningsdatoKommuneUsikkerhedsmarkering, int fraflytningskommunekode,
                                    LocalDateTime tilflytningsdatoKommune,
                                    boolean tilflytningsdatoKommuneUsikkerhedsmarkering, OffsetDateTime updateTime) {
        if (this.moveMunicipality == null) {
            this.moveMunicipality = new PersonMoveMunicipalityData();
        }
        this.moveMunicipality.setAuthority(authority);
        this.moveMunicipality.setOutDatetime(fraflytningsdatoKommune);
        this.moveMunicipality.setOutDatetimeUncertain(fraflytningsdatoKommuneUsikkerhedsmarkering);
        this.moveMunicipality.setOutMunicipality(fraflytningskommunekode);
        this.moveMunicipality.setInDatetime(tilflytningsdatoKommune);
        this.moveMunicipality.setInDatetimeUncertain(tilflytningsdatoKommuneUsikkerhedsmarkering);
        this.moveMunicipality.setDafoUpdated(updateTime);
    }

    public void clearMoveMunicipality(Session session) {
        if (this.moveMunicipality != null) {
            session.delete(this.moveMunicipality);
            this.moveMunicipality = null;
        }
    }

    public void setName(int authority, String adresseringsnavn, String efternavn, String fornavne, String mellemnavn,
                        boolean efternavnMarkering, boolean fornavneMarkering, boolean mellemnavnMarkering,
                        String egetEfternavn, boolean ownLastNameMarking, boolean reportNames, OffsetDateTime updateTime) {
        if (this.name == null) {
            this.name = new PersonNameData();
        }
        this.name.setAuthority(authority);
        this.name.setAddressingName(adresseringsnavn);
        this.name.setLastName(efternavn);
        this.name.setFirstNames(fornavne);
        this.name.setMiddleName(mellemnavn);


        this.name.setLastNameMarking(efternavnMarkering);
        this.name.setFirstNamesMarking(fornavneMarkering);
        this.name.setMiddleNameMarking(mellemnavnMarkering);

        this.name.setEgetEfternavn(egetEfternavn);
        this.name.setEgetEfternavnMarkering(ownLastNameMarking);
        this.name.setRapportnavne(reportNames);
        this.name.setDafoUpdated(updateTime);
    }

    public void clearName(Session session) {
        if (this.name != null) {
            session.delete(this.name);
            this.name = null;
        }
    }

    public void setAddressName(int authority, String addressName, OffsetDateTime updateTime) {
        if (this.addressingName == null) {
            this.addressingName = new PersonAddressNameData();
        }
        this.addressingName.setAuthority(authority);
        this.addressingName.setAddressName(addressName);
        this.addressingName.setDafoUpdated(updateTime);
    }

    public void clearAddressName(Session session) {
        if (this.addressingName != null) {
            session.delete(this.addressingName);
            this.addressingName = null;
        }
    }

    public void setNameVerification(int authority, boolean verification, OffsetDateTime updateTime) {
        if (this.nameVerification == null) {
            this.nameVerification = new PersonNameVerificationData();
        }
        this.nameVerification.setAuthority(authority);
        this.nameVerification.setVerified(verification);
        this.nameVerification.setDafoUpdated(updateTime);
    }

    public void clearNameVerification(Session session) {
        if (this.nameVerification != null) {
            session.delete(this.nameVerification);
            this.nameVerification = null;
        }
    }

    public void setNameAuthorityText(int authority, String text, OffsetDateTime updateTime) {
        if (this.nameAuthority == null) {
            this.nameAuthority = new PersonNameAuthorityTextData();
        }
        this.nameAuthority.setAuthority(authority);
        this.nameAuthority.setText(text);
        this.nameAuthority.setDafoUpdated(updateTime);
    }

    public void clearNameAuthorityText(Session session) {
        if (this.nameAuthority != null) {
            session.delete(this.nameAuthority);
            this.nameAuthority = null;
        }
    }

    public void addProtection(int authority, int beskyttelsestype, boolean reportMarking, OffsetDateTime updateTime) {
        for (PersonProtectionData existing : this.protection) {
            if (existing.getProtectionType() == beskyttelsestype && existing.getReportMarking() == reportMarking) {
                return;
            }
        }
        PersonProtectionData protection = new PersonProtectionData();
        protection.setAuthority(authority);
        protection.setProtectionType(beskyttelsestype);
        protection.setReportMarking(reportMarking);
        protection.setBaseData(this);
        protection.setDafoUpdated(updateTime);
        this.protection.add(protection);
    }

    public void setEmigration(int authority, int countryCode, OffsetDateTime updateTime) {
        if (this.migration == null) {
            this.migration = new PersonEmigrationData();
        }
        this.migration.setAuthority(authority);
        this.migration.setCountryCode(countryCode);
        this.migration.setDafoUpdated(updateTime);
    }

    public void clearEmigration(Session session) {
        if (this.migration != null) {
            session.delete(this.migration);
            this.migration = null;
        }
    }

    public void setForeignAddress(int authority, String adresselinie1, String adresselinie2, String adresselinie3, String adresselinie4, String adresselinie5, OffsetDateTime updateTime) {
        if (this.foreignAddress == null) {
            this.foreignAddress = new PersonForeignAddressData();
        }
        this.foreignAddress.setAuthority(authority);
        this.foreignAddress.setAddressLine1(adresselinie1);
        this.foreignAddress.setAddressLine2(adresselinie2);
        this.foreignAddress.setAddressLine3(adresselinie3);
        this.foreignAddress.setAddressLine4(adresselinie4);
        this.foreignAddress.setAddressLine5(adresselinie5);
        this.foreignAddress.setDafoUpdated(updateTime);
    }

    public void clearForeignAddress(Session session) {
        if (this.foreignAddress != null) {
            session.delete(this.foreignAddress);
            this.foreignAddress = null;
        }
    }

    public void setCivilStatus(int authority, String civilStatus, String spouseCpr, LocalDate spouseBirthdate,
                               boolean spouseBirthdateUncertain, String spouseName, boolean spouseNameMarking, OffsetDateTime updateTime) {
        this.setCivilStatus(authority, civilStatus, spouseCpr, spouseBirthdate, spouseBirthdateUncertain, spouseName, spouseNameMarking, null, updateTime);
    }

    public void setCivilStatus(int authority, String civilStatus, String spouseCpr, LocalDate spouseBirthdate,
                               boolean spouseBirthdateUncertain, String spouseName, boolean spouseNameMarking, String correctionMarking, OffsetDateTime updateTime) {
        if (this.civilStatus == null) {
            this.civilStatus = new PersonCivilStatusData();
        }
        this.civilStatus.setAuthority(authority);
        this.civilStatus.setCorrectionMarking(correctionMarking);
        this.civilStatus.setCivilStatus(civilStatus);
        this.civilStatus.setSpouseCpr(spouseCpr);
        this.civilStatus.setSpouseBirthdate(spouseBirthdate);
        this.civilStatus.setSpouseBirthdateUncertain(spouseBirthdateUncertain);
        this.civilStatus.setSpouseName(spouseName);
        this.civilStatus.setSpouseNameMarking(spouseNameMarking);
        this.civilStatus.setDafoUpdated(updateTime);
    }

    public void clearCivilStatus(Session session) {
        if (this.civilStatus != null) {
            session.delete(this.civilStatus);
            this.civilStatus = null;
        }
    }


    public void setCivilStatusVerification(int authority, boolean verification, OffsetDateTime updateTime) {
        this.setCivilStatusVerification(authority, verification, null, updateTime);
    }
    public void setCivilStatusVerification(int authority, boolean verification, String correctionMarking, OffsetDateTime updateTime) {
        if (this.civilStatusVerification == null) {
            this.civilStatusVerification = new PersonCivilStatusVerificationData();
        }
        this.civilStatusVerification.setAuthority(authority);
        this.civilStatusVerification.setVerified(verification);
        this.civilStatusVerification.setCorrectionMarking(correctionMarking);
        this.civilStatusVerification.setDafoUpdated(updateTime);
    }

    public void clearCivilStatusVerification(Session session) {
        if (this.civilStatusVerification != null) {
            session.delete(this.civilStatusVerification);
            this.civilStatusVerification = null;
        }
    }

    public void setCivilStatusAuthorityText(int authority, String text, OffsetDateTime updateTime) {
        this.setCivilStatusAuthorityText(authority, text, null, updateTime);
    }
    public void setCivilStatusAuthorityText(int authority, String text, String correctionMarking, OffsetDateTime updateTime) {
        if (this.civilStatusAuthority == null) {
            this.civilStatusAuthority = new PersonCivilStatusAuthorityTextData();
        }
        this.civilStatusAuthority.setAuthority(authority);
        this.civilStatusAuthority.setText(text);
        this.civilStatusAuthority.setCorrectionMarking(correctionMarking);
        this.civilStatusAuthority.setDafoUpdated(updateTime);
    }

    public void clearCivilStatusAuthorityText(Session session) {
        if (this.civilStatusAuthority != null) {
            session.delete(this.civilStatusAuthority);
            this.civilStatusAuthority = null;
        }
    }

    public void setCprNumber(int authority, String cprNumber, OffsetDateTime updateTime) {
        if (this.cprNumber == null) {
            this.cprNumber = new PersonNumberData();
        }
        this.cprNumber.setAuthority(authority);
        this.cprNumber.setCprNumber(cprNumber);
        this.cprNumber.setDafoUpdated(updateTime);
    }

    public void clearCprNumber(Session session) {
        if (this.cprNumber != null) {
            session.delete(this.cprNumber);
            this.cprNumber = null;
        }
    }

    /**
     * Return a map of attributes, including those from the superclass
     * @return
     */
    @Override
    public Map<String, Object> asMap() {
        HashMap<String, Object> map = new HashMap<>();
        if (this.coreData != null) {
            map.putAll(this.coreData.asMap());
        }
        if (this.status != null) {
            map.put("status", this.status.getStatus());
        }
        if (this.mother != null) {
            map.put("mother", this.mother);
        }
        if (this.father != null) {
            map.put("father", this.father);
        }
        if (this.motherVerification != null) {
            map.put("motherVerification", this.motherVerification);
        }
        if (this.fatherVerification != null) {
            map.put("fatherVerification", this.fatherVerification);
        }
        if (this.position != null) {
            map.put("position", this.position);
        }
        if (this.birth != null) {
            map.put("birth", this.birth);
        }
        if (this.birthVerification != null) {
            map.put("birthVerification", this.birthVerification);
        }
        if (this.church != null) {
            map.put("church", this.church);
        }
        if (this.churchVerification != null) {
            map.put("churchVerification", this.churchVerification);
        }
        if (this.citizenship != null) {
            map.put("citizenship", this.citizenship);
        }
        if (this.citizenshipVerification != null) {
            map.put("citizenshipVerification", this.citizenshipVerification);
        }
        if (this.address != null) {
            map.put("address", this.address);
        }
        if (this.name != null) {
            map.put("name", this.name);
        }
        if (this.addressingName != null) {
            map.put("addressingName", this.addressingName);
        }
        if (this.nameVerification != null) {
            map.put("nameVerification", this.nameVerification);
        }
        if (this.nameAuthority != null) {
            map.put("nameAuthority", this.nameAuthority);
        }
        if (this.protection != null && !this.protection.isEmpty()) {
            map.put("protection", this.protection);
        }
        if (this.migration != null) {
            map.put("migration", this.migration);
        }
        if (this.foreignAddress != null) {
            map.put("foreignAddress", this.foreignAddress);
        }
        if (this.civilStatus != null) {
            map.put("civilStatus", this.civilStatus);
        }
        if (this.civilStatusVerification != null) {
            map.put("civilStatusVerification", this.civilStatusVerification);
        }
        if (this.civilStatusAuthority != null) {
            map.put("civilStatusAuthority", this.civilStatusAuthority);
        }
        if (this.cprNumber != null) {
            map.put("cprNumber", this.cprNumber);
        }
        return map;
    }


    @Override
    public LookupDefinition getLookupDefinition() {
        LookupDefinition lookupDefinition = new LookupDefinition(PersonBaseData.class);
        lookupDefinition.setMatchNulls(true);

        if (this.coreData != null) {
            lookupDefinition.putAll(DB_FIELD_CORE, this.coreData.databaseFields());
        }
        if (this.status != null) {
            lookupDefinition.putAll("status", this.status.databaseFields());
        }
        if (this.mother != null) {
            lookupDefinition.putAll("mother", this.mother.databaseFields());
        }
        if (this.father != null) {
            lookupDefinition.putAll("father", this.father.databaseFields());
        }
        if (this.motherVerification != null) {
            lookupDefinition.putAll("motherVerification", this.motherVerification.databaseFields());
        }
        if (this.fatherVerification != null) {
            lookupDefinition.putAll("fatherVerification", this.fatherVerification.databaseFields());
        }
        if (this.position != null) {
            lookupDefinition.putAll("position", this.position.databaseFields());
        }
        if (this.birth != null) {
            lookupDefinition.putAll("birth", this.birth.databaseFields());
        }
        if (this.address != null) {
            lookupDefinition.putAll("address", this.address.databaseFields());
        }
        if (this.coname != null) {
            lookupDefinition.putAll("coname", this.coname.databaseFields());
        }
        if (this.moveMunicipality != null) {
            lookupDefinition.putAll("moveMunicipality", this.moveMunicipality.databaseFields());
        }
        if (this.name != null) {
            lookupDefinition.putAll("name", this.name.databaseFields());
        }
        if (this.addressingName != null) {
            lookupDefinition.putAll("addressingName", this.addressingName.databaseFields());
        }
        if (this.nameVerification != null) {
            lookupDefinition.putAll("nameVerification", this.nameVerification.databaseFields());
        }
        if (this.nameAuthority != null) {
            lookupDefinition.putAll("nameAuthority", this.nameAuthority.databaseFields());
        }
        if (this.protection != null) {
            lookupDefinition.putAll("protection", DetailData.listDatabaseFields(this.protection));
        }
        if (this.migration != null) {
            lookupDefinition.putAll("migration", this.migration.databaseFields());
        }
        if (this.foreignAddress != null) {
            lookupDefinition.putAll("foreignAddress", this.foreignAddress.databaseFields());
        }
        if (this.civilStatus != null) {
            lookupDefinition.putAll("civilStatus", this.civilStatus.databaseFields());
        }
        if (this.civilStatusVerification != null) {
            lookupDefinition.putAll("civilStatusVerification", this.civilStatusVerification.databaseFields());
        }
        if (this.civilStatusAuthority != null) {
            lookupDefinition.putAll("civilStatusAuthority", this.civilStatusAuthority.databaseFields());
        }
        if (this.cprNumber != null) {
            lookupDefinition.putAll("cprNumber", this.cprNumber.databaseFields());
        }
        return lookupDefinition;
    }

    @Override
    public void forceLoad(Session session) {

    }

    @Override
    public PersonBaseData clone() {
        PersonBaseData clone = new PersonBaseData();
        if (this.coreData != null) {
            clone.coreData = this.coreData.clone();
        }
        if (this.status != null) {
            clone.status = this.status.clone();
        }
        if (this.mother != null) {
            clone.mother = this.mother.clone();
        }
        if (this.father != null) {
            clone.father = this.father.clone();
        }
        if (this.motherVerification != null) {
            clone.motherVerification = this.motherVerification.clone();
        }
        if (this.fatherVerification != null) {
            clone.fatherVerification = this.fatherVerification.clone();
        }
        if (this.position != null) {
            clone.position = this.position.clone();
        }
        if (this.birth != null) {
            clone.birth = this.birth.clone();
        }
        if (this.birthVerification != null) {
            clone.birthVerification = this.birthVerification.clone();
        }
        if (this.church != null) {
            clone.church = this.church.clone();
        }
        if (this.churchVerification != null) {
            clone.churchVerification = this.churchVerification.clone();
        }
        if (this.citizenship != null) {
            clone.citizenship = this.citizenship.clone();
        }
        if (this.citizenshipVerification != null) {
            clone.citizenshipVerification = this.citizenshipVerification.clone();
        }
        if (this.address != null) {
            clone.address = this.address.clone();
        }
        if (this.coname != null) {
            clone.coname = this.coname.clone();
        }
        if (this.moveMunicipality != null) {
            clone.moveMunicipality = this.moveMunicipality.clone();
        }
        if (this.name != null) {
            clone.name = this.name.clone();
        }
        if (this.addressingName != null) {
            clone.addressingName = this.addressingName.clone();
        }
        if (this.nameVerification != null) {
            clone.nameVerification = this.nameVerification.clone();
        }
        if (this.nameAuthority != null) {
            clone.nameAuthority = this.nameAuthority.clone();
        }
        if (this.protection != null) {
            clone.protection = new HashSet<>();
            for (PersonProtectionData protectionData : this.protection) {
                PersonProtectionData protectionClone = protectionData.clone();
                protectionClone.setBaseData(clone);
                clone.protection.add(protectionClone);
            }
        }
        if (this.migration != null) {
            clone.migration = this.migration.clone();
        }
        if (this.foreignAddress != null) {
            clone.foreignAddress = this.foreignAddress.clone();
        }
        if (this.civilStatus != null) {
            clone.civilStatus = this.civilStatus.clone();
        }
        if (this.civilStatusVerification != null) {
            clone.civilStatusVerification = this.civilStatusVerification.clone();
        }
        if (this.civilStatusAuthority != null) {
            clone.civilStatusAuthority = this.civilStatusAuthority.clone();
        }
        if (this.cprNumber != null) {
            clone.cprNumber = this.cprNumber.clone();
        }
        return clone;
    }

    public boolean isEmpty() {
        if (this.coreData != null) {
            return false;
        }
        if (this.status != null) {
            return false;
        }
        if (this.mother != null) {
            return false;
        }
        if (this.father != null) {
            return false;
        }
        if (this.motherVerification != null) {
            return false;
        }
        if (this.fatherVerification != null) {
            return false;
        }
        if (this.position != null) {
            return false;
        }
        if (this.birth != null) {
            return false;
        }
        if (this.birthVerification != null) {
            return false;
        }
        if (this.church != null) {
            return false;
        }
        if (this.churchVerification != null) {
            return false;
        }
        if (this.citizenship != null) {
            return false;
        }
        if (this.citizenshipVerification != null) {
            return false;
        }
        if (this.address != null) {
            return false;
        }
        if (this.coname != null) {
            return false;
        }
        if (this.moveMunicipality != null) {
            return false;
        }
        if (this.name != null) {
            return false;
        }
        if (this.addressingName != null) {
            return false;
        }
        if (this.nameVerification != null) {
            return false;
        }
        if (this.nameAuthority != null) {
            return false;
        }
        if (this.protection != null && !this.protection.isEmpty()) {
            return false;
        }
        if (this.migration != null) {
            return false;
        }
        if (this.foreignAddress != null) {
            return false;
        }
        if (this.civilStatus != null) {
            return false;
        }
        if (this.civilStatusVerification != null) {
            return false;
        }
        if (this.civilStatusAuthority != null) {
            return false;
        }
        if (this.cprNumber != null) {
            return false;
        }
        return true;
    }
}
