package dk.magenta.datafordeler.plugindemo;

import com.fasterxml.jackson.databind.ObjectMapper;
import dk.magenta.datafordeler.core.Application;
import dk.magenta.datafordeler.core.PluginManager;
import dk.magenta.datafordeler.core.database.SessionManager;
import dk.magenta.datafordeler.core.exception.DataFordelerException;
import dk.magenta.datafordeler.core.io.Event;
import dk.magenta.datafordeler.core.io.ImportMetadata;
import dk.magenta.datafordeler.core.io.PluginSourceData;
import dk.magenta.datafordeler.core.plugin.*;
import dk.magenta.datafordeler.core.util.ItemInputStream;
import dk.magenta.datafordeler.plugindemo.configuration.DemoConfigurationManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.hibernate.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.UUID;

@Component
public class DemoRegisterManager extends RegisterManager {

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private DemoConfigurationManager configurationManager;

    private DemoPlugin plugin;

    @Autowired
    private SessionManager sessionManager;

    private HttpCommunicator commonFetcher;

    private static Logger log = LogManager.getLogger(DemoRegisterManager.class.getCanonicalName());

    private int port;

    private UUID id;

    public DemoRegisterManager() {
        this.id = UUID.randomUUID();
        this.commonFetcher = new HttpCommunicator();
        this.port = Application.servicePort;
        instances.add(this);
    }

    private static ArrayList<DemoRegisterManager> instances = new ArrayList();

    @Override
    protected Logger getLog() {
        return this.log;
    }

    @Override
    public Plugin getPlugin() {
        return this.plugin;
    }

    public void setPlugin(DemoPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public SessionManager getSessionManager() {
        return this.sessionManager;
    }



    public static void setPortOnAll(int port) {
        for (DemoRegisterManager instance : instances) {
            instance.setPort(port);
        }
    }

    public void setPort(int port) {
        this.port = port;
        PluginManager pluginManager = this.plugin.getPluginManager();

        for (EntityManager entityManager : this.entityManagers) {
            if (entityManager instanceof DemoEntityManager) {
                DemoEntityManager demoEntityManager = (DemoEntityManager) entityManager;
                Collection<String> oldSubstrings = new ArrayList<>(demoEntityManager.getHandledURISubstrings());
                demoEntityManager.setPort(port);
                Collection<String> newSubstrings = demoEntityManager.getHandledURISubstrings();
                for (String oldSubstring : oldSubstrings) {
                    this.entityManagerByURISubstring.remove(oldSubstring);
                    pluginManager.removePluginURISubstring(this.plugin, oldSubstring);
                }
                for (String newSubstring : newSubstrings) {
                    this.entityManagerByURISubstring.put(newSubstring, demoEntityManager);
                    pluginManager.addPluginURISubstring(this.plugin, newSubstring);
                }
            }
        }
    }

    @Override
    public URI getBaseEndpoint() {
        try {
            return new URI("http", null, "localhost", this.port, "/test", null, null);
        } catch (URISyntaxException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    protected Communicator getEventFetcher() {
        return this.commonFetcher;
    }

    @Override
    protected ObjectMapper getObjectMapper() {
        return this.objectMapper;
    }

    /** Event fetching **/

    @Override
    public URI getEventInterface(EntityManager entityManager) {
        return expandBaseURI(this.getBaseEndpoint(), "/getNewEvents");
    }

    public ItemInputStream<? extends PluginSourceData> pullEvents(ImportMetadata importMetadata) throws DataFordelerException {
        return this.pullEvents(this.getEventInterface(null), null, importMetadata);
    }

    @Override
    protected ItemInputStream<? extends PluginSourceData> parseEventResponse(InputStream responseContent, EntityManager entityManager) throws DataFordelerException {
        return ItemInputStream.parseJsonStream(responseContent, Event.class, "events", this.getObjectMapper());
    }

    public String getPullCronSchedule() {
        return this.configurationManager.getConfiguration().getPullCronSchedule();
    }

    @Override
    public void setLastUpdated(EntityManager entityManager, ImportMetadata importMetadata) {
        boolean inTransaction = importMetadata.isTransactionInProgress();
        Session session = importMetadata.getSession();
        if (!inTransaction) {
            session.beginTransaction();
            importMetadata.setTransactionInProgress(true);
        }
        if (entityManager == null) {
            for (EntityManager e : this.entityManagers) {
                e.setLastUpdated(session, importMetadata.getImportTime());
            }
        } else {
            entityManager.setLastUpdated(session, importMetadata.getImportTime());
        }

        if (!inTransaction) {
            session.getTransaction().commit();
            importMetadata.setTransactionInProgress(false);
        }
    }


    private InputStream printStream(InputStream input) {
        try {
            if (input.markSupported()) {
                input.mark(8192);
            }
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            IOUtils.copy(input, baos);
            byte[] bytes = baos.toByteArray();
            this.log.debug(new String(bytes, "utf-8"));
            if (input.markSupported()) {
                input.reset();
                return input;
            }
            return new ByteArrayInputStream(bytes);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return input;
    }
}
