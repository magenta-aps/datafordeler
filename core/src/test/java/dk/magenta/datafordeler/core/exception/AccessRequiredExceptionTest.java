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
public class AccessRequiredExceptionTest {

    @Test
    public void testAccessRequiredException() {
        String message = "You need the blue key to open this door";
        AccessRequiredException exception = new AccessRequiredException(message);

        Assert.assertEquals(message, exception.getMessage());
        Assert.assertEquals("datafordeler.accessrequired", exception.getCode());
    }
}
