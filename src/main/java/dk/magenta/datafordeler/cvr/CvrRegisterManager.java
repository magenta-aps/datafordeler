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
import dk.magenta.datafordeler.cvr.synchronization.CvrSourceData;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

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

    private Logger log = LogManager.getLogger("CvrRegisterManager");

    public CvrRegisterManager() {

    }

    /**
    * RegisterManager initialization; set up configuration, source fetcher and source url
    */
    @PostConstruct
    public void init() {
        this.commonFetcher = new ScanScrollCommunicator();
        this.commonFetcher.setScrollIdJsonKey("_scroll_id");

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


    @Override
    protected Communicator getChecksumFetcher() {
        return this.getEventFetcher();
    }

    @Override
    public URI getListChecksumInterface(String schema, OffsetDateTime from) {
        return null;
    }

    public String getPullCronSchedule() {
        return this.configurationManager.getConfiguration().getPullCronSchedule();
    }

    @Override
    public boolean pullsEventsCommonly() {
        return false;
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
        CvrConfiguration.RegisterType registerType = configuration.getRegisterType(schema);
        if (registerType == null) {
            registerType = CvrConfiguration.RegisterType.DISABLED;
        }
        switch (registerType) {
            case DISABLED:
                break;
            case REMOTE_HTTP:
                final ArrayList<Throwable> errors = new ArrayList<>();
                InputStream responseBody;
                File cacheFile = new File("local/cvr/" + entityManager.getSchema() + "_" + LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE));
                try {
                    if (!cacheFile.exists()) {
                        log.info("Cache file "+cacheFile.getAbsolutePath()+" doesn't exist. Creating new and filling from source");
                        if (lastUpdateTime == null) {
                            lastUpdateTime = OffsetDateTime.parse("0000-01-01T00:00:00Z");
                            log.info("Last update time not found");
                        } else {
                            log.info("Last update time: "+lastUpdateTime.format(DateTimeFormatter.ISO_LOCAL_DATE));
                        }
                        requestBody = String.format(
                                configuration.getQuery(schema),
                                lastUpdateTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
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
                        log.info("Cache file "+cacheFile.getAbsolutePath()+" already exists.");
                    }
                } catch (URISyntaxException e) {
                    throw new ConfigurationException("Invalid pull URI '"+e.getInput()+"'");
                } catch (IOException e) {
                    throw new DataStreamException(e);
                }

                if (!errors.isEmpty()) {
                    throw new ParseException("Error while loading data for "+entityManager.getSchema(), errors.get(0));
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

            //System.out.println("There are "+entityManagerParseStreams.size()+" streams");
            return new ItemInputStream<>(inputStream);
        } catch (IOException e) {
            throw new DataStreamException(e);
        }
    }
}
