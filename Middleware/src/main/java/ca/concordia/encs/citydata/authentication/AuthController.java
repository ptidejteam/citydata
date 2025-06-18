package ca.concordia.encs.citydata.authentication;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AuthController {

	@Autowired
	private AuthenticationManager authenticationManager;
	@Autowired
	private JwtUtil jwtUtil;

	// Authenticates user and returns JWT token
	@PostMapping("/authenticate")
	public ResponseEntity<?> createToken(@RequestBody AuthRequest authRequest) {
		authenticationManager.authenticate(
				new UsernamePasswordAuthenticationToken(authRequest.getUsername(), authRequest.getPassword()));

		String jwt = jwtUtil.generateToken(authRequest.getUsername());
		return ResponseEntity.ok(new AuthResponse(jwt));
	}

}
