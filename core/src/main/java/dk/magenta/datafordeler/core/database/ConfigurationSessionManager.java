package dk.magenta.datafordeler.core.database;


import dk.magenta.datafordeler.core.DatabaseConfiguration;
import dk.magenta.datafordeler.core.SecondaryDatabaseConfiguration;
import dk.magenta.datafordeler.core.command.Command;
//import dk.magenta.datafordeler.core.SecondaryDatabaseConfiguration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AssignableTypeFilter;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.HashSet;
import java.util.Set;

/**
 * A bean to obtain Sessions with. Autowire this in, and obtain sessions with
 * sessionManager.getSessionFactory().openSession();
 */
@Component
public class ConfigurationSessionManager {

    private SessionFactory sessionFactory;

    @Autowired
    private SecondaryDatabaseConfiguration databaseConfiguration;

    private static final Logger log = LogManager.getLogger(ConfigurationSessionManager.class.getCanonicalName());

    private static final HashSet<Class> managedClasses = new HashSet<>();

    static {
        managedClasses.add(Command.class);
        managedClasses.add(InterruptedPull.class);
        managedClasses.add(InterruptedPullFile.class);
    }

    public static Set<Class> getManagedClasses() {
        return managedClasses;
    }


    public ConfigurationSessionManager() {
    }

    @PostConstruct
    private void init() {
        this.sessionFactory = this.databaseConfiguration.secondarySessionFactory().getObject();
    }

    /**
     * Get the session factory, used for obtaining Sessions
     */
    public SessionFactory getSessionFactory() {


        return this.sessionFactory;
    }

    @PreDestroy
    public void shutdown() {
        log.info("Shutting down SessionManager. Closing SessionFactory.");
        // Close caches and connection pools
        this.sessionFactory.close();
    }

}
