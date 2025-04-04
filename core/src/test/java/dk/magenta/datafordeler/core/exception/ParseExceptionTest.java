package dk.magenta.datafordeler.core.exception;

import dk.magenta.datafordeler.core.Application;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ContextConfiguration;

@ContextConfiguration(classes = Application.class)
public class ParseExceptionTest {

    @Test
    public void testParseException() {
        String message = "Error parsing char at position 0";
        ParseException exception1 = new ParseException(message);

        Assertions.assertEquals(message, exception1.getMessage());
        Assertions.assertNull(exception1.getCause());
        Assertions.assertEquals("datafordeler.import.parse_error", exception1.getCode());

        NullPointerException cause = new NullPointerException();
        ParseException exception2 = new ParseException(message, cause);

        Assertions.assertEquals(message, exception2.getMessage());
        Assertions.assertEquals(cause, exception2.getCause());
        Assertions.assertEquals("datafordeler.import.parse_error", exception2.getCode());

    }

}
