package dk.magenta.datafordeler.cvr.data.company;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import dk.magenta.datafordeler.core.database.RegistrationReference;
import dk.magenta.datafordeler.core.database.SessionManager;
import dk.magenta.datafordeler.core.exception.DataStreamException;
import dk.magenta.datafordeler.core.exception.HttpStatusException;
import dk.magenta.datafordeler.core.fapi.FapiService;
import dk.magenta.datafordeler.core.plugin.ScanScrollCommunicator;
import dk.magenta.datafordeler.cvr.CvrRegisterManager;
import dk.magenta.datafordeler.cvr.configuration.CvrConfiguration;
import dk.magenta.datafordeler.cvr.configuration.CvrConfigurationManager;
import dk.magenta.datafordeler.cvr.data.CvrEntityManager;
import dk.magenta.datafordeler.cvr.records.CompanyRecord;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.GeneralSecurityException;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.List;
import java.util.StringJoiner;
import java.util.UUID;

/**
 * Company-specific EntityManager, specifying various settings that methods in the superclass
 * will use to import data.
 */
@Component
public class CompanyEntityManager extends CvrEntityManager<CompanyEntity, CompanyRegistration, CompanyEffect, CompanyBaseData, CompanyRecord> {

    public static final OffsetDateTime MIN_SQL_SERVER_DATETIME = OffsetDateTime.of(
            1, 1, 1, 0, 0, 0, 0,
            ZoneOffset.UTC
    );

    @Autowired
    private CompanyEntityService companyEntityService;

    @Autowired
    private SessionManager sessionManager;

    private Logger log = LogManager.getLogger(CompanyEntityManager.class);

    public CompanyEntityManager() {
        this.managedEntityClass = CompanyEntity.class;
        this.managedEntityReferenceClass = CompanyEntityReference.class;
        this.managedRegistrationClass = CompanyRegistration.class;
        this.managedRegistrationReferenceClass = CompanyRegistrationReference.class;
    }

    @Override
    protected String getBaseName() {
        return "company";
    }

    @Override
    public FapiService getEntityService() {
        return this.companyEntityService;
    }

    @Override
    public String getSchema() {
        return CompanyEntity.schema;
    }

    @Override
    protected RegistrationReference createRegistrationReference(URI uri) {
        return new CompanyRegistrationReference(uri);
    }

    @Override
    protected SessionManager getSessionManager() {
        return this.sessionManager;
    }

    @Override
    protected String getJsonTypeName() {
        return "Vrvirksomhed";
    }

    @Override
    protected Class getRecordClass() {
        return CompanyRecord.class;
    }

    @Override
    protected Class getEntityClass() {
        return CompanyEntity.class;
    }

    @Override
    protected UUID generateUUID(CompanyRecord record) {
        return CompanyEntity.generateUUID(record.getCvrNumber());
    }

    @Override
    protected CompanyEntity createBasicEntity(CompanyRecord record) {
        CompanyEntity entity = new CompanyEntity();
        entity.setCvrNumber(record.getCvrNumber());
        return entity;
    }

    @Override
    protected CompanyBaseData createDataItem() {
        return new CompanyBaseData();
    }

    @Autowired
    private CvrConfigurationManager configurationManager;

    @Override
    public boolean pullEnabled() {
        CvrConfiguration configuration = configurationManager.getConfiguration();
        return (configuration.getCompanyRegisterType() != CvrConfiguration.RegisterType.DISABLED);
    }


    public HashSet<CompanyRecord> directLookup(HashSet<String> cvrNumbers, OffsetDateTime since, List<Integer> municipalityCodes) {
        HashSet<CompanyRecord> records = new HashSet<>();
        CvrRegisterManager registerManager = (CvrRegisterManager) this.getRegisterManager();
        ScanScrollCommunicator eventCommunicator = (ScanScrollCommunicator) registerManager.getEventFetcher();
        CvrConfiguration configuration = this.configurationManager.getConfiguration();

        String schema = CompanyEntity.schema;

        eventCommunicator.setUsername(configuration.getUsername(schema));
        try {
            eventCommunicator.setPassword(configuration.getPassword(schema));
        } catch (GeneralSecurityException | IOException e) {
            log.error(e);
            return records;
        }
        eventCommunicator.setThrottle(0);

        StringJoiner csep = new StringJoiner(",");
        for (String cvrNumber : cvrNumbers) {
            csep.add("\"" + cvrNumber + "\"");
        }

        ObjectMapper objectMapper = this.getObjectMapper();
        ObjectNode topQuery = objectMapper.createObjectNode();
        ObjectNode query = objectMapper.createObjectNode();
        topQuery.set("query", query);
        ObjectNode bool = objectMapper.createObjectNode();
        query.set("bool", bool);
        ArrayNode must = objectMapper.createArrayNode();
        bool.set("must", must);

        ObjectNode cvrNumberQuery = objectMapper.createObjectNode();
        must.add(cvrNumberQuery);
        ObjectNode cvrNumberTerms = objectMapper.createObjectNode();
        cvrNumberQuery.set("terms", cvrNumberTerms);
        ArrayNode cvrNumberTermValues = objectMapper.createArrayNode();
        cvrNumberTerms.set("Vrvirksomhed.cvrNummer", cvrNumberTermValues);
        for (String cvrNumber : cvrNumbers) {
            cvrNumberTermValues.add(cvrNumber);
        }

        if (municipalityCodes != null) {
            ObjectNode municipalityQuery = objectMapper.createObjectNode();
            must.add(municipalityQuery);
            ObjectNode municipalityTerms = objectMapper.createObjectNode();
            municipalityQuery.set("terms", municipalityTerms);
            ArrayNode municipalityTermValues = objectMapper.createArrayNode();
            cvrNumberTerms.set("Vrvirksomhed.beliggenhedsadresse.kommune.kommuneKode", municipalityTermValues);
            for (Integer municipalityCode : municipalityCodes) {
                municipalityTermValues.add(municipalityCode);
            }
        }

        if (since != null) {
            ObjectNode sinceQuery = objectMapper.createObjectNode();
            must.add(sinceQuery);
            ObjectNode sinceRange = objectMapper.createObjectNode();
            sinceQuery.set("range", sinceRange);
            ObjectNode sinceTermValue = objectMapper.createObjectNode();
            sinceRange.set("Vrvirksomhed.sidstOpdateret", sinceTermValue);
            sinceTermValue.put("gte", since.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        }

        try {
            String requestBody = objectMapper.writeValueAsString(topQuery);
            InputStream rawData = eventCommunicator.fetch(
                    new URI(configuration.getStartAddress(schema)),
                    new URI(configuration.getScrollAddress(schema)),
                    requestBody
            );
            JsonNode topNode = this.getObjectMapper().readTree(rawData);
            ObjectReader reader = this.getObjectMapper().readerFor(CompanyRecord.class);
            if (topNode != null && topNode.has("hits")) {
                topNode = topNode.get("hits");
                if (topNode.has("hits")) {
                    topNode = topNode.get("hits");
                }
                if (topNode.isArray()) {
                    for (JsonNode item : topNode) {
                        if (item.has("_source")) {
                            item = item.get("_source");
                        }
                        String jsonTypeName = this.getJsonTypeName();
                        if (item.has(jsonTypeName)) {
                            item = item.get(jsonTypeName);
                        }
                        CompanyRecord record = reader.readValue(item);
                        records.add(record);
                    }
                }
            }
        } catch (HttpStatusException | DataStreamException | URISyntaxException | IOException e) {
            e.printStackTrace();
        }
        return records;
    }
}