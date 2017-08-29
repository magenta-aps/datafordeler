package dk.magenta.datafordeler.core.io;

import dk.magenta.datafordeler.core.Application;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.UUID;

/**
 * Created by lars on 08-05-17.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = Application.class)
public class EventTest {
    @Test
    public void testBeskedID() {
        Event event = new Event();
        UUID uuid = UUID.randomUUID();
        event.setId(uuid.toString());
        Assert.assertEquals(uuid.toString(), event.getId());
    }
    @Test
    public void testBeskedversion() {
        Event event = new Event();
        event.setVersion("1.0");
        Assert.assertEquals("1.0", event.getVersion());
    }
    @Test
    public void testDataSkema() {
        Event event = new Event();
        event.setSchema("testskema");
        Assert.assertEquals("testskema", event.getSchema());
    }
    @Test
    public void testObjectData() {
        Event event = new Event();
        event.setData("{\"test\":42}");
        Assert.assertEquals("{\"test\":42}", event.getData());
    }
    @Test
    public void testObjektReference() {
        Event event = new Event();
        event.setReference("{\"test\":42}");
        Assert.assertEquals("{\"test\":42}", event.getReference());
    }
}
