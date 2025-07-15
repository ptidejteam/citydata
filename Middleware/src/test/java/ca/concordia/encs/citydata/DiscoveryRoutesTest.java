package ca.concordia.encs.citydata;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import ca.concordia.encs.citydata.core.configs.AppConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.web.servlet.MockMvc;

/***
 * Tests for Discovery routes
 *
 * @author Sikandar Ejaz
 * @since 2025-04-08
 */
@SpringBootTest(classes = AppConfig.class)
@AutoConfigureMockMvc
@ComponentScan(basePackages = "ca.concordia.encs.citydata.core")
public class DiscoveryRoutesTest {
	
	@Autowired
	private MockMvc mockMvc;

	@Test
	void testListOperationsController() throws Exception {
		mockMvc.perform(get("/operations/list")).andExpect(status().is2xxSuccessful())
				// Check presence of a known operation from your perfect input (e.g.,
				// MergeOperation)
				.andExpect(content().string(containsString("ca.concordia.encs.citydata.operations.MergeOperation")))
				// Check params string pattern for MergeOperation
				.andExpect(content().string(containsString("targetProducerParams")))
				.andExpect(content().string(containsString("targetProducer")));
	}

	@Test
	void testListProducerController() throws Exception {
		mockMvc.perform(get("/producers/list")).andExpect(status().is2xxSuccessful())
				// Check presence of a known producer from your perfect input (e.g.,
				// EnergyConsumptionProducer)
				.andExpect(content()
						.string(containsString("ca.concordia.encs.citydata.producers.EnergyConsumptionProducer")))
				// Check one expected parameter from that producer
				.andExpect(content().string(containsString("operation")))
				.andExpect(content().string(containsString("city")));
	}

	@Test
	void testRouteController() throws Exception {
		mockMvc.perform(get("/routes/list")).andExpect(status().is2xxSuccessful())
				// The response is a JSON array of route description strings, check for a known
				// route path
				.andExpect(jsonPath("$").isArray())
				.andExpect(jsonPath("$[?(@ =~ /.*\\/operations\\/list.*/)]").exists())
				.andExpect(content().string(containsString("/operations/list")))
				.andExpect(content().string(containsString("Method: [GET]")));
	}

}
