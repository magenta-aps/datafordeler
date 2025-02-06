package dk.magenta.datafordeler.core.exception;

import dk.magenta.datafordeler.core.Application;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ContextConfiguration;


@ContextConfiguration(classes = Application.class)
public class InvalidClientInputExceptionTest {

    @Test
    public void testInvalidClientInputException() {
        String message = "invalid input";
        InvalidClientInputException exception = new InvalidClientInputException(message);

        Assertions.assertEquals(message, exception.getMessage());
        Assertions.assertEquals("datafordeler.http.invalid-client-input", exception.getCode());
    }

}
