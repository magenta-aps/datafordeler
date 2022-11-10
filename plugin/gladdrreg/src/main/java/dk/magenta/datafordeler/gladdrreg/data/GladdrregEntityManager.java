package dk.magenta.datafordeler.gladdrreg.data;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import dk.magenta.datafordeler.core.database.*;
import dk.magenta.datafordeler.core.exception.DataFordelerException;
import dk.magenta.datafordeler.core.exception.DataStreamException;
import dk.magenta.datafordeler.core.exception.ParseException;
import dk.magenta.datafordeler.core.exception.WrongSubclassException;
import dk.magenta.datafordeler.core.io.ImportMetadata;
import dk.magenta.datafordeler.core.io.PluginSourceData;
import dk.magenta.datafordeler.core.io.Receipt;
import dk.magenta.datafordeler.core.plugin.Communicator;
import dk.magenta.datafordeler.core.plugin.EntityManager;
import dk.magenta.datafordeler.core.plugin.HttpCommunicator;
import dk.magenta.datafordeler.core.plugin.RegisterManager;
import dk.magenta.datafordeler.core.util.ItemInputStream;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.time.OffsetDateTime;
import java.util.*;

/**
 * Created by lars on 29-05-17.
 */
@Component
public abstract class GladdrregEntityManager extends EntityManager {

    @Autowired
    private ObjectMapper objectMapper;

    private HttpCommunicator commonFetcher;

    protected Logger log = LogManager.getLogger(GladdrregEntityManager.class.getCanonicalName());

    private Collection<String> handledURISubstrings;

    protected abstract String getBaseName();

    public GladdrregEntityManager() {
        this.commonFetcher = new HttpCommunicator();
        this.handledURISubstrings = new ArrayList<>();
    }

    @Override
    public void setRegisterManager(RegisterManager registerManager) {
        super.setRegisterManager(registerManager);
        this.handledURISubstrings.add(expandBaseURI(this.getBaseEndpoint(), "/" + this.getBaseName(), null, null).toString());
        this.handledURISubstrings.add(expandBaseURI(this.getBaseEndpoint(), "/get/" + this.getBaseName(), null, null).toString());
    }

    @Override
    public Collection<String> getHandledURISubstrings() {
        return this.handledURISubstrings;
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
    public URI getBaseEndpoint() {
        return this.getRegisterManager().getBaseEndpoint();
    }

    @Override
    protected URI getReceiptEndpoint(Receipt receipt) {
        return expandBaseURI(this.getBaseEndpoint(), "/receipt/" + receipt.getEventID());
    }

    @Override
    public RegistrationReference parseReference(InputStream referenceData) throws IOException {
        return this.getObjectMapper().readValue(referenceData, this.managedRegistrationReferenceClass);
    }

    @Override
    public RegistrationReference parseReference(String referenceData, String charsetName) throws IOException {
        return this.getObjectMapper().readValue(referenceData.getBytes(charsetName), this.managedRegistrationReferenceClass);
    }

    @Override
    public List<? extends Registration> parseData(PluginSourceData registrationData, ImportMetadata importMetadata) throws DataFordelerException {
        try {
            return this.parseData(objectMapper.readTree(registrationData.getData()), importMetadata);
        } catch (IOException e) {
            throw new DataStreamException(e);
        }
    }

    @Override
    public List<? extends Registration> parseData(InputStream registrationData, ImportMetadata importMetadata) throws DataFordelerException {
        try {
            return this.parseData(objectMapper.readTree(registrationData), importMetadata);
        } catch (IOException e) {
            throw new DataStreamException(e);
        } finally {
            try {
                registrationData.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public List<? extends Registration> parseData(JsonNode registrationData, ImportMetadata importMetadata) throws DataFordelerException {
        OffsetDateTime timestamp = OffsetDateTime.now();
        // Check whether the object is wrapped
        if (registrationData.has("registrationFrom") ||
                registrationData.has("registreringFra")) {
            // Unwrapped case
            try {
                Registration registration = this.getObjectMapper().treeToValue(registrationData, this.managedRegistrationClass);
                registration.setLastImportTime(importMetadata.getImportTime());
                for (Object oEffect : registration.getEffects()) {
                    Effect effect = (Effect) oEffect;
                    for (Object oDataItem : effect.getDataItems()) {
                        DataItem dataItem = (DataItem) oDataItem;
                        RecordData recordData = new RecordData(timestamp);
                        recordData.setSourceData(objectMapper.valueToTree(dataItem).toString());
                        dataItem.setLastUpdated(importMetadata.getImportTime());
                        dataItem.addRecordData(recordData);
                    }
                }

                registration.wireEffects();
                return Collections.singletonList(registration);
            } catch (JsonProcessingException e) {
                throw new ParseException("Error parsing registration " + registrationData, e);
            }
        } else {
            // Wrapped case
            Iterator<String> keyIterator = registrationData.fieldNames();
            ArrayList<Registration> list = new ArrayList<>();
            while (keyIterator.hasNext()) {
                String key = keyIterator.next();
                list.addAll(this.parseData(registrationData.get(key), importMetadata));
            }
            return list;
        }
    }

    protected abstract RegistrationReference createRegistrationReference(URI uri);

    @Override
    public RegistrationReference parseReference(URI uri) {
        return this.createRegistrationReference(uri);
    }

    @Override
    public URI getRegistrationInterface(RegistrationReference reference) throws WrongSubclassException {
        if (!this.managedRegistrationReferenceClass.isInstance(reference)) {
            throw new WrongSubclassException(this.managedRegistrationReferenceClass, reference);
        }
        if (reference.getURI() != null) {
            return reference.getURI();
        }
        return EntityManager.expandBaseURI(this.getBaseEndpoint(), "/get/" + this.getBaseName() + "/" + reference.getChecksum());
    }

    @Override
    protected URI getListChecksumInterface(OffsetDateTime fromDate) {
        return this.getRegisterManager().getListChecksumInterface(this.getSchema(), fromDate);
    }

    @Override
    protected ItemInputStream<? extends EntityReference> parseChecksumResponse(InputStream responseContent) throws DataFordelerException {
        return ItemInputStream.parseJsonStream(responseContent, this.managedEntityReferenceClass, "items", this.getObjectMapper());
    }

    @Override
    protected Logger getLog() {
        return this.log;
    }

}
