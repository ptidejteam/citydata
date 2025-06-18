package ca.concordia.encs.citydata.core.configs;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/***
 * This is the Spring Boot base configuration file.
 * 
 * @author Gabriel C. Ullmann, Sikandar Ejaz
 * @since 2025-01-01
 */

@Configuration
public class AppConfig {

	@Bean
	public String exampleBean() {
		return "Hello, Spring Boot!";
	}

}