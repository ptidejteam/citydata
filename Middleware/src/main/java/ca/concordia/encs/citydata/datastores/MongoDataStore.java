package ca.concordia.encs.citydata.datastores;

import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.data.mongodb.core.query.Query.query;

import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

import ca.concordia.encs.citydata.core.contracts.IDataStore;
import ca.concordia.encs.citydata.core.implementations.AbstractEntity;
import ca.concordia.encs.citydata.core.utils.ProducerUsageData;

/***
 * This class is responsible for storing and retrieving data from MongoDB. Data
 * remains persists even after the application stops.
 * 
 * @author Rushin Makwana
 * @since 2024-02-26
 */
@Component
public class MongoDataStore extends AbstractEntity implements IDataStore<ProducerUsageData> {

	@Autowired(required = false)
	private MongoTemplate mongoTemplate;

	@Value("${spring.data.mongodb.uri:}")
	private String mongoUri;

	private static final MongoDataStore storeInstance = new MongoDataStore();

	// Private constructor prevents instantiation (this is a singleton)
	private MongoDataStore() {
		this.setMetadata("role", "datastore");
	}

	// Public method to provide access to the instance
	public static MongoDataStore getInstance() {
		return storeInstance;
	}

	@PostConstruct
	private void init() {
		if (mongoUri.isEmpty()) {
			mongoTemplate = null;
		}
	}

	public boolean hasConnection() {
		return mongoTemplate != null;
	}

	@Override
	public void set(String key, ProducerUsageData value) {
		if (mongoTemplate != null) {
			mongoTemplate.save(value, key);
		}
	}

	@Override
	public ProducerUsageData get(String key) {
		if (mongoTemplate != null) {
			return mongoTemplate.findById(key, ProducerUsageData.class);
		}
		return null;
	}

	@Override
	public void delete(String key) {
		if (mongoTemplate != null) {
			mongoTemplate.remove(query(where("_id").is(key)), ProducerUsageData.class);
		}
	}

	public void save(ProducerUsageData callInfo) {
		if (mongoTemplate != null) {
			mongoTemplate.save(callInfo);
		}
	}

	public List<ProducerUsageData> findByProducerName(String producerName) {
		if (mongoTemplate != null) {
			return mongoTemplate.find(query(where("producerName").is(producerName)), ProducerUsageData.class);
		}
		return null;
	}

	public List<ProducerUsageData> findAll() {
		if (mongoTemplate != null) {
			return mongoTemplate.findAll(ProducerUsageData.class);
		}
		return null;
	}

	public void deleteAll() {
		if (mongoTemplate != null) {
			mongoTemplate.remove(new Query(), ProducerUsageData.class);
		}
	}
	
	@Override
	public ProducerUsageData get(UUID key) {
	    if (mongoTemplate != null) {
	        return mongoTemplate.findById(key.toString(), ProducerUsageData.class);
	    }
	    return null;
	}
	
	@Override
	public void set(UUID key, ProducerUsageData value) {
	    if (mongoTemplate != null) {
	        mongoTemplate.save(value, key.toString());
	    }
	}

	@Override
	public void delete(UUID key) {
	    if (mongoTemplate != null) {
	        mongoTemplate.remove(query(where("_id").is(key.toString())), ProducerUsageData.class);
	    }
	}
	
	

	@Override
	public Iterator<ProducerUsageData> getValues() {
		// TODO Auto-generated method stub
		return null;
	}
}