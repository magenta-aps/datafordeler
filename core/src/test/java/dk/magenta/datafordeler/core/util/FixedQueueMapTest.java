package dk.magenta.datafordeler.core.util;

import dk.magenta.datafordeler.core.Application;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ContextConfiguration;


@ContextConfiguration(classes = Application.class)
public class FixedQueueMapTest {
    @Test
    public void testFixedQueueMap() {
        FixedQueueMap<String, String> map = new FixedQueueMap<>(5);

        map.put("foo1", "foo");
        Assertions.assertEquals(1, map.getUsedCapacity());
        Assertions.assertEquals("foo", map.get("foo1"));
        map.put("foo2", "foo");
        Assertions.assertEquals(2, map.getUsedCapacity());
        Assertions.assertEquals("foo", map.get("foo1"));
        Assertions.assertEquals("foo", map.get("foo2"));
        map.put("foo3", "foo");
        Assertions.assertEquals(3, map.getUsedCapacity());
        Assertions.assertEquals("foo", map.get("foo1"));
        Assertions.assertEquals("foo", map.get("foo2"));
        Assertions.assertEquals("foo", map.get("foo3"));
        map.put("foo4", "foo");
        Assertions.assertEquals(4, map.getUsedCapacity());
        Assertions.assertEquals("foo", map.get("foo1"));
        Assertions.assertEquals("foo", map.get("foo2"));
        Assertions.assertEquals("foo", map.get("foo3"));
        Assertions.assertEquals("foo", map.get("foo4"));
        map.put("foo5", "foo");
        Assertions.assertEquals(5, map.getUsedCapacity());
        Assertions.assertEquals("foo", map.get("foo1"));
        Assertions.assertEquals("foo", map.get("foo2"));
        Assertions.assertEquals("foo", map.get("foo3"));
        Assertions.assertEquals("foo", map.get("foo4"));
        Assertions.assertEquals("foo", map.get("foo5"));
        map.put("foo6", "foo");
        Assertions.assertEquals(5, map.getUsedCapacity());
        Assertions.assertEquals(null, map.get("foo1"));
        Assertions.assertEquals("foo", map.get("foo2"));
        Assertions.assertEquals("foo", map.get("foo3"));
        Assertions.assertEquals("foo", map.get("foo4"));
        Assertions.assertEquals("foo", map.get("foo5"));
        Assertions.assertEquals("foo", map.get("foo6"));
        map.put("foo7", "foo");
        Assertions.assertEquals(5, map.getCapacity());
        Assertions.assertEquals(null, map.get("foo1"));
        Assertions.assertEquals(null, map.get("foo2"));
        Assertions.assertEquals("foo", map.get("foo3"));
        Assertions.assertEquals("foo", map.get("foo4"));
        Assertions.assertEquals("foo", map.get("foo5"));
        Assertions.assertEquals("foo", map.get("foo6"));
        Assertions.assertEquals("foo", map.get("foo7"));
        map.put("foo8", "foo");
        Assertions.assertEquals(5, map.getCapacity());
        Assertions.assertEquals(null, map.get("foo1"));
        Assertions.assertEquals(null, map.get("foo2"));
        Assertions.assertEquals(null, map.get("foo3"));
        Assertions.assertEquals("foo", map.get("foo4"));
        Assertions.assertEquals("foo", map.get("foo5"));
        Assertions.assertEquals("foo", map.get("foo6"));
        Assertions.assertEquals("foo", map.get("foo7"));
        Assertions.assertEquals("foo", map.get("foo8"));

    }
}
