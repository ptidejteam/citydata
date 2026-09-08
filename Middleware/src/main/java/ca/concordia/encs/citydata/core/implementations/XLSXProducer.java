package ca.concordia.encs.citydata.core.implementations;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

import com.google.gson.JsonObject;

import ca.concordia.encs.citydata.core.contracts.IProducer;
import ca.concordia.encs.citydata.core.exceptions.MiddlewareException;
import ca.concordia.encs.citydata.core.utils.RequestOptions;


/**
 * This producer can load XLSX from a file or remotely via an HTTP request.
 *
 * @author Vinicius Mioto
 * @since 2026-09-01
 * This base producer was refactored to follow AbstractProducer's new logic.
 */

public non-sealed class XLSXProducer extends AbstractProducer<JsonObject> implements IProducer<JsonObject> {

	private int sheetIndex = 0;
	private String sheetName = null;
	private boolean hasHeader = true;

	public XLSXProducer(final String filePath, final RequestOptions fileOptions) {
		super(filePath, fileOptions);
	}

	public XLSXProducer(final String filePath) {
		super(filePath);
	}

	public XLSXProducer() {
		super();
	}

	public void setSheetIndex(int sheetIndex) {
		this.sheetIndex = sheetIndex;
	}

	public void setSheetName(String sheetName) {
		this.sheetName = sheetName;
	}

	public void setHasHeader(boolean hasHeader) {
		this.hasHeader = hasHeader;
	}

	@Override
	public void fetch() {
		beforeFetch();
		ArrayList<JsonObject> records = new ArrayList<>();

		try (InputStream inputStream = obtainInputStream();
				Workbook workbook = WorkbookFactory.create(inputStream)) {

			Sheet sheet = (sheetName != null && !sheetName.isBlank())
					? workbook.getSheet(sheetName)
					: workbook.getSheetAt(sheetIndex);

			if (sheet == null) {
				throw new MiddlewareException.DatasetNotFound("Sheet not found in XLSX workbook");
			}

			DataFormatter dataFormatter = new DataFormatter();
			List<String> headers = new ArrayList<>();
			boolean isFirstRow = true;

			for (Row row : sheet) {
				if (row == null) {
					continue;
				}

				List<String> cellValues = new ArrayList<>();
				for (int colIndex = 0; colIndex < row.getLastCellNum(); colIndex++) {
					Cell cell = row.getCell(colIndex);
					cellValues.add(cell == null ? "" : dataFormatter.formatCellValue(cell).trim());
				}

				// Skip completely empty rows
				boolean allEmpty = cellValues.stream().allMatch(String::isEmpty);
				if (allEmpty) {
					continue;
				}

				if (isFirstRow && hasHeader) {
					headers = cellValues;
					isFirstRow = false;
					continue;
				}

				records.add(parseRow(cellValues, headers));
				isFirstRow = false;
			}

		} catch (Exception e) {
			throw new MiddlewareException.DatasetNotFound("Error processing XLSX data: " + e.getMessage());
		}

		this.setResult(records);
		this.applyOperation();
	}

	// For authorization checks - if the user has the right to access a specific producer. Implemented within the producers
	protected void beforeFetch() {

	}

	protected InputStream obtainInputStream() {
		return this.fetchStream();
	}

	// Default row parser mapping header names or column index to cell values
	protected JsonObject parseRow(List<String> cellValues, List<String> headers) {
		JsonObject record = new JsonObject();
		if (!headers.isEmpty()) {
			for (int i = 0; i < cellValues.size(); i++) {
				String header = (i < headers.size() && !headers.get(i).isEmpty())
						? headers.get(i)
						: "column_" + i;
				record.addProperty(header, cellValues.get(i));
			}
		} else {
			for (int i = 0; i < cellValues.size(); i++) {
				record.addProperty("column_" + i, cellValues.get(i));
			}
		}
		return record;
	}
}