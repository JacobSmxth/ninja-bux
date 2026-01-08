package dev.jsmitty.bux.system;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Spring Boot entry point for the Ninja Bux backend.
 *
 * <p>Bootstraps component scanning and auto-configuration.
 */
@SpringBootApplication
public class BuxSystemApplication {

	public static void main(String[] args) {
		SpringApplication.run(BuxSystemApplication.class, args);
	}

}
