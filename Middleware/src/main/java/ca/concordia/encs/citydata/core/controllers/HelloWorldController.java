package ca.concordia.encs.citydata.core.controllers;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/helloworld")
public class HelloWorldController {

	@GetMapping("")
	public String sayHello() {
		return "Hello, World!";
	}
}