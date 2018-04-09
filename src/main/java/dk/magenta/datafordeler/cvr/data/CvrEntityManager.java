package dk.magenta.datafordeler.cvr.data;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import dk.magenta.datafordeler.core.database.*;
import dk.magenta.datafordeler.core.exception.*;
import dk.magenta.datafordeler.core.io.ImportInputStream;
import dk.magenta.datafordeler.core.io.ImportMetadata;
import dk.magenta.datafordeler.core.io.Receipt;
import dk.magenta.datafordeler.core.plugin.Communicator;
import dk.magenta.datafordeler.core.plugin.EntityManager;
import dk.magenta.datafordeler.core.plugin.RegisterManager;
import dk.magenta.datafordeler.core.plugin.ScanScrollCommunicator;
import dk.magenta.datafordeler.core.util.ItemInputStream;
import dk.magenta.datafordeler.core.util.ListHashMap;
import dk.magenta.datafordeler.core.util.Stopwatch;
import dk.magenta.datafordeler.cvr.CvrPlugin;
import dk.magenta.datafordeler.cvr.CvrRegisterManager;
import dk.magenta.datafordeler.cvr.configuration.CvrConfiguration;
import dk.magenta.datafordeler.cvr.data.unversioned.*;
import dk.magenta.datafordeler.cvr.records.CvrBitemporalRecord;
import dk.magenta.datafordeler.cvr.records.CvrEntityRecord;
import dk.magenta.datafordeler.cvr.records.CvrRecord;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Session;
import org.springframework.beans.factory.annotation.Autowired;
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
public abstract class CvrEntityManager<E extends CvrEntity<E, R>, R extends CvrRegistration<E, R, V>, V extends CvrEffect, D extends CvrData<V, D>, T extends CvrEntityRecord>
        extends EntityManager {

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private Stopwatch timer;

    @Autowired
    private ConfigurationSessionManager configurationSessionManager;

    private static final String TASK_PARSE = "CvrParse";
    private static final String TASK_FIND_ENTITY = "CvrFindEntity";
    private static final String TASK_FIND_REGISTRATIONS = "CvrFindRegistrations";
    private static final String TASK_FIND_ITEMS = "CvrFindItems";
    private static final String TASK_POPULATE_DATA = "CvrPopulateData";
    private static final String TASK_SAVE = "CvrSave";
    private static final String TASK_COMMIT = "Transaction commit";

    private static boolean IMPORT_ONLY_CURRENT = false;
    private static boolean DONT_IMPORT_CURRENT = false;
    private static boolean SAVE_RECORD_DATA = false;
    private static boolean SAVE_ONLY_RECORDS = false;

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

    @Override
    protected Communicator getReceiptSender() {
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
    protected URI getReceiptEndpoint(Receipt receipt) {
        return null;
    }

    /**
     * Parse a raw reference data stream into a RegistrationReference object
     */
    @Override
    public RegistrationReference parseReference(InputStream referenceData) throws IOException {
        return this.getObjectMapper().readValue(referenceData, this.managedRegistrationReferenceClass);
    }

    /**
     * Parse a raw reference data string into a RegistrationReference object
     */
    @Override
    public RegistrationReference parseReference(String referenceData, String charsetName) throws IOException {
        return this.getObjectMapper().readValue(referenceData.getBytes(charsetName), this.managedRegistrationReferenceClass);
    }

    protected abstract RegistrationReference createRegistrationReference(URI uri);

    /**
     * Create a RegistrationReference object that encapsulates the given URI in the proper object
     */
    @Override
    public RegistrationReference parseReference(URI uri) {
        return this.createRegistrationReference(uri);
    }

    /**
     * Returns the reference URI based on a RegistrationReference, basically unpacking it
     */
    @Override
    public URI getRegistrationInterface(RegistrationReference reference) throws WrongSubclassException {
        if (!this.managedRegistrationReferenceClass.isInstance(reference)) {
            throw new WrongSubclassException(this.managedRegistrationReferenceClass, reference);
        }
        if (reference.getURI() != null) {
            return reference.getURI();
        }
        return null;
    }

    @Override
    protected URI getListChecksumInterface(OffsetDateTime fromDate) {
        return this.getRegisterManager().getListChecksumInterface(this.getSchema(), fromDate);
    }

    @Override
    protected ItemInputStream<? extends EntityReference> parseChecksumResponse(InputStream responseContent) throws DataFordelerException {
        return ItemInputStream.parseJsonStream(responseContent, this.managedEntityReferenceClass, "items", this.getObjectMapper());
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
            Industry.initializeCache(session);
            CompanyForm.initializeCache(session);
            CompanyStatus.initializeCache(session);
            Municipality.initializeCache(session);
            PostCode.initializeCache(session);
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
                            this.parseData(this.getObjectMapper().readTree(data), importMetadata, session);
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

                        log.info("Chunk " + chunkCount + ":\n" + timer.formatAllTotal());
                    }
                    chunkCount++;
                } catch (IOException e) {
                    throw new DataStreamException(e);
                }
            }
            log.info("Removing progress indicator");
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
        return null;
    }

    protected abstract SessionManager getSessionManager();
    protected abstract String getJsonTypeName();
    protected abstract Class<T> getRecordClass();
    protected abstract Class<E> getEntityClass();
    protected abstract UUID generateUUID(T record);
    protected abstract E createBasicEntity(T record);
    protected abstract D createDataItem();

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
    public List<? extends Registration> parseData(JsonNode jsonNode, ImportMetadata importMetadata, Session session) throws DataFordelerException {
        ArrayList<Registration> registrations = new ArrayList<>();

        if (jsonNode.has("hits")) {
            jsonNode = jsonNode.get("hits");
            if (jsonNode.has("hits")) {
                jsonNode = jsonNode.get("hits");
            }
            if (jsonNode.isArray()) {
                if (jsonNode.size() == 0) {
                    throw new DataStreamException("No input data");
                }
                log.info("Node contains "+jsonNode.size()+" subnodes");
                // We have a list of results

                for (JsonNode item : jsonNode) {
                    registrations.addAll(this.parseData(item, importMetadata, session));
                    //this.parseData(item, importMetadata, session);
                }

                return registrations;
            }
        }


        timer.start(TASK_PARSE);
        this.checkInterrupt(importMetadata);
        if (jsonNode.has("_source")) {
            jsonNode = jsonNode.get("_source");
        }
        String jsonTypeName = this.getJsonTypeName();
        if (jsonNode.has(jsonTypeName)) {
            jsonNode = jsonNode.get(jsonTypeName);
        }
        T toplevelRecord;
        try {
            toplevelRecord = getObjectMapper().treeToValue(jsonNode, this.getRecordClass());
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return null;
        }
        timer.measure(TASK_PARSE);

        
        toplevelRecord.setDafoUpdateOnTree(importMetadata.getImportTime());
        toplevelRecord.save(session);

        if (!SAVE_ONLY_RECORDS) {

            timer.start(TASK_FIND_ENTITY);
            this.checkInterrupt(importMetadata);
            UUID uuid = this.generateUUID(toplevelRecord);
            E entity = null;
            String domain = CvrPlugin.getDomain();
            Identification identification = QueryManager.getIdentification(session, uuid, domain);
            if (identification != null) {
                entity = QueryManager.getEntity(session, identification, this.getEntityClass());
            }
            if (entity == null) {
                log.debug("Creating new Entity");
                entity = this.createBasicEntity(toplevelRecord);
                entity.setIdentifikation(
                        QueryManager.getOrCreateIdentification(session, uuid, domain)
                );
            } else {
                log.debug("Using existing entity");
            }
            timer.measure(TASK_FIND_ENTITY);

            this.checkInterrupt(importMetadata);
            HashSet<R> entityRegistrations = new HashSet<>();
            OffsetDateTime lastUpdate = this.getLastUpdated(session);
            List<CvrRecord> recentlyUpdated = toplevelRecord.getSince(lastUpdate);


            ListHashMap<Bitemporality, CvrRecord> groups = this.sortIntoGroups(recentlyUpdated);

            for (Bitemporality bitemporality : groups.keySet()) {

                timer.start(TASK_FIND_REGISTRATIONS);
                List<CvrRecord> group = groups.get(bitemporality);
                List<R> entityRegistrationList = entity.findRegistrations(bitemporality.registrationFrom, bitemporality.registrationTo);
                ArrayList<V> effects = new ArrayList<>();
                for (R registration : entityRegistrationList) {
                    V effect = registration.getEffect(bitemporality);
                    if (effect == null) {
                        effect = registration.createEffect(bitemporality);
                    }
                    effects.add(effect);
                }
                entityRegistrations.addAll(entityRegistrationList);
                timer.measure(TASK_FIND_REGISTRATIONS);


                timer.start(TASK_FIND_ITEMS);
                // R-V-D scenario
                // Every DataItem that we locate for population must match the given effects exactly,
                // or we risk assigning data to an item that shouldn't be assigned to
                D baseData = null;
                HashSet<D> searchPool = new HashSet<>();
                for (V effect : effects) {
                    searchPool.addAll(effect.getDataItems());
                }

                for (D data : searchPool) {
                    if (data.getEffects().containsAll(effects) && effects.containsAll(data.getEffects())) {
                        baseData = data;
                        log.debug("Reuse existing basedata");
                        break;
                    }
                }
                if (baseData == null) {
                    log.debug("Creating new basedata");
                    baseData = this.createDataItem();
                    for (V effect : effects) {
                        log.debug("Wire basedata to effect " + effect.getRegistration().getRegistrationFrom() + "|" + effect.getRegistration().getRegistrationTo() + "|" + effect.getEffectFrom() + "|" + effect.getEffectTo());
                        baseData.addEffect(effect);
                    }
                }
                timer.measure(TASK_FIND_ITEMS);

                OffsetDateTime timestamp = importMetadata.getImportTime();
                for (CvrRecord record : group) {
                    timer.start(TASK_POPULATE_DATA + " " + record.getClass().getSimpleName());
                    record.populateBaseData(baseData, session, timestamp);
                    baseData.setUpdated(timestamp);
                    if (SAVE_RECORD_DATA) {
                        RecordData recordData = new RecordData(timestamp);
                        recordData.setSourceData(objectMapper.valueToTree(record).toString());
                        baseData.addRecordData(recordData);
                    }
                    timer.measure(TASK_POPULATE_DATA + " " + record.getClass().getSimpleName());
                }
            }


            timer.start(TASK_SAVE);
            for (R registration : entityRegistrations) {
                registration.setLastImportTime(importMetadata.getImportTime());
                session.saveOrUpdate(registration);
            }
            session.saveOrUpdate(entity);
            timer.measure(TASK_SAVE);


            registrations.addAll(entityRegistrations);

            this.checkInterrupt(importMetadata);

        }
        return registrations;
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
