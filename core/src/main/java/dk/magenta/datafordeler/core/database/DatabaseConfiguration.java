package dk.magenta.datafordeler.core;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.core.type.filter.AssignableTypeFilter;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.orm.hibernate5.LocalSessionFactoryBean;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.Properties;

@Component
public class DatabaseConfiguration {

    private static final Logger log = LogManager.getLogger(DatabaseConfiguration.class.getCanonicalName());

    //@Bean
    public HashSet<Class> managedClasses() {
        System.out.println("SETTING UP THE MANAGED CLASSES");
        HashSet<Class> managedClasses = new HashSet<Class>();
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

        for (Class cls : managedClasses) {
            log.info("Located hardcoded data class " + cls.getCanonicalName());
            componentProvider.addExcludeFilter(new AssignableTypeFilter(cls));
        }
        /*
        for (Class cls : ConfigurationSessionManager.getManagedClasses()) {
            componentProvider.addExcludeFilter(new AssignableTypeFilter(cls));
        }
        */

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

    public LocalSessionFactoryBean sessionFactory() {
        System.out.println("DATABASE CONFIGURATION STARTED");
        LocalSessionFactoryBean sessionFactory = new LocalSessionFactoryBean();
        sessionFactory.setDataSource(dataSource());
        sessionFactory.setPackagesToScan("dk.magenta.datafordeler", "dk.magenta.datafordeler.database");
        sessionFactory.setHibernateProperties(hibernateProperties());
        for (Class managedClass : managedClasses()) {
            sessionFactory.setAnnotatedClasses(managedClass);
        }
        try {
            sessionFactory.afterPropertiesSet();
            System.out.println("AFTERPROPERTIESSET");
            System.out.println(sessionFactory.getObject());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return sessionFactory;
    }

    public DataSource dataSource() {
        System.out.println("SET UP PRIMARY DATASOURCE");
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        System.out.println("SET DATABASE CLASS TO " + System.getenv("DATABASE_CLASS"));
        dataSource.setDriverClassName(System.getenv("DATABASE_CLASS"));
        System.out.println("SET DATABASE URL TO " + System.getenv("DATABASE_URL"));
        dataSource.setUrl(System.getenv("DATABASE_URL"));
        System.out.println("SET DATABASE USERNAME");
        dataSource.setUsername(System.getenv("DATABASE_USERNAME TO ") + System.getenv("DATABASE_USERNAME"));
        System.out.println("SET DATABASE PASSWORD");
        dataSource.setPassword(System.getenv("DATABASE_PASSWORD"));
        System.out.println("PRIMARY DATASOURCE SETUP COMPLETE");
        return dataSource;
    }

    private static String getEnv(String key, String fallback) {
        String value = System.getenv(key);
        return (value != null) ? value : fallback;
    }

    private Properties hibernateProperties() {
        System.out.println("CREATE PRIMARY HIBERNATE PROPERTIES");
        Properties hibernateProperties = new Properties();

        hibernateProperties.setProperty("hibernate.dialect", getEnv("DATABASE_DIALECT", "org.hibernate.spatial.dialect.sqlserver.SqlServer2008SpatialDialect"));
        hibernateProperties.setProperty("hibernate.show_sql", getEnv("DATABASE_SHOW_SQL", "false"));
        hibernateProperties.setProperty("hibernate.hbm2ddl.auto", getEnv("SECONDARY_DATABASE_METHOD", "validate"));
        hibernateProperties.setProperty("hibernate.default_schema", getEnv("SECONDARY_DATABASE_DEFAULT_SCHEMA", "dbo"));

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
