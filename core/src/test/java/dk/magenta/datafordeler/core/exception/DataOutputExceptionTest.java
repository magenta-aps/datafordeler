package dk.magenta.datafordeler.core.exception;

import dk.magenta.datafordeler.core.Application;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.ws.rs.core.Response;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = Application.class)
public class DataOutputExceptionTest {

    @Test
    public void testDataOutputException() {
        NullPointerException cause = new NullPointerException();
        DataOutputException exception = new DataOutputException(cause);
        Assert.assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), exception.getResponse().getStatus());
        Assert.assertEquals(cause.getClass().getCanonicalName(), exception.getResponse().getEntity().toString());
    }

}
