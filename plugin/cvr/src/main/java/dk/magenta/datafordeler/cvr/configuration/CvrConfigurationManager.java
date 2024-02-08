package dk.magenta.datafordeler.cvr.configuration;

import dk.magenta.datafordeler.core.configuration.ConfigurationManager;
import dk.magenta.datafordeler.core.database.setup.ConfigurationSessionManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.File;

@Component
public class CvrConfigurationManager extends ConfigurationManager<CvrConfiguration> {

    @Autowired
    private ConfigurationSessionManager sessionManager;

    @Value("${cvr.encryption.keyfile:local/cvr/keyfile.json}")
    private String encryptionKeyFileName;

    @Value("${dafo.cpr.demoCompanyList:bob}")
    private String cvrDemoList;

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
        File encryptionFile = new File(this.encryptionKeyFileName);
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
