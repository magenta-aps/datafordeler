package dk.magenta.datafordeler.cvr;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import dk.magenta.datafordeler.core.configuration.ConfigurationManager;
import dk.magenta.datafordeler.core.database.SessionManager;
import dk.magenta.datafordeler.core.exception.*;
import dk.magenta.datafordeler.core.io.ImportInputStream;
import dk.magenta.datafordeler.core.io.ImportMetadata;
import dk.magenta.datafordeler.core.io.PluginSourceData;
import dk.magenta.datafordeler.core.plugin.*;
import dk.magenta.datafordeler.core.util.ItemInputStream;
import dk.magenta.datafordeler.cvr.configuration.CvrConfiguration;
import dk.magenta.datafordeler.cvr.configuration.CvrConfigurationManager;
import dk.magenta.datafordeler.cvr.entitymanager.CompanyEntityManager;
import dk.magenta.datafordeler.cvr.entitymanager.CvrEntityManager;
import dk.magenta.datafordeler.cvr.records.CompanyRecord;
import dk.magenta.datafordeler.cvr.records.CompanySubscription;
import dk.magenta.datafordeler.cvr.records.CompanyUnitRecord;
import dk.magenta.datafordeler.cvr.records.ParticipantRecord;
import dk.magenta.datafordeler.cvr.synchronization.CvrSourceData;
import jakarta.annotation.PostConstruct;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.query.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
public class CvrRegisterManager extends RegisterManager {

    private ScanScrollCommunicator commonFetcher;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CvrConfigurationManager configurationManager;

    private CvrPlugin plugin;

    @Autowired
    private SessionManager sessionManager;

    private String cvrDemoCompanyFile;

    private String cvrDemoUnitFile;

    private String cvrDemoParticipantFile;

    private String localCopyFolder;


    private final Logger log = LogManager.getLogger(CvrRegisterManager.class.getCanonicalName());

    public CvrRegisterManager() {
    }

    public void setPlugin(CvrPlugin plugin) throws IOException {
        this.plugin = plugin;
        CvrConfiguration configuration = this.configurationManager.getConfiguration();
        this.localCopyFolder = this.ensureLocalCopyFolder(configuration.getLocalCopyFolder());
    }

    public void setCvrDemoCompanyFile(String cvrDemoCompanyFile) {
        this.cvrDemoCompanyFile = cvrDemoCompanyFile;
    }

    public void setCvrDemoUnitFile(String cvrDemoUnitFile) {
        this.cvrDemoUnitFile = cvrDemoUnitFile;
    }

    public void setCvrDemoParticipantFile(String cvrDemoParticipantFile) {
        this.cvrDemoParticipantFile = cvrDemoParticipantFile;
    }

    /**
     * RegisterManager initialization; set up configuration, source fetcher and source url
     */
    @PostConstruct
    public void init() throws IOException {
        CvrConfiguration configuration = this.configurationManager.getConfiguration();
        this.commonFetcher = new ScanScrollCommunicator();
        this.commonFetcher.setScrollIdJsonKey("_scroll_id");
        this.cvrDemoCompanyFile = configuration.getCvrDemoCompanyFile();
        this.cvrDemoUnitFile = configuration.getCvrDemoUnitFile();
        this.cvrDemoParticipantFile = configuration.getCvrDemoParticipantFile();
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
    public Communicator getEventFetcher() {
        return this.commonFetcher;
    }

    @Override
    protected ObjectMapper getObjectMapper() {
        return this.objectMapper;
    }

    public String getPullCronSchedule() {
        return this.configurationManager.getConfiguration().getPullCronSchedule();
    }


    @Override
    public URI getEventInterface(EntityManager entityManager) {
        try {
            return new URI(this.getConfigurationManager().getConfiguration().getStartAddress(entityManager.getSchema()));
        } catch (URISyntaxException e) {
            e.printStackTrace();
            return null;
        }
    }

    public ConfigurationManager<CvrConfiguration> getConfigurationManager() {
        return this.configurationManager;
    }

    /**
     * Pull data from the data source denoted by eventInterface, using the
     * mechanism appropriate for the source.
     * For CVR, this is done using a ScanScrollCommunicator, where we specify the
     * query in a POST, then get a handle back that we can use in a series of
     * subsequent GET requests to get all the data.
     * We then package each response in an Event, and feed them into a stream for
     * returning.
     */
    @Override
    public InputStream pullRawData(URI eventInterface, EntityManager entityManager, ImportMetadata importMetadata) throws DataFordelerException {
        return this.pullRawData(eventInterface, entityManager, importMetadata, null);
    }

    public InputStream pullRawData(URI eventInterface, EntityManager entityManager, ImportMetadata importMetadata, CvrConfiguration.RegisterType registerType) throws DataFordelerException {
        if (!(entityManager instanceof CvrEntityManager)) {
            throw new WrongSubclassException(CvrEntityManager.class, entityManager);
        }
        CvrEntityManager cvrEntityManager = (CvrEntityManager) entityManager;
        String schema = entityManager.getSchema();

        ScanScrollCommunicator eventCommunicator = (ScanScrollCommunicator) this.getEventFetcher();
        eventCommunicator.setThrottle(0);


        Session session = this.sessionManager.getSessionFactory().openSession();
        OffsetDateTime lastUpdateTime = entityManager.getLastUpdated(session);
        session.close();

        CvrConfiguration configuration = this.configurationManager.getConfiguration();
        if (registerType == null) {
            registerType = configuration.getRegisterType(schema);
            if (registerType == null) {
                registerType = CvrConfiguration.RegisterType.DISABLED;
            }
        }
        switch (registerType) {
            case DISABLED:
                break;
            case LOCAL_FILE:
                try {
                    URI uri = null;
                    switch (schema) {
                        case "virksomhed":
                            uri = new URI(cvrDemoCompanyFile);
                            break;
                        case "produktionsenhed":
                            uri = new URI(cvrDemoUnitFile);
                            break;
                        case "deltager":
                            uri = new URI(cvrDemoParticipantFile);
                            break;
                    }
                    if (uri != null) {
                        String content = "";
                        if (uri.getScheme().equals("file")) {
                            File demoCompanyFile = new File(uri);
                            content = new String(new FileInputStream(demoCompanyFile).readAllBytes());
                        } else if (uri.getScheme().equals("classpath")) {
                            content = new String(new ClassPathResource(uri.getPath()).getInputStream().readAllBytes());
                        }
                        String noLineContent = content.replace("\n", "").replace("\r", "");
                        InputStream stream = new ByteArrayInputStream(noLineContent.getBytes(StandardCharsets.UTF_8));
                        return new ImportInputStream(stream);
                    }
                } catch (Exception e) {
                    log.error("Failed loading demodata", e);
                }
                break;
            case ALL_LOCAL_FILES:
                File cacheFolder = new File(this.localCopyFolder);
                if (cacheFolder.isDirectory()) {
                    File[] files = cacheFolder.listFiles((dir, name) -> name.startsWith(schema+"_"));
                    if (files != null) {
                        log.info("Loading data from "+files.length+" local files");
                        Arrays.sort(files);
                        ByteArrayOutputStream baos = new ByteArrayOutputStream();

                        for (File file : files) {
                            try (FileInputStream fileInputStream = new FileInputStream(file)) {
                                String content = new String(fileInputStream.readAllBytes());
                                baos.write(content.getBytes(StandardCharsets.UTF_8));
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                        }
                        ImportInputStream stream = new ImportInputStream(new ByteArrayInputStream(baos.toByteArray()));
                        for (File file : files) {
                            stream.addCacheFile(file);
                        }
                        return stream;
                    }
                }
            case REMOTE_HTTP:
                final ArrayList<Throwable> errors = new ArrayList<>();
                InputStream responseBody;
                File cacheFile;
                String requestBody = null;

                try (Session missingCompanySession = this.sessionManager.getSessionFactory().openSession()) {
                    List<Integer> specificCvrs = this.specificCvrs(importMetadata);
                    List<Integer> specificPnrs = this.specificPnrs(importMetadata);
                    List<Integer> specificUnitNrs = this.specificUnitNrs(importMetadata);

                    if (specificCvrs != null) {
                        if (schema.equals(CompanyRecord.schema)) {
                            cacheFile = new File(this.localCopyFolder, schema + "_" + LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
                            requestBody = cvrEntityManager.getSpecificQuery(specificCvrs);
                        } else {
                            return null;
                        }
                    } else if (specificPnrs != null) {
                        if (schema.equals(CompanyUnitRecord.schema)) {
                            cacheFile = new File(this.localCopyFolder, schema + "_" + LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
                            requestBody = cvrEntityManager.getSpecificQuery(specificPnrs);
                        } else {
                            return null;
                        }
                    } else if (specificUnitNrs != null) {
                        if (schema.equals(ParticipantRecord.schema)) {
                            cacheFile = new File(this.localCopyFolder, schema + "_" + LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
                            requestBody = cvrEntityManager.getSpecificQuery(specificUnitNrs);
                        } else {
                            return null;
                        }

                    } else {
                        cacheFile = new File(this.localCopyFolder, schema + "_" + LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE));
                        if (!cacheFile.exists()) {
                            log.info("Cache file " + cacheFile.getAbsolutePath() + " doesn't exist. Creating new and filling from source");
                            if (lastUpdateTime == null) {
                                lastUpdateTime = OffsetDateTime.parse("0000-01-01T00:00:00Z");
                                log.info("Last update time not found");
                            } else {
                                log.info("Last update time: " + lastUpdateTime.format(DateTimeFormatter.ISO_LOCAL_DATE));
                            }

                            // TODO: Erstat med getDailyQuery
                            CriteriaBuilder subscriptionBuilder = missingCompanySession.getCriteriaBuilder();
                            CriteriaQuery<CompanySubscription> allCompanySubscription = subscriptionBuilder.createQuery(CompanySubscription.class);
                            allCompanySubscription.from(CompanySubscription.class);
                            List<Integer> subscribedCompanyList = missingCompanySession.createQuery(allCompanySubscription).getResultList().stream().map(s -> s.getCvrNumber()).sorted().collect(Collectors.toList());

                            Query<Integer> query = missingCompanySession.createQuery("select " + CompanyRecord.DB_FIELD_CVR_NUMBER + " from " + CompanyRecord.class.getCanonicalName(), Integer.class);
                            HashSet<Integer> missingCompanyList = new HashSet<>(subscribedCompanyList);
                            missingCompanyList.removeAll(new HashSet<>(query.list()));

                            // TODO: use this instead
                            // requestBody = cvrEntityManager.getDailyQuery(missingCompanySession, lastUpdateTime);

                            requestBody = String.format(
                                    configuration.getQuery(schema),
                                    lastUpdateTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                                    subscribedCompanyList,
                                    lastUpdateTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                                    new ArrayList<>(missingCompanyList)
                            );
                        } else {
                            log.info("Cache file " + cacheFile.getAbsolutePath() + " already exists.");
                        }
                    }
                    if (!cacheFile.exists()) {
                        log.info("Fetching CVR data for schema "+schema+" with username "+configuration.getUsername(schema)+" and request body "+requestBody);
                        eventCommunicator.setUsername(configuration.getUsername(schema));
                        eventCommunicator.setPassword(configuration.getPassword(schema));

                        eventCommunicator.setUncaughtExceptionHandler((t, e) -> errors.add(e));
                        responseBody = eventCommunicator.fetch(
                                new URI(configuration.getStartAddress(schema)),
                                new URI(configuration.getScrollAddress(schema)),
                                requestBody
                        );

                        cacheFile.createNewFile();
                        FileWriter fileWriter = new FileWriter(cacheFile);
                        IOUtils.copy(responseBody, fileWriter);
                        fileWriter.close();
                        eventCommunicator.wait(responseBody);
                        responseBody.close();
                        log.info("Loaded into cache file");
                    }

                } catch (URISyntaxException e) {
                    throw new ConfigurationException("Invalid pull URI '" + e.getInput() + "'");
                } catch (IOException e) {
                    throw new DataStreamException(e);
                } catch (GeneralSecurityException e) {
                    throw new ConfigurationException("Failed password decryption", e);
                }

                if (!errors.isEmpty()) {
                    throw new ParseException("Error while loading data for " + entityManager.getSchema(), errors.get(0));
                }

                try {
                    return new ImportInputStream(new FileInputStream(cacheFile), cacheFile);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
        }
        return null;
    }

    @Override
    protected ItemInputStream<? extends PluginSourceData> parseEventResponse(final InputStream responseBody, EntityManager entityManager) throws DataFordelerException {
        PipedInputStream inputStream = new PipedInputStream();
        final PipedOutputStream outputStream;
        final String charsetName = "UTF-8";
        try {
            outputStream = new PipedOutputStream(inputStream);
            final ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream);

            Thread t = new Thread(new Runnable() {
                @Override
                public void run() {
                    if (responseBody != null) {
                        final int dataBaseId = responseBody.hashCode();
                        BufferedReader responseReader = null;
                        int count = 0;
                        try {
                            responseReader = new BufferedReader(new InputStreamReader(responseBody, charsetName));
                            String line;
                            while ((line = responseReader.readLine()) != null) {
                                objectOutputStream.writeObject(new CvrSourceData(entityManager.getSchema(), line, dataBaseId + ":" + count));
                                count++;
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        } finally {
                            log.info("Wrote " + count + " events");
                            try {
                                if (responseReader != null) {
                                    responseReader.close();
                                }
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            try {
                                responseBody.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
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
            throw new DataStreamException(e);
        }
    }

    private List<Integer> specificCvrs(ImportMetadata importMetadata) {
        return this.specificIds(importMetadata, "cvr");
    }
    private List<Integer> specificPnrs(ImportMetadata importMetadata) {
        return this.specificIds(importMetadata, "pnr");
    }
    private List<Integer> specificUnitNrs(ImportMetadata importMetadata) {
        return this.specificIds(importMetadata, "unitNr");
    }

    private List<Integer> specificIds(ImportMetadata importMetadata, String key) {
        ObjectNode importConfiguration = importMetadata.getImportConfiguration();
        List<Integer> specificCvrs = new ArrayList<>();
        if (importConfiguration != null) {
            JsonNode cvrNodes = importConfiguration.get(key);
            if (cvrNodes != null) {
                if (cvrNodes.isInt()) {
                    specificCvrs.add(cvrNodes.asInt());
                } else if (cvrNodes.isArray()) {
                    for (JsonNode cvrNode : cvrNodes) {
                        specificCvrs.add(cvrNode.asInt());
                    }
                }
                return specificCvrs;
            }
        }
        return null;
    }

}
