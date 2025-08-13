package ca.concordia.encs.citydata.core.implementations;

import java.io.*;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

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
 * @author Gabriel C. Ullmann, Rushin D. Makwana
 * @since 2025-05-27
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

	@SuppressWarnings("rawtypes")
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
		for (final IRunner runner : this.runners) {
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
	private void doHTTPRequest(OutputStream outputStream) throws Exception {
		URI endpointURI = new URI(this.filePath);
		HttpRequest.Builder requestBuilder = HttpRequest.newBuilder().uri(endpointURI);
		/*
		 *  TODO: we should support idempotent HTTP methods only to avoid unexpected side effects
		 *  (e.g. a producer changing data in the API)
		 *  for now, I kept support to PUT and POST because they are needed for Hub API auth
		 */
		switch (this.fileOptions.getMethod()) {
			case "HEAD":
				break;
			case "GET":
				requestBuilder.GET();
				break;
			case "POST":
				requestBuilder.POST(BodyPublishers.ofString(this.fileOptions.getRequestBody()));
				break;
			case "PUT":
				requestBuilder.PUT(BodyPublishers.ofString(this.fileOptions.getRequestBody()));
				break;
			default:
				throw new IllegalArgumentException("Unsupported method: " + this.fileOptions.getMethod());
		}

		if (!this.fileOptions.getHeaders().isEmpty()) {
			this.fileOptions.getHeaders().forEach(requestBuilder::header);
		}

		try (HttpClient client = HttpClient.newHttpClient()) {
			HttpResponse<InputStream> response = client.send(
					requestBuilder.build(),
					HttpResponse.BodyHandlers.ofInputStream()
			);

			if (this.fileOptions.isReturnHeaders()) {
				try (OutputStreamWriter writer = new OutputStreamWriter(outputStream)) {
					new Gson().toJson(response.headers().map(), writer);
				}
			} else {
				response.body().transferTo(outputStream);
			}
		}
	}

	/**
	 * Fetch file from filesystem
	 */
	private void readFile(OutputStream outputStream) throws IOException {
		Path path = Paths.get(this.filePath);
		Files.copy(path, outputStream);
	}

	protected OutputStream fetchFromPath() {
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		try {
			// If the file path is a URL and there are RequestOptions
			if (this.filePath != null && this.filePath.contains("://") && this.fileOptions != null) {
				doHTTPRequest(outputStream);
			} else {
				// Fetch from the filesystem
				readFile(outputStream);
			}
		} catch (FileNotFoundException e) {
			throw new RuntimeException("File not found: " + this.filePath, e);
		} catch (IOException e) {
			throw new RuntimeException("Cannot read file: " + this.filePath + ". " +
					"The file may be corrupted or inaccessible to CITYdata right now.");
		} catch (Exception e) {
			throw new RuntimeException("An error occurred while fetching the data: " + e.getMessage());
		}
		return outputStream;
	}

	@Override
	public String toString() {
		final JsonArray jsonArray = new JsonArray();
		if (!this.result.isEmpty() && this.result.getFirst() instanceof JsonElement) {
			for (E element : this.result) {
				jsonArray.add((JsonElement) element);
			}
		} else {
			final JsonObject result = new JsonObject();
			result.addProperty("result", this.result.toString());
			jsonArray.add(result);
		}
		return jsonArray.toString();
	}

}