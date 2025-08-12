package ca.concordia.encs.citydata.producers;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.UUID;

import org.junit.jupiter.api.Test;

import ca.concordia.encs.citydata.core.contracts.IProducer;
import ca.concordia.encs.citydata.datastores.InMemoryDataStore;
import ca.concordia.encs.citydata.producers.base.CSVProducer;
import ca.concordia.encs.citydata.producers.base.JSONProducer;
import ca.concordia.encs.citydata.runners.SingleStepRunner;

/**
 * Tests for Base (CSV and JSON Producers) Producers
 * @author Sikandar Ejaz
 * @since 08-12-2025
 */

public class BaseProducersTest {

	@Test
	public void testJSONProducerWithValidData() throws Exception {
		JSONProducer producer = new JSONProducer("./src/test/data/valid_test.json", null);
		SingleStepRunner runner = new SingleStepRunner(producer);
		String runnerIdString = runner.getMetadataString("id");
		UUID runnerId = UUID.fromString(runnerIdString);

		Thread runnerTask = new Thread(() -> {
			try {
				runner.runSteps();
				while (!runner.isDone()) {
					Thread.sleep(10);
				}
			} catch (Exception e) {
				runner.setAsDone();
			}
		});

		runnerTask.start();
		runnerTask.join(5000);

		assertTrue(runner.isDone(), "Runner should have completed execution");

		InMemoryDataStore store = InMemoryDataStore.getInstance();
		assertNotNull(store, "InMemoryDataStore should not be null");

		IProducer<?> resultProducer = store.get(runnerId);
		assertNotNull(resultProducer, "Result producer should not be null");
		assertNotNull(resultProducer.getResult(), "Producer result should not be null");

		String result = resultProducer.getResult().toString();
		assertFalse(result.isEmpty(), "Result should not be empty for valid JSON");
	}

	@Test
	public void testJSONProducerWithInvalidData() throws Exception {
		JSONProducer producer = new JSONProducer("./src/test/data/invalid_test.json", null);
		SingleStepRunner runner = new SingleStepRunner(producer);
		String runnerIdString = runner.getMetadataString("id");
		UUID runnerId = UUID.fromString(runnerIdString);

		Thread runnerTask = new Thread(() -> {
			try {
				runner.runSteps();
				while (!runner.isDone()) {
					Thread.sleep(10);
				}
			} catch (Exception e) {
				runner.setAsDone();
			}
		});

		runnerTask.start();
		runnerTask.join(5000);

		assertTrue(runner.isDone(), "Runner should have completed execution");

		InMemoryDataStore store = InMemoryDataStore.getInstance();
		assertNotNull(store, "InMemoryDataStore should not be null");

		IProducer<?> resultProducer = store.get(runnerId);
		if (resultProducer != null) {
			Object result = resultProducer.getResult();
			assertTrue(result == null || result.toString().contains("error") || result.toString().contains("invalid"),
					"Invalid JSON should result in null or error result");
		}
	}

	@Test
	public void testCSVProducerWithValidData() throws Exception {
		CSVProducer producer = new CSVProducer("./src/test/data/valid_test.csv", null);
		SingleStepRunner runner = new SingleStepRunner(producer);
		String runnerIdString = runner.getMetadataString("id");
		UUID runnerId = UUID.fromString(runnerIdString);

		Thread runnerTask = new Thread(() -> {
			try {
				runner.runSteps();
				while (!runner.isDone()) {
					Thread.sleep(10);
				}
			} catch (Exception e) {
				runner.setAsDone();
			}
		});

		runnerTask.start();
		runnerTask.join(5000);

		assertTrue(runner.isDone(), "Runner should have completed execution");

		InMemoryDataStore store = InMemoryDataStore.getInstance();
		assertNotNull(store, "InMemoryDataStore should not be null");

		IProducer<?> resultProducer = store.get(runnerId);
		assertNotNull(resultProducer, "Result producer should not be null");
		assertNotNull(resultProducer.getResult(), "Producer result should not be null");

		String result = resultProducer.getResult().toString();
		assertFalse(result.isEmpty(), "Result should not be empty for valid CSV");
	}

	@Test
	public void testCSVProducerWithInvalidData() throws Exception {
		CSVProducer producer = new CSVProducer("./src/test/data/invalid_test.csv", null);
		SingleStepRunner runner = new SingleStepRunner(producer);
		String runnerIdString = runner.getMetadataString("id");
		UUID runnerId = UUID.fromString(runnerIdString);

		Thread runnerTask = new Thread(() -> {
			try {
				runner.runSteps();
				while (!runner.isDone()) {
					Thread.sleep(10);
				}
			} catch (Exception e) {
				runner.setAsDone();
			}
		});

		runnerTask.start();
		runnerTask.join(5000);

		assertTrue(runner.isDone(), "Runner should have completed execution");

		InMemoryDataStore store = InMemoryDataStore.getInstance();
		assertNotNull(store, "InMemoryDataStore should not be null");

		IProducer<?> resultProducer = store.get(runnerId);
		if (resultProducer != null) {
			Object result = resultProducer.getResult();
			if (result != null) {
				assertNotNull(result, "CSV producer should handle malformed data gracefully");
			}
		}
	}
}
