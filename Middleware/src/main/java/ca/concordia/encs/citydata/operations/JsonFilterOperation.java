package ca.concordia.encs.citydata.operations;

import java.util.ArrayList;

import com.google.gson.JsonObject;

import ca.concordia.encs.citydata.core.implementations.AbstractOperation;
import ca.concordia.encs.citydata.core.contracts.IOperation;

/**
 * This operation filters an array of JsonObjects by a given key and value.
 *
 * @author Gabriel C. Ullmann
 * @since 2025-06-18
 */
public class JsonFilterOperation extends AbstractOperation<JsonObject> implements IOperation<JsonObject> {
	String key;
	String value;
	Boolean isExactlyEqual = false;

	public void setKey(String key) {
		this.key = key;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public void setIsExactlyEqual(Boolean isExactlyEqual) {
		this.isExactlyEqual = isExactlyEqual;
	}

	@Override
	public ArrayList<JsonObject> apply(ArrayList<JsonObject> inputs) {
		final ArrayList<JsonObject> filteredList = new ArrayList<>();
		for (JsonObject jsonObject : inputs) {
			if (key != null && jsonObject.has(key)) {
				String objectValue = jsonObject.get(key).getAsString();
				if (isExactlyEqual && objectValue.equals(value)) {
					filteredList.add(jsonObject);
				} else if (!isExactlyEqual && objectValue.contains(value)) {
					filteredList.add(jsonObject);
				}
			}
		}
		return filteredList;
	}
}
