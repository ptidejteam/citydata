/**
 *
 */
package ca.concordia.encs.citydata.core.implementations;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublisher;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpRequest.Builder;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.Map.Entry;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import ca.concordia.encs.citydata.core.contracts.IOperation;
import ca.concordia.encs.citydata.core.contracts.IProducer;
import ca.concordia.encs.citydata.core.contracts.IRunner;
import ca.concordia.encs.citydata.core.exceptions.MiddlewareException.DatasetNotFound;
import ca.concordia.encs.citydata.core.utils.RequestOptions;

/**
 *
 * This implements features common to all Producers, such as reading data from
 * files and URLs and notifying runners
 *
 * @author Gabriel C. Ullmann
 * @date 2025-05-27
 */
public abstract class AbstractProducer<E> extends AbstractEntity implements IProducer<E> {

	private String filePath;
	private RequestOptions fileOptions;
	private IOperation<E> operation;
	private final Set<IRunner> runners = new HashSet<>();
	private ArrayList<E> result = new ArrayList<>();

	public String getFilePath() {
		return filePath;
	}

	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}

	public RequestOptions getFileOptions() {
		return fileOptions;
	}

	public void setFileOptions(RequestOptions fileOptions) {
		this.fileOptions = fileOptions;
	}

	public IOperation<E> getOperation() {
		return operation;
	}

	public Set<IRunner> getRunners() {
		return runners;
	}

	public void setResult(ArrayList<E> result) {
		this.result = result;
	}

	public AbstractProducer() {
		this.setMetadata("role", "producer");
	}

	@Override
	public void addObserver(final IRunner aRunner) {
		this.runners.add(aRunner);
	}

	@Override
	public void setOperation(IOperation operation) {
		this.operation = operation;
	}

	@Override
	public void fetch() {
		if (this.filePath == null || this.filePath.isEmpty()) {
			throw new DatasetNotFound(this.filePath);
		}
		System.out.println("Unimplemented method! This method must be implemented by a subclass.");
	}

	@Override
	public void notifyObservers() {
		for (final Iterator<IRunner> iterator = this.runners.iterator(); iterator.hasNext();) {
			final IRunner runner = iterator.next();
			runner.newDataAvailable(this);
		}
	}

	@Override
	public void applyOperation() {
		// if an operation exists, apply it, notify anyway after done
		if (this.operation != null) {
			this.result = this.operation.apply(this.result);
		}
		this.notifyObservers();
	}

	@Override
	public ArrayList<E> getResult() {
		return this.result;
	}

	public boolean isEmpty() {
		return this.result == null || this.result.isEmpty();
	}

	/**
	 * Fetch file via HTTP GET or POST
	 */
	protected byte[] doHTTPRequest() throws Exception {
		HttpRequest request;
		BodyPublisher requestBody;
		URI endpointURI = new URI(this.filePath);
		Builder requestBuilder = HttpRequest.newBuilder().uri(endpointURI);
		HttpClient client = HttpClient.newHttpClient();

		// TODO: we should support idempotent HTTP methods only to avoid unexpected side
		// effects (e.g. a producer changing data in the API)
		// for now, I kept support to PUT and POST because they are needed for Hub API
		// auth
		switch (this.fileOptions.getMethod()) {
			case "HEAD":
				break;
			case "GET":
				requestBuilder.GET();
				break;
			case "POST":
				requestBody = BodyPublishers.ofString(this.fileOptions.getRequestBody());
				requestBuilder.POST(requestBody);
				break;
			case "PUT":
				requestBody = BodyPublishers.ofString(this.fileOptions.getRequestBody());
				requestBuilder.PUT(requestBody);
				break;
			default:
				throw new IllegalArgumentException("Unsupported method: " + this.fileOptions.getMethod());
		}

		// add headers to builder, if any
		HashMap<String, String> listOfHeaders = this.fileOptions.getHeaders();
		if (!listOfHeaders.isEmpty()) {
			for (Entry<String, String> header : listOfHeaders.entrySet()) {
				requestBuilder.header(header.getKey(), header.getValue());
			}
		}
		request = requestBuilder.build();

		HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
		System.out.println(response.statusCode());

		if (this.fileOptions.isReturnHeaders()) {
			Gson gson = new Gson();
			return gson.toJson(response.headers().map()).getBytes();
		}
		return response.body().getBytes();
	}

	/**
	 * Fetch file from filesystem
	 */
	protected byte[] readFile() throws Exception {
		Path path = Paths.get(this.filePath);
		return Files.readAllBytes(path);
	}

	protected byte[] fetchFromPath() {
		try {
			// If the file path is a URL and there are RequestOptions
			if (this.filePath != null && this.filePath.contains("://") && this.fileOptions != null) {
				return this.doHTTPRequest();
			}

			// else, fetch from the filesystem
			return this.readFile();

		} catch (FileNotFoundException e) {
			throw new RuntimeException("File not found: " + this.filePath, e);
		} catch (IOException e) {
			throw new RuntimeException("Cannot read file: " + this.filePath + ". " +
					"The file may be corrupted or inaccessible to CITYdata right now.");
		} catch (OutOfMemoryError e) {
			throw new RuntimeException("There is not enough memory to load file: " + this.filePath);
		} catch (Exception e) {
			throw new RuntimeException("An error occurred while fetching the data: " + e.getMessage());
		}

	}

	@Override
	public String toString() {
		JsonArray jsonArray = new JsonArray();
		if (!this.result.isEmpty() && this.result.getFirst() instanceof JsonElement) {
			for (E element : this.result) {
				jsonArray.add((JsonElement) element);
			}
		} else {
			JsonObject result = new JsonObject();
			result.addProperty("result", this.result.toString());
			jsonArray.add(result);
		}
		return jsonArray.toString();
	}
}