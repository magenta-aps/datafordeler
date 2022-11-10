package dk.magenta.datafordeler.ger.data.unit;

import dk.magenta.datafordeler.core.fapi.BaseQuery;
import dk.magenta.datafordeler.core.fapi.FapiBaseService;
import dk.magenta.datafordeler.core.io.ImportMetadata;
import dk.magenta.datafordeler.ger.data.GerEntityManager;
import dk.magenta.datafordeler.ger.data.RawData;
import org.hibernate.Session;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.UUID;

@Component("GerAccessAddressEntityManager")
public class UnitEntityManager extends GerEntityManager<UnitEntity> {

    public UnitEntityManager() {
    }

    @Override
    protected String getBaseName() {
        return "accessaddress";
    }

    @Override
    public FapiBaseService getEntityService() {
        return null;
    }

    @Override
    public String getDomain() {
        return "https://data.gl/ger/unit/1/rest";
    }

    @Override
    protected String getSheetName() {
        return "PE";
    }

    @Override
    public String getSchema() {
        return UnitEntity.schema;
    }

    @Override
    public BaseQuery getQuery() {
        return new UnitQuery();
    }

    @Override
    public BaseQuery getQuery(String... strings) {
        return this.getQuery();
    }

    @Override
    protected Class<UnitEntity> getEntityClass() {
        return UnitEntity.class;
    }

    @Override
    protected UUID generateUUID(RawData rawData) {
        return rawData.getUUID("DEID");
    }

    @Override
    protected UnitEntity createBasicEntity(RawData record, Session session) {
        return new UnitEntity(record);
    }

    @Override
    protected void updateEntity(UnitEntity entity, RawData rawData, ImportMetadata importMetadata) {
        entity.setGerNumber(rawData.getInt("GERNR"));
        entity.setOperationFormCode(rawData.getInt("DRIFTFORMKODE"));
        entity.setOperationFormText(rawData.getString("DRIFTFORMTEKST"));
        entity.setJeEndDate(rawData.getDate("JE_SLUTDTO"));
        entity.setDeid(rawData.getUUID("DEID"));
        entity.setDnr(rawData.getInt("DNR"));
        entity.setJeid(rawData.getUUID("JEID"));
        entity.setValidated(rawData.getBoolean("VALIDERET"));
        entity.setCopy(rawData.getBoolean("KOPI"));
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
        entity.setOldTaxKom(rawData.getInt("GAMMELSKATKOMKOD"));
        entity.setMunicipalityCode(rawData.getInt("CPR_KOMKODE_2018"));
        entity.setLocalityCode(rawData.getInt("STEDKODE"));
        entity.setCountryCode(rawData.getInt("LANDKODE"));
        entity.setPhone(rawData.getString("TLFNR"));
        entity.setFax(rawData.getString("FAXNR"));
        entity.setMobilePhone(rawData.getString("MOBILNR"));
        entity.setEmail(rawData.getString("EMAIL"));
        entity.setBusinessText(rawData.getString("BRANCHETEKST"));
        entity.setBusinessCode(rawData.getInt("BRANCHEKODE"));
        entity.setStartDate(rawData.getDate("STARTDTO"));
        entity.setEndDate(rawData.getDate("SLUTDTO"));
        entity.setAdvertFree(rawData.getBoolean("REKLAMEFRI"));
        entity.setMemo(rawData.getString("NOTAT"));
        entity.setFoundDate(rawData.getDate("OPRETDTO"));
        entity.setAjourDate(rawData.getDate("AJOURDTO"));
        entity.setAjourInit(rawData.getString("AJOURINIT"));
        entity.setLuliNumber(rawData.getString("LULINR"));
    }


    private static final HashMap<String, String> keyMappingEntityToRaw = new HashMap<>();
    private static final HashMap<String, String> keyMappingRawToEntity = new HashMap<>();

    static {
        keyMappingEntityToRaw.put(UnitEntity.IO_FIELD_GERNR, "GERNR");
        keyMappingEntityToRaw.put(UnitEntity.IO_FIELD_OPERATION_FORM_CODE, "DRIFTFORMKODE");
        keyMappingEntityToRaw.put(UnitEntity.IO_FIELD_OPERATION_FORM_TEXT, "DRIFTFORMTEKST");
        keyMappingEntityToRaw.put(UnitEntity.IO_FIELD_JE_END_DATE, "JE_SLUTDTO");
        keyMappingEntityToRaw.put(UnitEntity.IO_FIELD_DEID, "DEID");
        keyMappingEntityToRaw.put(UnitEntity.IO_FIELD_DNR, "DNR");
        keyMappingEntityToRaw.put(UnitEntity.IO_FIELD_JEID, "JEID");
        keyMappingEntityToRaw.put(UnitEntity.IO_FIELD_VALIDATED, "VALIDERET");
        keyMappingEntityToRaw.put(UnitEntity.IO_FIELD_COPY, "KOPI");
        keyMappingEntityToRaw.put(UnitEntity.IO_FIELD_NAME, "NAVN");
        keyMappingEntityToRaw.put(UnitEntity.IO_FIELD_DANISH_NAME, "NAVN_DK");
        keyMappingEntityToRaw.put(UnitEntity.IO_FIELD_SHORT_NAME, "KORTNAVN");
        keyMappingEntityToRaw.put(UnitEntity.IO_FIELD_SHORT_DANISH_NAME, "KORTNAVN_DK");
        keyMappingEntityToRaw.put(UnitEntity.IO_FIELD_CO_NAME, "CONAVN");
        keyMappingEntityToRaw.put(UnitEntity.IO_FIELD_ADDRESS1, "ADR1");
        keyMappingEntityToRaw.put(UnitEntity.IO_FIELD_ADDRESS2, "ADR2");
        keyMappingEntityToRaw.put(UnitEntity.IO_FIELD_ADDRESS3, "ADR3");
        keyMappingEntityToRaw.put(UnitEntity.IO_FIELD_BNR, "BNR");
        keyMappingEntityToRaw.put(UnitEntity.IO_FIELD_BOXNR, "BOXNR");
        keyMappingEntityToRaw.put(UnitEntity.IO_FIELD_POSTNR, "POSTNR");
        keyMappingEntityToRaw.put(UnitEntity.IO_FIELD_OLD_TAX, "GAMMELSKATKOMKOD");
        keyMappingEntityToRaw.put(UnitEntity.IO_FIELD_MUNICIPALITY_CODE, "CPR_KOMKODE_2018");
        keyMappingEntityToRaw.put(UnitEntity.IO_FIELD_LOCALITY_CODE, "STEDKODE");
        keyMappingEntityToRaw.put(UnitEntity.IO_FIELD_COUNTRY_CODE, "LANDKODE");
        keyMappingEntityToRaw.put(UnitEntity.IO_FIELD_PHONE, "TLFNR");
        keyMappingEntityToRaw.put(UnitEntity.IO_FIELD_FAX, "FAXNR");
        keyMappingEntityToRaw.put(UnitEntity.IO_FIELD_MOBILEPHONE, "MOBILNR");
        keyMappingEntityToRaw.put(UnitEntity.IO_FIELD_EMAIL, "EMAIL");
        keyMappingEntityToRaw.put(UnitEntity.IO_FIELD_BUSINESS_TEXT, "BRANCHETEKST");
        keyMappingEntityToRaw.put(UnitEntity.IO_FIELD_BUSINESS_CODE, "BRANCHEKODE");
        keyMappingEntityToRaw.put(UnitEntity.IO_FIELD_START_DATE, "STARTDTO");
        keyMappingEntityToRaw.put(UnitEntity.IO_FIELD_END_DATE, "SLUTDTO");
        keyMappingEntityToRaw.put(UnitEntity.IO_FIELD_ADVERTFREE, "REKLAMEFRI");
        keyMappingEntityToRaw.put(UnitEntity.IO_FIELD_MEMO, "NOTAT");
        keyMappingEntityToRaw.put(UnitEntity.IO_FIELD_FOUND_DATE, "OPRETDTO");
        keyMappingEntityToRaw.put(UnitEntity.IO_FIELD_AJOUR_DATE, "AJOURDTO");
        keyMappingEntityToRaw.put(UnitEntity.IO_FIELD_AJOUR_INIT, "AJOURINIT");
        keyMappingEntityToRaw.put(UnitEntity.IO_FIELD_LULI, "LULINR");

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
