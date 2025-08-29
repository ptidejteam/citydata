package ca.concordia.encs.citydata.core;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import ca.concordia.encs.citydata.PayloadFactory;
import ca.concordia.encs.citydata.TestTokenGenerator;

import ca.concordia.encs.citydata.core.configs.AppConfig;
import ca.concordia.encs.citydata.datastores.DatastoreManager;
import ca.concordia.encs.citydata.core.contracts.IDatastoreManager;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.*;

@SpringBootTest(classes = AppConfig.class)
@AutoConfigureMockMvc
@ComponentScan(basePackages = "ca.concordia.encs.citydata.core")
public class DatastoreManagerTest extends TestTokenGenerator {
	
	@Autowired
	private MockMvc mockMvc;

    private DatastoreManager datastoreManager;

    @BeforeEach
    void setUp() {
        datastoreManager = DatastoreManager.getInstance();
    }

    /**
     * Test that all required datastores exist and are registered
     */
    @Test
    public void testDatastoresExist() {
        // Test IN_MEMORY datastore exists
        assertTrue(datastoreManager.hasStore(IDatastoreManager.DatastoreType.IN_MEMORY),
                "IN_MEMORY datastore should exist");

        // Test DISK datastore exists
        assertTrue(datastoreManager.hasStore(IDatastoreManager.DatastoreType.DISK),
                "DISK datastore should exist");

        // Test MONGODB datastore exists
        assertTrue(datastoreManager.hasStore(IDatastoreManager.DatastoreType.MONGODB),
                "MONGODB datastore should exist");

        // Verify all datastores are properly initialized
        assertNotNull(datastoreManager.getStore(IDatastoreManager.DatastoreType.IN_MEMORY),
                "IN_MEMORY datastore should be initialized");
        assertNotNull(datastoreManager.getStore(IDatastoreManager.DatastoreType.DISK),
                "DISK datastore should be initialized");
        assertNotNull(datastoreManager.getStore(IDatastoreManager.DatastoreType.MONGODB),
                "MONGODB datastore should be initialized");
    }

    /**
     * Test datastore management via API endpoint using example query
     * This tests if datastores are manageable through the API
     */
    @Test
    public void testDatastoreManageableViaAPI() throws Exception {
        // Use getExampleQuery to load a specific query from a JSON file
        String jsonPayload = PayloadFactory.getExampleQuery("stringProducerRandom");

        // Execute the request and verify response
        MvcResult syncResult = mockMvc
                .perform(post("/apply/sync")
                        .header("Authorization", "Bearer " + getToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPayload))
                .andExpect(status().isOk())
                .andReturn();

        // Get the response content
        String resultJson = syncResult.getResponse().getContentAsString();
        
        // Verify the response is not empty and doesn't contain error messages
        assertNotNull(resultJson, "Response should not be null");
        assertFalse(resultJson.isEmpty(), "Response should not be empty");
        assertFalse(resultJson.contains("An error occurred"), "Response should not contain error messages");
        assertFalse(resultJson.contains("Exception"), "Response should not contain exceptions");
        
        // Print the result for debugging (optional)
        System.out.println("API Response: " + resultJson);
    }

    /**
     * Test with basic query to ensure datastore interaction works
     */
    @Test
    public void testBasicQueryDatastoreInteraction() throws Exception {
        // Use basic query from PayloadFactory
        String jsonPayload = PayloadFactory.getBasicQuery();

        MvcResult syncResult = mockMvc
                .perform(post("/apply/sync")
                        .header("Authorization", "Bearer " + getToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPayload))
                .andExpect(status().isOk())
                .andReturn();

        String resultJson = syncResult.getResponse().getContentAsString();
        
        // Basic validation of response
        assertNotNull(resultJson, "Response should not be null");
        assertFalse(resultJson.isEmpty(), "Response should not be empty");
        
        // The response should be the result from the producer stored in InMemoryDataStore
        System.out.println("Basic Query Response: " + resultJson);
    }

    /**
     * Test error handling with invalid JSON
     */
    @Test
    public void testInvalidJsonHandling() throws Exception {
        String invalidJson = PayloadFactory.getInvalidJson();

        mockMvc.perform(post("/apply/sync")
                .header("Authorization", "Bearer " + getToken())
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidJson))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("not a valid JSON file")));
    }
}