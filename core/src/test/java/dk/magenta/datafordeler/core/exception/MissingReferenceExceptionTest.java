package dk.magenta.datafordeler.core.exception;

import dk.magenta.datafordeler.core.Application;
import dk.magenta.datafordeler.core.io.Event;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ContextConfiguration;


@ContextConfiguration(classes = Application.class)
public class MissingReferenceExceptionTest {

    @Test
    public void testMissingReferenceException() {
        Event event = new Event();
        MissingReferenceException exception = new MissingReferenceException(event);

        Assertions.assertEquals(event, exception.getEvent());
        Assertions.assertEquals("datafordeler.import.missing_reference", exception.getCode());
    }

}
