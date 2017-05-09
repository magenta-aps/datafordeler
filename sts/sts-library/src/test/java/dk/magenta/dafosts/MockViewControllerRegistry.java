package dk.magenta.dafosts;

import org.springframework.context.support.GenericApplicationContext;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistration;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;

import java.util.HashSet;
import java.util.Set;

public class MockViewControllerRegistry extends ViewControllerRegistry {

    private Set<String> mappings;

    public MockViewControllerRegistry() {
        mappings = new HashSet<>();
        setApplicationContext(new GenericApplicationContext());
    }

    public Set<String>  getMappings() {
        return mappings;
    }

    @Override
    public ViewControllerRegistration addViewController(String urlPath) {
        mappings.add(urlPath);
        return super.addViewController(urlPath);
    }


}
