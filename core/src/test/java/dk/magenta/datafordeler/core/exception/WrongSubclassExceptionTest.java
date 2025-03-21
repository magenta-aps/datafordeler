package dk.magenta.datafordeler.core.exception;

import dk.magenta.datafordeler.core.Application;
import dk.magenta.datafordeler.core.database.Entity;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ContextConfiguration;

@ContextConfiguration(classes = Application.class)
public class WrongSubclassExceptionTest {

    @Test
    public void testWrongSubclassException() {
        Class cls = Entity.class;
        String object = "foobar";
        WrongSubclassException exception = new WrongSubclassException(cls, object);

        Assertions.assertEquals(1, exception.getExpectedClasses().length);
        Assertions.assertEquals(cls, exception.getExpectedClasses()[0]);
        Assertions.assertEquals(object, exception.getReceivedObject());
        Assertions.assertEquals("datafordeler.plugin.plugin_received_wrong_class", exception.getCode());
    }

}
