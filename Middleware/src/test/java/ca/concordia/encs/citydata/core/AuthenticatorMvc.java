package ca.concordia.encs.citydata.core;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import ca.concordia.encs.citydata.BaseMvc;
import ca.concordia.encs.citydata.services.TokenService;

/**
 * Added global token generation for running tests
 * @author Sikandar Ejaz 
 * @since 2025-07-18
 */

@SpringBootTest
public abstract class AuthenticatorMvc extends BaseMvc {

	@Autowired
	private TokenService tokenService;

	public String getToken() {
		List<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_USER"));
		Authentication auth = new UsernamePasswordAuthenticationToken("testuser", null, authorities);
		return tokenService.generateToken(auth);
	}
}