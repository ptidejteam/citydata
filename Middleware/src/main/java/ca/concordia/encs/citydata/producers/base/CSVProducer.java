package ca.concordia.encs.citydata.producers.base;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Arrays;

import ca.concordia.encs.citydata.core.exceptions.MiddlewareException;
import ca.concordia.encs.citydata.core.implementations.AbstractProducer;
import ca.concordia.encs.citydata.core.contracts.IProducer;
import ca.concordia.encs.citydata.core.utils.RequestOptions;

/**
 * This producer can load CSV from a file or remotely via an HTTP request.
 *
 * @author Gabriel C. Ullmann
 * @since 2024-12-01
 */
public class CSVProducer extends AbstractProducer<String> implements IProducer<String> {

	public CSVProducer(String filePath, RequestOptions fileOptions) {
		this.setFilePath(filePath);
		this.setFileOptions(fileOptions);
	}

	// I added the error handling to ensure I actually read my local file

	@Override
	public void fetch() {
		try (OutputStream outputStream = this.fetchFromPath();
			 OutputStreamWriter writer = new OutputStreamWriter(outputStream)) {
			writer.flush(); // Ensure all data is written to the stream
			String csvString = outputStream.toString();

			// Split CSV string by line, add lines to the list
			ArrayList<String> csvLines = new ArrayList<>();
			csvLines.addAll(Arrays.asList(csvString.split(System.lineSeparator())));
			this.setResult(csvLines);
			this.applyOperation();
		} catch (IOException e) {
			throw new MiddlewareException.DatasetNotFound("Error processing CSV data");
		}
	}

}