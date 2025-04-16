package dk.magenta.datafordeler.core.plugin;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import dk.magenta.datafordeler.core.exception.DataStreamException;
import dk.magenta.datafordeler.core.exception.HttpStatusException;
import dk.magenta.datafordeler.core.util.InputStreamReader;
import org.apache.hc.client5.http.classic.HttpClient;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.entity.EntityBuilder;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
/**
 * A special Communicator that fetches data over a HTTP connection by the
 * scan-scroll pattern: We specify the query in a POST, then get a handle back
 * that we can use in a series of subsequent GET requests to get all the data
 * (which tends to be a lot).
 */
public class ScanScrollCommunicator extends HttpCommunicator {

    public ScanScrollCommunicator() {
        this.recompilePattern();
    }

    public ScanScrollCommunicator(URI httpHost, String username, String password) {
        super(httpHost, username, password);
        this.recompilePattern();
    }

    public ScanScrollCommunicator(URI httpHost, File keystoreFile, String keystorePassword) {
        super(httpHost, keystoreFile, keystorePassword);
    }

    private final ObjectMapper objectMapper = new ObjectMapper();

    private String scrollIdJsonKey = "scroll_id";

    public void setScrollIdJsonKey(String scrollIdJsonKey) {
        this.scrollIdJsonKey = scrollIdJsonKey;
        this.recompilePattern();
    }

    private Pattern scrollIdPattern;

    private void recompilePattern() {
        this.scrollIdPattern = Pattern.compile("\"" + this.scrollIdJsonKey + "\":\\s*\"([^\"]+)\"");
    }

    public static char delimiter = '\n';


    private int throttle = 1000;

    public void setThrottle(int throttleMillis) {
        this.throttle = throttleMillis;
    }

    private Thread.UncaughtExceptionHandler uncaughtExceptionHandler = null;

    public void setUncaughtExceptionHandler(Thread.UncaughtExceptionHandler uncaughtExceptionHandler) {
        this.uncaughtExceptionHandler = uncaughtExceptionHandler;
    }

    private static final Logger log = LogManager.getLogger(ScanScrollCommunicator.class.getCanonicalName());


    private final Pattern emptyResultsPattern = Pattern.compile("\"hits\":\\s*\\[\\s*\\]");

    private final HashMap<InputStream, Thread> fetches = new HashMap();

    /**
     * Fetch data from the external source; sends a POST to the initialUri,
     * with the body, and waits for a response.
     * If all goes well, this response contains a scrollId somewhere in the
     * JSON, which is the handle we use on subsequent requests.
     * For the purposes of this project, we assume the response is JSON-encoded.
     * We then send further requests using the handle, expecting a handle in
     * each response until we are done. The full payload of all GET responses
     * is sent into the InputStream that we return.
     * This all happens in a thread, so you should get an InputStream returned immediately.
     */
    public InputStream fetch(URI initialUri, URI scrollUri, final String body) throws HttpStatusException, DataStreamException, URISyntaxException, IOException {
        this.setHost(initialUri);
        CloseableHttpClient httpclient = this.buildClient();

        final URI startUri = new URI(
                initialUri.getScheme(),
                initialUri.getUserInfo(),
                initialUri.getHost(),
                initialUri.getPort(),
                initialUri.getPath(),
                "search_type=query_then_fetch&scroll=1m",
                null
        );
        final URI endUri = new URI(
                scrollUri.getScheme(),
                scrollUri.getUserInfo(),
                scrollUri.getHost(),
                scrollUri.getPort(),
                scrollUri.getPath(),
                null,
                null
        );

        PipedInputStream inputStream = new PipedInputStream(); // Return this one
        BufferedOutputStream outputStream = new BufferedOutputStream(new PipedOutputStream(inputStream));
        final OutputStreamWriter writer = new OutputStreamWriter(outputStream, StandardCharsets.UTF_8);

        log.info("Streams created");
        Thread fetcher = new Thread(new Runnable() {
            @Override
            public void run() {
                HashSet<String> scrollIds = new HashSet<>();
                try {
                    JsonNode responseNode;
                    log.info("Scan-scroll fetching from " + startUri + " with body:\n" + body);

                    HttpPost initialPost = new HttpPost(startUri);
                    initialPost.setEntity(new StringEntity(body, StandardCharsets.UTF_8));
                    initialPost.setHeader("Content-Type", "application/json");
                    CloseableHttpResponse response;
                    log.info("Sending initial POST to " + startUri);
                    try {
                        response = httpclient.execute(initialPost);
                    } catch (IOException e) {
                        log.error(e);
                        throw new DataStreamException(e);
                    }
                    log.info("Initial POST sent");
                    log.info("HTTP status: " + response.getCode());
                    String content = InputStreamReader.readInputStream(response.getEntity().getContent());
                    if (response.getCode() != 200) {
                        log.error(content);
                        throw new HttpStatusException(response, startUri);
                    }

                    responseNode = objectMapper.readTree(content);
                    writer.append(content);
                    writer.flush();

                    String scrollId = responseNode.get(ScanScrollCommunicator.this.scrollIdJsonKey).asText();
                    if (scrollId != null) {
                        writer.append(delimiter);
                    }
                    while (scrollId != null) {
                        scrollIds.add(scrollId);

                        URI fetchUri = new URI(scrollUri.getScheme(), scrollUri.getUserInfo(), scrollUri.getHost(), scrollUri.getPort(), scrollUri.getPath(), "scroll=10m", null);
                        EntityBuilder entityBuilder = EntityBuilder.create();
                        entityBuilder.setContentType(ContentType.APPLICATION_JSON);
                        ObjectNode scrollObject = objectMapper.createObjectNode();
                        scrollObject.put("scroll", "10m");
                        scrollObject.put("scroll_id", scrollId);
                        entityBuilder.setText(scrollObject.toString());
                        HttpGet partialGet = new HttpGet(fetchUri);
                        Header[] headers = partialGet.getHeaders();
                        for (Header header : headers) {
                            System.out.println(header.getName() + ": " + header.getValue());
                        }
                        partialGet.setEntity(new StringEntity(entityBuilder.build().toString(), StandardCharsets.UTF_8));
                        try {
                            log.info("Sending chunk GET to " + fetchUri);
                            response = httpclient.execute(partialGet);
                            content = InputStreamReader.readInputStream(response.getEntity().getContent());
                        } catch (IOException e) {
                            throw new DataStreamException(e);
                        }

                        try {

                            Matcher m = ScanScrollCommunicator.this.emptyResultsPattern.matcher(content);
                            if (m.find()) {
                                log.info("Empty results encountered");
                                scrollId = null;
                            } else {
                                m = ScanScrollCommunicator.this.scrollIdPattern.matcher(content);
                                if (m.find()) {
                                    scrollId = m.group(1);
                                } else {
                                    scrollId = null;
                                    log.info("next scrollId not found");
                                }
                                writer.append(content);
                            }

                        } catch (IOException e) {
                            throw new DataStreamException(e);
                        }

                        if (scrollId != null) {
                            // There is more data
                            writer.append(delimiter);
                            writer.flush();
                            if (throttle > 0) {
                                try {
                                    log.info("Waiting " + throttle + " milliseconds before next request");
                                    Thread.sleep(throttle);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }
                        } else {
                            // Reached the end
                            break;
                        }
                        writer.flush();
                    }
                } catch (DataStreamException | IOException | URISyntaxException | HttpStatusException e) {
                    log.error(e);
                    throw new RuntimeException(e);
                } finally {
                    try {
                        log.info("Closing outputstream");
                        writer.flush();
                        writer.close();
                    } catch (IOException e1) {
                    }
                    ScanScrollCommunicator.this.fetches.remove(inputStream);

                    for (String s : scrollIds) {
                        try {
                            ScanScrollCommunicator.this.cleanupScrollId(httpclient, scrollUri, s);
                        } catch (URISyntaxException e) {
                        }
                    }
                }
            }
        });

        if (this.uncaughtExceptionHandler != null) {
            fetcher.setUncaughtExceptionHandler(this.uncaughtExceptionHandler);
        }
        this.fetches.put(inputStream, fetcher);

        fetcher.start();
        return inputStream;
    }

    public void wait(InputStream inputStream) {
        Thread fetcher = this.fetches.get(inputStream);
        if (fetcher != null) {
            try {
                fetcher.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void cleanupScrollId(HttpClient httpClient, URI scrollUri, String scrollId) throws URISyntaxException {
        final URI endUri = new URI(
                scrollUri.getScheme(),
                scrollUri.getUserInfo(),
                scrollUri.getHost(),
                scrollUri.getPort(),
                scrollUri.getPath(),
                null,
                null
        );
        HttpPost scrollDelete = new HttpPost(endUri) {
            @Override
            public String getMethod() {
                return "DELETE";
            }
        };
        ObjectNode objectNode = objectMapper.createObjectNode();
        objectNode.put("scroll_id", scrollId);
        scrollDelete.setEntity(new StringEntity(objectNode.toString(), StandardCharsets.UTF_8));
        scrollDelete.setHeader("Content-Type", "application/json");
        try {
            httpClient.execute(scrollDelete);
        } catch (IOException e) {
            log.error(e);
        }
    }
}
