package dk.magenta.datafordeler.plugindemo.configuration;

import dk.magenta.datafordeler.core.configuration.ConfigurationManager;
import dk.magenta.datafordeler.core.database.setup.ConfigurationSessionManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Component
public class DemoConfigurationManager extends ConfigurationManager<DemoConfiguration> {

    @Autowired
    ConfigurationSessionManager sessionManager;

    private static Logger log = LogManager.getLogger(DemoConfigurationManager.class.getCanonicalName());

    /**
     * Run bean initialization
     * Load configuration from database
     */
    @PostConstruct
    public void init() {
        // Very important to call init() on ConfigurationManager, or the config will not be loaded
        super.init();
    }

    @Override
    protected Class<DemoConfiguration> getConfigurationClass() {
        return DemoConfiguration.class;
    }

    @Override
    protected DemoConfiguration createConfiguration() {
        return new DemoConfiguration();
    }

    @Override
    protected ConfigurationSessionManager getSessionManager() {
        return this.sessionManager;
    }

    @Override
    protected Logger getLog() {
        return this.log;
    }

}
