package dk.magenta.datafordeler.core.exception;

import dk.magenta.datafordeler.core.TestConfig;
import org.apache.http.HttpVersion;
import org.apache.http.StatusLine;
import org.apache.http.message.BasicStatusLine;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.net.URI;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = TestConfig.class)
public class HttpStatusExceptionTest {

    @Test
    public void testHttpStatusException() throws Exception {
        StatusLine statusLine = new BasicStatusLine(HttpVersion.HTTP_1_1, 200, "OK");
        URI uri = new URI("https://data.gl");
        HttpStatusException exception1 = new HttpStatusException(statusLine, uri);

        Assert.assertEquals(statusLine, exception1.getStatusLine());
        Assert.assertEquals(uri, exception1.getUri());
        Assert.assertEquals("datafordeler.import.http_status", exception1.getCode());

        HttpStatusException exception2 = new HttpStatusException(statusLine);
        Assert.assertEquals(statusLine, exception2.getStatusLine());
        Assert.assertEquals(null, exception2.getUri());
        Assert.assertEquals("datafordeler.import.http_status", exception2.getCode());
    }

}
