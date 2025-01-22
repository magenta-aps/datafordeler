package dk.magenta.datafordeler.core.exception;

import dk.magenta.datafordeler.core.Application;
import dk.magenta.datafordeler.core.io.Event;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = Application.class)
public class MissingReferenceExceptionTest {

    @Test
    public void testMissingReferenceException() {
        Event event = new Event();
        MissingReferenceException exception = new MissingReferenceException(event);

        Assert.assertEquals(event, exception.getEvent());
        Assert.assertEquals("datafordeler.import.missing_reference", exception.getCode());
    }

}
