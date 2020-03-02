package dk.magenta.datafordeler.cpr.configuration;

import dk.magenta.datafordeler.core.configuration.ConfigurationManager;
import dk.magenta.datafordeler.core.database.ConfigurationSessionManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.File;

@Component
public class CprConfigurationManager extends ConfigurationManager<CprConfiguration> {

    @Autowired
    private ConfigurationSessionManager configurationSessionManager;

    @Value("${cpr.encryption.keyfile:local/cpr/keyfile.json}")
    private String encryptionKeyFileName;

    private Logger log = LogManager.getLogger("CprConfigurationManager");

    @PostConstruct
    public void init() {
        // Very important to call init() on ConfigurationManager, or the config will not be loaded
        super.init();
    }

    @Override
    protected Class<CprConfiguration> getConfigurationClass() {
        return CprConfiguration.class;
    }

    @Override
    protected CprConfiguration createConfiguration() {
        return new CprConfiguration();
    }

    @Override
    protected ConfigurationSessionManager getSessionManager() {
        return this.configurationSessionManager;
    }

    @Override
    protected Logger getLog() {
        return this.log;
    }


    @Override
    public CprConfiguration getConfiguration() {
        CprConfiguration configuration = super.getConfiguration();
        File encryptionFile = new File(this.encryptionKeyFileName);
        configuration.setPersonRegisterPasswordEncryptionFile(encryptionFile);
        configuration.setRoadRegisterPasswordEncryptionFile(encryptionFile);
        configuration.setResidenceRegisterPasswordEncryptionFile(encryptionFile);
        return configuration;
    }
}
