package dk.magenta.datafordeler.gladdrreg.configuration;

import dk.magenta.datafordeler.core.configuration.ConfigurationManager;
import dk.magenta.datafordeler.core.database.ConfigurationSessionManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * Created by lars on 16-05-17.
 */
@Component
public class GladdregConfigurationManager extends ConfigurationManager<GladdregConfiguration> {

    @Autowired
    private ConfigurationSessionManager sessionManager;

    private Logger log = LogManager.getLogger(GladdregConfigurationManager.class.getCanonicalName());

    @PostConstruct
    public void init() {
        // Very important to call init() on ConfigurationManager, or the config will not be loaded
        super.init();
    }

    @Override
    protected Class<GladdregConfiguration> getConfigurationClass() {
        return GladdregConfiguration.class;
    }

    @Override
    protected GladdregConfiguration createConfiguration() {
        return new GladdregConfiguration();
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
