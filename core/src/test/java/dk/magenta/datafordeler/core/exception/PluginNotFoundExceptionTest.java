package dk.magenta.datafordeler.core.exception;

import dk.magenta.datafordeler.core.Application;
import dk.magenta.datafordeler.plugindemo.model.DemoEntityRecord;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.junit4.SpringRunner;

import java.net.URI;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = Application.class)
public class PluginNotFoundExceptionTest {

    @Test
    public void testPluginNotFoundException() throws Exception {
        String schema = DemoEntityRecord.schema;
        PluginNotFoundException exception1 = new PluginNotFoundException(schema, true);
        Assert.assertEquals("Plugin that handles schema " + schema + " was not found", exception1.getMessage());
        Assert.assertEquals("datafordeler.plugin.plugin_not_found", exception1.getCode());

        String pluginName = "Demo";
        PluginNotFoundException exception2 = new PluginNotFoundException(pluginName, false);
        Assert.assertEquals("Plugin named " + pluginName + " was not found", exception2.getMessage());
        Assert.assertEquals("datafordeler.plugin.plugin_not_found", exception2.getCode());

        URI uri = new URI("https://data.gl/demo");
        PluginNotFoundException exception3 = new PluginNotFoundException(uri);
        Assert.assertEquals("Plugin that handles URI " + uri.toString() + " was not found", exception3.getMessage());
        Assert.assertEquals("datafordeler.plugin.plugin_not_found", exception3.getCode());

        PluginNotFoundException exception4 = new PluginNotFoundException(null);
        Assert.assertEquals("Plugin lookup on null", exception4.getMessage());
        Assert.assertEquals("datafordeler.plugin.plugin_not_found", exception4.getCode());
    }

}
