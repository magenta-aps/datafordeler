package dk.magenta.dafosts.clientcertificates.config;

import dk.magenta.dafosts.library.SharedConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

@Configuration
public class MvcConfig extends WebMvcConfigurerAdapter {

    @Autowired
    SharedConfig sharedConfig;


    /**
     * Import the shared views.
     * @param registry The view controller registry
     */
    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        sharedConfig.addViewControllers(registry);
    }
}
