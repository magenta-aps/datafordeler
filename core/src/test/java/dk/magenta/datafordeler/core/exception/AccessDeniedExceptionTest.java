package dk.magenta.datafordeler.core.exception;

import dk.magenta.datafordeler.core.Application;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;

@ContextConfiguration(classes = Application.class)
public class AccessDeniedExceptionTest {

    @Test
    public void testAccessDeniedException() {
      String message = "You need the red key to open this door";
      AccessDeniedException exception1 = new AccessDeniedException(message);
      Assertions.assertEquals(message, exception1.getMessage());
      Assertions.assertEquals("datafordeler.accessdenied", exception1.getCode());

      NullPointerException cause = new NullPointerException();
      AccessDeniedException exception2 = new AccessDeniedException(message, cause);
      Assertions.assertEquals(message, exception2.getMessage());
      Assertions.assertEquals(cause, exception2.getCause());
      Assertions.assertEquals("datafordeler.accessdenied", exception2.getCode());
  }

}
