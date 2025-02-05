package ca.concordia.encs.citydata.runners;

import java.lang.reflect.Method;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import ca.concordia.encs.citydata.core.AbstractRunner;
import ca.concordia.encs.citydata.core.IDataStore;
import ca.concordia.encs.citydata.core.IOperation;
import ca.concordia.encs.citydata.core.IProducer;
import ca.concordia.encs.citydata.core.IRunner;
import ca.concordia.encs.citydata.datastores.InMemoryDataStore;

/**
 *
 * This Runner starts with data provided by a producer P1, then applies
 * operations in order based on P1' (P1 prime). For example: P1 + O1 = P1'. P1'
 * + O2 -> P1'', etc.
 * 
 */
public class SequentialRunner extends AbstractRunner implements IRunner {

	private JsonObject steps = null;
	private int operationCounter = 0;

	public SequentialRunner(JsonObject steps) {
		this.steps = steps;
	}

	private JsonElement getRequiredField(JsonObject jsonObject, String fieldName) {
		if (!jsonObject.has(fieldName)) {
			throw new IllegalArgumentException("Error: Missing '" + fieldName + "' field");
		}
		return jsonObject.get(fieldName);
	}

	private Object instantiateClass(String className) throws Exception {
		Class<?> clazz = Class.forName(className);
		return clazz.getDeclaredConstructor().newInstance();
	}

	private void setParameters(Object instance, JsonArray params) throws Exception {
		Class<?> clazz = instance.getClass();
		for (JsonElement paramElement : params) {
			JsonObject paramObject = paramElement.getAsJsonObject();
			String paramName = paramObject.get("name").getAsString();
			JsonElement paramValue = paramObject.get("value");
			Method setter = findSetterMethod(clazz, paramName, paramValue);
			setter.invoke(instance, convertValue(setter.getParameterTypes()[0], paramValue));
		}
	}

	private Method findSetterMethod(Class<?> clazz, String paramName, JsonElement paramValue)
			throws NoSuchMethodException {
		String methodName = "set" + capitalize(paramName);
		for (Method method : clazz.getMethods()) {
			if (method.getName().equals(methodName) && method.getParameterCount() == 1) {
				return method;
			}
		}
		throw new NoSuchMethodException("No suitable setter found for " + paramName);
	}

	private Object convertValue(Class<?> targetType, JsonElement value) {
		if (targetType == int.class || targetType == Integer.class) {
			return value.getAsInt();
		} else if (targetType == boolean.class || targetType == Boolean.class) {
			return value.getAsBoolean();
		} else if (targetType == double.class || targetType == Double.class) {
			return value.getAsDouble();
		} else {
			return value.getAsString();
		}
	}

	private String capitalize(String str) {
		return str == null || str.isEmpty() ? str : str.substring(0, 1).toUpperCase() + str.substring(1);
	}

	@Override
	public void runSteps() throws Exception {
		// if there are no steps to run, warn the user and stop
		if (this.steps == null) {
			this.setAsDone();
			throw new RuntimeException("No steps to run! Please provide steps so the runner can execute them.");
		}

		// start by extracting Producers, Operations and their params from the query
		System.out.println("Run started!");
		String producerName = getRequiredField(this.steps, "use").getAsString();
		JsonArray producerParams = getRequiredField(this.steps, "withParams").getAsJsonArray();

		// spin up a new Producer instance and set its params
		Object producerInstance = instantiateClass(
				"ca.concordia.ngci.tools4cities.middleware.producers." + producerName);
		setParameters(producerInstance, producerParams);

		// TODO: add observer to producer instance
		for (Method method : producerInstance.getClass().getMethods()) {
			if (method.getName().equals("addObserver") && method.getParameterCount() == 1) {
				method.invoke(producerInstance, this);
				break;
			}
		}

		// if there are operations, apply the first one
		// subsequent operation will be applied on P1' once the first is done
		this.applyNextOperation((IProducer<?>) producerInstance);

	}

	@Override
	public void applyNextOperation(IProducer<?> producer) throws Exception {
		JsonArray operationsToApply = getRequiredField(this.steps, "apply").getAsJsonArray();
		JsonObject currentOperation = operationsToApply.get(this.operationCounter).getAsJsonObject();

		// spin up current operation and their params
		JsonObject operationNode = currentOperation.getAsJsonObject();
		String operationName = getRequiredField(operationNode, "name").getAsString();
		JsonArray operationParams = getRequiredField(operationNode, "withParams").getAsJsonArray();

		Object operationInstance = instantiateClass(
				"ca.concordia.ngci.tools4cities.middleware.operations." + operationName);
		setParameters(operationInstance, operationParams);

		// set operation to producer
		for (Method method : producer.getClass().getMethods()) {
			if (method.getName().equals("setOperation") && method.getParameterCount() == 1) {
				method.invoke(producer, operationInstance);
				break;
			}
		}

		System.out.println("Applying operation " + (this.operationCounter + 1) + " out of " + operationsToApply.size());
		producer.fetch();

	}

	@Override
	public void newDataAvailable(IProducer<?> producer) throws Exception {

		// congratulations, you are done with your operation, go to the next one
		this.operationCounter += 1;

		// but is there really a next one? if not, stop
		JsonArray operationsToApply = this.steps.get("apply").getAsJsonArray();
		if (this.operationCounter >= operationsToApply.size()) {
			this.storeResults(producer);
			this.setAsDone();
			System.out.println("Run completed!");
		} else {
			// if there are operations to be applied, apply the first one
			// subsequent operations will be applied on the P1' once the first is done
			this.applyNextOperation(producer);
		}

	}

	@Override
	public void newOperationApplied(IOperation<?> operation) {
		// this is mostly for debugging purposes, it could be removed in the future
		System.out.println("Operation applied: " + operation.getClass().getCanonicalName());
	}

	@Override
	public void storeResults(IProducer<?> producer) {
		IDataStore store = InMemoryDataStore.getInstance();
		String runnerId = this.getMetadata("id").toString();
		store.set(runnerId, producer);
	}

}
