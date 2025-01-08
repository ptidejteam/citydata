package ca.concordia.ngci.tools4cities.middleware.middleware;

import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * This is an abstract consumer implementing the observer pattern.
 */
public abstract class AbstractConsumer<E> implements IConsumer<E> {

	public AbstractConsumer(final Set<IProducer<E>> setOfProducers) {
		for (Iterator<IProducer<E>> iterator = setOfProducers.iterator(); iterator.hasNext();) {

			final IProducer<E> producer = iterator.next();
			producer.addObserver(this);
			try {
				producer.fetchData();
			} catch (Exception ex) {
				System.out.println(ex);
			}
		}
	}

	@Override
	public void newDataAvailable(List<E> data) {
		throw new UnsupportedOperationException("Not implemented.");
	}

}
