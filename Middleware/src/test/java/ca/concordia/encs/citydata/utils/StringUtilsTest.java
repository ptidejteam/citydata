package ca.concordia.encs.citydata.utils;

import static org.junit.jupiter.api.Assertions.*;

import java.lang.reflect.Method;
import java.util.List;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.Path;
import java.io.IOException;
import ca.concordia.encs.citydata.core.utils.StringUtils;

/**
 * This class tests various utility methods for string transformations and operations,
 * as well as methods for retrieving environment variables from system properties or a JSON file.
 * Methods in this class are static and can be used without instantiating the class.
 *
 * @author Rushin Makwana
 * @since 2025-03-26
 */

public class StringUtilsTest {

    @Test
    public void testCapitalizeWithLowerCase() {
        assertEquals("Rushin", StringUtils.capitalize("rushin"));
    }

    @Test
    public void testCapitalizeWithEmptyString() {
        assertEquals("", StringUtils.capitalize(""));
    }

    @Test
    public void testGetParamDescriptionsWithValidMethods() {
        class TestClass {

            public void setName(String name) {
            }
            public void setAge(int age) {
            }
        }

        Method[] methods = TestClass.class.getDeclaredMethods();
        List<String> descriptions = StringUtils.getParamDescriptions(methods);
        System.out.println(descriptions);
        assertTrue(descriptions.contains("name (String)"));
        assertTrue(descriptions.contains("age (int)"));
    }

    @Test
    public void testGetParamDescriptionsExcludesInvalidMethods() {
        class TestClass {
            @SuppressWarnings("unused")
            public void setMetadata(String metadata) {}
        }

        Method[] methods = TestClass.class.getDeclaredMethods();
        List<String> descriptions = StringUtils.getParamDescriptions(methods);

        assertFalse(descriptions.contains("metadata (String)")); // setMetadata should be excluded
    }

    @Test
    public void fetchEnvVariableAndMatchWithConstant(){
        // Load the properties file
        Path propertiesFilePath = Paths.get("src/main/resources/application.properties");
        assertTrue(Files.exists(propertiesFilePath), "Properties file does not exist");

        java.util.Properties properties = new java.util.Properties();
        try (var inputStream = Files.newInputStream(propertiesFilePath)) {
            properties.load(inputStream);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // Fetch the value from the properties file
        String expectedValue = "mongodb://localhost:27017/citydata";
        String actualValue = properties.getProperty("spring.data.mongodb.uri");

        // Assert the value
        assertEquals(expectedValue, actualValue);
    }

    @Test
    public void fetchEnvVariableAndMatchWithAnotherConstant()  {
        // Load the properties file
        Path propertiesFilePath = Paths.get("src/main/resources/application.properties");
        assertTrue(Files.exists(propertiesFilePath), "Properties file does not exist");

        java.util.Properties properties = new java.util.Properties();
        try (var inputStream = Files.newInputStream(propertiesFilePath)) {
            properties.load(inputStream);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // Fetch the value from the properties file
        String expectedValue = null;
        String actualValue = properties.getProperty("HUB_MONGO_DB_URI");

        // Assert the value
        assertNull(actualValue);
    }
}