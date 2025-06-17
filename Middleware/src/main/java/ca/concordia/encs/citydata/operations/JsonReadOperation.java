package ca.concordia.encs.citydata.operations;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ca.concordia.encs.citydata.core.exceptions.MiddlewareException;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import ca.concordia.encs.citydata.core.implementations.AbstractOperation;
import ca.concordia.encs.citydata.core.contracts.IOperation;

public class JsonReadOperation extends AbstractOperation<JsonObject> implements IOperation<JsonObject> {
	private String path = "";
	private JsonElement currentObject;
	final Pattern containsArrayAccess = Pattern.compile("(.+)\\[(\\d+)\\]");

	public void setPath(String path) {
		this.path = path;
	}

	private void handleJsonElementType(JsonElement element, String key) {
		if (element.isJsonArray()) {
			this.currentObject = element.getAsJsonArray();
		} else if (element.isJsonPrimitive() || this.currentObject.isJsonNull()) {
			final JsonObject objectWrapper = new JsonObject();
			objectWrapper.addProperty(key, element.getAsString());
			this.currentObject = objectWrapper;
		} else {
			this.currentObject = element.getAsJsonObject();
		}
	}

	private void handlePathNotFound(String key) {
		final JsonObject errorWrapper = new JsonObject();

		if (this.currentObject == null || this.currentObject.isJsonNull()) {
			errorWrapper.addProperty("error", "The path \"" + this.path + "\" cannot be found.");
		} else if (this.currentObject.isJsonObject()) {
			errorWrapper.addProperty("error",
					"The key \"" + key + "\" cannot be found. You can try one of the following keys instead: "
							+ this.currentObject.getAsJsonObject().keySet().toString());
		} else {
			String lastKnowGoodObject = this.currentObject.toString();
			String excerpt = lastKnowGoodObject.length() > 50 ? lastKnowGoodObject.substring(0, 50) + "..."
					: lastKnowGoodObject;
			errorWrapper.addProperty("error", "The key \"" + key + "\" cannot be found in: " + excerpt);
		}

		this.currentObject = errorWrapper;
	}

	@Override
	public ArrayList<JsonObject> apply(ArrayList<JsonObject> inputs) {

		final ArrayList<JsonObject> matchingJsonObjects = new ArrayList<>();
		final String[] pathParts = this.path.split("\\.");

		if (pathParts.length > 0) {

			for (JsonObject input : inputs) {

				this.currentObject = input;
				for (String key : pathParts) {

					try {
						// check if key contains []
						final Matcher arrayAccessMatcher = this.containsArrayAccess.matcher(key);

						// if so, go to key and array position
						if (this.currentObject.isJsonObject() && arrayAccessMatcher.matches()) {
							final String arrayKey = arrayAccessMatcher.group(1);
							final String arrayPositionString = arrayAccessMatcher.group(2);
							final JsonArray jsonArray = ((JsonObject) this.currentObject).get(arrayKey)
									.getAsJsonArray();
							final int arrayPosition = Integer.parseInt(arrayPositionString);
							final JsonElement currentElement = jsonArray.get(arrayPosition);
							this.handleJsonElementType(currentElement, key);

							// else, it is not an array, so attempt to read it as an object
						} else if (this.currentObject.isJsonObject()) {
							// this object may contain another object, an array or primitive (e.g. a number)
							final JsonElement currentElement = ((JsonObject) this.currentObject).get(key);
							this.handleJsonElementType(currentElement, key);
						} else {
							// this means the current object is either a primitive or null
							break;
						}
					} catch (MiddlewareException e) {
						this.handlePathNotFound(key);
						break;
					}

				}

				// if the final result is object, return object, else add return to a wrapper
				if (this.currentObject.isJsonObject()) {
					matchingJsonObjects.add(this.currentObject.getAsJsonObject());
				} else {
					final JsonObject objectWrapper = new JsonObject();
					objectWrapper.add(pathParts[pathParts.length - 1], this.currentObject);
					matchingJsonObjects.add(objectWrapper);
				}

			}

		}

		return matchingJsonObjects;
	}
}
