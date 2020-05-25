package dk.magenta.dafosts.saml.config;

import dk.magenta.dafosts.library.SharedConfig;
import dk.magenta.dafosts.saml.users.DafoSAMLUserArgumentResolver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import java.util.List;

@Configuration
public class MvcConfig extends WebMvcConfigurerAdapter {

    @Autowired
    DafoSAMLUserArgumentResolver dafoSAMLUserArgumentResolver;
    @Autowired
    SharedConfig sharedConfig;

    /**
     * Adds the dafoSAMLUserArgumentResolver to the list of argumentResolvers processed for each request.
     * @param argumentResolvers - the existing list of argumentResolvers.
     */
    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> argumentResolvers) {
        argumentResolvers.add(dafoSAMLUserArgumentResolver);
        super.addArgumentResolvers(argumentResolvers);
    }

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        sharedConfig.addViewControllers(registry);
        registry.addViewController("/by_saml_sso/").setViewName("by_saml_sso/index");
    }
}
