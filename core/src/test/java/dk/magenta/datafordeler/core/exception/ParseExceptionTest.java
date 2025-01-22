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
public class ParseExceptionTest {

    @Test
    public void testParseException() {
        String message = "Error parsing char at position 0";
        ParseException exception1 = new ParseException(message);

        Assert.assertEquals(message, exception1.getMessage());
        Assert.assertNull(exception1.getCause());
        Assert.assertEquals("datafordeler.import.parse_error", exception1.getCode());

        NullPointerException cause = new NullPointerException();
        ParseException exception2 = new ParseException(message, cause);

        Assert.assertEquals(message, exception2.getMessage());
        Assert.assertEquals(cause, exception2.getCause());
        Assert.assertEquals("datafordeler.import.parse_error", exception2.getCode());

    }

}
