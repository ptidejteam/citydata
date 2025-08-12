package ca.concordia.encs.citydata;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;

import ca.concordia.encs.citydata.base.BaseMvc;
import ca.concordia.encs.citydata.core.configs.AppConfig;

/**
 * Tests for Discovery routes
 *
 * @author Sikandar Ejaz
 * @since 08-04-2025
 * 
 * Last Update: Removed local mockMvc instance, used from BaseMvc and removed unnecessary comments
 * @author Sikandar Ejaz
 * @since 12-08-2025
 */

@SpringBootTest(classes = AppConfig.class)
@AutoConfigureMockMvc
@ComponentScan(basePackages = "ca.concordia.encs.citydata.core")
public class DiscoveryRoutesTest extends BaseMvc {

	@Test
	void testListOperationsController() throws Exception {
		mockMvc.perform(get("/operations/list")).andExpect(status().is2xxSuccessful())
				.andExpect(content().string(containsString("ca.concordia.encs.citydata.operations.MergeOperation")))
				.andExpect(content().string(containsString("targetProducerParams")))
				.andExpect(content().string(containsString("targetProducer")));
	}

	@Test
	void testListProducerController() throws Exception {
		mockMvc.perform(get("/producers/list")).andExpect(status().is2xxSuccessful())
				.andExpect(content()
						.string(containsString("ca.concordia.encs.citydata.producers.EnergyConsumptionProducer")))
				.andExpect(content().string(containsString("operation")))
				.andExpect(content().string(containsString("city")));
	}

	@Test
	void testRouteController() throws Exception {
		mockMvc.perform(get("/routes/list")).andExpect(status().is2xxSuccessful()).andExpect(jsonPath("$").isArray())
				.andExpect(jsonPath("$[?(@ =~ /.*\\/operations\\/list.*/)]").exists())
				.andExpect(content().string(containsString("/operations/list")))
				.andExpect(content().string(containsString("Method: [GET]")));
	}

}
