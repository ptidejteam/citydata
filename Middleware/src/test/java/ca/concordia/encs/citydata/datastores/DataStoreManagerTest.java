package ca.concordia.encs.citydata.datastores;

import ca.concordia.encs.citydata.core.contracts.IDataStore;
import ca.concordia.encs.citydata.core.contracts.IProducer;
import ca.concordia.encs.citydata.datastores.DataStoreManager;
import ca.concordia.encs.citydata.producers.RandomStringProducer;
import ca.concordia.encs.citydata.runners.SequentialRunner;
import ca.concordia.encs.citydata.PayloadFactory;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class DataStoreManagerTest {

    @Test
    public void testRandomStringProducerStoredInMemoryFromFactoryQuery() throws Exception {
        // Load the existing query from PayloadFactory (no new query created)
        String jsonPayload = PayloadFactory.getExampleQuery("stringProducerRandom");
        JsonObject query = JsonParser.parseString(jsonPayload).getAsJsonObject();

        // Run SequentialRunner with that query
        SequentialRunner runner = new SequentialRunner(query);
        runner.runSteps();

        // Wait for the runner to complete
        int waited = 0;
        int timeout = 5000;
        while (!runner.isDone() && waited < timeout) {
            Thread.sleep(100);
            waited += 100;
        }

        // Fetch the result from InMemory store
        UUID runnerId = runner.getId();
        DataStoreManager manager = DataStoreManager.getInstance();
        IDataStore<IProducer<?>> store = manager.getStore("InMemory");
        IProducer<?> storedProducer = store.get(runnerId.toString());

        // Assertions
        assertNotNull(storedProducer, "No producer was stored in InMemory store.");
        assertTrue(storedProducer instanceof RandomStringProducer,
                "Expected RandomStringProducer but got " + storedProducer.getClass().getCanonicalName());
        assertFalse(storedProducer.getResult().isEmpty(), "Stored producer result is empty.");
    }
}
