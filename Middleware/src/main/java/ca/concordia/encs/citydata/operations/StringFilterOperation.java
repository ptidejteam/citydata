package ca.concordia.encs.citydata.operations;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import ca.concordia.encs.citydata.core.implementations.AbstractOperation;
import ca.concordia.encs.citydata.core.contracts.IOperation;

/**
 * This operation filters an array of strings by a substring.
 *
 * @author Gabriel C. Ullmann
 * @date 2025-01-01
 */
public class StringFilterOperation extends AbstractOperation<String> implements IOperation<String> {
	private String filterBy;
	private Boolean isExactlyEqual = false;

	public void setFilterBy(String filterBy) {
		this.filterBy = filterBy;
	}

	public void setIsExactlyEqual(Boolean isExactlyEqual) {
		this.isExactlyEqual = isExactlyEqual;
	}

	@Override
	public ArrayList<String> apply(ArrayList<String> inputs) {
		List<String> filteredList = inputs;
		if (!isExactlyEqual) {
			filteredList = inputs.stream().filter(s -> s.toString().contains(filterBy)).collect(Collectors.toList());
		} else {
			filteredList = inputs.stream().filter(s -> s.toString().equalsIgnoreCase(filterBy))
					.collect(Collectors.toList());
		}
		return new ArrayList<String>(filteredList);
	}
}
