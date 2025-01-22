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
public class InvalidReferenceExceptionTest {

    @Test
    public void testInvalidReferenceException() {
        String reference = "reference";
        InvalidReferenceException exception = new InvalidReferenceException(reference);

        Assert.assertEquals(reference, exception.getReference());
        Assert.assertEquals("datafordeler.import.reference_invalid", exception.getCode());
    }

}
