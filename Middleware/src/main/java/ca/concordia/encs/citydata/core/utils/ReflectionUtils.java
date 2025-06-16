package ca.concordia.encs.citydata.core.utils;

import java.lang.reflect.Method;

import ca.concordia.encs.citydata.core.exceptions.MiddlewareException.NoSuitableSetterException;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * This class contains Reflection functions used throughout the code to
 * instantiate classes, methods and fields dynamically.
 * 
 * @author: Rushin Makwana
 * @date: 2025-02-01
 */
public abstract class ReflectionUtils {

	public static JsonElement getRequiredField(JsonObject jsonObject, String fieldName) {
		if (!jsonObject.has(fieldName)) {
			throw new IllegalArgumentException("Error: Missing '" + fieldName + "' field");
		}
		return jsonObject.get(fieldName);
	}

	public static Object instantiateClass(String className) throws Exception {
		Class<?> clazz = Class.forName(className);
		return clazz.getDeclaredConstructor().newInstance();
	}

	public static void setParameters(Object instance, JsonArray params) throws Exception {
		Class<?> clazz = instance.getClass();
		for (JsonElement paramElement : params) {
			JsonObject paramObject = paramElement.getAsJsonObject();
			String paramName = paramObject.get("name").getAsString();
			JsonElement paramValue = paramObject.get("value");
			Method setter = findSetterMethod(clazz, paramName, paramValue);
			setter.invoke(instance, convertValue(setter.getParameterTypes()[0], paramValue));
		}
	}

	public static Method findSetterMethod(Class<?> clazz, String paramName, JsonElement paramValue)
			throws NoSuitableSetterException {
		String methodName = "set" + StringUtils.capitalize(paramName);
		for (Method method : clazz.getMethods()) {
			if (method.getName().equals(methodName) && method.getParameterCount() == 1) {
				return method;
			}
		}
		throw new  NoSuitableSetterException("No suitable setter found for " + paramName);
	}

	public static Object convertValue(Class<?> targetType, JsonElement value) {
		if (targetType == int.class || targetType == Integer.class) {
			return value.getAsInt();
		} else if (targetType == boolean.class || targetType == Boolean.class) {
			return value.getAsBoolean();
		} else if (targetType == double.class || targetType == Double.class) {
			return value.getAsDouble();
		} else if (targetType == JsonObject.class) {
			return value.getAsJsonObject();
		} else if (targetType == JsonArray.class) {
			return value.getAsJsonArray();
		}
		return value.getAsString();
	}

}