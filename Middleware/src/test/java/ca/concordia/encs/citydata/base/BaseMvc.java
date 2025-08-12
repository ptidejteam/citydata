package ca.concordia.encs.citydata.base;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Base test class for all tests requiring MockMvc.
 * Provides common setup and configuration for web layer testing.
 * 
 * @author Sikandar Ejaz
 * @since 08-12-2025
 */

@SpringBootTest
@AutoConfigureMockMvc
public abstract class BaseMvc {

	@Autowired
	protected MockMvc mockMvc;

	@BeforeEach
	public void baseSetUp() {
		performCommonSetup();
	}

	protected void performCommonSetup() {
	}

	protected void performCommonCleanup() {
	}

}
