package dk.magenta.datafordeler.core.exception;

import dk.magenta.datafordeler.core.Application;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ContextConfiguration;

@ContextConfiguration(classes = Application.class)
public class InvalidReferenceExceptionTest {

    @Test
    public void testInvalidReferenceException() {
        String reference = "reference";
        InvalidReferenceException exception = new InvalidReferenceException(reference);

        Assertions.assertEquals(reference, exception.getReference());
        Assertions.assertEquals("datafordeler.import.reference_invalid", exception.getCode());
    }

}
