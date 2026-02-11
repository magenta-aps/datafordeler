package dk.magenta.datafordeler.cpr.configuration;

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
import java.io.IOException;
import java.security.GeneralSecurityException;

@Component
public class CprConfigurationManager extends ConfigurationManager<CprConfiguration> {

    @Autowired
    private ConfigurationSessionManager configurationSessionManager;

    private final Logger log = LogManager.getLogger(CprConfigurationManager.class.getCanonicalName());

    @PostConstruct
    public void init() {
        // Very important to call init() on ConfigurationManager, or the config will not be loaded
        super.init();
        /*try {
            this.setDirectPassword("newpassword");
        } catch (GeneralSecurityException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }*/
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
        File encryptionFile = new File(configuration.getEncryptionKeyFileName());
        configuration.setPersonRegisterPasswordEncryptionFile(encryptionFile);
        configuration.setRoadRegisterPasswordEncryptionFile(encryptionFile);
        configuration.setDirectPasswordPasswordEncryptionFile(encryptionFile);
        return configuration;
    }
    @PostConstruct
    public void encryptPasswords() {
        log.info("Encrypting passwords");
        try (Session session = this.getSessionManager().getSessionFactory().openSession()) {
            CprConfiguration configuration = this.getConfiguration();
            if (configuration != null) {
                configuration.setDirectPasswordPasswordEncryptionFile(new File(configuration.getEncryptionKeyFileName()));
                Transaction transaction = session.beginTransaction();
                try {
                    if (configuration.encryptDirectPassword(false, true)) {
                        configuration = (CprConfiguration) session.merge(configuration);
                    }
                    transaction.commit();
                } catch (Exception e) {
                    transaction.rollback();
                    e.printStackTrace();
                }
            }
        }
    }
    public void setDirectPassword(String password) throws GeneralSecurityException, IOException {
        // Updates the encrypted password in the database
        Session session = this.getSessionManager().getSessionFactory().openSession();
        Transaction transaction = session.beginTransaction();
        try {
            CprConfiguration cprConfiguration = session.createQuery("select c from " + CprConfiguration.class.getCanonicalName() + " c", CprConfiguration.class).getSingleResult();
            cprConfiguration.setDirectPasswordPasswordEncryptionFile(new File(cprConfiguration.getEncryptionKeyFileName()));
            cprConfiguration.setDirectPassword(password);
            session.persist(cprConfiguration);
            transaction.commit();
        } catch (Exception e) {
            transaction.rollback();
            throw e;
        } finally {
            session.close();
        }

        /*try {
            CprConfiguration configuration = super.getConfiguration();
            Files.write(new File(encryptedPassword + UUID.randomUUID()).toPath(), configuration.getEncryptedDirectPassword());
        } catch (Exception e) {
            log.error("Exception", e);
        }*/
    }
/*
    @PostConstruct
    public void printDirectPassword() throws GeneralSecurityException, IOException {
        try {
            String encryptedPasswordFileName = "/tmp/cpr_key.json";
            CprConfiguration configuration = super.getConfiguration();
            configuration.setDirectPasswordPasswordEncryptionFile(new File(encryptedPasswordFileName));
            System.out.println("Encrypted password: " + configuration.getEncryptedDirectPassword());
            System.out.println("Password: " + configuration.getDirectPassword());
        } catch (Exception ioe) {
            log.error("Exception", ioe);
        }
    }*/
}
