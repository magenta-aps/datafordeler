package dk.magenta.datafordeler.core.database.setup;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.IOException;

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

    @PostConstruct
    private void init() throws IOException {
        this.sessionFactory = this.databaseConfiguration.sessionFactory().getObject();
    }

    public SessionFactory getSessionFactory() {
        return this.sessionFactory;
    }

    @PreDestroy
    public void shutdown() {
        log.info("Shutting down SessionManager. Closing SessionFactory.");
        this.sessionFactory.close();
    }

}
