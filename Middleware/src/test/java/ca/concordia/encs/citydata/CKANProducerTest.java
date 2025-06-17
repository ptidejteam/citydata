package ca.concordia.encs.citydata;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import ca.concordia.encs.citydata.core.configs.AppConfig;

/* CKANProducer tests
 * Author: Gabriel C. Ullmann
 * Date: 2025-02-12
 */

@SpringBootTest(classes = AppConfig.class)
@AutoConfigureMockMvc
@ComponentScan(basePackages = "ca.concordia.encs.citydata.core")
public class CKANProducerTest {

	@Autowired
	private MockMvc mockMvc;

	// FETCHING METADATA
	@Test
	void testListDatasets() throws Exception {
		String jsonPayload = PayloadFactory.getExampleQuery("ckanMetadataProducerListDatasets");
		mockMvc.perform(post("/apply/sync").contentType(MediaType.APPLICATION_JSON).content(jsonPayload))
				.andExpect(status().isOk()).andExpect(content().string(containsString("montreal-buildings")));
	}

	@Test
	void testFetchDatasetMetadata() throws Exception {
		String jsonPayload = PayloadFactory.getExampleQuery("ckanMetadataProducerDataset");
		mockMvc.perform(post("/apply/sync").contentType(MediaType.APPLICATION_JSON).content(jsonPayload))
				.andExpect(status().isOk())
				.andExpect(content().string(containsString("a948a9ed-9e79-46eb-9cf2-a1a3e56ac9b0")));
	}

	@Test
	void testFetchResourceMetadata() throws Exception {
		String jsonPayload = PayloadFactory.getExampleQuery("ckanMetadataProducerResource");
		mockMvc.perform(post("/apply/sync").contentType(MediaType.APPLICATION_JSON).content(jsonPayload))
				.andExpect(status().isOk())
				.andExpect(content().string(containsString("a948a9ed-9e79-46eb-9cf2-a1a3e56ac9b0")));
	}

	@Test
	void testFetchNonExistingDatasetMetadata() throws Exception {
		String jsonPayload = PayloadFactory.getExampleQuery("ckanMetadataProducerDataset").replace("montreal-buildings",
				"bogus-dataset");
		mockMvc.perform(post("/apply/sync").contentType(MediaType.APPLICATION_JSON).content(jsonPayload))
				.andExpect(status().isOk()).andExpect(content().string(containsString("Not found")));
	}

	// FETCHING DATA
	@Test
	void testFetchResource() throws Exception {
		String jsonPayload = PayloadFactory.getExampleQuery("ckanProducer");
		mockMvc.perform(post("/apply/sync").contentType(MediaType.APPLICATION_JSON).content(jsonPayload))
				.andExpect(status().isOk()).andExpect(content().string(containsString("FeatureCollection")));
	}

	@Test
	void testFetchResourceWithOperation() throws Exception {
		String runnerId = "";
		String jsonPayload = PayloadFactory.getExampleQuery("ckanProducerWithReplace");

		// do async request
		MvcResult asyncRequestResult = mockMvc
				.perform(post("/apply/async").contentType(MediaType.APPLICATION_JSON).content(jsonPayload))
				.andExpect(status().isOk()).andReturn();

		// Regular expression for extracting the runner ID
		String text = asyncRequestResult.getResponse().getContentAsString();
		String uuidRegex = "\\b[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}\\b";
		Pattern pattern = Pattern.compile(uuidRegex);
		Matcher matcher = pattern.matcher(text);

		while (matcher.find()) {
			runnerId = matcher.group();
			break;
		}

		mockMvc.perform(get("/apply/async/" + runnerId)).andExpect(status().isOk())
				.andExpect(content().string(containsString("MiddlewareCollection")));
	}

	@Test
	void testFetchNonExistingResource() throws Exception {
		String jsonPayload = PayloadFactory.getExampleQuery("ckanProducer").replace("c67", "123");
		mockMvc.perform(post("/apply/sync").contentType(MediaType.APPLICATION_JSON).content(jsonPayload))
				.andExpect(status().isInternalServerError())
				.andExpect(content().string(containsString("Cannot invoke")));
	}

}
