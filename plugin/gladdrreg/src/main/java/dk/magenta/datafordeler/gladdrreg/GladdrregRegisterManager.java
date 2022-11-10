package dk.magenta.datafordeler.gladdrreg;

import com.fasterxml.jackson.databind.ObjectMapper;
import dk.magenta.datafordeler.core.database.SessionManager;
import dk.magenta.datafordeler.core.exception.DataFordelerException;
import dk.magenta.datafordeler.core.exception.WrongSubclassException;
import dk.magenta.datafordeler.core.io.Event;
import dk.magenta.datafordeler.core.io.ImportMetadata;
import dk.magenta.datafordeler.core.io.PluginSourceData;
import dk.magenta.datafordeler.core.plugin.*;
import dk.magenta.datafordeler.core.util.ItemInputStream;
import dk.magenta.datafordeler.core.util.ListHashMap;
import dk.magenta.datafordeler.gladdrreg.configuration.GladdregConfigurationManager;
import dk.magenta.datafordeler.gladdrreg.data.GladdrregEntityManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Created by lars on 16-05-17.
 */
@Component
public class GladdrregRegisterManager extends RegisterManager {

    private HttpCommunicator commonFetcher;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private GladdregConfigurationManager configurationManager;

    @Autowired
    private GladdrregPlugin plugin;

    @Autowired
    private SessionManager sessionManager;

    private Logger log = LogManager.getLogger(GladdregRegisterManager.class.getCanonicalName());

    public GladdrregRegisterManager() {
        this.commonFetcher = new HttpCommunicator();
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
        try {
            return new URI(this.configurationManager.getConfiguration().getRegisterAddress());
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

    @Override
    public URI getEventInterface(EntityManager entityManager) {
        return this.getEventInterface();
    }

    public URI getEventInterface() {
        return expandBaseURI(this.getBaseEndpoint(), "/getNewEvents");
    }

    @Override
    public boolean pullsEventsCommonly() {
        return true;
    }

    public ItemInputStream<? extends PluginSourceData> pullEvents() throws DataFordelerException {
        Communicator eventCommunicator = this.getEventFetcher();
        InputStream responseBody = eventCommunicator.fetch(this.getEventInterface());
        return this.parseEventResponse(responseBody, null);
    }

    @Override
    protected ItemInputStream<Event> parseEventResponse(InputStream responseContent, EntityManager entityManager) throws DataFordelerException {
        if (entityManager != null && !(entityManager instanceof GladdrregEntityManager)) {
            throw new WrongSubclassException(GladdrregEntityManager.class, entityManager);
        }


        return ItemInputStream.parseJsonStream(responseContent, Event.class, "events", this.getObjectMapper());
    }

    @Override
    protected Communicator getChecksumFetcher() {
        return this.commonFetcher;
    }

    @Override
    public URI getListChecksumInterface(String schema, OffsetDateTime from) {
        ListHashMap<String, String> parameters = new ListHashMap<>();
        if (schema != null) {
            parameters.add("objectType", schema);
        }
        if (from != null) {
            parameters.add("timestamp", from.format(DateTimeFormatter.ISO_DATE_TIME));
        }
        return expandBaseURI(this.getBaseEndpoint(), "/listChecksums", RegisterManager.joinQueryString(parameters), null);
    }

    public String getPullCronSchedule() {
        return this.configurationManager.getConfiguration().getPullCronSchedule();
    }

    @Override
    public void setLastUpdated(EntityManager entityManager, ImportMetadata importMetadata) {
        Session session = this.getSessionManager().getSessionFactory().openSession();
        OffsetDateTime timestamp = importMetadata.getImportTime();
        if (entityManager == null) {
            for (EntityManager e : this.entityManagers) {
                e.setLastUpdated(session, timestamp);
            }
        } else {
            entityManager.setLastUpdated(session, timestamp);
        }
        session.close();
    }

}
