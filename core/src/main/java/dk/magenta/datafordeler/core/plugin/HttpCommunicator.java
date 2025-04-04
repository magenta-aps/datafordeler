package dk.magenta.datafordeler.core.plugin;


import dk.magenta.datafordeler.core.exception.DataStreamException;
import dk.magenta.datafordeler.core.exception.HttpStatusException;
import org.apache.commons.io.FilenameUtils;
import org.apache.hc.client5.http.auth.CredentialsProvider;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.config.TlsConfig;
import org.apache.hc.client5.http.cookie.BasicCookieStore;
import org.apache.hc.client5.http.cookie.CookieStore;
import org.apache.hc.client5.http.entity.UrlEncodedFormEntity;
import org.apache.hc.client5.http.impl.auth.CredentialsProviderBuilder;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.client5.http.io.HttpClientConnectionManager;
import org.apache.hc.client5.http.ssl.DefaultClientTlsStrategy;
import org.apache.hc.client5.http.ssl.TlsSocketStrategy;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.HttpHost;
import org.apache.hc.core5.http.NameValuePair;
import org.apache.hc.core5.http.message.BasicNameValuePair;
import org.apache.hc.core5.http.ssl.TLS;
import org.apache.hc.core5.ssl.SSLContexts;
import org.apache.hc.core5.util.Timeout;

import javax.net.ssl.SSLContext;
import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.*;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Very basic Communicator that fetches a resource over HTTP,
 * and/or sends a payload on a POST request. Takes optional
 * username and password for authentication, and holds cookies
 * between requests.
 */
public class HttpCommunicator implements Communicator {

    private final CookieStore cookieStore = new BasicCookieStore();

    private String username;
    private String password;

    private File keystoreFile;
    private String keystorePassword;

    private HttpHost httpHost;

    public HttpCommunicator() {
    }

    public HttpCommunicator(URI httpHost, String username, String password) {
        this(httpHost, username, password, null, null);
    }

    public HttpCommunicator(URI httpHost, File keystoreFile, String keystorePassword) {
        this(httpHost, null, null, keystoreFile, keystorePassword);
    }

    public HttpCommunicator(URI httpHost, String username, String password, File keystoreFile, String keystorePassword) {
        try {
            this.httpHost = HttpHost.create(new URI(httpHost.getScheme(), null, httpHost.getHost(), httpHost.getPort(), null, null, null));
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
        this.username = username;
        this.password = password;
        this.keystoreFile = keystoreFile;
        this.keystorePassword = keystorePassword;
    }

    @Override
    public InputStream fetch(URI uri) throws HttpStatusException, DataStreamException {
        return this.get(uri, null);
    }

    public InputStream get(URI uri, Map<String, String> headers) throws DataStreamException, HttpStatusException {
        CloseableHttpClient httpclient = this.buildClient();
        HttpGet get = new HttpGet(uri);
        if (headers != null) {
            for (String key : headers.keySet()) {
                get.setHeader(key, headers.get(key));
            }
        }
        CloseableHttpResponse response;
        try {
            response = httpclient.execute(get);
        } catch (IOException e) {
            throw new DataStreamException(e);
        }
        if (response.getCode() != 200) {
            throw new HttpStatusException(response, uri);
        }
        try {
            return response.getEntity().getContent();
        } catch (IOException e) {
            throw new DataStreamException(e);
        }
    }

    public InputStream post(URI uri, Map<String, String> parameters, Map<String, String> headers) throws DataStreamException, HttpStatusException, UnsupportedEncodingException {
        ArrayList<NameValuePair> paramList = new ArrayList<>();
        for (String key : parameters.keySet()) {
            paramList.add(new BasicNameValuePair(key, parameters.get(key)));
        }
        return this.post(
                uri,
                new UrlEncodedFormEntity(paramList),
                headers
        );
    }

    public InputStream post(URI uri, HttpEntity body, Map<String, String> headers) throws DataStreamException, HttpStatusException {
        CloseableHttpClient httpclient = this.buildClient();
        HttpPost post = new HttpPost(uri);
        if (headers != null) {
            for (String key : headers.keySet()) {
                post.setHeader(key, headers.get(key));
            }
        }
        post.setEntity(body);
        try {
            CloseableHttpResponse response = httpclient.execute(post);
            if (response.getCode() != 200) {
                throw new HttpStatusException(response, uri);
            }
            return response.getEntity().getContent();
        } catch (IOException e) {
            throw new DataStreamException(e);
        }
    }

    protected CloseableHttpClient buildClient() throws DataStreamException {
        HttpClientBuilder httpclient = HttpClients.custom();


        if (this.username != null && this.password != null) {
            CredentialsProvider credentialsProvider = CredentialsProviderBuilder.create()
                    .add(this.httpHost, this.username, this.password.toCharArray()).build();
            httpclient.setDefaultCredentialsProvider(credentialsProvider);
        }

        if (this.keystoreFile != null && this.keystorePassword != null) {
            try {
                KeyStore keyStore = KeyStore.getInstance(
                        keystoreMap.get(FilenameUtils.getExtension(this.keystoreFile.getName()))
                );
                try (FileInputStream fileInputStream = new FileInputStream(this.keystoreFile)) {
                    keyStore.load(fileInputStream, this.keystorePassword.toCharArray());
                }
                SSLContext sslContext = SSLContexts.custom().loadKeyMaterial(keyStore, this.keystorePassword.toCharArray()).build();

                TlsSocketStrategy tlsStrategy = new DefaultClientTlsStrategy(sslContext);
                HttpClientConnectionManager connectionManager = PoolingHttpClientConnectionManagerBuilder.create()
                        .setTlsSocketStrategy(tlsStrategy)
                        .setDefaultTlsConfig(TlsConfig.custom()
                                .setHandshakeTimeout(Timeout.ofSeconds(30))
                                .setSupportedProtocols(TLS.V_1_2)
                                .build())
                        .build();
                httpclient.setConnectionManager(connectionManager);
            } catch (NoSuchAlgorithmException | CertificateException | KeyManagementException | IOException | KeyStoreException | UnrecoverableKeyException e) {
                throw new DataStreamException(e);
            }
        }

        httpclient.setDefaultCookieStore(this.cookieStore);
        return httpclient.build();
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setKeystoreFile(File keystoreFile) {
        this.keystoreFile = keystoreFile;
    }

    public void setKeystorePassword(String keystorePassword) {
        this.keystorePassword = keystorePassword;
    }

    private static final HashMap<String, String> keystoreMap = new HashMap<>();

    static {
        keystoreMap.put("jks", "jks");
        keystoreMap.put("p12", "pkcs12");
    }

}
