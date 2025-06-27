package ca.concordia.encs.citydata.core;

import ca.concordia.encs.citydata.core.configs.AppConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Discovery routes test
 *
 * @author Sikandar Ejaz
 * @since 2025-06-18
 */
@SpringBootTest(classes = AppConfig.class)
@AutoConfigureMockMvc
@ComponentScan(basePackages = "ca.concordia.encs.citydata.core")
public class DiscoveryRoutesTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void testRoutesList() throws Exception {
        mockMvc.perform(get("/routes/list")).andExpect(status().isOk())
                .andExpect(content().string(containsString("Method: [")));

    }

    @Test
    public void testOperationsList() throws Exception {
        mockMvc.perform(get("/operations/list")).andExpect(status().isOk())
                .andExpect(content().string(containsString("ca.concordia.encs.citydata")));
    }

    @Test
    public void testProducersList() throws Exception {
        mockMvc.perform(get("/producers/list")).andExpect(status().isOk())
                .andExpect(content().string(containsString("ca.concordia.encs.citydata")));
    }

}
