package ca.concordia.encs.citydata.producers;

import java.security.InvalidParameterException;

import ca.concordia.encs.citydata.core.implementations.AbstractProducer;
import ca.concordia.encs.citydata.producers.base.JSONProducer;
import ca.concordia.encs.citydata.core.contracts.IOperation;
import ca.concordia.encs.citydata.core.contracts.IProducer;
import ca.concordia.encs.citydata.core.contracts.IRunner;

/**
 * This Producer outputs GeoJSON geometries for a given city.
 *
 * @author Gabriel C. Ullmann
 * @since 2025-05-28
 */
public class GeometryProducer extends AbstractProducer<String> implements IProducer<String> {
    private JSONProducer jsonProducer;

	public void setCity(String city) {
        if (city != null) {
			jsonProducer = new JSONProducer("docs/examples/data/" + city + "_geometries.json", null);
		} else {
			throw new InvalidParameterException("Please provide a city name to the producer.");
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