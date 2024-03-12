package dk.magenta.datafordeler.core.database;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AssignableTypeFilter;
import org.springframework.core.type.filter.AnnotationTypeFilter;
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
 * A bean to obtain Sessions with. This should be autowired in, and sessions obtained with
 * sessionManager.getSessionFactory().openSession();
 */
@Component
public class SessionManager {
    
    protected SessionFactory sessionFactory;

    private static final Logger log = LogManager.getLogger(SessionManager.class.getCanonicalName());

    public SessionManager() throws IOException {
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
        HashSet<Class> managedClasses = new HashSet<Class>();
        managedClasses.add(dk.magenta.datafordeler.core.database.Identification.class);
        managedClasses.add(dk.magenta.datafordeler.core.database.Entity.class);
        managedClasses.add(dk.magenta.datafordeler.core.database.Registration.class);
        managedClasses.add(dk.magenta.datafordeler.core.database.Effect.class);
        managedClasses.add(dk.magenta.datafordeler.core.database.DataItem.class);
        managedClasses.add(dk.magenta.datafordeler.core.database.RecordCollection.class);
        managedClasses.add(dk.magenta.datafordeler.core.database.RecordData.class);
        managedClasses.add(dk.magenta.datafordeler.core.database.LastUpdated.class);

        ClassPathScanningCandidateComponentProvider componentProvider = new ClassPathScanningCandidateComponentProvider(false);
        componentProvider.addIncludeFilter(new AnnotationTypeFilter(javax.persistence.Entity.class));
        componentProvider.addExcludeFilter(new AssignableTypeFilter(dk.magenta.datafordeler.core.configuration.Configuration.class));

        for (Class cls : managedClasses) {
            log.info("Located hardcoded data class " + cls.getCanonicalName());
            componentProvider.addExcludeFilter(new AssignableTypeFilter(cls));
        }

        Set<BeanDefinition> components = componentProvider.findCandidateComponents("dk.magenta.datafordeler");
        try {
            ClassLoader cl = Thread.currentThread().getContextClassLoader();
            for (BeanDefinition component : components) {
                Class cls = Class.forName(component.getBeanClassName(), true, cl);
                log.info("Located autodetected data class " + cls.getCanonicalName());
                managedClasses.add(cls);
            }
        } catch (Throwable ex) {
            log.error("Initial SessionFactoryBean creation failed.", ex);
            throw new ExceptionInInitializerError(ex);
        }
        return managedClasses;
    }

    protected DataSource dataSource() {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName(System.getenv("DATABASE_CLASS"));
        dataSource.setUrl(System.getenv("DATABASE_URL"));
        dataSource.setUsername(System.getenv("DATABASE_USERNAME"));
        dataSource.setPassword(System.getenv("DATABASE_PASSWORD"));
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

        hibernateProperties.setProperty("hibernate.dialect", getEnv("DATABASE_DIALECT", "org.hibernate.spatial.dialect.sqlserver.SqlServer2008SpatialDialect"));
        hibernateProperties.setProperty("hibernate.show_sql", getEnv("DATABASE_SHOW_SQL", "false"));
        hibernateProperties.setProperty("hibernate.hbm2ddl.auto", getEnv("DATABASE_METHOD", "validate"));
        hibernateProperties.setProperty("hibernate.default_schema", getEnv("DATABASE_DEFAULT_SCHEMA", "dbo"));

        hibernateProperties.setProperty("hibernate.connection.username", System.getenv("DATABASE_USERNAME"));
        hibernateProperties.setProperty("hibernate.connection.password", System.getenv("DATABASE_PASSWORD"));

        hibernateProperties.setProperty("hibernate.hbm2ddl.auto", "update");
        hibernateProperties.setProperty("hibernate.jdbc.batch_size", "30");
        hibernateProperties.setProperty("hibernate.c3p0.min_size", "5");
        hibernateProperties.setProperty("hibernate.c3p0.max_size", "200");
        hibernateProperties.setProperty("hibernate.c3p0.timeout", "300");
        hibernateProperties.setProperty("hibernate.c3p0.max_statements", "50");
        hibernateProperties.setProperty("hibernate.c3p0.idle_test_period", "3000");
        return hibernateProperties;
    }

}
