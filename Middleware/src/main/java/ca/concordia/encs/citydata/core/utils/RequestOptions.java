package ca.concordia.encs.citydata.core.utils;

import java.util.HashMap;

/**
 * This class represents HTTP request options, such as headers. It is used by
 * the AbstractProducer to fetch files via HTTP.
 *
 * @author Gabriel C. Ullmann
 * @date 2025-03-28
 */
public class RequestOptions {
	private String method;
	private String requestBody = "";
	private Boolean isReturnHeaders = false;
	private final HashMap<String, String> headers = new HashMap<String, String>();

	public String getMethod() {
		return this.method;
	}

	public void setMethod(String method) {
		this.method = method;
	}

	public String getRequestBody() {
		return this.requestBody;
	}

	public void setRequestBody(String requestBody) {
		this.requestBody = requestBody;
	}

	public Boolean isReturnHeaders() {
		return this.isReturnHeaders;
	}

	public void setIsReturnHeaders(Boolean isReturnHeaders) {
		this.isReturnHeaders = isReturnHeaders;
	}

	public HashMap<String, String> getHeaders() {
		return this.headers;
	}

	public void addToHeaders(String key, String value) {
		headers.put(key, value);
	}

}