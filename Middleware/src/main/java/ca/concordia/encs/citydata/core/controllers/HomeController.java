package ca.concordia.encs.citydata.core.controllers;

import java.security.Principal;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/*
 * JWT Authentication Implementation
 * Author: Sikandar Ejaz 
 * Date: 18-07-2025
 */

@RestController
public class HomeController {

	@GetMapping("")
	public String home(Principal principal) {
		return "Hello, " + principal.getName();
	}

	@PreAuthorize("hasAuthority('SCOPE_read')")
	@GetMapping("/secure")
	public String secure() {
		return "This is secured!";
	}
}
