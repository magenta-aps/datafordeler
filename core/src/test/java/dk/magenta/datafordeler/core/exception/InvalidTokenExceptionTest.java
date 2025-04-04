package dk.magenta.datafordeler.core.exception;

import dk.magenta.datafordeler.core.Application;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ContextConfiguration;

@ContextConfiguration(classes = Application.class)
public class InvalidTokenExceptionTest {

    @Test
    public void testInvalidTokenException() {
        String message = "Your token is bad, and you should feel bad";
        InvalidTokenException exception1 = new InvalidTokenException(message);

        Assertions.assertEquals(message, exception1.getMessage());
        Assertions.assertNull(exception1.getCause());
        Assertions.assertEquals("datafordeler.authorization.invalid_token", exception1.getCode());

        NullPointerException cause = new NullPointerException();
        InvalidTokenException exception2 = new InvalidTokenException(message, cause);

        Assertions.assertEquals(message, exception2.getMessage());
        Assertions.assertEquals(cause, exception2.getCause());
        Assertions.assertEquals("datafordeler.authorization.invalid_token", exception2.getCode());
    }

}
