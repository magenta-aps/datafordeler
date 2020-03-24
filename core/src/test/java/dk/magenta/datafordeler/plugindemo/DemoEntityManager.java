package dk.magenta.datafordeler.plugindemo;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import dk.magenta.datafordeler.core.Application;
import dk.magenta.datafordeler.core.database.EntityReference;
import dk.magenta.datafordeler.core.database.Registration;
import dk.magenta.datafordeler.core.database.RegistrationReference;
import dk.magenta.datafordeler.core.exception.DataFordelerException;
import dk.magenta.datafordeler.core.exception.DataStreamException;
import dk.magenta.datafordeler.core.exception.ParseException;
import dk.magenta.datafordeler.core.exception.WrongSubclassException;
import dk.magenta.datafordeler.core.fapi.BaseQuery;
import dk.magenta.datafordeler.core.fapi.FapiBaseService;
import dk.magenta.datafordeler.core.io.ImportMetadata;
import dk.magenta.datafordeler.core.io.PluginSourceData;
import dk.magenta.datafordeler.core.io.Receipt;
import dk.magenta.datafordeler.core.plugin.Communicator;
import dk.magenta.datafordeler.core.plugin.EntityManager;
import dk.magenta.datafordeler.core.plugin.HttpCommunicator;
import dk.magenta.datafordeler.core.util.ItemInputStream;
import dk.magenta.datafordeler.plugindemo.fapi.DemoEntityService;
import dk.magenta.datafordeler.plugindemo.fapi.DemoRecordQuery;
import dk.magenta.datafordeler.plugindemo.model.DemoEntityRecord;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

@Component
public class DemoEntityManager extends EntityManager {

    private static Logger log = LogManager.getLogger(DemoEntityManager.class.getCanonicalName());

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private DemoEntityService demoEntityService;

    private HttpCommunicator commonFetcher;

    private String[] URISubstrings = {
            "http://localhost:" + Application.servicePort
    };

    private URI baseEndpoint = null;

    public DemoEntityManager() {
        this.managedEntityClass = DemoEntityRecord.class;
        this.commonFetcher = new HttpCommunicator();
        try {
            this.baseEndpoint = new URI("http", null, "localhost", Application.servicePort, "/test", null, null);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    public void setPort(int port) {
        this.URISubstrings = new String[] {
                "http://localhost:" + port
        };
        try {
            this.baseEndpoint = new URI("http", null, "localhost", port, "/test", null, null);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Collection<String> getHandledURISubstrings() {
        ArrayList<String> list = new ArrayList<>();
        Collections.addAll(list, this.URISubstrings);
        return list;
    }

    @Override
    public boolean handlesOwnSaves() {
        return true;
    }

    @Override
    protected ObjectMapper getObjectMapper() {
        return this.objectMapper;
    }

    @Override
    protected Communicator getRegistrationFetcher() {
        return this.commonFetcher;
    }

    @Override
    protected Communicator getReceiptSender() {
        return this.commonFetcher;
    }

    @Override
    public FapiBaseService getEntityService() {
        return this.demoEntityService;
    }

    @Override
    protected Logger getLog() {
        return this.log;
    }

    @Override
    public BaseQuery getQuery() {
        return new DemoRecordQuery();
    }

    @Override
    public BaseQuery getQuery(String... joined) {
        return this.getQuery();
    }


    /**
     * Get the base URI for the register; other EndPoint methods will call this to construct their own return values
     * @return
     */
    @Override
    public URI getBaseEndpoint() {
        return this.baseEndpoint;
    }

    @Override
    public String getSchema() {
        return DemoEntityRecord.schema;
    }

    public void setBaseEndpoint(URI baseEndpoint) {
        this.baseEndpoint = baseEndpoint;
    }

    /** Receipt sending **/

    @Override
    protected URI getReceiptEndpoint(Receipt receipt) {
        return expandBaseURI(this.getBaseEndpoint(), "/receipt");
    }


    /** Reference parsing **/

    public RegistrationReference parseReference(InputStream referenceData) throws IOException {
        return this.objectMapper.readValue(referenceData, this.managedRegistrationReferenceClass);
    }

    public RegistrationReference parseReference(String referenceData, String charsetName) throws IOException {
        return this.objectMapper.readValue(referenceData.getBytes(charsetName), this.managedRegistrationReferenceClass);
    }

    public RegistrationReference parseReference(URI referenceURI) {
        return null;
    }

    /** Registration parsing **/

    public List<? extends Registration> parseData(InputStream registrationData, ImportMetadata importMetadata) throws DataFordelerException {
        try {
            this.parseData(objectMapper.readTree(registrationData), importMetadata);
        } catch (IOException e) {
            throw new DataStreamException(e);
        }
        return null;
    }

    @Override
    public List<? extends Registration> parseData(PluginSourceData registrationData, ImportMetadata importMetadata) throws DataFordelerException {
        try {
            this.parseData(objectMapper.readTree(registrationData.getData()), importMetadata);
        } catch (IOException e) {
            throw new DataStreamException(e);
        }
        return null;
    }

    public void parseData(JsonNode jsonNode, ImportMetadata importMetadata) throws ParseException {
        System.out.println("parse this: "+jsonNode.toString());
    }

    /** Registration fetching **/

    public URI getRegistrationInterface(RegistrationReference reference) throws WrongSubclassException {
        if (!this.managedRegistrationReferenceClass.isInstance(reference)) {
            throw new WrongSubclassException(this.managedRegistrationReferenceClass, reference);
        }
        if (reference.getURI() != null) {
            return reference.getURI();
        }
        return EntityManager.expandBaseURI(this.getBaseEndpoint(), "/get/"+reference.getChecksum());
    }

    /** Checksum fetching **/

    @Override
    protected URI getListChecksumInterface(OffsetDateTime fromDate) {
        return this.getRegisterManager().getListChecksumInterface(this.getSchema(), fromDate);
    }

    @Override
    protected ItemInputStream<? extends EntityReference> parseChecksumResponse(InputStream responseContent) throws DataFordelerException {
        return null;
    }

}
