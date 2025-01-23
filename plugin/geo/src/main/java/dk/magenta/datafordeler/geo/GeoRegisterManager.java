package dk.magenta.datafordeler.geo;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import dk.magenta.datafordeler.core.database.QueryManager;
import dk.magenta.datafordeler.core.database.SessionManager;
import dk.magenta.datafordeler.core.exception.*;
import dk.magenta.datafordeler.core.io.ImportInputStream;
import dk.magenta.datafordeler.core.io.ImportMetadata;
import dk.magenta.datafordeler.core.io.PluginSourceData;
import dk.magenta.datafordeler.core.plugin.*;
import dk.magenta.datafordeler.core.util.InputStreamReader;
import dk.magenta.datafordeler.core.util.ItemInputStream;
import dk.magenta.datafordeler.geo.configuration.GeoConfiguration;
import dk.magenta.datafordeler.geo.configuration.GeoConfigurationManager;
import dk.magenta.datafordeler.geo.data.GeoEntityManager;
import jakarta.annotation.PostConstruct;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.*;
import java.net.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Component
public class GeoRegisterManager extends RegisterManager {

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private GeoConfigurationManager configurationManager;

    private GeoPlugin plugin;

    @Autowired
    private SessionManager sessionManager;

    private final Logger log = LogManager.getLogger(GeoRegisterManager.class.getCanonicalName());

    @Value("${dafo.geo.proxy-url:}")
    private String proxyString;

    private String localCopyFolder;

    public GeoRegisterManager() {

    }

    /**
     * RegisterManager initialization; set up dk.magenta.datafordeler.geo.configuration and source fetcher.
     * We store fetched data in a local cache, so create a random folder for that.
     */
    @PostConstruct
    public void init() throws IOException {
    }

    public GeoConfigurationManager getConfigurationManager() {
        return this.configurationManager;
    }

    @Override
    protected Logger getLog() {
        return this.log;
    }

    public void setPlugin(GeoPlugin plugin) throws IOException {
        this.plugin = plugin;
        GeoConfiguration configuration = configurationManager.getConfiguration();
        this.localCopyFolder = this.ensureLocalCopyFolder(configuration.getLocalCopyFolder());
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
        Session session = this.sessionManager.getSessionFactory().openSession();
        OffsetDateTime lastUpdateTime = entityManager.getLastUpdated(session);
        session.close();

        if (lastUpdateTime == null) {
            lastUpdateTime = OffsetDateTime.parse("1900-01-01T00:00:00Z");
            log.info("Last update time not found");
        } else {
            log.info("Last update time: " + lastUpdateTime.format(DateTimeFormatter.ISO_LOCAL_DATE));
        }

        String address = this.configurationManager.getConfiguration().getURL(entityManager.getSchema());
        if (address == null || address.isEmpty()) {
            throw new ConfigurationException("Invalid URL for schema " + entityManager.getSchema() + ": " + address);
        }
        address = address.replace("%{editDate}", lastUpdateTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));

        try {
            URL url = new URL(address);
            return new URI(url.getProtocol(), url.getUserInfo(), url.getHost(), url.getPort(), url.getPath(), url.getQuery(), url.getRef());
        } catch (URISyntaxException | MalformedURLException e) {
            throw new ConfigurationException("Invalid URL for schema " + entityManager.getSchema() + ": " + address, e);
        }
    }

    public URI getDeletionInterface(EntityManager entityManager) throws ConfigurationException {
        Session session = this.sessionManager.getSessionFactory().openSession();
        OffsetDateTime lastUpdateTime = entityManager.getLastUpdated(session);
        session.close();

        if (lastUpdateTime == null) {
            lastUpdateTime = OffsetDateTime.parse("1900-01-01T00:00:00Z");
            log.info("Last update time not found");
        } else {
            log.info("Last update time: " + lastUpdateTime.format(DateTimeFormatter.ISO_LOCAL_DATE));
        }

        String address = this.configurationManager.getConfiguration().getDeletionURL(entityManager.getSchema());
        if (address == null || address.isEmpty()) {
            throw new ConfigurationException("Invalid URL for schema " + entityManager.getSchema() + ": " + address);
        }
        address = address.replace("%{editDate}", lastUpdateTime.minusDays(30).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));

        try {
            URL url = new URL(address);
            return new URI(url.getProtocol(), url.getUserInfo(), url.getHost(), url.getPort(), url.getPath(), url.getQuery(), url.getRef());
        } catch (URISyntaxException | MalformedURLException e) {
            throw new ConfigurationException("Invalid URL for schema " + entityManager.getSchema() + ": " + address, e);
        }
    }

    @Override
    public void beforePull(EntityManager entityManager, ImportMetadata importMetadata) {
        // First delete items that are deleted on input server
        try {
            this.removeDeleted((GeoEntityManager) entityManager, importMetadata);
        } catch (DataFordelerException e) {
            e.printStackTrace();
        }
    }

    @Override
    public ImportInputStream pullRawData(URI eventInterface, EntityManager entityManager, ImportMetadata importMetadata) throws DataFordelerException {

        this.log.info("eventInterface: " + eventInterface);
        GeoConfiguration configuration = this.configurationManager.getConfiguration();
        GeoConfiguration.RegisterType registerType = configuration.getRegisterType(entityManager.getSchema());
        if (registerType == null) {
            registerType = GeoConfiguration.RegisterType.DISABLED;
        }
        switch (registerType) {
            case LOCAL_FILE:
                try {
                    File file = new File(eventInterface);
                    ImportInputStream response = new ImportInputStream(new FileInputStream(file));
                    response.addCacheFile(file);
                    return response;
                } catch (FileNotFoundException e) {
                    this.log.error(e);
                    throw new DataStreamException(e);
                }
            case REMOTE_HTTP:
                final ArrayList<Throwable> errors = new ArrayList<>();
                InputStream responseBody;
                HttpCommunicator communicator = new HttpCommunicator();

                Session session = this.sessionManager.getSessionFactory().openSession();
                session.close();
                LocalDateTime now = LocalDateTime.now();
                long s = 1000000000L * (3600L * now.getHour() + 60L * now.getMinute() + now.getSecond()) + now.getNano();
                File cacheFile = new File(this.localCopyFolder, entityManager.getSchema() + "_" + LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE) + "_" + s);
                Charset charset = this.getConfigurationManager().getConfiguration().getCharset();
                try {
                    if (!cacheFile.exists()) {
                        log.info("Cache file " + cacheFile.getAbsolutePath() + " doesn't exist. Creating new and filling from source");

                        cacheFile.createNewFile();

                        OutputStreamWriter fileWriter = new OutputStreamWriter(
                                new FileOutputStream(cacheFile),
                                charset.newEncoder()
                        );
                        try {
                            fileWriter.append("[\n");
                            int count = 1000;

                            String query = eventInterface.getQuery();
                            query = query.replace("%{count}", Integer.toString(count));
                            URL tokenUrl = new URL(configuration.getTokenService());
                            String token = this.getToken(tokenUrl, configuration.getUsername(), configuration.getPassword());
                            Map<String, String> headers = Collections.singletonMap("Authorization", "Bearer " + token);
                            for (int offset = 0; offset < 1000000000; offset += count) {
                                String offsetQuery = URLDecoder.decode(query.replace("%{offset}", Integer.toString(offset)), StandardCharsets.UTF_8);
                                eventInterface = new URI(eventInterface.getScheme(), eventInterface.getUserInfo(), eventInterface.getHost(), eventInterface.getPort(), eventInterface.getPath(), offsetQuery, eventInterface.getFragment());

                                responseBody = communicator.get(eventInterface, headers);
                                try {
                                    String data = InputStreamReader.readInputStream(responseBody, charset.name());
                                    long responseCount = GeoEntityManager.parseJsonStream(data, "features", this.objectMapper, null);
                                    if (responseCount == 0) {
                                        break;
                                    }
                                    if (offset > 0) {
                                        fileWriter.append(",\n");
                                    }
                                    fileWriter.append(data);
                                    if (responseCount < count) {
                                        break;
                                    }
                                } finally {
                                    responseBody.close();
                                }
                            }
                            log.info("Loaded into cache file");
                            fileWriter.append("\n]");
                            fileWriter.close();
                        } catch (Exception e) {
                            fileWriter.close();
                            cacheFile.delete();
                            throw e;
                        }
                    } else {
                        log.info("Cache file " + cacheFile.getAbsolutePath() + " exists.");
                    }
                } catch (URISyntaxException | IOException e) {
                    throw new DataStreamException(e);
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

    public void removeDeleted(GeoEntityManager entityManager, ImportMetadata importMetadata) throws DataFordelerException {
        try {
            ImportInputStream stream = this.pullRawData(this.getDeletionInterface(entityManager), entityManager, importMetadata);
            if (stream != null) {
                try {
                    entityManager.parseDeletionData(stream);
                } finally {
                    QueryManager.clearCaches();
                    try {
                        stream.close();
                    } catch (IOException e) {
                        throw new DataStreamException(e);
                    }
                }
            }
        } catch (ConfigurationException e) {
            log.error("Incorrect configuration for deletion at entity manager " + entityManager.getClass().getSimpleName());
        }
    }

    private String getToken(URL tokenUrl, String username, String password) throws DataStreamException {
        try {
            HashMap<String, String> parameters = new HashMap<>();
            parameters.put("username", username);
            parameters.put("password", password);
            parameters.put("f", "json");
            parameters.put("client", "referer");
            parameters.put("referer", "dafo");
            parameters.put("expiration", "3600");
            HttpCommunicator communicator = new HttpCommunicator();
            InputStream response = communicator.post(tokenUrl.toURI(), parameters, null);
            String responseString = InputStreamReader.readInputStream(response);
            ObjectNode responseJson = (ObjectNode) objectMapper.readTree(responseString);
            return responseJson.get("token").textValue();
        } catch (HttpStatusException | NullPointerException | URISyntaxException | IOException e) {
            throw new DataStreamException(e);
        }
    }

    @Override
    protected ItemInputStream<? extends PluginSourceData> parseEventResponse(InputStream rawData, EntityManager entityManager) {
        return null;
    }

    @Override
    public String getPullCronSchedule() {
        return this.configurationManager.getConfiguration().getPullCronSchedule();
    }

}
