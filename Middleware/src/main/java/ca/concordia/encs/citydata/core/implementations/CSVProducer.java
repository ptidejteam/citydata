package ca.concordia.encs.citydata.core.implementations;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;

import ca.concordia.encs.citydata.core.contracts.IProducer;
import ca.concordia.encs.citydata.core.exceptions.MiddlewareException;
import ca.concordia.encs.citydata.core.utils.RequestOptions;

/**
 * This producer can load CSV from a file or remotely via an HTTP request.
 *
 * @author Gabriel C. Ullmann
 * @since 2024-12-01
 * This base producer was refactored to follow AbstractProducer's new logic.
 * @author Minette Zongo
 * @since 2026-08-03
 */

public non-sealed class CSVProducer extends AbstractProducer<String> implements IProducer<String> {

	public CSVProducer(final String filePath, final RequestOptions fileOptions) {
		super(filePath, fileOptions);
	}

	public CSVProducer(final String filePath) {
		super(filePath);
	}

	@Override
	public void fetch() {
		beforeFetch();
		try (InputStream inputStream = obtainInputStream();
				BufferedReader reader = new BufferedReader(
						new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
			ArrayList<String> csvLines  = parseLines(reader);
			this.setResult(csvLines);
			this.applyOperation();
		} catch (IOException e) {
			throw new MiddlewareException.DatasetNotFound("Error processing CSV data");
		}
				
	}
	
	// For authorization checks - if the user has the right to access a specific producer. Implemented within the producers
	protected void beforeFetch() {
		
	}
	
	protected InputStream obtainInputStream() {
		return this.fetchStream();
	}
	
	protected ArrayList<String> parseLines(BufferedReader reader) throws IOException {
		ArrayList<String> lines = new ArrayList<String>();
		String line;
		while ((line = reader.readLine()) != null) {
			lines.add(line);
		}
		return lines;
	}
}