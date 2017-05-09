package dk.magenta.datafordeler.core.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import dk.magenta.datafordeler.core.AppConfig;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;

/**
 * Created by lars on 05-05-17.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = AppConfig.class)
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
        Assert.assertEquals(42, item.foo);
        Assert.assertEquals("foobar", item.bar);
        item = stream.next();
        Assert.assertEquals(24, item.foo);
        Assert.assertEquals("barfoo", item.bar);
        item = stream.next();
        Assert.assertNull(item);
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
        Assert.assertEquals(42, item.foo);
        Assert.assertEquals("foobar", item.bar);
        item = stream.next();
        Assert.assertEquals(24, item.foo);
        Assert.assertEquals("barfoo", item.bar);
        item = stream.next();
        Assert.assertNull(item);
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
        Assert.assertEquals(42, item.foo);
        Assert.assertEquals("foobar", item.bar);
        item = stream.next();
        Assert.assertEquals(24, item.foo);
        Assert.assertEquals("barfoo", item.bar);
        item = stream.next();
        Assert.assertNull(item);
    }
}
