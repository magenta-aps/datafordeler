package dk.magenta.dafosts.config;

import dk.magenta.dafosts.users.DafoSAMLUserArgumentResolver;
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
        registry.addViewController("/").setViewName("index");
        registry.addViewController("/by_certificate/home").setViewName("by_certificate/home");
        registry.addViewController("/by_certificate/").setViewName("by_certificate/home");
        registry.addViewController("/by_certificate/hello").setViewName("by_certificate/hello");
        registry.addViewController("/by_certificate/login").setViewName("by_certificate/login");
    }
}
