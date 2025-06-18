package ca.concordia.encs.citydata.operations;

import java.util.ArrayList;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;

import ca.concordia.encs.citydata.core.contracts.IOperation;
import ca.concordia.encs.citydata.core.implementations.AbstractOperation;

/**
 * This operation computes the average (arithmetic mean) of a JsonArray,
 * retrieving the numerical values from a given key in each element in the
 * JsonArray
 *
 * @author Gabriel C. Ullmann
 * @date 2025-06-18
 */
public class JsonArrayAverageOperation extends AbstractOperation<JsonArray> implements IOperation<JsonArray> {

	private String keyName;
	private String roundingMethod = "";

	public void setKeyName(String keyName) {
		this.keyName = keyName;
	}

	public void setRoundingMethod(String roundingMethod) {
		this.roundingMethod = roundingMethod;
	}

	@Override
	public ArrayList<JsonArray> apply(ArrayList<JsonArray> inputs) {
		ArrayList<JsonArray> results = new ArrayList<>();
		Number roundedAverage = 0;
		Float floatAverage = 0.0f;

		// compute the average
		for (JsonArray list : inputs) {
			for (JsonElement element : list) {
				floatAverage += element.getAsJsonObject().get(keyName).getAsFloat();
			}
			floatAverage = floatAverage / list.size();

			// choose whether (and how) to round the result
			JsonArray resultingAverage = new JsonArray();
			if (roundingMethod.equalsIgnoreCase("floor")) {
				roundedAverage = (int) Math.floor(floatAverage);
			} else if (roundingMethod.equalsIgnoreCase("ceil")) {
				roundedAverage = (int) Math.ceil(floatAverage);
			} else if (roundingMethod.equalsIgnoreCase("round")) {
				roundedAverage = (int) Math.round(floatAverage);
			} else {
				roundedAverage = floatAverage;
			}
			resultingAverage.add(roundedAverage);
			results.add(resultingAverage);
		}

		return results;
	}

}
