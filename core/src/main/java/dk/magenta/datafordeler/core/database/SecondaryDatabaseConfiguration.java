package dk.magenta.datafordeler.core;

import dk.magenta.datafordeler.core.database.DatabaseEntry;
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
public class SecondaryDatabaseConfiguration {

    private static final Logger log = LogManager.getLogger(SecondaryDatabaseConfiguration.class.getCanonicalName());

    public HashSet<Class> managedSecondaryClasses() {
        System.out.println("SETTING UP SECONDARY MANAGED CLASSES");
        HashSet<Class> managedSecondaryClasses = new HashSet<>();
        managedSecondaryClasses.add(dk.magenta.datafordeler.core.command.Command.class);
        managedSecondaryClasses.add(dk.magenta.datafordeler.core.database.InterruptedPull.class);
        managedSecondaryClasses.add(dk.magenta.datafordeler.core.database.InterruptedPullFile.class);

        for (Class cls : managedSecondaryClasses) {
            log.info("Located hardcoded secondary data class " + cls.getCanonicalName());
        }
        ClassPathScanningCandidateComponentProvider componentProvider = new ClassPathScanningCandidateComponentProvider(false);
        componentProvider.addIncludeFilter(new AssignableTypeFilter(dk.magenta.datafordeler.core.configuration.Configuration.class));


//        for (Class cls : ConfigurationSessionManager.getManagedClasses()) {
//            componentProvider.addExcludeFilter(new AssignableTypeFilter(cls));
//        }
//

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
        System.out.println("DONE");
        return managedSecondaryClasses;
    }

    public LocalSessionFactoryBean secondarySessionFactory() {
        System.out.println("SECONDARY DATABASE CONFIGURATION STARTED");
        LocalSessionFactoryBean secondarySessionFactory = new LocalSessionFactoryBean();
        secondarySessionFactory.setDataSource(secondaryDataSource());
        secondarySessionFactory.setPackagesToScan("dk.magenta.datafordeler");
        secondarySessionFactory.setHibernateProperties(hibernateProperties());
        for (Class managedClass : managedSecondaryClasses()) {
            secondarySessionFactory.setAnnotatedClasses(managedClass);
        }
        try {
            secondarySessionFactory.afterPropertiesSet();
            System.out.println("AFTERPROPERTIESSET");
            System.out.println(secondarySessionFactory.getObject());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return secondarySessionFactory;
    }

    public DataSource secondaryDataSource() {
        System.out.println("SET UP SECONDARY DATASOURCE");
        DriverManagerDataSource secondaryDataSource = new DriverManagerDataSource();
        secondaryDataSource.setDriverClassName(System.getenv("SECONDARY_DATABASE_CLASS"));
        secondaryDataSource.setUrl(System.getenv("SECONDARY_DATABASE_URL"));
        secondaryDataSource.setUsername(System.getenv("SECONDARY_DATABASE_USERNAME"));
        secondaryDataSource.setPassword(System.getenv("SECONDARY_DATABASE_PASSWORD"));
        return secondaryDataSource;
    }

    private static String getEnv(String key, String fallback) {
        String value = System.getenv(key);
        return (value != null) ? value : fallback;
    }

    private final Properties hibernateProperties() {
        System.out.println("SET UP SECONDARY HIBERNATE PROPERTIES");
        Properties hibernateProperties = new Properties();

        hibernateProperties.setProperty("hibernate.dialect", getEnv("DATABASE_DIALECT", "org.hibernate.dialect.H2Dialect"));
        hibernateProperties.setProperty("hibernate.show_sql", getEnv("DATABASE_SHOW_SQL", "false"));
        hibernateProperties.setProperty("hibernate.hbm2ddl.auto", getEnv("SECONDARY_DATABASE_METHOD", "validate"));
        hibernateProperties.setProperty("hibernate.default_schema", getEnv("SECONDARY_DATABASE_DEFAULT_SCHEMA", "dbo"));

        hibernateProperties.setProperty("hibernate.jdbc.batch_size", "30");
        hibernateProperties.setProperty("hibernate.c3p0.min_size", "5");
        hibernateProperties.setProperty("hibernate.c3p0.max_size", "200");
        hibernateProperties.setProperty("hibernate.c3p0.timeout", "300");
        hibernateProperties.setProperty("hibernate.c3p0.max_statements", "50");
        hibernateProperties.setProperty("hibernate.c3p0.idle_test_period", "3000");

        System.out.println(hibernateProperties.toString());
        return hibernateProperties;
    }
}
