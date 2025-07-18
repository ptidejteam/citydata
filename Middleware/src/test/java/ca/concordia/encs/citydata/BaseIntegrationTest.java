package ca.concordia.encs.citydata;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import ca.concordia.encs.citydata.services.TokenService;

/*
 * Added global token generation for running tests
 * Author: Sikandar Ejaz 
 * Date: 18-07-2025
 */

public abstract class BaseIntegrationTest {

	@Autowired
	protected TokenService tokenService;

	protected String getToken() {
		List<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_USER"));
		Authentication auth = new UsernamePasswordAuthenticationToken("testuser", null, authorities);
		return tokenService.generateToken(auth);
	}
}