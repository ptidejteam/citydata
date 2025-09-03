package ca.concordia.encs.citydata.datastores;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;

import ca.concordia.encs.citydata.core.contracts.IDataStore;
import ca.concordia.encs.citydata.core.contracts.IDatastoreManager;

/**
*
* CityData datastores manager: a singleton implementation of IDatastoreManager that orchestrates all datastores.
* 
* @author Minette Zongo
* @since 2025-08-19
* 
*/


public class DatastoreManager implements IDatastoreManager {
	private final Map<DatastoreType, IDataStore<?>> stores = new EnumMap<>(DatastoreType.class);

    private static final DatastoreManager instance = new DatastoreManager();

    private DatastoreManager() {
        registerStores();
    }

    public static DatastoreManager getInstance() {
        return instance;
    }

    @Override
    public void registerStores() {
        stores.put(DatastoreType.IN_MEMORY, InMemoryDataStore.getInstance());
        stores.put(DatastoreType.DISK, DiskDatastore.getInstance());
        stores.put(DatastoreType.MONGODB, MongoDataStore.getInstance());
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> IDataStore<T> getStore(DatastoreType type) {
        if (type == null) {
            throw new IllegalArgumentException("Datastore type cannot be null");
        }
        IDataStore<?> dataStore = stores.get(type);
        if (dataStore == null) {
            throw new IllegalArgumentException("No datastore registered for: " + type);
        }
        return (IDataStore<T>) dataStore;
    }

    @Override
    public Map<DatastoreType, IDataStore<?>> getStores() {
        return Collections.unmodifiableMap(stores);
    }
	

}
