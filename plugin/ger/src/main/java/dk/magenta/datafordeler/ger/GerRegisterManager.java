package dk.magenta.datafordeler.ger;

import com.fasterxml.jackson.databind.ObjectMapper;
import dk.magenta.datafordeler.core.database.setup.SessionManager;
import dk.magenta.datafordeler.core.exception.ConfigurationException;
import dk.magenta.datafordeler.core.exception.DataFordelerException;
import dk.magenta.datafordeler.core.exception.DataStreamException;
import dk.magenta.datafordeler.core.io.ImportInputStream;
import dk.magenta.datafordeler.core.io.ImportMetadata;
import dk.magenta.datafordeler.core.io.PluginSourceData;
import dk.magenta.datafordeler.core.plugin.Communicator;
import dk.magenta.datafordeler.core.plugin.EntityManager;
import dk.magenta.datafordeler.core.plugin.Plugin;
import dk.magenta.datafordeler.core.plugin.RegisterManager;
import dk.magenta.datafordeler.core.util.ItemInputStream;
import dk.magenta.datafordeler.ger.configuration.GerConfiguration;
import dk.magenta.datafordeler.ger.configuration.GerConfigurationManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

@Component
public class GerRegisterManager extends RegisterManager {

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private GerConfigurationManager configurationManager;

    @Autowired
    private GerPlugin plugin;

    @Autowired
    private SessionManager sessionManager;

    private final Logger log = LogManager.getLogger(GerRegisterManager.class.getCanonicalName());

    @Value("${dafo.ger.proxy-url:}")
    private String proxyString;

    @Value("${dafo.ger.local-copy-folder:}")
    private String localCopyFolder;

    public GerRegisterManager() {

    }

    /**
     * RegisterManager initialization; set up dk.magenta.datafordeler.ger.dk.magenta.datafordeler.ger.configuration and source fetcher.
     * We store fetched dk.magenta.datafordeler.ger.data in a local cache, so create a random folder for that.
     */
    @PostConstruct
    public void init() throws IOException {
        if (this.localCopyFolder == null || this.localCopyFolder.isEmpty()) {
            File temp = File.createTempFile("datafordeler-cache", "");
            temp.delete();
            temp.mkdir();
            this.localCopyFolder = temp.getAbsolutePath();
        }
    }

    public GerConfigurationManager getConfigurationManager() {
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

    @Override
    public ImportInputStream pullRawData(URI eventInterface, EntityManager entityManager, ImportMetadata importMetadata) throws DataFordelerException {
        this.log.info("eventInterface: " + eventInterface);
        GerConfiguration configuration = this.configurationManager.getConfiguration();
        GerConfiguration.RegisterType registerType = configuration.getRegisterType(entityManager.getSchema());
        if (registerType == null) {
            registerType = GerConfiguration.RegisterType.DISABLED;
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
        }
        return null;
    }

    @Override
    protected ItemInputStream<? extends PluginSourceData> parseEventResponse(InputStream rawData, EntityManager entityManager) throws DataFordelerException {
        return null;
    }

    @Override
    public String getPullCronSchedule() {
        return this.configurationManager.getConfiguration().getPullCronSchedule();
    }

}
