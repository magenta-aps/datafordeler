package dk.magenta.datafordeler.core.configuration;

import dk.magenta.datafordeler.core.Application;
import dk.magenta.datafordeler.plugindemo.configuration.DemoConfiguration;
import dk.magenta.datafordeler.plugindemo.configuration.DemoConfigurationManager;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;


@ContextConfiguration(classes = Application.class)
public class ConfigurationTest {

    @Autowired
    DemoConfigurationManager configurationManager;

    @Test
    public void testConfiguration() throws Exception {
        DemoConfiguration configuration = configurationManager.getConfiguration();
        Assertions.assertNotNull(configuration);
        configurationManager.init();
        Assertions.assertEquals(configuration.getPullCronSchedule(), configurationManager.getConfiguration().getPullCronSchedule());
    }
}
