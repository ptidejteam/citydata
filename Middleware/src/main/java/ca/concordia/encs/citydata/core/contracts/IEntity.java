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

	public void setMetadata(String key, Object value);

	public Object getMetadata(String key);

	public Set<String> getMetadataKeySet();

	public String getMetadataString(String key);
}
