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

import javax.sql.DataSource;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.Properties;

@Configuration
//public class DatabaseConfiguration extends org.hibernate.cfg.Configuration {
public class SecondaryDatabaseConfiguration {

    //private static final Logger log = LogManager.getLogger(SessionManager.class.getCanonicalName());

    @Bean
    public HashSet<Class> managedSecondaryClasses() {
        HashSet<Class> managedSecondaryClasses = new HashSet<Class>();
        managedSecondaryClasses.add(dk.magenta.datafordeler.core.command.Command.class);
        managedSecondaryClasses.add(dk.magenta.datafordeler.core.database.InterruptedPull.class);
        managedSecondaryClasses.add(dk.magenta.datafordeler.core.database.InterruptedPullFile.class);

        Iterator<Class> itr = managedSecondaryClasses.iterator();
        /*
        for (Class cls : managedSecondaryClasses) {
            log.info("Located hardcoded data class " + cls.getCanonicalName());
        }*/
        ClassPathScanningCandidateComponentProvider componentProvider = new ClassPathScanningCandidateComponentProvider(false);
        componentProvider.addIncludeFilter(new AssignableTypeFilter(dk.magenta.datafordeler.core.configuration.Configuration.class));

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
                //log.info("Located autodetected data class " + cls.getCanonicalName());
                managedSecondaryClasses.add(cls);
            }
        } catch (Throwable ex) {
            //log.error("Initial SessionFactoryBean creation failed.", ex);
            throw new ExceptionInInitializerError(ex);
        }
        return managedSecondaryClasses;
    }

    @Bean
    public LocalSessionFactoryBean secondarySessionFactory() {
        LocalSessionFactoryBean secondarySessionFactory = new LocalSessionFactoryBean();
        secondarySessionFactory.setDataSource(secondaryDataSource());
        secondarySessionFactory.setPackagesToScan("dk.magenta.datafordeler");
        secondarySessionFactory.setHibernateProperties(hibernateProperties());
        while (managedSecondaryClasses().iterator().hasNext()) {
            secondarySessionFactory.setAnnotatedClasses(managedSecondaryClasses().iterator().next());
        }
        return secondarySessionFactory;
    }

    @Bean
    public DataSource secondaryDataSource() {
        DriverManagerDataSource secondaryDataSource = new DriverManagerDataSource();
        secondaryDataSource.setDriverClassName(System.getenv("SECONDARY_DATABASE_CLASS"));
        secondaryDataSource.setUrl(System.getenv("SECONDARY_DATABASE_URL"));
        secondaryDataSource.setUsername(System.getenv("SECONDARY_DATABASE_USERNAME"));
        secondaryDataSource.setPassword(System.getenv("SECONDARY_DATABASE_PASSWORD"));
        return secondaryDataSource;
    }

    private final Properties hibernateProperties() {
        Properties hibernateProperties = new Properties();

        hibernateProperties.setProperty("hibernate.dialect",
            (System.getenv("DATABASE_DIALECT") != null)
            ? System.getenv("DATABASE_DIALECT") : "org.hibernate.dialect.H2Dialect");
        hibernateProperties.setProperty("hibernate.show_sql",
            (System.getenv("DATABASE_SHOW_SQL") != null)
            ? System.getenv("DATABASE_SHOW_SQL") : "false");
        hibernateProperties.setProperty("hibernate.hbm2ddl.auto",
            (System.getenv("SECONDARY_DATABASE_METHOD") != null)
            ? System.getenv("SECONDARY_DATABASE_METHOD") : "validate");
        hibernateProperties.setProperty("hibernate.default_schema",
            (System.getenv("SECONDARY_DATABASE_DEFAULT_SCHEMA") != null)
            ? System.getenv("SECONDARY_DATABASE_DEFAULT_SCHEMA") : null);
        hibernateProperties.setProperty("hibernate.jdbc.batch_size", "30");
        hibernateProperties.setProperty("hibernate.c3p0.min_size", "5");
        hibernateProperties.setProperty("hibernate.c3p0.max_size", "200");
        hibernateProperties.setProperty("hibernate.c3p0.timeout", "300");
        hibernateProperties.setProperty("hibernate.c3p0.max_statements", "50");
        hibernateProperties.setProperty("hibernate.c3p0.idle_test_period", "3000");

        return hibernateProperties;
    }
}
