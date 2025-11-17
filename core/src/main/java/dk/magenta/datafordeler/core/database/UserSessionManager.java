package dk.magenta.datafordeler.core.database;


import dk.magenta.datafordeler.core.exception.ConfigurationException;
import dk.magenta.datafordeler.core.user.NoDBUserQueryManager;
import dk.magenta.datafordeler.core.user.UserQueryManager;
import dk.magenta.datafordeler.core.user.UserQueryManagerImpl;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.io.IOException;
import java.util.HashSet;
import java.util.Properties;

/**
 * A bean to obtain Sessions with. Autowire this in, and obtain sessions with
 * sessionManager.getSessionFactory().openSession();
 */
@Component
public class UserSessionManager extends SessionManager {

    private static final Logger log = LogManager.getLogger(UserSessionManager.class.getCanonicalName());

    protected boolean enabled() {
        return Boolean.parseBoolean(getEnv("USER_DATABASE_ENABLED", "false"));
    }

    @Bean
    public UserQueryManager userQueryManager() throws ConfigurationException {
        if (this.enabled()) {
            return new UserQueryManagerImpl();
        } else {
            return new NoDBUserQueryManager();
        }
    }

    public UserSessionManager() throws IOException {
        super();
    }

    protected HashSet<Class> managedClasses() {
        HashSet<Class> managedSecondaryClasses = new HashSet<>();
        return managedSecondaryClasses;
    }

    public DataSource dataSource() {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName(System.getenv("USER_DATABASE_CLASS"));
        dataSource.setUrl(System.getenv("USER_DATABASE_URL"));
        dataSource.setUsername(System.getenv("USER_DATABASE_USERNAME"));
        dataSource.setPassword(System.getenv("USER_DATABASE_PASSWORD"));

        System.out.println(System.getenv("USER_DATABASE_CLASS"));
        System.out.println(System.getenv("USER_DATABASE_URL"));
        System.out.println(System.getenv("USER_DATABASE_USERNAME"));

        System.out.println("DataSource created");
        return dataSource;
    }

    protected Properties hibernateProperties() {
        Properties hibernateProperties = new Properties();

        hibernateProperties.setProperty("hibernate.dialect", getEnv("USER_DATABASE_DIALECT", "org.hibernate.dialect.H2Dialect"));
        hibernateProperties.setProperty("hibernate.show_sql", getEnv("USER_DATABASE_SHOW_SQL", "false"));
        hibernateProperties.setProperty("hibernate.hbm2ddl.auto", getEnv("USER_DATABASE_METHOD", "validate"));
        hibernateProperties.setProperty("hibernate.default_schema", getEnv("USER_DATABASE_DEFAULT_SCHEMA", "dbo"));

        hibernateProperties.setProperty("hibernate.connection.username", System.getenv("USER_DATABASE_USERNAME"));
        hibernateProperties.setProperty("hibernate.connection.password", System.getenv("USER_DATABASE_PASSWORD"));

        hibernateProperties.setProperty("hibernate.jdbc.batch_size", "30");
        hibernateProperties.setProperty("hibernate.jdbc.time_zone", "UTC");
        hibernateProperties.setProperty("hibernate.globally_quoted_identifiers", "true");
        hibernateProperties.setProperty("hibernate.globally_quoted_identifiers_skip_column_definitions", "true");
        hibernateProperties.setProperty("hibernate.type.preferred_uuid_jdbc_type", "binary");

        hibernateProperties.setProperty("hibernate.c3p0.min_size", "5");
        hibernateProperties.setProperty("hibernate.c3p0.max_size", "200");
        hibernateProperties.setProperty("hibernate.c3p0.timeout", "300");
        hibernateProperties.setProperty("hibernate.c3p0.max_statements", "50");
        hibernateProperties.setProperty("hibernate.c3p0.idle_test_period", "3000");
        hibernateProperties.setProperty("hibernate.c3p0.debugUnreturnedConnectionStackTraces", "true");
        hibernateProperties.setProperty("hibernate.c3p0.unreturnedConnectionTimeout", "10");

        hibernateProperties.setProperty("hibernate.query.plan_cache_max_size", "1024");
        hibernateProperties.setProperty("hibernate.query.plan_parameter_metadata_max_size", "1024");

        log.info("UserSessionManager properties: "+hibernateProperties.toString());
        return hibernateProperties;
    }

}
