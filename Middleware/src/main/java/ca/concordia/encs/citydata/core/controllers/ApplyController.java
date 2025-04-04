package ca.concordia.encs.citydata.core.controllers;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;

import ca.concordia.encs.citydata.core.contracts.IProducer;
import ca.concordia.encs.citydata.datastores.InMemoryDataStore;
import ca.concordia.encs.citydata.producers.ExceptionProducer;
import ca.concordia.encs.citydata.runners.SequentialRunner;

/***
 * This class manages all requests sent to the /apply route
 * 
 * @author Gabriel C. Ullmann
 * @date 2024-12-01
 */
@RestController
@RequestMapping("/apply")
public class ApplyController {

	@RequestMapping(value = "/sync", method = RequestMethod.POST)
	public ResponseEntity<String> sync(@RequestBody String steps) {
		String runnerId = "";
		String errorMessage = "";
		HttpStatus responseCode = HttpStatus.OK;

		try {
			JsonObject stepsObject = JsonParser.parseString(steps).getAsJsonObject();
			SequentialRunner deckard = new SequentialRunner(stepsObject);
			runnerId = deckard.getMetadataString("id");
			Thread runnerTask = new Thread() {
				public void run() {
					try {
						deckard.runSteps();
						while (!deckard.isDone()) {
							System.out.println("Busy waiting!");
						}
					} catch (Exception e) {
						System.out.println(e.getMessage());
						// stop runner as soon as an exception is thrown to avoid infinite loops
						deckard.setAsDone();
						InMemoryDataStore store = InMemoryDataStore.getInstance();
						store.set(deckard.getMetadataString("id"), new ExceptionProducer(e));
					}
				}
			};
			runnerTask.start();
			runnerTask.join();
		} catch (IllegalStateException | JsonParseException e) {
			String detailedMessage = e.getCause() != null ? e.getCause().getMessage() : e.getMessage();
			errorMessage = "Your query is not a valid JSON file. Details: " + detailedMessage;
			responseCode = HttpStatus.BAD_REQUEST;
		} catch (Exception e) {
			errorMessage = "An error occurred while processing your query. Details: " + e.getClass().getName() + ": "
					+ e.getMessage();
			responseCode = HttpStatus.INTERNAL_SERVER_ERROR;
		}

		// if there are execution errors, return an error message
		if (responseCode.isError()) {
			return ResponseEntity.status(responseCode).body(errorMessage);
		}

		// else, return the data
		InMemoryDataStore store = InMemoryDataStore.getInstance();
		IProducer<?> resultProducer = store.get(runnerId);

		// if the thread, which cannot throw exceptions, produces an ExceptionProducer,
		// return an error code
		if (resultProducer.getClass() == ExceptionProducer.class) {
			responseCode = HttpStatus.INTERNAL_SERVER_ERROR;
			return ResponseEntity.status(responseCode).body(resultProducer.getResultJSONString());
		}

		return ResponseEntity.status(responseCode).body(resultProducer.getResultJSONString());
	}

	@RequestMapping(value = "/async", method = RequestMethod.POST)
	public ResponseEntity<String> async(@RequestBody String steps) {
		String runnerId = "";
		String errorMessage = "";
		HttpStatus responseCode = HttpStatus.OK;

		try {
			JsonObject stepsObject = JsonParser.parseString(steps).getAsJsonObject();
			SequentialRunner deckard = new SequentialRunner(stepsObject);
			runnerId = deckard.getMetadata("id").toString();
			deckard.runSteps();
		} catch (IllegalStateException | JsonParseException e) {
			String detailedMessage = e.getCause() != null ? e.getCause().getMessage() : e.getMessage();
			errorMessage = "Your query is not a valid JSON file. Details: " + detailedMessage;
			responseCode = HttpStatus.BAD_REQUEST;
		} catch (Exception e) {
			errorMessage = "An error occurred while processing your query. Details: " + e.getClass().getName() + ": "
					+ e.getMessage();
			responseCode = HttpStatus.INTERNAL_SERVER_ERROR;
		}

		// if there are execution errors, return an error message
		if (responseCode.isError()) {
			return ResponseEntity.status(responseCode).body(errorMessage);
		}

		return ResponseEntity.status(responseCode)
				.body("Hello! The runner " + runnerId
						+ " is currently working on your request. Please make a GET request to /apply/async/ "
						+ runnerId + " to retrieve request results.");
	}

	@RequestMapping(value = "/async/{runnerId}", method = RequestMethod.GET)
	public ResponseEntity<String> asyncId(@PathVariable("runnerId") String runnerId) {

		InMemoryDataStore store = InMemoryDataStore.getInstance();
		IProducer<?> storeResult = store.get(runnerId);

		if (storeResult != null) {
			return ResponseEntity.status(HttpStatus.OK).body(storeResult.getResultJSONString());
		} else {
			return ResponseEntity.status(HttpStatus.NOT_FOUND)
					.body("Sorry, your request result is not ready yet. Please try again later.");
		}

	}

	@RequestMapping(value = "/ping", method = RequestMethod.GET)
	public ResponseEntity<String> ping() {
		Date timeObject = Calendar.getInstance().getTime();
		String timeStamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(timeObject);
		return ResponseEntity.status(HttpStatus.OK).body("pong at " + timeStamp);
	}
}