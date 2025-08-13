package ca.concordia.encs.citydata;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import ca.concordia.encs.citydata.core.configs.AppConfig;
import ca.concordia.encs.citydata.core.contracts.IDataStore;
import ca.concordia.encs.citydata.core.contracts.IProducer;
import ca.concordia.encs.citydata.datastores.DataStoreManager;
import ca.concordia.encs.citydata.datastores.InMemoryDataStore;
import ca.concordia.encs.citydata.producers.EnergyConsumptionProducer;
import ca.concordia.encs.citydata.operations.StringFilterOperation;
import ca.concordia.encs.citydata.PayloadFactory;
import ca.concordia.encs.citydata.runners.SingleStepRunner;

/***
 * This test validates the energy consumption data filtering through the API endpoint and also directly through 
 * the producer component.
 * 
 * @author Minette Zongo M.
 * @date 2025-04-29
 */


@SpringBootTest(classes = AppConfig.class)
@AutoConfigureMockMvc
@ComponentScan(basePackages = "ca.concordia.encs.citydata.core")
public class EnergyConsumptionWithFilterTest {
    @Autowired
    private MockMvc mockMvc;

    private EnergyConsumptionProducer energyConsumptionProducer;

    @BeforeEach
    void setUp() {
        energyConsumptionProducer = new EnergyConsumptionProducer();
    }
    
    @Test
    public void testEnergyConsumptionWithTimeFilter() throws Exception {
    	
        String jsonPayload = PayloadFactory.getExampleQuery("energyConsumptionWithFilter");

        MvcResult mvcResult = mockMvc.perform(post("/apply/sync")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonPayload))
                .andExpect(status().isOk())
                // changed the "results" to "runnerId". The endpoint /apply/sync does not return the runnerId in response, it only sets.
                .andExpect(content().string(containsString("consumptionKwh")))
                .andReturn();
        
        String responseContent = mvcResult.getResponse().getContentAsString();
        System.out.println("API Response: " + responseContent);  
        
        // Prepare the Producer
        energyConsumptionProducer = new EnergyConsumptionProducer();
        energyConsumptionProducer.setCity("montreal");
        StringFilterOperation filterOperation = new StringFilterOperation();
        //filterOperation.setFilterBy("09:45:00");
        energyConsumptionProducer.setStartDatetime("2021-09-01 00:00:00");
        energyConsumptionProducer.setEndDatetime("2021-09-01 23:59:00");
        energyConsumptionProducer.setClientId(1);
        energyConsumptionProducer.setOperation(filterOperation);

        // Setup and run the runner
        SingleStepRunner runner = new SingleStepRunner(energyConsumptionProducer);

        // Generate a UUID and store it in runner metadata
        UUID runnerId = UUID.randomUUID();
        runner.setMetadata("id", runnerId.toString());
        
     // Get the data store and manually store the producer BEFORE running
        DataStoreManager manager = DataStoreManager.getInstance();
        IDataStore<Object> store = (IDataStore<Object>) manager.getStore("InMemory");
        store.set(runnerId, energyConsumptionProducer);
        
        // Run the runner in a thread
        Thread runnerThread = new Thread(() -> {
            try {
                System.out.println("Starting runner thread");
                runner.runSteps();
                System.out.println("Runner steps completed");
            } catch (Exception e) {
                System.err.println("Runner thread error: " + e.getMessage());
                e.printStackTrace();
            }
        });
        runnerThread.start();
        runnerThread.join();  // Wait for execution to finish
        
        // Add a brief delay to ensure result is persisted
        Thread.sleep(500);
        
        // Now fetch the result using the same runnerId
        IProducer<?> retrievedProducer = (IProducer<?>) store.get(runnerId);
        
        assertNotNull(retrievedProducer, "Producer should not be null");
        
        ArrayList<?> result = retrievedProducer.getResult();
        
        assertNotNull(result, "Result should not be null");
        assertThat(result).isNotEmpty();
        
        for (Object item : result) {
            String itemString = item.toString();
            assertTrue(itemString.contains("timestamp"),
                    "Filtered item should contain 'timestamp': " + itemString);
        }
        
        System.out.println("Test completed successfully");
    }
       
}