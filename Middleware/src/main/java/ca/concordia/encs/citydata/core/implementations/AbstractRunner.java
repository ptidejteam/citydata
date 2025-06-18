package ca.concordia.encs.citydata.core.implementations;

import ca.concordia.encs.citydata.core.contracts.IRunner;

/**
 *
 * This implements features common to all Runners, such as state control
 * (possibility to mark Runner as done after work is finished)
 * 
 * @author Gabriel C. Ullmann
 * @since 2025-04-23
 */
public abstract class AbstractRunner extends AbstractEntity implements IRunner {

	private boolean isDone = false;

	public AbstractRunner() {
		this.setMetadata("role", "runner");
	}

	@Override
	public boolean isDone() {
		return this.isDone;
	}

	@Override
	public void setAsDone() {
		this.isDone = true;
	}

}
