package ca.concordia.encs.citydata.core.configs;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.type.AnnotatedTypeMetadata;
import org.springframework.data.mongodb.core.MongoTemplate;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;

import ca.concordia.encs.citydata.datastores.MongoDataStore;

/**
 * This is the Spring Boot configuration file which contains configuration
 * related to MongoDB. This will run on every application startup, just like
 * AppConfig.java, but the mongoTemplate method will only be NOT NULL if MongoDB
 * is enabled and if a database connection string is found in the
 * application.properties file.
 * 
 * @author Gabriel C. Ullmann, Rushin Makwana
 * @since 2025-03-28
 */

@Configuration
public class OptionalMongoConfig {

	@Value("${spring.data.mongodb.uri:}")
	private String mongoUri;

	@Bean
	@Conditional(MongoUriProvidedCondition.class)
	public MongoClient mongoClient() {
		return MongoClients.create(mongoUri);
	}

	@Bean
	@Conditional(MongoUriProvidedCondition.class)
	public MongoTemplate mongoTemplate(MongoClient mongoClient) {
		String databaseName = mongoUri.split("/")[3];
		return new MongoTemplate(mongoClient, databaseName);
	}

	@Bean
	public MongoDataStore mongoDataStore(@Autowired(required = false) MongoTemplate mongoTemplate) {
		return MongoDataStore.getInstance();
	}

	// Checks whether the MongoDB configurations should be applied or not
	public static class MongoUriProvidedCondition implements Condition {
		@Override
		public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
			String uri = null;
			String isMongoEnabled = context.getEnvironment().getProperty("mongo.enabled");

			if (isMongoEnabled.equalsIgnoreCase("true")) {
				uri = context.getEnvironment().getProperty("spring.data.mongodb.uri");
			}

			return uri != null && !uri.isEmpty();
		}
	}
}
