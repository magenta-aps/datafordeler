package dk.magenta.datafordeler.core.plugin;

import dk.magenta.datafordeler.core.Application;
import dk.magenta.datafordeler.core.exception.DataStreamException;
import dk.magenta.datafordeler.core.exception.HttpStatusException;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = Application.class)
public class HttpCommunicatorTest {

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Test
    public void testFetchOk() throws URISyntaxException, HttpStatusException, DataStreamException, IOException {
        Communicator communicator = new HttpCommunicator();
        InputStream data = communicator.fetch(new URI("https://www.example.com"));
        Assert.assertNotNull(data);
        Assert.assertNotNull(data.read(new byte[10]));
    }

    @Test
    public void testFetchFail1() throws URISyntaxException, HttpStatusException, DataStreamException, IOException {
        Communicator communicator = new HttpCommunicator();
        exception.expect(DataStreamException.class);
        communicator.fetch(new URI("https://frsghr8hffdh0gtonxhpjd.gl"));
    }

    @Test
    public void testFetchFail2() throws URISyntaxException, HttpStatusException, DataStreamException, IOException {
        Communicator communicator = new HttpCommunicator();
        exception.expect(HttpStatusException.class);
        communicator.fetch(new URI("https://www.example.com/foo"));
    }
}
