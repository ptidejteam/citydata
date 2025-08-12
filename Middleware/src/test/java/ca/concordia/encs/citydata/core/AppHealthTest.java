package ca.concordia.encs.citydata.core;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.web.context.WebApplicationContext;

import ca.concordia.encs.citydata.base.BaseMvc;
import ca.concordia.encs.citydata.core.configs.AppConfig;

/**
 * App health routes test
 *
 * @author Minette Zongo
 * @since 2025-06-18
 * 
 * Last Update: Removed local mockMvc instance, used from BaseMvc
 * @author Sikandar Ejaz
 * @since 12-08-2025
 */

@SpringBootTest(classes = AppConfig.class)
@AutoConfigureMockMvc
@ComponentScan(basePackages = "ca.concordia.encs.citydata.core")
public class AppHealthTest extends BaseMvc {

	@Autowired
	private WebApplicationContext webApplicationContext;

	// Test for ping route
	@Test
	public void testPingRoute() throws Exception {
		mockMvc.perform(get("/health/ping")).andExpect(status().isOk())
				.andExpect(content().string(org.hamcrest.Matchers.startsWith("CITYdata running")));
	}

}
