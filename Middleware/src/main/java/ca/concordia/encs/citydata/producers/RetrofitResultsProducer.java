package ca.concordia.encs.citydata.producers;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import ca.concordia.encs.citydata.core.contracts.IOperation;
import ca.concordia.encs.citydata.core.contracts.IProducer;
import ca.concordia.encs.citydata.core.contracts.IRunner;
import ca.concordia.encs.citydata.core.implementations.AbstractProducer;
import ca.concordia.encs.citydata.core.utils.RequestOptions;
import ca.concordia.encs.citydata.core.utils.StringUtils;
import ca.concordia.encs.citydata.datastores.InMemoryDataStore;
import ca.concordia.encs.citydata.producers.base.JSONProducer;
import ca.concordia.encs.citydata.runners.SingleStepRunner;

/**
 * This producer fetches data from the HUB API. Credentials are needed to access
 * this API in your environment variables.
 *
 * @author Gabriel C. Ullmann
 * @date 2025-04-04
 */
public class RetrofitResultsProducer extends AbstractProducer<JsonObject> implements IProducer<JsonObject> {

	private JsonArray buildingIds;
	private JSONProducer jsonProducer;
	private IOperation<JsonObject> jsonProducerOperation;
	private IRunner runnerObserver;

	private final String HUB_APPLICATION_UUID = StringUtils.getEnvVariable("HUB_APPLICATION_UUID");
	private final String HUB_USERNAME = StringUtils.getEnvVariable("HUB_USERNAME");
	private final String HUB_PASSWORD = StringUtils.getEnvVariable("HUB_PASSWORD");
	private final String HUB_START_URL = "https://ngci.encs.concordia.ca/api/v1.4/session/start";
	private final String HUB_RETROFIT_URL = "https://ngci.encs.concordia.ca/api/v1.4/persistence/full-retrofit-results";

	public void setBuildingIds(JsonArray buildingIds) {
		this.buildingIds = buildingIds;
	}

	@Override
	public void setOperation(IOperation operation) {
		this.jsonProducerOperation = operation;
	}

	private String getBody() {
		final JsonObject wrapper = new JsonObject();
		final JsonArray scenarioArray = new JsonArray();
		final JsonObject scenarioObject = new JsonObject();
		final List<String> scenarioNames = List.of("current status", "skin retrofit", "system retrofit and pv",
				"skin and system retrofit with pv");

		for (String scenarioName : scenarioNames) {
			scenarioObject.add(scenarioName, this.buildingIds);
		}
		scenarioArray.add(scenarioObject);
		wrapper.add("scenarios", scenarioArray);
		return wrapper.toString();
	}

	private RequestOptions getStartOptions() {

		RequestOptions startOptions = new RequestOptions();
		startOptions.setIsReturnHeaders(true);
		startOptions.addToHeaders("Application-Uuid", HUB_APPLICATION_UUID);
		startOptions.addToHeaders("Username", HUB_USERNAME);
		startOptions.addToHeaders("Password", HUB_PASSWORD);
		startOptions.addToHeaders("accept", "application/json");
		startOptions.addToHeaders("content-type", "application/json");
		startOptions.setMethod("PUT");
		return startOptions;
	}

	private RequestOptions getRetrofitOptions(JsonObject startResponseHeaders) {
		RequestOptions retrofitOptions = new RequestOptions();
		retrofitOptions.setRequestBody(getBody());
		retrofitOptions.addToHeaders("token", startResponseHeaders.get("token").getAsString());
		retrofitOptions.addToHeaders("session-id", startResponseHeaders.get("session_id").getAsString());
		retrofitOptions.addToHeaders("Application-Uuid", HUB_APPLICATION_UUID);
		retrofitOptions.addToHeaders("accept", "application/json");
		retrofitOptions.addToHeaders("content-type", "application/json");
		retrofitOptions.setMethod("POST");
		return retrofitOptions;
	}

	private JsonObject startHubSession() {
		try {
			final RequestOptions startOptions = this.getStartOptions();
			final JSONProducer startProducer = new JSONProducer(HUB_START_URL, startOptions);
			final SingleStepRunner deckard = new SingleStepRunner(startProducer);
			final Thread runnerTask = new Thread() {
				public void run() {
					try {
						deckard.runSteps();
						while (!deckard.isDone()) {
							System.out.println("Busy waiting!");
						}
					} catch (Exception e) {
						deckard.setAsDone();
					}

				}
			};
			runnerTask.start();
			runnerTask.join();

			final InMemoryDataStore memoryStore = InMemoryDataStore.getInstance();
			final String runnerId = deckard.getMetadata("id").toString();
			final IProducer<?> storeResult = memoryStore.get(runnerId);
			if (storeResult != null) {
				final ArrayList<JsonObject> retrofitResults = (ArrayList<JsonObject>) memoryStore.get(runnerId)
						.getResult();
				if (retrofitResults != null && !retrofitResults.isEmpty()) {
					return retrofitResults.get(0);
				}

			}
		} catch (InterruptedException e) {
			ArrayList<JsonObject> errorMessageList = new ArrayList<>();
			JsonObject errorMessage = new JsonObject();
			errorMessage.addProperty("error", e.getMessage());
			errorMessageList.add(errorMessage);
			this.setResult(errorMessageList);
		}
		return new JsonObject();
	}

	@Override
	public void fetch() {

		final JsonObject errorObject = new JsonObject();

		if (this.buildingIds != null && !buildingIds.isEmpty()) {
			// start: get session token
			JsonObject startResponseHeaders = this.startHubSession();

			// get retrofit result
			RequestOptions requestOptions = this.getRetrofitOptions(startResponseHeaders);
			this.jsonProducer = new JSONProducer(HUB_RETROFIT_URL, requestOptions);
			this.jsonProducer.setOperation(this.jsonProducerOperation);
			this.jsonProducer.addObserver(this.runnerObserver);
			this.jsonProducer.fetch();
		} else {
			errorObject.addProperty("error",
					"No buildingIds informed. Please use the 'buildingIds' parameter to specify buildingIds.");
			ArrayList<JsonObject> result = new ArrayList<>();
			result.add(errorObject);
			this.setResult(result);
			super.setOperation(this.jsonProducerOperation);
			super.addObserver(this.runnerObserver);
		}

		this.applyOperation();
	}

	@Override
	public void addObserver(final IRunner aRunner) {
		this.runnerObserver = aRunner;
	}

}