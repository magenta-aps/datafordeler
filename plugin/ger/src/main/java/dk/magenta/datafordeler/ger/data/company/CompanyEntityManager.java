package dk.magenta.datafordeler.ger.data.company;

import dk.magenta.datafordeler.core.fapi.BaseQuery;
import dk.magenta.datafordeler.core.fapi.FapiBaseService;
import dk.magenta.datafordeler.core.io.ImportMetadata;
import dk.magenta.datafordeler.ger.data.GerEntityManager;
import dk.magenta.datafordeler.ger.data.RawData;
import org.hibernate.Session;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.UUID;

@Component("GerCompanyEntityManager")
public class CompanyEntityManager extends GerEntityManager<CompanyEntity> {

    public CompanyEntityManager() {
    }

    @Override
    protected String getBaseName() {
        return "company";
    }

    @Override
    public FapiBaseService getEntityService() {
        return null;
    }

    @Override
    public String getDomain() {
        return "https://data.gl/ger/company/1/rest";
    }

    @Override
    protected String getSheetName() {
        return "JE";
    }

    @Override
    public String getSchema() {
        return CompanyEntity.schema;
    }

    @Override
    public BaseQuery getQuery() {
        return new CompanyQuery();
    }

    @Override
    public BaseQuery getQuery(String... strings) {
        return this.getQuery();
    }

    @Override
    protected Class<CompanyEntity> getEntityClass() {
        return CompanyEntity.class;
    }

    @Override
    protected UUID generateUUID(RawData rawData) {
        return CompanyEntity.generateUUID(rawData.getInt("GERNR"));
    }

    @Override
    protected CompanyEntity createBasicEntity(RawData record, Session session) {
        return new CompanyEntity(record);
    }

    @Override
    protected void updateEntity(CompanyEntity entity, RawData rawData, ImportMetadata importMetadata) {
        entity.setValidated(rawData.getBoolean("VALIDERET"));
        entity.setName(rawData.getString("NAVN"));
        entity.setDanishName(rawData.getString("NAVN_DK"));
        entity.setShortName(rawData.getString("KORTNAVN"));
        entity.setShortDanishName(rawData.getString("KORTNAVN_DK"));
        entity.setCoName(rawData.getString("CONAVN"));
        entity.setAddress1(rawData.getString("ADR1"));
        entity.setAddress2(rawData.getString("ADR2"));
        entity.setAddress3(rawData.getString("ADR3"));
        entity.setBnr(rawData.getString("BNR", true));
        entity.setBoxNr(rawData.getString("BOXNR"));
        entity.setPostNr(rawData.getInt("POSTNR"));
        entity.setOldTaxKom(rawData.getInt("GAMMELSKATKOM"));
        entity.setMunicipalityCode(rawData.getInt("CPR_KOMKODE_2018"));
        entity.setLocalityCode(rawData.getInt("STEDKODE"));
        entity.setCountryCode(rawData.getInt("LANDKODE"));
        entity.setPhone(rawData.getString("TLFNR"));
        entity.setFax(rawData.getString("FAXNR"));
        entity.setMobilePhone(rawData.getString("MOBILNR"));
        entity.setEmail(rawData.getString("EMAIL"));
        entity.setOperationFormCode(rawData.getInt("DRIFTFORMKODE"));
        entity.setOperationFormText(rawData.getString("DRIFTFORMTEKST"));
        entity.setBusinessCode(rawData.getInt("BRANCHEKODE"));
        entity.setBusinessText(rawData.getString("BRANCHETEKST"));
        entity.setStartDate(rawData.getDate("STARTDTO"));
        entity.setEndDate(rawData.getDate("SLUTDTO"));
        entity.setLnr(rawData.getInt("LNR"));
        entity.setMemo(rawData.getString("NOTAT"));
        entity.setFoundDate(rawData.getDate("OPRETDTO"));
        entity.setAjourDate(rawData.getDate("AJOURDTO"));
        entity.setAjourInit(rawData.getString("AJOURINIT"));
        entity.setStatusGuid(rawData.getUUID("JE_STATUS_GUID"));
        entity.setStatusChange(rawData.getDate("JE_STATUS_CHANGE"));
        entity.setStatusInit(rawData.getString("JE_STATUS_INIT"));
        entity.setDoor(rawData.getString("SIDEDOER"));
        entity.setFloor(rawData.getInt("ETAGE"));
        entity.setLetterFrom(rawData.getCharacter("BOGSTAVFRA"));
        entity.setLetterTo(rawData.getCharacter("BOGSTAVTIL"));
        entity.setHouseNumberFrom(rawData.getInt("HUSNUMMERFRA"));
        entity.setHouseNumberTo(rawData.getInt("HUSNUMMERTIL"));
        entity.setRoadCode(rawData.getInt("VEJKODE"));
    }

    private static final HashMap<String, String> keyMappingEntityToRaw = new HashMap<>();
    private static final HashMap<String, String> keyMappingRawToEntity = new HashMap<>();

    static {
        keyMappingEntityToRaw.put(CompanyEntity.IO_FIELD_GERNR, "GERNR");
        keyMappingEntityToRaw.put(CompanyEntity.IO_FIELD_VALIDATED, "VALIDERET");
        keyMappingEntityToRaw.put(CompanyEntity.IO_FIELD_NAME, "NAVN");
        keyMappingEntityToRaw.put(CompanyEntity.IO_FIELD_DANISH_NAME, "NAVN_DK");
        keyMappingEntityToRaw.put(CompanyEntity.IO_FIELD_SHORT_NAME, "KORTNAVN");
        keyMappingEntityToRaw.put(CompanyEntity.IO_FIELD_SHORT_DANISH_NAME, "KORTNAVN_DK");
        keyMappingEntityToRaw.put(CompanyEntity.IO_FIELD_CO_NAME, "CONAVN");
        keyMappingEntityToRaw.put(CompanyEntity.IO_FIELD_ADDRESS1, "ADR1");
        keyMappingEntityToRaw.put(CompanyEntity.IO_FIELD_ADDRESS2, "ADR2");
        keyMappingEntityToRaw.put(CompanyEntity.IO_FIELD_ADDRESS3, "ADR3");
        keyMappingEntityToRaw.put(CompanyEntity.IO_FIELD_BNR, "BNR");
        keyMappingEntityToRaw.put(CompanyEntity.IO_FIELD_BOXNR, "BOXNR");
        keyMappingEntityToRaw.put(CompanyEntity.IO_FIELD_POSTNR, "POSTNR");
        keyMappingEntityToRaw.put(CompanyEntity.IO_FIELD_OLD_TAX, "GAMMELSKATKOM");
        keyMappingEntityToRaw.put(CompanyEntity.IO_FIELD_MUNICIPALITY_CODE, "CPR_KOMKODE_2018");
        keyMappingEntityToRaw.put(CompanyEntity.IO_FIELD_LOCALITY_CODE, "STEDKODE");
        keyMappingEntityToRaw.put(CompanyEntity.IO_FIELD_COUNTRY_CODE, "LANDKODE");
        keyMappingEntityToRaw.put(CompanyEntity.IO_FIELD_PHONE, "TLFNR");
        keyMappingEntityToRaw.put(CompanyEntity.IO_FIELD_FAX, "FAXNR");
        keyMappingEntityToRaw.put(CompanyEntity.IO_FIELD_MOBILEPHONE, "MOBILNR");
        keyMappingEntityToRaw.put(CompanyEntity.IO_FIELD_EMAIL, "EMAIL");
        keyMappingEntityToRaw.put(CompanyEntity.IO_FIELD_OPERATION_FORM_CODE, "DRIFTFORMKODE");
        keyMappingEntityToRaw.put(CompanyEntity.IO_FIELD_OPERATION_FORM_TEXT, "DRIFTFORMTEKST");
        keyMappingEntityToRaw.put(CompanyEntity.IO_FIELD_BUSINESS_CODE, "BRANCHEKODE");
        keyMappingEntityToRaw.put(CompanyEntity.IO_FIELD_BUSINESS_TEXT, "BRANCHETEKST");
        keyMappingEntityToRaw.put(CompanyEntity.IO_FIELD_START_DATE, "STARTDTO");
        keyMappingEntityToRaw.put(CompanyEntity.IO_FIELD_END_DATE, "SLUTDTO");
        keyMappingEntityToRaw.put(CompanyEntity.IO_FIELD_LNR, "LNR");
        keyMappingEntityToRaw.put(CompanyEntity.IO_FIELD_MEMO, "NOTAT");
        keyMappingEntityToRaw.put(CompanyEntity.IO_FIELD_FOUND_DATE, "OPRETDTO");
        keyMappingEntityToRaw.put(CompanyEntity.IO_FIELD_AJOUR_DATE, "AJOURDTO");
        keyMappingEntityToRaw.put(CompanyEntity.IO_FIELD_AJOUR_INIT, "AJOURINIT");
        keyMappingEntityToRaw.put(CompanyEntity.IO_FIELD_STATUS_GUID, "JE_STATUS_GUID");
        keyMappingEntityToRaw.put(CompanyEntity.IO_FIELD_STATUS_CHANGE, "JE_STATUS_CHANGE");
        keyMappingEntityToRaw.put(CompanyEntity.IO_FIELD_STATUS_INIT, "JE_STATUS_INIT");
        keyMappingEntityToRaw.put(CompanyEntity.IO_FIELD_DOOR, "SIDEDOER");
        keyMappingEntityToRaw.put(CompanyEntity.IO_FIELD_FLOOR, "ETAGE");
        keyMappingEntityToRaw.put(CompanyEntity.IO_FIELD_LETTER_FROM, "BOGSTAVFRA");
        keyMappingEntityToRaw.put(CompanyEntity.IO_FIELD_LETTER_TO, "BOGSTAVTIL");
        keyMappingEntityToRaw.put(CompanyEntity.IO_FIELD_HOUSENUMBER_FROM, "HUSNUMMERFRA");
        keyMappingEntityToRaw.put(CompanyEntity.IO_FIELD_HOUSENUMBER_TO, "HUSNUMMERTIL");
        keyMappingEntityToRaw.put(CompanyEntity.IO_FIELD_ROAD_CODE, "VEJKODE");

        for (String key : keyMappingEntityToRaw.keySet()) {
            keyMappingRawToEntity.put(keyMappingEntityToRaw.get(key), key);
        }
    }

    public static HashMap<String, String> getKeyMappingEntityToRaw() {
        return keyMappingEntityToRaw;
    }

    public static HashMap<String, String> getKeyMappingRawToEntity() {
        return keyMappingRawToEntity;
    }
}
