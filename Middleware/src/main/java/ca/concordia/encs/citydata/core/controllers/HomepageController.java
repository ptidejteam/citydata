package ca.concordia.encs.citydata.core.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * This is a welcome page for CITYdata, intended for access via browser.
 * 
 * @author Gabriel C. Ullmann
 * @since 2025-08-27
 */

@RestController
public class HomepageController {

	@GetMapping("/home")
	@ResponseBody
	public String home() {
		// TODO: load HTML from a file
		return "<!DOCTYPE html>" +
				"<html lang=\"en\">" +
				"<body>" +
				"<h1>CITYdata</h1>" +
				"<p>Welcome to CITYdata! For more info on how to use our API, please visit " +
				"<a href=\"https://github.com/ptidejteam/citydata/blob/master/Middleware/README.md\" target=\"_blank\">our GitHub page</a>.<p>" +
				"</body>" +
				"</html>";
	}


}