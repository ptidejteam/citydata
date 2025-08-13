package ca.concordia.encs.citydata.producers;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * This java class is to test randomNumberProducer and occupancyProducer
 *
 * @author Sikandar Ejaz
 * @since 08-02-2025
 */

class ProducersTests {

	private RandomNumberProducer randomNumberProducer;
	private OccupancyProducer occupancyProducer;

	@BeforeEach
	void setUp() {
		randomNumberProducer = new RandomNumberProducer();
		occupancyProducer = new OccupancyProducer();
	}

	// Tests for RandomNumberProducer

	@Test
	void testRandomNumberProducer_withValidInput_shouldGenerateCorrectListSize() {
		randomNumberProducer.setListSize(5);
		randomNumberProducer.setGenerationDelay(0);

		randomNumberProducer.fetch();

		List<Integer> result = randomNumberProducer.getResult();
		assertThat(result).hasSize(5).allMatch(number -> number >= 0 && number < 100);
	}

	@Test
	void testRandomNumberProducer_withInvalidInput_shouldReturnEmptyList() {
		randomNumberProducer.setListSize(-1); // Invalid input
		randomNumberProducer.setGenerationDelay(0);

		randomNumberProducer.fetch();

		List<Integer> result = randomNumberProducer.getResult();
		assertThat(result).isNullOrEmpty();
	}

	// Tests for OccupancyProducer

	@Test
	void testOccupancyProducer_withValidInput_shouldGenerateCorrectListSize() {
		occupancyProducer.setListSize(5);

		occupancyProducer.fetch();

		List<String> result = occupancyProducer.getResult();
		assertThat(result).hasSize(5).allMatch(value -> value.equals("Occupied") || value.equals("Vacant"));
	}

	@Test
	void testOccupancyProducer_withInvalidInput_shouldReturnEmptyList() {
		occupancyProducer.setListSize(-1); // Invalid input

		occupancyProducer.fetch();

		List<String> result = occupancyProducer.getResult();
		assertThat(result).isNullOrEmpty();
	}
}