package ca.concordia.encs.citydata.core.controllers;

import java.io.File;
import java.lang.reflect.Method;
import java.nio.file.Paths;
import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import ca.concordia.encs.citydata.core.utils.Constants;
import ca.concordia.encs.citydata.core.utils.StringUtils;

/**
 * This class is to print all available producers and their characteristics
 * 
 * @author Sikandar Ejaz
 * @since 2025-06-02
 */
@RestController
@RequestMapping("/producers")
public class ListProducerController {

	@GetMapping("/list")
	public String listProducers() {
		final JsonArray producerDetailsList = new JsonArray();
		// Get the path to the package
		final String projectRootPath = Paths.get("").toAbsolutePath() + "/";
		final String packagePath = projectRootPath + Constants.PRODUCER_ROOT_PACKAGE;

		try {
			// Scan for class files in the package directory
			final String fileExtension = ".java";
			final File[] files = new File(packagePath).listFiles((dir, name) -> name.endsWith(fileExtension));

			if (files != null) {
				for (File file : files) {
					// Remove .class extension
					final String className = file.getName().replace(fileExtension, "");

					// Load the class using reflection
					final Class<?> clazz = Class.forName("ca.concordia.encs.citydata.producers." + className);

					// Map to hold operation details
					final JsonObject operationDetails = new JsonObject();

					// Set class name
					operationDetails.addProperty("name", clazz.getName());

					// List setter methods, which correspond to user-accessible params
					final Method[] methods = clazz.getMethods();
					final List<String> paramList = StringUtils.getParamDescriptions(methods);
					operationDetails.addProperty("params", String.join(", ", paramList));
					producerDetailsList.add(operationDetails);
				}
			} else {
				final JsonObject errorObject = new JsonObject();
				errorObject.addProperty("error", "No files found in " + packagePath);
				producerDetailsList.add(errorObject);
			}

		} catch (ClassNotFoundException e) {
			final JsonObject errorObject = new JsonObject();
			errorObject.addProperty("error", e.getMessage());
			producerDetailsList.add(errorObject);
		}

		return producerDetailsList.toString();

	}
}