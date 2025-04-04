package dk.magenta.datafordeler.core.io;

import dk.magenta.datafordeler.core.Application;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ContextConfiguration;

import java.util.UUID;

@ContextConfiguration(classes = Application.class)
public class EventTest {
    @Test
    public void testBeskedID() {
        Event event = new Event();
        UUID uuid = UUID.randomUUID();
        event.setId(uuid.toString());
        Assertions.assertEquals(uuid.toString(), event.getId());
    }
    @Test
    public void testBeskedversion() {
        Event event = new Event();
        event.setVersion("1.0");
        Assertions.assertEquals("1.0", event.getVersion());
    }
    @Test
    public void testDataSkema() {
        Event event = new Event();
        event.setSchema("testskema");
        Assertions.assertEquals("testskema", event.getSchema());
    }
    @Test
    public void testObjectData() {
        Event event = new Event();
        event.setData("{\"test\":42}");
        Assertions.assertEquals("{\"test\":42}", event.getData());
    }
    @Test
    public void testObjektReference() {
        Event event = new Event();
        event.setReference("{\"test\":42}");
        Assertions.assertEquals("{\"test\":42}", event.getReference());
    }
}
