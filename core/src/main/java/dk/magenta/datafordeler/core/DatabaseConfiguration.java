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
public class DatabaseConfiguration {

    //private static final Logger log = LogManager.getLogger(SessionManager.class.getCanonicalName());

    @Bean
    public HashSet<Class> managedClasses() {
        HashSet<Class> managedClasses = new HashSet<Class>();
        managedClasses.add(dk.magenta.datafordeler.core.database.Identification.class);
        managedClasses.add(dk.magenta.datafordeler.core.database.Entity.class);
        managedClasses.add(dk.magenta.datafordeler.core.database.Registration.class);
        managedClasses.add(dk.magenta.datafordeler.core.database.Effect.class);
        managedClasses.add(dk.magenta.datafordeler.core.database.DataItem.class);
        managedClasses.add(dk.magenta.datafordeler.core.database.RecordCollection.class);
        managedClasses.add(dk.magenta.datafordeler.core.database.RecordData.class);
        managedClasses.add(dk.magenta.datafordeler.core.database.LastUpdated.class);

        Iterator<Class> itr = managedClasses.iterator();
        /*
        for (Class cls : managedClasses) {
            log.info("Located hardcoded data class " + cls.getCanonicalName());
        }*/
        ClassPathScanningCandidateComponentProvider componentProvider = new ClassPathScanningCandidateComponentProvider(false);
        componentProvider.addIncludeFilter(new AnnotationTypeFilter(javax.persistence.Entity.class));
        componentProvider.addExcludeFilter(new AssignableTypeFilter(dk.magenta.datafordeler.core.configuration.Configuration.class));

        while (itr.hasNext()) {
            Class cls = itr.next();
            //log.info("Located hardcoded data class " + cls.getCanonicalName());
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
                //log.info("Located autodetected data class " + cls.getCanonicalName());
                managedClasses.add(cls);
            }
        } catch (Throwable ex) {
            //log.error("Initial SessionFactoryBean creation failed.", ex);
            throw new ExceptionInInitializerError(ex);
        }
        return managedClasses;
    }

    @Bean
    public LocalSessionFactoryBean sessionFactory() {
        LocalSessionFactoryBean sessionFactory = new LocalSessionFactoryBean();
        sessionFactory.setDataSource(dataSource());
        sessionFactory.setPackagesToScan("dk.magenta.datafordeler");
        sessionFactory.setHibernateProperties(hibernateProperties());
        while (managedClasses().iterator().hasNext()) {
            sessionFactory.setAnnotatedClasses(managedClasses().iterator().next());
        }
        return sessionFactory;
    }

    @Bean
    public DataSource dataSource() {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName(System.getenv("DATABASE_CLASS"));
        dataSource.setUrl(System.getenv("DATABASE_URL"));
        dataSource.setUsername(System.getenv("DATABASE_USERNAME"));
        dataSource.setPassword(System.getenv("DATABASE_PASSWORD"));
        return dataSource;
    }

    private final Properties hibernateProperties() {
        Properties hibernateProperties = new Properties();

        hibernateProperties.setProperty("hibernate.dialect",
            (System.getenv("DATABASE_DIALECT") != null)
            ? System.getenv("DATABASE_DIALECT") : "org.hibernate.dialect.H2Dialect");
        hibernateProperties.setProperty("hibernate.show_sql",
            (System.getenv("DATABASE_SHOW_SQL") != null)
            ? System.getenv("DATABASE_SHOW_SQL") : "false");
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
