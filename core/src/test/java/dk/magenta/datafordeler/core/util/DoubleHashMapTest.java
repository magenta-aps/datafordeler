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
public class DoubleHashMapTest {

    @Test
    public void testContainsKey() {
        DoubleHashMap<String, String, String> map = new DoubleHashMap<>();
        String key1 = "foo";
        String key2 = "bar";
        map.put(key1, key2, "foobar");
        Assert.assertTrue(map.containsKey(key1, key2));
        Assert.assertFalse(map.containsKey(key1, "nothing_here"));
        Assert.assertFalse(map.containsKey("nothing_here", "nothing_here"));
    }

    @Test
    public void testPutGet() {
        DoubleHashMap<String, String, String> map = new DoubleHashMap<>();
        String key1 = "foo";
        String key2 = "bar";
        String value = "foobar";
        map.put(key1, key2, "dummy");
        map.put(key1, key2, value);
        Assert.assertEquals(value, map.get(key1, key2));
        Assert.assertNotEquals("dummy", map.get(key1, key2));
        Assert.assertNotEquals(value, map.get(key1, "nothing_here"));
        Assert.assertNotEquals(value, map.get("nothing_here", "nothing_here"));
    }
}
