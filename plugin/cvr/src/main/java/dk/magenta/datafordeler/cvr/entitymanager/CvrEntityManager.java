package dk.magenta.datafordeler.cvr.entitymanager;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import dk.magenta.datafordeler.core.database.*;
import dk.magenta.datafordeler.core.exception.*;
import dk.magenta.datafordeler.core.io.ImportInputStream;
import dk.magenta.datafordeler.core.io.ImportMetadata;
import dk.magenta.datafordeler.core.plugin.EntityManager;
import dk.magenta.datafordeler.core.plugin.RegisterManager;
import dk.magenta.datafordeler.core.plugin.ScanScrollCommunicator;
import dk.magenta.datafordeler.core.util.Bitemporality;
import dk.magenta.datafordeler.core.util.ListHashMap;
import dk.magenta.datafordeler.core.util.Stopwatch;
import dk.magenta.datafordeler.cvr.CvrRegisterManager;
import dk.magenta.datafordeler.cvr.configuration.CvrConfiguration;
import dk.magenta.datafordeler.cvr.query.CompanyRecordQuery;
import dk.magenta.datafordeler.cvr.records.CompanyRecord;
import dk.magenta.datafordeler.cvr.records.CvrBitemporalRecord;
import dk.magenta.datafordeler.cvr.records.CvrEntityRecord;
import dk.magenta.datafordeler.cvr.records.CvrRecord;
import dk.magenta.datafordeler.cvr.records.CompanyUnitMetadataRecord;
import dk.magenta.datafordeler.cvr.records.CompanySubscription;
import dk.magenta.datafordeler.cvr.records.CompanyMetadataRecord;
import dk.magenta.datafordeler.cvr.records.unversioned.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.time.OffsetDateTime;
import java.util.*;

/**
 * Base EntityManager for CVR, implementing shared methods for the Company, CompanyUnit and Participant EntityManagers.
 * In particular, defines the flow of how data is imported.
 */
@Component
public abstract class CvrEntityManager<T extends CvrEntityRecord>
        extends EntityManager {

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private Stopwatch timer;

    @Autowired
    private ConfigurationSessionManager configurationSessionManager;

    @Value("${dafo.cpr.demoCompanyList}")
    private String cvrDemoList;

    private static final String TASK_PARSE = "CvrParse";
    private static final String TASK_FIND_ENTITY = "CvrFindEntity";
    private static final String TASK_FIND_REGISTRATIONS = "CvrFindRegistrations";
    private static final String TASK_FIND_ITEMS = "CvrFindItems";
    private static final String TASK_POPULATE_DATA = "CvrPopulateData";
    private static final String TASK_SAVE = "CvrSave";
    private static final String TASK_COMMIT = "Transaction commit";

    private static boolean IMPORT_ONLY_CURRENT = false;
    private static boolean DONT_IMPORT_CURRENT = false;

    private ScanScrollCommunicator commonFetcher;

    protected Logger log = LogManager.getLogger(this.getClass().getSimpleName());

    private Collection<String> handledURISubstrings;

    protected abstract String getBaseName();

    public CvrEntityManager() {
        this.commonFetcher = new ScanScrollCommunicator();
        this.handledURISubstrings = new ArrayList<>();
    }

    @PostConstruct
    public void init() {
        // Ignore case on property names when parsing incoming JSON
        this.objectMapper.configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true);
    }

    public void setCvrDemoList(String cvrDemoList) {
        this.cvrDemoList = cvrDemoList;
    }

    /**
     * Set the associated RegisterManager (called as part of Plugin initialization), setting up
     * the service paths listened on in the process
     */
    @Override
    public void setRegisterManager(RegisterManager registerManager) {
        super.setRegisterManager(registerManager);
        //this.handledURISubstrings.add(expandBaseURI(this.getBaseEndpoint(), "/" + this.getBaseName(), null, null).toString());
        //this.handledURISubstrings.add(expandBaseURI(this.getBaseEndpoint(), "/get/" + this.getBaseName(), null, null).toString());
    }

    @Override
    public CvrRegisterManager getRegisterManager() {
        return (CvrRegisterManager) super.getRegisterManager();
    }

    /**
     * Return the URI substrings that are listened on in the service
     */
    @Override
    public Collection<String> getHandledURISubstrings() {
        return this.handledURISubstrings;
    }

    @Override
    protected ObjectMapper getObjectMapper() {
        return this.objectMapper;
    }

    @Override
    protected ScanScrollCommunicator getRegistrationFetcher() {
        return this.commonFetcher;
    }

    /**
     * Return the base endpoint, which points to the external source
     */
    @Override
    public URI getBaseEndpoint() {
        return this.getRegisterManager().getBaseEndpoint();
    }

    @Override
    protected Logger getLog() {
        return this.log;
    }

    /**
     * Takes a stream of data, parses it in chunks, and saves it to the database.
     * The Stream is read in chunks (separated by newline), each chunk expected to be a properly formatted JSON object.
     * In accordance with the flow laid out in Pull, an ImportInterruptedException will be thrown if the importMetadata signals an orderly halt
     * @param registrationData
     * @param importMetadata
     * @return
     * @throws DataFordelerException
     */
    @Override
    public List<? extends Registration> parseData(InputStream registrationData, ImportMetadata importMetadata) throws DataFordelerException {
        Session session = importMetadata.getSession();
        if (session != null) {
            //With this flag true initiated testdata is cleared before initiation of new data is initiated
            if(importMetadata.getImportConfiguration()!=null &&
                    importMetadata.getImportConfiguration().has("cleantestdatafirst") &&
                    importMetadata.getImportConfiguration().get("cleantestdatafirst").booleanValue()) {
                cleanDemoData(session);
            }
            Industry.initializeCache(session);
            CompanyForm.initializeCache(session);
            CompanyStatus.initializeCache(session);
            Municipality.initializeCache(session);
            CvrPostCode.initializeCache(session);
        }
        List<File> cacheFiles = null;
        int lines = 0;
        if (registrationData instanceof ImportInputStream) {
            ImportInputStream importStream = (ImportInputStream) registrationData;
            cacheFiles = importStream.getCacheFiles();
            lines = importStream.getLineCount();
        }

        Scanner scanner = new Scanner(registrationData, "UTF-8").useDelimiter(String.valueOf(this.commonFetcher.delimiter));
        boolean wrappedInTransaction = importMetadata.isTransactionInProgress();
        long chunkCount = 1;
        long startChunk = importMetadata.getStartChunk();

        InterruptedPull progress = new InterruptedPull();
        timer.clear();

        try {
            while (scanner.hasNext()) {
                try {
                    String data = scanner.next();
                    if (chunkCount >= startChunk) {
                        log.info("Handling chunk " + chunkCount + (lines > 0 ? ("/" + lines) : "") + " (" + data.length() + " chars)");

                        // Save progress
                        progress.setChunk(chunkCount);
                        progress.setFiles(cacheFiles);
                        progress.setStartTime(importMetadata.getImportTime());
                        progress.setInterruptTime(OffsetDateTime.now());
                        progress.setSchemaName(this.getSchema());
                        progress.setPlugin(this.getRegisterManager().getPlugin());

                        Session progressSession = this.configurationSessionManager.getSessionFactory().openSession();
                        progressSession.beginTransaction();
                        progressSession.saveOrUpdate(progress);
                        progressSession.getTransaction().commit();
                        progressSession.close();

                        if (session == null) {
                            session = this.getSessionManager().getSessionFactory().openSession();
                        }

                        if (!wrappedInTransaction) {
                            session.beginTransaction();
                            importMetadata.setTransactionInProgress(true);
                        }
                        try {
                            int count = this.parseData(this.getObjectMapper().readTree(data), importMetadata, session);
                        } catch (JsonParseException e) {
                            ImportInterruptedException ex = new ImportInterruptedException(e);
                            session.getTransaction().rollback();
                            importMetadata.setTransactionInProgress(false);
                            session.clear();
                            ex.setChunk(chunkCount);
                            throw ex;
                        } catch (ImportInterruptedException e) {
                            session.getTransaction().rollback();
                            importMetadata.setTransactionInProgress(false);
                            session.clear();
                            e.setChunk(chunkCount);
                            throw e;
                        }

                        timer.start(TASK_COMMIT);
                        session.flush();
                        if (!wrappedInTransaction) {
                            session.getTransaction().commit();
                            importMetadata.setTransactionInProgress(false);
                            session.clear();
                        }
                        timer.measure(TASK_COMMIT);

                        log.debug("Chunk " + chunkCount + ":\n" + timer.formatAllTotal());
                    }
                    chunkCount++;
                } catch (IOException e) {
                    throw new DataStreamException(e);
                }
            }
            subscribeToMissingCvr();

            log.info("All chunks handled\n"+timer.formatAllTotal());
            Session progressSession = this.configurationSessionManager.getSessionFactory().openSession();
            progressSession.beginTransaction();
            progressSession.delete(progress);
            progressSession.getTransaction().commit();
            progressSession.close();
        } catch (ImportInterruptedException e) {
            log.info("Import aborted in chunk " + chunkCount);
            if (e.getChunk() == null) {
                log.info("That's before our startPoint, propagate startPoint " + startChunk);
                e.setChunk(startChunk);
            }
            e.setFiles(cacheFiles);
            e.setEntityManager(this);
            throw e;
        }
        log.info("Parse complete");
        return null;
    }

    /**
     * If there is any P-numbers that is assigned under a CVR-number that does not exist in datafordeler, ad the missing CVR to a list for fetching
     */
    public void subscribeToMissingCvr() {
        try (Session sessionSub = this.getSessionManager().getSessionFactory().openSession()){
            Transaction tx = sessionSub.beginTransaction();
            String hql_companies = "SELECT DISTINCT companyUnit.newestCvrRelation FROM " + CompanyUnitMetadataRecord.class.getCanonicalName() + " companyUnit " +

                    "WHERE aggregateStatus = 'Aktiv' " +
                    "AND (newestCvrRelation) NOT IN " +
                    "(SELECT company.cvrNumber FROM " + CompanyRecord.class.getCanonicalName() + " company " +
                    "JOIN " + CompanyMetadataRecord.class.getCanonicalName() + " companyMetadata ON company.id" + "=companyMetadata." + CompanyMetadataRecord.DB_FIELD_COMPANY + ")";

            Query querya = sessionSub.createQuery(hql_companies);
            List<Integer> companies = querya.getResultList();
            for (Integer cvr : companies) {
                try {
                    CompanySubscription companySubscription = new CompanySubscription(cvr);
                    sessionSub.save(companySubscription);
                } catch (Exception e) {
                    // Empty catch as a convenient way for if the system tries to add the same cvr twice
                }
            }
            sessionSub.flush();
            tx.commit();
        } catch(Exception e) {
            log.error("Error creating subscription for CVR", e.getStackTrace());
        }
    }



    /**
     * Clean democompanys which has been initiated in the database.
     * democompanys is used on the demoenvironment for demo and education purposes
     */
    public void cleanDemoData(Session session) {
        CompanyRecordQuery personQuery = new CompanyRecordQuery();
        List<String> testCompanyList = Arrays.asList(cvrDemoList.split(","));
        for(String testCompany : testCompanyList) {
            personQuery.addCvrNummer(testCompany);
        }
        session.beginTransaction();
        personQuery.setPageSize(1000);
        personQuery.applyFilters(session);
        List<CompanyRecord> companyEntities = QueryManager.getAllEntities(session, personQuery, CompanyRecord.class);
        for(CompanyRecord companyForDeletion : companyEntities) {
            session.delete(companyForDeletion);
        }
        session.getTransaction().commit();
    }


    protected abstract SessionManager getSessionManager();
    protected abstract String getJsonTypeName();
    protected abstract Class<T> getRecordClass();
    protected abstract UUID generateUUID(T record);

    /**
     * Parse an incoming JsonNode containing CVR data. A node may be a collection of nodes, in which case
     * this method recurses to handle each node separately.
     * Must be idempotent: Running a second time with the same input should not result in new data added to the database
     *
     * The input data for a given entity (e.g. a company) is parsed into a collection of records (instances of subclasses of CvrRecord),
     * then sorted into buckets sharing bitemporality. For each unique bitemporality (representing one or more records),
     * a list of registrations and effects are found and/or created, and one basedata item (instance of subclass of CvrData)
     * is found or created, wired to the registrations and effects, and populated with all records in the bucket.
     *
     * @param jsonNode JSON object containing one or more parseable entities from the CVR data source
     * @return A list of registrations that have been saved to the database
     * @throws ParseException
     */
    public int parseData(JsonNode jsonNode, ImportMetadata importMetadata, Session session) throws DataFordelerException {
        timer.start(TASK_PARSE);
        this.checkInterrupt(importMetadata);
        List<T> items = this.parseNode(jsonNode);
        timer.measure(TASK_PARSE);
        for (T item : items) {
            this.beforeParseSave(item, importMetadata, session);
            item.save(session);
        }
        return items.size();
    }

    protected void beforeParseSave(T item, ImportMetadata importMetadata, Session session) {
        item.setDafoUpdateOnTree(importMetadata.getImportTime());
    }

    public List<T> parseNode(JsonNode jsonNode) {
        if (jsonNode.has("hits")) {
            List<T> items = new ArrayList<T>();
            jsonNode = jsonNode.get("hits");
            if (jsonNode.has("hits")) {
                jsonNode = jsonNode.get("hits");
            }
            if (jsonNode.isArray()) {
                log.debug("Node contains "+jsonNode.size()+" subnodes");
                for (JsonNode item : jsonNode) {
                    //this.parseData(item, importMetadata, session);
                    items.addAll(this.parseNode(item));
                }
                return items;
            }
        }

        if (jsonNode.has("_source")) {
            jsonNode = jsonNode.get("_source");
        }
        String jsonTypeName = this.getJsonTypeName();
        if (jsonNode.has(jsonTypeName)) {
            jsonNode = jsonNode.get(jsonTypeName);
        }
        try {
            return Collections.singletonList(getObjectMapper().treeToValue(jsonNode, this.getRecordClass()));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return Collections.EMPTY_LIST;
        }
    }


    /**
     * Sorts a collection of records into buckets sharing bitemporality
     * @param records
     * @return
     */
    public ListHashMap<Bitemporality, CvrRecord> sortIntoGroups(Collection<CvrRecord> records) {
        // Sort the records into groups that share bitemporality
        ListHashMap<Bitemporality, CvrRecord> recordGroups = new ListHashMap<>();
        for (CvrRecord record : records) {
            if (record instanceof CvrBitemporalRecord) {
                CvrBitemporalRecord bitemporalRecord = (CvrBitemporalRecord) record;
                // Find the appropriate registration object
                if (IMPORT_ONLY_CURRENT) {
                    if (bitemporalRecord.getRegistrationTo() == null && bitemporalRecord.getValidTo() == null) {
                        recordGroups.add(new Bitemporality(roundTime(bitemporalRecord.getRegistrationFrom()), roundTime(bitemporalRecord.getRegistrationTo()), bitemporalRecord.getValidFrom(), bitemporalRecord.getValidTo()), bitemporalRecord);
                    }
                } else if (DONT_IMPORT_CURRENT) {
                    if (bitemporalRecord.getRegistrationTo() != null || bitemporalRecord.getValidTo() != null) {
                        recordGroups.add(new Bitemporality(roundTime(bitemporalRecord.getRegistrationFrom()), roundTime(bitemporalRecord.getRegistrationTo()), bitemporalRecord.getValidFrom(), bitemporalRecord.getValidTo()), bitemporalRecord);
                    }
                } else {
                    recordGroups.add(new Bitemporality(roundTime(bitemporalRecord.getRegistrationFrom()), roundTime(bitemporalRecord.getRegistrationTo()), bitemporalRecord.getValidFrom(), bitemporalRecord.getValidTo()), bitemporalRecord);
                }
            }
        }
        return recordGroups;
    }

    /**
     * Internal rounding of timestamps, for determining how far apart two data points
     * must be for us to consider them separate registrations and effects.
     * Often, data points may be separated by less than one second, but it would be ineffective
     * to store this as two separate registrations, leading to an explosion in data.
     * It is better to cut off some unnecessary precision to get better performance
     * As it stands now, if two data points are timestamped in the same minute, we consider
     * them in the same registration
     */
    private static OffsetDateTime roundTime(OffsetDateTime in) {
        if (in != null) {
            //return in.withHour(0).withMinute(0).withSecond(0).withNano(0);
            //return in.withMinute(0).withSecond(0).withNano(0);
            return in.withSecond(0).withNano(0);
        }
        return null;
    }

    /**
     * This class saves to the database during import, again to achieve better performance,
     * instead of returning potentially millions of unsaved objects for others to save.
     * (Which quickly fills up the heap, leading to OutOfMemory errors)
     * @return true
     */
    @Override
    public boolean handlesOwnSaves() {
        return true;
    }

    private void checkInterrupt(ImportMetadata importMetadata) throws ImportInterruptedException {
        if (importMetadata.getStop()) {
            throw new ImportInterruptedException(new InterruptedException());
        }
    }

    @Override
    public boolean pullEnabled() {
        CvrConfiguration configuration = this.getRegisterManager().getConfigurationManager().getConfiguration();
        CvrConfiguration.RegisterType registerType = configuration.getRegisterType(this.getSchema());
        return (registerType != null && registerType != CvrConfiguration.RegisterType.DISABLED);
    }

}
