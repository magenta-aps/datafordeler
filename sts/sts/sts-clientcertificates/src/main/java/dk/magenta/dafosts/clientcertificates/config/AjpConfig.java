package dk.magenta.dafosts.clientcertificates.config;

import org.apache.catalina.connector.Connector;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.embedded.EmbeddedServletContainerFactory;
import org.springframework.boot.context.embedded.tomcat.TomcatEmbeddedServletContainerFactory;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


/**
 * Sets up Ajp by adding an ajp connector to the Tomcat embedded servlet container.
 */
@Configuration
@EnableConfigurationProperties(AjpProperties.class)
public class AjpConfig {

    @Autowired
    AjpProperties ajpProperties;

    /**
     * Create a custom factory for the Tomcat embedded servlet container that adds an Ajp connector to
     * created Tomcats.
     * @return EmbeddedServletContainerFactory bean.
     */
    @Bean
    EmbeddedServletContainerFactory servletContainer() {

        TomcatEmbeddedServletContainerFactory tomcat = new TomcatEmbeddedServletContainerFactory();
        if (ajpProperties.isEnabled()) {
            Connector ajpConnector = new Connector("AJP/1.3");
            ajpConnector.setPort(ajpProperties.getAjpPort());
            ajpConnector.setSecure(ajpProperties.isSecure());
            ajpConnector.setAllowTrace(ajpProperties.isAllowTrace());
            ajpConnector.setScheme(ajpProperties.getScheme());
            tomcat.addAdditionalTomcatConnectors(ajpConnector);
        }

        return tomcat;
    }

}
