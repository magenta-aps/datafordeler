package dk.magenta.datafordeler.core.plugin;


import dk.magenta.datafordeler.core.exception.DataFordelerException;
import dk.magenta.datafordeler.core.exception.DataStreamException;
import dk.magenta.datafordeler.core.exception.HttpStatusException;
import org.apache.commons.io.FilenameUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.StatusLine;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CookieStore;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.*;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.ssl.SSLContexts;

import javax.net.ssl.SSLContext;
import java.io.*;
import java.net.URI;
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

    public HttpCommunicator() {
    }

    public HttpCommunicator(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public HttpCommunicator(File keystoreFile, String keystorePassword) {
        this.keystoreFile = keystoreFile;
        this.keystorePassword = keystorePassword;
    }

    public HttpCommunicator(String username, String password, File keystoreFile, String keystorePassword) {
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
        StatusLine statusLine = response.getStatusLine();
        if (statusLine.getStatusCode() != 200) {
            throw new HttpStatusException(statusLine, uri);
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
            HttpResponse response = httpclient.execute(post);
            StatusLine statusLine = response.getStatusLine();
            if (statusLine.getStatusCode() != 200) {
                throw new HttpStatusException(statusLine, uri);
            }
            return response.getEntity().getContent();
        } catch (IOException e) {
            throw new DataStreamException(e);
        }
    }

    @Override
    public StatusLine send(URI endpoint, String payload) throws DataFordelerException {
        CloseableHttpClient httpclient = this.buildClient();
        HttpPost request = new HttpPost(endpoint);
        try {
            request.setEntity(new StringEntity(payload));
            // TODO: Do this in a thread?
            return httpclient.execute(request).getStatusLine();
        } catch (IOException e) {
            throw new DataStreamException(e);
        }
    }

    protected CloseableHttpClient buildClient() throws DataStreamException {
        HttpClientBuilder builder = HttpClients.custom();

        if (this.username != null && this.password != null) {
            CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
            credentialsProvider.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(this.username, this.password));
            builder.setDefaultCredentialsProvider(credentialsProvider);
        }

        if (this.keystoreFile != null && this.keystorePassword != null) {
            try {
                KeyStore keyStore = KeyStore.getInstance(
                        keystoreMap.get(FilenameUtils.getExtension(this.keystoreFile.getName()))
                );
                try (FileInputStream fileInputStream = new FileInputStream(this.keystoreFile)) {
                    keyStore.load(fileInputStream, this.keystorePassword.toCharArray());
                }
                SSLContextBuilder sslBuilder = SSLContexts.custom().loadKeyMaterial(
                        keyStore,
                        this.keystorePassword.toCharArray()
                );
                SSLContext sslContext = sslBuilder.build();
                SSLConnectionSocketFactory socketFactory = new SSLConnectionSocketFactory(
                        sslContext,
                        new String[]{"TLSv1.2"},
                        null,
                        SSLConnectionSocketFactory.getDefaultHostnameVerifier()
                );
                builder.setSSLSocketFactory(socketFactory);
            } catch (NoSuchAlgorithmException | CertificateException | KeyManagementException | IOException | KeyStoreException | UnrecoverableKeyException e) {
                throw new DataStreamException(e);
            }
        }

        builder.setDefaultCookieStore(this.cookieStore);
        return builder.build();
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
