package dk.magenta.dafosts.saml;

import com.github.ulisesbocchio.spring.boot.security.saml.annotation.EnableSAMLSSO;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@EnableSAMLSSO
public class DafoStsBySamlApplication {

	public static void main(String[] args) throws Exception {
	    // Boostrap the Opensaml Library to use default configuration
        org.opensaml.DefaultBootstrap.bootstrap();
        // Run the application
		SpringApplication.run(DafoStsBySamlApplication.class, args);
	}
}
