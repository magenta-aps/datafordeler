package dk.magenta.datafordeler.cvr.configuration;

import dk.magenta.datafordeler.core.configuration.ConfigurationManager;
import dk.magenta.datafordeler.core.database.ConfigurationSessionManager;
import jakarta.annotation.PostConstruct;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

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
        if (configuration != null) {
            log.info("Using encryption key file: {}", configuration.getEncryptionKeyFileName());
            File encryptionFile = new File(configuration.getEncryptionKeyFileName());
            configuration.setCompanyRegisterPasswordEncryptionFile(encryptionFile);
            configuration.setCompanyUnitRegisterPasswordEncryptionFile(encryptionFile);
            configuration.setParticipantRegisterPasswordEncryptionFile(encryptionFile);
        }
        return configuration;
    }

    @PostConstruct
    public void encryptPasswords() {
        log.info("Encrypting passwords");
        CvrConfiguration configuration = this.getConfiguration();
        if (configuration != null) {
            Session session = this.getSessionManager().getSessionFactory().openSession();
            try {
                Transaction transaction = session.beginTransaction();
                try {
                    if (configuration.encryptCompanyDirectRegisterPassword(true, true)) {
                        // Must use merge instead of save, because we are updating an object that was born in another session
                        configuration = (CvrConfiguration) session.merge(configuration);
                        log.info("Encrypted company direct register password");
                    }
                    if (configuration.encryptParticipantDirectRegisterPassword(true, true)) {
                        // Must use merge instead of save, because we are updating an object that was born in another session
                        configuration = (CvrConfiguration) session.merge(configuration);
                        log.info("Encrypted participant direct register password");
                    }
                    if (configuration.encryptCompanyRegisterPassword(true, true)) {
                        configuration = (CvrConfiguration) session.merge(configuration);
                        log.info("Encrypted company register password");
                    }
                    if (configuration.encryptCompanyUnitRegisterPassword(true, true)) {
                        configuration = (CvrConfiguration) session.merge(configuration);
                        log.info("Encrypted company unit register password");
                    }
                    if (configuration.encryptParticipantRegisterPassword(true, true)) {
                        configuration = (CvrConfiguration) session.merge(configuration);
                        log.info("Encrypted company participant register password");
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
}
