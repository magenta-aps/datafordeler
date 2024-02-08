package dk.magenta.datafordeler.core.database;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AssignableTypeFilter;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.io.IOException;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

/**
 * A bean to obtain Sessions with. Autowire this in, and obtain sessions with
 * sessionManager.getSessionFactory().openSession();
 */
@Component
public class ConfigurationSessionManager extends SessionManager {

    private static final Logger log = LogManager.getLogger(ConfigurationSessionManager.class.getCanonicalName());

    public ConfigurationSessionManager() throws IOException {
        super();
    }

    protected HashSet<Class> managedClasses() {
        HashSet<Class> managedSecondaryClasses = new HashSet<>();
        managedSecondaryClasses.add(dk.magenta.datafordeler.core.command.Command.class);
        managedSecondaryClasses.add(dk.magenta.datafordeler.core.database.InterruptedPull.class);
        managedSecondaryClasses.add(dk.magenta.datafordeler.core.database.InterruptedPullFile.class);

        ClassPathScanningCandidateComponentProvider componentProvider = new ClassPathScanningCandidateComponentProvider(false);
        componentProvider.addIncludeFilter(new AssignableTypeFilter(dk.magenta.datafordeler.core.configuration.Configuration.class));

        for (Class cls : managedSecondaryClasses) {
            log.info("Located hardcoded configuration data class " + cls.getCanonicalName());
            componentProvider.addExcludeFilter(new AssignableTypeFilter(cls));
        }

        Set<BeanDefinition> components = componentProvider.findCandidateComponents("dk.magenta.datafordeler");
        try {
            ClassLoader cl = Thread.currentThread().getContextClassLoader();
            for (BeanDefinition component : components) {
                Class cls = Class.forName(component.getBeanClassName(), true, cl);
                log.info("Located autodetected secondary data class " + cls.getCanonicalName());
                managedSecondaryClasses.add(cls);
            }
        } catch (Throwable ex) {
            log.error("Initial SessionFactoryBean creation failed.", ex);
            throw new ExceptionInInitializerError(ex);
        }
        return managedSecondaryClasses;
    }

    public DataSource dataSource() {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName(System.getenv("SECONDARY_DATABASE_CLASS"));
        dataSource.setUrl(System.getenv("SECONDARY_DATABASE_URL"));
        dataSource.setUsername(System.getenv("SECONDARY_DATABASE_USERNAME"));
        dataSource.setPassword(System.getenv("SECONDARY_DATABASE_PASSWORD"));
        return dataSource;
    }

    protected Properties hibernateProperties() {
        Properties hibernateProperties = new Properties();

        hibernateProperties.setProperty("hibernate.dialect", getEnv("SECONDARY_DATABASE_DIALECT", "org.hibernate.dialect.H2Dialect"));
        hibernateProperties.setProperty("hibernate.show_sql", getEnv("SECONDARY_DATABASE_SHOW_SQL", "false"));
        hibernateProperties.setProperty("hibernate.hbm2ddl.auto", getEnv("SECONDARY_DATABASE_METHOD", "validate"));
        hibernateProperties.setProperty("hibernate.default_schema", getEnv("SECONDARY_DATABASE_DEFAULT_SCHEMA", "dbo"));

        hibernateProperties.setProperty("hibernate.jdbc.batch_size", "30");
        hibernateProperties.setProperty("hibernate.c3p0.min_size", "5");
        hibernateProperties.setProperty("hibernate.c3p0.max_size", "200");
        hibernateProperties.setProperty("hibernate.c3p0.timeout", "300");
        hibernateProperties.setProperty("hibernate.c3p0.max_statements", "50");
        hibernateProperties.setProperty("hibernate.c3p0.idle_test_period", "3000");

        return hibernateProperties;
    }

}
