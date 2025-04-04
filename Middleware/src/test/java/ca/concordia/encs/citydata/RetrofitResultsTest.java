package ca.concordia.encs.citydata;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import ca.concordia.encs.citydata.core.AppConfig;

@SpringBootTest(classes = AppConfig.class)
@AutoConfigureMockMvc
@ComponentScan(basePackages = "ca.concordia.encs.citydata.core")
public class RetrofitResultsTest {

	private static String retrofitResultsProducer;
	private static String retrofitResultsProducerReadPath;

	@Autowired
	private MockMvc mockMvc;

	@BeforeAll
	public static void setUp() throws Exception {
		retrofitResultsProducer = PayloadFactory.getExampleQuery("retrofitResultsProducer");
		retrofitResultsProducerReadPath = PayloadFactory.getExampleQuery("retrofitResultsProducerReadPath");
	}

	@Test
	public void testMultipleBuildings() throws Exception {
		mockMvc.perform(post("/apply/sync").contentType(MediaType.APPLICATION_JSON).content(retrofitResultsProducer))
				.andExpect(status().isOk()).andExpect(content().string(containsString("results")));
	}

	@Test
	public void testMultipleBuildingsAndReadPath() throws Exception {
		mockMvc.perform(
				post("/apply/sync").contentType(MediaType.APPLICATION_JSON).content(retrofitResultsProducerReadPath))
				.andExpect(status().isOk()).andExpect(content().string(containsString("B2010_opaque_walls")));
	}

	@Test
	public void testNoBuildings() throws Exception {
		JsonObject jsonPayloadObject = JsonParser.parseString(retrofitResultsProducer).getAsJsonObject();
		jsonPayloadObject.get("withParams").getAsJsonArray().get(0).getAsJsonObject().add("value", new JsonArray());

		mockMvc.perform(
				post("/apply/sync").contentType(MediaType.APPLICATION_JSON).content(jsonPayloadObject.toString()))
				.andExpect(status().isOk()).andExpect(content().string(containsString("No buildingIds informed")));
	}

	@Test
	public void testBrokenInput() throws Exception {
		JsonObject jsonPayloadObject = JsonParser.parseString(retrofitResultsProducer).getAsJsonObject();
		jsonPayloadObject.get("withParams").getAsJsonArray().get(0).getAsJsonObject().addProperty("value", "");

		mockMvc.perform(
				post("/apply/sync").contentType(MediaType.APPLICATION_JSON).content(jsonPayloadObject.toString()))
				.andExpect(status().isInternalServerError())
				.andExpect(content().string(containsString("Not a JSON Array")));
	}

}
