package dk.magenta.datafordeler.cpr;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import dk.magenta.datafordeler.core.Engine;
import dk.magenta.datafordeler.core.database.DatabaseEntry;
import dk.magenta.datafordeler.core.database.LastUpdated;
import dk.magenta.datafordeler.core.database.QueryManager;
import dk.magenta.datafordeler.core.database.SessionManager;
import dk.magenta.datafordeler.core.fapi.ParameterMap;
import dk.magenta.datafordeler.core.plugin.FtpCommunicator;
import dk.magenta.datafordeler.core.user.DafoUserManager;
import dk.magenta.datafordeler.core.util.UnorderedJsonListComparator;
import dk.magenta.datafordeler.cpr.configuration.CprConfiguration;
import dk.magenta.datafordeler.cpr.configuration.CprConfigurationManager;
import dk.magenta.datafordeler.cpr.data.CprRecordEntityManager;
import dk.magenta.datafordeler.cpr.data.person.PersonCustodyRelationsManager;
import dk.magenta.datafordeler.cpr.data.person.PersonEntity;
import dk.magenta.datafordeler.cpr.data.person.PersonEntityManager;
import dk.magenta.datafordeler.cpr.data.person.PersonSubscription;
import dk.magenta.datafordeler.cpr.data.road.RoadEntityManager;
import dk.magenta.datafordeler.cpr.records.output.PersonRecordOutputWrapper;
import dk.magenta.datafordeler.cpr.records.road.data.RoadEntity;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.stubbing.Answer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;

@Component
public abstract class TestBase {

    @Autowired
    protected CprPlugin plugin;

    public CprPlugin getPlugin() {
        return this.plugin;
    }

    @Autowired
    protected Engine engine;

    public Engine getEngine() {
        return this.engine;
    }

    @MockitoSpyBean
    protected CprConfigurationManager configurationManager;

    protected CprConfiguration configuration;

    private static final HashMap<String, String> schemaMap = new HashMap<>();

    static {
        schemaMap.put("person", PersonEntity.schema);
    }

    @Autowired
    protected PersonCustodyRelationsManager custodyManager;

    @MockitoSpyBean
    protected PersonEntityManager personEntityManager;

    @Autowired
    protected RoadEntityManager roadEntityManager;

    @Autowired
    protected PersonRecordOutputWrapper personRecordOutputWrapper;


    @BeforeEach
    public void setupPlugin() throws IOException {
        this.plugin.init();
    }

    @BeforeEach
    public void setupConfiguration() {
        this.configuration = ((CprConfigurationManager) this.plugin.getConfigurationManager()).getConfiguration();
        when(this.configurationManager.getConfiguration()).thenReturn(this.configuration);
    }

    public CprConfiguration getConfiguration() {
        return this.configuration;
    }

    @MockitoSpyBean
    protected CprRegisterManager registerManager;

    @BeforeEach
    public void setupRegisterManager() throws IOException {
        this.registerManager.setProxyString(null);
    }

    public CprRegisterManager getRegisterManager() {
        return this.registerManager;
    }

    @Autowired
    protected SessionManager sessionManager;

    public SessionManager getSessionManager() {
        return this.sessionManager;
    }

    @Autowired
    protected ObjectMapper objectMapper;

    public ObjectMapper getObjectMapper() {
        return this.objectMapper;
    }

    @Autowired
    protected TestRestTemplate restTemplate;

    public TestRestTemplate getRestTemplate() {
        return this.restTemplate;
    }

    protected static final HashSet<String> ignoreKeys = new HashSet<String>();

    static {
        ignoreKeys.add("sidstImporteret");
    }

    @MockitoSpyBean
    protected DafoUserManager dafoUserManager;

    protected void applyAccess(TestUserDetails testUserDetails) {
        when(this.dafoUserManager.getFallbackUser()).thenReturn(testUserDetails);
    }

    protected ResponseEntity<String> restSearch(ParameterMap parameters, String type) throws URISyntaxException {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", "application/json");
        HttpEntity<String> httpEntity = new HttpEntity<String>("", headers);
        return this.restTemplate.exchange(new URI("/cpr/" + type + "/1/rest/search?" + parameters.asUrlParams()), HttpMethod.GET, httpEntity, String.class);
    }

    protected ResponseEntity<String> uuidSearch(String id, String type) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", "application/json");
        HttpEntity<String> httpEntity = new HttpEntity<String>("", headers);
        return this.restTemplate.exchange("/cpr/" + type + "/1/rest/" + id, HttpMethod.GET, httpEntity, String.class);
    }

    protected void assertJsonEquality(JsonNode node1, JsonNode node2, boolean ignoreArrayOrdering, boolean printDifference) {
        try {
            Assertions.assertEquals(node1.isNull(), node2.isNull());
            Assertions.assertEquals(node1.isArray(), node2.isArray());
            Assertions.assertEquals(node1.isObject(), node2.isObject());
            Assertions.assertEquals(node1.isLong(), node2.isLong());
            Assertions.assertEquals(node1.isInt(), node2.isInt());
            Assertions.assertEquals(node1.isShort(), node2.isShort());
            Assertions.assertEquals(node1.isBoolean(), node2.isBoolean());
            Assertions.assertEquals(node1.isTextual(), node2.isTextual());
            if (node1.isArray()) {
                Assertions.assertEquals(node1.size(), node2.size());
                if (ignoreArrayOrdering) {
                    for (int i = 0; i < node1.size(); i++) {
                        boolean match = false;
                        for (int j = 0; j < node2.size(); j++) {
                            try {
                                assertJsonEquality(node1.get(i), node2.get(j), true, false);
                                match = true;
                            } catch (AssertionError e) {
                            }
                        }
                        if (!match) {
                            throw new AssertionError();
                        }
                    }
                } else {
                    for (int i = 0; i < node1.size(); i++) {
                        assertJsonEquality(node1.get(i), node2.get(i), false, printDifference);
                    }
                }
            } else if (node1.isObject()) {
                Assertions.assertEquals(node1.size(), node2.size());
                Iterator<String> keys = node1.fieldNames();
                while (keys.hasNext()) {
                    String key = keys.next();
                    Assertions.assertNotNull(node2.get(key));
                    if (!ignoreKeys.contains(key)) {
                        assertJsonEquality(node1.get(key), node2.get(key), ignoreArrayOrdering, printDifference);
                    }
                }
            } else {
                Assertions.assertEquals(node1.asText(), node2.asText());
            }
        } catch (AssertionError e) {
            if (printDifference) {
                try {
                    System.out.println("\n" + this.objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(node1) + "\n != \n" + this.objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(node2) + "\n\n\n");
                } catch (JsonProcessingException e1) {
                    System.out.println("\n" + node1.asText() + "\n != \n" + node2.asText() + "\n\n\n");
                }
            }
            throw e;
        }
    }


    private static SSLSocketFactory getTrustAllSSLSocketFactory() {
        TrustManager[] trustManager = new TrustManager[]{new X509TrustManager() {
            public X509Certificate[] getAcceptedIssuers() {
                return null;
            }

            public void checkClientTrusted(X509Certificate[] certs, String authType) {
            }

            public void checkServerTrusted(X509Certificate[] certs, String authType) {
            }
        }};
        SSLContext sslContext = null;
        try {
            sslContext = SSLContext.getInstance("SSL");
            sslContext.init(null, trustManager, new SecureRandom());
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (KeyManagementException e) {
            e.printStackTrace();
        }
        return sslContext.getSocketFactory();
    }


    private FtpService ftpService;

    protected void startFtp(String username, String password, int port, List<File> files) throws Exception {
        if (this.ftpService != null) {
            this.stopFtp();
        }
        doAnswer((Answer<FtpCommunicator>) invocation -> {
            FtpCommunicator ftpCommunicator = (FtpCommunicator) invocation.callRealMethod();
            ftpCommunicator.setSslSocketFactory(getTrustAllSSLSocketFactory());
            return ftpCommunicator;
        }).when(this.registerManager).getFtpCommunicator(any(Session.class), any(URI.class), any(CprRecordEntityManager.class));

        this.ftpService = new FtpService();
        this.ftpService.startServer(username, password, port, files);
    }

    protected void stopFtp() {
        this.ftpService.stopServer();
        this.ftpService = null;
    }

    protected void assertJsonEquals(String jsonExpected, String jsonActual) throws JsonProcessingException {
        Assertions.assertTrue(
                new UnorderedJsonListComparator().compare(objectMapper.readTree(jsonExpected), objectMapper.readTree(jsonActual)) == 0,
                objectMapper.readTree(jsonExpected) + "   !=   " + objectMapper.readTree(jsonActual)
        );
    }

    protected void assertJsonContains(String message, String jsonExpected, String jsonActual) throws JsonProcessingException {
        this.assertJsonContains(null, objectMapper.readTree(jsonExpected), objectMapper.readTree(jsonActual));
    }
    protected void assertJsonContains(String jsonExpected, String jsonActual) throws JsonProcessingException {
        this.assertJsonContains(objectMapper.readTree(jsonExpected), objectMapper.readTree(jsonActual));
    }

    protected void assertJsonContains(JsonNode jsonExpected, JsonNode jsonActual) throws JsonProcessingException {
        this.assertJsonContains(null, jsonExpected, jsonActual);
    }
    protected void assertJsonContains(String message, JsonNode jsonExpected, JsonNode jsonActual) throws JsonProcessingException {
        Assertions.assertTrue(
                jsonContains(jsonExpected, jsonActual),
                message
        );
    }
    protected boolean jsonContains(JsonNode jsonExpected, JsonNode jsonActual) throws JsonProcessingException {
        if (!jsonExpected.getNodeType().equals(jsonActual.getNodeType())) {
            return false;
        } else if (jsonExpected.isArray()) {
            for (int i=0; i<jsonExpected.size(); i++) {
                boolean found = false;
                for (int j=0; j<jsonActual.size() && !found; j++) {
                    if (jsonContains(jsonExpected.get(i), jsonActual.get(j))) {
                        found = true;
                    }
                }
                if (!found) {
                    return false;
                }
            }
            return true;
        } else if (jsonExpected.isObject()) {
            ObjectNode expectedObject = (ObjectNode) jsonExpected;
            ObjectNode actualObject = (ObjectNode) jsonActual;
            for (Iterator<String> it = expectedObject.fieldNames(); it.hasNext(); ) {
                String key = it.next();
                if (!actualObject.has(key)) {
                    return false;
                }
                if (!jsonContains(expectedObject.get(key), actualObject.get(key))) {
                    return false;
                }
            }
            return true;
        } else if (jsonExpected.isNull()) {
            return jsonActual.isNull();
        } else if (jsonExpected.isTextual()) {
            return jsonExpected.asText().equals(jsonActual.asText());
        } else if (jsonExpected.isInt()) {
            return jsonExpected.asInt() == jsonActual.asInt();
        } else if (jsonExpected.isDouble()) {
            return jsonExpected.asDouble() == jsonActual.asDouble();
        } else if (jsonExpected.isLong()) {
            return jsonExpected.asLong() == jsonActual.asLong();
        } else if (jsonExpected.isBoolean()) {
            return jsonExpected.asBoolean() == jsonActual.asBoolean();
        }
        return true;
    }

    @AfterEach
    public void cleanup() {
        SessionFactory sessionFactory = sessionManager.getSessionFactory();
        try (Session session = sessionFactory.openSession()) {
            QueryManager.clearCaches();
            Class[] classes = new Class[]{
                    PersonEntity.class,
                    RoadEntity.class,
                    PersonSubscription.class,
                    LastUpdated.class,
            };
            Transaction transaction = session.beginTransaction();
            for (Class cls : classes) {
                List<DatabaseEntry> eList = QueryManager.getAllItems(session, cls);
                for (DatabaseEntry e : eList) {
                    session.remove(e);
                }
            }
            transaction.commit();
            QueryManager.clearCaches();
        }
    }
}
