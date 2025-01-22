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
public class InvalidTokenExceptionTest {

    @Test
    public void testInvalidTokenException() {
        String message = "Your token is bad, and you should feel bad";
        InvalidTokenException exception1 = new InvalidTokenException(message);

        Assert.assertEquals(message, exception1.getMessage());
        Assert.assertNull(exception1.getCause());
        Assert.assertEquals("datafordeler.authorization.invalid_token", exception1.getCode());

        NullPointerException cause = new NullPointerException();
        InvalidTokenException exception2 = new InvalidTokenException(message, cause);

        Assert.assertEquals(message, exception2.getMessage());
        Assert.assertEquals(cause, exception2.getCause());
        Assert.assertEquals("datafordeler.authorization.invalid_token", exception2.getCode());
    }

}
