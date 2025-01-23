package dk.magenta.datafordeler.ger.data.company;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import dk.magenta.datafordeler.core.database.IdentifiedEntity;
import dk.magenta.datafordeler.ger.GerPlugin;
import dk.magenta.datafordeler.ger.data.GerEntity;
import dk.magenta.datafordeler.ger.data.RawData;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = GerPlugin.DEBUG_TABLE_PREFIX + CompanyEntity.TABLE_NAME, indexes = {
        @Index(
                name = GerPlugin.DEBUG_TABLE_PREFIX + CompanyEntity.TABLE_NAME + CompanyEntity.DB_FIELD_GERNR,
                columnList = CompanyEntity.DB_FIELD_GERNR
        ),
        @Index(name = GerPlugin.DEBUG_TABLE_PREFIX + CompanyEntity.TABLE_NAME + CompanyEntity.DB_FIELD_DAFO_UPDATED, columnList = CompanyEntity.DB_FIELD_DAFO_UPDATED)
})
public class CompanyEntity extends GerEntity implements IdentifiedEntity {

    public static final String TABLE_NAME = "ger_company";

    @JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "type")
    public static final String schema = "Company";


    public CompanyEntity() {
    }

    public CompanyEntity(RawData record) {
        this.setGerNr(record.getInt("GERNR"));
    }

    public static UUID generateUUID(int gerNr) {
        String uuidInput = "company:" + gerNr;
        return UUID.nameUUIDFromBytes(uuidInput.getBytes());
    }


    public static final String DB_FIELD_GERNR = "gerNr";
    public static final String IO_FIELD_GERNR = "gerNr";
    @Column(name = DB_FIELD_GERNR)
    @JsonProperty(value = DB_FIELD_GERNR)
    private int gerNr;

    public int getGerNr() {
        return this.gerNr;
    }

    public void setGerNr(int gerNr) {
        this.gerNr = gerNr;
    }


    public static final String DB_FIELD_VALIDATED = "validated";
    public static final String IO_FIELD_VALIDATED = "valideret";
    @Column(name = DB_FIELD_VALIDATED)
    @JsonProperty(value = IO_FIELD_VALIDATED)
    private boolean validated;

    public boolean isValidated() {
        return this.validated;
    }

    public void setValidated(boolean validated) {
        this.validated = validated;
    }

    public static final String DB_FIELD_NAME = "name";
    public static final String IO_FIELD_NAME = "navn";
    @Column(name = DB_FIELD_NAME)
    @JsonProperty(value = IO_FIELD_NAME)
    private String name;

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }


    public static final String DB_FIELD_DANISH_NAME = "danishName";
    public static final String IO_FIELD_DANISH_NAME = "danskNavn";
    @Column(name = DB_FIELD_DANISH_NAME)
    @JsonProperty(value = IO_FIELD_DANISH_NAME)
    private String danishName;

    public String getDanishName() {
        return this.danishName;
    }

    public void setDanishName(String danishName) {
        this.danishName = danishName;
    }

    public static final String DB_FIELD_SHORT_NAME = "shortName";
    public static final String IO_FIELD_SHORT_NAME = "kortNavn";
    @Column(name = DB_FIELD_SHORT_NAME)
    @JsonProperty(value = IO_FIELD_SHORT_NAME)
    private String shortName;

    public String getShortName() {
        return this.shortName;
    }

    public void setShortName(String shortName) {
        this.shortName = shortName;
    }

    public static final String DB_FIELD_SHORT_DANISH_NAME = "shortDanishName";
    public static final String IO_FIELD_SHORT_DANISH_NAME = "kortDanskNavn";
    @Column(name = DB_FIELD_SHORT_DANISH_NAME)
    @JsonProperty(value = IO_FIELD_SHORT_DANISH_NAME)
    private String shortDanishName;

    public String getShortDanishName() {
        return this.shortDanishName;
    }

    public void setShortDanishName(String shortDanishName) {
        this.shortDanishName = shortDanishName;
    }

    public static final String DB_FIELD_CO_NAME = "coName";
    public static final String IO_FIELD_CO_NAME = "coNavn";
    @Column(name = DB_FIELD_CO_NAME)
    @JsonProperty(value = IO_FIELD_CO_NAME)
    private String coName;

    public String getCoName() {
        return this.coName;
    }

    public void setCoName(String coName) {
        this.coName = coName;
    }

    public static final String DB_FIELD_ADDRESS1 = "address1";
    public static final String IO_FIELD_ADDRESS1 = "adresse1";
    @Column(name = DB_FIELD_ADDRESS1)
    @JsonProperty(value = IO_FIELD_ADDRESS1)
    private String address1;

    public String getAddress1() {
        return this.address1;
    }

    public void setAddress1(String address1) {
        this.address1 = address1;
    }

    public static final String DB_FIELD_ADDRESS2 = "address2";
    public static final String IO_FIELD_ADDRESS2 = "adresse2";
    @Column(name = DB_FIELD_ADDRESS2)
    @JsonProperty(value = IO_FIELD_ADDRESS2)
    private String address2;

    public String getAddress2() {
        return this.address2;
    }

    public void setAddress2(String address2) {
        this.address2 = address2;
    }

    public static final String DB_FIELD_ADDRESS3 = "address3";
    public static final String IO_FIELD_ADDRESS3 = "adresse3";
    @Column(name = DB_FIELD_ADDRESS3)
    @JsonProperty(value = IO_FIELD_ADDRESS3)
    private String address3;

    public String getAddress3() {
        return this.address3;
    }

    public void setAddress3(String address3) {
        this.address3 = address3;
    }

    public static final String DB_FIELD_BNR = "bnr";
    public static final String IO_FIELD_BNR = "bnr";
    @Column(name = DB_FIELD_BNR, length = 12)
    @JsonProperty(value = IO_FIELD_BNR)
    private String bnr;

    public String getBnr() {
        return this.bnr;
    }

    public void setBnr(String bnr) {
        this.bnr = bnr;
    }

    public void setBnr(Integer bnr) {
        this.setBnr(bnr == null ? null : bnr.toString());
    }

    public static final String DB_FIELD_BOXNR = "boxNr";
    public static final String IO_FIELD_BOXNR = "boxNr";
    @Column(name = DB_FIELD_BOXNR)
    @JsonProperty(value = IO_FIELD_BOXNR)
    private String boxNr;

    public String getBoxNr() {
        return this.boxNr;
    }

    public void setBoxNr(String boxNr) {
        this.boxNr = boxNr;
    }

    public static final String DB_FIELD_POSTNR = "postNr";
    public static final String IO_FIELD_POSTNR = "postNr";
    @Column(name = DB_FIELD_POSTNR)
    @JsonProperty(value = IO_FIELD_POSTNR)
    private Integer postNr;

    public Integer getPostNr() {
        return this.postNr;
    }

    public void setPostNr(Integer postNr) {
        this.postNr = postNr;
    }

    public static final String DB_FIELD_OLD_TAX = "oldTaxKom";
    public static final String IO_FIELD_OLD_TAX = "gammelSkatKom";
    @Column(name = DB_FIELD_OLD_TAX)
    @JsonProperty(value = IO_FIELD_OLD_TAX)
    private Integer oldTaxKom;

    public Integer getOldTaxKom() {
        return this.oldTaxKom;
    }

    public void setOldTaxKom(Integer oldTaxKom) {
        this.oldTaxKom = oldTaxKom;
    }

    public static final String DB_FIELD_MUNICIPALITY_CODE = "municipalityCode";
    public static final String IO_FIELD_MUNICIPALITY_CODE = "kommuneKode";
    @Column(name = DB_FIELD_MUNICIPALITY_CODE)
    @JsonProperty(value = IO_FIELD_MUNICIPALITY_CODE)
    private Integer municipalityCode;

    public Integer getMunicipalityCode() {
        return this.municipalityCode;
    }

    public void setMunicipalityCode(Integer municipalityCode) {
        this.municipalityCode = municipalityCode;
    }

    public static final String DB_FIELD_LOCALITY_CODE = "localityCode";
    public static final String IO_FIELD_LOCALITY_CODE = "stedKode";
    @Column(name = DB_FIELD_LOCALITY_CODE)
    @JsonProperty(value = IO_FIELD_LOCALITY_CODE)
    private Integer localityCode;

    public Integer getLocalityCode() {
        return this.localityCode;
    }

    public void setLocalityCode(Integer localityCode) {
        this.localityCode = localityCode;
    }

    public static final String DB_FIELD_COUNTRY_CODE = "countryCode";
    public static final String IO_FIELD_COUNTRY_CODE = "landeKode";
    @Column(name = DB_FIELD_COUNTRY_CODE)
    @JsonProperty(value = IO_FIELD_COUNTRY_CODE)
    private Integer countryCode;

    public Integer getCountryCode() {
        return this.countryCode;
    }

    public void setCountryCode(Integer countryCode) {
        this.countryCode = countryCode;
    }

    public static final String DB_FIELD_PHONE = "phone";
    public static final String IO_FIELD_PHONE = "telefonNummer";
    @Column(name = DB_FIELD_PHONE)
    @JsonProperty(value = IO_FIELD_PHONE)
    private String phone;

    public String getPhone() {
        return this.phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public static final String DB_FIELD_FAX = "fax";
    public static final String IO_FIELD_FAX = "faxNummer";
    @Column(name = DB_FIELD_FAX)
    @JsonProperty(value = IO_FIELD_FAX)
    private String fax;

    public String getFax() {
        return this.fax;
    }

    public void setFax(String fax) {
        this.fax = fax;
    }

    public static final String DB_FIELD_MOBILEPHONE = "mobilePhone";
    public static final String IO_FIELD_MOBILEPHONE = "mobilNummer";
    @Column(name = DB_FIELD_MOBILEPHONE)
    @JsonProperty(value = IO_FIELD_MOBILEPHONE)
    private String mobilePhone;

    public String getMobilePhone() {
        return this.mobilePhone;
    }

    public void setMobilePhone(String mobilePhone) {
        this.mobilePhone = mobilePhone;
    }

    public static final String DB_FIELD_EMAIL = "email";
    public static final String IO_FIELD_EMAIL = "email";
    @Column(name = DB_FIELD_EMAIL)
    @JsonProperty(value = IO_FIELD_EMAIL)
    private String email;

    public String getEmail() {
        return this.email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public static final String DB_FIELD_OPERATION_FORM_CODE = "operationFormCode";
    public static final String IO_FIELD_OPERATION_FORM_CODE = "driftsformKode";
    @Column(name = DB_FIELD_OPERATION_FORM_CODE)
    @JsonProperty(value = IO_FIELD_OPERATION_FORM_CODE)
    private Integer operationFormCode;

    public Integer getOperationFormCode() {
        return this.operationFormCode;
    }

    public void setOperationFormCode(Integer operationFormCode) {
        this.operationFormCode = operationFormCode;
    }

    public static final String DB_FIELD_OPERATION_FORM_TEXT = "operationFormText";
    public static final String IO_FIELD_OPERATION_FORM_TEXT = "driftsformTekst";
    @Column(name = DB_FIELD_OPERATION_FORM_TEXT)
    @JsonProperty(value = IO_FIELD_OPERATION_FORM_TEXT)
    private String operationFormText;

    public String getOperationFormText() {
        return this.operationFormText;
    }

    public void setOperationFormText(String operationFormText) {
        this.operationFormText = operationFormText;
    }

    public static final String DB_FIELD_BUSINESS_CODE = "businessCode";
    public static final String IO_FIELD_BUSINESS_CODE = "brancheKode";
    @Column(name = DB_FIELD_BUSINESS_CODE)
    @JsonProperty(value = IO_FIELD_BUSINESS_CODE)
    private Integer businessCode;

    public Integer getBusinessCode() {
        return this.businessCode;
    }

    public void setBusinessCode(Integer businessCode) {
        this.businessCode = businessCode;
    }

    public static final String DB_FIELD_BUSINESS_TEXT = "businessText";
    public static final String IO_FIELD_BUSINESS_TEXT = "brancheTekst";
    @Column(name = DB_FIELD_BUSINESS_TEXT)
    @JsonProperty(value = IO_FIELD_BUSINESS_TEXT)
    private String businessText;

    public String getBusinessText() {
        return this.businessText;
    }

    public void setBusinessText(String businessText) {
        this.businessText = businessText;
    }

    public static final String DB_FIELD_START_DATE = "startDate";
    public static final String IO_FIELD_START_DATE = "startDato";
    @Column(name = DB_FIELD_START_DATE)
    @JsonProperty(value = IO_FIELD_START_DATE)
    private LocalDate startDate;

    public LocalDate getStartDate() {
        return this.startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public static final String DB_FIELD_END_DATE = "endDate";
    public static final String IO_FIELD_END_DATE = "slutDato";
    @Column(name = DB_FIELD_END_DATE)
    @JsonProperty(value = IO_FIELD_END_DATE)
    private LocalDate endDate;

    public LocalDate getEndDate() {
        return this.endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public static final String DB_FIELD_LNR = "lnr";
    public static final String IO_FIELD_LNR = "lnr";
    @Column(name = DB_FIELD_LNR)
    @JsonProperty(value = IO_FIELD_LNR)
    private Integer lnr;

    public Integer getLnr() {
        return this.lnr;
    }

    public void setLnr(Integer lnr) {
        this.lnr = lnr;
    }

    public static final String DB_FIELD_MEMO = "memo";
    public static final String IO_FIELD_MEMO = "notat";
    @Column(name = DB_FIELD_MEMO, length = 5000)
    @JsonProperty(value = IO_FIELD_MEMO)
    private String memo;

    public String getMemo() {
        return this.memo;
    }

    public void setMemo(String memo) {
        this.memo = memo;
    }

    public static final String DB_FIELD_FOUND_DATE = "foundDate";
    public static final String IO_FIELD_FOUND_DATE = "opretDato";
    @Column(name = DB_FIELD_FOUND_DATE)
    @JsonProperty(value = IO_FIELD_FOUND_DATE)
    private LocalDate foundDate;

    public LocalDate getFoundDate() {
        return this.foundDate;
    }

    public void setFoundDate(LocalDate foundDate) {
        this.foundDate = foundDate;
    }

    public static final String DB_FIELD_AJOUR_DATE = "ajourDate";
    public static final String IO_FIELD_AJOUR_DATE = "ajourDato";
    @Column(name = DB_FIELD_AJOUR_DATE)
    @JsonProperty(value = IO_FIELD_AJOUR_DATE)
    private LocalDate ajourDate;

    public LocalDate getAjourDate() {
        return this.ajourDate;
    }

    public void setAjourDate(LocalDate ajourDate) {
        this.ajourDate = ajourDate;
    }

    public static final String DB_FIELD_AJOUR_INIT = "ajourInit";
    public static final String IO_FIELD_AJOUR_INIT = "ajourInit";
    @Column(name = DB_FIELD_AJOUR_INIT)
    @JsonProperty(value = IO_FIELD_AJOUR_INIT)
    private String ajourInit;

    public String getAjourInit() {
        return this.ajourInit;
    }

    public void setAjourInit(String ajourInit) {
        this.ajourInit = ajourInit;
    }

    public static final String DB_FIELD_STATUS_GUID = "statusGuid";
    public static final String IO_FIELD_STATUS_GUID = "statusGuid";
    @Column(name = DB_FIELD_STATUS_GUID)
    @JsonProperty(value = IO_FIELD_STATUS_GUID)
    private UUID statusGuid;

    public UUID getStatusGuid() {
        return this.statusGuid;
    }

    public void setStatusGuid(UUID statusGuid) {
        this.statusGuid = statusGuid;
    }

    public static final String DB_FIELD_STATUS_CHANGE = "statusChange";
    public static final String IO_FIELD_STATUS_CHANGE = "statusÆndret";
    @Column(name = DB_FIELD_STATUS_CHANGE)
    @JsonProperty(value = IO_FIELD_STATUS_CHANGE)
    private LocalDate statusChange;

    public LocalDate getStatusChange() {
        return this.statusChange;
    }

    public void setStatusChange(LocalDate statusChange) {
        this.statusChange = statusChange;
    }

    public static final String DB_FIELD_STATUS_INIT = "statusInit";
    public static final String IO_FIELD_STATUS_INIT = "statusInit";
    @Column(name = DB_FIELD_STATUS_INIT)
    @JsonProperty(value = IO_FIELD_STATUS_INIT)
    private String statusInit;

    public String getStatusInit() {
        return this.statusInit;
    }

    public void setStatusInit(String statusInit) {
        this.statusInit = statusInit;
    }

    public static final String DB_FIELD_DOOR = "door";
    public static final String IO_FIELD_DOOR = "sidedør";
    @Column(name = DB_FIELD_DOOR)
    @JsonProperty(value = IO_FIELD_DOOR)
    private String door;

    public String getDoor() {
        return this.door;
    }

    public void setDoor(String door) {
        this.door = door;
    }

    public static final String DB_FIELD_FLOOR = "floor";
    public static final String IO_FIELD_FLOOR = "etage";
    @Column(name = DB_FIELD_FLOOR)
    @JsonProperty(value = IO_FIELD_FLOOR)
    private Integer floor;

    public Integer getFloor() {
        return this.floor;
    }

    public void setFloor(Integer floor) {
        this.floor = floor;
    }

    public static final String DB_FIELD_LETTER_FROM = "letterFrom";
    public static final String IO_FIELD_LETTER_FROM = "bogstavFra";
    @Column(name = DB_FIELD_LETTER_FROM)
    @JsonProperty(value = IO_FIELD_LETTER_FROM)
    private Character letterFrom;

    public Character getLetterFrom() {
        return this.letterFrom;
    }

    public void setLetterFrom(Character letterFrom) {
        this.letterFrom = letterFrom;
    }

    public static final String DB_FIELD_LETTER_TO = "letterTo";
    public static final String IO_FIELD_LETTER_TO = "bogstavTil";
    @Column(name = DB_FIELD_LETTER_TO)
    @JsonProperty(value = IO_FIELD_LETTER_TO)
    private Character letterTo;

    public Character getLetterTo() {
        return this.letterTo;
    }

    public void setLetterTo(Character letterTo) {
        this.letterTo = letterTo;
    }

    public static final String DB_FIELD_HOUSENUMBER_FROM = "houseNumberFrom";
    public static final String IO_FIELD_HOUSENUMBER_FROM = "husnummerFra";
    @Column(name = DB_FIELD_HOUSENUMBER_FROM)
    @JsonProperty(value = IO_FIELD_HOUSENUMBER_FROM)
    private Integer houseNumberFrom;

    public Integer getHouseNumberFrom() {
        return this.houseNumberFrom;
    }

    public void setHouseNumberFrom(Integer houseNumberFrom) {
        this.houseNumberFrom = houseNumberFrom;
    }

    public static final String DB_FIELD_HOUSENUMBER_TO = "houseNumberTo";
    public static final String IO_FIELD_HOUSENUMBER_TO = "husnummerTil";
    @Column(name = DB_FIELD_HOUSENUMBER_TO)
    @JsonProperty(value = IO_FIELD_HOUSENUMBER_TO)
    private Integer houseNumberTo;

    public Integer getHouseNumberTo() {
        return this.houseNumberTo;
    }

    public void setHouseNumberTo(Integer houseNumberTo) {
        this.houseNumberTo = houseNumberTo;
    }

    public static final String DB_FIELD_ROAD_CODE = "roadCode";
    public static final String IO_FIELD_ROAD_CODE = "vejkode";
    @Column(name = DB_FIELD_ROAD_CODE)
    @JsonProperty(value = IO_FIELD_ROAD_CODE)
    private Integer roadCode;

    public Integer getRoadCode() {
        return this.roadCode;
    }

    public void setRoadCode(Integer roadCode) {
        this.roadCode = roadCode;
    }


    public Map<String, Object> asMap() {
        HashMap<String, Object> map = new HashMap<>();

        map.put(IO_FIELD_GERNR, this.gerNr);
        map.put(IO_FIELD_VALIDATED, this.validated);
        map.put(IO_FIELD_NAME, this.name);
        map.put(IO_FIELD_DANISH_NAME, this.danishName);
        map.put(IO_FIELD_SHORT_NAME, this.shortName);
        map.put(IO_FIELD_SHORT_DANISH_NAME, this.shortDanishName);
        map.put(IO_FIELD_CO_NAME, this.coName);
        map.put(IO_FIELD_ADDRESS1, this.address1);
        map.put(IO_FIELD_ADDRESS2, this.address2);
        map.put(IO_FIELD_ADDRESS3, this.address3);
        map.put(IO_FIELD_BNR, this.bnr);
        map.put(IO_FIELD_BOXNR, this.boxNr);
        map.put(IO_FIELD_POSTNR, this.postNr);
        map.put(IO_FIELD_OLD_TAX, this.oldTaxKom);
        map.put(IO_FIELD_MUNICIPALITY_CODE, this.municipalityCode);
        map.put(IO_FIELD_LOCALITY_CODE, this.localityCode);
        map.put(IO_FIELD_COUNTRY_CODE, this.countryCode);
        map.put(IO_FIELD_PHONE, this.phone);
        map.put(IO_FIELD_FAX, this.fax);
        map.put(IO_FIELD_MOBILEPHONE, this.mobilePhone);
        map.put(IO_FIELD_EMAIL, this.email);
        map.put(IO_FIELD_OPERATION_FORM_CODE, this.operationFormCode);
        map.put(IO_FIELD_OPERATION_FORM_TEXT, this.operationFormText);
        map.put(IO_FIELD_BUSINESS_CODE, this.businessCode);
        map.put(IO_FIELD_BUSINESS_TEXT, this.businessText);
        map.put(IO_FIELD_START_DATE, this.startDate);
        map.put(IO_FIELD_END_DATE, this.endDate);
        map.put(IO_FIELD_LNR, this.lnr);
        map.put(IO_FIELD_MEMO, this.memo);
        map.put(IO_FIELD_FOUND_DATE, this.foundDate);
        map.put(IO_FIELD_AJOUR_DATE, this.ajourDate);
        map.put(IO_FIELD_AJOUR_INIT, this.ajourInit);
        map.put(IO_FIELD_STATUS_GUID, this.statusGuid);
        map.put(IO_FIELD_STATUS_CHANGE, this.statusChange);
        map.put(IO_FIELD_STATUS_INIT, this.statusInit);
        map.put(IO_FIELD_DOOR, this.door);
        map.put(IO_FIELD_FLOOR, this.floor);
        map.put(IO_FIELD_LETTER_FROM, this.letterFrom);
        map.put(IO_FIELD_LETTER_TO, this.letterTo);
        map.put(IO_FIELD_HOUSENUMBER_FROM, this.houseNumberFrom);
        map.put(IO_FIELD_HOUSENUMBER_TO, this.houseNumberTo);
        map.put(IO_FIELD_ROAD_CODE, this.roadCode);

        return map;
    }
}
