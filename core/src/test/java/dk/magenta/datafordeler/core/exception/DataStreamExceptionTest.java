package dk.magenta.datafordeler.core.exception;

import dk.magenta.datafordeler.core.Application;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ContextConfiguration;

import java.io.IOException;

@ContextConfiguration(classes = Application.class)
public class DataStreamExceptionTest {

    @Test
    public void testDataStreamException() {
        IOException cause = new IOException();
        DataStreamException exception1 = new DataStreamException(cause);
        Assertions.assertEquals(cause, exception1.getCause());
        Assertions.assertEquals("datafordeler.ioexception", exception1.getCode());

        String message = "Stream interrupted";
        DataStreamException exception2 = new DataStreamException(message);
        Assertions.assertEquals(message, exception2.getMessage());
        Assertions.assertEquals("datafordeler.ioexception", exception2.getCode());

        DataStreamException exception3 = new DataStreamException(message, cause);
        Assertions.assertEquals(cause, exception3.getCause());
        Assertions.assertEquals(message, exception3.getMessage());
        Assertions.assertEquals("datafordeler.ioexception", exception3.getCode());
    }

}
