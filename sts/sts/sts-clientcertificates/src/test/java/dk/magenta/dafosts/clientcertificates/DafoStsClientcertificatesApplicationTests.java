package dk.magenta.dafosts.clientcertificates;

import dk.magenta.dafosts.library.DafoTokenGenerator;
import dk.magenta.dafosts.library.DatabaseQueryManager;
import org.apache.http.client.HttpClient;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContextBuilder;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.ResourceAccessException;

import javax.annotation.PostConstruct;
import java.io.File;
import java.security.KeyStore;
import java.util.Arrays;
import java.util.List;

import static dk.magenta.dafosts.library.DafoTokenGenerator.ON_BEHALF_OF_CLAIM_URL;
import static dk.magenta.dafosts.library.DafoTokenGenerator.USERPROFILE_CLAIM_URL;
import static dk.magenta.dafosts.library.DatabaseQueryManager.IDENTIFICATION_MODE_ON_BEHALF_OF;
import static dk.magenta.dafosts.library.DatabaseQueryManager.IDENTIFICATION_MODE_SINGLE_USER;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.when;


@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class DafoStsClientcertificatesApplicationTests {

    public static List<String> DUMMY_USER_PROFILES = Arrays.asList(new String[] {"UserProfile1", "UserProfile2"});

    @Value("${server.ssl.trust-store}")
    String trustStorePath;

    @Value("${server.ssl.trust-store-password}")
    String trustStorePassword;

    static int MOCK_USERID = -42;

    @Autowired
    private TestRestTemplate restTemplate;

    @MockBean
    private DatabaseQueryManager dbManager;

    @Autowired
    private DafoTokenGenerator dafoTokenGenerator;

    public KeyStore getClientKeyStore() throws Exception {
        ClassPathResource clientCertificate = new ClassPathResource("client.p12");
        KeyStore clientKeystore = KeyStore.getInstance("pkcs12");
        clientKeystore.load(clientCertificate.getInputStream(), "dafo".toCharArray());

        return clientKeystore;
    }

    public SSLConnectionSocketFactory getSSLConnectionSocketFactory(boolean withClientCertificate) throws Exception {
        SSLContextBuilder ctxBuilder = new SSLContextBuilder();
        // Trust our own certificate and any self-signed one
        ctxBuilder.loadTrustMaterial(
                new File(trustStorePath),
                trustStorePassword.toCharArray(),
                new TrustSelfSignedStrategy()
        );

        if(withClientCertificate) {
            ctxBuilder.loadKeyMaterial(getClientKeyStore(), "dafo".toCharArray());
        }

        return new SSLConnectionSocketFactory(ctxBuilder.build());
    }

    @BeforeClass
    public static void setUp() throws Exception {
        // Boostrap the Opensaml Library to use default configuration
        org.opensaml.DefaultBootstrap.bootstrap();
    }

    @PostConstruct
    public void postContruct() throws Exception {
        // Configure the restTemplate to use a HTTPS transport that uses a client certificate and allows connection
        // to a https server with a self-signed certificate
        SSLConnectionSocketFactory socketFactory = getSSLConnectionSocketFactory(true);
        HttpClient httpClient = HttpClients.custom().setSSLSocketFactory(socketFactory).build();

        ((HttpComponentsClientHttpRequestFactory) restTemplate.getRestTemplate().getRequestFactory()).setHttpClient(
                httpClient
        );
    }

	@Test
	public void contextLoads() {
	}

	@Test(expected = ResourceAccessException.class)
	public void rejectRequestWithoutCertificate() throws Exception {
	    SSLConnectionSocketFactory socketFactory = getSSLConnectionSocketFactory(false);
        HttpClient httpClient = HttpClients.custom().setSSLSocketFactory(socketFactory).build();

        ((HttpComponentsClientHttpRequestFactory) restTemplate.getRestTemplate().getRequestFactory()).setHttpClient(
                httpClient
        );

        ResponseEntity<String> response = restTemplate.getForEntity("/get_token", String.class);
	}

    @Test
    public void issueTokenFromCertificate() throws Exception {
        when(dbManager.getUserIdByCertificateData(anyString())).thenReturn(MOCK_USERID);
        when(dbManager.isAccessAccountActive(anyInt())).thenReturn(true);
        when(dbManager.getUserIdentificationByAccountId(MOCK_USERID)).thenReturn("jubk@magenta.dk");
        when(dbManager.getUserProfiles(MOCK_USERID)).thenReturn(DUMMY_USER_PROFILES);
        when(dbManager.getCertUserIdentificationMode(MOCK_USERID)).thenReturn(IDENTIFICATION_MODE_SINGLE_USER);


        ResponseEntity<String> response = restTemplate.getForEntity("/get_token", String.class);
        assertThat(response.getStatusCodeValue()).isEqualTo(200);

        String rawToken = dafoTokenGenerator.decodeAndInflate(response.getBody());
        assertThat(rawToken).contains("jubk@magenta.dk");
        assertThat(rawToken).contains(USERPROFILE_CLAIM_URL);
        assertThat(rawToken).contains(">UserProfile1<");
    }

    @Test
    public void rejectInactiveUser() throws Exception {
        when(dbManager.getUserIdByCertificateData(anyString())).thenReturn(MOCK_USERID);
        when(dbManager.isAccessAccountActive(anyInt())).thenReturn(false);
        when(dbManager.getUserIdentificationByAccountId(MOCK_USERID)).thenReturn("jubk@magenta.dk");
        when(dbManager.getUserProfiles(MOCK_USERID)).thenReturn(DUMMY_USER_PROFILES);
        when(dbManager.getCertUserIdentificationMode(MOCK_USERID)).thenReturn(IDENTIFICATION_MODE_SINGLE_USER);


        ResponseEntity<String> response = restTemplate.getForEntity("/get_token", String.class);
        assertThat(response.getStatusCodeValue()).isEqualTo(403);
        assertThat(response.getBody().toString()).contains("is not active");
    }

    @Test
    public void issueTokenOnBehalfOf() throws Exception {
        when(dbManager.getUserIdByCertificateData(anyString())).thenReturn(MOCK_USERID);
        when(dbManager.isAccessAccountActive(anyInt())).thenReturn(true);
        when(dbManager.getUserIdentificationByAccountId(MOCK_USERID)).thenReturn("jubk@magenta.dk");
        when(dbManager.getUserProfiles(MOCK_USERID)).thenReturn(DUMMY_USER_PROFILES);
        when(dbManager.getCertUserIdentificationMode(MOCK_USERID)).thenReturn(IDENTIFICATION_MODE_ON_BEHALF_OF);

        ResponseEntity<String> response = restTemplate.getForEntity(
                "/get_token?on_behalf_of=testuser",
                String.class
        );
        assertThat(response.getStatusCodeValue()).isEqualTo(200);

        String rawToken = dafoTokenGenerator.decodeAndInflate(response.getBody());
        assertThat(rawToken).contains("jubk@magenta.dk");
        assertThat(rawToken).contains(USERPROFILE_CLAIM_URL);
        assertThat(rawToken).contains(">UserProfile1<");
        assertThat(rawToken).contains(ON_BEHALF_OF_CLAIM_URL);
        assertThat(rawToken).contains(">testuser<");
    }

    @Test
    public void rejectOnBehalfOfForSingleUser() {
        when(dbManager.getUserIdByCertificateData(anyString())).thenReturn(MOCK_USERID);
        when(dbManager.isAccessAccountActive(anyInt())).thenReturn(true);
        when(dbManager.getUserProfiles(MOCK_USERID)).thenReturn(DUMMY_USER_PROFILES);
        when(dbManager.getCertUserIdentificationMode(MOCK_USERID)).thenReturn(IDENTIFICATION_MODE_SINGLE_USER);

        ResponseEntity<String> response = restTemplate.getForEntity(
                "/get_token?on_behalf_of=testuser",
                String.class
        );
        assertThat(response.getStatusCodeValue()).isEqualTo(403);
    }

    @Test
    public void rejectEmptyOnBehalfOf() {
        when(dbManager.getUserIdByCertificateData(anyString())).thenReturn(MOCK_USERID);
        when(dbManager.isAccessAccountActive(anyInt())).thenReturn(true);
        when(dbManager.getUserProfiles(MOCK_USERID)).thenReturn(
                Arrays.asList(new String[] {"UserProfile1", "UserProfile2"})
        );
        when(dbManager.getCertUserIdentificationMode(MOCK_USERID)).thenReturn(IDENTIFICATION_MODE_ON_BEHALF_OF);

        ResponseEntity<String> response = restTemplate.getForEntity("/get_token", String.class);
        assertThat(response.getStatusCodeValue()).isEqualTo(403);
    }

}


