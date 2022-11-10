package dk.magenta.datafordeler.cvr;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import dk.magenta.datafordeler.core.exception.DataFordelerException;
import dk.magenta.datafordeler.core.exception.DataStreamException;
import dk.magenta.datafordeler.core.exception.HttpStatusException;
import dk.magenta.datafordeler.core.plugin.HttpCommunicator;
import dk.magenta.datafordeler.core.plugin.ScanScrollCommunicator;
import dk.magenta.datafordeler.cvr.configuration.CvrConfiguration;
import dk.magenta.datafordeler.cvr.configuration.CvrConfigurationManager;
import dk.magenta.datafordeler.cvr.entitymanager.CvrEntityManager;
import dk.magenta.datafordeler.cvr.records.CompanyRecord;
import dk.magenta.datafordeler.cvr.records.CvrEntityRecord;
import dk.magenta.datafordeler.cvr.records.ParticipantRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Scanner;

@Component
public class DirectLookup {

    @Autowired
    private CvrConfigurationManager configurationManager;

    @Autowired
    private ObjectMapper objectMapper;

    public Collection<CompanyRecord> companyLookup(Collection<String> cvrNumbers) throws DataFordelerException {
        List<CompanyRecord> records = new ArrayList<CompanyRecord>();
        for (String cvrNumber : cvrNumbers) {
            records.add(companyLookup(cvrNumber));
        }
        return records;
    }

    @Autowired
    private CvrRegisterManager cvrRegisterManager;

    public <R extends CvrEntityRecord> List<R> lookup(ObjectNode requestBody, String schema) throws DataStreamException, HttpStatusException, IOException, URISyntaxException, GeneralSecurityException {
        CvrConfiguration configuration = this.configurationManager.getConfiguration();

        File keystore = new File(configuration.getCompanyRegisterDirectLookupCertificate());
        String keystorePassword = null;
        try {
            keystorePassword = configuration.getCompanyRegisterDirectLookupPassword();
        } catch (GeneralSecurityException | IOException e) {
            throw new DataStreamException(e);
        }
        ScanScrollCommunicator httpCommunicator = new ScanScrollCommunicator(keystore, keystorePassword);


        URI queryUri;
        URI scrollURI;
        try {
            queryUri = new URL(configuration.getStartAddress(schema)).toURI();
            scrollURI = new URL(configuration.getScrollAddress(schema)).toURI();
        } catch (URISyntaxException | MalformedURLException e) {
            throw new DataStreamException(e);
        }
        CvrEntityManager<R> entityManager = (CvrEntityManager<R>) cvrRegisterManager.getEntityManager(schema);

        httpCommunicator.setScrollIdJsonKey("_scroll_id");

        try (InputStream response = httpCommunicator.fetch(queryUri, scrollURI, requestBody.toString())) {
            Scanner scanner = new Scanner(response, StandardCharsets.UTF_8).useDelimiter(String.valueOf(ScanScrollCommunicator.delimiter));
            List<R> companyRecords = new ArrayList<>();
            while (scanner.hasNext()) {
                String data = scanner.next();
                companyRecords.addAll(entityManager.parseNode(objectMapper.readTree(data)));
            }
            return companyRecords;
        }
    }


    public CompanyRecord companyLookup(String cvrNumber) throws DataFordelerException {
        CvrConfiguration configuration = this.configurationManager.getConfiguration();
        File keystore = new File(configuration.getCompanyRegisterDirectLookupCertificate());
        String keystorePassword = null;
        try {
            keystorePassword = configuration.getCompanyRegisterDirectLookupPassword();
        } catch (GeneralSecurityException | IOException e) {
            throw new DataStreamException(e);
        }
        URI queryUri;
        try {
            queryUri = new URL(configuration.getCompanyRegisterDirectLookupAddress().replace("%{cvr}", cvrNumber)).toURI();
        } catch (URISyntaxException | MalformedURLException e) {
            throw new DataStreamException(e);
        }
        HttpCommunicator httpCommunicator = new HttpCommunicator(keystore, keystorePassword);
        InputStream response = httpCommunicator.fetch(queryUri);
        try {
            return objectMapper.readValue(response, CompanyRecord.class);
        } catch (IOException e) {
            throw new DataStreamException(e);
        } finally {
            try {
                response.close();
            } catch (IOException e) {
            }
        }
    }

    public Collection<ParticipantRecord> participantLookup(Collection<String> unitNumbers) throws DataFordelerException {
        List<ParticipantRecord> records = new ArrayList<ParticipantRecord>();
        for (String unitNumber : unitNumbers) {
            records.add(participantLookup(unitNumber));
        }
        return records;
    }


    public ParticipantRecord participantLookup(String unitNumber) throws DataFordelerException {
        CvrConfiguration configuration = this.configurationManager.getConfiguration();
        File keystore = new File(configuration.getParticipantRegisterDirectLookupCertificate());
        String keystorePassword = null;
        try {
            keystorePassword = configuration.getParticipantRegisterDirectLookupPassword();
        } catch (GeneralSecurityException | IOException e) {
            throw new DataStreamException(e);
        }
        URI queryUri;
        try {
            queryUri = new URL(configuration.getParticipantRegisterDirectLookupAddress().replace("%{unit}", unitNumber)).toURI();
        } catch (URISyntaxException | MalformedURLException e) {
            throw new DataStreamException(e);
        }
        HttpCommunicator httpCommunicator = new HttpCommunicator(keystore, keystorePassword);
        InputStream response = httpCommunicator.fetch(queryUri);
        try {
            return objectMapper.readValue(response, ParticipantRecord.class);
        } catch (IOException e) {
            throw new DataStreamException(e);
        } finally {
            try {
                response.close();
            } catch (IOException e) {
            }
        }
    }


    public static ObjectNode addObject(ObjectMapper objectMapper, ObjectNode parent, String key) {
        ObjectNode object = objectMapper.createObjectNode();
        if (parent != null) {
            parent.set(key, object);
        }
        return object;
    }

    public static ObjectNode addObject(ObjectMapper objectMapper, ArrayNode parent) {
        ObjectNode object = objectMapper.createObjectNode();
        if (parent != null) {
            parent.add(object);
        }
        return object;
    }

    public static ArrayNode addList(ObjectMapper objectMapper, ObjectNode parent, String key) {
        ArrayNode object = objectMapper.createArrayNode();
        if (parent != null) {
            parent.set(key, object);
        }
        return object;
    }

    public static ArrayNode addList(ObjectMapper objectMapper, ArrayNode parent) {
        ArrayNode object = objectMapper.createArrayNode();
        if (parent != null) {
            parent.add(object);
        }
        return object;
    }

}
