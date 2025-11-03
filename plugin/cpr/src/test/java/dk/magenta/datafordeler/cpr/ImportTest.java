package dk.magenta.datafordeler.cpr;


import dk.magenta.datafordeler.core.Application;
import dk.magenta.datafordeler.core.Pull;
import dk.magenta.datafordeler.cpr.configuration.CprConfiguration;
import dk.magenta.datafordeler.cpr.configuration.CprConfigurationManager;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;

import static org.mockito.Mockito.when;

@ContextConfiguration(classes = Application.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ImportTest extends TestBase {

    @Test
    public void importTest() {
        CprConfiguration configuration = ((CprConfigurationManager) plugin.getConfigurationManager()).getConfiguration();
        when(configurationManager.getConfiguration()).thenReturn(configuration);
        configuration.setRoadRegisterType(CprConfiguration.RegisterType.DISABLED);
        configuration.setPersonRegisterType(CprConfiguration.RegisterType.LOCAL_FILE);
        configuration.setPersonRegisterLocalFile("data/test.txt");

        Pull pull = new Pull(engine, plugin);
        pull.run();
    }
}
