package ca.concordia.encs.citydata.operations;

import java.util.ArrayList;

import ca.concordia.encs.citydata.core.implementations.AbstractOperation;
import ca.concordia.encs.citydata.core.contracts.IOperation;

/**
 * Placeholder operation
 * @author Gabriel C. Ullmann
 * @date 2025-06-18
 */
public class NullOperation extends AbstractOperation<Object> implements IOperation<Object> {

    @Override
    public ArrayList<Object> apply(ArrayList<Object> inputs) {
        return inputs != null ? inputs : new ArrayList<>();
    }

}
