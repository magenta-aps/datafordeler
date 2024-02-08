package dk.magenta.datafordeler.core.plugin;

import com.fasterxml.jackson.databind.ObjectMapper;
import dk.magenta.datafordeler.core.database.SessionManager;
import dk.magenta.datafordeler.core.exception.DataFordelerException;
import dk.magenta.datafordeler.core.io.ImportInputStream;
import dk.magenta.datafordeler.core.io.ImportMetadata;
import dk.magenta.datafordeler.core.io.PluginSourceData;
import dk.magenta.datafordeler.core.util.ItemInputStream;
import dk.magenta.datafordeler.core.util.ListHashMap;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Session;
import org.springframework.web.util.UriUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.SequenceInputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

/**
 * Manager for a Plugin's connection to its data source, through a series of EntityManagers
 */
public abstract class RegisterManager {

    protected List<EntityManager> entityManagers;

    protected Map<String, EntityManager> entityManagerBySchema;

    protected Map<String, EntityManager> entityManagerByURISubstring;

    protected HashSet<String> handledSchemas;

    private static final Logger log = LogManager.getLogger(RegisterManager.class.getCanonicalName());

    public RegisterManager() {
        this.handledSchemas = new HashSet<>();
        this.entityManagers = new ArrayList<>();
        this.entityManagerBySchema = new HashMap<>();
        this.entityManagerByURISubstring = new HashMap<>();
    }

    protected abstract Logger getLog();

    public abstract Plugin getPlugin();

    public abstract SessionManager getSessionManager();


    public abstract URI getBaseEndpoint();

    /**
     * Plugins must return a Fetcher instance from this method
     *
     * @return
     */
    protected abstract Communicator getEventFetcher();

    /**
     * Plugins must return an autowired ObjectMapper instance from this method
     *
     * @return
     */
    protected abstract ObjectMapper getObjectMapper();

    public boolean handlesSchema(String schema) {
        return this.handledSchemas.contains(schema);
    }

    public List<EntityManager> getEntityManagers() {
        return this.entityManagers;
    }

    public EntityManager getEntityManager(String schema) {
        return this.entityManagerBySchema.get(schema);
    }

    public EntityManager getEntityManager(URI uri) {
        String uriString = uri.toString();
        for (String substring : this.entityManagerByURISubstring.keySet()) {
            if (uriString.startsWith(substring)) {
                return this.entityManagerByURISubstring.get(substring);
            }
        }
        return null;
    }

    public EntityManager getEntityManager(Class<?> entityClass) {
        for (EntityManager entityManager : this.entityManagers) {
            if (entityManager.getManagedEntityClass() == entityClass) {
                return entityManager;
            }
        }
        return null;
    }

    public void addEntityManager(EntityManager entityManager) {
        entityManager.setRegisterManager(this);
        String schema = entityManager.getSchema();
        this.handledSchemas.add(schema);
        this.entityManagers.add(entityManager);
        this.entityManagerBySchema.put(schema, entityManager);
        for (String substring : entityManager.getHandledURISubstrings()) {
            this.entityManagerByURISubstring.put(substring, entityManager);
        }
    }

    public Collection<String> getHandledURISubstrings() {
        return this.entityManagerByURISubstring.keySet();
    }


    /** Event fetching **/

    /**
     * Obtain remote URI from which data can be pulled, given an EntityManager
     *
     * @param entityManager
     * @return
     */
    public abstract URI getEventInterface(EntityManager entityManager) throws DataFordelerException;

    /**
     * General data pull. Will perform a pull for all EntityManagers registered under the RegisterManager.
     * Subclasses may override this, but should endeavour to avoid doing so, instead overriding
     * pullEvents(URI eventInterface, EntityManager entityManager) and/or parseEventResponse(InputStream responseContent, EntityManager entityManager)
     *
     * @return
     * @throws DataFordelerException
     */
    private ItemInputStream<? extends PluginSourceData> pullEvents(EntityManager entityManager, ImportMetadata importMetadata) throws DataFordelerException {
        return this.pullEvents(this.getEventInterface(entityManager), entityManager, importMetadata);
    }

    /**
     * Specific data pull. Performs a pull from the given URI, for the given EntityManager.
     * Subclasses are free to override this if needed; at present it simply fetches the URI with the
     * Communicator returned by this.getEventFetcher(), and parses the raw data through
     * parseEventResponse(InputStream responseContent, EntityManager entityManager)
     * which must be implemented in subclasses.
     *
     * @param eventInterface
     * @param entityManager
     * @return
     * @throws DataFordelerException
     */
    public ItemInputStream<? extends PluginSourceData> pullEvents(URI eventInterface, EntityManager entityManager, ImportMetadata importMetadata) throws DataFordelerException {
        return this.parseEventResponse(this.pullRawData(eventInterface, entityManager, importMetadata), entityManager);
    }

    public InputStream pullRawData(URI eventInterface, EntityManager entityManager, ImportMetadata importMetadata) throws DataFordelerException {
        this.getLog().info("Pulling events from " + eventInterface + ", for entityManager " + entityManager);
        Communicator eventCommunicator = this.getEventFetcher();
        return eventCommunicator.fetch(eventInterface);
    }

    public ImportInputStream getCacheStream(List<File> cacheFiles) throws IOException {
        InputStream inputStream = null;
        for (File file : cacheFiles) {
            Path filePath = Paths.get(file.toURI());
            InputStream newInputStream = Files.newInputStream(filePath);
            if (inputStream == null) {
                inputStream = newInputStream;
            } else {
                inputStream = new SequenceInputStream(inputStream, newInputStream);
            }
        }
        return new ImportInputStream(inputStream, cacheFiles);
    }

    public ItemInputStream<? extends PluginSourceData> pullEvents(ImportMetadata importMetadata) throws DataFordelerException {
        /*HashMap<EntityManager, ItemInputStream<? extends PluginSourceData>> streams = new HashMap<>();
        for (EntityManager entityManager : this.getEntityManagers()) {
            streams.put(entityManager, this.pullEvents(this.getEventInterface(entityManager), entityManager));
        }
        return streams;*/
        return null;
    }

    /**
     * Parses the raw inputstream from a data source into a stream of PluginSourceData objects, usually by wrapping the important parts.
     * The resultant objects should be consumable by
     *
     * @param responseContent
     * @param entityManager
     * @return
     * @throws DataFordelerException
     */
    protected abstract ItemInputStream<? extends PluginSourceData> parseEventResponse(InputStream responseContent, EntityManager entityManager) throws DataFordelerException;

    /**
     * Return a Cron expression telling when to perform a pull from the register
     * Subclasses that want to enable pull must override this and return a valid cron expression
     *
     * @return
     */
    public String getPullCronSchedule() {
        return null;
    }

    /**
     * Utility method to be used by subclasses
     *
     * @param base
     * @param path
     * @return Expanded URI, with scheme, host and port from the base, a custom path, and no query or fragment
     * @throws URISyntaxException
     */
    public static URI expandBaseURI(URI base, String path) {
        return expandBaseURI(base, path, null, null);
    }

    /**
     * Utility method to be used by subclasses
     *
     * @param base
     * @param path
     * @return Expanded URI, with scheme, host and port from the base, and a custom path query and fragment
     * @throws URISyntaxException
     */
    public static URI expandBaseURI(URI base, String path, String query, String fragment) {
        try {
            return new URI(base.getScheme(), base.getUserInfo(), base.getHost(), base.getPort(), base.getPath() + path, query, fragment);
        } catch (URISyntaxException e) {
            log.error("URLEXPANDFAIL", e);
            return null;
        }
    }

    public static String joinQueryString(ListHashMap<String, String> params) {
        StringJoiner sj = new StringJoiner("&");
        for (String key : params.keySet()) {
            ArrayList<String> values = params.get(key);
            if (values != null && !values.isEmpty()) {
                for (String value : values) {
                    sj.add(key + "=" + UriUtils.encodeQueryParam(value, StandardCharsets.UTF_8));
                }
            }
        }
        return sj.toString();
    }

    public void setLastUpdated(EntityManager entityManager, ImportMetadata importMetadata) {
        boolean inTransaction = importMetadata.isTransactionInProgress();
        Session session = importMetadata.getSession();
        if (!inTransaction) {
            session.beginTransaction();
            importMetadata.setTransactionInProgress(true);
        }
        entityManager.setLastUpdated(session, importMetadata.getImportTime());
        if (!inTransaction) {
            session.getTransaction().commit();
            importMetadata.setTransactionInProgress(false);
        }
    }

    public void beforePull(EntityManager entityManager, ImportMetadata importMetadata) {
        // Override in subclasses
    }
}
