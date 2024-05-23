package dk.magenta.datafordeler.core;

import dk.magenta.datafordeler.core.util.Encryption;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ConcurrentTaskScheduler;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.security.GeneralSecurityException;

@ComponentScan({"dk.magenta.datafordeler", "dk.magenta.datafordeler.core", "dk.magenta.datafordeler.core.database", "dk.magenta.datafordeler.core.util"})
@EntityScan("dk.magenta.datafordeler")
@ServletComponentScan
@SpringBootApplication
@EnableScheduling
public class Application {

    private static final Logger log = LogManager.getLogger(Application.class.getCanonicalName());

    public static final int servicePort = 8445;

    public static void main(final String[] args) throws Exception {

        //Used for finding the password for direct lookup in cpr
        if (shouldDecrypt(args)) {
            showDecrypt(args);
            return;
        }

        // Run Spring
        try {
            SpringApplication.run(Application.class, args);
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

    private static boolean shouldDecrypt(String[] args) {
        return (args.length == 3 && args[0].equals("DECRYPT"));
    }
    private static void showDecrypt(String[] args) throws IOException, GeneralSecurityException {
        File encryptionFile = new File(args[1]);
        byte[] lastBytes = Files.readAllBytes(new File(args[2]).toPath());
        String pass = Encryption.decrypt(encryptionFile, lastBytes);
        System.out.println(pass);
    }

}
