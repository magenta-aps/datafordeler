package dk.magenta.datafordeler.cvr.entitymanager;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import dk.magenta.datafordeler.core.database.SessionManager;
import dk.magenta.datafordeler.core.exception.DataStreamException;
import dk.magenta.datafordeler.core.exception.HttpStatusException;
import dk.magenta.datafordeler.core.fapi.BaseQuery;
import dk.magenta.datafordeler.core.fapi.FapiBaseService;
import dk.magenta.datafordeler.core.plugin.ScanScrollCommunicator;
import dk.magenta.datafordeler.cvr.CvrRegisterManager;
import dk.magenta.datafordeler.cvr.configuration.CvrConfiguration;
import dk.magenta.datafordeler.cvr.configuration.CvrConfigurationManager;
import dk.magenta.datafordeler.cvr.query.CompanyRecordQuery;
import dk.magenta.datafordeler.cvr.records.CompanyRecord;
import dk.magenta.datafordeler.cvr.service.CompanyRecordService;
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
public class CompanyEntityManager extends CvrEntityManager<CompanyRecord> {

    public static final OffsetDateTime MIN_SQL_SERVER_DATETIME = OffsetDateTime.of(
            1, 1, 1, 0, 0, 0, 0,
            ZoneOffset.UTC
    );

    @Autowired
    private CompanyRecordService companyRecordService;

    @Autowired
    private SessionManager sessionManager;

    private Logger log = LogManager.getLogger(CompanyEntityManager.class);

    public CompanyEntityManager() {
    }

    @Override
    protected String getBaseName() {
        return "company";
    }

    @Override
    public FapiBaseService getEntityService() {
        return this.companyRecordService;
    }

    @Override
    public String getSchema() {
        return CompanyRecord.schema;
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
    protected UUID generateUUID(CompanyRecord record) {
        return record.generateUUID();
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

        String schema = CompanyRecord.schema;

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


/*
    @Override
    public Map<String, String> getJoinHandles(String entityIdentifier) {
        HashMap<String, String> handles = new HashMap<>();
        final String sep = BaseLookupDefinition.separator;
        handles.put(
                "municipalitycode",
                BaseLookupDefinition.getParameterPath(entityIdentifier, entityIdentifier, CompanyRecord.DB_FIELD_LOCATION_ADDRESS + sep + AddressRecord.DB_FIELD_MUNICIPALITY) + sep +
                        AddressMunicipalityRecord.DB_FIELD_MUNICIPALITY + sep +
                        Municipality.DB_FIELD_CODE
        );
        handles.put(
                "roadcode",
                        BaseLookupDefinition.getParameterPath(entityIdentifier, entityIdentifier, CompanyRecord.DB_FIELD_LOCATION_ADDRESS) + sep +
                        AddressRecord.DB_FIELD_ROADCODE
        );
        return handles;
    }*/

    @Override
    public BaseQuery getQuery() {
        return new CompanyRecordQuery();
    }

    @Override
    public BaseQuery getQuery(String... strings) {
        return this.getQuery();
    }
}
