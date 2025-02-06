package dk.magenta.datafordeler.core.util;

import dk.magenta.datafordeler.core.Application;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ContextConfiguration;

import java.util.List;


@ContextConfiguration(classes = Application.class)
public class ListHashMapTest {
    @Test
    public void testAdd() {
        ListHashMap<String, String> map = new ListHashMap<>();
        String key = "foo";
        String value = "bar";
        map.add(key, value);
        List<String> list = map.get(key);
        Assertions.assertTrue(list.contains(value));
    }

    @Test
    public void testGet() {
        ListHashMap<String, String> map = new ListHashMap<>();
        String key = "foo";
        String value1 = "bar";
        String value2 = "baz";
        map.add(key, value1);
        map.add(key, value2);
        Assertions.assertEquals(value1, map.get(key, 0));
        Assertions.assertEquals(value2, map.get(key, 1));
        Assertions.assertNull(map.get(key, 2));
        Assertions.assertNull(map.get("nothing_here", 0));
        Assertions.assertNull(map.get("nothing_here", 1));
    }
}
