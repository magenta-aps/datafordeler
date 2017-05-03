package dk.magenta.dafosts.clientcertificates;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class DafoStsByCertificateApplication {

	public static void main(String[] args) throws Exception {
		// Boostrap the Opensaml Library to use default configuration
		org.opensaml.DefaultBootstrap.bootstrap();

		// Run the application
		SpringApplication.run(DafoStsByCertificateApplication.class, args);
	}
}
