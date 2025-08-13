package ca.concordia.encs.citydata.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.fail;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import ca.concordia.encs.citydata.PayloadFactory;
import ca.concordia.encs.citydata.core.configs.AppConfig;

/**
 * ExistsController routes test
 *
 * @author Minette Zongo
 * @since 2025-02-26
 *  
 * Last Update: Fixed failing tests after implementing Authentication
 * @author Sikandar Ejaz 
 * @since 2025-07-18 
 * 
 * Last Update: Added universal MockMvc
 * @author Sikandar Ejaz
 * @since 2025-08-13
*/

@SpringBootTest(classes = AppConfig.class)
@AutoConfigureMockMvc
@ComponentScan(basePackages = "ca.concordia.encs.citydata.core")
public class ExistsTest extends AuthenticatorMvc {

	// Store the runnerId as an instance variable so it can be used across test methods
	private UUID runnerId;

	@Test
	void testQueryExists() throws Exception {
		// Use getExampleQuery to load a specific query from a JSON file
		String jsonPayload = PayloadFactory.getExampleQuery("stringProducerRandom");

		// creating a producer
		MvcResult syncResult = mockMvc
				.perform(post("/apply/sync").header("Authorization", "Bearer " + getToken())
						.contentType(MediaType.APPLICATION_JSON).content(jsonPayload))
				.andExpect(status().isOk()).andReturn();

		// Get the response but don't try to parse it as a UUID
		String resultJson = syncResult.getResponse().getContentAsString();

		// Store this result for later use if needed, but don't parse as UUID
		// If you really need a UUID for later, you'll need to extract it from the JSON

		// Check if the query exists
		MvcResult existsResult = mockMvc
				.perform(post("/exists/").header("Authorization", "Bearer " + getToken())
						.contentType(MediaType.APPLICATION_JSON).content(jsonPayload))
				.andExpect(status().isOk()).andReturn();

		String responseContent = existsResult.getResponse().getContentAsString();

		assertNotEquals("[]", responseContent, "Response should not be an empty array");
	}

	@Test
	void testQueryNotExists() throws Exception {
		String jsonPayload = PayloadFactory.getExampleQuery("ckanMetadataProducerListDatasets");

		MvcResult existsResult = mockMvc
				.perform(post("/exists/").header("Authorization", "Bearer " + getToken())
						.contentType(MediaType.APPLICATION_JSON).content(jsonPayload))
				.andExpect(status().isNotFound()).andReturn();

		String responseContent = existsResult.getResponse().getContentAsString();
		assertEquals("[]", responseContent);
	}

	@Test
	void testBrokenJsonQuery() throws Exception {
		String jsonPayload = PayloadFactory.getInvalidJson();
		mockMvc.perform(post("/exists/").header("Authorization", "Bearer " + getToken())
				.contentType(MediaType.APPLICATION_JSON).content(jsonPayload))
				.andExpect(status().isInternalServerError());
	}

	@Test
	void testQueryExistsFollowedBySync() throws Exception {
		String jsonPayload = PayloadFactory.getExampleQuery("stringProducerRandom");

		MvcResult existsResult = mockMvc.perform(post("/exists/").header("Authorization", "Bearer " + getToken())
				.contentType(MediaType.APPLICATION_JSON).content(jsonPayload)).andReturn();

		String responseContent = existsResult.getResponse().getContentAsString();
		int status = existsResult.getResponse().getStatus();

		if (status == 404 || responseContent.equals("[]")) {
			MvcResult syncResult = mockMvc.perform(post("/apply/sync").header("Authorization", "Bearer " + getToken())
					.contentType(MediaType.APPLICATION_JSON).content(jsonPayload)).andReturn();

			int syncStatus = syncResult.getResponse().getStatus();
			String syncResponse = syncResult.getResponse().getContentAsString();

			// Log the response before failing
			System.out.println("apply/sync Status: " + syncStatus);
			System.out.println("apply/sync Response: " + syncResponse);

			if (syncStatus != 200) {
				fail("apply/sync failed with status: " + syncStatus + " and response: " + syncResponse);
			}
		} else if (status == 200) {
			assertNotEquals("[]", responseContent, "Response should not be an empty array");
		} else {
			fail("Unexpected status code: " + status + ". Response content: " + responseContent);
		}
	}
}