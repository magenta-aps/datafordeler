package dk.magenta.datafordeler.cpr;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import dk.magenta.datafordeler.core.Engine;
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
import dk.magenta.datafordeler.cpr.data.residence.ResidenceEntityManager;
import dk.magenta.datafordeler.cpr.data.road.RoadEntityManager;
import dk.magenta.datafordeler.cpr.records.output.PersonRecordOutputWrapper;
import dk.magenta.datafordeler.cpr.records.road.data.RoadEntity;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.mockito.stubbing.Answer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.*;

import static org.mockito.Matchers.any;
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

    @SpyBean
    protected CprConfigurationManager configurationManager;

    protected CprConfiguration configuration;

    private static final HashMap<String, String> schemaMap = new HashMap<>();

    static {
        schemaMap.put("person", PersonEntity.schema);
    }

    @Autowired
    protected PersonCustodyRelationsManager custodyManager;

    @SpyBean
    protected PersonEntityManager personEntityManager;

    @Autowired
    protected RoadEntityManager roadEntityManager;

    @Autowired
    protected ResidenceEntityManager residenceEntityManager;

    @Autowired
    protected PersonRecordOutputWrapper personRecordOutputWrapper;

    @Before
    public void setupConfiguration() {
        this.configuration = ((CprConfigurationManager) this.plugin.getConfigurationManager()).getConfiguration();
        when(this.configurationManager.getConfiguration()).thenReturn(this.configuration);
    }

    public CprConfiguration getConfiguration() {
        return this.configuration;
    }

    @SpyBean
    private CprRegisterManager registerManager;

    @Before
    public void setupRegisterManager() {
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

    @SpyBean
    protected DafoUserManager dafoUserManager;

    protected void applyAccess(TestUserDetails testUserDetails) {
        when(this.dafoUserManager.getFallbackUser()).thenReturn(testUserDetails);
    }

    protected void whitelistLocalhost() {
        when(this.dafoUserManager.getIpWhitelist()).thenReturn(Collections.singleton("127.0.0.1"));
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
            Assert.assertEquals(node1.isNull(), node2.isNull());
            Assert.assertEquals(node1.isArray(), node2.isArray());
            Assert.assertEquals(node1.isObject(), node2.isObject());
            Assert.assertEquals(node1.isLong(), node2.isLong());
            Assert.assertEquals(node1.isInt(), node2.isInt());
            Assert.assertEquals(node1.isShort(), node2.isShort());
            Assert.assertEquals(node1.isBoolean(), node2.isBoolean());
            Assert.assertEquals(node1.isTextual(), node2.isTextual());
            if (node1.isArray()) {
                Assert.assertEquals(node1.size(), node2.size());
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
                Assert.assertEquals(node1.size(), node2.size());
                Iterator<String> keys = node1.fieldNames();
                while (keys.hasNext()) {
                    String key = keys.next();
                    Assert.assertNotNull(node2.get(key));
                    if (!ignoreKeys.contains(key)) {
                        assertJsonEquality(node1.get(key), node2.get(key), ignoreArrayOrdering, printDifference);
                    }
                }
            } else {
                Assert.assertEquals(node1.asText(), node2.asText());
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
        }).when(this.registerManager).getFtpCommunicator(any(URI.class), any(CprRecordEntityManager.class));

        this.ftpService = new FtpService();
        this.ftpService.startServer(username, password, port, files);
    }

    protected void stopFtp() {
        this.ftpService.stopServer();
        this.ftpService = null;
    }

    protected void assertJsonEquals(String jsonExpected, String jsonActual) throws JsonProcessingException {
        Assert.assertTrue(
                objectMapper.readTree(jsonExpected) + "   !=   " + objectMapper.readTree(jsonActual),
                new UnorderedJsonListComparator().compare(objectMapper.readTree(jsonExpected), objectMapper.readTree(jsonActual)) == 0
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
        Assert.assertTrue(message, jsonContains(jsonExpected, jsonActual));
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

    @After
    public void cleanup() {
        try (Session session = sessionManager.getSessionFactory().openSession()) {
            QueryManager.clearCaches();
            Transaction transaction = session.beginTransaction();
            for (PersonEntity entity : QueryManager.getAllEntities(session, PersonEntity.class)) {
                session.delete(entity);
            }
            for (RoadEntity entity : QueryManager.getAllEntities(session, RoadEntity.class)) {
                session.delete(entity);
            }
            for (PersonSubscription entity : QueryManager.getAllEntities(session, PersonSubscription.class, false)) {
                session.delete(entity);
            }
            transaction.commit();
        }
    }
}
