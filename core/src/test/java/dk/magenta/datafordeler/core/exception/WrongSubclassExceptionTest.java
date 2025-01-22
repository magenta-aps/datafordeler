package dk.magenta.datafordeler.core.exception;

import dk.magenta.datafordeler.core.Application;
import dk.magenta.datafordeler.core.database.Entity;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = Application.class)
public class WrongSubclassExceptionTest {

    @Test
    public void testWrongSubclassException() {
        Class cls = Entity.class;
        String object = "foobar";
        WrongSubclassException exception = new WrongSubclassException(cls, object);

        Assert.assertEquals(1, exception.getExpectedClasses().length);
        Assert.assertEquals(cls, exception.getExpectedClasses()[0]);
        Assert.assertEquals(object, exception.getReceivedObject());
        Assert.assertEquals("datafordeler.plugin.plugin_received_wrong_class", exception.getCode());
    }

}
