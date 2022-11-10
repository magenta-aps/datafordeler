package dk.magenta.datafordeler.core;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import dk.magenta.datafordeler.core.database.SessionManager;
import dk.magenta.datafordeler.core.exception.ConfigurationException;
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

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.util.Properties;
import java.util.regex.Matcher;

@ComponentScan({"dk.magenta.datafordeler", "dk.magenta.datafordeler.core", "dk.magenta.datafordeler.core.gapi", "dk.magenta.datafordeler.core.database", "dk.magenta.datafordeler.core.util"})
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

        // We need the jarFolderPath before starting Spring, so load it from properties the old-fashioned way
        Properties defaultProperties = new Properties();
        defaultProperties.load(Application.class.getResourceAsStream("/application.properties"));
        Properties properties = new Properties(defaultProperties);
        String argSearch = "--spring.config.location=";
        for (String arg : args) {
            if (arg.startsWith(argSearch)) {
                String value = arg.substring(argSearch.length());
                for (String path : value.split(",")) {
                    InputStream stream = getConfigStream(path);
                    if (stream != null) {
                        properties.load(stream);
                        stream.close();
                    }
                }
                break;
            }
        }

        String jarFolderPath = properties.getProperty("dafo.plugins.folder");
        log.info("Plugin folder path: " + jarFolderPath);

        // Jam the jar files on the given path into the classloader
        if (jarFolderPath != null) {
            extendClassLoader(Thread.currentThread().getContextClassLoader(), jarFolderPath);
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

    // Seriously unholy reflection magic, to force our URLs into the existing classloader
    private static void extendClassLoader(ClassLoader classLoader, String jarFolderPath) throws ConfigurationException {
        URLClassLoader urlClassLoader = (URLClassLoader) classLoader;
        try {
            Method method = URLClassLoader.class.getDeclaredMethod("addURL", URL.class);
            method.setAccessible(true);
            File pluginFolder = new File(jarFolderPath);
            if (pluginFolder.isDirectory()) {
                File[] files = pluginFolder.listFiles((dir, name) -> name.endsWith(".jar"));
                if (files != null) {
                    for (File file : files) {
                        try {
                            method.invoke(urlClassLoader, file.toURI().toURL());
                        } catch (MalformedURLException e) {
                            log.error("Invalid URL for Jar file", e);
                        }
                    }
                }
            } else {
                String pathDescription = jarFolderPath;
                try {
                    pathDescription += " => " + pluginFolder.getCanonicalPath();
                } catch (IOException e) {
                }
                throw new ConfigurationException("Configured plugin folder path " + pathDescription + " is not a folder");
            }
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            log.error(e);
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
