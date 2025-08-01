package ca.concordia.encs.citydata.core.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import ca.concordia.encs.citydata.core.configs.SecurityConfig;
import ca.concordia.encs.citydata.core.utils.LoginRequest;
import ca.concordia.encs.citydata.services.TokenService;

/**
 * JWT Authentication Implementation
 * Author: Sikandar Ejaz 
 * Date: 18-07-2025
 */

@RestController
public class AuthController {

	private final TokenService tokenService;
	private final AuthenticationManager authenticationManager;
	private final PasswordEncoder passwordEncoder;

	public AuthController(TokenService tokenService, AuthenticationManager authenticationManager,
			PasswordEncoder passwordEncoder, SecurityConfig securityConfig) {
		this.tokenService = tokenService;
		this.authenticationManager = authenticationManager;
		this.passwordEncoder = passwordEncoder;
	}

	@PostMapping("/authenticate")
	public ResponseEntity<String> token(@RequestBody LoginRequest userLogin) {
		try {
			Authentication authentication = authenticationManager
					.authenticate(new UsernamePasswordAuthenticationToken(userLogin.username(), userLogin.password()));

			if (userLogin.username().equals(SecurityConfig.DEFAULT_USERNAME)
					&& passwordEncoder.matches(SecurityConfig.DEFAULT_PASSWORD, userLogin.password())) {
				System.out.println(
						"WARNING: you are using the default username and password. For your security, please change it in ca.concordia.encs.citydata.core.configs.SecurityConfig");
			}

			return ResponseEntity.ok(tokenService.generateToken(authentication));

		} catch (AuthenticationException e) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid credentials");
		}
	}
}