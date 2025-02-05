package ca.concordia.encs.citydata.core;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import ca.concordia.encs.citydata.datastores.InMemoryDataStore;
import ca.concordia.encs.citydata.runners.SequentialRunner;

@RestController
@RequestMapping("/apply")
public class ProducerController {

	@RequestMapping(value = "/sync", method = RequestMethod.POST)
	public String sync(@RequestBody String steps) {
		JsonObject errorLog = new JsonObject();
		JsonObject stepsObject = JsonParser.parseString(steps).getAsJsonObject();
		SequentialRunner deckard = new SequentialRunner(stepsObject);
		try {
			Thread runnerTask = new Thread() {
				public void run() {
					try {
						deckard.runSteps();
						while (!deckard.isDone()) {
							System.out.println("Busy waiting!");
						}
					} catch (Exception e) {
						errorLog.addProperty("runnerError", e.getMessage());
					}

				}
			};
			runnerTask.start();
			runnerTask.join();
		} catch (Exception e) {
			errorLog.addProperty("threadError", e.getMessage());
		}

		// if there are execution errors, return an error message
		if (errorLog.keySet().size() > 0) {
			return errorLog.toString();
		}

		// else, return the data
		IDataStore store = InMemoryDataStore.getInstance();
		String runnerId = deckard.getMetadata("id").toString();
		return store.get(runnerId).getResultJSONString();
	}

	@RequestMapping(value = "/async", method = RequestMethod.POST)
	public String async(@RequestBody String steps) {
		JsonObject errorLog = new JsonObject();
		JsonObject stepsObject = JsonParser.parseString(steps).getAsJsonObject();
		SequentialRunner deckard = new SequentialRunner(stepsObject);

		try {
			deckard.runSteps();
		} catch (Exception e) {
			errorLog.addProperty("runnerError", e.getMessage());
		}

		// if there are execution errors, return an error message
		if (errorLog.keySet().size() > 0) {
			return errorLog.toString();
		}

		return "Hello! The runner " + deckard.getMetadata("id")
				+ " is currently working on your request. Please make a GET request to /apply/async/ "
				+ deckard.getMetadata("id") + " to find out your request status.";
	}

	@RequestMapping(value = "/async/{runnerId}", method = RequestMethod.GET)
	public String asyncId(@PathVariable String runnerId) {
		InMemoryDataStore store = InMemoryDataStore.getInstance();
		Object storeResult = store.get(runnerId);

		if (storeResult != null) {
			return store.get(runnerId).getResultJSONString();
		}
		return "Sorry, your request result is not ready yet. Please try again later.";
	}

	@RequestMapping(value = "/ping", method = RequestMethod.GET)
	public String ping() {
		Date timeObject = Calendar.getInstance().getTime();
		String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(timeObject);
		return "pong - " + timeStamp;
	}
}