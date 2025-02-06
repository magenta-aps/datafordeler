package dk.magenta.datafordeler.core.exception;

import dk.magenta.datafordeler.core.Application;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ContextConfiguration;


@ContextConfiguration(classes = Application.class)
public class SimilarJobRunningExceptionTest {

    @Test
    public void testSimilarJobRunningException() {
        String message = "DemoPull";
        SimilarJobRunningException exception = new SimilarJobRunningException(message);

        Assertions.assertEquals(message, exception.getMessage());
        Assertions.assertEquals("datafordeler.import.similar_job_running", exception.getCode());
    }

}
