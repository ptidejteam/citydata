package ca.concordia.encs.citydata.test.producers;


import java.util.ArrayList;

import org.junit.jupiter.api.Test;

import com.google.gson.JsonObject;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import ca.concordia.encs.citydata.producers.XMLBuildingProducer;

public class XMLBuildingProducerTest {

	@Test
	public void testXmlBuildingProducer() {
		final XMLBuildingProducer producer = new XMLBuildingProducer(null);
		producer.setFilePath("./src/test/resources/Building.xml");
		producer.fetch();

		ArrayList<JsonObject> result = producer.getResult();
		System.out.println("Result size: " + result.size());
		result.forEach(System.out::println);

		assertFalse(result.isEmpty(), "Should produce at least one building record");
		assertEquals("Montreal", result.get(0).get("city").getAsString());
	}
}