package dk.magenta.datafordeler.core.plugin;

import dk.magenta.datafordeler.core.Application;
import dk.magenta.datafordeler.core.exception.DataStreamException;
import dk.magenta.datafordeler.core.exception.HttpStatusException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ContextConfiguration;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;

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
}
