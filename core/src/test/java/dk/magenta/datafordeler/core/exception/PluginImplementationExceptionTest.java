package dk.magenta.datafordeler.core.exception;

import dk.magenta.datafordeler.core.Application;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = Application.class)
public class PluginImplementationExceptionTest {

    @Test
    public void testPluginImplementationException() {
        String message = "Your plugin is bad, and you should feel bad";
        PluginImplementationException exception1 = new PluginImplementationException(message);

        Assert.assertEquals(message, exception1.getMessage());
        Assert.assertEquals("datafordeler.plugin.implementation_error", exception1.getCode());

        NullPointerException cause = new NullPointerException();
        PluginImplementationException exception2 = new PluginImplementationException(message, cause);

        Assert.assertEquals(message, exception2.getMessage());
        Assert.assertEquals(cause, exception2.getCause());
        Assert.assertEquals("datafordeler.plugin.implementation_error", exception2.getCode());
    }

}
