package dk.magenta.datafordeler.cvr.configuration;

import dk.magenta.datafordeler.core.configuration.ConfigurationManager;
import dk.magenta.datafordeler.core.database.ConfigurationSessionManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.io.File;

@Component
public class CvrConfigurationManager extends ConfigurationManager<CvrConfiguration> {

    @Autowired
    private ConfigurationSessionManager sessionManager;

    private final Logger log = LogManager.getLogger("CvrConfigurationManager");

    @PostConstruct
    public void init() {
        // Very important to call init() on ConfigurationManager, or the config will not be loaded
        super.init();
    }

    @Override
    protected Class<CvrConfiguration> getConfigurationClass() {
        return CvrConfiguration.class;
    }

    @Override
    protected CvrConfiguration createConfiguration() {
        return new CvrConfiguration();
    }

    @Override
    protected ConfigurationSessionManager getSessionManager() {
        return this.sessionManager;
    }

    @Override
    protected Logger getLog() {
        return this.log;
    }

    @Override
    public CvrConfiguration getConfiguration() {
        CvrConfiguration configuration = super.getConfiguration();
        File encryptionFile = new File(configuration.getEncryptionKeyFileName());
        configuration.setCompanyRegisterPasswordEncryptionFile(encryptionFile);
        configuration.setCompanyUnitRegisterPasswordEncryptionFile(encryptionFile);
        configuration.setParticipantRegisterPasswordEncryptionFile(encryptionFile);
        return configuration;
    }

    @PostConstruct
    public void encryptPasswords() {
        CvrConfiguration configuration = this.getConfiguration();
        Session session = this.getSessionManager().getSessionFactory().openSession();
        try {
            Transaction transaction = session.beginTransaction();
            try {
                if (configuration.encryptCompanyDirectRegisterPassword(true)) {
                    // Must use merge instead of save, because we are updating an object that was born in another session
                    configuration = (CvrConfiguration) session.merge(configuration);
                }
                if (configuration.encryptParticipantDirectRegisterPassword(true)) {
                    // Must use merge instead of save, because we are updating an object that was born in another session
                    configuration = (CvrConfiguration) session.merge(configuration);
                }
                transaction.commit();
            } catch (Exception e) {
                transaction.rollback();
                e.printStackTrace();
            }
            // TODO: Update more passwords?
        } finally {
            session.close();
        }
    }
}
