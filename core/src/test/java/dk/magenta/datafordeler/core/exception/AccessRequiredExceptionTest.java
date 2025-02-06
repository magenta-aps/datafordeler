package dk.magenta.datafordeler.core.exception;

import dk.magenta.datafordeler.core.Application;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ContextConfiguration;


@ContextConfiguration(classes = Application.class)
public class AccessRequiredExceptionTest {

    @Test
    public void testAccessRequiredException() {
        String message = "You need the blue key to open this door";
        AccessRequiredException exception = new AccessRequiredException(message);

        Assertions.assertEquals(message, exception.getMessage());
        Assertions.assertEquals("datafordeler.accessrequired", exception.getCode());
    }
}
