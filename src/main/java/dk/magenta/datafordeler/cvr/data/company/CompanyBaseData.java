package dk.magenta.datafordeler.cvr.data.company;

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
import dk.magenta.datafordeler.cvr.data.unversioned.CompanyForm;
import dk.magenta.datafordeler.cvr.data.unversioned.CompanyStatus;
import dk.magenta.datafordeler.cvr.data.unversioned.Industry;
import org.hibernate.Hibernate;
import org.hibernate.Session;
import org.hibernate.annotations.SortComparator;

import javax.persistence.*;
import java.lang.reflect.Field;
import java.time.OffsetDateTime;
import java.util.*;

/**
 * Base class for Company data, linking to Effects and delegating storage to referred classes
 */
@Entity
@Table(name="cvr_company_basedata", indexes = {
        @Index(name = "cvr_company_lastUpdated", columnList = DataItem.DB_FIELD_LAST_UPDATED),
        @Index(name = "cvr_company_form", columnList = CompanyBaseData.DB_FIELD_FORM + DatabaseEntry.REF),
        @Index(name = "cvr_company_advertprotection", columnList = CompanyBaseData.DB_FIELD_ADVERTPROTECTION + DatabaseEntry.REF),
        @Index(name = "cvr_company_name", columnList = CompanyBaseData.DB_FIELD_NAME),
        @Index(name = "cvr_company_phone", columnList = CompanyBaseData.DB_FIELD_PHONENUMBER + DatabaseEntry.REF),
        @Index(name = "cvr_company_fax", columnList = CompanyBaseData.DB_FIELD_FAXNUMBER + DatabaseEntry.REF),
        @Index(name = "cvr_company_email", columnList = CompanyBaseData.DB_FIELD_EMAIL + DatabaseEntry.REF),
        @Index(name = "cvr_company_location", columnList = CompanyBaseData.DB_FIELD_LOCATION_ADDRESS + DatabaseEntry.REF)
})
public class CompanyBaseData extends CvrData<CompanyEffect, CompanyBaseData> {

    public static final String DB_FIELD_FORM = "companyForm";
    public static final String IO_FIELD_FORM = "virksomhedsform";

    @OneToOne(optional = true, cascade = CascadeType.ALL)
    @JoinColumn(name = DB_FIELD_FORM + DatabaseEntry.REF)
    private CompanyFormData companyForm;

    public CompanyForm getCompanyForm() {
        if (companyForm != null) {
            return companyForm.getCompanyForm();
        } else {
            return null;
        }
    }

    public void setCompanyForm(CompanyForm form) {
        if (this.companyForm == null) {
            this.companyForm = new CompanyFormData();
        }
        this.companyForm.setCompanyForm(form);
    }
    public boolean hasForm() {
        return this.companyForm != null;
    }


    //--------------------------------------------------------------------------


    public static final String DB_FIELD_STATUS = "status";
    public static final String IO_FIELD_STATUS = "status";

    @OneToOne(optional = true, cascade = CascadeType.ALL)
    @JoinColumn(name = DB_FIELD_STATUS + DatabaseEntry.REF)
    private CompanyStatusData status;

    public CompanyStatusData getStatus() {
        return status;
    }

    public String getStatusCode() {
        if (this.status != null) {
            CompanyStatus companyStatus = this.status.getStatus();
            if (companyStatus != null) {
                return companyStatus.getStatus();
            }
        }
        return null;
    }

    public void setStatus(CompanyStatus status) {
        if (this.status == null) {
            this.status = new CompanyStatusData();
        }
        this.status.setStatus(status);
    }


    //--------------------------------------------------------------------------


    public static final String DB_FIELD_LIFECYCLE = "lifecycleData";
    public static final String IO_FIELD_LIFECYCLE = "livscyklus";

    @OneToOne(optional = true, cascade = CascadeType.ALL)
    @JoinColumn(name = DB_FIELD_LIFECYCLE + DatabaseEntry.REF)
    private LifecycleData lifecycleData;

    @JsonProperty(value = IO_FIELD_LIFECYCLE)
    public LifecycleData getLifecycleData() {
        return lifecycleData;
    }

    public void setLivsforloebStart(OffsetDateTime startDate) {
        if (this.lifecycleData == null) {
            this.lifecycleData = new LifecycleData();
        }
        this.lifecycleData.setStartDate(startDate);
    }

    public void setLivsforloebSlut(OffsetDateTime endDate) {
        if (this.lifecycleData == null) {
            this.lifecycleData = new LifecycleData();
        }
        this.lifecycleData.setEndDate(endDate);
    }


    //--------------------------------------------------------------------------


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

    public void setAdvertProtection(boolean advertProtection) {
        if (this.advertProtection == null) {
            this.advertProtection = new BooleanData(BooleanData.Type.REKLAME_BESKYTTELSE);
        }
        this.advertProtection.setValue(advertProtection);
    }


    //--------------------------------------------------------------------------


    public static final String DB_FIELD_CVR = "cvrNumber";
    public static final String IO_FIELD_CVR = "CVRNummer";

    @OneToOne(optional = true, cascade = CascadeType.ALL)
    private IntegerData cvrNumber;

    @JsonProperty(value = IO_FIELD_CVR)
    public Long getCvrNumber() {
        if (cvrNumber != null){
            return cvrNumber.getValue();
        } else {
            return null;
        }
    }

    public void setCvrNumber(long unitNumber) {
        if (this.cvrNumber == null) {
            this.cvrNumber = new IntegerData();
        }
        this.cvrNumber.setValue(unitNumber);
    }


    //--------------------------------------------------------------------------


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

    public void setLocationAddress(Address address) {
        if (this.locationAddress == null) {
            this.locationAddress = new AddressData();
        }
        this.locationAddress.setAddress(address);
    }


    //--------------------------------------------------------------------------


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

    public void setPostalAddress(Address address) {
        if (this.postalAddress == null) {
            this.postalAddress = new AddressData();
        }
        this.postalAddress.setAddress(address);
    }


    //--------------------------------------------------------------------------


    public static final String DB_FIELD_YEARLY_NUMBERS = "yearlyEmployeeNumbersData";
    public static final String IO_FIELD_YEARLY_NUMBERS = "aarsbeskaeftigelse";

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "companyBaseData")
    @OrderBy(value = CompanyYearlyEmployeeNumbersData.DB_FIELD_YEAR + " asc")
    private List<CompanyYearlyEmployeeNumbersData> yearlyEmployeeNumbersData;

    @JsonProperty(value = IO_FIELD_YEARLY_NUMBERS)
    public List<CompanyYearlyEmployeeNumbersData> getYearlyEmployeeNumbersData() {
        return yearlyEmployeeNumbersData;
    }

    public void setYearlyEmployeeNumbers(int year, Integer employeesLow, Integer employeesHigh, Integer fulltimeEquivalentLow, Integer fulltimeEquivalentHigh, Integer includingOwnersLow, Integer includingOwnersHigh) throws ParseException {
        if (this.yearlyEmployeeNumbersData == null) {
            this.yearlyEmployeeNumbersData = new ArrayList<>();
        }
        CompanyYearlyEmployeeNumbersData yearlyEmployeeNumbersData = null;
        for (CompanyYearlyEmployeeNumbersData data : this.yearlyEmployeeNumbersData) {
            if (data.getYear() == year) {
                yearlyEmployeeNumbersData = data;
            }
        }
        if (yearlyEmployeeNumbersData == null) {
            yearlyEmployeeNumbersData = new CompanyYearlyEmployeeNumbersData();
            yearlyEmployeeNumbersData.setCompanyBaseData(this);
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


    //--------------------------------------------------------------------------


    public static final String DB_FIELD_QUARTERLY_NUMBERS = "quarterlyEmployeeNumbersData";
    public static final String IO_FIELD_QUARTERLY_NUMBERS = "kvartalsbeskaeftigelse";

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "companyBaseData")
    @OrderBy(value = CompanyQuarterlyEmployeeNumbersData.DB_FIELD_YEAR + " asc, " + CompanyQuarterlyEmployeeNumbersData.DB_FIELD_QUARTER + " asc")
    private List<CompanyQuarterlyEmployeeNumbersData> quarterlyEmployeeNumbersData;

    @JsonProperty(value = IO_FIELD_QUARTERLY_NUMBERS)
    public List<CompanyQuarterlyEmployeeNumbersData> getQuarterlyEmployeeNumbersData() {
        return quarterlyEmployeeNumbersData;
    }

    public void setQuarterlyEmployeeNumbers(int year, int quarter, Integer employeesLow, Integer employeesHigh, Integer fulltimeEquivalentLow, Integer fulltimeEquivalentHigh, Integer includingOwnersLow, Integer includingOwnersHigh) throws ParseException {
        if (this.quarterlyEmployeeNumbersData == null) {
            this.quarterlyEmployeeNumbersData = new ArrayList<>();
        }
        CompanyQuarterlyEmployeeNumbersData quarterlyEmployeeNumbersData = null;
        for (CompanyQuarterlyEmployeeNumbersData data : this.quarterlyEmployeeNumbersData) {
            if (data.getYear() == year && data.getQuarter() == quarter) {
                quarterlyEmployeeNumbersData = data;
            }
        }
        if (quarterlyEmployeeNumbersData == null) {
            quarterlyEmployeeNumbersData = new CompanyQuarterlyEmployeeNumbersData();
            quarterlyEmployeeNumbersData.setCompanyBaseData(this);
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


    //--------------------------------------------------------------------------


    public static final String DB_FIELD_MONTHLY_NUMBERS = "monthlyEmployeeNumbersData";
    public static final String IO_FIELD_MONTHLY_NUMBERS = "maanedsbeskaeftigelse";

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "companyBaseData")
    @OrderBy(value = MonthlyEmployeeNumbersData.DB_FIELD_YEAR + " asc, "+MonthlyEmployeeNumbersData.DB_FIELD_MONTH+" asc")
    private List<CompanyMonthlyEmployeeNumbersData> monthlyEmployeeNumbersData;

    @JsonProperty(value = IO_FIELD_MONTHLY_NUMBERS)
    public List<CompanyMonthlyEmployeeNumbersData> getMonthlyEmployeeNumbersData() {
        return monthlyEmployeeNumbersData;
    }

    public void setMonthlyEmployeeNumbers(int year, int month, Integer employeesLow, Integer employeesHigh, Integer fulltimeEquivalentLow, Integer fulltimeEquivalentHigh, Integer includingOwnersLow, Integer includingOwnersHigh) throws ParseException {
        if (this.monthlyEmployeeNumbersData == null) {
            this.monthlyEmployeeNumbersData = new ArrayList<>();
        }
        CompanyMonthlyEmployeeNumbersData monthlyEmployeeNumbersData = null;
        for (CompanyMonthlyEmployeeNumbersData data : this.monthlyEmployeeNumbersData) {
            if (data.getYear() == year && data.getMonth() == month) {
                monthlyEmployeeNumbersData = data;
            }
        }
        if (monthlyEmployeeNumbersData == null) {
            monthlyEmployeeNumbersData = new CompanyMonthlyEmployeeNumbersData();
            monthlyEmployeeNumbersData.setCompanyBaseData(this);
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


    //--------------------------------------------------------------------------


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

    public void setPrimaryIndustry(Industry industry) {
        if (this.primaryIndustry == null) {
            this.primaryIndustry = new IndustryData(true);
        }
        this.primaryIndustry.setIndustry(industry);
    }


    //--------------------------------------------------------------------------


    public static final String DB_FIELD_SECONDARY_INDUSTRY_1 = "secondaryIndustry1";
    public static final String IO_FIELD_SECONDARY_INDUSTRY_1 = "bibranche1";
    @OneToOne(optional = true, cascade = CascadeType.ALL)
    @JoinColumn(name = DB_FIELD_SECONDARY_INDUSTRY_1 + DatabaseEntry.REF)
    private IndustryData secondaryIndustry1;

    @JsonProperty(value = IO_FIELD_SECONDARY_INDUSTRY_1)
    public Industry getSecondaryIndustry1() {
        if(secondaryIndustry1 != null)
            return secondaryIndustry1.getIndustry();
        else
            return null;
    }

    public void setSecondaryIndustry1(Industry industry) {
        if (this.secondaryIndustry1 == null) {
            this.secondaryIndustry1 = new IndustryData(false);
        }
        this.secondaryIndustry1.setIndustry(industry);
    }


    //--------------------------------------------------------------------------


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

    public void setSecondaryIndustry2(Industry industry) {
        if (this.secondaryIndustry2 == null) {
            this.secondaryIndustry2 = new IndustryData(false);
        }
        this.secondaryIndustry2.setIndustry(industry);
    }


    //--------------------------------------------------------------------------


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

    public void setSecondaryIndustry3(Industry industry) {
        if (this.secondaryIndustry3 == null) {
            this.secondaryIndustry3 = new IndustryData(false);
        }
        this.secondaryIndustry3.setIndustry(industry);
    }


    //--------------------------------------------------------------------------


    public static final String DB_FIELD_NAME = "companyName";
    public static final String IO_FIELD_NAME = "navn";

    @Column(length = 1024, name = DB_FIELD_NAME)
    private String companyName;

    @JsonProperty(value = IO_FIELD_NAME)
    public String getCompanyName() {
        return this.companyName;
    }

    public void setCompanyName(String name) {
        this.companyName = name;
    }


    //--------------------------------------------------------------------------


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

    public void setFaxNumber(String fax, boolean secret) {
        if (this.faxNumber == null) {
            this.faxNumber = new ContactData(ContactData.Type.TELEFAXNUMMER);
        }
        this.faxNumber.setValue(fax);
        this.faxNumber.setSecret(secret);
    }


    //--------------------------------------------------------------------------


    public static final String DB_FIELD_HOMEPAGE = "homepage";
    public static final String IO_FIELD_HOMEPAGE = "hjemmeside";

    @OneToOne(optional = true, cascade = CascadeType.ALL)
    private ContactData homepage;

    @JsonProperty(value = IO_FIELD_HOMEPAGE)
    public ContactData getHomepage() {
        return this.homepage;
    }

    public void setHomepage(String email, boolean secret) {
        if (this.homepage == null) {
            this.homepage = new ContactData(ContactData.Type.HJEMMESIDE);
        }
        this.homepage.setValue(email);
        this.homepage.setSecret(secret);
    }


    //--------------------------------------------------------------------------


    public static final String DB_FIELD_MANDATORY_EMAIL = "mandatoryEmail";
    public static final String IO_FIELD_MANDATORY_EMAIL = "obligatoriskEmail";

    @OneToOne(optional = true, cascade = CascadeType.ALL)
    private ContactData mandatoryEmail;

    @JsonProperty(IO_FIELD_MANDATORY_EMAIL)
    public ContactData getMandatoryEmail() {
        return this.mandatoryEmail;
    }

    public void setMandatoryEmail(String fax, boolean secret) {
        if (this.mandatoryEmail == null) {
            this.mandatoryEmail = new ContactData(ContactData.Type.OBLIGATORISK_EMAILADRESSE);
        }
        this.mandatoryEmail.setValue(fax);
        this.mandatoryEmail.setSecret(secret);
    }


    //--------------------------------------------------------------------------


    public static final String DB_FIELD_UNITS = "unitData";
    public static final String IO_FIELD_UNITS = "produktionsenheder";

    @ManyToMany(cascade = CascadeType.ALL)
    private Set<CompanyUnitLink> unitData = new HashSet<>();

    @JsonProperty(value = IO_FIELD_UNITS)
    public Set<CompanyUnitLink> getUnitData() {
        return this.unitData;
    }

    public void addCompanyUnit(int pNumber, Identification identification) {
        for (CompanyUnitLink link : this.unitData) {
            if (link.getpNumber() == pNumber) {
                return;
            }
        }
        CompanyUnitLink link = new CompanyUnitLink();
        link.setpNumber(pNumber);
        link.setIdentification(identification);
        this.unitData.add(link);
    }


    //--------------------------------------------------------------------------


    public static final String DB_FIELD_PARTICIPANTS = "participantData";
    public static final String IO_FIELD_PARTICIPANTS = "deltagere";

    @ManyToMany(mappedBy = ParticipantLink.DB_FIELD_COMPANYBASES)
    private Set<ParticipantLink> participantData = new HashSet<>();

    @JsonProperty(value = IO_FIELD_PARTICIPANTS)
    public Set<ParticipantLink> getParticipantData() {
        return this.participantData;
    }

    public void addParticipant(ParticipantLink deltagerlink) {
        this.participantData.add(deltagerlink);
    }


    //--------------------------------------------------------------------------


    public static final String DB_FIELD_ATTRIBUTES = "attributeData";
    public static final String IO_FIELD_ATTRIBUTES = "attributter";

    @OneToMany(cascade = CascadeType.ALL, mappedBy = AttributeData.DB_FIELD_COMPANYBASE)
    @OrderBy(value = AttributeData.DB_FIELD_TYPE)
    private SortedSet<AttributeData> attributeData = new TreeSet<>();

    @JsonProperty(value = IO_FIELD_ATTRIBUTES)
    public Set<AttributeData> getAttributes() {
        return this.attributeData;
    }

    public void addAttribute(String type, String valueType, String value, int sequenceNumber) {
        AttributeData attributeData = null;
        for (AttributeData data : this.attributeData) {
            if (data.getType().equals(type) && data.getSequenceNumber() == sequenceNumber && data.getValueType().equals(valueType) && data.getValue().equals(value)) {
                attributeData = data;
            }
        }
        if (attributeData == null) {
            attributeData = new AttributeData();
            this.addAttribute(attributeData);
        }
        attributeData.setType(type);
        attributeData.setValueType(valueType);
        attributeData.setValue(value);
        attributeData.setSequenceNumber(sequenceNumber);
    }

    public void addAttribute(AttributeData attributeData) {
        attributeData.setCompanyBaseData(this);
        this.attributeData.add(attributeData);
    }


    //--------------------------------------------------------------------------


    public static final String DB_FIELD_PARTICIPANT_RELATIONS = "participantRelationData";
    public static final String IO_FIELD_PARTICIPANT_RELATIONS = "deltagerRelationer";

    @OneToMany(cascade = CascadeType.ALL)
    @SortComparator(ParticipantRelationData.Comparator.class)
    private SortedSet<ParticipantRelationData> participantRelationData = new TreeSet<>();

    @JsonProperty(value = IO_FIELD_PARTICIPANT_RELATIONS)
    public Set<ParticipantRelationData> getParticipantRelations() {
        return this.participantRelationData;
    }

    public void addParticipantRelation(Identification participant, Set<Identification> organizations) {
        ParticipantRelationData participantRelationData = null;
        for (ParticipantRelationData data : this.participantRelationData) {
            if (participant != null && data.getParticipant() != null && data.getParticipant().equals(participant)) {
                participantRelationData = data;
            }
        }
        if (participantRelationData == null) {
            participantRelationData = new ParticipantRelationData();
            this.participantRelationData.add(participantRelationData);
        }
        if (participant != null) {
            participantRelationData.setParticipant(participant);
        }
        for (Identification organization : organizations) {
            participantRelationData.addOrganization(organization);
        }
    }


    //--------------------------------------------------------------------------


    @Override
    public Map<String, Object> asMap() {
        HashMap<String, Object> map = new HashMap<>();
        if (this.companyForm != null) {
            map.put("companyForm", this.companyForm.getCompanyForm());
        }
        if (this.status != null) {
            map.put("status", this.status.getStatus());
        }
        if (this.lifecycleData != null) {
            map.put("lifecycleData", this.lifecycleData.asMap());
        }
        if (this.advertProtection != null) {
            map.put("advertProtection", this.advertProtection.getValue());
        }
        if (this.cvrNumber != null) {
            map.put("CVRNumber", this.cvrNumber.getValue());
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
            map.put("primaryIndustry", this.primaryIndustry.getIndustry());
        }
        if (this.secondaryIndustry1 != null) {
            map.put("secondaryIndustry1", this.secondaryIndustry1.getIndustry());
        }
        if (this.secondaryIndustry2 != null) {
            map.put("secondaryIndustry2", this.secondaryIndustry2.getIndustry());
        }
        if (this.secondaryIndustry3 != null) {
            map.put("secondaryIndustry3", this.secondaryIndustry3.getIndustry());
        }
        if (this.companyName != null) {
            map.put("virksomhedsnavn", this.companyName);
        }
        if (this.phoneNumber != null) {
            map.put("phoneNumber", this.phoneNumber);
        }
        if (this.emailAddress != null) {
            map.put("emailAddress", this.emailAddress);
        }
        if (this.faxNumber != null) {
            map.put("faxNumber", this.faxNumber);
        }
        if (this.homepage != null) {
            map.put("homepage", this.homepage);
        }
        if (this.mandatoryEmail != null) {
            map.put("mandatoryEmail", this.mandatoryEmail);
        }
        if (this.unitData != null && !this.unitData.isEmpty()) {
            map.put("unitData", this.unitData);
        }
        if (this.participantData != null && !this.participantData.isEmpty()) {
            map.put("participantData", this.participantData);
        }
        if (this.attributeData != null && !this.attributeData.isEmpty()) {
            map.put("attributes", this.attributeData);
        }
        if (this.participantRelationData != null && !this.participantRelationData.isEmpty()) {
            map.put("deltagerRelation", this.participantRelationData);
        }
        return map;
    }


    @Override
    public LookupDefinition getLookupDefinition() {
        LookupDefinition lookupDefinition = new LookupDefinition(CompanyBaseData.class);
        lookupDefinition.setMatchNulls(true);

        if (this.lifecycleData != null) {
            lookupDefinition.putAll(DB_FIELD_LIFECYCLE, this.lifecycleData.databaseFields());
        }
        if (this.companyForm != null) {
            lookupDefinition.putAll(DB_FIELD_FORM, this.companyForm.databaseFields());
        }
        if (this.status != null) {
            lookupDefinition.putAll(DB_FIELD_STATUS, this.status.databaseFields());
        }
        if (this.advertProtection != null) {
            lookupDefinition.putAll(DB_FIELD_ADVERTPROTECTION, this.advertProtection.databaseFields());
        }
        if (this.cvrNumber != null) {
            lookupDefinition.putAll(DB_FIELD_CVR, this.cvrNumber.databaseFields());
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
        if (this.companyName != null) {
            lookupDefinition.put(DB_FIELD_NAME, this.companyName);
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
        if (this.participantRelationData != null) {
            lookupDefinition.putAll(DB_FIELD_PARTICIPANT_RELATIONS, DetailData.listDatabaseFields(this.participantRelationData));
        }
        return lookupDefinition;
    }

    public void forceLoad(Session session) {
        for (Field field : this.getClass().getDeclaredFields()) {
            try {
                Hibernate.initialize(field.get(this));
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        Hibernate.initialize(this.yearlyEmployeeNumbersData);
        Hibernate.initialize(this.quarterlyEmployeeNumbersData);
        Hibernate.initialize(this.monthlyEmployeeNumbersData);
        Hibernate.initialize(this.unitData);
        Hibernate.initialize(this.participantData);
        Hibernate.initialize(this.attributeData);
        for (ParticipantRelationData participantRelationData : this.participantRelationData) {
            participantRelationData.forceLoad(session);
        }
    }
}
