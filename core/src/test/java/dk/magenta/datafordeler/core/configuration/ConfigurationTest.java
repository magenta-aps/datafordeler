package dk.magenta.datafordeler.core.configuration;

import dk.magenta.datafordeler.core.Application;
import dk.magenta.datafordeler.plugindemo.configuration.DemoConfiguration;
import dk.magenta.datafordeler.plugindemo.configuration.DemoConfigurationManager;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = Application.class)
public class ConfigurationTest {

    @Autowired
    DemoConfigurationManager configurationManager;

    @Test
    public void testConfiguration() throws Exception {
        DemoConfiguration configuration = configurationManager.getConfiguration();
        Assert.assertNotNull(configuration);
        configurationManager.init();
        Assert.assertEquals(configuration.getPullCronSchedule(), configurationManager.getConfiguration().getPullCronSchedule());
    }
}
