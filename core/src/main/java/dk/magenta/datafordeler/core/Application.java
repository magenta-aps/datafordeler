package dk.magenta.datafordeler.core;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import dk.magenta.datafordeler.core.database.setup.ConfigurationSessionManager;
import dk.magenta.datafordeler.core.database.setup.SessionManager;
import dk.magenta.datafordeler.core.util.Encryption;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ConcurrentTaskScheduler;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.regex.Matcher;

@ComponentScan({"dk.magenta.datafordeler", "dk.magenta.datafordeler.core", "dk.magenta.datafordeler.core.database", "dk.magenta.datafordeler.core.util"})
@EntityScan("dk.magenta.datafordeler")
@ServletComponentScan
@SpringBootApplication
@EnableScheduling
public class Application {

    @Autowired
    PluginManager pluginManager;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    XmlMapper xmlMapper;

    @Autowired
    SessionManager sessionManager;

    @Autowired
    ConfigurationSessionManager configurationSessionManager;

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
            System.out.println("RUN SPRING APPLICATION");
            SpringApplication.run(Application.class, args);
            System.out.println("SPRING APPLICATION FINISHED RUNNING");
        } catch (Throwable e) {
            log.error(e);
            while (e != null) {
                if (e instanceof com.sun.xml.bind.v2.runtime.IllegalAnnotationsException) {
                    log.error(((com.sun.xml.bind.v2.runtime.IllegalAnnotationsException) e).getErrors());
                }
                e = e.getCause();
            }
        }
    }

    private static InputStream getConfigStream(String path) {
        int typeSepIndex = path.indexOf(":");
        if (typeSepIndex != -1) {
            String type = path.substring(0, typeSepIndex);
            String value = path.substring(typeSepIndex + 1);
            switch (type) {
                case "file":
                    value = value.replaceAll("\\\\+", Matcher.quoteReplacement(File.separator));
                    try {
                        return new FileInputStream(value);
                    } catch (FileNotFoundException e) {
                        log.warn("Config file not found: " + value);
                    }
            }
        }
        return null;
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
