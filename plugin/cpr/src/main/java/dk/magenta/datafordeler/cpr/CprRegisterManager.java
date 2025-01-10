package dk.magenta.datafordeler.cpr;

import com.fasterxml.jackson.databind.ObjectMapper;
import dk.magenta.datafordeler.core.database.SessionManager;
import dk.magenta.datafordeler.core.exception.ConfigurationException;
import dk.magenta.datafordeler.core.exception.DataFordelerException;
import dk.magenta.datafordeler.core.exception.DataStreamException;
import dk.magenta.datafordeler.core.exception.WrongSubclassException;
import dk.magenta.datafordeler.core.io.ImportInputStream;
import dk.magenta.datafordeler.core.io.ImportMetadata;
import dk.magenta.datafordeler.core.io.PluginSourceData;
import dk.magenta.datafordeler.core.plugin.*;
import dk.magenta.datafordeler.core.util.ItemInputStream;
import dk.magenta.datafordeler.cpr.configuration.CprConfiguration;
import dk.magenta.datafordeler.cpr.configuration.CprConfigurationManager;
import dk.magenta.datafordeler.cpr.data.CprGeoEntityManager;
import dk.magenta.datafordeler.cpr.data.CprRecordEntityManager;
import dk.magenta.datafordeler.cpr.data.person.PersonEntity;
import dk.magenta.datafordeler.cpr.synchronization.CprSourceData;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.*;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

@Component
public class CprRegisterManager extends RegisterManager {

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CprConfigurationManager configurationManager;

    @Autowired
    private CprPlugin plugin;

    @Autowired
    private SessionManager sessionManager;

    private final Logger log = LogManager.getLogger(CprRegisterManager.class.getCanonicalName());

    private String localCopyFolder;

    private String proxyString = null;

    /**
     * RegisterManager initialization; set up configuration and source fetcher.
     * We store fetched data in a local cache, so create a random folder for that.
     */
    @PostConstruct
    public void init() throws IOException {
        CprConfiguration configuration = this.configurationManager.getConfiguration();
        this.localCopyFolder = this.ensureLocalCopyFolder(configuration.getLocalCopyFolder());
        this.proxyString = configuration.getProxyUrl();
    }

    public CprConfigurationManager getConfigurationManager() {
        return this.configurationManager;
    }

    @Override
    protected Logger getLog() {
        return this.log;
    }

    @Override
    public Plugin getPlugin() {
        return this.plugin;
    }

    @Override
    public SessionManager getSessionManager() {
        return this.sessionManager;
    }

    @Override
    public URI getBaseEndpoint() {
        return null;
    }

    @Override
    protected Communicator getEventFetcher() {
        return null;
    }

    @Override
    protected ObjectMapper getObjectMapper() {
        return this.objectMapper;
    }

    @Override
    public URI getEventInterface(EntityManager entityManager) throws DataFordelerException {
        if (entityManager instanceof CprRecordEntityManager || entityManager instanceof CprGeoEntityManager) {
            CprConfiguration configuration = this.configurationManager.getConfiguration();
            return configuration.getRegisterURI(entityManager);
        }
        return null;
    }

    public String getPullCronSchedule() {
        // TODO: make entitymanager specific
        return this.configurationManager.getConfiguration().getPersonRegisterPullCronSchedule();
    }

    public FtpCommunicator getFtpCommunicator(Session session, URI eventInterface, EntityManager cprEntityManager) throws DataStreamException {
        CprConfiguration configuration = this.configurationManager.getConfiguration();
        try {
            return new DatabaseProgressFtpCommunicator(
                    session,
                    cprEntityManager.getSchema(),
                    configuration.getRegisterFtpUsername(cprEntityManager),
                    configuration.getRegisterFtpPassword(cprEntityManager),
                    eventInterface != null && "ftps".equals(eventInterface.getScheme()),
                    this.proxyString,
                    this.localCopyFolder,
                    true,
                    false,
                    false
            );
        } catch (GeneralSecurityException | IOException e) {
            throw new DataStreamException(e);
        }
    }

    public void setProxyString(String proxyString) {
        this.proxyString = proxyString;
    }

    /**
     * Pull data from the data source denoted by eventInterface, using the
     * mechanism appropriate for the source.
     * For CPR, this is done using a LocalCopyFtpCommunicator, where we fetch all
     * files in a remote folder (known to be text files).
     * We then package chunks of lines into Events, and feed them into a stream for
     * returning.
     */
    @Override
    public ImportInputStream pullRawData(URI eventInterface, EntityManager entityManager, ImportMetadata importMetadata) throws DataFordelerException {
        if (!(entityManager instanceof CprRecordEntityManager) && !(entityManager instanceof CprGeoEntityManager)) {
            throw new WrongSubclassException(CprRecordEntityManager.class, entityManager);
        }
        if (eventInterface == null) {
            this.log.info("Not pulling for " + entityManager);
            return null;
        }
        this.log.info("Pulling from " + eventInterface + " for entitymanager " + entityManager);
        ImportInputStream responseBody = null;
        String scheme = eventInterface.getScheme();
        this.log.info("scheme: " + scheme);
        this.log.info("eventInterface: " + eventInterface);
        switch (scheme) {
            case "file":
                try {
                    File file = new File(eventInterface);
                    responseBody = new ImportInputStream(new FileInputStream(file));
                    responseBody.addCacheFile(file);
                } catch (FileNotFoundException e) {
                    this.log.error(e);
                    throw new DataStreamException(e);
                }
                break;

            case "ftp":
            case "ftps":
                try (Session session = sessionManager.getSessionFactory().openSession()) {
                    FtpCommunicator ftpFetcher = this.getFtpCommunicator(session, eventInterface, entityManager);
                    if (
                            importMetadata != null &&
                                    importMetadata.getImportConfiguration() != null &&
                                    importMetadata.getImportConfiguration().size() > 0 &&
                                    (!importMetadata.getImportConfiguration().has("remote") || !importMetadata.getImportConfiguration().get("remote").booleanValue())
                    ) {
                        responseBody = ftpFetcher.fetchLocal();
                    } else {
                        responseBody = ftpFetcher.fetch(eventInterface);
                    }
                } catch (DataStreamException e) {
                    this.log.error(e);
                    throw e;
                }
                break;
        }
        if (responseBody == null) {
            throw new DataStreamException("No data received from source " + eventInterface);
        }

        return responseBody;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void loadFileCacheToDatabase() {
        try (Session session = sessionManager.getSessionFactory().openSession()) {
            EntityManager entityManager = this.entityManagerBySchema.get(PersonEntity.schema);
            DatabaseProgressFtpCommunicator ftpFetcher = (DatabaseProgressFtpCommunicator) this.getFtpCommunicator(session, null, entityManager);
            ftpFetcher.insertFromFolder();

            CprConfiguration configuration = this.configurationManager.getConfiguration();

            ftpFetcher.listDownloadable(configuration.getPersonRegisterURI());
        } catch (DataStreamException | ConfigurationException e) {
            // Do nothing
        }
    }

    @Override
    protected ItemInputStream<? extends PluginSourceData> parseEventResponse(InputStream rawData, EntityManager entityManager) throws DataFordelerException {
        if (!(entityManager instanceof CprRecordEntityManager)) {
            throw new WrongSubclassException(CprRecordEntityManager.class, entityManager);
        }

        final int linesPerEvent = 1000;
        PipedInputStream inputStream = new PipedInputStream();

        try {
            final PipedOutputStream outputStream = new PipedOutputStream(inputStream);
            final ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream);
            final int dataIdBase = rawData.hashCode();
            final String schema = entityManager.getSchema();

            Thread t = new Thread(new Runnable() {
                @Override
                public void run() {
                    BufferedReader responseReader = new BufferedReader(new InputStreamReader(rawData, StandardCharsets.ISO_8859_1));
                    int eventCount = 0;
                    long totalLines = 0;
                    try {
                        String line;
                        int lineCount = 0;
                        ArrayList<String> lines = new ArrayList<>();
                        while ((line = responseReader.readLine()) != null) {
                            lines.add(line);
                            lineCount++;
                            if (lineCount >= linesPerEvent) {
                                objectOutputStream.writeObject(CprRegisterManager.this.wrap(lines, schema, dataIdBase, eventCount));
                                lines.clear();
                                lineCount = 0;
                                eventCount++;
                                totalLines++;
                            }
                        }
                        if (lineCount > 0) {
                            objectOutputStream.writeObject(CprRegisterManager.this.wrap(lines, schema, dataIdBase, eventCount));
                            eventCount++;
                        }
                        CprRegisterManager.this.log.info("Packed " + eventCount + " data objects with " + totalLines + " lines");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    try {
                        responseReader.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    try {
                        objectOutputStream.close();
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                }
            });
            t.start();

            return new ItemInputStream<>(inputStream);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private CprSourceData wrap(List<String> lines, String schema, int base, int index) {
        StringJoiner s = new StringJoiner("\n");
        for (String line : lines) {
            s.add(line);
        }
        return new CprSourceData(schema, s.toString(), base + ":" + index);
    }

}
