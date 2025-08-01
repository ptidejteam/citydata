package ca.concordia.encs.citydata.core.controllers;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import ca.concordia.encs.citydata.core.utils.ProducerUsageData;
import ca.concordia.encs.citydata.datastores.MongoDataStore;

/**
 * This class has all the routes for the report generation
 * 
 * @author Rushin Makwana
 * @since 2024-02-26
 */
@RestController
@RequestMapping("/report")
public class ReportController {
	@Autowired
	private MongoDataStore mongoDataStore;

	@RequestMapping(value = "/producers", method = RequestMethod.GET)
	public ResponseEntity<List<Map<String, String>>> report() {
		final List<Map<String, String>> report = new ArrayList<>();
		final List<ProducerUsageData> callInfoList = mongoDataStore.findAll();
		final Map<String, Integer> producerCountMap = new HashMap<>();
		final Map<String, Date> lastRequestDateMap = new HashMap<>();

		for (ProducerUsageData callInfo : callInfoList) {
			final String producerName = callInfo.getProducerName();
			producerCountMap.put(producerName, producerCountMap.getOrDefault(producerName, 0) + 1);
			lastRequestDateMap.put(producerName, callInfo.getTimestamp());
		}

		producerCountMap.forEach((producerName, count) -> {
			final Map<String, String> reportEntry = new HashMap<>();
			reportEntry.put("query", producerName);
			reportEntry.put("count", String.valueOf(count));
			reportEntry.put("lastRequestDate",
					new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(lastRequestDateMap.get(producerName)));
			report.add(reportEntry);
		});

		return ResponseEntity.status(HttpStatus.OK).body(report);
	}

	@RequestMapping(value = "/producers", method = RequestMethod.DELETE)
	public ResponseEntity<String> deleteAllProducers() {
		mongoDataStore.deleteAll();
		return ResponseEntity.status(HttpStatus.OK).body("All producer records have been deleted.");
	}
}