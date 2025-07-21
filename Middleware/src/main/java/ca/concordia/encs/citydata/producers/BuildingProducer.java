package ca.concordia.encs.citydata.producers;

import ca.concordia.encs.citydata.core.contracts.IOperation;
import ca.concordia.encs.citydata.core.contracts.IProducer;
import ca.concordia.encs.citydata.core.contracts.IRunner;
import ca.concordia.encs.citydata.core.implementations.AbstractProducer;
import ca.concordia.encs.citydata.producers.base.JSONProducer;

import java.security.InvalidParameterException;

/**
 * This Producer outputs metadata about a building, such as floors, zones and sensors.
 * @author Gabriel C. Ullmann
 * @since 2025-05-28
 */
public class BuildingProducer extends AbstractProducer<String> implements IProducer<String> {
	private JSONProducer jsonProducer;

	public void setBuildingName(String buildingName) {
		if (buildingName != null) {
			jsonProducer = new JSONProducer("docs/examples/data/" + buildingName + "_building.json", null);
		} else {
			throw new InvalidParameterException("Please provide a building name to the producer.");
		}
	}

	@SuppressWarnings("rawtypes")
    @Override
	public void setOperation(IOperation operation) {
		this.jsonProducer.setOperation(operation);
	}

	@Override
	public void fetch() {
		this.jsonProducer.fetch();
	}

	@Override
	public void addObserver(final IRunner aRunner) {
		this.jsonProducer.addObserver(aRunner);
	}

}