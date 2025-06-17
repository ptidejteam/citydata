package ca.concordia.encs.citydata.producers;

import java.security.InvalidParameterException;

import ca.concordia.encs.citydata.core.implementations.AbstractProducer;
import ca.concordia.encs.citydata.producers.base.CSVProducer;
import ca.concordia.encs.citydata.core.contracts.IOperation;
import ca.concordia.encs.citydata.core.contracts.IProducer;
import ca.concordia.encs.citydata.core.contracts.IRunner;

public class EnergyConsumptionProducer extends AbstractProducer<String> implements IProducer<String> {
	private String city;
	private CSVProducer csvProducer;

	public void setCity(String city) {
		this.city = city;
		if (this.city != null) {
			csvProducer = new CSVProducer("./docs/examples/data/" + this.city + "_energy_consumption.csv", null);
		} else {
			throw new InvalidParameterException("Please provide a city name to the producer.");
		}
	}

	@Override
	public void setOperation(IOperation operation) {
		this.csvProducer.operation = operation;
	}

	@Override
	public void fetch() {
		this.csvProducer.fetch();
	}

	@Override
	public void addObserver(final IRunner aConsumer) {
		this.csvProducer.addObserver(aConsumer);
	}

}
