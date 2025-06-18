package ca.concordia.encs.citydata.core.utils;

import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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
 * @date 2025-03-28
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
			if (methodName != "setMetadata" && methodName.startsWith("set") && params.length > 0) {
				String paramName = method.getName().replace("set", "");
				paramName = paramName.substring(0, 1).toLowerCase() + paramName.substring(1, paramName.length());
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

	public static boolean isValidDate(String date) throws IllegalArgumentException {
		try {
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
			LocalDateTime.parse(date, formatter);
			return true;
		} catch (DateTimeParseException | NullPointerException e) {
			return false;
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

}