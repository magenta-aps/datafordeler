package dk.magenta.datafordeler.core.gapi;

import dk.magenta.datafordeler.core.database.QueryManager;
import dk.magenta.datafordeler.core.database.SessionManager;
import dk.magenta.datafordeler.core.testutil.CallbackController;
import dk.magenta.datafordeler.plugindemo.model.DemoEntityRecord;
import jakarta.xml.bind.DatatypeConverter;
import org.apache.commons.io.IOUtils;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.OffsetDateTime;
import java.util.UUID;


// 
public abstract class GapiTestBase {

    @Autowired
    protected TestRestTemplate restTemplate;

    @Autowired
    protected CallbackController callbackController;

    @Autowired
    private SessionManager sessionManager;

    protected static final String INTERFACE_PATH = "/odata/gapi/Events";

    protected String envelopData(String schema, String data) throws IOException {
        String template = IOUtils.toString(GapiTestBase.class.getResourceAsStream("/gapitest_dataenvelope.json"), StandardCharsets.UTF_8);
        return template.replace("%{skema}", schema).replace("%{data}", data.replace("\"", "\\\""));
    }

    protected String envelopReference(String schema, String reference) throws IOException {
        String template = IOUtils.toString(GapiTestBase.class.getResourceAsStream("/gapitest_referenceenvelope.json"), StandardCharsets.UTF_8);
        return template.replace("%{skema}", schema).replace("%{reference}", reference.replace("\"", "\\\""));
    }

    protected HttpHeaders getHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }

    protected String getPayload(String resourceName) throws IOException {
        return IOUtils.toString(GapiTestBase.class.getResourceAsStream(resourceName), StandardCharsets.UTF_8).strip();
    }

    protected String getBody(String schema, String resourceName) throws IOException {
        return this.envelopData(schema, this.getPayload(resourceName));
    }

    protected String hash(String input) {
        MessageDigest md = null;
        try {
            md = MessageDigest.getInstance("SHA-256");
            md.update(input.getBytes());
            byte[] digest = md.digest();
            return DatatypeConverter.printHexBinary(digest);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return null;
    }

    protected void deleteEntity(UUID uuid) {
        this.deleteEntity(uuid.toString());
    }

    protected void deleteEntity(String uuid) {
        Session session = this.sessionManager.getSessionFactory().openSession();
        try {
            Transaction transaction = session.beginTransaction();
            DemoEntityRecord entity = QueryManager.getEntity(session, UUID.fromString(uuid), DemoEntityRecord.class);
            if (entity != null) {
                session.remove(entity);
            }
            transaction.commit();
        } finally {
            session.close();
        }
    }


    // Because code doesn't execute instantaneously, there are often times when we wait for one second,
    // perform a time-sensitive check on whether a job has run, and it turns out that a little over one
    // second has passed, making the job run one more time than we expected, failing the test
    // E.g. start a x.998, wait 1000ms, check job. But now it's (x+2).001 (instead of (x+1).998), and the job has run three times, where we expected two
    // Solution: always start at around x.500
    protected void waitToMilliseconds(int millis, int tolerance) throws
        InterruptedException {
        int current = OffsetDateTime.now().getNano() / 1000000;
        int wait = 0;
        if (current > millis + tolerance) {
            wait = 1000 + millis - current;
        } else if (current < millis - tolerance) {
            wait = millis - current;
        }
        Thread.sleep(wait);
    }}
