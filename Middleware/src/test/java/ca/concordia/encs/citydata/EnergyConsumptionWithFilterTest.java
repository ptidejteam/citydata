package ca.concordia.encs.citydata;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.ArrayList;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import ca.concordia.encs.citydata.core.configs.AppConfig;
import ca.concordia.encs.citydata.datastores.InMemoryDataStore;
import ca.concordia.encs.citydata.producers.EnergyConsumptionProducer;
import ca.concordia.encs.citydata.runners.SingleStepRunner;

/***
 * This test validates the energy consumption data filtering through the API
 * endpoint and also directly through the producer component.
 * 
 * 
 * 
 * Originally tested with a file with 2.556.132.960 records, time series from
 * 2021-09-01 to 2022-08-31. Current mock file has 100 mock records.
 *
 * 
 * @author Minette Zongo M., Gabriel C. Ullmann
 * @date 2025-04-29
 */

/*
 * Last Update: 18-07-2025 Author Sikandar Ejaz Fixed failing tests after
 * implementing Authentication
 */

@SpringBootTest(classes = AppConfig.class)
@AutoConfigureMockMvc
@ComponentScan(basePackages = "ca.concordia.encs.citydata.core")
public class EnergyConsumptionWithFilterTest extends BaseIntegrationTest {
	@Autowired
	private MockMvc mockMvc;

	private EnergyConsumptionProducer energyConsumptionProducer;

	@BeforeEach
	void setUp() {
		energyConsumptionProducer = new EnergyConsumptionProducer();
	}

	@Test
	public void testEnergyConsumptionWithTimeFilter() throws Exception {

		String jsonPayload = PayloadFactory.getExampleQuery("energyConsumptionWithFilter");

		MvcResult mvcResult = mockMvc
				.perform(post("/apply/sync").header("Authorization", "Bearer " + getToken())
						.contentType(MediaType.APPLICATION_JSON).content(jsonPayload))
				.andExpect(status().isOk()).andExpect(content().string(containsString("clientId"))).andReturn();

		String responseContent = mvcResult.getResponse().getContentAsString();
		assertThat(responseContent).isNotEmpty();

		// test by direct instantiation of producer
		energyConsumptionProducer = new EnergyConsumptionProducer();
		energyConsumptionProducer.setCity("montreal");
		energyConsumptionProducer.setStartDatetime("2021-09-01 00:00:00");
		energyConsumptionProducer.setEndDatetime("2021-09-01 23:59:00");
		energyConsumptionProducer.setClientId(1);

		SingleStepRunner runner = new SingleStepRunner(energyConsumptionProducer);
		UUID runnerId = UUID.randomUUID();
		runner.setMetadata("id", runnerId.toString());

		Thread runnerThread = new Thread(() -> {
			try {
				runner.runSteps();
			} catch (Exception e) {
				System.err.println("Runner thread error: " + e.getMessage());
				e.printStackTrace();
			}
		});

		runnerThread.start();
		runnerThread.join();
		ArrayList<?> result = InMemoryDataStore.getInstance().get(runnerId).getResult();

		assertNotNull(result, "Result should not be null");
		assertThat(result).isNotEmpty();

		for (Object item : result) {
			String itemString = item.toString();
			assertTrue(itemString.contains("2021-09-01 00:00:00"),
					"Filtered item should contain '2021-09-01 00:00:00': " + itemString);
		}
	}

	@Test
	public void testEnergyConsumptionWithAverage() throws Exception {
		String jsonPayload = PayloadFactory.getExampleQuery("energyConsumptionAverage");
		mockMvc.perform(post("/apply/sync").header("Authorization", "Bearer " + getToken())
				.contentType(MediaType.APPLICATION_JSON).content(jsonPayload))
				.andExpect(status().isOk()).andExpect(content().string(containsString("0.35069153"))).andReturn();

	}

}