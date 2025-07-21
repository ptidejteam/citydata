package ca.concordia.encs.citydata.core.contracts;

import java.util.Set;

/**
 *
 * CityData entities such as Producers, Operations, Runners and DataStores shall
 * have the capacity of holding metadata about themselves.
 * 
 * @author Gabriel C. Ullmann
 * @since 2025-04-23
 * 
 */
public interface IEntity {

	void setMetadata(String key, Object value);

	Object getMetadata(String key);

	Set<String> getMetadataKeySet();

	String getMetadataString(String key);
}
