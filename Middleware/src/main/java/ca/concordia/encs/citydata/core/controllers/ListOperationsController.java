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

/***
 * This class is to print all available operations and their characteristics
 * 
 * @author Sikandar Ejaz
 * @date 2025-06-02
 */
@RestController
@RequestMapping("/operations")
public class ListOperationsController {

	@GetMapping("/list")
	public String listOperations() {
		JsonArray operationDetailsList = new JsonArray();
		// Get the path to the package
		String projectRootPath = Paths.get("").toAbsolutePath().toString() + "/";
		String packagePath = projectRootPath + Constants.OPERATION_ROOT_PACKAGE;

		try {
			// Scan for class files in the package directory
			String fileExtension = ".java";
			File[] files = new File(packagePath).listFiles((dir, name) -> name.endsWith(fileExtension));

			if (files != null) {
				for (File file : files) {
					// Remove .class extension
					String className = file.getName().replace(fileExtension, "");

					// Load the class using reflection
					Class<?> clazz = Class.forName("ca.concordia.encs.citydata.operations." + className);

					// Map to hold operation details
					JsonObject operationDetails = new JsonObject();

					// Set class name
					operationDetails.addProperty("name", clazz.getName());

					// List setter methods, which correspond to user-accessible params
					Method[] methods = clazz.getMethods();
					List<String> paramList = StringUtils.getParamDescriptions(methods);
					operationDetails.addProperty("params", String.join(", ", paramList));
					operationDetailsList.add(operationDetails);
				}
			} else {
				JsonObject errorObject = new JsonObject();
				errorObject.addProperty("error", "No files found in " + packagePath);
				operationDetailsList.add(errorObject);
			}

		} catch (ClassNotFoundException e) {
			JsonObject errorObject = new JsonObject();
			errorObject.addProperty("error", e.getMessage());
			operationDetailsList.add(errorObject);
		}

		return operationDetailsList.toString();

	}
}
