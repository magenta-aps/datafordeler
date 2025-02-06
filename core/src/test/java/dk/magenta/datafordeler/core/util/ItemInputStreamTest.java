package dk.magenta.datafordeler.core.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import dk.magenta.datafordeler.core.Application;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;


@ContextConfiguration(classes = Application.class)
public class ItemInputStreamTest {

    @Autowired
    ObjectMapper objectMapper;

    @Test
    public void parseTest1() throws IOException {
        String inputText = "{\"items\":[{\"foo\":42,\"bar\":\"foobar\"},{\"foo\":24,\"bar\":\"barfoo\"}]}";
        InputStream jsonStream = new ByteArrayInputStream(inputText.getBytes());
        ItemInputStream<TestObject> stream = ItemInputStream.parseJsonStream(jsonStream, TestObject.class, "items", this.objectMapper);
        TestObject item;
        item = stream.next();
        Assertions.assertEquals(42, item.foo);
        Assertions.assertEquals("foobar", item.bar);
        item = stream.next();
        Assertions.assertEquals(24, item.foo);
        Assertions.assertEquals("barfoo", item.bar);
        item = stream.next();
        Assertions.assertNull(item);
    }

    @Test
    public void parseTest2() throws IOException {
        String inputText = "{\"items\":[{\"type\":\"TestObject\",\"foo\":42,\"bar\":\"foobar\"},{\"type\":\"TestObject\",\"foo\":24,\"bar\":\"barfoo\"},{\"type\":\"NotPresent\"}]}";
        InputStream jsonStream = new ByteArrayInputStream(inputText.getBytes());
        HashMap<String, Class<? extends TestObject>> classMap = new HashMap<>();
        classMap.put("TestObject", TestObject.class);
        ItemInputStream<TestObject> stream = ItemInputStream.parseJsonStream(jsonStream, classMap, "items", "type", this.objectMapper);
        TestObject item;
        item = stream.next();
        Assertions.assertEquals(42, item.foo);
        Assertions.assertEquals("foobar", item.bar);
        item = stream.next();
        Assertions.assertEquals(24, item.foo);
        Assertions.assertEquals("barfoo", item.bar);
        item = stream.next();
        Assertions.assertNull(item);
    }

    @Test
    public void parseTest3() throws IOException {
        String inputText = "{\"container\":{\"items\":[{\"type\":\"TestObject\",\"foo\":42,\"bar\":\"foobar\"},{\"type\":\"TestObject\",\"foo\":24,\"bar\":\"barfoo\"},{\"type\":\"NotPresent\"}]}}";
        InputStream jsonStream = new ByteArrayInputStream(inputText.getBytes());
        HashMap<String, Class<? extends TestObject>> classMap = new HashMap<>();
        classMap.put("TestObject", TestObject.class);
        ItemInputStream<TestObject> stream = ItemInputStream.parseJsonStream(jsonStream, classMap, "items", "type", this.objectMapper);
        TestObject item;
        item = stream.next();
        Assertions.assertEquals(42, item.foo);
        Assertions.assertEquals("foobar", item.bar);
        item = stream.next();
        Assertions.assertEquals(24, item.foo);
        Assertions.assertEquals("barfoo", item.bar);
        item = stream.next();
        Assertions.assertNull(item);
    }
}
