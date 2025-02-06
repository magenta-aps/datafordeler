package dk.magenta.datafordeler.core.exception;

import dk.magenta.datafordeler.core.Application;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ContextConfiguration;


@ContextConfiguration(classes = Application.class)
public class PluginImplementationExceptionTest {

    @Test
    public void testPluginImplementationException() {
        String message = "Your plugin is bad, and you should feel bad";
        PluginImplementationException exception1 = new PluginImplementationException(message);

        Assertions.assertEquals(message, exception1.getMessage());
        Assertions.assertEquals("datafordeler.plugin.implementation_error", exception1.getCode());

        NullPointerException cause = new NullPointerException();
        PluginImplementationException exception2 = new PluginImplementationException(message, cause);

        Assertions.assertEquals(message, exception2.getMessage());
        Assertions.assertEquals(cause, exception2.getCause());
        Assertions.assertEquals("datafordeler.plugin.implementation_error", exception2.getCode());
    }

}
