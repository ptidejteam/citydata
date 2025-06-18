package ca.concordia.encs.citydata.core.utils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import ca.concordia.encs.citydata.core.exceptions.MiddlewareException;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import ca.concordia.encs.citydata.core.exceptions.MiddlewareException.InvalidProducerException;
import ca.concordia.encs.citydata.core.exceptions.MiddlewareException.InvalidOperationException;
import ca.concordia.encs.citydata.core.exceptions.MiddlewareException.InvalidParameterException;
import ca.concordia.encs.citydata.core.exceptions.MiddlewareException.UnsupportedParameterTypeException;
import ca.concordia.encs.citydata.core.exceptions.MiddlewareException.MalformedParameterException;

/**
 * This class contains Reflection functions used throughout the code to
 * instantiate classes, methods and fields dynamically.
 *
 * @author Rushin Makwana
 * @date 2025-02-01
 */
public abstract class ReflectionUtils {

	public static JsonElement getRequiredField(JsonObject jsonObject, String fieldName) {
		if (!jsonObject.has(fieldName)) {
			throw new IllegalArgumentException("Error: Missing required '" + fieldName + "' field");
		}
		return jsonObject.get(fieldName);
	}

	public static Object instantiateClass(String className) throws MiddlewareException {
		try {
			Class<?> clazz = Class.forName(className);
			return clazz.getDeclaredConstructor().newInstance();
		} catch (ClassNotFoundException | NoSuchMethodException e) {
			if (className.contains("Operation") || className.contains("operation")) {
				throw new InvalidOperationException(className);
			} else if (className.contains("Producer") || className.contains("producer")) {
				throw new InvalidProducerException(className);
			} else {
				throw new MiddlewareException("Producer or Operation " + e.getClass().getSimpleName() + " was not found. Please check for typos and try again.");
			}
		} catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
			throw new MiddlewareException("CITYdata entity could not be created: " + e.getClass().getSimpleName() + ". Please contact the system administrator.");
		}
	}

	public static void setParameters(Object instance, JsonArray params) throws MiddlewareException {
		Class<?> clazz = instance.getClass();
		int i = 0;
		JsonElement paramValue = new JsonObject();
		String paramName = "";
		Method setter = null;
		try {
			for (i = 0; i < params.size(); i++) {
				paramName = params.get(i).getAsJsonObject().get("name").getAsString();
				paramValue = params.get(i).getAsJsonObject().get("value");
				setter = findSetterMethod(clazz, paramName, paramValue);
				setter.invoke(instance, convertValue(setter.getParameterTypes()[0], paramValue));
			}
		} catch (NullPointerException | IllegalStateException e) {
			throw new MalformedParameterException(params.get(i).toString());
		}  catch (Exception e) {
			if (setter != null) {
				throw new UnsupportedParameterTypeException(paramName, paramValue.toString(), setter.getParameterTypes()[0].toString());
			} else {
				throw new InvalidParameterException(paramName);
			}
		}
	}

	public static Method findSetterMethod(Class<?> clazz, String paramName)
			throws InvalidParameterException {
		String methodName = "set" + StringUtils.capitalize(paramName);
		for (Method method : clazz.getMethods()) {
			if (method.getName().equals(methodName) && method.getParameterCount() == 1) {
				return method;
			}
		}
		throw new InvalidParameterException(paramName);
	}

	public static Object convertValue(Class<?> targetType, JsonElement value) throws UnsupportedOperationException, NumberFormatException {
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