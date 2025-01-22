package dk.magenta.datafordeler.core.exception;

import dk.magenta.datafordeler.core.Application;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = Application.class)
public class DataStreamExceptionTest {

    @Test
    public void testDataStreamException() {
        IOException cause = new IOException();
        DataStreamException exception1 = new DataStreamException(cause);
        Assert.assertEquals(cause, exception1.getCause());
        Assert.assertEquals("datafordeler.ioexception", exception1.getCode());

        String message = "Stream interrupted";
        DataStreamException exception2 = new DataStreamException(message);
        Assert.assertEquals(message, exception2.getMessage());
        Assert.assertEquals("datafordeler.ioexception", exception2.getCode());

        DataStreamException exception3 = new DataStreamException(message, cause);
        Assert.assertEquals(cause, exception3.getCause());
        Assert.assertEquals(message, exception3.getMessage());
        Assert.assertEquals("datafordeler.ioexception", exception3.getCode());
    }

}
