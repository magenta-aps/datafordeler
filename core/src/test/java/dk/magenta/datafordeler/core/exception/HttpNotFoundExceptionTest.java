package dk.magenta.datafordeler.core.exception;

import dk.magenta.datafordeler.core.Application;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ContextConfiguration;


@ContextConfiguration(classes = Application.class)
public class HttpNotFoundExceptionTest {

    @Test
    public void testHttpNotFoundException() {
        String message = "Not found";
        HttpNotFoundException exception = new HttpNotFoundException(message);
        Assertions.assertEquals(message, exception.getMessage());
        Assertions.assertEquals("datafordeler.http.not-found", exception.getCode());
    }

}
