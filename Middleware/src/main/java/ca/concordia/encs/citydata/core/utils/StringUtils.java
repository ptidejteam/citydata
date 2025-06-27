package ca.concordia.encs.citydata.core.utils;

import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import static ca.concordia.encs.citydata.core.utils.Constants.ENV_PATH;

/**
 * This class contains functions to perform transformations on strings.
 * 
 * @author Rushin Makwana and Gabriel C. Ullmann
 * @since 2025-03-28
 */
public abstract class StringUtils {

	public static String capitalize(String str) {
		return str == null || str.isEmpty() ? str : str.substring(0, 1).toUpperCase() + str.substring(1);
	}

	public static List<String> getParamDescriptions(Method[] methods) {
		final List<String> descriptions = new ArrayList<>();
		for (Method method : methods) {
			final Parameter[] params = method.getParameters();
			final String methodName = method.getName();
			if (isUserAccessibleSetter(methodName, params)) {
				final String paramName = extractParamNameFromSetterMethod(methodName);
				descriptions.add(paramName + " (" + params[0].getType().getSimpleName() + ")");
			}
		}
		return descriptions;
	}

	public static String getEnvVariable(String variableKey) {
		String value = System.getenv(variableKey);
		if (value == null || value.length() == 0) {
			final JsonElement envVariables = StringUtils.getEnvVariables();
			if (envVariables.getAsJsonObject() != null && envVariables.getAsJsonObject().get(variableKey) != null) {
				value = envVariables.getAsJsonObject().get(variableKey).getAsString();
			}
		}
		return value;
	}

	public static JsonElement getEnvVariables() {
		try {
			final String values = new String(Files.readAllBytes(ENV_PATH));
			return JsonParser.parseString(values);
		} catch (IOException e) {
			return new JsonObject();
		}
	}

	public static LocalDateTime parseDate(String date) throws IllegalArgumentException {
		try {
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
			return LocalDateTime.parse(date, formatter);
		} catch (DateTimeParseException | NullPointerException e) {
			return null;
		}
	}

	private static boolean isUserAccessibleSetter(String methodName, Parameter[] params){
		return methodName != null && methodName.startsWith("set") && !methodName.equals("setMetadata") && params.length > 0;
	}

	private static String extractParamNameFromSetterMethod(String setterMethodName) {
		final String setterName = setterMethodName.replace("set", "");
		return setterName.substring(0, 1).toLowerCase() + setterName.substring(1);
	}

}