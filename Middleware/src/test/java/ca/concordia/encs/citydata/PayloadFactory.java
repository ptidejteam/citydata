package ca.concordia.encs.citydata;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

// A class that produces mock request payloads, for example, JSON queries to use with /apply/sync
public abstract class PayloadFactory {

	public static String getBasicQuery() {
		JsonObject payload = new JsonObject();
		payload.addProperty("use", "ca.concordia.encs.citydata.producers.RandomStringProducer");

		JsonArray withParams = new JsonArray();
		JsonObject param1 = new JsonObject();
		param1.addProperty("name", "stringLength");
		param1.addProperty("value", "10");
		withParams.add(param1);

		payload.add("withParams", withParams);

		JsonArray apply = new JsonArray();
		JsonObject operation1 = new JsonObject();
		operation1.addProperty("name", "ca.concordia.encs.citydata.operations.StringReplaceOperation");

		JsonArray operation1Params = new JsonArray();
		JsonObject operation1Param1 = new JsonObject();
		operation1Param1.addProperty("name", "searchFor");
		operation1Param1.addProperty("value", "a");
		operation1Params.add(operation1Param1);

		JsonObject operation1Param2 = new JsonObject();
		operation1Param2.addProperty("name", "replaceBy");
		operation1Param2.addProperty("value", "b");
		operation1Params.add(operation1Param2);

		operation1.add("withParams", operation1Params);
		apply.add(operation1);

		JsonObject operation2 = new JsonObject();
		operation2.addProperty("name", "ca.concordia.encs.citydata.operations.StringReplaceOperation");

		JsonArray operation2Params = new JsonArray();
		JsonObject operation2Param1 = new JsonObject();
		operation2Param1.addProperty("name", "searchFor");
		operation2Param1.addProperty("value", "cxsffdf");
		operation2Params.add(operation2Param1);

		JsonObject operation2Param2 = new JsonObject();
		operation2Param2.addProperty("name", "replaceBy");
		operation2Param2.addProperty("value", "c");
		operation2Params.add(operation2Param2);

		operation2.add("withParams", operation2Params);
		apply.add(operation2);

		payload.add("apply", apply);

		return payload.toString();
		
	}

	public static String getExampleQuery(String queryFileName) throws Exception {
		Path filePath = Path.of("./docs/examples/queries/" + queryFileName + ".json");
		return new String(Files.readAllBytes(filePath));
	}

	public static String getInvalidJson() throws Exception {
		return "{broken_json:}";
	}

}