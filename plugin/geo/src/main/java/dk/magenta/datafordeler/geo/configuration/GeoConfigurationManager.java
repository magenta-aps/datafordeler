package dk.magenta.datafordeler.geo.configuration;

import dk.magenta.datafordeler.core.configuration.ConfigurationManager;
import dk.magenta.datafordeler.core.database.ConfigurationSessionManager;
import jakarta.annotation.PostConstruct;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class GeoConfigurationManager extends ConfigurationManager<GeoConfiguration> {

    @Autowired
    private ConfigurationSessionManager configurationSessionManager;

    private final Logger log = LogManager.getLogger(GeoConfigurationManager.class.getCanonicalName());

    @PostConstruct
    public void init() {
        // Very important to call init() on ConfigurationManager, or the config will not be loaded
        super.init();
    }

    @Override
    protected Class<GeoConfiguration> getConfigurationClass() {
        return GeoConfiguration.class;
    }

    @Override
    protected GeoConfiguration createConfiguration() {
        return new GeoConfiguration();
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
