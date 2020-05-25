package dk.magenta.datafordeler.ger.data.responsible;

import dk.magenta.datafordeler.core.fapi.BaseQuery;
import dk.magenta.datafordeler.core.fapi.FapiBaseService;
import dk.magenta.datafordeler.core.io.ImportMetadata;
import dk.magenta.datafordeler.ger.data.GerEntityManager;
import dk.magenta.datafordeler.ger.data.RawData;
import org.hibernate.Session;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.UUID;

@Component("GerParticipantEntityManager")
public class ResponsibleEntityManager extends GerEntityManager<ResponsibleEntity> {

    public ResponsibleEntityManager() {
    }

    @Override
    protected String getBaseName() {
        return "responsible";
    }

    @Override
    public FapiBaseService getEntityService() {
        return null;
    }

    @Override
    public String getDomain() {
        return "https://data.gl/ger/responsible/1/rest";
    }

    @Override
    protected String getSheetName() {
        return "Ansvarlige";
    }

    @Override
    public String getSchema() {
        return ResponsibleEntity.schema;
    }

    @Override
    public BaseQuery getQuery() {
        return new ResponsibleQuery();
    }

    @Override
    public BaseQuery getQuery(String... strings) {
        return this.getQuery();
    }

    @Override
    protected Class<ResponsibleEntity> getEntityClass() {
        return ResponsibleEntity.class;
    }

    @Override
    protected UUID generateUUID(RawData rawData) {
        return UUID.randomUUID(); // Spreadsheet does not have a field that uniquely identifies a row
        //return ResponsibleEntity.generateUUID(rawData.getInt("GERNR"), rawData.getUUID("CVR_DELTAGER_GUID"));
    }

    @Override
    protected ResponsibleEntity createBasicEntity(RawData record, Session session) {
        return new ResponsibleEntity(record);
    }

    @Override
    protected void updateEntity(ResponsibleEntity entity, RawData rawData, ImportMetadata importMetadata) {
        entity.setOperationFormCode(rawData.getInt("DRIFTFORMKODE"));
        entity.setOperationFormText(rawData.getString("DRIFTFORMTEKST"));
        entity.setJeEndDate(rawData.getDate("JE_SLUTDTO"));
        entity.setRelationMemo(rawData.getString("RELATIONSNOTAT"));
        entity.setRelationStartDate(rawData.getDate("RELATIONSSTART"));
        entity.setRelationEndDate(rawData.getDate("RELATIONSSLUT"));
        entity.setRelationCreateDate(rawData.getDate("RELATIONOPRETTET"));
        entity.setRelationUpdateDate(rawData.getDate("RELATIONSOPDATERET"));
        entity.setCvrParticipantGuid(rawData.getUUID("CVR_DELTAGER_GUID"));
        entity.setUnitNumber(rawData.getLong("ENHEDSNUMMER"));
        entity.setUnitType(rawData.getString("ENHEDSTYPE"));
        entity.setName(rawData.getString("NAVN"));
        entity.setCprNumber(rawData.getLong("CPRNUMMER"));
        entity.setCvrNumber(rawData.getInt("CVRNUMMER"));
        entity.setLastUpdated(rawData.getDate("SIDSTOPDATERET"));
    }

    private static final HashMap<String, String> keyMappingEntityToRaw = new HashMap<>();
    private static final HashMap<String, String> keyMappingRawToEntity = new HashMap<>();
    static {
        keyMappingEntityToRaw.put(ResponsibleEntity.IO_FIELD_GERNR, "GERNR");
        keyMappingEntityToRaw.put(ResponsibleEntity.IO_FIELD_OPERATION_FORM_CODE, "DRIFTFORMKODE");
        keyMappingEntityToRaw.put(ResponsibleEntity.IO_FIELD_OPERATION_FORM_TEXT, "DRIFTFORMTEKST");
        keyMappingEntityToRaw.put(ResponsibleEntity.IO_FIELD_JE_END_DATE, "JE_SLUTDTO");
        keyMappingEntityToRaw.put(ResponsibleEntity.IO_FIELD_MEMO, "RELATIONSNOTAT");
        keyMappingEntityToRaw.put(ResponsibleEntity.IO_FIELD_RELATION_START_DATE, "RELATIONSSTART");
        keyMappingEntityToRaw.put(ResponsibleEntity.IO_FIELD_RELATION_END_DATE, "RELATIONSSLUT");
        keyMappingEntityToRaw.put(ResponsibleEntity.IO_FIELD_RELATION_CREATE_DATE, "RELATIONOPRETTET");
        keyMappingEntityToRaw.put(ResponsibleEntity.IO_FIELD_RELATION_UPDATE_DATE, "RELATIONSOPDATERET");
        keyMappingEntityToRaw.put(ResponsibleEntity.IO_FIELD_NAME, "NAVN");
        keyMappingEntityToRaw.put(ResponsibleEntity.IO_FIELD_CVR_PARTICIPANT_GUID, "CVR_DELTAGER_GUID");
        keyMappingEntityToRaw.put(ResponsibleEntity.IO_FIELD_UNIT_NUMBER, "ENHEDSNUMMER");
        keyMappingEntityToRaw.put(ResponsibleEntity.IO_FIELD_UNIT_TYPE, "ENHEDSTYPE");
        keyMappingEntityToRaw.put(ResponsibleEntity.IO_FIELD_NAME, "NAVN");
        keyMappingEntityToRaw.put(ResponsibleEntity.IO_FIELD_CPR, "CPRNUMMER");
        keyMappingEntityToRaw.put(ResponsibleEntity.IO_FIELD_CVR, "CVRNUMMER");
        keyMappingEntityToRaw.put(ResponsibleEntity.IO_FIELD_LAST_UPDATED, "SIDSTOPDATERET");

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
