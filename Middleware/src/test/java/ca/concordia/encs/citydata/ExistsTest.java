package ca.concordia.encs.citydata;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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
import org.springframework.test.web.servlet.MvcResult;

import com.fasterxml.jackson.core.JsonParser;
import com.google.gson.JsonArray;

import ca.concordia.encs.citydata.core.AppConfig;

/* ExistsController tests
 * Author: Minette
 * Date: 2025-02-26
 */

@SpringBootTest(classes = AppConfig.class)
@AutoConfigureMockMvc
@ComponentScan(basePackages = "ca.concordia.encs.citydata.core")
public class ExistsTest {

    @Autowired
    private MockMvc mockMvc;
    
    // Store the runnerId as an instance variable so it can be used across test methods
    private String runnerId;

    @Test
    void testQueryExists() throws Exception {
        // Use getExampleQuery to load a specific query from a JSON file
        String jsonPayload = PayloadFactory.getExampleQuery("stringProducerStaticWithParams");
        
        // creating a producer
        MvcResult syncResult = mockMvc.perform(post("/apply/sync")
            .contentType(MediaType.APPLICATION_JSON)
            .content(jsonPayload))
            .andExpect(status().isOk())
            .andReturn();
        
        // Get the runner ID (though not used in this particular test)
        runnerId = syncResult.getResponse().getContentAsString();
        
        // Check if the query exists
        MvcResult existsResult = mockMvc.perform(post("/exists/")
            .contentType(MediaType.APPLICATION_JSON)
            .content(jsonPayload))
            .andExpect(status().isOk())
            .andReturn();
        
        String responseContent = existsResult.getResponse().getContentAsString();
      
        assertFalse(responseContent.equals("[]"), "Response should not be an empty array");
    }
    
    @Test
    void testQueryNotExists() throws Exception {
        String jsonPayload = PayloadFactory.getExampleQuery("ckanMetadataProducerListDatasets");

        MvcResult existsResult = mockMvc.perform(post("/exists/")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonPayload))
                .andExpect(status().isNotFound())
                .andReturn();
                
        String responseContent = existsResult.getResponse().getContentAsString();
        assertEquals("[]", responseContent);
    }
    
    @Test
    void testBrokenJsonQuery() throws Exception {
        String jsonPayload = PayloadFactory.getInvalidJson();
        mockMvc.perform(post("/exists/")  // Changed from get to post to match my endpoint
            .contentType(MediaType.APPLICATION_JSON)
            .content(jsonPayload))
            .andExpect(status().isInternalServerError());
    }

    @Test
    void testQueryExistsFollowedBySync() throws Exception {
        String jsonPayload = PayloadFactory.getExampleQuery("stringProducerRandom");
        
        MvcResult existsResult = mockMvc.perform(post("/exists/")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonPayload))
                .andReturn();
        
        String responseContent = existsResult.getResponse().getContentAsString();
        int status = existsResult.getResponse().getStatus();
        
        if (status == 404 || responseContent.equals("[]")) {
            MvcResult syncResult = mockMvc.perform(post("/apply/sync")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(jsonPayload))
                    .andReturn(); 

            int syncStatus = syncResult.getResponse().getStatus();
            String syncResponse = syncResult.getResponse().getContentAsString();
            
            // Log the response before failing
            System.out.println("apply/sync Status: " + syncStatus);
            System.out.println("apply/sync Response: " + syncResponse);

            if (syncStatus != 200) {
                fail("apply/sync failed with status: " + syncStatus + " and response: " + syncResponse);
            }
        } else if (status == 200) {
            assertFalse(responseContent.equals("[]"), "Response should not be an empty array");
        } else {
            fail("Unexpected status code: " + status + ". Response content: " + responseContent);
        }
    }
}