package dk.magenta.datafordeler.core.exception;

import dk.magenta.datafordeler.core.Application;
import org.apache.http.HttpVersion;
import org.apache.http.StatusLine;
import org.apache.http.message.BasicStatusLine;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ContextConfiguration;

import java.net.URI;


@ContextConfiguration(classes = Application.class)
public class HttpStatusExceptionTest {

    @Test
    public void testHttpStatusException() throws Exception {
        StatusLine statusLine = new BasicStatusLine(HttpVersion.HTTP_1_1, 200, "OK");
        URI uri = new URI("https://data.gl");
        HttpStatusException exception1 = new HttpStatusException(statusLine, uri);

        Assertions.assertEquals(statusLine, exception1.getStatusLine());
        Assertions.assertEquals(uri, exception1.getUri());
        Assertions.assertEquals("datafordeler.import.http_status", exception1.getCode());

        HttpStatusException exception2 = new HttpStatusException(statusLine);
        Assertions.assertEquals(statusLine, exception2.getStatusLine());
        Assertions.assertEquals(null, exception2.getUri());
        Assertions.assertEquals("datafordeler.import.http_status", exception2.getCode());
    }

}
