package dk.magenta.datafordeler.core.exception;

import dk.magenta.datafordeler.core.TestConfig;
import dk.magenta.datafordeler.core.io.Event;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = TestConfig.class)
public class MissingReferenceExceptionTest {

    @Test
    public void testMissingReferenceException() {
        Event event = new Event();
        MissingReferenceException exception = new MissingReferenceException(event);

        Assert.assertEquals(event, exception.getEvent());
        Assert.assertEquals("datafordeler.import.missing_reference", exception.getCode());
    }

}
