package dk.magenta.datafordeler.cvr;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import dk.magenta.datafordeler.cvr.entitymanager.CvrEntityManager;
import dk.magenta.datafordeler.cvr.records.CompanyRecord;
import dk.magenta.datafordeler.cvr.records.CompanySubscription;
import dk.magenta.datafordeler.cvr.synchronization.CvrSourceData;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.query.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
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
import java.util.stream.Collectors;

@Component
public class CvrRegisterManager extends RegisterManager {

    private ScanScrollCommunicator commonFetcher;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CvrConfigurationManager configurationManager;

    @Autowired
    private CvrPlugin plugin;

    @Autowired
    private SessionManager sessionManager;

    private String cvrDemoCompanyFile;

    private String cvrDemoUnitFile;

    private String cvrDemoParticipantFile;


    private final Logger log = LogManager.getLogger(CvrRegisterManager.class.getCanonicalName());

    public CvrRegisterManager() {

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
    public void init() {
        this.commonFetcher = new ScanScrollCommunicator();
        this.commonFetcher.setScrollIdJsonKey("_scroll_id");
        this.cvrDemoCompanyFile = configurationManager.getConfiguration().getCvrDemoCompanyFile();
        this.cvrDemoUnitFile = configurationManager.getConfiguration().getCvrDemoUnitFile();
        this.cvrDemoParticipantFile = configurationManager.getConfiguration().getCvrDemoParticipantFile();
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
        String schema = entityManager.getSchema();
        ScanScrollCommunicator eventCommunicator = (ScanScrollCommunicator) this.getEventFetcher();
        eventCommunicator.setThrottle(0);

        String requestBody;

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
                        File demoCompanyFile = new File(uri);
                        FileInputStream demoCompanyFileInputStream = new FileInputStream(demoCompanyFile);
                        String content = new String(demoCompanyFileInputStream.readAllBytes());
                        String noLineContent = content.replace("\n", "").replace("\r", "");
                        InputStream stream = new ByteArrayInputStream(noLineContent.getBytes(StandardCharsets.UTF_8));
                        return new ImportInputStream(stream, demoCompanyFile);
                    }
                } catch (Exception e) {
                    log.error("Failed loading demodata", e);
                }
                break;
            case ALL_LOCAL_FILES:
                File cacheFolder = new File("local/cvr/");
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
                File cacheFile = new File("local/cvr/" + schema + "_" + LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE));
                try (Session missingCompanySession = this.sessionManager.getSessionFactory().openSession()) {
                    if (!cacheFile.exists()) {
                        log.info("Cache file " + cacheFile.getAbsolutePath() + " doesn't exist. Creating new and filling from source");
                        if (lastUpdateTime == null) {
                            lastUpdateTime = OffsetDateTime.parse("0000-01-01T00:00:00Z");
                            log.info("Last update time not found");
                        } else {
                            log.info("Last update time: " + lastUpdateTime.format(DateTimeFormatter.ISO_LOCAL_DATE));
                        }

                        CriteriaBuilder subscriptionBuilder = missingCompanySession.getCriteriaBuilder();
                        CriteriaQuery<CompanySubscription> allCompanySubscription = subscriptionBuilder.createQuery(CompanySubscription.class);
                        allCompanySubscription.from(CompanySubscription.class);
                        List<Integer> subscribedCompanyList = missingCompanySession.createQuery(allCompanySubscription).getResultList().stream().map(s -> s.getCvrNumber()).sorted().collect(Collectors.toList());

                        Query<Integer> query = missingCompanySession.createQuery("select "+CompanyRecord.DB_FIELD_CVR_NUMBER+" from "+CompanyRecord.class.getCanonicalName(), Integer.class);
                        HashSet<Integer> missingCompanyList = new HashSet<>(subscribedCompanyList);
                        missingCompanyList.removeAll(new HashSet<>(query.list()));

                        requestBody = String.format(
                                configuration.getQuery(schema),
                                lastUpdateTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                                subscribedCompanyList,
                                lastUpdateTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                                new ArrayList<>(missingCompanyList)
                        );

                        eventCommunicator.setUsername(configuration.getUsername(schema));
                        eventCommunicator.setPassword(configuration.getPassword(schema));

                        eventCommunicator.setUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
                            @Override
                            public void uncaughtException(Thread t, Throwable e) {
                                errors.add(e);
                            }
                        });
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
                    } else {
                        log.info("Cache file " + cacheFile.getAbsolutePath() + " already exists.");
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
}
