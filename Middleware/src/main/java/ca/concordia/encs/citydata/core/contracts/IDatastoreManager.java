package ca.concordia.encs.citydata.core.contracts;

import java.util.Map;
import java.util.Set;

/**
*
* CityData datastores interface to orchestrate all datastores.
* 
* @author Minette Zongo
* @since 2025-08-19
* 
*/

public interface IDatastoreManager {
	
    enum DatastoreType {
        IN_MEMORY,
        DISK,
        MONGODB
    }

    void registerStores();

    <T> IDataStore<T> getStore(DatastoreType type);

    Map<DatastoreType, IDataStore<?>> getStores();

    default boolean hasStore(DatastoreType type) {
        return getStores().containsKey(type);
    }

}
