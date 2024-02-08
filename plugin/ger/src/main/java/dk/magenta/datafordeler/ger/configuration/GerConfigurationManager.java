package dk.magenta.datafordeler.ger.configuration;

import dk.magenta.datafordeler.core.configuration.ConfigurationManager;
import dk.magenta.datafordeler.core.database.ConfigurationSessionManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Component
public class GerConfigurationManager extends ConfigurationManager<GerConfiguration> {

    @Autowired
    private ConfigurationSessionManager configurationSessionManager;

    private final Logger log = LogManager.getLogger(GerConfigurationManager.class.getCanonicalName());

    @PostConstruct
    public void init() {
        // Very important to call init() on ConfigurationManager, or the config will not be loaded
        super.init();
    }

    @Override
    protected Class<GerConfiguration> getConfigurationClass() {
        return GerConfiguration.class;
    }

    @Override
    protected GerConfiguration createConfiguration() {
        return new GerConfiguration();
    }

    @Override
    protected ConfigurationSessionManager getSessionManager() {
        return this.configurationSessionManager;
    }

    @Override
    protected Logger getLog() {
        return this.log;
    }
}
