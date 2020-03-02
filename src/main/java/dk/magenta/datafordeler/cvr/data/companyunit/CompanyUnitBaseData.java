package dk.magenta.datafordeler.cvr.data.companyunit;

import com.fasterxml.jackson.annotation.JsonProperty;
import dk.magenta.datafordeler.core.database.DataItem;
import dk.magenta.datafordeler.core.database.DatabaseEntry;
import dk.magenta.datafordeler.core.database.Identification;
import dk.magenta.datafordeler.core.database.LookupDefinition;
import dk.magenta.datafordeler.core.exception.ParseException;
import dk.magenta.datafordeler.cvr.data.CvrData;
import dk.magenta.datafordeler.cvr.data.DetailData;
import dk.magenta.datafordeler.cvr.data.shared.*;
import dk.magenta.datafordeler.cvr.data.unversioned.Address;
import dk.magenta.datafordeler.cvr.data.unversioned.Industry;
import org.hibernate.Hibernate;
import org.hibernate.Session;

import javax.persistence.*;
import java.time.OffsetDateTime;
import java.util.*;

/**
 * Base class for Company Unit data, linking to Effects and delegating storage to referred classes
 */
@Entity
@Table(name="cvr_companyunit_basedata", indexes = {
        @Index(name = "cvr_companyunit_lastUpdated", columnList = DataItem.DB_FIELD_LAST_UPDATED),
        @Index(name = "cvr_companyunit_industry", columnList = CompanyUnitBaseData.DB_FIELD_PRIMARY_INDUSTRY + DatabaseEntry.REF),
        @Index(name = "cvr_companyunit_location", columnList = CompanyUnitBaseData.DB_FIELD_LOCATION_ADDRESS + DatabaseEntry.REF)
})
public class CompanyUnitBaseData extends CvrData<CompanyUnitEffect, CompanyUnitBaseData> {


    public static final String DB_FIELD_LIFECYCLE = "lifecycleData";
    public static final String IO_FIELD_LIFECYCLE = "livscyklus";

    @OneToOne(optional = true, cascade = CascadeType.ALL)
    private LifecycleData lifecycleData;

    @JsonProperty(value = IO_FIELD_LIFECYCLE)
    public LifecycleData getLifecycleData() {
        return lifecycleData;
    }




    public static final String DB_FIELD_ADVERTPROTECTION = "advertProtection";
    public static final String IO_FIELD_ADVERTPROTECTION = "reklamebeskyttelse";
    @OneToOne(optional = true, cascade = CascadeType.ALL)
    @JoinColumn(name = DB_FIELD_ADVERTPROTECTION + DatabaseEntry.REF)
    private BooleanData advertProtection;

    @JsonProperty(value = IO_FIELD_ADVERTPROTECTION)
    public Boolean getAdvertProtection() {
        if (advertProtection != null) {
            return advertProtection.getValue();
        } else {
            return null;
        }
    }




    public static final String DB_FIELD_PNUMBER = "pNumber";
    public static final String IO_FIELD_PNUMBER = "pNummer";
    @OneToOne(optional = true, cascade = CascadeType.ALL)
    @JoinColumn(name = DB_FIELD_PNUMBER + DatabaseEntry.REF)
    private IntegerData pNumber;

    @JsonProperty(value = IO_FIELD_PNUMBER)
    public Long getpNumber() {
        if (pNumber != null) {
            return pNumber.getValue();
        } else {
            return null;
        }
    }




    public static final String DB_FIELD_LOCATION_ADDRESS = "locationAddress";
    public static final String IO_FIELD_LOCATION_ADDRESS = "beliggenhedsadresse";
    @OneToOne(optional = true, cascade = CascadeType.ALL)
    @JoinColumn(name = DB_FIELD_LOCATION_ADDRESS + DatabaseEntry.REF)
    private AddressData locationAddress;

    @JsonProperty(value = IO_FIELD_LOCATION_ADDRESS)
    public Address getLocationAddress() {
        if (locationAddress != null) {
            return locationAddress.getAddress();
        } else {
            return null;
        }
    }




    public static final String DB_FIELD_POSTAL_ADDRESS = "postalAddress";
    public static final String IO_FIELD_POSTAL_ADDRESS = "postadresse";
    @OneToOne(optional = true, cascade = CascadeType.ALL)
    @JoinColumn(name = DB_FIELD_POSTAL_ADDRESS + DatabaseEntry.REF)
    private AddressData postalAddress;

    @JsonProperty(value = IO_FIELD_POSTAL_ADDRESS)
    public Address getPostalAddress() {
        if (postalAddress != null) {
            return postalAddress.getAddress();
        } else {
            return null;
        }
    }




    public static final String DB_FIELD_YEARLY_NUMBERS = "yearlyEmployeeNumbersData";
    public static final String IO_FIELD_YEARLY_NUMBERS = "aarsbeskaeftigelse";
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "companyUnitBaseData")
    @OrderBy(value = CompanyUnitYearlyEmployeeNumbersData.DB_FIELD_YEAR + " asc")
    private List<CompanyUnitYearlyEmployeeNumbersData> yearlyEmployeeNumbersData;

    @JsonProperty(value = IO_FIELD_YEARLY_NUMBERS)
    public List<CompanyUnitYearlyEmployeeNumbersData> getYearlyEmployeeNumbersData() {
        return yearlyEmployeeNumbersData;
    }




    public static final String DB_FIELD_QUARTERLY_NUMBERS = "quarterlyEmployeeNumbersData";
    public static final String IO_FIELD_QUARTERLY_NUMBERS = "kvartalsbeskaeftigelse";
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "companyUnitBaseData")
    @OrderBy(value = CompanyUnitQuarterlyEmployeeNumbersData.DB_FIELD_YEAR + " asc, " + CompanyUnitQuarterlyEmployeeNumbersData.DB_FIELD_QUARTER + " asc")
    private List<CompanyUnitQuarterlyEmployeeNumbersData> quarterlyEmployeeNumbersData;

    @JsonProperty(value = IO_FIELD_QUARTERLY_NUMBERS)
    public List<CompanyUnitQuarterlyEmployeeNumbersData> getQuarterlyEmployeeNumbersData() {
        return quarterlyEmployeeNumbersData;
    }




    public static final String DB_FIELD_MONTHLY_NUMBERS = "monthlyEmployeeNumbersData";
    public static final String IO_FIELD_MONTHLY_NUMBERS = "maanedsbeskaeftigelse";
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "companyUnitBaseData")
    @OrderBy(value = CompanyUnitMonthlyEmployeeNumbersData.DB_FIELD_YEAR + " asc, "+CompanyUnitMonthlyEmployeeNumbersData.DB_FIELD_MONTH+" asc")
    private List<CompanyUnitMonthlyEmployeeNumbersData> monthlyEmployeeNumbersData;

    @JsonProperty(value = IO_FIELD_MONTHLY_NUMBERS)
    public List<CompanyUnitMonthlyEmployeeNumbersData> getMonthlyEmployeeNumbersData() {
        return monthlyEmployeeNumbersData;
    }




    public static final String DB_FIELD_PRIMARY_INDUSTRY = "primaryIndustry";
    public static final String IO_FIELD_PRIMARY_INDUSTRY = "hovedbranche";
    @OneToOne(optional = true, cascade = CascadeType.ALL)
    @JoinColumn(name = DB_FIELD_PRIMARY_INDUSTRY + DatabaseEntry.REF)
    private IndustryData primaryIndustry;

    @JsonProperty(value = IO_FIELD_PRIMARY_INDUSTRY)
    public Industry getPrimaryIndustry() {
        if (primaryIndustry != null) {
            return primaryIndustry.getIndustry();
        } else {
            return null;
        }
    }




    public static final String DB_FIELD_SECONDARY_INDUSTRY_1 = "secondaryIndustry1";
    public static final String IO_FIELD_SECONDARY_INDUSTRY_1 = "bibranche1";
    @OneToOne(optional = true, cascade = CascadeType.ALL)
    @JoinColumn(name = DB_FIELD_SECONDARY_INDUSTRY_1 + DatabaseEntry.REF)
    private IndustryData secondaryIndustry1;

    @JsonProperty(value = IO_FIELD_SECONDARY_INDUSTRY_1)
    public Industry getSecondaryIndustry1() {
        if (secondaryIndustry1 != null)
            return secondaryIndustry1.getIndustry();
        else
            return null;
    }




    public static final String DB_FIELD_SECONDARY_INDUSTRY_2 = "secondaryIndustry2";
    public static final String IO_FIELD_SECONDARY_INDUSTRY_2 = "bibranche2";
    @OneToOne(optional = true, cascade = CascadeType.ALL)
    @JoinColumn(name = DB_FIELD_SECONDARY_INDUSTRY_2 + DatabaseEntry.REF)
    private IndustryData secondaryIndustry2;

    @JsonProperty(value = IO_FIELD_SECONDARY_INDUSTRY_2)
    public Industry getSecondaryIndustry2() {
        if (secondaryIndustry2 != null) {
            return secondaryIndustry2.getIndustry();
        } else {
            return null;
        }
    }




    public static final String DB_FIELD_SECONDARY_INDUSTRY_3 = "secondaryIndustry3";
    public static final String IO_FIELD_SECONDARY_INDUSTRY_3 = "bibranche3";
    @OneToOne(optional = true, cascade = CascadeType.ALL)
    @JoinColumn(name = DB_FIELD_SECONDARY_INDUSTRY_3 + DatabaseEntry.REF)
    private IndustryData secondaryIndustry3;

    @JsonProperty(value = IO_FIELD_SECONDARY_INDUSTRY_3)
    public Industry getSecondaryIndustry3() {
        if (secondaryIndustry3 != null) {
            return secondaryIndustry3.getIndustry();
        } else {
            return null;
        }
    }




    public static final String DB_FIELD_NAME = "name";
    public static final String IO_FIELD_NAME = "navn";
    @OneToOne(optional = true, cascade = CascadeType.ALL)
    @JoinColumn(name = DB_FIELD_NAME + DatabaseEntry.REF)
    private TextData name;

    @JsonProperty(value = IO_FIELD_NAME)
    public String getName() {
        if (name != null) {
            return name.getValue();
        } else {
            return null;
        }
    }




    public static final String DB_FIELD_PHONENUMBER = "phoneNumber";
    public static final String IO_FIELD_PHONENUMBER = "telefon";
    @OneToOne(optional = true, cascade = CascadeType.ALL)
    @JoinColumn(name = DB_FIELD_PHONENUMBER + DatabaseEntry.REF)
    private ContactData phoneNumber;

    @JsonProperty(value = IO_FIELD_PHONENUMBER)
    public String getPhoneNumber() {
        if (phoneNumber != null) {
            return phoneNumber.getValue();
        } else {
            return null;
        }
    }




    public static final String DB_FIELD_EMAIL = "emailAddress";
    public static final String IO_FIELD_EMAIL = "email";
    @OneToOne(optional = true, cascade = CascadeType.ALL)
    @JoinColumn(name = DB_FIELD_EMAIL + DatabaseEntry.REF)
    private ContactData emailAddress;

    @JsonProperty(value = IO_FIELD_EMAIL)
    public String getEmailAddress() {
        if (emailAddress != null) {
            return emailAddress.getValue();
        } else {
            return null;
        }
    }




    public static final String DB_FIELD_FAXNUMBER = "faxNumber";
    public static final String IO_FIELD_FAXNUMBER = "telefax";
    @OneToOne(optional = true, cascade = CascadeType.ALL)
    @JoinColumn(name = DB_FIELD_FAXNUMBER + DatabaseEntry.REF)
    private ContactData faxNumber;

    @JsonProperty(IO_FIELD_FAXNUMBER)
    public String getFaxNumber() {
        if (faxNumber != null) {
            return faxNumber.getValue();
        } else {
            return null;
        }
    }




    public static final String DB_FIELD_IS_PRIMARY = "isPrimary";
    public static final String IO_FIELD_IS_PRIMARY = "primaer";
    @OneToOne(optional = true, cascade = CascadeType.ALL)
    @JoinColumn(name = DB_FIELD_IS_PRIMARY + DatabaseEntry.REF)
    private BooleanData isPrimary;

    @JsonProperty(value = IO_FIELD_IS_PRIMARY)
    public Boolean getIsPrimary() {
        return isPrimary.getValue();
    }





    public static final String DB_FIELD_CVR_NUMBER = "associatedCvrNumber";
    public static final String IO_FIELD_CVR_NUMBER = "tilknyttetCvrNummer";
    @OneToMany(cascade = CascadeType.ALL)
    private Set<CompanyUnitCvrNumber> associatedCvrNumber;

    @JsonProperty(value = IO_FIELD_CVR_NUMBER)
    public List<Long> getAssociatedCvrNumber() {
        if (associatedCvrNumber != null) {
            ArrayList<Long> list = new ArrayList<>();
            for (CompanyUnitCvrNumber i : this.associatedCvrNumber) {
                Long cvr = i.getValue();
                if (cvr != null) {
                    list.add(cvr);
                }
            }
            return list;
        } else {
            return null;
        }
    }




    public static final String DB_FIELD_PARTICIPANTS = "participantData";
    public static final String IO_FIELD_PARTICIPANTS = "deltagere";
    @ManyToMany(mappedBy = "companyUnitBases")
    private Set<ParticipantLink> participantData = new HashSet<>();

    @JsonProperty(value = IO_FIELD_PARTICIPANTS)
    public Set<ParticipantLink> getParticipantData() {
        return participantData;
    }




    public static final String DB_FIELD_ATTRIBUTES = "attributeData";
    public static final String IO_FIELD_ATTRIBUTES = "attributter";
    @OneToMany(cascade = CascadeType.ALL, mappedBy = AttributeData.DB_FIELD_COMPANYUNITBASE)
    private Set<AttributeData> attributeData = new HashSet<>();

    @JsonProperty(value = IO_FIELD_ATTRIBUTES)
    public Set<AttributeData> getAttributeData() {
        return attributeData;
    }




    public static final String DB_FIELD_PARTICIPANT_RELATIONS = "participantRelationData";
    public static final String IO_FIELD_PARTICIPANT_RELATIONS = "deltagerRelation";
    @OneToMany(cascade = CascadeType.ALL)
    private Set<ParticipantRelationData> participantRelationData = new HashSet<>();

    @JsonProperty(IO_FIELD_PARTICIPANT_RELATIONS)
    public Set<ParticipantRelationData> getParticipantRelationData() {
        return participantRelationData;
    }





    @Override
    public Map<String, Object> asMap() {
        HashMap<String, Object> map = new HashMap<>();
        if (this.lifecycleData != null) {
            map.put("livsforloeb", this.lifecycleData.asMap());
        }
        if (this.advertProtection != null) {
            map.put("reklamebeskyttelse", this.advertProtection.getValue());
        }
        if (this.pNumber != null) {
            map.put("pNummer", this.pNumber.getValue());
        }
        if (this.locationAddress != null) {
            map.put("locationAddress", this.locationAddress.getAddress());
        }
        if (this.postalAddress != null) {
            map.put("postadresse", this.postalAddress.getAddress());
        }
        if (this.yearlyEmployeeNumbersData != null) {
            map.put("aarsbeskaeftigelse", this.yearlyEmployeeNumbersData);
        }
        if (this.quarterlyEmployeeNumbersData != null) {
            map.put("kvartalsbeskaeftigelse", this.quarterlyEmployeeNumbersData);
        }
        if (this.monthlyEmployeeNumbersData != null) {
            map.put("maanedsbeskaeftigelse", this.monthlyEmployeeNumbersData);
        }
        if (this.primaryIndustry != null) {
            map.put("hovedbranche", this.primaryIndustry.getIndustry());
        }
        if (this.secondaryIndustry1 != null) {
            map.put("bibranche1", this.secondaryIndustry1.getIndustry());
        }
        if (this.secondaryIndustry2 != null) {
            map.put("bibranche2", this.secondaryIndustry2.getIndustry());
        }
        if (this.secondaryIndustry3 != null) {
            map.put("bibranche3", this.secondaryIndustry3.getIndustry());
        }
        if (this.name != null) {
            map.put("name", this.name.getValue());
        }
        if (this.phoneNumber != null) {
            map.put("telefonnummer", this.phoneNumber.asMap());
        }
        if (this.emailAddress != null) {
            map.put("emailadresse", this.emailAddress.asMap());
        }
        if (this.faxNumber != null) {
            map.put("telefaxnummer", this.faxNumber.asMap());
        }
        if (this.isPrimary != null) {
            map.put("primaer", this.isPrimary.getValue());
        }
        if (this.associatedCvrNumber != null) {
            map.put("tilknyttetVirksomhedsCVRNummer", this.associatedCvrNumber);
        }
        if (this.participantData != null && !this.participantData.isEmpty()) {
            map.put("deltagere", this.participantData);
        }
        if (this.attributeData != null && !this.attributeData.isEmpty()) {
            map.put("attributes", this.attributeData);
        }
        if (this.participantRelationData != null && !this.participantRelationData.isEmpty()) {
            map.put("deltagerRelation", this.participantRelationData);
        }
        return map;
    }

    public void setLifecycleStart(OffsetDateTime startDate) {
        if (this.lifecycleData == null) {
            this.lifecycleData = new LifecycleData();
        }
        this.lifecycleData.setStartDate(startDate);
    }
    public void setLifecycleStop(OffsetDateTime endDate) {
        if (this.lifecycleData == null) {
            this.lifecycleData = new LifecycleData();
        }
        this.lifecycleData.setEndDate(endDate);
    }
    public void setAdvertProtection(boolean advertProtection) {
        if (this.advertProtection == null) {
            this.advertProtection = new BooleanData(BooleanData.Type.REKLAME_BESKYTTELSE);
        }
        this.advertProtection.setValue(advertProtection);
    }
    public void setPNumber(long unitNumber) {
        if (this.pNumber == null) {
            this.pNumber = new IntegerData();
        }
        this.pNumber.setValue(unitNumber);
    }
    public void setLocationAddress(Address address) {
        if (this.locationAddress == null) {
            this.locationAddress = new AddressData();
        }
        this.locationAddress.setAddress(address);
    }
    public void setPostalAddress(Address address) {
        if (this.postalAddress == null) {
            this.postalAddress = new AddressData();
        }
        this.postalAddress.setAddress(address);
    }

    public void setYearlyEmployeeNumbers(int year, Integer employeesLow, Integer employeesHigh, Integer fulltimeEquivalentLow, Integer fulltimeEquivalentHigh, Integer includingOwnersLow, Integer includingOwnersHigh) throws ParseException {
        if (this.yearlyEmployeeNumbersData == null) {
            //this.yearlyEmployeeNumbersData = new CompanyYearlyEmployeeNumbersData();
            this.yearlyEmployeeNumbersData = new ArrayList<>();
        }
        CompanyUnitYearlyEmployeeNumbersData yearlyEmployeeNumbersData = null;
        for (CompanyUnitYearlyEmployeeNumbersData data : this.yearlyEmployeeNumbersData) {
            if (data.getYear() == year) {
                yearlyEmployeeNumbersData = data;
            }
        }
        if (yearlyEmployeeNumbersData == null) {
            yearlyEmployeeNumbersData = new CompanyUnitYearlyEmployeeNumbersData();
            yearlyEmployeeNumbersData.setCompanyUnitBaseData(this);
            this.yearlyEmployeeNumbersData.add(yearlyEmployeeNumbersData);
        }
        yearlyEmployeeNumbersData.setYear(year);
        yearlyEmployeeNumbersData.setEmployeesLow(employeesLow);
        yearlyEmployeeNumbersData.setEmployeesHigh(employeesHigh);
        yearlyEmployeeNumbersData.setFullTimeEquivalentLow(fulltimeEquivalentLow);
        yearlyEmployeeNumbersData.setFullTimeEquivalentHigh(fulltimeEquivalentHigh);
        yearlyEmployeeNumbersData.setIncludingOwnersLow(includingOwnersLow);
        yearlyEmployeeNumbersData.setIncludingOwnersHigh(includingOwnersHigh);
    }


    public void setQuarterlyEmployeeNumbers(int year, int quarter, Integer employeesLow, Integer employeesHigh, Integer fulltimeEquivalentLow, Integer fulltimeEquivalentHigh, Integer includingOwnersLow, Integer includingOwnersHigh) throws ParseException {
        if (this.quarterlyEmployeeNumbersData == null) {
            //this.quarterlyEmployeeNumbersData = new CompanyQuarterlyEmployeeNumbersData();
            this.quarterlyEmployeeNumbersData = new ArrayList<>();
        }
        CompanyUnitQuarterlyEmployeeNumbersData quarterlyEmployeeNumbersData = null;
        for (CompanyUnitQuarterlyEmployeeNumbersData data : this.quarterlyEmployeeNumbersData) {
            if (data.getYear() == year && data.getQuarter() == quarter) {
                quarterlyEmployeeNumbersData = data;
            }
        }
        if (quarterlyEmployeeNumbersData == null) {
            quarterlyEmployeeNumbersData = new CompanyUnitQuarterlyEmployeeNumbersData();
            quarterlyEmployeeNumbersData.setCompanyUnitBaseData(this);
            this.quarterlyEmployeeNumbersData.add(quarterlyEmployeeNumbersData);
        }
        quarterlyEmployeeNumbersData.setYear(year);
        quarterlyEmployeeNumbersData.setQuarter(quarter);
        quarterlyEmployeeNumbersData.setEmployeesLow(employeesLow);
        quarterlyEmployeeNumbersData.setEmployeesHigh(employeesHigh);
        quarterlyEmployeeNumbersData.setFullTimeEquivalentLow(fulltimeEquivalentLow);
        quarterlyEmployeeNumbersData.setFullTimeEquivalentHigh(fulltimeEquivalentHigh);
        quarterlyEmployeeNumbersData.setIncludingOwnersLow(includingOwnersLow);
        quarterlyEmployeeNumbersData.setIncludingOwnersHigh(includingOwnersHigh);
    }


    public void setMonthlyEmployeeNumbers(int year, int month, Integer employeesLow, Integer employeesHigh, Integer fulltimeEquivalentLow, Integer fulltimeEquivalentHigh, Integer includingOwnersLow, Integer includingOwnersHigh) throws ParseException {
        if (this.monthlyEmployeeNumbersData == null) {
            //this.monthlyEmployeeNumbersData = new CompanyMonthlyEmployeeNumbersData();
            this.monthlyEmployeeNumbersData = new ArrayList<>();
        }
        CompanyUnitMonthlyEmployeeNumbersData monthlyEmployeeNumbersData = null;
        for (CompanyUnitMonthlyEmployeeNumbersData data : this.monthlyEmployeeNumbersData) {
            if (data.getYear() == year && data.getMonth() == month) {
                monthlyEmployeeNumbersData = data;
            }
        }
        if (monthlyEmployeeNumbersData == null) {
            monthlyEmployeeNumbersData = new CompanyUnitMonthlyEmployeeNumbersData();
            monthlyEmployeeNumbersData.setCompanyUnitBaseData(this);
            this.monthlyEmployeeNumbersData.add(monthlyEmployeeNumbersData);
        }
        monthlyEmployeeNumbersData.setYear(year);
        monthlyEmployeeNumbersData.setMonth(month);
        monthlyEmployeeNumbersData.setEmployeesLow(employeesLow);
        monthlyEmployeeNumbersData.setEmployeesHigh(employeesHigh);
        monthlyEmployeeNumbersData.setFullTimeEquivalentLow(fulltimeEquivalentLow);
        monthlyEmployeeNumbersData.setFullTimeEquivalentHigh(fulltimeEquivalentHigh);
        monthlyEmployeeNumbersData.setIncludingOwnersLow(includingOwnersLow);
        monthlyEmployeeNumbersData.setIncludingOwnersHigh(includingOwnersHigh);
    }

    public void setPrimaryIndustry(Industry industry) {
        if (this.primaryIndustry == null) {
            this.primaryIndustry = new IndustryData(true);
        }
        this.primaryIndustry.setIndustry(industry);
    }
    public void setSecondaryIndustry1(Industry industry) {
        if (this.secondaryIndustry1 == null) {
            this.secondaryIndustry1 = new IndustryData(false);
        }
        this.secondaryIndustry1.setIndustry(industry);
    }
    public void setSecondaryIndustry2(Industry industry) {
        if (this.secondaryIndustry2 == null) {
            this.secondaryIndustry2 = new IndustryData(false);
        }
        this.secondaryIndustry2.setIndustry(industry);
    }
    public void setSecondaryIndustry3(Industry industry) {
        if (this.secondaryIndustry3 == null) {
            this.secondaryIndustry3 = new IndustryData(false);
        }
        this.secondaryIndustry3.setIndustry(industry);
    }

    public void setName(String name) {
        if (this.name == null) {
            this.name = new TextData(TextData.Type.NAVN);
        }
        this.name.setValue(name);
    }
    public void setPhoneNumber(String phone, boolean secret) {
        if (this.phoneNumber == null) {
            this.phoneNumber = new ContactData(ContactData.Type.TELEFONNUMMER);
        }
        this.phoneNumber.setValue(phone);
        this.phoneNumber.setSecret(secret);
    }
    public void setEmailAddress(String email, boolean secret) {
        if (this.emailAddress == null) {
            this.emailAddress = new ContactData(ContactData.Type.EMAILADRESSE);
        }
        this.emailAddress.setValue(email);
        this.emailAddress.setSecret(secret);
    }
    public void setFaxNumber(String fax, boolean secret) {
        if (this.faxNumber == null) {
            this.faxNumber = new ContactData(ContactData.Type.TELEFAXNUMMER);
        }
        this.faxNumber.setValue(fax);
        this.faxNumber.setSecret(secret);
    }

    public void setIsPrimary(boolean isPrimary) {
        if (this.isPrimary == null) {
            this.isPrimary = new BooleanData(BooleanData.Type.ER_PRIMAER_ENHED);
        }
        this.isPrimary.setValue(isPrimary);
    }

    public void addAssociatedCvrNumber(long cvrNumber) {
        if (this.associatedCvrNumber == null) {
            this.associatedCvrNumber = new HashSet<>();
        }
        for (CompanyUnitCvrNumber i : this.associatedCvrNumber) {
            if (i.getValue() == cvrNumber) {
                return;
            }
        }
        CompanyUnitCvrNumber i = new CompanyUnitCvrNumber();
        i.setCompanyUnitBaseData(this);
        i.setValue(cvrNumber);
        this.associatedCvrNumber.add(i);
    }

    public void addParticipant(ParticipantLink participantLink) {
        this.participantData.add(participantLink);
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
        attributeData.setCompanyUnitBaseData(this);
        this.attributeData.add(attributeData);
    }

    public void addParticipantRelation(Identification participant, Set<Identification> organizations) {
        ParticipantRelationData participantRelationData = new ParticipantRelationData();
        participantRelationData.setParticipant(participant);
        for (Identification organization : organizations) {
            participantRelationData.addOrganization(organization);
        }
        this.participantRelationData.add(participantRelationData);
    }

    @Override
    public LookupDefinition getLookupDefinition() {
        LookupDefinition lookupDefinition = new LookupDefinition(CompanyUnitBaseData.class);
        lookupDefinition.setMatchNulls(true);
        if (this.lifecycleData != null) {
            lookupDefinition.putAll(DB_FIELD_LIFECYCLE, this.lifecycleData.databaseFields());
        }
        if (this.advertProtection != null) {
            lookupDefinition.putAll(DB_FIELD_ADVERTPROTECTION, this.advertProtection.databaseFields());
        }
        if (this.pNumber != null) {
            lookupDefinition.putAll(DB_FIELD_PNUMBER, this.pNumber.databaseFields());
        }
        if (this.locationAddress != null) {
            lookupDefinition.putAll(DB_FIELD_LOCATION_ADDRESS, this.locationAddress.databaseFields());
        }
        if (this.postalAddress != null) {
            lookupDefinition.putAll(DB_FIELD_POSTAL_ADDRESS, this.postalAddress.databaseFields());
        }
        /*if (this.aarsbeskaeftigelse != null) {
            lookupDefinition.putAll("aarsbeskaeftigelse", this.aarsbeskaeftigelse.databaseFields());
        }*/
        /*if (this.kvartalsbeskaeftigelse != null) {
            lookupDefinition.putAll("kvartalsbeskaeftigelse", this.kvartalsbeskaeftigelse.databaseFields());
        }*/
        /*if (this.maanedsbeskaeftigelse != null) {
            lookupDefinition.putAll("maanedsbeskaeftigelse", this.maanedsbeskaeftigelse.databaseFields());
        }*/
        if (this.primaryIndustry != null) {
            lookupDefinition.putAll(DB_FIELD_PRIMARY_INDUSTRY, this.primaryIndustry.databaseFields());
        }
        if (this.secondaryIndustry1 != null) {
            lookupDefinition.putAll(DB_FIELD_SECONDARY_INDUSTRY_1, this.secondaryIndustry1.databaseFields());
        }
        if (this.secondaryIndustry2 != null) {
            lookupDefinition.putAll(DB_FIELD_SECONDARY_INDUSTRY_2, this.secondaryIndustry2.databaseFields());
        }
        if (this.secondaryIndustry3 != null) {
            lookupDefinition.putAll(DB_FIELD_SECONDARY_INDUSTRY_3, this.secondaryIndustry3.databaseFields());
        }
        if (this.name != null) {
            lookupDefinition.putAll(DB_FIELD_NAME, this.name.databaseFields());
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
        if (this.isPrimary != null) {
            lookupDefinition.putAll(DB_FIELD_IS_PRIMARY, this.isPrimary.databaseFields());
        }
        if (this.associatedCvrNumber != null) {
            lookupDefinition.putAll(DB_FIELD_CVR_NUMBER, DetailData.listDatabaseFields(this.associatedCvrNumber));
        }
        if (this.participantRelationData != null) {
            lookupDefinition.putAll(DB_FIELD_PARTICIPANT_RELATIONS, DetailData.listDatabaseFields(this.participantRelationData));
        }
        return lookupDefinition;
    }

    public void forceLoad(Session session) {
        Hibernate.initialize(this.yearlyEmployeeNumbersData);
        Hibernate.initialize(this.quarterlyEmployeeNumbersData);
        Hibernate.initialize(this.monthlyEmployeeNumbersData);
        Hibernate.initialize(this.participantData);
        Hibernate.initialize(this.attributeData);
        Hibernate.initialize(this.participantRelationData);
    }
}
