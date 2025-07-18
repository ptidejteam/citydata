package ca.concordia.encs.citydata.core.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import ca.concordia.encs.citydata.core.utils.LoginRequest;
import ca.concordia.encs.citydata.services.TokenService;

/*
 * JWT Authentication Implementation
 * Author: Sikandar Ejaz 
 * Date: 18-07-2025
 */

@RestController
public class AuthController {

	private final TokenService tokenService;
	private final AuthenticationManager authenticationManager;

	public AuthController(TokenService tokenService, AuthenticationManager authenticationManager) {
		this.tokenService = tokenService;
		this.authenticationManager = authenticationManager;
	}

	@PostMapping("/token")
	public ResponseEntity<String> token(@RequestBody LoginRequest userLogin) {
		try {
			Authentication authentication = authenticationManager
					.authenticate(new UsernamePasswordAuthenticationToken(userLogin.username(), userLogin.password()));
			return ResponseEntity.ok(tokenService.generateToken(authentication));
		} catch (AuthenticationException e) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid credentials");
		}
	}
}