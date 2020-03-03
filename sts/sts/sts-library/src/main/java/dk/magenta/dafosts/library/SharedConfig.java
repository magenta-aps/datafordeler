package dk.magenta.dafosts.library;

import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;

public class SharedConfig {

    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addViewController("/").setViewName("index");
    }

}
