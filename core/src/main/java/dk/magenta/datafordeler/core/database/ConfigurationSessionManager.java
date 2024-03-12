package dk.magenta.datafordeler.core.database;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AssignableTypeFilter;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.orm.hibernate5.LocalSessionFactoryBean;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;
import javax.sql.DataSource;
import java.io.IOException;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;

/**
 * A bean to obtain Sessions with. Autowire this in, and obtain sessions with
 * sessionManager.getSessionFactory().openSession();
 */
//NOTE: Copy(ish) from SessionManager, instead of inheriting, so we have 2 ~identical classes, that aren't related
@Component
public class ConfigurationSessionManager {

    protected SessionFactory sessionFactory;

    private static final Logger log = LogManager.getLogger(ConfigurationSessionManager.class.getCanonicalName());

    public ConfigurationSessionManager() throws IOException {
        LocalSessionFactoryBean sessionFactoryBean = new LocalSessionFactoryBean();
        sessionFactoryBean.setDataSource(dataSource());
        sessionFactoryBean.setPackagesToScan("dk.magenta.datafordeler");
        sessionFactoryBean.setHibernateProperties(this.hibernateProperties());
        for (Class managedClass : managedClasses()) {
            System.out.println(this.getClass().getSimpleName() + " : "+  managedClass.getSimpleName());
            sessionFactoryBean.setAnnotatedClasses(managedClass);
        }
        sessionFactoryBean.afterPropertiesSet();
        this.sessionFactory = sessionFactoryBean.getObject();
    }

    public SessionFactory getSessionFactory() {
        return this.sessionFactory;
    }

    @PreDestroy
    public void shutdown() {
        log.info("Shutting down SessionManager. Closing SessionFactory.");
        this.sessionFactory.close();
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

    protected DataSource dataSource() {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName(System.getenv("SECONDARY_DATABASE_CLASS"));
        dataSource.setUrl(System.getenv("SECONDARY_DATABASE_URL"));
        dataSource.setUsername(System.getenv("SECONDARY_DATABASE_USERNAME"));
        dataSource.setPassword(System.getenv("SECONDARY_DATABASE_PASSWORD"));
        try {
            Connection connection = dataSource.getConnection();
            DatabaseMetaData metaData = connection.getMetaData();
            ResultSet rs = metaData.getTables(null, null, "%", null);
            while (rs.next()) {
                System.out.println(connection.getCatalog() + ": " + rs.getString("TABLE_NAME"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return dataSource;
    }

    protected static String getEnv(String key, String fallback) {
        String value = System.getenv(key);
        return (value != null) ? value : fallback;
    }

    protected Properties hibernateProperties() {
        Properties hibernateProperties = new Properties();

        hibernateProperties.setProperty("hibernate.dialect", getEnv("SECONDARY_DATABASE_DIALECT", "org.hibernate.dialect.H2Dialect"));
        hibernateProperties.setProperty("hibernate.show_sql", getEnv("SECONDARY_DATABASE_SHOW_SQL", "false"));
        hibernateProperties.setProperty("hibernate.hbm2ddl.auto", getEnv("SECONDARY_DATABASE_METHOD", "validate"));
        hibernateProperties.setProperty("hibernate.default_schema", getEnv("SECONDARY_DATABASE_DEFAULT_SCHEMA", "dbo"));

        hibernateProperties.setProperty("hibernate.connection.username", System.getenv("DATABASE_USERNAME"));
        hibernateProperties.setProperty("hibernate.connection.password", System.getenv("DATABASE_PASSWORD"));

        hibernateProperties.setProperty("hibernate.jdbc.batch_size", "30");
        hibernateProperties.setProperty("hibernate.c3p0.min_size", "5");
        hibernateProperties.setProperty("hibernate.c3p0.max_size", "200");
        hibernateProperties.setProperty("hibernate.c3p0.timeout", "300");
        hibernateProperties.setProperty("hibernate.c3p0.max_statements", "50");
        hibernateProperties.setProperty("hibernate.c3p0.idle_test_period", "3000");

        return hibernateProperties;
    }

}
