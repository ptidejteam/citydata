package ca.concordia.encs.citydata.operations;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import ca.concordia.encs.citydata.core.exceptions.MiddlewareException.ThreadInterruptedException;
import com.google.gson.JsonArray;

import ca.concordia.encs.citydata.core.implementations.AbstractOperation;
import ca.concordia.encs.citydata.core.contracts.IOperation;
import ca.concordia.encs.citydata.datastores.InMemoryDataStore;
import ca.concordia.encs.citydata.runners.SingleStepRunner;

/**
 * This operation merges two Producer results together.
 *
 * @author Gabriel C. Ullmann
 * @since 2025-01-01
 */
public class MergeOperation extends AbstractOperation<String> implements IOperation<String> {

	private String targetProducer;
	private JsonArray targetProducerParams;

	public void setTargetProducer(String targetProducer) {
		this.targetProducer = targetProducer;
	}

	public void setTargetProducerParams(JsonArray targetProducerParams) {
		this.targetProducerParams = targetProducerParams;
	}

	@Override
	public ArrayList<String> apply(ArrayList<String> inputs) {

		// all keys are timestamps because timestamps are unique, and JSON cannot have
		// duplicated keys
		final String timeStampFormat = "yyyy-MM-dd_HH:mm:ss";
		final Date timeObject = Calendar.getInstance().getTime();
		final String timeStampSource = new SimpleDateFormat(timeStampFormat).format(timeObject);
		final ArrayList<String> sourceList = new ArrayList<>();
		sourceList.add("{\"" + timeStampSource + "\": \"" + inputs + "\" }");

		try {
			final SingleStepRunner deckard = new SingleStepRunner(targetProducer, targetProducerParams);
			final Thread runnerTask = new Thread(() -> {
                try {
                    deckard.runSteps();
                    while (!deckard.isDone()) {
                        System.out.println("Busy waiting!");
                    }
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                }

            });
			runnerTask.start();
			runnerTask.join();

			final String runnerId = deckard.getMetadata("id").toString();
			final InMemoryDataStore store = InMemoryDataStore.getInstance();

			final ArrayList<?> targetList =  store.get(runnerId).getResult();
			if (targetList != null && !targetList.isEmpty()) {
				String timeStampTarget = new SimpleDateFormat(timeStampFormat).format(timeObject);
				sourceList.add("{\"" + timeStampTarget + "\": \"" + targetList + "\" }");
			}

		} catch (InterruptedException e) {
			throw new ThreadInterruptedException("Thread was interrupted during execution." +e.getMessage());
		}

		return sourceList;
	}
}
