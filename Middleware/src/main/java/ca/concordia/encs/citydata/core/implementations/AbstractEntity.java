package ca.concordia.encs.citydata.core.implementations;

import java.util.HashMap;
import java.util.Set;
import java.util.UUID;

import ca.concordia.encs.citydata.core.contracts.IEntity;

/**
 *
 * This is the most generic blueprint of what a "thing" in the CityData should
 * be. Producers, Operations, Runners and DataStores are CityData entities.
 * 
 * 
 * @author Gabriel C. Ullmann
 * @date 2025-04-23
 */
public abstract class AbstractEntity implements IEntity {

	private final HashMap<String, Object> metadata = new HashMap<>();

	public AbstractEntity() {
		UUID uniqueId = UUID.randomUUID();
		// Storing as UUID instead of uniqueId.toString()
		this.setMetadata("id", uniqueId);
	}
	
	public void setMetadata(String key, Object value) {
		metadata.put(key, value);
	}

	public Object getMetadata(String key) {
		return metadata.get(key);
	}
	
	// Here we get the entity's UUID 
    public UUID getId() {
        Object id = metadata.get("id");
        if (id instanceof UUID) {
            return (UUID) id;
        } else if (id instanceof String) {
            // To maintain compatibility with IDs stored as strings
            return UUID.fromString((String) id);
        }
        return null;
    }
    
    // Here we get the entity's ID as a String whenever it's needed
    public String getIdAsString() {
        UUID id = getId();
        return id != null ? id.toString() : "";
    }

	public Set<String> getMetadataKeySet() {
		return metadata.keySet();
	}

	public String getMetadataString(String key) {
        Object value = metadata.get(key);
        if (value != null) {
            return value.toString();
        }
        return "";
    }
		
}
