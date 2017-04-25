package dk.magenta.dafosts;

import com.github.ulisesbocchio.spring.boot.security.saml.annotation.EnableSAMLSSO;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import static org.opensaml.DefaultBootstrap.bootstrap;

@SpringBootApplication
@EnableSAMLSSO
public class Application {

	public static void main(String[] args) throws Exception {
	    // Boostrap the Opensaml Library to use default configuration
        org.opensaml.DefaultBootstrap.bootstrap();
        // Run the application
		SpringApplication.run(Application.class, args);
	}
}
