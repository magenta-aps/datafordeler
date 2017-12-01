package dk.magenta.datafordeler.core.database;

import dk.magenta.datafordeler.core.command.Command;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AssignableTypeFilter;

import javax.annotation.PreDestroy;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

/**
 * A bean to obtain Sessions with. Autowire this in, and obtain sessions with
 * sessionManager.getSessionFactory().openSession();
 */
public class ConfigurationSessionManager {

    private SessionFactory sessionFactory;

    private Logger log = LogManager.getLogger(ConfigurationSessionManager.class);

    private static HashSet<Class> managedClasses = new HashSet<>();
    static {
        managedClasses.add(Command.class);
        managedClasses.add(InterruptedPull.class);
        managedClasses.add(InterruptedPullFile.class);
    }
    public static Set<Class> getManagedClasses() {
        return managedClasses;
    }


    public ConfigurationSessionManager(SessionManagerConfiguration smConfig) {
        try {
            this.log.info("Initialize ConfigurationSessionManager");

            // Create empty configuration object
            Configuration configuration = new Configuration();

            this.log.info("Loading configuration from "+smConfig.getSecondaryHibernateConfigurationFile());
            configuration.configure(smConfig.getSecondaryHibernateConfigurationFile());

            Set<Class> managedClasses = new HashSet<>(ConfigurationSessionManager.managedClasses);

            for (Class cls : managedClasses) {
                this.log.info("Located hardcoded data class "+cls.getCanonicalName());
            }

            ClassPathScanningCandidateComponentProvider componentProvider = new ClassPathScanningCandidateComponentProvider(false);
            componentProvider.addIncludeFilter(new AssignableTypeFilter(dk.magenta.datafordeler.core.configuration.Configuration.class));

            Set<BeanDefinition> components = componentProvider.findCandidateComponents("dk.magenta.datafordeler");
            ClassLoader cl = Thread.currentThread().getContextClassLoader();
            for (BeanDefinition component : components) {
                Class cls = Class.forName(component.getBeanClassName(), true, cl);
                this.log.info("Located autodetected data class "+cls.getCanonicalName());
                managedClasses.add(cls);
            }
            for (Class cls : managedClasses) {
                this.log.info("Adding managed data class "+cls.getCanonicalName());
                configuration.addAnnotatedClass(cls);
            }

            // Create our session factory
            this.log.info("Creating SessionFactory");
            this.sessionFactory = configuration.buildSessionFactory();
        }
        catch (Throwable ex) {
            // Make sure you log the exception, as it might be swallowed
            this.log.error("Initial SessionFactory creation failed.", ex);
            throw new ExceptionInInitializerError(ex);
        }
    }

    /**
     * Get the session factory, used for obtaining Sessions
     */
    public SessionFactory getSessionFactory() {
        return this.sessionFactory;
    }

    @PreDestroy
    public void shutdown() {
        this.log.info("Shutting down SessionManager. Closing SessionFactory.");
        // Close caches and connection pools
        this.sessionFactory.close();
    }

}
