package dk.magenta.datafordeler.cvr.entitymanager;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeType;
import com.fasterxml.jackson.databind.node.ObjectNode;
import dk.magenta.datafordeler.core.database.QueryManager;
import dk.magenta.datafordeler.core.database.SessionManager;
import dk.magenta.datafordeler.core.exception.DataFordelerException;
import dk.magenta.datafordeler.core.exception.DataStreamException;
import dk.magenta.datafordeler.core.exception.HttpStatusException;
import dk.magenta.datafordeler.core.fapi.BaseQuery;
import dk.magenta.datafordeler.core.fapi.FapiBaseService;
import dk.magenta.datafordeler.core.io.ImportInputStream;
import dk.magenta.datafordeler.core.io.ImportMetadata;
import dk.magenta.datafordeler.core.plugin.ScanScrollCommunicator;
import dk.magenta.datafordeler.cvr.CvrRegisterManager;
import dk.magenta.datafordeler.cvr.QueryBuilder;
import dk.magenta.datafordeler.cvr.configuration.CvrConfiguration;
import dk.magenta.datafordeler.cvr.configuration.CvrConfigurationManager;
import dk.magenta.datafordeler.cvr.query.CompanyRecordQuery;
import dk.magenta.datafordeler.cvr.records.CompanyRecord;
import dk.magenta.datafordeler.cvr.records.CompanySubscription;
import dk.magenta.datafordeler.cvr.service.CompanyRecordService;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.GeneralSecurityException;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import static dk.magenta.datafordeler.cvr.configuration.CvrConfiguration.RegisterType.ALL_LOCAL_FILES;

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

    private final CompanyRecordService companyRecordService;

    private final SessionManager sessionManager;

    private final Logger log = LogManager.getLogger(CompanyEntityManager.class.getCanonicalName());

    @Autowired
    QueryBuilder queryBuilder;

    @Autowired
    public CompanyEntityManager(@Lazy CompanyRecordService companyRecordService, @Lazy SessionManager sessionManager) {
        this.companyRecordService = companyRecordService;
        this.sessionManager = sessionManager;
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
        CvrRegisterManager registerManager = this.getRegisterManager();
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
            JsonNode topNode = this.getObjectMapper().readTree(rawData.readAllBytes());
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
        final String sep = BaseQuery.separator;
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



    private void loadOneCompany(String cvr) throws GeneralSecurityException, IOException, URISyntaxException, DataFordelerException {
        String requestBody = String.format(
                "{\"query\":{\"terms\":{\"Vrvirksomhed.cvrNummer\":[%s]}}}",
                cvr
        );
        String schema = this.getSchema();
        CvrRegisterManager registerManager = this.getRegisterManager();
        CvrConfiguration configuration = this.getRegisterManager().getConfigurationManager().getConfiguration();

        ScanScrollCommunicator eventCommunicator = (ScanScrollCommunicator) registerManager.getEventFetcher();
        eventCommunicator.setThrottle(0);

        eventCommunicator.setUsername(configuration.getUsername(schema));
        eventCommunicator.setPassword(configuration.getPassword(schema));

        final ArrayList<Throwable> errors = new ArrayList<>();
        eventCommunicator.setUncaughtExceptionHandler((t, e) -> errors.add(e));
        InputStream responseBody = eventCommunicator.fetch(
                new URI(configuration.getStartAddress(schema)),
                new URI(configuration.getScrollAddress(schema)),
                requestBody
        );

        try (Session session = this.getSessionManager().getSessionFactory().openSession()) {
            Transaction transaction = session.beginTransaction();
            ImportMetadata importMetadata = new ImportMetadata();
            importMetadata.setSession(session);
            importMetadata.setTransactionInProgress(true);
            this.parseData(responseBody, importMetadata);
            transaction.commit();
        }
    }

    public void loadMagenta() {
        int cvr = 12950160;
        try (Session session = sessionManager.getSessionFactory().openSession()) {
            this.reloadCompany(""+cvr, session);
        } catch (DataFordelerException e) {
            e.printStackTrace();
        }
    }

    public void reloadCompany(String cvr, Session session) throws DataFordelerException {
        ImportMetadata importMetadata = new ImportMetadata();

        importMetadata.setSession(session);
        Transaction transaction = session.beginTransaction();
        importMetadata.setTransactionInProgress(true);

        ImportInputStream allCacheData = (ImportInputStream) this.getRegisterManager().pullRawData(null, this, importMetadata, ALL_LOCAL_FILES);
        this.parseData(allCacheData, importMetadata, jsonNode -> {
            if (jsonNode.getNodeType() == JsonNodeType.OBJECT) {
                ObjectNode objectNode = (ObjectNode) jsonNode;
                JsonNode cvrNode = objectNode.get("cvrNummer");
                if (cvrNode != null && Objects.equals(cvrNode.asText(), cvr)) {
                    return true;
                }
            }
            return false;
        });
        transaction.commit();
    }

    /**
     * Clean democompanys which has been initiated in the database.
     * democompanys is used on the demoenvironment for demo and education purposes
     */
    @Override
    public void cleanDemoData(Session session) {
        CompanyRecordQuery personQuery = new CompanyRecordQuery();
        List<String> testCompanyList = Arrays.asList(cvrDemoList.split(","));
        personQuery.setParameter(CompanyRecordQuery.CVRNUMMER, testCompanyList);
        session.beginTransaction();
        personQuery.setPageSize(1000);
        personQuery.applyFilters(session);
        List<CompanyRecord> companyEntities = QueryManager.getAllEntities(session, personQuery, CompanyRecord.class);
        for (CompanyRecord companyForDeletion : companyEntities) {
            session.remove(companyForDeletion);
        }
        session.getTransaction().commit();
    }


    protected ObjectNode queryFromMunicipalities(List<Integer> municipalities) {
        return queryFromIntegerTerms("Vrvirksomhed.beliggenhedsadresse.kommune.kommuneKode", municipalities);
    }
    protected ObjectNode queryFromUpdatedSince(OffsetDateTime updatedSince) {
        return queryFromUpdatedSince("Vrvirksomhed.sidstOpdateret", updatedSince);
    }
    protected ObjectNode queryFromCvrs(List<Integer> cvrs) {
        return queryFromIntegerTerms("Vrvirksomhed.cvrNummer", cvrs);
    }

    public String getDailyQuery(Session session, OffsetDateTime lastUpdated) {

        CriteriaBuilder subscriptionBuilder = session.getCriteriaBuilder();
        CriteriaQuery<CompanySubscription> allCompanySubscription = subscriptionBuilder.createQuery(CompanySubscription.class);
        allCompanySubscription.from(CompanySubscription.class);
        List<Integer> subscribedCompanyList = session.createQuery(allCompanySubscription).getResultList().stream().map(s -> s.getCvrNumber()).sorted().collect(Collectors.toList());

        Query<Integer> query = session.createQuery("select " + CompanyRecord.DB_FIELD_CVR_NUMBER + " from " + CompanyRecord.class.getCanonicalName(), Integer.class);
        HashSet<Integer> missingCompanyList = new HashSet<>(subscribedCompanyList);
        missingCompanyList.removeAll(new HashSet<>(query.list()));

        return finalizeQuery(
            combineQueryOr(
                combineQueryAnd(
                    queryFromMunicipalities(Arrays.asList(954, 955, 956, 957, 958, 959, 960, 961, 962)),
                    queryFromUpdatedSince(lastUpdated)
                ),
                combineQueryAnd(
                    queryFromCvrs(subscribedCompanyList),
                    queryFromUpdatedSince(lastUpdated)
                ),
                queryFromCvrs(missingCompanyList.stream().toList())
            )
        );
    }

    public String getSpecificQuery(List<Integer> ids) {
        return finalizeQuery(queryFromCvrs(ids));
    }
}
