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
 * A bean to obtain Sessions with. This should be autowired in, and sessions obtained with
 * sessionManager.getSessionFactory().openSession();
 */
@Component
public class SessionManager {
    
    private SessionFactory sessionFactory;

    @Autowired
    private DatabaseConfiguration databaseConfiguration;

    private static final Logger log = LogManager.getLogger(SessionManager.class.getCanonicalName());

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
