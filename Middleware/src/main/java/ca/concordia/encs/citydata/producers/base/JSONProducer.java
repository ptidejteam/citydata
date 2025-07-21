package ca.concordia.encs.citydata.producers.base;

import java.util.ArrayList;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import ca.concordia.encs.citydata.core.implementations.AbstractProducer;
import ca.concordia.encs.citydata.core.contracts.IProducer;
import ca.concordia.encs.citydata.core.utils.RequestOptions;

/**
 * This producer can load JSON from a file or remotely via an HTTP request.
 *
 * @author Gabriel C. Ullmann
 * @since 2024-12-01
 */
public class JSONProducer extends AbstractProducer<JsonObject> implements IProducer<JsonObject> {

	public JSONProducer(String filePath, RequestOptions fileOptions) {
		this.setFilePath(filePath);
		this.setFileOptions(fileOptions);
	}

	@Override
	public void fetch() {

		final ArrayList<JsonObject> jsonOutput = new ArrayList<>();
		final String inputJson = new String(this.fetchFromPath());

		// convert JSON string to object
		final JsonElement inputJsonElement = JsonParser.parseString(inputJson);

		JsonObject outputJsonObject = new JsonObject();
		if (inputJsonElement.isJsonArray()) {
			outputJsonObject.add("result", inputJsonElement);
		} else {
			outputJsonObject = inputJsonElement.getAsJsonObject();
		}

		jsonOutput.add(outputJsonObject);
		this.setResult(jsonOutput);
		this.applyOperation();
	}

}