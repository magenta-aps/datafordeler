package dk.magenta.datafordeler.core.configuration;

import dk.magenta.datafordeler.core.Environment;
import dk.magenta.datafordeler.core.database.ConfigurationSessionManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.Transaction;

import javax.persistence.NoResultException;

/**
 * Plugin configurations are stored in separate database tables, each with only one
 * row. On loading the program, the Configuration object is retrieved from the database, or
 * if one doesn’t exist (such as on the first run), created and saved.
 */
public abstract class ConfigurationManager<C extends Configuration> extends Environment {

    /**
     * Locate Configuration object, or create one if none found.
     * Subclasses MUST call this in their initialization routines
     */
    public void init() {
        Session session = this.getSessionManager().getSessionFactory().openSession();
        Transaction transaction = session.beginTransaction();
        Class<C> configurationClass = this.getConfigurationClass();
        try {
            session.createQuery("select c from " + configurationClass.getSimpleName() + " c", configurationClass).getSingleResult();
            this.getLog().info("Loaded configuration object");
        } catch (NoResultException e) {
            this.getLog().info("No configuration object exists, create one.");
            session.persist(this.createConfiguration());
            transaction.commit();
        } finally {
            session.close();
        }
    }

    /**
     * Get the specific class object, a subclass of Configuration
     *
     * @return
     */
    protected abstract Class<C> getConfigurationClass();

    /**
     * Create a new Configuration object, empty or with default values, because one does not already exist
     *
     * @return
     */
    protected abstract C createConfiguration();

    /**
     * Return a session manager
     *
     * @return
     */
    protected abstract ConfigurationSessionManager getSessionManager();

    /**
     * Retrieve the configuration object from the database
     *
     * @return
     */
    public C getConfiguration() {
        // This should always fetch fresh data from the database as that data might have been
        // changed.
        Session session = this.getSessionManager().getSessionFactory().openSession();
        C configuration;
        try {
            Class<C> configurationClass = this.getConfigurationClass();
            configuration = session.createQuery(
                    "select c from " + configurationClass.getSimpleName() + " c",
                    configurationClass
            ).getSingleResult();
            session.refresh(configuration);
            return configuration;
        } finally {
            session.close();
        }
    }

    protected abstract Logger getLog();
}
