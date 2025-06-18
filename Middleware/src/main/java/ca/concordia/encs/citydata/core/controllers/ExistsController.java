package ca.concordia.encs.citydata.core.controllers;

import java.util.Iterator;
import java.util.UUID;

import ca.concordia.encs.citydata.core.contracts.IProducer;
import ca.concordia.encs.citydata.core.implementations.AbstractProducer;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import ca.concordia.encs.citydata.datastores.InMemoryDataStore;

/**
 * This route check whether a the input query is already related to one of the producers stored
 * in the middleware's DataStore. If so, it returns the list of producers with that match the query,
 * along with their generation timestamps.
 * 
 * @author Minette
 * @since 21-02-2025
 */

@RestController
@RequestMapping("/exists")
public class ExistsController {

	@RequestMapping(value = "/", method = RequestMethod.POST)
	public ResponseEntity<String> sync(@RequestBody String query) {

		try {
			final JsonArray matchingProducers = new JsonArray();
			final JsonObject queryFromUserRequest = JsonParser.parseString(query).getAsJsonObject();
			final InMemoryDataStore store = InMemoryDataStore.getInstance();
			final Iterator<IProducer<?>> storedProducers = store.getValues();

			while (storedProducers.hasNext()) {
				final AbstractProducer producer = (AbstractProducer) storedProducers.next();
				final JsonObject queryInProducer = (JsonObject) producer.getMetadata("query");

				// ignore producers without queries because this means they are result of an
				// error or intermediate operation
				// e.g., producers handled by the SingleStepRunner
				if (queryInProducer != null) {
					final UUID runnerIdFromProducer = producer.getId();
					final String timestamp = producer.getMetadataString("timestamp");
					if (queryInProducer.equals(queryFromUserRequest)) {
						final JsonObject producerMetadata = new JsonObject();
						producerMetadata.addProperty("runnerId", runnerIdFromProducer.toString());
						producerMetadata.addProperty("timestamp", timestamp);
						matchingProducers.add(producerMetadata);
					}
				}
			}

			// if empty, return empty list and status = not found
			if (matchingProducers.isEmpty()) {
				return ResponseEntity.status(404).body(matchingProducers.toString());
			}
			return ResponseEntity.status(200).body(matchingProducers.toString());

		} catch (Exception e) {
			final JsonObject errorDetails = new JsonObject();
			errorDetails.addProperty("error", e.getMessage());
			System.out.println(e.getMessage());
			return ResponseEntity.status(500).body(errorDetails.toString());
		}

	}

}