package ca.concordia.encs.citydata.core;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;

import ca.concordia.encs.citydata.base.BaseMvc;
import ca.concordia.encs.citydata.core.configs.AppConfig;

/**
 * Discovery routes test
 *
 * @author Sikandar Ejaz
 * @since 18-06-2025
 * 
 * Last Update: Removed local mockMvc instance, used from BaseMvc
 * @author Sikandar Ejaz
 * @since 12-08-2025
 */

@SpringBootTest(classes = AppConfig.class)
@AutoConfigureMockMvc
@ComponentScan(basePackages = "ca.concordia.encs.citydata.core")
public class DiscoveryRoutesTest extends BaseMvc {

	@Test
	public void testRoutesList() throws Exception {
		mockMvc.perform(get("/routes/list")).andExpect(status().isOk())
				.andExpect(content().string(containsString("Method: [")));

	}

	@Test
	public void testOperationsList() throws Exception {
		mockMvc.perform(get("/operations/list")).andExpect(status().isOk())
				.andExpect(content().string(containsString("ca.concordia.encs.citydata")));
	}

	@Test
	public void testProducersList() throws Exception {
		mockMvc.perform(get("/producers/list")).andExpect(status().isOk())
				.andExpect(content().string(containsString("ca.concordia.encs.citydata")));
	}

}
