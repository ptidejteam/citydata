package ca.concordia.encs.citydata.core.exceptions;

/**
 * This java class contains definitions of custom exceptions
 * @author Sikandar Ejaz, Rushin D. Makwana, Gabriel C. Ullmann
 * @date 2025-06-17
 */

public class MiddlewareException extends RuntimeException {

	public MiddlewareException(String message) {
		super(message);
	}


	public static class InvalidProducerException extends MiddlewareException {
		public InvalidProducerException(String producerName) {
			super("Producer " + producerName + " was not found. Please check whether the fully-qualified name is correct and try again.");
		}
	}

	public static class InvalidOperationException extends MiddlewareException {
		public InvalidOperationException(String operationName) {
			super("Producer '" + operationName + "' was not found. Please check whether the fully-qualified name is correct and try again.");
		}
	}

	public static class MalformedParameterException extends MiddlewareException {
		public MalformedParameterException(String parameterJsonString) {
			super("Malformed Producer or Operation parameter. Expected keys 'name' and 'value', found: " + parameterJsonString);
		}
	}

	public static class InvalidParameterException extends MiddlewareException {
		public InvalidParameterException(String parameterName) {
			super("Producer or Operation parameter '" + parameterName + "' was not found. Please make sure you input names and values correctly for every parameter.");
		}
	}

	public static class UnsupportedParameterTypeException extends MiddlewareException {
		public UnsupportedParameterTypeException(String parameterName, String parameterValue, String parameterTypeSupported) {
			super("Parameter '" + parameterName + "' does not accept value '" + parameterValue + "' as an input. Please input a value of type " + parameterTypeSupported + " instead.");
		}
	}

	public static class NoStepsToRunException extends MiddlewareException {
		public NoStepsToRunException(String message) {
			super(message);
		}
	}

	public static class DatasetNotFound extends MiddlewareException {
		public DatasetNotFound(String message) {
			super("Dataset not found: " + message);
		}
	}

	public static class DataStoreWritingFailureException extends MiddlewareException {
		public DataStoreWritingFailureException(String message) {
			super("Data store writing failure: " + message);
		}
	}

	public static class DataStoreFailureReadingException extends MiddlewareException {
		public DataStoreFailureReadingException(String message) {
			super("Data store reading failure: " + message);
		}
	}

	public static class DataStoreDeleteFailureException extends MiddlewareException {
		public DataStoreDeleteFailureException(String message) {
			super("Data store deletion failure: " + message);
		}
	}

	public static class ThreadInterruptedException extends MiddlewareException {
		public ThreadInterruptedException(String message) {
			super(message);
		}
	}

}