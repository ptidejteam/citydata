package ca.concordia.encs.citydata.producers;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import ca.concordia.encs.citydata.PayloadFactory;
import ca.concordia.encs.citydata.core.TestTokenGenerator;
import ca.concordia.encs.citydata.core.configs.AppConfig;
import ca.concordia.encs.citydata.operations.MergeOperation;

/**
 * Tests the API endpoint with the merge operation between EnergyConsumption and Geometry producers
 *
 * @author Minette Zongo M. 
 * @since 29-04-2025 
 *
 * Last Update: Fixed failing tests after implementing Authentication
 * @author Sikandar Ejaz 
 * @since 18-07-2025
 */

@SpringBootTest(classes = AppConfig.class)
@AutoConfigureMockMvc
@ComponentScan(basePackages = "ca.concordia.encs.citydata.core")
public class GeometryProducerTest extends TestTokenGenerator {
	@Autowired
	private MockMvc mockMvc;

	private GeometryProducer geometryProducer;
	private EnergyConsumptionProducer energyConsumptionProducer;
	private MergeOperation mergeOperation;
	private final String CITY = "montreal";

	@BeforeEach
	void setUp() {
		geometryProducer = new GeometryProducer();
		energyConsumptionProducer = new EnergyConsumptionProducer();
		mergeOperation = new MergeOperation();
	}

	/**
	 * 
	 */
	@Test
	public void testMergeOperationViaAPI() throws Exception {
		// Get example query using the PayloadFactory
		String jsonPayload = PayloadFactory.getExampleQuery("mergeEnergyConsumptionAndGeometries");

		mockMvc.perform(post("/apply/sync").header("Authorization", "Bearer " + getToken())
				.contentType(MediaType.APPLICATION_JSON).content(jsonPayload)).andExpect(status().isOk())
				.andExpect(content().string(containsString("result")));
	}

}