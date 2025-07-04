package ca.concordia.encs.citydata.core;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.ComponentScan;

import ca.concordia.encs.citydata.datastores.DiskDatastore;
import ca.concordia.encs.citydata.datastores.InMemoryDataStore;
import ca.concordia.encs.citydata.datastores.MongoDataStore;

/***
 * This is the Spring Boot application entry point.
 * 
 * @author Gabriel C. Ullmann, Sikandar Ejaz, Rushin Makwana, Minette Zongo
 * @since 2025-01-01
 */

@SpringBootApplication
@ComponentScan(basePackages = { "ca.concordia.encs.citydata.core.controllers",
		"ca.concordia.encs.citydata.core.configs", "ca.concordia.encs.citydata.datastores" })
public class Application {

	// initialize all datastore for later use
	final InMemoryDataStore memoryStore = InMemoryDataStore.getInstance();
	final DiskDatastore diskStore = DiskDatastore.getInstance();
	final MongoDataStore mongoDataStore = MongoDataStore.getInstance();

	public static void main(String[] args) {
		final ApplicationContext context = SpringApplication.run(Application.class, args);
		final MongoDataStore mongoDataStore = context.getBean(MongoDataStore.class);
	}

}