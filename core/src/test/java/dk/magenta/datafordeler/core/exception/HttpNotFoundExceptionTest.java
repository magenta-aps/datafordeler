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
public class HttpNotFoundExceptionTest {

    @Test
    public void testHttpNotFoundException() {
        String message = "Not found";
        HttpNotFoundException exception = new HttpNotFoundException(message);
        Assert.assertEquals(message, exception.getMessage());
        Assert.assertEquals("datafordeler.http.not-found", exception.getCode());
    }

}
