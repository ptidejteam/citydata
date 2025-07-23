package ca.concordia.encs.citydata.core;

import ca.concordia.encs.citydata.core.configs.AppConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * App health routes test
 *
 * @author Minette Zongo
 * @since 2025-06-18
 */
@SpringBootTest(classes = AppConfig.class)
@AutoConfigureMockMvc
@ComponentScan(basePackages = "ca.concordia.encs.citydata.core")
public class AppHealthTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext webApplicationContext;

    // Test for ping route
    @Test
    public void testPingRoute() throws Exception {
        mockMvc.perform(get("/health/ping")).andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.startsWith("CITYdata running")));
    }

}
