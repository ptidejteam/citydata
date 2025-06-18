package ca.concordia.encs.citydata.core.contracts;

import ca.concordia.encs.citydata.core.exceptions.MiddlewareException;

import java.util.ArrayList;

/**
 *
 * The Producer entity is responsible for: - Fetching data - Applying a
 * operation on the result - Notify observers when both tasks are done
 *
 * @author Gabriel C. Ullmann
 * @since 2024-12-01
 */

public interface IProducer<E> {

	// 1 - prepare producer
	void addObserver(final IRunner aRunner);

	void setOperation(IOperation operation);

	// 2 - fetch data
	void fetch() throws MiddlewareException;

	// 3 - transform data and notify when done
	void applyOperation();

	void notifyObservers();

	// 4 - output data
	ArrayList<E> getResult();

}