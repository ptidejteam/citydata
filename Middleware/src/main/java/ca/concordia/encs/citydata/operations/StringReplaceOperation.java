package ca.concordia.encs.citydata.operations;

import java.util.ArrayList;

import ca.concordia.encs.citydata.core.implementations.AbstractOperation;
import ca.concordia.encs.citydata.core.contracts.IOperation;

/**
 *
 * This operation searches for a sequence in a string, and replaces this
 * sequence with another one.
 *
 * @author Gabriel C. Ullmann
 * @since 2025-01-01
 */
public class StringReplaceOperation extends AbstractOperation<String> implements IOperation<String> {

	private String searchFor = "";
	private String replaceBy = "";

	@SuppressWarnings("unused")
    public void setSearchFor(String searchFor) {
		this.searchFor = searchFor;
	}

	@SuppressWarnings("unused")
    public void setReplaceBy(String replaceBy) {
		this.replaceBy = replaceBy;
	}

	@SuppressWarnings("unused")
    @Override
	public ArrayList<String> apply(ArrayList<String> input) {
		final ArrayList<String> resultSet = new ArrayList<>();
		for (String item : input) {
			resultSet.add(item.replace(searchFor, replaceBy));
		}
		return resultSet;
	}

}
