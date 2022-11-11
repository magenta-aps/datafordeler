package dk.magenta.datafordeler.cpr;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import dk.magenta.datafordeler.core.Application;
import dk.magenta.datafordeler.core.Engine;
import dk.magenta.datafordeler.core.Pull;
import dk.magenta.datafordeler.core.database.QueryManager;
import dk.magenta.datafordeler.core.database.SessionManager;
import dk.magenta.datafordeler.core.exception.DataFordelerException;
import dk.magenta.datafordeler.core.io.ImportInputStream;
import dk.magenta.datafordeler.core.io.ImportMetadata;
import dk.magenta.datafordeler.core.plugin.FtpCommunicator;
import dk.magenta.datafordeler.core.util.LabeledSequenceInputStream;
import dk.magenta.datafordeler.cpr.configuration.CprConfiguration;
import dk.magenta.datafordeler.cpr.configuration.CprConfigurationManager;
import dk.magenta.datafordeler.cpr.data.CprRecordEntityManager;
import dk.magenta.datafordeler.cpr.data.person.PersonEntity;
import dk.magenta.datafordeler.cpr.data.person.PersonEntityManager;
import dk.magenta.datafordeler.cpr.data.person.PersonRecordQuery;
import dk.magenta.datafordeler.cpr.data.person.PersonSubscription;
import dk.magenta.datafordeler.cpr.records.person.data.BirthPlaceDataRecord;
import org.apache.commons.io.FileUtils;
import org.hibernate.Session;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = Application.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class PullTest {

    @Autowired
    private CprPlugin plugin;

    @Autowired
    private Engine engine;

    @Autowired
    private SessionManager sessionManager;

    @Autowired
    private ObjectMapper objectMapper;

    @SpyBean
    private CprConfigurationManager cprConfigurationManager;

    @SpyBean
    private CprRegisterManager cprRegisterManager;

    @SpyBean
    private PersonEntityManager personEntityManager;

    @After
    public void cleanup() {
        QueryManager.clearCaches();
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

    /**
     * Load the GLBASETEST, which is the file with testpersons recieved from CPR-office
     *
     * @param importMetadata
     * @throws DataFordelerException
     * @throws IOException
     * @throws URISyntaxException
     */
    private void loadPersonWithOrigin(ImportMetadata importMetadata) throws DataFordelerException, IOException, URISyntaxException {
        InputStream testData1 = cprLoadTestdatasetTest.class.getResourceAsStream("/GLBASETEST");
        LabeledSequenceInputStream labeledInputStream = new LabeledSequenceInputStream("GLBASETEST", new ByteArrayInputStream("GLBASETEST".getBytes()), "GLBASETEST", testData1);
        ImportInputStream inputstream = new ImportInputStream(labeledInputStream);
        personEntityManager.parseData(inputstream, importMetadata);
        testData1.close();
    }

    @Test
    public void pull() throws Exception {

        CprConfiguration configuration = ((CprConfigurationManager) plugin.getConfigurationManager()).getConfiguration();
        when(cprConfigurationManager.getConfiguration()).thenReturn(configuration);

        CprRegisterManager registerManager = (CprRegisterManager) plugin.getRegisterManager();
        registerManager.setProxyString(null);

        doAnswer(new Answer<FtpCommunicator>() {
            @Override
            public FtpCommunicator answer(InvocationOnMock invocation) throws Throwable {
                FtpCommunicator ftpCommunicator = (FtpCommunicator) invocation.callRealMethod();
                ftpCommunicator.setSslSocketFactory(PullTest.getTrustAllSSLSocketFactory());
                return ftpCommunicator;
            }
        }).when(cprRegisterManager).getFtpCommunicator(any(URI.class), any(CprRecordEntityManager.class));


        String username = "test";
        String password = "test";


        InputStream personContents = this.getClass().getResourceAsStream("/persondata.txt");
        File personFile = File.createTempFile("persondata", "txt");
        personFile.createNewFile();
        FileUtils.copyInputStreamToFile(personContents, personFile);
        personContents.close();

        FtpService personFtp = new FtpService();
        int personPort = 2101;
        personFtp.startServer(username, password, personPort, Collections.singletonList(personFile));

        configuration.setPersonRegisterPasswordEncryptionFile(new File(PullTest.class.getClassLoader().getResource("keyfile.json").getFile()));
        configuration.setPersonRegisterType(CprConfiguration.RegisterType.REMOTE_FTP);
        configuration.setPersonRegisterFtpAddress("ftps://localhost:" + personPort);
        configuration.setPersonRegisterFtpUsername(username);
        configuration.setPersonRegisterFtpPassword(password);
        configuration.setPersonRegisterDataCharset(CprConfiguration.Charset.UTF_8);


        Pull pull = new Pull(engine, plugin);
        pull.run();

        personFtp.stopServer();
        personFile.delete();

        Session session = sessionManager.getSessionFactory().openSession();
        try {
            PersonRecordQuery personQuery = new PersonRecordQuery();
            personQuery.addFornavn("Tester");
            List<PersonEntity> personEntities = QueryManager.getAllEntities(session, personQuery, PersonEntity.class);
            Assert.assertEquals(1, personEntities.size());
            Assert.assertEquals(PersonEntity.generateUUID("0101001234"), personEntities.get(0).getUUID());
        } finally {
            session.close();
        }
    }

    @Test
    public void testConfiguredPull() throws Exception {
        CprConfiguration configuration = ((CprConfigurationManager) plugin.getConfigurationManager()).getConfiguration();
        when(cprConfigurationManager.getConfiguration()).thenReturn(configuration);

        CprRegisterManager registerManager = (CprRegisterManager) plugin.getRegisterManager();
        registerManager.setProxyString(null);

        doAnswer(new Answer<FtpCommunicator>() {
            @Override
            public FtpCommunicator answer(InvocationOnMock invocation) throws Throwable {
                FtpCommunicator ftpCommunicator = (FtpCommunicator) invocation.callRealMethod();
                ftpCommunicator.setSslSocketFactory(PullTest.getTrustAllSSLSocketFactory());
                return ftpCommunicator;
            }
        }).when(cprRegisterManager).getFtpCommunicator(any(URI.class), any(CprRecordEntityManager.class));


        String username = "test";
        String password = "test";


        InputStream personContents = this.getClass().getResourceAsStream("/persondata.txt");
        File personFile = File.createTempFile("persondata", "txt");
        personFile.createNewFile();
        FileUtils.copyInputStreamToFile(personContents, personFile);
        personContents.close();

        FtpService personFtp = new FtpService();
        int personPort = 2101;
        personFtp.startServer(username, password, personPort, Collections.singletonList(personFile));

        configuration.setPersonRegisterType(CprConfiguration.RegisterType.REMOTE_FTP);
        configuration.setPersonRegisterFtpAddress("ftps://localhost:" + personPort);
        configuration.setPersonRegisterFtpUsername(username);
        configuration.setPersonRegisterFtpPassword(password);
        configuration.setPersonRegisterDataCharset(CprConfiguration.Charset.UTF_8);

        ObjectNode config = (ObjectNode) objectMapper.readTree("{\"" + CprRecordEntityManager.IMPORTCONFIG_RECORDTYPE + "\": [5], \"remote\":true}");
        Pull pull = new Pull(engine, plugin, config);
        pull.run();

        personFtp.stopServer();
        personFile.delete();

        Session session = sessionManager.getSessionFactory().openSession();
        try {

            PersonRecordQuery personQuery = new PersonRecordQuery();
            personQuery.setPersonnummer("0101001234");
            List<PersonEntity> personEntities = QueryManager.getAllEntities(session, personQuery, PersonEntity.class);
            Assert.assertEquals(1, personEntities.size());
            PersonEntity personEntity = personEntities.get(0);
            Assert.assertEquals(PersonEntity.generateUUID("0101001234"), personEntity.getUUID());
            Set<BirthPlaceDataRecord> birthPlaceDataRecords = personEntity.getBirthPlace();
            Assert.assertEquals(1, birthPlaceDataRecords.size());
            BirthPlaceDataRecord birthPlaceDataRecord = birthPlaceDataRecords.iterator().next();
            Assert.assertTrue(OffsetDateTime.parse("1991-09-23T12:00+02:00").isEqual(birthPlaceDataRecord.getRegistrationFrom()));
            Assert.assertNull(birthPlaceDataRecord.getRegistrationTo());
            Assert.assertEquals(9510, birthPlaceDataRecord.getAuthority());
            Assert.assertEquals(1234, birthPlaceDataRecord.getBirthPlaceCode().intValue());
        } finally {
            session.close();
        }
    }

    @Test
    public void testSubscription() throws Exception {

        CprConfiguration configuration = ((CprConfigurationManager) plugin.getConfigurationManager()).getConfiguration();
        when(cprConfigurationManager.getConfiguration()).thenReturn(configuration);
        when(personEntityManager.isSetupSubscriptionEnabled()).thenReturn(true);
        when(personEntityManager.getCustomerId()).thenReturn(1234);
        when(personEntityManager.getJobId()).thenReturn(123456);
        //when(personEntityManager.getLastUpdated(any(Session.class))).thenReturn(null);
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
                return null;
            }
        }).when(personEntityManager).getLastUpdated(any(Session.class));

        File localCacheSubFolder = File.createTempFile("foo", "bar");
        localCacheSubFolder.delete();
        localCacheSubFolder.mkdirs();
        when(personEntityManager.getLocalSubscriptionFolder()).thenReturn(localCacheSubFolder.getAbsolutePath());

        CprRegisterManager registerManager = (CprRegisterManager) plugin.getRegisterManager();
        registerManager.setProxyString(null);

        doAnswer(new Answer<FtpCommunicator>() {
            @Override
            public FtpCommunicator answer(InvocationOnMock invocation) throws Throwable {
                FtpCommunicator ftpCommunicator = (FtpCommunicator) invocation.callRealMethod();
                ftpCommunicator.setSslSocketFactory(PullTest.getTrustAllSSLSocketFactory());
                return ftpCommunicator;
            }
        }).when(cprRegisterManager).getFtpCommunicator(any(URI.class), any(CprRecordEntityManager.class));


        String username = "test";
        String password = "test";


        int personPort = 2101;
        InputStream personContents = this.getClass().getResourceAsStream("/persondata.txt");
        File personFile = File.createTempFile("persondata", "txt");
        personFile.createNewFile();
        FileUtils.copyInputStreamToFile(personContents, personFile);
        personContents.close();

        FtpService personFtp = new FtpService();
        personFtp.startServer(username, password, personPort, Collections.singletonList(personFile));

        configuration.setPersonRegisterType(CprConfiguration.RegisterType.REMOTE_FTP);
        configuration.setPersonRegisterFtpAddress("ftps://localhost:" + personPort);
        configuration.setPersonRegisterFtpUsername(username);
        configuration.setPersonRegisterFtpPassword(password);
        configuration.setPersonRegisterDataCharset(CprConfiguration.Charset.UTF_8);

        configuration.setRoadRegisterType(CprConfiguration.RegisterType.DISABLED);
        configuration.setResidenceRegisterType(CprConfiguration.RegisterType.DISABLED);


        Pull pull = new Pull(engine, plugin);
        pull.run();


        personFtp.stopServer();
        personFile.delete();

        personContents = this.getClass().getResourceAsStream("/persondata2.txt");
        personFile = File.createTempFile("persondata2", "txt");
        personFile.createNewFile();
        FileUtils.copyInputStreamToFile(personContents, personFile);
        personContents.close();


        Session session = sessionManager.getSessionFactory().openSession();
        try {
            List<PersonSubscription> subscriptions = QueryManager.getAllItems(session, PersonSubscription.class);
            Assert.assertEquals(1, subscriptions.size());
        } finally {
            session.close();
        }

        personFtp.startServer(username, password, personPort, Collections.EMPTY_LIST);
        File localSubFolder = File.createTempFile("foo", "bar");

        try {
            personEntityManager.createSubscriptionFile();

            when(personEntityManager.getLocalSubscriptionFolder()).thenReturn(localSubFolder.getAbsolutePath());

            File[] subFiles = personFtp.getTempDir().listFiles();
            Assert.assertEquals(1, subFiles.length);
            String contents = FileUtils.readFileToString(subFiles[0]);
            Assert.assertEquals("06123400OP0101001234                                                            \r\n" +
                    "071234560101001234               ", contents);

            personFtp.stopServer();
        } finally {
            localSubFolder.delete();
        }
    }

    /**
     * Verify that testdata can be cleaned through calling pull with flag "cleantestdatafirst":true
     *
     * @throws Exception
     */
    @Test
    public void testCleanTestdataThroughPull() throws Exception {

        try (Session session = sessionManager.getSessionFactory().openSession()) {
            List<PersonEntity> personEntities = QueryManager.getAllEntities(session, PersonEntity.class);
            Assert.assertEquals(0, personEntities.size());//Validate that no persons is initiated in the beginning of this test
        }
        pull();//Pull 1 person from persondata
        try (Session session = sessionManager.getSessionFactory().openSession()) {
            List<PersonEntity> personEntities = QueryManager.getAllEntities(session, PersonEntity.class);
            Assert.assertEquals(1, personEntities.size());//Validate that 1 person from the file persondata is initiated
        }

        //Pull 39 persons from GLBASETEST
        try (Session session = sessionManager.getSessionFactory().openSession()) {
            ImportMetadata importMetadata = new ImportMetadata();
            importMetadata.setSession(session);
            this.loadPersonWithOrigin(importMetadata);
            session.close();
        }

        try (Session session = sessionManager.getSessionFactory().openSession()) {
            List<PersonEntity> personEntities = QueryManager.getAllEntities(session, PersonEntity.class);
            Assert.assertEquals(52, personEntities.size());//Validate that 52 persons is now initiated
        }

        //Clean the testdata
        ObjectNode config = (ObjectNode) objectMapper.readTree("{\"" + CprRecordEntityManager.IMPORTCONFIG_RECORDTYPE + "\": [5], \"cleantestdatafirst\":true}");
        Pull pull = new Pull(engine, plugin, config);
        pull.run();

        try (Session session = sessionManager.getSessionFactory().openSession()) {
            List<PersonEntity> personEntities = QueryManager.getAllEntities(session, PersonEntity.class);
            Assert.assertEquals(1, personEntities.size());//Validate that 1 person from the file persondata is initiated
        }
    }
}
