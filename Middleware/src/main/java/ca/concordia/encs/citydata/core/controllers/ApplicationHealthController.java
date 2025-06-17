package ca.concordia.encs.citydata.core.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/***
 * This is the running checkpoint of the Spring Boot Application.
 * 
 * @author Minette Zongo
 * @date 2025-04-22
 */

@RestController
@RequestMapping("/health")
public class ApplicationHealthController {

	@GetMapping("/ping")
	public ResponseEntity<String> ping() {
		Date timeObject = Calendar.getInstance().getTime();
		String timeStamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(timeObject);
		return ResponseEntity.status(HttpStatus.OK).body("CITYdata running at " + timeStamp);
	}


}