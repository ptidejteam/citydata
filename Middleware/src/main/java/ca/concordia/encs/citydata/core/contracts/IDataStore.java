package ca.concordia.encs.citydata.core.contracts;

import java.util.Iterator;
import java.util.UUID;

/**
 *
 * The DataStore entity is responsible for: - Create and Update records in a
 * key/value database (set method) - Read records from a key/value database (get
 * method) - Delete records in a key/value database (delete method)
 * 
 * 
 * Refactoring made: changed the method's signature to use UUIDs instead of Strings
 * and default methods to still handle String IDs, set, get, delete
 *
 * @author Gabriel C. Ullmann, Minette Zongo
 * @since 2025-06-18
 */
public interface IDataStore<E> {

	void set(UUID key, E value);
	// Still support String Ids
    default void set(String key, E value) {
        if (key != null) {
            set(UUID.fromString(key), value);
        }
    }
    
	E get(UUID key);
	// Still support String Ids
	default E get(String key) {
        if (key != null) {
            return get(UUID.fromString(key));
        }
        return null;
    }
	
    
    Iterator<E> getValues();
    
    void delete(UUID key);
    // Still support String Ids
    default void delete(String key) {
        if (key != null) {
            delete(UUID.fromString(key));
        }
}
	
	

}
