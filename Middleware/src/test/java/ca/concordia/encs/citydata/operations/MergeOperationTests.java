package ca.concordia.encs.citydata.operations;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import ca.concordia.encs.citydata.PayloadFactory;
import ca.concordia.encs.citydata.TestTokenGenerator;
import ca.concordia.encs.citydata.core.configs.AppConfig;

/*
 * Tests added for MergeOperation 
 * Author: Sikandar Ejaz 
 * Date: 4-8-2025
 * Last Update: 16-06-2025
 * Fixed failing tests after implementing Authentication
 * 
 * Tests for MergeOperation
 * * @author Sikandar Ejaz
 * @since 2025-04-08
 */

@SpringBootTest(classes = AppConfig.class)
@AutoConfigureMockMvc
@ComponentScan(basePackages = "ca.concordia.encs.citydata.core")
public class MergeOperationTests extends TestTokenGenerator {

	@Autowired
	private MockMvc mockMvc;

	@Test
	public void testMergeOperation() throws Exception {
		String jsonPayload = PayloadFactory.getExampleQuery("mergeEnergyConsumptionAndGeometries");

		mockMvc.perform(post("/apply/sync").header("Authorization", "Bearer " + getToken())
				.contentType(MediaType.APPLICATION_JSON).content(jsonPayload)).andExpect(status().isOk());
	}

	@Test
	public void testMergeOperationMissingTargetProducer() throws Exception {
		String jsonPayload = PayloadFactory.getExampleQuery("mergeEnergyConsumptionAndGeometries");
		JsonObject jsonObject = com.google.gson.JsonParser.parseString(jsonPayload).getAsJsonObject();
		jsonObject.getAsJsonArray("apply").get(0).getAsJsonObject().remove("withParams");
		mockMvc.perform(post("/apply/sync").header("Authorization", "Bearer " + getToken())
				.contentType(MediaType.APPLICATION_JSON).content(jsonObject.toString()))
				.andExpect(status().is5xxServerError())
				.andExpect(content().string(containsString("")));
	}

	@Test
	public void testMergeOperationMissingTargetProducerParams() throws Exception {
		String jsonPayload = PayloadFactory.getExampleQuery("mergeEnergyConsumptionAndGeometries");
		JsonObject jsonObject = com.google.gson.JsonParser.parseString(jsonPayload).getAsJsonObject();
		JsonObject applyObject = jsonObject.getAsJsonArray("apply").get(0).getAsJsonObject();
		JsonArray withParamsArray = applyObject.getAsJsonArray("withParams");

		// Remove the targetProducerParams object from the withParams array.
		for (int i = 0; i < withParamsArray.size(); i++) {
			JsonObject param = withParamsArray.get(i).getAsJsonObject();
			if (param.has("name") && param.get("name").getAsString().equals("targetProducerParams")) {
				withParamsArray.remove(i);
				break; // Stop after removing the element.
			}
		}
		applyObject.add("withParams", withParamsArray);

		mockMvc.perform(post("/apply/sync").header("Authorization", "Bearer " + getToken())
				.contentType(MediaType.APPLICATION_JSON).content(jsonObject.toString()))
				.andExpect(status().is5xxServerError())
				.andExpect(content().string(containsString("")));
	}

	@Test
	public void testMergeOperationWrongTargetProducer() throws Exception {
		String jsonPayload = PayloadFactory.getExampleQuery("mergeEnergyConsumptionAndGeometries");
		JsonObject jsonObject = com.google.gson.JsonParser.parseString(jsonPayload).getAsJsonObject();
		jsonObject.getAsJsonArray("apply").get(0).getAsJsonObject().getAsJsonArray("withParams").get(0)
				.getAsJsonObject().addProperty("value", "Wrong.Producer");

		mockMvc.perform(post("/apply/sync").header("Authorization", "Bearer " + getToken())
				.contentType(MediaType.APPLICATION_JSON).content(jsonObject.toString()))
				.andExpect(status().is5xxServerError());
	}

	@Test
	public void testMergeOperationWrongTargetProducerParams() throws Exception {
		String jsonPayload = PayloadFactory.getExampleQuery("mergeEnergyConsumptionAndGeometries");
		JsonObject jsonObject = com.google.gson.JsonParser.parseString(jsonPayload).getAsJsonObject();
		jsonObject.getAsJsonArray("apply").get(0).getAsJsonObject().getAsJsonArray("withParams").get(1)
				.getAsJsonObject().getAsJsonArray("value").get(0).getAsJsonObject().addProperty("name", "wrongParam");

		mockMvc.perform(post("/apply/sync").header("Authorization", "Bearer " + getToken())
				.contentType(MediaType.APPLICATION_JSON).content(jsonObject.toString()))
				.andExpect(status().is5xxServerError());
	}

	@Test
	public void testMergeOperationWithBrokenJson() throws Exception {
		String brokenJson = PayloadFactory.getInvalidJson();
		mockMvc.perform(post("/apply/sync").header("Authorization", "Bearer " + getToken())
				.contentType(MediaType.APPLICATION_JSON).content(brokenJson)).andExpect(status().is4xxClientError())
				.andExpect(content().string(containsString("")));
	}

}
