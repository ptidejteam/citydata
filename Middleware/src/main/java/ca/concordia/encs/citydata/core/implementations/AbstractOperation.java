package ca.concordia.encs.citydata.core.implementations;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import ca.concordia.encs.citydata.core.contracts.IOperation;
import ca.concordia.encs.citydata.core.contracts.IRunner;

import ca.concordia.encs.citydata.core.exceptions.MiddlewareException;


/**
 *
 * This implements features common to all Operations, such as notifying Runners
 * 
 * @author Gabriel C. Ullmann
 * @date 2025-05-27
 */
public abstract class AbstractOperation<E> extends AbstractEntity implements IOperation<E> {

	private final Set<IRunner> runners = new HashSet<>();

	public AbstractOperation() {
		this.setMetadata("role", "operation");
	}

	@Override
	public void addObserver(final IRunner aRunner) {
		this.runners.add(aRunner);
	}

	@Override
	public void notifyObservers() {
		for (final Iterator<IRunner> iterator = this.runners.iterator(); iterator.hasNext();) {
			final IRunner runner = iterator.next();
			runner.newOperationApplied(this);
		}
	}

}
