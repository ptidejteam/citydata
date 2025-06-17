package ca.concordia.encs.citydata.producers;

import java.security.InvalidParameterException;

import ca.concordia.encs.citydata.core.implementations.AbstractProducer;
import ca.concordia.encs.citydata.producers.base.JSONProducer;
import ca.concordia.encs.citydata.core.contracts.IOperation;
import ca.concordia.encs.citydata.core.contracts.IProducer;
import ca.concordia.encs.citydata.core.contracts.IRunner;

public class GeometryProducer extends AbstractProducer<String> implements IProducer<String> {
	private String city;
	private JSONProducer jsonProducer;

	public void setCity(String city) {
		this.city = city;
		if (this.city != null) {
			jsonProducer = new JSONProducer("docs/examples/data/" + this.city + "_geometries.json", null);
		} else {
			throw new InvalidParameterException("Please provide a city name to the producer.");
		}
	}

	@Override
	public void setOperation(IOperation operation) {
		this.jsonProducer.operation = operation;
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