package ca.concordia.encs.citydata.producers;

import java.util.ArrayList;
import java.util.Random;

import ca.concordia.encs.citydata.core.contracts.IOperation;
import ca.concordia.encs.citydata.core.contracts.IProducer;
import ca.concordia.encs.citydata.core.implementations.AbstractProducer;

/**
 *
 * This Producer outputs random strings. For test only.
 *
 * @author Gabriel C. Ullmann, Minette Zongo
 * @date 2025-05-28
 */
public class RandomStringProducer extends AbstractProducer<String> implements IProducer<String> {

	private int stringLength = 10;

	public void setStringLength(Integer stringLength) {
		if (stringLength > 0 && stringLength < 999) {
			this.stringLength = stringLength;
		}
	}

	@Override
	public void fetch() {
		ArrayList<String> resultingString = new ArrayList<>();
		if (this.isEmpty()) {
			String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
			Random random = new Random();
			StringBuilder randomString = new StringBuilder();
			for (int i = 0; i < this.stringLength; i++) {
				int index = random.nextInt(characters.length());
				randomString.append(characters.charAt(index));
			}
			resultingString.add(randomString.toString());
			this.setResult(resultingString);
		}
		this.applyOperation();
	}
}