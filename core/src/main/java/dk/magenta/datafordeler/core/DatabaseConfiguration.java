package dk.magenta.datafordeler.core;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.orm.hibernate5.LocalSessionFactoryBean;

import javax.sql.DataSource;
import java.util.Properties;

@Configuration
public class DatabaseConfiguration {

    @Bean
    public LocalSessionFactoryBean sessionFactory() {
        LocalSessionFactoryBean sessionFactory = new LocalSessionFactoryBean();
        sessionFactory.setDataSource(dataSource());
        sessionFactory.setPackagesToScan("dk.magenta.datafordeler");
        sessionFactory.setHibernateProperties(hibernateProperties());
        return sessionFactory;
    }

    @Bean
    public DataSource dataSource() {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName(System.getenv("DATABASE_CLASS"));
        dataSource.setUrl(System.getenv("DATABASE_URL"));
        dataSource.setUsername("test");
        dataSource.setPassword("test");
        return dataSource;
    }

    private final Properties hibernateProperties() {
        Properties hibernateProperties = new Properties();

        hibernateProperties.setProperty("hibernate.dialect", "org.hibernate.dialect.H2Dialect");
        hibernateProperties.setProperty("hibernate.hbm2ddl.auto", "update");
        hibernateProperties.setProperty("hibernate.show_sql", "false");
        hibernateProperties.setProperty("hibernate.jdbc.batch_size", "30");
        hibernateProperties.setProperty("hibernate.c3p0.min_size", "5");
        hibernateProperties.setProperty("hibernate.c3p0.max_size", "200");
        hibernateProperties.setProperty("hibernate.c3p0.timeout", "300");
        hibernateProperties.setProperty("hibernate.c3p0.max_statements", "50");
        hibernateProperties.setProperty("hibernate.c3p0.idle_test_period", "3000");

        return hibernateProperties;
    }
}


/*
    <property name="connection.url">jdbc:h2:mem:db1;DB_CLOSE_DELAY=-1;MVCC=TRUE</property>
    <property name="connection.driver_class">org.h2.Driver</property>
    <property name="connection.username">test</property>
    <property name="connection.password">test</property>
    <property name="hibernate.hbm2ddl.auto">update</property>
    <property name="hibernate.show_sql">false</property>
    <!--<property name="hibernate.default_schema">dbo</property>-->
    <property name="hibernate.jdbc.batch_size">30</property>

    <property name="hibernate.c3p0.min_size">5</property>
    <property name="hibernate.c3p0.max_size">200</property>
    <property name="hibernate.c3p0.timeout">300</property>
    <property name="hibernate.c3p0.max_statements">50</property>
    <property name="hibernate.c3p0.idle_test_period">3000</property>
* */
