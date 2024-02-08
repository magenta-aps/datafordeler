package dk.magenta.datafordeler.ger.data;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import dk.magenta.datafordeler.core.database.*;
import dk.magenta.datafordeler.core.database.ConfigurationSessionManager;
import dk.magenta.datafordeler.core.database.SessionManager;
import dk.magenta.datafordeler.core.exception.DataFordelerException;
import dk.magenta.datafordeler.core.exception.DataStreamException;
import dk.magenta.datafordeler.core.exception.ImportInterruptedException;
import dk.magenta.datafordeler.core.io.ImportInputStream;
import dk.magenta.datafordeler.core.io.ImportMetadata;
import dk.magenta.datafordeler.core.plugin.Communicator;
import dk.magenta.datafordeler.core.plugin.EntityManager;
import dk.magenta.datafordeler.core.plugin.HttpCommunicator;
import dk.magenta.datafordeler.core.plugin.RegisterManager;
import dk.magenta.datafordeler.core.util.Stopwatch;
import dk.magenta.datafordeler.ger.GerRegisterManager;
import dk.magenta.datafordeler.ger.configuration.GerConfiguration;
import dk.magenta.datafordeler.ger.configuration.GerConfigurationManager;
import dk.magenta.datafordeler.ger.parser.SpreadsheetConverter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.InputStream;
import java.net.URI;
import java.util.*;

@Component
public abstract class GerEntityManager<E extends GerEntity> extends EntityManager {

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    GerConfigurationManager gerConfigurationManager;

    @Autowired
    Stopwatch timer;

    @Autowired
    private ConfigurationSessionManager configurationSessionManager;

    @Autowired
    private SessionManager sessionManager;

    private final HttpCommunicator commonFetcher;

    protected Logger log = LogManager.getLogger(this.getClass().getCanonicalName());

    private final Collection<String> handledURISubstrings;

    protected abstract String getBaseName();

    public GerEntityManager() {
        this.commonFetcher = new HttpCommunicator();
        this.handledURISubstrings = new ArrayList<>();
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

    @Override
    protected Logger getLog() {
        return this.log;
    }

    protected GerConfiguration getConfiguration() {
        return this.gerConfigurationManager.getConfiguration();
    }

    public GerRegisterManager getRegisterManager() {
        return (GerRegisterManager) super.getRegisterManager();
    }

    protected abstract String getSheetName();

    private static final String TASK_PARSE = "GerParse";
    private static final String TASK_FIND_ENTITY = "GerFindEntity";
    private static final String TASK_POPULATE_DATA = "GerPopulateData";
    private static final String TASK_SAVE = "GerSave";
    private static final String TASK_COMMIT = "GerCommit";
    private static final String TASK_CHUNK_HANDLE = "GerChunk";


    protected abstract Class<E> getEntityClass();

    protected abstract UUID generateUUID(RawData rawData);

    protected abstract E createBasicEntity(RawData record, Session session);

    @Override
    public void parseData(InputStream sheetData, ImportMetadata importMetadata) throws DataFordelerException {

        String extension = "xlsx";
        if (sheetData instanceof ImportInputStream) {
            ImportInputStream importInputStream = (ImportInputStream) sheetData;
            if (!importInputStream.getCacheFiles().isEmpty()) {
                File firstFile = importInputStream.getCacheFiles().get(0);
                String fileName = firstFile.getName();
                extension = fileName.substring(fileName.lastIndexOf('.') + 1);
            }
        }
        SpreadsheetConverter converter = SpreadsheetConverter.getConverterByExtension(extension);

        HashMap<UUID, E> entityCache = new HashMap<>();
        Session session = importMetadata.getSession();
        boolean wrappedInTransaction = importMetadata.isTransactionInProgress();
        if (!wrappedInTransaction) {
            session.beginTransaction();
            importMetadata.setTransactionInProgress(true);
        }
        timer.clear();
        Map<String, List<RawData>> sheets;
        try {
            sheets = converter.convert(sheetData);
        } catch (Exception e) {
            throw new DataStreamException(e);
        }

        String sheetName = this.getSheetName();
        List<RawData> sheet = sheets.get(sheetName);

        for (RawData rawData : sheet) {
            if (!rawData.isEmpty()) {
                timer.start(TASK_PARSE);
                timer.measure(TASK_PARSE);

                timer.start(TASK_FIND_ENTITY);
                E entity = this.getEntity(entityCache, session, rawData);
                timer.measure(TASK_FIND_ENTITY);

                timer.start(TASK_POPULATE_DATA);
                this.updateEntity(entity, rawData, importMetadata);
                entity.setDafoUpdated(importMetadata.getImportTime());
                timer.measure(TASK_POPULATE_DATA);

                timer.start(TASK_SAVE);
                session.save(entity);
                timer.measure(TASK_SAVE);
            }
        }

        if (!wrappedInTransaction) {
            session.getTransaction().commit();
            importMetadata.setTransactionInProgress(false);
        }
        log.info(timer.formatAllTotal());
    }

    private E getEntity(HashMap<UUID, E> entityCache, Session session, RawData rawData) {
        UUID uuid = this.generateUUID(rawData);
        E entity = entityCache.get(uuid);
        if (entity == null) {
            Identification identification = QueryManager.getOrCreateIdentification(session, uuid, this.getDomain());
            entity = QueryManager.getEntity(session, identification, this.getEntityClass());
            if (entity == null) {
                entity = this.createBasicEntity(rawData, session);
                entity.setIdentification(identification);
            }
            entityCache.put(uuid, entity);
        }
        return entity;
    }

    protected abstract void updateEntity(E entity, RawData rawData, ImportMetadata importMetadata);

    protected boolean filter(JsonNode record, ObjectNode importConfiguration) {
        return true;
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

    /**
     * Should return whether the configuration is set so that pulls are enabled for this entitymanager
     */
    @Override
    public boolean pullEnabled() {
        GerConfiguration configuration = this.getRegisterManager().getConfigurationManager().getConfiguration();
        GerConfiguration.RegisterType registerType = configuration.getRegisterType(this.getSchema());
        return (registerType != null && registerType != GerConfiguration.RegisterType.DISABLED);
    }
}
