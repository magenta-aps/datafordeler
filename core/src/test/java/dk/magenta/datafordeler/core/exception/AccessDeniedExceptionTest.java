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
public class AccessDeniedExceptionTest {

    @Test
    public void testAccessDeniedException() {
      String message = "You need the red key to open this door";
      AccessDeniedException exception1 = new AccessDeniedException(message);
      Assert.assertEquals(message, exception1.getMessage());
      Assert.assertEquals("datafordeler.accessdenied", exception1.getCode());

      NullPointerException cause = new NullPointerException();
      AccessDeniedException exception2 = new AccessDeniedException(message, cause);
      Assert.assertEquals(message, exception2.getMessage());
      Assert.assertEquals(cause, exception2.getCause());
      Assert.assertEquals("datafordeler.accessdenied", exception2.getCode());
  }

}
