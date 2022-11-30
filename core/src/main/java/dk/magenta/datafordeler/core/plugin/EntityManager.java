package dk.magenta.datafordeler.core.plugin;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import dk.magenta.datafordeler.core.database.IdentifiedEntity;
import dk.magenta.datafordeler.core.database.LastUpdated;
import dk.magenta.datafordeler.core.database.QueryManager;
import dk.magenta.datafordeler.core.exception.DataFordelerException;
import dk.magenta.datafordeler.core.fapi.BaseQuery;
import dk.magenta.datafordeler.core.fapi.FapiBaseService;
import dk.magenta.datafordeler.core.fapi.OutputWrapper;
import dk.magenta.datafordeler.core.io.ImportMetadata;
import dk.magenta.datafordeler.core.io.PluginSourceData;
import org.apache.logging.log4j.Logger;
import org.hibernate.Session;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.HashMap;

/**
 * Entity (and associates) specific manager. Subclass in plugins
 * A plugin can have any number of Entity classes, each needing their own way of handling.
 * An EntityManager basically specifies how to parse raw input data into the bitemporal data
 * structure under an Entity, where to get the input data, and how and where to send receipts.
 */
public abstract class EntityManager {
    private RegisterManager registerManager;
    protected Class<? extends IdentifiedEntity> managedEntityClass;


    public Class<? extends IdentifiedEntity> getManagedEntityClass() {
        return this.managedEntityClass;
    }

    public abstract Collection<String> getHandledURISubstrings();

    /**
     * Plugins must return an autowired ObjectMapper instance from this method
     *
     * @return
     */
    protected abstract ObjectMapper getObjectMapper();

    /**
     * Plugins must return a Fetcher instance from this method
     *
     * @return
     */
    protected abstract Communicator getRegistrationFetcher();

    /**
     * Plugins must return an instance of a FapiService subclass from this method
     *
     * @return
     */
    public abstract FapiBaseService getEntityService();


    public abstract URI getBaseEndpoint();

    public RegisterManager getRegisterManager() {
        return this.registerManager;
    }

    public void setRegisterManager(RegisterManager registerManager) {
        this.registerManager = registerManager;
    }

    public abstract String getSchema();

    /** Registration parsing **/

    /**
     * Parse incoming data into a Registration (data coming from within a request envelope)
     *
     * @param registrationData
     * @throws IOException
     */
    public void parseData(InputStream registrationData, ImportMetadata importMetadata) throws DataFordelerException {
    }

    /**
     * Parse incoming data into a Registration (data coming from within a request envelope)
     *
     * @param registrationData
     * @throws IOException
     */
    public void parseData(PluginSourceData registrationData, ImportMetadata importMetadata) throws DataFordelerException {
    }


    public boolean handlesOwnSaves() {
        return false;
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
        return RegisterManager.expandBaseURI(base, path);
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
        return RegisterManager.expandBaseURI(base, path, query, fragment);
    }

    protected abstract Logger getLog();


    private LastUpdated getLastUpdatedObject(Session session) {
        HashMap<String, Object> filter = new HashMap<>();
        filter.put(LastUpdated.DB_FIELD_PLUGIN, this.registerManager.getPlugin().getName());
        filter.put(LastUpdated.DB_FIELD_SCHEMA_NAME, this.getSchema());
        return QueryManager.getItem(session, LastUpdated.class, filter);
    }

    public OffsetDateTime getLastUpdated(Session session) {
        LastUpdated lastUpdated = this.getLastUpdatedObject(session);
        if (lastUpdated != null) {
            return lastUpdated.getTimestamp();
        }
        return null;
    }


    public void setLastUpdated(Session session, OffsetDateTime time) {
        LastUpdated lastUpdated = this.getLastUpdatedObject(session);
        if (lastUpdated == null) {
            lastUpdated = new LastUpdated();
            lastUpdated.setPlugin(this.registerManager.getPlugin().getName());
            lastUpdated.setSchemaName(this.getSchema());
        }
        lastUpdated.setTimestamp(time);
        session.saveOrUpdate(lastUpdated);
    }

    // Override as needed in subclasses
    public boolean shouldSkipLastUpdate(ImportMetadata importMetadata) {
        ObjectNode importConfiguration = importMetadata.getImportConfiguration();
        if (importConfiguration == null || importConfiguration.size() == 0) {
            return false;
        } else return importConfiguration.has("skipLastUpdate");
    }

    /**
     * Should return whether the configuration is set so that pulls are enabled for this entitymanager
     */
    public boolean pullEnabled() {
        return false;
    }

    public OutputWrapper getOutputWrapper() {
        return this.getEntityService().getOutputWrapper();
    }

    public abstract BaseQuery getQuery();

    public abstract BaseQuery getQuery(String... joined);
}
