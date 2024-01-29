package dk.magenta.datafordeler.core.database;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

/**
 * Configuration class for creating the Session Manager based on an external file
 */
@Component
public class SessionManagerConfiguration {

    @Value("${dafo.hibernate.configuration-file:/hibernate.cfg.xml}")
    private String primaryHibernateConfigurationFile;

    @Value("${dafo.hibernate.secondary-configuration-file:/hibernate_config.cfg.xml}")
    private String secondaryHibernateConfigurationFile;

    public String getPrimaryHibernateConfigurationFile() {
        return primaryHibernateConfigurationFile;
    }

    public String getSecondaryHibernateConfigurationFile() {
        return secondaryHibernateConfigurationFile;
    }
    
    /*
    @Bean
    public SessionManager sessionManager(SessionManagerConfiguration configuration) {
        return new SessionManager(configuration);
    }
    */

    @Bean
    public ConfigurationSessionManager configurationSessionManager(SessionManagerConfiguration configuration) {
        return new ConfigurationSessionManager(configuration);
    }
}
