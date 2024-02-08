package dk.magenta.datafordeler.core.database;

//import dk.magenta.datafordeler.core.DatabaseConfiguration;
import dk.magenta.datafordeler.core.DatabaseConfiguration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.h2.engine.Database;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.core.type.filter.AssignableTypeFilter;
import org.springframework.orm.hibernate5.LocalSessionFactoryBean;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.HashSet;
import java.util.Set;

/**
 * A bean to obtain Sessions with. This should be autowired in, and sessions obtained with
 * sessionManager.getSessionFactory().openSession();
 */
@Component
public class SessionManager {
    
    private SessionFactory sessionFactory;

    @Autowired
    private DatabaseConfiguration databaseConfiguration;

    private static final Logger log = LogManager.getLogger(SessionManager.class.getCanonicalName());



    public SessionManager() {
    }
    @PostConstruct
    private void init() {
        this.sessionFactory = this.databaseConfiguration.sessionFactory().getObject();
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
