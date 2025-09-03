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

/**
* DatastoreManager test to check existing datastores.
* @author Minette Zongo
* @since 2025-08-19
*/

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

    @Test
    public void testDatastoresExist() {
        assertTrue(datastoreManager.hasStore(IDatastoreManager.DatastoreType.IN_MEMORY),
                "IN_MEMORY datastore should exist");

        assertTrue(datastoreManager.hasStore(IDatastoreManager.DatastoreType.DISK),
                "DISK datastore should exist");

        assertTrue(datastoreManager.hasStore(IDatastoreManager.DatastoreType.MONGODB),
                "MONGODB datastore should exist");

        assertNotNull(datastoreManager.getStore(IDatastoreManager.DatastoreType.IN_MEMORY),
                "IN_MEMORY datastore should be initialized");
        assertNotNull(datastoreManager.getStore(IDatastoreManager.DatastoreType.DISK),
                "DISK datastore should be initialized");
        assertNotNull(datastoreManager.getStore(IDatastoreManager.DatastoreType.MONGODB),
                "MONGODB datastore should be initialized");
    }   
}
