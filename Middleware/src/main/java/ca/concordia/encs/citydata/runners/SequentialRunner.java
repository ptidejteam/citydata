package ca.concordia.encs.citydata.runners;

import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.UUID;
import java.util.List;

import ca.concordia.encs.citydata.core.exceptions.MiddlewareException;
import org.springframework.stereotype.Component;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import ca.concordia.encs.citydata.core.contracts.IOperation;
import ca.concordia.encs.citydata.core.contracts.IProducer;
import ca.concordia.encs.citydata.core.contracts.IRunner;
import ca.concordia.encs.citydata.core.implementations.AbstractRunner;
import ca.concordia.encs.citydata.core.utils.ProducerUsageData;
import ca.concordia.encs.citydata.core.utils.ReflectionUtils;
import ca.concordia.encs.citydata.datastores.InMemoryDataStore;
import ca.concordia.encs.citydata.datastores.MongoDataStore;
import ca.concordia.encs.citydata.producers.ExceptionProducer;

/***
 * This Runner starts with data provided by a producer P1, then applies
 * operations in order based on P1' (P1 prime). For example: P1 + O1 = P1'. P1'
 * + O2 -> P1'', etc. It is notified via the Observer pattern when P1' is ready,
 * then proceeds to the next Operation until all operations are completed.
 *
 * @author Gabriel C. Ullmann
 * @date 2025-05-27
 */
@Component
public class SequentialRunner extends AbstractRunner implements IRunner {

	private MongoDataStore mongoDataStore = MongoDataStore.getInstance();
	private JsonObject steps = null;
	private int operationCounter = 0;

	public SequentialRunner(JsonObject steps) {
		this.steps = steps;
	}

	@Override
	public void runSteps() throws Exception {
		// if there are no steps to run, warn the user and stop
		if (this.steps == null) {
			this.setAsDone();
			throw new MiddlewareException.NoStepsToRunException("No steps to run! Please provide steps so the runner can execute them.");
		}

		// start by extracting Producers, Operations and their params from the query
		System.out.println("Run started!");
		String producerName = ReflectionUtils.getRequiredField(this.steps, "use").getAsString();
		JsonArray producerParams = ReflectionUtils.getRequiredField(this.steps, "withParams").getAsJsonArray();

		// instantiate a new Producer instance and set its params
		Object producerInstance = ReflectionUtils.instantiateClass(producerName);
		ReflectionUtils.setParameters(producerInstance, producerParams);

		// set query to producer so we can check it later against other queries
		Method setMetadataMethod = producerInstance.getClass().getMethod("setMetadata", String.class, Object.class);
		setMetadataMethod.invoke(producerInstance, "query", this.steps);

		// add this Runner as an observer of the Producer instance
		Method addObserverMethod = producerInstance.getClass().getMethod("addObserver", IRunner.class);
		addObserverMethod.invoke(producerInstance, this);

		// if there are operations, apply the first one
		// subsequent operation will be applied on P1' once the first is done
		this.applyNextOperation((IProducer<?>) producerInstance);

	}

	@Override
	public void applyNextOperation(IProducer<?> producer) throws Exception {
		/*
		 * get list of operations and choose which one to execute next based on the
		 * sequential operation counter
		 */
		JsonArray operationsToApply = ReflectionUtils.getRequiredField(this.steps, "apply").getAsJsonArray();
		int totalOperations = operationsToApply.size();
		if (producer != null && totalOperations > 0) {

			JsonObject currentOperation = operationsToApply.get(this.operationCounter).getAsJsonObject();

			// instantiate current operation
			String operationName = ReflectionUtils.getRequiredField(currentOperation, "name").getAsString();
			Object operationInstance = ReflectionUtils.instantiateClass(operationName);

			// extract operation parameters and set them
			JsonArray operationParams = ReflectionUtils.getRequiredField(currentOperation, "withParams")
					.getAsJsonArray();
			ReflectionUtils.setParameters(operationInstance, operationParams);

			// set operation to producer
			Method setOperationMethod = producer.getClass().getMethod("setOperation", IOperation.class);

			setOperationMethod.invoke(producer, operationInstance);

			// trigger data fetching, which will in turn apply the operation
			System.out.println("Applying operation " + (this.operationCounter + 1) + " out of " + totalOperations);
		} else {
			System.out.println("No operations to apply");
		}

		producer.fetch();

	}

	@Override
	public void newDataAvailable(IProducer<?> producer) {

		// congratulations, you are done with your operation, go to the next one
		this.operationCounter += 1;

		try {
			// but is there really a next one? if not, stop
			JsonArray operationsToApply = ReflectionUtils.getRequiredField(this.steps, "apply").getAsJsonArray();
			if (this.operationCounter >= operationsToApply.size()) {
				this.storeResults(producer);
				this.setAsDone();
				System.out.println("Run completed!");
			} else {
				// if there are operations to be applied, apply the first one
				// subsequent operations will be applied on the P1' once the first is done
				this.applyNextOperation(producer);
			}
		} catch (Exception e) {
			// stop runner as soon as an exception is thrown to avoid infinite loops
			InMemoryDataStore store = InMemoryDataStore.getInstance();
			store.set(this.getId(), new ExceptionProducer(e));
			this.setAsDone();
		}

	}

	@Override
	public void newOperationApplied(IOperation<?> operation) {
		// this is mostly for debugging purposes, it could be removed in the future
		System.out.println("Operation applied: " + operation.getClass().getCanonicalName());
	}

	@Override
	public void storeResults(IProducer<?> producer) throws Exception {

		// the task is done, register a timestamp in the producer so we can keep track
		// of when it is done
		Method setMetadataMethod = producer.getClass().getMethod("setMetadata", String.class, Object.class);
		Date timeObject = Calendar.getInstance().getTime();
		String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(timeObject);
		setMetadataMethod.invoke(producer, "timestamp", timestamp);

		// store producer in the datastore
		InMemoryDataStore store = InMemoryDataStore.getInstance();
		UUID runnerId = this.getId();
        store.set(runnerId, producer);

		String producerName = this.steps.get("use").getAsString();
		this.storeProducerCallInfo(runnerId, this.steps.toString(), producerName);
	}

	private void storeProducerCallInfo(UUID runnerId, String requestBody, String producerName) {
		ProducerUsageData callInfo = new ProducerUsageData("anonymous", new Date(), requestBody, producerName);
		/*
		 * If Spring does not find a MongoDB connection string in its properties, this
		 * variable will be null. Otherwise, it will contain a value, which means a
		 * connection to MongoDB is available.
		 */
		if (mongoDataStore != null && mongoDataStore.hasConnection()) {
			List<ProducerUsageData> callInfoList = mongoDataStore.findByProducerName(producerName);
			if (callInfoList.isEmpty()) {
				mongoDataStore.save(callInfo);
			} else {
				ProducerUsageData existingCallInfo = callInfoList.get(0);
				existingCallInfo.setTimestamp(new Date());
				mongoDataStore.save(existingCallInfo);
			}
		}

	}

}