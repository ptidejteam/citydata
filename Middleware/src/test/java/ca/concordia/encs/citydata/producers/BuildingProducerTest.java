package ca.concordia.encs.citydata.producers;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;

import ca.concordia.encs.citydata.PayloadFactory;
import ca.concordia.encs.citydata.core.TestTokenGenerator;
import ca.concordia.encs.citydata.core.configs.AppConfig;

/**
 * BuildingProducer tests
 *
 * @author Gabriel C. Ullmann
 * @since 2025-06-18
 */

@SpringBootTest(classes = AppConfig.class)
@AutoConfigureMockMvc
@ComponentScan(basePackages = "ca.concordia.encs.citydata.core")
public class BuildingProducerTest extends TestTokenGenerator {

    @Autowired
    private MockMvc mockMvc;

	List<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_USER"));
	Authentication authentication = new UsernamePasswordAuthenticationToken("testuser", null, authorities);

    // FETCHING METADATA
    @Test
    void testListDatasets() throws Exception {
        String jsonPayload = PayloadFactory.getExampleQuery("buildingProducer");
		mockMvc.perform(post("/apply/sync").header("Authorization", "Bearer " + getToken())
				.contentType(MediaType.APPLICATION_JSON).content(jsonPayload))
                .andExpect(status().isOk()).andExpect(content().string(containsString("open_spaces")));
    }

}


