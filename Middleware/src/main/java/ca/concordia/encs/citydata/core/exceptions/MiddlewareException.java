package ca.concordia.encs.citydata.core.exceptions;

/* This java class contains definitions of custom exceptions
 * @author: Sikandar Ejaz, Rushin D. Makwana
 * @date: 2025-08-02
 */

public class MiddlewareException extends RuntimeException {

	public MiddlewareException(String message) {
		super(message);
	}

	public static class InvalidProducerException extends MiddlewareException {
		public InvalidProducerException(String producerName) {
			super("Invalid producer: " + producerName);
		}
	}

	public static class InvalidOperationException extends MiddlewareException {
		public InvalidOperationException(String operationName) {
			super("Invalid operation: " + operationName);
		}
	}

	public static class InvalidProducerParameterException extends MiddlewareException {
		public InvalidProducerParameterException(String parameterName) {
			super("Invalid producer parameter: " + parameterName);
		}
	}

	public static class InvalidOperationParameterException extends MiddlewareException {
		public InvalidOperationParameterException(String parameterName) {
			super("Invalid operation parameter: " + parameterName);
		}
	}

	public static class UnsupportedParameterTypeException extends MiddlewareException {
		public UnsupportedParameterTypeException(String parameterType) {
			super("Unsupported parameter type: " + parameterType);
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

	public static class ReflectionOperationException extends MiddlewareException {
		public ReflectionOperationException(String message, Throwable cause) {
			super(message);
		}
	}
}