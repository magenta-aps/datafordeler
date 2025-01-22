package dk.magenta.datafordeler.core.util;

import dk.magenta.datafordeler.core.Application;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = Application.class)
public class FixedQueueMapTest {
    @Test
    public void testFixedQueueMap() {
        FixedQueueMap<String, String> map = new FixedQueueMap<>(5);

        map.put("foo1", "foo");
        Assert.assertEquals(1, map.getUsedCapacity());
        Assert.assertEquals("foo", map.get("foo1"));
        map.put("foo2", "foo");
        Assert.assertEquals(2, map.getUsedCapacity());
        Assert.assertEquals("foo", map.get("foo1"));
        Assert.assertEquals("foo", map.get("foo2"));
        map.put("foo3", "foo");
        Assert.assertEquals(3, map.getUsedCapacity());
        Assert.assertEquals("foo", map.get("foo1"));
        Assert.assertEquals("foo", map.get("foo2"));
        Assert.assertEquals("foo", map.get("foo3"));
        map.put("foo4", "foo");
        Assert.assertEquals(4, map.getUsedCapacity());
        Assert.assertEquals("foo", map.get("foo1"));
        Assert.assertEquals("foo", map.get("foo2"));
        Assert.assertEquals("foo", map.get("foo3"));
        Assert.assertEquals("foo", map.get("foo4"));
        map.put("foo5", "foo");
        Assert.assertEquals(5, map.getUsedCapacity());
        Assert.assertEquals("foo", map.get("foo1"));
        Assert.assertEquals("foo", map.get("foo2"));
        Assert.assertEquals("foo", map.get("foo3"));
        Assert.assertEquals("foo", map.get("foo4"));
        Assert.assertEquals("foo", map.get("foo5"));
        map.put("foo6", "foo");
        Assert.assertEquals(5, map.getUsedCapacity());
        Assert.assertEquals(null, map.get("foo1"));
        Assert.assertEquals("foo", map.get("foo2"));
        Assert.assertEquals("foo", map.get("foo3"));
        Assert.assertEquals("foo", map.get("foo4"));
        Assert.assertEquals("foo", map.get("foo5"));
        Assert.assertEquals("foo", map.get("foo6"));
        map.put("foo7", "foo");
        Assert.assertEquals(5, map.getCapacity());
        Assert.assertEquals(null, map.get("foo1"));
        Assert.assertEquals(null, map.get("foo2"));
        Assert.assertEquals("foo", map.get("foo3"));
        Assert.assertEquals("foo", map.get("foo4"));
        Assert.assertEquals("foo", map.get("foo5"));
        Assert.assertEquals("foo", map.get("foo6"));
        Assert.assertEquals("foo", map.get("foo7"));
        map.put("foo8", "foo");
        Assert.assertEquals(5, map.getCapacity());
        Assert.assertEquals(null, map.get("foo1"));
        Assert.assertEquals(null, map.get("foo2"));
        Assert.assertEquals(null, map.get("foo3"));
        Assert.assertEquals("foo", map.get("foo4"));
        Assert.assertEquals("foo", map.get("foo5"));
        Assert.assertEquals("foo", map.get("foo6"));
        Assert.assertEquals("foo", map.get("foo7"));
        Assert.assertEquals("foo", map.get("foo8"));

    }
}
