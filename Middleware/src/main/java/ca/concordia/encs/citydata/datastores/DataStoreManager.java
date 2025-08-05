package ca.concordia.encs.citydata.datastores;

import ca.concordia.encs.citydata.core.contracts.IDataStore;
import ca.concordia.encs.citydata.core.implementations.AbstractEntity;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class DataStoreManager extends AbstractEntity {
	
	// Static registry of all known datastores
	private static final Map<String, IDataStore<?>> stores = new HashMap<>();

	private static final DataStoreManager instance = new DataStoreManager();

    private DataStoreManager() {
        this.setMetadata("role", "manager");
        registerStores();
    }

    public static DataStoreManager getInstance() {
        return instance;
    }
    
    // Register all known datastores
    private void registerStores() {
    	stores.put("InMemory", InMemoryDataStore.getInstance());
    	stores.put("Disk", DiskDatastore.getInstance());
    	stores.put("MongoDB", MongoDataStore.getInstance());
    	
    	// For simplicity we can add aliases
        stores.put("Memory", InMemoryDataStore.getInstance());
    	stores.put("File", DiskDatastore.getInstance());
    	stores.put("Mongo", MongoDataStore.getInstance());
    }
    
    @SuppressWarnings("unchecked")
    public <T> IDataStore<T> getStore(String storeName) {
        if (storeName == null || storeName.trim().isEmpty()) {
            throw new IllegalArgumentException("Store name cannot be null or empty");
        }

        IDataStore<?> store = stores.get(storeName);
        if (store == null) {
            throw new IllegalArgumentException("Unknown datastore: " + storeName +
                    ". Available: " + getAvailableStores());
        }

        return (IDataStore<T>) store;
    }

    public Set<String> getAvailableStores() {
        return new HashSet<>(stores.keySet());
    }

    public boolean hasStore(String storeName) {
        return stores.containsKey(storeName);
    }
  
}
