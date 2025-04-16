package dk.magenta.datafordeler.core.plugin;

import dk.magenta.datafordeler.core.Application;
import dk.magenta.datafordeler.core.exception.DataStreamException;
import dk.magenta.datafordeler.core.exception.HttpStatusException;
import dk.magenta.datafordeler.core.util.InputStreamReader;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.HttpResponse;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ContextConfiguration;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;

@ContextConfiguration(classes = Application.class)
public class HttpCommunicatorTest {

    @Test
    public void testFetchOk() throws URISyntaxException, HttpStatusException, DataStreamException, IOException {
        Communicator communicator = new HttpCommunicator();
        InputStream data = communicator.fetch(new URI("https://www.example.com"));
        Assertions.assertNotNull(data);
        Assertions.assertNotNull(data.read(new byte[10]));
    }

    @Test
    public void testFetchFail1() throws URISyntaxException, HttpStatusException, DataStreamException, IOException {
        Communicator communicator = new HttpCommunicator();
        DataStreamException thrown = Assertions.assertThrows(DataStreamException.class, () -> communicator.fetch(new URI("https://frsghr8hffdh0gtonxhpjd.gl")));
    }

    @Test
    public void testFetchFail2() throws URISyntaxException, HttpStatusException, DataStreamException, IOException {
        Communicator communicator = new HttpCommunicator();
        HttpStatusException thrown = Assertions.assertThrows(HttpStatusException.class, () -> {
                communicator.fetch(new URI("https://www.example.com/foo"));
        });
    }
/*
    @Test
    public void testGetBody() throws DataStreamException, URISyntaxException {
        System.out.println("--------------------------------------------");
        System.out.println("TESTGETBODY");
        HttpGetWithEntity partialGet = new HttpGetWithEntity(new URI("https://echo.free.beeceptor.com"));
        partialGet.setHeader("Content-Type", "application/json");

        String body = "{\"scroll\":\"10m\",\"scroll_id\":\"foobar\"}";
        partialGet.setEntity(new StringEntity(body, StandardCharsets.UTF_8));
        CloseableHttpClient httpclient = HttpClients.custom().build();

        try {
            CloseableHttpResponse response = httpclient.execute(partialGet);
            String content = InputStreamReader.readInputStream(response.getEntity().getContent());
            System.out.println(response.getCode());
            System.out.println(content);
        } catch (IOException e) {
            throw new DataStreamException(e);
        }

    }*/
}
