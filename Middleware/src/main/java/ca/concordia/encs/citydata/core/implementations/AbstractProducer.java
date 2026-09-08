package ca.concordia.encs.citydata.core.implementations;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import com.google.api.gax.rpc.NotFoundException;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import ca.concordia.encs.citydata.core.contracts.IOperation;
import ca.concordia.encs.citydata.core.contracts.IProducer;
import ca.concordia.encs.citydata.core.contracts.IRunner;
import ca.concordia.encs.citydata.core.exceptions.MiddlewareException.DatasetNotFound;
import ca.concordia.encs.citydata.core.utils.RequestOptions;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
/**
 *
 * This implements features common to all Producers, such as reading data from
 * files and URLs and notifying runners
 *
 * @author Gabriel C. Ullmann, Rushin D. Makwana
 * @since 2025-05-27S
 * 
 * This implementation was refactored to reduce redundancy, remove unused methods, and add Streaming support.
 * @author Minette Zongo
 * @since 2026-08-03
 *  
 */

public sealed abstract class AbstractProducer<E> extends AbstractEntity implements IProducer<E>
        permits CSVProducer, ExceptionProducer, FirebaseProducer, JPGProducer, JSONProducer, JsonArrayProducer, JsonStreamingProducer, PortfolioManagerMetadataProducer, PortfolioManagerProducer, TXTProducer, XLSXProducer {
	private String filePath;
	private RequestOptions fileOptions;
	private IOperation<E> operation;
	private final Set<IRunner> runners = new HashSet<>();
	private ArrayList<E> result = new ArrayList<>();

	public AbstractProducer(final String filePath, final RequestOptions fileOptions) {
		this.filePath = filePath;
		this.fileOptions = fileOptions;
		this.setMetadata("role", "producer");
	}

	public AbstractProducer(final String filePath) {
		this(filePath, null);
	}

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

	public void setResult(ArrayList<?> result) {
		this.result = (ArrayList<E>) result;
	}

	@Override
	public ArrayList<E> getResult() {
		return this.result;
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
		// This is implemented in subclasses. Added two primitives:
		//  1. fetchFromPath() for buffered OutputStream, what exist currently
		//  2. fetchStteam() for streaming, reads incrementally
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

	public boolean isEmpty() {
		return this.result == null || this.result.isEmpty();
	}

	/**
	 * Single point of access to the raw bytes at this.filePath, whether the
	 * source is HTTP or local file. Both fetchFromPath() (buffered)
	 * and fetchStream() (streaming) build on this — it replaces the old
	 * doHTTPRequest(OutputStream)/readFile(OutputStream) pair, which existed
	 * only to push bytes into a caller-provided stream and duplicated the
	 * same "get bytes from wherever filePath points" job under two names.
	 */
	private InputStream openInputStream() throws Exception {
		if (this.filePath != null && this.filePath.contains("://") && this.fileOptions != null) {
			return openHttpStream();
		}
		return openFileStream();
	}
	
	/**
	 * Fetch via HTTP GET/POST/PUT/HEAD, returning the response body as a
	 * live stream instead of draining it into a buffer immediately.
	 *
	 * NOTE: HttpClient is intentionally NOT wrapped in try-with-resources
	 * here. The original code closed the client only after fully draining
	 * the body inside the same block, which was safe. Now that the body
	 * stream is handed back to the caller to read later, closing the client
	 * before that read happens could terminate the connection mid-stream.
	 * The client is left to be reclaimed once the response completes.
	 */
	private InputStream openHttpStream() throws Exception {
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
 
		HttpClient client = HttpClient.newHttpClient();
		HttpResponse<InputStream> response = client.send(requestBuilder.build(),
				HttpResponse.BodyHandlers.ofInputStream());
 
		if (this.fileOptions.isReturnHeaders()) {
			String json = new Gson().toJson(response.headers().map());
			return new ByteArrayInputStream(json.getBytes(StandardCharsets.UTF_8));
		}
		return response.body();
	}
	
	/**
	 * Fetch from the filesystem, resolving this.filePath against the
	 * configured/discovered Data directory when it isn't already an
	 * absolute, existing path. This folds in what fetchData() used to do
	 * in isolation, without ever being wired into the actual read.
	 */
	private InputStream openFileStream() throws IOException {
		Path path = resolveFilePath();
		return Files.newInputStream(path);
	}
	
	/**
	 * I renamed fetchData()-which was unused previously- to locateDataDirectory() and made it private, and now resolveFilePath() actually uses it
	 * Resolves this.filePath to an actual location on disk: used as-is if
	 * it already exists (absolute path, or valid relative to the working
	 * directory), otherwise resolved against the discovered Data directory.
	 * Falls back to the raw, unresolved path so existing "file not found"
	 * error handling in fetchFromPath()/fetchStream() is unaffected.
	 */
	
//	private Path resolveFilePath() {
//		Path direct = Paths.get(this.filePath);
//		if (Files.exists(direct)) {
//			return direct.toAbsolutePath().normalize();
//		}
// 
//		Path dataDir = locateDataDirectory();
//		if (dataDir != null) {
//			Path resolved = dataDir.resolve(this.filePath).normalize();
//			if (Files.exists(resolved)) {
//				return resolved;
//			}
//		}
// 
//		return direct;
//	}
	
	private Path resolveFilePath() {
		Path direct = Paths.get(this.filePath);
		if (Files.exists(direct)) {
			return direct.toAbsolutePath().normalize();
		}
 
		String relative = this.filePath.startsWith("/") || this.filePath.startsWith("\\")
				? this.filePath.substring(1)
				: this.filePath;
		
		for (String propertyKey : java.util.List.of("data.path.route", "test.data.path.route")) {
			Path root = locateDataDirectory(propertyKey);
			if (root != null) {
				Path resolved = root.resolve(relative).normalize();
				if (Files.exists(resolved)) {
					return resolved;
				}
			}
		}
		return direct;
	}
	
	

	/**
	 * Buffered (non-streaming) fetch: reads the entire payload into memory
	 * and returns it as an OutputStream, exactly as before. Existing
	 * subclasses (CSVProducer, etc.) that call outputStream.toString() keep
	 * working unchanged.
    */
	

	protected OutputStream fetchFromPath() {
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		try (InputStream inputStream = openInputStream()) {
				inputStream.transferTo(outputStream);
		} catch (FileNotFoundException e){
			throw new RuntimeException("File not found: " + this.filePath, e);
		} catch (IOException e) {
			throw new RuntimeException("Cannot read file: " + this.filePath + ". "
					+ "The file may be corrupted or inaccessible to CITYdata right now.");
		} catch (Exception e) {
			throw new RuntimeException("An error occurred while fetching the data: " + e.getMessage());
		}
		return outputStream;
	}
	
	/**
	 * Streaming fetch: hands back the live InputStream directly, for
	 * sources too large to buffer fully. The caller is responsible for
	 * closing it (use try-with-resources) and for reading incrementally —
	 * e.g. wrapping it in a BufferedReader and processing one line at a
	 * time rather than calling readAllBytes()/toString().
	 */
	protected InputStream fetchStream() {
		if (this.filePath == null || this.filePath.isEmpty()) {
			throw new DatasetNotFound(this.filePath);
		}
		try {
			return openInputStream();
		} catch (FileNotFoundException e) {
			throw new RuntimeException("File not found: " + this.filePath, e);
		} catch (IOException e) {
			throw new RuntimeException("Cannot read file: " + this.filePath + ". "
					+ "The file may be corrupted or inaccessible to CITYdata right now.");
		} catch (Exception e) {
			throw new RuntimeException("An error occurred while fetching the data: " + e.getMessage());
		}
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
	
	/**
	 * Locates the "Data" folder used as a base directory for relative
	 * filePath values. Same logic as the original fetchData(), renamed and
	 * made private since its only purpose is to support resolveFilePath() —
	 * previously it was computed but never actually consulted anywhere.
	 */
//	private Path locateDataDirectory() {
//		java.util.Properties props = new java.util.Properties();
//		try (java.io.InputStream in = getClass().getClassLoader().getResourceAsStream("application.properties")) {
//			if (in != null) {
//				props.load(in);
//			}
//		} catch (java.io.IOException e) {
//			// ignore and use defaults
//		}
// 
//		String configured = props.getProperty("data.path.route");
//		if (configured != null && !configured.isBlank()) {
//			configured = configured.trim();
//			if (configured.startsWith("~")) {
//				configured = configured.replaceFirst("^~", System.getProperty("user.home"));
//			}
//			Path configuredPath = Paths.get(configured).toAbsolutePath().normalize();
//			if (Files.exists(configuredPath)) {
//				return configuredPath;
//			}
//	}
	
	private Path locateDataDirectory(String propertyKey) {
		java.util.Properties props = new java.util.Properties();
		try (java.io.InputStream in = getClass().getClassLoader().getResourceAsStream("application.properties")) {
			if (in != null) {
				props.load(in);
			}
		} catch (java.io.IOException e) {
			// ignore and use defaults
		}
 
		String configured = props.getProperty(propertyKey);
		if (configured == null || configured.isBlank()) {
		    return null;
		}
		configured = configured.trim();
		if (configured.startsWith("~")) {
			configured = configured.replaceFirst("^~", System.getProperty("user.home"));
		}
		Path configuredPath = Paths.get(configured).toAbsolutePath().normalize();
		if (Files.exists(configuredPath)) {
			return configuredPath;
		}


		// Trying to build a list of candidate locations where a Data folder might live (relative to the working dir and /or  its parseents)
		Path cwd = Paths.get(System.getProperty("user.dir")).toAbsolutePath().normalize();
		java.util.List<Path> candidates = new java.util.ArrayList<>();
		candidates.add(cwd.resolve("Data"));
		candidates.add(cwd.resolve("..").resolve("Data").normalize());
		candidates.add(cwd.resolve("..").resolve("..").resolve("Data").normalize());
		candidates.add(cwd.resolve("tools4cities-middleware").resolve("Data").normalize());
		if (cwd.getParent() != null) {
			candidates.add(cwd.getParent().resolve("Data").normalize());
			if (cwd.getParent().getParent() != null) {
				candidates.add(cwd.getParent().getParent().resolve("Data").normalize());
			}
		}

		for (Path p : candidates) {
			if (p != null && Files.exists(p)) {
				return p.toAbsolutePath().normalize();
			}
		}
		return null;
	}
}