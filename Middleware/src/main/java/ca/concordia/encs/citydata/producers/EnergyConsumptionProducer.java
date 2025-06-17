package ca.concordia.encs.citydata.producers;

import java.io.File;
import java.security.InvalidParameterException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import ca.concordia.encs.citydata.core.contracts.IProducer;
import ca.concordia.encs.citydata.core.exceptions.MiddlewareException;
import ca.concordia.encs.citydata.core.exceptions.MiddlewareException.DatasetNotFound;
import ca.concordia.encs.citydata.core.implementations.AbstractProducer;
import ca.concordia.encs.citydata.core.utils.StringUtils;

/**
 *
 * This Producer provide energy consumption data read from a Parquet file which
 * must be provided by the CityData instance. If no file is found, this producer
 * will return an message telling the user no data is available.
 * 
 * @author Gabriel C. Ullmann, Minette Zongo
 * @date 2025-05-28
 */
public class EnergyConsumptionProducer extends AbstractProducer<JsonArray> implements IProducer<JsonArray> {
	private String city;
	private String startDatetime;
	private String endDatetime;
	private Integer clientId;
	private String cityConsumptionDataset;

	public void setCity(String city) {
		this.city = city;
		if (this.city != null) {
			this.cityConsumptionDataset = "./src/test/data/" + this.city + "_energy_consumption.parquet";
		} else {
			throw new InvalidParameterException("Please provide a city name to the producer.");
		}
	}

	public void setStartDatetime(String startDate) {
		this.startDatetime = startDate;
	}

	public void setEndDatetime(String endDate) {
		this.endDatetime = endDate;
	}

	public void setClientId(Integer clientId) {
		this.clientId = clientId;
	}

	private String buildQuery() {
		Object[] arr = new Object[4];
		String query = "SELECT Identifiant as clientId, dateinterval as timestamp, energieactivelivree_kwh as consumptionKwh FROM '%s' "
				+ "WHERE Identifiant is not null";
		arr[0] = this.cityConsumptionDataset.replace("\\", "\\\\");

		if (this.startDatetime != null) {
			arr[1] = this.startDatetime;
			query += " AND dateinterval >= '%s'";
		}

		if (this.endDatetime != null) {
			arr[2] = this.endDatetime;
			query += " AND dateinterval <= '%s'";
		}

		if (this.clientId != null) {
			arr[3] = this.clientId;
			query += " AND Identifiant = %d";
		}

		return String.format(query, arr);
	}

	private void validateParams() {
		int MAX_QUERY_DAYS = 30;
		LocalDateTime localStartDate = StringUtils.parseDate(this.startDatetime);
		LocalDateTime localEndDate = StringUtils.parseDate(this.endDatetime);

		File f = new File(this.cityConsumptionDataset);
		if (!f.exists()) {
			throw new DatasetNotFound(this.city);
		}
		if (this.clientId == null || this.clientId < 0) {
			throw new IllegalArgumentException("Please inform a clientId from 0 to 72947.");
		}
		if (localStartDate == null || localEndDate == null) {
			throw new IllegalArgumentException("Please inform a start and end date in the YYYY-MM-DD HH:mm:ss format.");
		}
		if (!localStartDate.isBefore(localEndDate)) {
			throw new IllegalArgumentException("Please inform a start date that is before the end date.");
		}
		if (localStartDate.plusDays(MAX_QUERY_DAYS).isBefore(localEndDate)) {
			throw new IllegalArgumentException(
					"Please inform a time period no longer than " + MAX_QUERY_DAYS + " days.");
		}
	}

	@Override
	public void fetch() {

		JsonArray resultsArray = new JsonArray();
		try {
			// validate dates, throws exception if incorrect
			this.validateParams();

			// connect, build query and run it
			final Connection conn = DriverManager.getConnection("jdbc:duckdb:");
			final Statement stmt = conn.createStatement();
			final String sqlQuery = this.buildQuery();

			// get query result and transform to JSON
			String columnValue;
			JsonObject resultRow;
			final ResultSet rs = stmt.executeQuery(sqlQuery);
			final ResultSetMetaData meta = rs.getMetaData();
			final int columnCount = meta.getColumnCount();
			while (rs.next()) {
				resultRow = new JsonObject();
				for (int i = 1; i <= columnCount; i++) {
					columnValue = rs.getObject(i).toString();
					if (meta.getColumnName(i).equalsIgnoreCase("timestamp")) {
						resultRow.addProperty(meta.getColumnName(i), columnValue);
					} else if (meta.getColumnName(i).equalsIgnoreCase("clientId")) {
						resultRow.addProperty(meta.getColumnName(i), Integer.parseInt(columnValue));
					} else {
						resultRow.addProperty(meta.getColumnName(i), Double.parseDouble(columnValue));
					}

				}
				resultsArray.add(resultRow);
			}
			rs.close();
			stmt.close();
			conn.close();
		} catch (SQLException | IllegalArgumentException | MiddlewareException e) {
			JsonObject resultRow = new JsonObject();
			resultRow.addProperty("error", e.getMessage());
			resultsArray.add(resultRow);
		} finally {
			this.result.add(resultsArray);
			this.applyOperation();
		}
	}

}
