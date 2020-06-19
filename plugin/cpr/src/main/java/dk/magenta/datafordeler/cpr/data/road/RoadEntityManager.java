package dk.magenta.datafordeler.cpr.data.road;

import com.fasterxml.jackson.core.JsonProcessingException;
import dk.magenta.datafordeler.core.database.SessionManager;
import dk.magenta.datafordeler.core.fapi.BaseQuery;
import dk.magenta.datafordeler.core.io.ImportMetadata;
import dk.magenta.datafordeler.core.io.Receipt;
import dk.magenta.datafordeler.cpr.data.CprRecordEntityManager;
import dk.magenta.datafordeler.cpr.parsers.CprSubParser;
import dk.magenta.datafordeler.cpr.parsers.RoadParser;
import dk.magenta.datafordeler.cpr.records.CprBitemporalRecord;
import dk.magenta.datafordeler.cpr.records.road.RoadDataRecord;
import dk.magenta.datafordeler.cpr.records.road.RoadRecordQuery;
import dk.magenta.datafordeler.cpr.records.road.data.CprBitemporalRoadRecord;
import dk.magenta.datafordeler.cpr.records.road.data.RoadEntity;
import dk.magenta.datafordeler.cpr.records.service.RoadEntityRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.UUID;

@Component
public class RoadEntityManager extends CprRecordEntityManager<RoadDataRecord, RoadEntity> {
    
    @Autowired
    private RoadEntityRecordService roadEntityService;

    @Autowired
    private RoadParser roadParser;

    @Autowired
    private SessionManager sessionManager;

    private static RoadEntityManager instance;

    public RoadEntityManager() {
        this.managedRegistrationReferenceClass = RoadRegistrationReference.class;
        instance = this;
    }

    @Override
    protected String getBaseName() {
        return "road";
    }

    @Override
    public RoadEntityRecordService getEntityService() {
        return this.roadEntityService;
    }

    @Override
    public String getDomain() {
        return "https://data.gl/cpr/road/1/rest/";
    }

    @Override
    public String getSchema() {
        return RoadEntity.schema;
    }

    @Override
    protected URI getReceiptEndpoint(Receipt receipt) {
        return null;
    }

    @Override
    public BaseQuery getQuery() {
        return new RoadRecordQuery();
    }

    @Override
    public BaseQuery getQuery(String... strings) {
        return this.getQuery();
    }


    @Override
    protected SessionManager getSessionManager() {
        return this.sessionManager;
    }

    @Override
    protected CprSubParser<RoadDataRecord> getParser() {
        return this.roadParser;
    }

    @Override
    protected Class<RoadEntity> getEntityClass() {
        return RoadEntity.class;
    }

    @Override
    protected UUID generateUUID(RoadDataRecord record) {
        return RoadEntity.generateUUID(record.getMunicipalityCode(), record.getRoadCode());
    }

    @Override
    protected RoadEntity createBasicEntity(RoadDataRecord record) {
        RoadEntity entity = new RoadEntity();
        entity.setRoadcode(record.getRoadCode());
        entity.setMunicipalityCode(record.getMunicipalityCode());
        return entity;
    }

    protected void parseAlternate(RoadEntity entity, Collection<RoadDataRecord> records, ImportMetadata importMetadata) {
        OffsetDateTime updateTime = importMetadata.getImportTime();
        for (RoadDataRecord record : records) {
            for (CprBitemporalRecord bitemporalRecord : record.getBitemporalRecords()) {
                bitemporalRecord.setDafoUpdated(updateTime);
                bitemporalRecord.setOrigin(record.getOrigin());
                bitemporalRecord.line = record.getLine();
                entity.addBitemporalRecord((CprBitemporalRoadRecord) bitemporalRecord, importMetadata.getSession());
            }
        }
    }

    public static String json(Object o) {
        try {
            return instance.getObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(o);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return null;
    }

}
