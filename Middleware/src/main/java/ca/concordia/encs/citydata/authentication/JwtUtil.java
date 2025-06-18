package ca.concordia.encs.citydata.authentication;

import java.util.Date;

import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

@Component
public class JwtUtil {

	private final String SECRET_KEY = "secret_key"; // secret key used to sign the token
	private final long EXPIRATION_TIME = 1000 * 60 * 60; // token expiration time: 1 hour

	// Generates a token using the username as subject
	public String generateToken(String username) {
		return Jwts.builder().setSubject(username).setIssuedAt(new Date())
				.setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
				.signWith(SignatureAlgorithm.HS256, SECRET_KEY).compact();
	}

	// Extracts the username from the token
	public String extractUsername(String token) {
		return Jwts.parser().setSigningKey(SECRET_KEY).parseClaimsJws(token).getBody().getSubject();
	}

	// Validates the token expiration
	public boolean isTokenValid(String token) {
		try {
			Claims claims = Jwts.parser().setSigningKey(SECRET_KEY).parseClaimsJws(token).getBody();
			return claims.getExpiration().after(new Date());
		} catch (Exception e) {
			return false;
		}
	}

}
