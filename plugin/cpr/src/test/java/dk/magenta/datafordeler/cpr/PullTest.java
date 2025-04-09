package dk.magenta.datafordeler.cpr;

import com.fasterxml.jackson.databind.node.ObjectNode;
import dk.magenta.datafordeler.core.Application;
import dk.magenta.datafordeler.core.Pull;
import dk.magenta.datafordeler.core.database.QueryManager;
import dk.magenta.datafordeler.core.exception.DataFordelerException;
import dk.magenta.datafordeler.core.io.ImportInputStream;
import dk.magenta.datafordeler.core.io.ImportMetadata;
import dk.magenta.datafordeler.core.plugin.FtpCommunicator;
import dk.magenta.datafordeler.core.util.LabeledSequenceInputStream;
import dk.magenta.datafordeler.cpr.configuration.CprConfiguration;
import dk.magenta.datafordeler.cpr.configuration.CprConfigurationManager;
import dk.magenta.datafordeler.cpr.data.CprRecordEntityManager;
import dk.magenta.datafordeler.cpr.data.person.PersonEntity;
import dk.magenta.datafordeler.cpr.data.person.PersonRecordQuery;
import dk.magenta.datafordeler.cpr.data.person.PersonSubscription;
import dk.magenta.datafordeler.cpr.records.person.data.BirthPlaceDataRecord;
import org.apache.commons.io.FileUtils;
import org.hibernate.Session;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.stubbing.Answer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;

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
import java.util.stream.Collectors;

import static org.mockito.Mockito.*;

@ContextConfiguration(classes = Application.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class PullTest extends TestBase {

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
        InputStream testData1 = CprLoadTestdatasetTest.class.getResourceAsStream("/GLBASETEST");
        LabeledSequenceInputStream labeledInputStream = new LabeledSequenceInputStream("GLBASETEST", new ByteArrayInputStream("GLBASETEST".getBytes()), "GLBASETEST", testData1);
        ImportInputStream inputstream = new ImportInputStream(labeledInputStream);
        personEntityManager.parseData(inputstream, importMetadata);
        testData1.close();
    }

    @BeforeEach
    public void setUp() {
        CprConfiguration configuration = ((CprConfigurationManager) plugin.getConfigurationManager()).getConfiguration();
        File localfolder = new File(configuration.getLocalCopyFolder());
        System.out.println(localfolder);
        if (localfolder.isDirectory()) {
            for (File f : localfolder.listFiles()) {
                f.delete();
            }
        }
    }


    @Test
    public void testPull() throws Exception {
        this.pull();
        Session session = sessionManager.getSessionFactory().openSession();
        try {
            PersonRecordQuery personQuery = new PersonRecordQuery();
            personQuery.setParameter(PersonRecordQuery.FORNAVNE, "Tester");
            List<PersonEntity> personEntities = QueryManager.getAllEntities(session, personQuery, PersonEntity.class);
            Assertions.assertEquals(1, personEntities.size());
            Assertions.assertEquals(PersonEntity.generateUUID("0101001234"), personEntities.get(0).getUUID());
        } finally {
            session.close();
        }
    }

    private void pull() throws Exception {
        this.pull(objectMapper.createObjectNode());
    }

    private void pull(ObjectNode importconfig) throws Exception {
        CprConfiguration configuration = ((CprConfigurationManager) plugin.getConfigurationManager()).getConfiguration();
        when(configurationManager.getConfiguration()).thenReturn(configuration);

        CprRegisterManager registerManager = (CprRegisterManager) plugin.getRegisterManager();
        registerManager.setProxyString(null);

        doAnswer((Answer<FtpCommunicator>) invocation -> {
            FtpCommunicator ftpCommunicator = (FtpCommunicator) invocation.callRealMethod();
            ftpCommunicator.setKeepFiles(false);
            ftpCommunicator.setSslSocketFactory(PullTest.getTrustAllSSLSocketFactory());
            return ftpCommunicator;
        }).when(registerManager).getFtpCommunicator(any(Session.class), any(URI.class), any(CprRecordEntityManager.class));


        String username = "test";
        String password = "test";

        InputStream personContents = this.getClass().getResourceAsStream("/persondata.txt");
        File personFile = File.createTempFile("persondata", "txt");
        personFile.createNewFile();
        FileUtils.copyInputStreamToFile(personContents, personFile);
        personContents.close();

        FtpService personFtp = new FtpService();
        int personPort = 2102;
        personFtp.startServer(username, password, personPort, Collections.singletonList(personFile));
        try {
            configuration.setPersonRegisterPasswordEncryptionFile(new File(PullTest.class.getClassLoader().getResource("keyfile.json").getFile()));
            configuration.setPersonRegisterType(CprConfiguration.RegisterType.REMOTE_FTP);
            configuration.setPersonRegisterFtpAddress("ftps://localhost:" + personPort);
            configuration.setPersonRegisterFtpDownloadFolder("/");
            configuration.setPersonRegisterFtpUsername(username);
            configuration.setPersonRegisterFtpPassword(password);
            configuration.setPersonRegisterDataCharset(CprConfiguration.Charset.UTF_8);
            Pull pull = new Pull(engine, plugin, importconfig);
            pull.run();
        } finally {
            personFtp.stopServer();
        }
        personFile.delete();
    }

    @Test
    public void testConfiguredPull() throws Exception {
        CprConfiguration configuration = ((CprConfigurationManager) plugin.getConfigurationManager()).getConfiguration();
        when(configurationManager.getConfiguration()).thenReturn(configuration);

        CprRegisterManager registerManager = (CprRegisterManager) plugin.getRegisterManager();
        registerManager.setProxyString(null);

        when(registerManager.getFtpCommunicator(any(Session.class), any(URI.class), any(CprRecordEntityManager.class)))
                .thenAnswer(
                        (Answer<FtpCommunicator>) invocation -> {
                            FtpCommunicator ftpCommunicator = (FtpCommunicator) invocation.callRealMethod();
                            ftpCommunicator.setSslSocketFactory(PullTest.getTrustAllSSLSocketFactory());
                            return ftpCommunicator;
                        }
        );

        String username = "test";
        String password = "test";

        InputStream personContents = this.getClass().getResourceAsStream("/persondata.txt");
        File personFile = File.createTempFile("persondata", "txt");
        personFile.createNewFile();
        FileUtils.copyInputStreamToFile(personContents, personFile);
        personContents.close();

        FtpService personFtp = new FtpService();
        int personPort = 2106;
        personFtp.startServer(username, password, personPort, Collections.singletonList(personFile));
        try {
            configuration.setPersonRegisterType(CprConfiguration.RegisterType.REMOTE_FTP);
            configuration.setPersonRegisterFtpAddress("ftps://localhost:" + personPort);
            configuration.setPersonRegisterFtpDownloadFolder("/");
            configuration.setPersonRegisterFtpUsername(username);
            configuration.setPersonRegisterFtpPassword(password);
            configuration.setPersonRegisterDataCharset(CprConfiguration.Charset.UTF_8);
            ObjectNode config = (ObjectNode) objectMapper.readTree("{\"" + CprRecordEntityManager.IMPORTCONFIG_RECORDTYPE + "\": [5], \"remote\":true}");
            Pull pull = new Pull(engine, plugin, config);
            pull.run();
        } finally {
            personFtp.stopServer();
        }
        personFile.delete();

        Session session = sessionManager.getSessionFactory().openSession();
        try {

            PersonRecordQuery personQuery = new PersonRecordQuery();
            personQuery.setParameter(PersonRecordQuery.PERSONNUMMER, "0101001234");
            List<PersonEntity> personEntities = QueryManager.getAllEntities(session, personQuery, PersonEntity.class);
            Assertions.assertEquals(1, personEntities.size());
            PersonEntity personEntity = personEntities.get(0);
            Assertions.assertEquals(PersonEntity.generateUUID("0101001234"), personEntity.getUUID());

            Set<BirthPlaceDataRecord> birthPlaceDataRecords = personEntity.getBirthPlace();
            Assertions.assertEquals(1, birthPlaceDataRecords.size());
            BirthPlaceDataRecord birthPlaceDataRecord = birthPlaceDataRecords.iterator().next();
            System.out.println("birthPlaceDataRecord.getRegistrationFrom(): "+birthPlaceDataRecord.getRegistrationFrom());
            Assertions.assertTrue(OffsetDateTime.parse("1991-09-23T12:00+02:00").isEqual(birthPlaceDataRecord.getRegistrationFrom()));
            Assertions.assertNull(birthPlaceDataRecord.getRegistrationTo());
            Assertions.assertEquals(9510, birthPlaceDataRecord.getAuthority());
            Assertions.assertEquals(1234, birthPlaceDataRecord.getBirthPlaceCode().intValue());
        } finally {
            session.close();
        }
    }

    @Test
    public void testSubscription() throws Exception {

        CprConfiguration configuration = ((CprConfigurationManager) plugin.getConfigurationManager()).getConfiguration();
        doReturn(configuration).when(configurationManager).getConfiguration();
        doReturn(true).when(personEntityManager).isSetupSubscriptionEnabled();
        doReturn(1234).when(personEntityManager).getCustomerId();
        doReturn(123456).when(personEntityManager).getJobId();
        doAnswer(invocationOnMock -> null).when(personEntityManager).getLastUpdated(any(Session.class));

        File localCacheSubFolder = File.createTempFile("foo", "bar");
        localCacheSubFolder.delete();
        localCacheSubFolder.mkdirs();
        doReturn(localCacheSubFolder.getAbsolutePath()).when(personEntityManager).getLocalSubscriptionFolder();

        CprRegisterManager registerManager = (CprRegisterManager) plugin.getRegisterManager();
        registerManager.setProxyString(null);

        doAnswer((Answer<FtpCommunicator>) invocation -> {
            FtpCommunicator ftpCommunicator = (FtpCommunicator) invocation.callRealMethod();
            ftpCommunicator.setSslSocketFactory(PullTest.getTrustAllSSLSocketFactory());
            return ftpCommunicator;
        }).when(registerManager).getFtpCommunicator(any(), any(URI.class), any(CprRecordEntityManager.class));

        String username = "test";
        String password = "test";

        int personPort = 2107;
        InputStream personContents = this.getClass().getResourceAsStream("/persondata.txt");
        File personFile = File.createTempFile("persondata", "txt");
        personFile.createNewFile();
        FileUtils.copyInputStreamToFile(personContents, personFile);
        personContents.close();

        FtpService personFtp = new FtpService();
        personFtp.startServer(username, password, personPort, Collections.singletonList(personFile));
        try {
            configuration.setPersonRegisterPasswordEncryptionFile(new File(PullTest.class.getClassLoader().getResource("keyfile.json").getFile()));
            configuration.setPersonRegisterType(CprConfiguration.RegisterType.REMOTE_FTP);
            configuration.setPersonRegisterFtpAddress("ftps://localhost:" + personPort);
            configuration.setPersonRegisterFtpDownloadFolder("/");
            configuration.setPersonRegisterFtpUsername(username);
            configuration.setPersonRegisterFtpPassword(password);
            configuration.setPersonRegisterDataCharset(CprConfiguration.Charset.UTF_8);
            configuration.setRoadRegisterType(CprConfiguration.RegisterType.DISABLED);
            configuration.setResidenceRegisterType(CprConfiguration.RegisterType.DISABLED);
            Pull pull = new Pull(engine, plugin);
            pull.run();
        } finally {
            personFtp.stopServer();
        }
        personFile.delete();

        Session session = sessionManager.getSessionFactory().openSession();
        try {
            List<PersonSubscription> subscriptions = QueryManager.getAllItems(session, PersonSubscription.class);
            Assertions.assertEquals(1, subscriptions.size());


            personFtp.startServer(username, password, personPort, Collections.EMPTY_LIST);
            File localSubFolder = File.createTempFile("foo", "bar");

            try {
                personEntityManager.createSubscriptionFile(session);
                doReturn(localSubFolder.getAbsolutePath()).when(personEntityManager).getLocalSubscriptionFolder();
                File[] subFiles = personFtp.getTempDir().listFiles();
                Assertions.assertEquals(1, subFiles.length);
                String contents = FileUtils.readFileToString(subFiles[0]);
                Assertions.assertEquals("06123400OP0101001234                                                            \r\n" +
                        "071234560101001234               ", contents);
            } finally {
                personFtp.stopServer();
                localSubFolder.delete();
            }
        } finally {
            session.close();
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
            Assertions.assertEquals(0, personEntities.size());//Validate that no persons is initiated in the beginning of this test
        }
        pull();  // Pull 1 person from persondata
        try (Session session = sessionManager.getSessionFactory().openSession()) {
            List<PersonEntity> personEntities = QueryManager.getAllEntities(session, PersonEntity.class);
            Assertions.assertEquals(1, personEntities.size());//Validate that 1 person from the file persondata is initiated
        }

        //Pull 39 persons from GLBASETEST
        try (Session session = sessionManager.getSessionFactory().openSession()) {
            ImportMetadata importMetadata = new ImportMetadata();
            importMetadata.setSession(session);
            this.loadPersonWithOrigin(importMetadata);
        }

        try (Session session = sessionManager.getSessionFactory().openSession()) {
            List<PersonEntity> personEntities = QueryManager.getAllEntities(session, PersonEntity.class);
            Assertions.assertEquals(52, personEntities.size());//Validate that 52 persons is now initiated
        }

        //Clean the testdata
        ObjectNode config = (ObjectNode) objectMapper.readTree("{\"" + CprRecordEntityManager.IMPORTCONFIG_RECORDTYPE + "\": [5], \"cleantestdatafirst\":true}");
        pull(config);

        try (Session session = sessionManager.getSessionFactory().openSession()) {
            List<PersonEntity> personEntities = QueryManager.getAllEntities(session, PersonEntity.class);
            String cprs = personEntities.stream().map(p -> p.getPersonnummer()).collect(Collectors.joining(","));
            Assertions.assertEquals(1, personEntities.size(), "Got "+cprs+" when expecting only 0101001234");//Validate that 1 person from the file persondata is initiated
        }
    }
}
