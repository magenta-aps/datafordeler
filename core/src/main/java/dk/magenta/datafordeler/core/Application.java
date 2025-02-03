package dk.magenta.datafordeler.core;

import dk.magenta.datafordeler.core.util.Encryption;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ConcurrentTaskScheduler;

import java.io.File;
import java.nio.file.Files;

@ComponentScan({"dk.magenta.datafordeler", "dk.magenta.datafordeler.core", "dk.magenta.datafordeler.core.database", "dk.magenta.datafordeler.core.util", "dk.magenta.datafordeler.core.user"})
@EntityScan("dk.magenta.datafordeler")
@ServletComponentScan
@SpringBootApplication
@EnableScheduling
@PropertySource("classpath:/application.properties")

public class Application {

    private static final Logger log = LogManager.getLogger(Application.class.getCanonicalName());

    public static final int servicePort = 8445;

    public static void main(final String[] args) throws Exception {

        //Used for finding the password for direct lookup in cpr
        if (args.length == 3 && args[0].equals("DECRYPT")) {
            File encryptionFile = new File(args[1]);
            byte[] lastBytes = Files.readAllBytes(new File(args[2]).toPath());
            String pass = Encryption.decrypt(encryptionFile, lastBytes);
            System.out.println(pass);
            return;
        }

        // Run Spring
        try {
            SpringApplication.run(Application.class, args);
        } catch (Throwable e) {
            log.error(e);
        }
    }

    @Bean
    public TaskScheduler taskScheduler() {
        return new ConcurrentTaskScheduler(); //single threaded by default
    }

    @Bean
    public static PropertySourcesPlaceholderConfigurer propertyConfigInDev() {
        PropertySourcesPlaceholderConfigurer p = new PropertySourcesPlaceholderConfigurer();
        p.setIgnoreUnresolvablePlaceholders(true);
        return p;
    }

}
