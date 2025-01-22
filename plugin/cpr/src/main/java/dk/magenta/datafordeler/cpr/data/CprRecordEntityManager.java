package dk.magenta.datafordeler.cpr.data;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import dk.magenta.datafordeler.core.database.Identification;
import dk.magenta.datafordeler.core.database.QueryManager;
import dk.magenta.datafordeler.core.database.SessionManager;
import dk.magenta.datafordeler.core.exception.ConfigurationException;
import dk.magenta.datafordeler.core.exception.DataFordelerException;
import dk.magenta.datafordeler.core.exception.DataStreamException;
import dk.magenta.datafordeler.core.exception.ImportInterruptedException;
import dk.magenta.datafordeler.core.io.ImportInputStream;
import dk.magenta.datafordeler.core.io.ImportMetadata;
import dk.magenta.datafordeler.core.io.WrappedInputStream;
import dk.magenta.datafordeler.core.plugin.*;
import dk.magenta.datafordeler.core.util.LabeledSequenceInputStream;
import dk.magenta.datafordeler.core.util.ListHashMap;
import dk.magenta.datafordeler.core.util.Stopwatch;
import dk.magenta.datafordeler.cpr.CprRegisterManager;
import dk.magenta.datafordeler.cpr.configuration.CprConfiguration;
import dk.magenta.datafordeler.cpr.configuration.CprConfigurationManager;
import dk.magenta.datafordeler.cpr.parsers.CprSubParser;
import dk.magenta.datafordeler.cpr.records.CprDataRecord;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.*;
import java.net.URI;
import java.nio.charset.Charset;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;

@Component
public abstract class CprRecordEntityManager<T extends CprDataRecord, E extends CprRecordEntity> extends EntityManager {

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CprConfigurationManager cprConfigurationManager;

    @Autowired
    Stopwatch timer;

    private static final boolean SAVE_RECORD_DATA = false;

    public static final String IMPORTCONFIG_RECORDTYPE = "recordtype";
    public static final String IMPORTCONFIG_PNR = "personnummer";

    private final HttpCommunicator commonFetcher;

    protected Logger log = LogManager.getLogger(this.getClass().getSimpleName());

    private final Collection<String> handledURISubstrings;

    protected abstract String getBaseName();

    public CprRecordEntityManager() {
        this.commonFetcher = new HttpCommunicator();
        this.handledURISubstrings = new ArrayList<>();
    }

    public CprConfigurationManager getCprConfigurationManager() {
        return this.cprConfigurationManager;
    }

    @Override
    public void setRegisterManager(RegisterManager registerManager) {
        super.setRegisterManager(registerManager);
        //this.handledURISubstrings.add(expandBaseURI(this.getBaseEndpoint(), "/" + this.getBaseName(), null, null).toString());
        //this.handledURISubstrings.add(expandBaseURI(this.getBaseEndpoint(), "/get/" + this.getBaseName(), null, null).toString());
    }

    public abstract String getDomain();

    @Override
    public Collection<String> getHandledURISubstrings() {
        return this.handledURISubstrings;
    }

    @Override
    protected ObjectMapper getObjectMapper() {
        return this.objectMapper;
    }

    @Override
    protected Communicator getRegistrationFetcher() {
        return this.commonFetcher;
    }

    @Override
    public URI getBaseEndpoint() {
        return this.getRegisterManager().getBaseEndpoint();
    }

    /*
        @Override
        protected URI getReceiptEndpoint(Receipt receipt) {
            return null;
        }

        @Override
        public RegistrationReference parseReference(InputStream referenceData) throws IOException {
            return this.getObjectMapper().readValue(referenceData, this.managedRegistrationReferenceClass);
        }

        @Override
        public RegistrationReference parseReference(String referenceData, String charsetName) throws IOException {
            return this.getObjectMapper().readValue(referenceData.getBytes(charsetName), this.managedRegistrationReferenceClass);
        }

        @Override
        public RegistrationReference parseReference(URI uri) {
            return this.createRegistrationReference(uri);
        }

        @Override
        public URI getRegistrationInterface(RegistrationReference reference) throws WrongSubclassException {
            if (!this.managedRegistrationReferenceClass.isInstance(reference)) {
                throw new WrongSubclassException(this.managedRegistrationReferenceClass, reference);
            }
            if (reference.getURI() != null) {
                return reference.getURI();
            }
            return EntityManager.expandBaseURI(this.getBaseEndpoint(), "/get/"+this.getBaseName()+"/"+reference.getChecksum());
        }

        @Override
        protected URI getListChecksumInterface(OffsetDateTime fromDate) {
            return this.getRegisterManager().getListChecksumInterface(this.getSchema(), fromDate);
        }

        @Override
        protected ItemInputStream<? extends EntityReference> parseChecksumResponse(InputStream responseContent) throws DataFordelerException {
            return ItemInputStream.parseJsonStream(responseContent, this.managedEntityReferenceClass, "items", this.getObjectMapper());
        }
    */
    @Override
    protected Logger getLog() {
        return this.log;
    }

    protected CprConfiguration getConfiguration() {
        return this.cprConfigurationManager.getConfiguration();
    }

    public CprRegisterManager getRegisterManager() {
        return (CprRegisterManager) super.getRegisterManager();
    }

    protected abstract void parseAlternate(E entity, Collection<T> records, ImportMetadata importMetadata);


    protected abstract SessionManager getSessionManager();

    protected abstract CprSubParser<T> getParser();

    protected abstract Class<E> getEntityClass();

    protected abstract UUID generateUUID(T record);

    protected abstract E createBasicEntity(T record);

    private static final String TASK_PARSE = "CprParse";
    private static final String TASK_FIND_ENTITY = "CprFindEntity";
    private static final String TASK_FIND_REGISTRATIONS = "CprFindRegistrations";
    private static final String TASK_FIND_ITEMS = "CprFindItems";
    private static final String TASK_POPULATE_DATA = "CprPopulateData";
    private static final String TASK_SAVE = "CprSave";
    private static final String TASK_CHUNK_HANDLE = "CprChunk";

    @Override
    public void parseData(InputStream registrationData, ImportMetadata importMetadata) throws DataFordelerException {
        //String charset = "UTF8";//this.getConfiguration().getRegisterCharset(this);
        String charset = this.getConfiguration().getRegisterCharset(this);
        CprSubParser<T> parser = this.getParser();
        Session session = importMetadata.getSession();
        boolean wrappedInTransaction = importMetadata.isTransactionInProgress();
        log.info("Parsing in thread " + Thread.currentThread().getId());

        int maxChunkSize = 1000;
        List<File> cacheFiles = null;
        int totalChunks = 0;
        LabeledSequenceInputStream labeledSequenceInputStream = null;

        if (registrationData instanceof ImportInputStream) {
            ImportInputStream importStream = (ImportInputStream) registrationData;
            cacheFiles = importStream.getCacheFiles();
            int lines = importStream.getLineCount();

            // Integer division with rounding up
            //totalChunks = (int) Math.ceil((float) lines / (float) maxChunkSize);
            totalChunks = (lines + maxChunkSize - 1) / maxChunkSize;
            //totalChunks = (lines / maxChunkSize) + (lines % maxChunkSize == 0 ? 0 : 1);
            InputStream innerStream = importStream;
            while (innerStream instanceof WrappedInputStream) {
                innerStream = ((WrappedInputStream) innerStream).getInner();
            }
            if (innerStream instanceof LabeledSequenceInputStream) {
                labeledSequenceInputStream = (LabeledSequenceInputStream) innerStream;
            }
        }

        BufferedReader reader = new BufferedReader(new InputStreamReader(registrationData, Charset.forName(charset)));

        boolean done = false;
        long chunkCount = 1;
        long startChunk = importMetadata.getStartChunk();
        while (!done) {
            try {
                String line = null;
                int i = 0;
                int size = 0;
                LinkedHashMap<String, ArrayList<String>> dataChunks = new LinkedHashMap<>();
                try {
                    for (i = 0; i < maxChunkSize; i++) {
                        line = reader.readLine();
                        if (line == null) {
                            break;
                        }
                        String origin = labeledSequenceInputStream != null ? labeledSequenceInputStream.getCurrentLabel() : null;
                        ArrayList<String> dataChunk = dataChunks.computeIfAbsent(origin, k -> new ArrayList<>());
                        dataChunk.add(line);
                        size += line.length();
                    }
                    if (line == null) {
                        done = true;
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    done = true;
                }

                if (chunkCount >= startChunk) {
                    log.info("Handling chunk " + chunkCount + (totalChunks > 0 ? ("/" + totalChunks) : "") + " (" + size + " chars)");
                    timer.start(TASK_CHUNK_HANDLE);
                    // Parse chunk into a set of records
                    timer.start(TASK_PARSE);
                    List<T> chunkRecords = parser.parse(dataChunks, charset);
                    log.debug("Batch parsed into " + chunkRecords.size() + " records");
                    timer.measure(TASK_PARSE);

                    if (!chunkRecords.isEmpty()) {

                        if (!wrappedInTransaction) {
                            session.beginTransaction();
                            importMetadata.setTransactionInProgress(true);
                        }
                        LinkedHashSet<UUID> uuids = new LinkedHashSet<>();

                        try {

                            // Find Entities (or create those that are missing), and put them in the recordMap
                            timer.start(TASK_FIND_ENTITY);
                            ListHashMap<E, T> recordMap = new ListHashMap<>();
                            HashMap<UUID, E> entityCache = new HashMap<>();
                            for (T record : chunkRecords) {
                                this.checkInterrupt(importMetadata);
                                this.handleRecord(record, importMetadata);
                                if (this.filter(record, importMetadata.getImportConfiguration())) {
                                    UUID uuid = this.generateUUID(record);
                                    uuids.add(uuid);
                                    E entity = entityCache.get(uuid);
                                    if (entity == null) {
                                        Identification identification = QueryManager.getOrCreateIdentification(session, uuid, this.getDomain());
                                        entity = QueryManager.getEntity(session, identification, this.getEntityClass());
                                        if (entity == null) {
                                            entity = this.createBasicEntity(record);
                                            entity.setIdentification(identification);
                                        }
                                        entityCache.put(uuid, entity);
                                    }
                                    recordMap.add(entity, record);
                                    recordMap.get(entity, 0);
                                }
                            }
                            log.info("Batch resulted in " + recordMap.keySet().size() + " unique entities");
                            timer.measure(TASK_FIND_ENTITY);


                            for (UUID uuid : uuids) {
                                E entity = entityCache.get(uuid);
                                List<T> records = recordMap.get(entity);


                                session.saveOrUpdate(entity);
                                this.parseAlternate(entity, records, importMetadata);
                                session.saveOrUpdate(entity);
                            }

                            this.checkInterrupt(importMetadata);

                        } catch (ImportInterruptedException e) {
                            if (!wrappedInTransaction) {
                                session.getTransaction().rollback();
                                importMetadata.setTransactionInProgress(false);
                                session.clear();
                            }
                            e.setChunk(chunkCount);
                            throw e;
                        }

                        session.flush();
                        session.clear();

                        if (!wrappedInTransaction) {
                            session.getTransaction().commit();
                            importMetadata.setTransactionInProgress(false);
                        }
                    }

                    long chunkTime = timer.getTotal(TASK_CHUNK_HANDLE);
                    timer.reset(TASK_CHUNK_HANDLE);
                    if (!chunkRecords.isEmpty()) {
                        log.info(i + " lines => " + chunkRecords.size() + " records handled in " + chunkTime + " ms (" + ((float) chunkTime / (float) chunkRecords.size()) + " ms avg)");
                    }
                    log.info(timer.formatAllTotal());
                }
                chunkCount++;

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
        }
    }


    protected boolean filter(T record, ObjectNode importConfiguration) {
        return record.filter(importConfiguration);
    }

    @Override
    public boolean handlesOwnSaves() {
        return true;
    }

    private void checkInterrupt(ImportMetadata importMetadata) throws ImportInterruptedException {
        if (importMetadata.getStop()) {
            throw new ImportInterruptedException(new InterruptedException());
        }
    }

    protected void handleRecord(T record, ImportMetadata importMetadata) {
    }


    public int getJobId() {
        return 0;
    }

    public int getCustomerId() {
        return 0;
    }

    public String getLocalSubscriptionFolder() {
        return null;
    }

    public boolean isSetupSubscriptionEnabled() {
        return false;
    }

    protected URI getSubscriptionURI() throws DataFordelerException {
        CprConfiguration configuration = this.getConfiguration();
        return configuration.getRegisterSubscriptionURI(this);
    }

    public void addSubscription(String contents, String charset, CprRecordEntityManager entityManager) throws DataFordelerException {
        if (this.getJobId() == 0) {
            throw new ConfigurationException("CPR jobId not set");
        }
        if (this.getCustomerId() == 0) {
            throw new ConfigurationException("CPR customerId not set");
        }
        if (this.getLocalSubscriptionFolder() == null || this.getLocalSubscriptionFolder().isEmpty()) {
            throw new ConfigurationException("CPR localSubscriptionFolder not set");
        }
        // Create file
        LocalDate subscriptionDate = LocalDate.now();
        // If it's after noon, CPR will not process the file today.
        ZonedDateTime dailyDeadline = subscriptionDate.atTime(LocalTime.of(11, 45)).atZone(ZoneId.of("Europe/Copenhagen"));
        if (ZonedDateTime.now().isAfter(dailyDeadline)) {
            subscriptionDate = subscriptionDate.plusDays(1);
        }
        File localSubscriptionFolder = new File(this.getLocalSubscriptionFolder());
        if (localSubscriptionFolder.isFile()) {
            throw new ConfigurationException("CPR localSubscriptionFolder is a file, not a folder");
        }
        if (!localSubscriptionFolder.exists()) {
            localSubscriptionFolder.mkdirs();
        }
        File subscriptionFile = new File(
                localSubscriptionFolder,
                String.format(
                        "d%02d%02d%02d",
                        subscriptionDate.getYear() % 100,
                        subscriptionDate.getMonthValue(),
                        subscriptionDate.getDayOfMonth()
                ) +
                        "." +
                        String.format("i%06d", this.getJobId())

        );
        try {
            if (!subscriptionFile.exists()) {
                subscriptionFile.createNewFile();
            }
            FileOutputStream fileOutputStream = new FileOutputStream(subscriptionFile, true);
            fileOutputStream.write(contents.getBytes(charset));
            fileOutputStream.close();
        } catch (IOException e) {
            throw new DataStreamException(e);
        }

        // Upload file
        URI destination = this.getSubscriptionURI();
        FtpCommunicator ftpSender = this.getRegisterManager().getFtpCommunicator(null, destination, entityManager);
        try {
            ftpSender.send(destination, subscriptionFile);
        } catch (IOException e) {
            throw new DataStreamException(e);
        }
    }

    /**
     * Should return whether the configuration is set so that pulls are enabled for this entitymanager
     */
    @Override
    public boolean pullEnabled() {
        try {
            return this.getRegisterManager().getEventInterface(this) != null;
        } catch (DataFordelerException e) {
            return false;
        }
    }

}
