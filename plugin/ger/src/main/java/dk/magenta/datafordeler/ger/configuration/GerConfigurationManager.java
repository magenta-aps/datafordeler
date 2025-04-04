package dk.magenta.datafordeler.ger.configuration;

import com.microsoft.sqlserver.jdbc.SQLServerDriver;
import dk.magenta.datafordeler.core.configuration.ConfigurationManager;
import dk.magenta.datafordeler.core.database.ConfigurationSessionManager;
import jakarta.annotation.PostConstruct;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class GerConfigurationManager extends ConfigurationManager<GerConfiguration> {

    @Autowired
    private ConfigurationSessionManager configurationSessionManager;

    SQLServerDriver sqlServerDriver;

    private final Logger log = LogManager.getLogger(GerConfigurationManager.class.getCanonicalName());

    @PostConstruct
    public void init() {
        try {
            System.out.println("Initializing GerConfigurationManager");
            // Very important to call init() on ConfigurationManager, or the config will not be loaded
            super.init();
            this.sqlServerDriver = new SQLServerDriver();
        } catch (Exception e) {
            System.out.println("Error initializing GerConfigurationManager: ");
            e.printStackTrace();
            throw e;
        }
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
