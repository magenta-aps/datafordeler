package dk.magenta.datafordeler.cpr;


import dk.magenta.datafordeler.core.Application;
import dk.magenta.datafordeler.core.Engine;
import dk.magenta.datafordeler.core.Pull;
import dk.magenta.datafordeler.cpr.configuration.CprConfiguration;
import dk.magenta.datafordeler.cpr.configuration.CprConfigurationManager;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.mockito.Mockito.when;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = Application.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class ImportTest {

    @Autowired
    private Engine engine;

    @SpyBean
    private CprConfigurationManager configurationManager;

    @Autowired
    private CprPlugin plugin;

    @Test
    public void importTest() {
        CprConfiguration configuration = ((CprConfigurationManager) plugin.getConfigurationManager()).getConfiguration();
        when(configurationManager.getConfiguration()).thenReturn(configuration);
        configuration.setRoadRegisterType(CprConfiguration.RegisterType.DISABLED);
        configuration.setResidenceRegisterType(CprConfiguration.RegisterType.DISABLED);
        configuration.setPersonRegisterType(CprConfiguration.RegisterType.LOCAL_FILE);
        configuration.setPersonRegisterLocalFile("data/test.txt");

        Pull pull = new Pull(engine, plugin);
        pull.run();
    }
}
