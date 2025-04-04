package dk.magenta.datafordeler.core.util;

import dk.magenta.datafordeler.core.Application;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;

@ContextConfiguration(classes = Application.class)
public class DoubleHashMapTest {

    @Test
    public void testContainsKey() {
        DoubleHashMap<String, String, String> map = new DoubleHashMap<>();
        String key1 = "foo";
        String key2 = "bar";
        map.put(key1, key2, "foobar");
        Assertions.assertTrue(map.containsKey(key1, key2));
        Assertions.assertFalse(map.containsKey(key1, "nothing_here"));
        Assertions.assertFalse(map.containsKey("nothing_here", "nothing_here"));
    }

    @Test
    public void testPutGet() {
        DoubleHashMap<String, String, String> map = new DoubleHashMap<>();
        String key1 = "foo";
        String key2 = "bar";
        String value = "foobar";
        map.put(key1, key2, "dummy");
        map.put(key1, key2, value);
        Assertions.assertEquals(value, map.get(key1, key2));
        Assertions.assertNotEquals("dummy", map.get(key1, key2));
        Assertions.assertNotEquals(value, map.get(key1, "nothing_here"));
        Assertions.assertNotEquals(value, map.get("nothing_here", "nothing_here"));
    }
}
