package ca.concordia.encs.citydata.producers;

import ca.concordia.encs.citydata.PayloadFactory;
import ca.concordia.encs.citydata.core.configs.AppConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = AppConfig.class)
@AutoConfigureMockMvc
@ComponentScan(basePackages = "ca.concordia.encs.citydata.core")
public class BuildingProducerTest {

    @Autowired
    private MockMvc mockMvc;

    // FETCHING METADATA
    @Test
    void testListDatasets() throws Exception {
        String jsonPayload = PayloadFactory.getExampleQuery("buildingProducer");
        mockMvc.perform(post("/apply/sync").contentType(MediaType.APPLICATION_JSON).content(jsonPayload))
                .andExpect(status().isOk()).andExpect(content().string(containsString("open_spaces")));
    }

}


