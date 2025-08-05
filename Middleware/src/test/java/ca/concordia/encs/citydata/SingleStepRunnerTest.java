package ca.concordia.encs.citydata;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;
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



import ca.concordia.encs.citydata.core.contracts.IProducer;
import ca.concordia.encs.citydata.core.contracts.IRunner;
import ca.concordia.encs.citydata.datastores.DataStoreManager;
import ca.concordia.encs.citydata.producers.RandomNumberProducer;
import ca.concordia.encs.citydata.runners.SingleStepRunner;

class SingleStepRunnerTest {

	@Test
    void testSingleStepRunnerStoresResultInDataStoreManager() throws Exception {
        // Create a RandomNumberProducer configured for test
        RandomNumberProducer producer = new RandomNumberProducer();
        producer.setListSize(5);
        producer.setGenerationDelay(0);

        // Create the runner with the producer instance directly
        IRunner runner = new SingleStepRunner(producer);

        // Run the steps (which internally calls fetch and stores result)
        runner.runSteps();

        // Get the runner's ID as the key
        String runnerId = ((SingleStepRunner) runner).getIdAsString();

        // Access DataStoreManager singleton and get InMemory store
        DataStoreManager manager = DataStoreManager.getInstance();
        IProducer<?> storedProducer = (IProducer<?>) manager.getStore("InMemory").get(runnerId);

        // Assertions
        assertThat(storedProducer).isNotNull();
        assertThat(storedProducer).isInstanceOf(RandomNumberProducer.class);

        // Check that the stored producer contains the expected data
        RandomNumberProducer resultProducer = (RandomNumberProducer) storedProducer;
        List<Integer> resultList = resultProducer.getResult();
        System.out.println(resultList);

        assertThat(resultList).isNotNull();
        assertThat(resultList).hasSize(5);
        assertThat(resultList).allMatch(num -> num >= 0 && num < 100);
    }

}
