package dk.magenta.datafordeler.core.database;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.core.type.filter.AssignableTypeFilter;

import javax.annotation.PreDestroy;
import java.util.HashSet;
import java.util.Set;

/**
 * A bean to obtain Sessions with. This should be autowired in, and sessions obtained with
 * sessionManager.getSessionFactory().openSession();
 */
public class SessionManager {

    private final SessionFactory sessionFactory;

    private static final Logger log = LogManager.getLogger(SessionManager.class.getCanonicalName());

    public SessionManager(SessionManagerConfiguration smConfig) {
        try {
            log.info("Initialize SessionManager");

            // Create empty configuration object
            Configuration configuration = new Configuration();

            log.info("Loading configuration from " + smConfig.getPrimaryHibernateConfigurationFile());
            configuration.configure(smConfig.getPrimaryHibernateConfigurationFile());

            Set<Class> managedClasses = new HashSet<>();
            managedClasses.add(dk.magenta.datafordeler.core.database.Identification.class);
            managedClasses.add(dk.magenta.datafordeler.core.database.Entity.class);
            managedClasses.add(dk.magenta.datafordeler.core.database.Registration.class);
            managedClasses.add(dk.magenta.datafordeler.core.database.Effect.class);
            managedClasses.add(dk.magenta.datafordeler.core.database.DataItem.class);
            managedClasses.add(dk.magenta.datafordeler.core.database.RecordCollection.class);
            managedClasses.add(dk.magenta.datafordeler.core.database.RecordData.class);
            managedClasses.add(dk.magenta.datafordeler.core.database.LastUpdated.class);

            for (Class cls : managedClasses) {
                log.info("Located hardcoded data class " + cls.getCanonicalName());
            }

            ClassPathScanningCandidateComponentProvider componentProvider = new ClassPathScanningCandidateComponentProvider(false);
            componentProvider.addIncludeFilter(new AnnotationTypeFilter(javax.persistence.Entity.class));
            componentProvider.addExcludeFilter(new AssignableTypeFilter(dk.magenta.datafordeler.core.configuration.Configuration.class));

            for (Class cls : ConfigurationSessionManager.getManagedClasses()) {
                componentProvider.addExcludeFilter(new AssignableTypeFilter(cls));
            }

            Set<BeanDefinition> components = componentProvider.findCandidateComponents("dk.magenta.datafordeler");
            ClassLoader cl = Thread.currentThread().getContextClassLoader();
            for (BeanDefinition component : components) {
                Class cls = Class.forName(component.getBeanClassName(), true, cl);
                log.info("Located autodetected data class " + cls.getCanonicalName());
                managedClasses.add(cls);
            }
            for (Class cls : managedClasses) {
                log.info("Adding managed data class " + cls.getCanonicalName());
                configuration.addAnnotatedClass(cls);
            }

            // Create our session factory
            log.info("Creating SessionFactory");
            this.sessionFactory = configuration.buildSessionFactory();
        } catch (Throwable ex) {
            // Make sure you log the exception, as it might be swallowed
            log.error("Initial SessionFactory creation failed.", ex);
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
        log.info("Shutting down SessionManager. Closing SessionFactory.");
        // Close caches and connection pools
        this.sessionFactory.close();
    }

}
