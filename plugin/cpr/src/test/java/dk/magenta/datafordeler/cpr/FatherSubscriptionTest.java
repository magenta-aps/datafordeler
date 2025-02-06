package dk.magenta.datafordeler.cpr;

import dk.magenta.datafordeler.core.Application;
import dk.magenta.datafordeler.core.Pull;
import dk.magenta.datafordeler.core.database.QueryManager;
import dk.magenta.datafordeler.core.plugin.FtpCommunicator;
import dk.magenta.datafordeler.cpr.configuration.CprConfiguration;
import dk.magenta.datafordeler.cpr.configuration.CprConfigurationManager;
import dk.magenta.datafordeler.cpr.data.CprRecordEntityManager;
import dk.magenta.datafordeler.cpr.data.person.PersonSubscription;
import org.apache.commons.io.FileUtils;
import org.hibernate.Session;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.stubbing.Answer;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.File;
import java.io.InputStream;
import java.net.URI;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.*;


@ContextConfiguration(classes = Application.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class FatherSubscriptionTest extends TestBase {

    private static SSLSocketFactory getTrustAllSSLSocketFactory() throws NoSuchAlgorithmException, KeyManagementException {
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

        sslContext = SSLContext.getInstance("SSL");
        sslContext.init(null, trustManager, new SecureRandom());

        return sslContext.getSocketFactory();
    }


    @Test
    public void testParentSubscription() throws Exception {

        Session session = sessionManager.getSessionFactory().openSession();
        Assertions.assertEquals(0, QueryManager.getAllItems(session, PersonSubscription.class).size());

        CprConfiguration configuration = ((CprConfigurationManager) plugin.getConfigurationManager()).getConfiguration();
        doReturn(configuration).when(configurationManager).getConfiguration();
        doReturn(true).when(personEntityManager).isSetupSubscriptionEnabled();
        doReturn(1234).when(personEntityManager).getCustomerId();
        doReturn(123456).when(personEntityManager).getJobId();
        doAnswer(invocationOnMock -> null).when(personEntityManager).getLastUpdated(any(Session.class));

        File localSubFolder = File.createTempFile("foo", "bar");
        localSubFolder.delete();
        localSubFolder.mkdirs();
        doReturn(localSubFolder.getAbsolutePath()).when(personEntityManager).getLocalSubscriptionFolder();

        CprRegisterManager registerManager = (CprRegisterManager) plugin.getRegisterManager();
        registerManager.setProxyString(null);

        doAnswer((Answer<FtpCommunicator>) invocation -> {
            FtpCommunicator ftpCommunicator = (FtpCommunicator) invocation.callRealMethod();
            ftpCommunicator.setSslSocketFactory(FatherSubscriptionTest.getTrustAllSSLSocketFactory());
            return ftpCommunicator;
        }).when(registerManager).getFtpCommunicator(any(Session.class), any(URI.class), any(CprRecordEntityManager.class));

        String username = "test";
        String password = "test";

        int personPort = 2105;
        InputStream personContents = this.getClass().getResourceAsStream("/personWithParentRelation.txt");
        File personFile = File.createTempFile("persondata", "txt");
        personFile.createNewFile();
        FileUtils.copyInputStreamToFile(personContents, personFile);
        personContents.close();

        FtpService personFtp = new FtpService();
        personFtp.startServer(username, password, personPort, Collections.singletonList(personFile));
        Pull pull = new Pull(engine, plugin);
        try {
            configuration.setPersonRegisterType(CprConfiguration.RegisterType.REMOTE_FTP);
            configuration.setPersonRegisterFtpAddress("ftps://localhost:" + personPort);
            configuration.setPersonRegisterFtpUsername(username);
            configuration.setPersonRegisterFtpPassword(password);
            configuration.setPersonRegisterDataCharset(CprConfiguration.Charset.UTF_8);
            configuration.setRoadRegisterType(CprConfiguration.RegisterType.DISABLED);
            configuration.setResidenceRegisterType(CprConfiguration.RegisterType.DISABLED);
            pull.run();
        } finally {
            personFtp.stopServer();
        }

        personFile.delete();

        personContents = this.getClass().getResourceAsStream("/personWithParentRelation.txt");
        personFile = File.createTempFile("persondata2", "txt");
        personFile.createNewFile();
        FileUtils.copyInputStreamToFile(personContents, personFile);
        personContents.close();

        personFtp = new FtpService();
        personFtp.startServer(username, password, personPort, Collections.singletonList(personFile));
        try {
            pull.run();
        } finally {
            personFtp.stopServer();
        }
        personFile.delete();

        try {
            List<PersonSubscription> subscriptions = QueryManager.getAllItems(session, PersonSubscription.class);
            //There is 5 fathers in the test-file
            //One should not be added to subscription becrause the child is more than 18 years old.
            //One person should not be added becrause the father allready exists as a person.
            Assertions.assertEquals(3, subscriptions.size());
        } finally {
            session.close();
            localSubFolder.delete();
        }
    }

    /**
     * Validate that subscription can handle collections that is unmodifiable
     *
     * @throws Exception
     */
    @Test
    public void testSubscriptionCreation() throws Exception {
        personEntityManager.createSubscription(Collections.singleton("1111111111"), Collections.singleton("1111111111"));
    }

}
