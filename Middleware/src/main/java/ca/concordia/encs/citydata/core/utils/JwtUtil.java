package ca.concordia.encs.citydata.core.utils;

import java.util.Date;
import java.util.List;

import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

@Component
public class JwtUtil {

	// Use a secure key in production (e.g., from environment variables)
	private String secret = "mySecretKey";
	private int expiryTime = 3600000; // 1 hour in milliseconds

	public String generateToken(String username, List<String> roles) {
		return Jwts.builder().setSubject(username).claim("roles", roles).setIssuedAt(new Date())
				.setExpiration(new Date(System.currentTimeMillis() + expiryTime))
				.signWith(SignatureAlgorithm.HS256, secret).compact();
	}

	public String extractUsername(String token) {
		return getClaims(token).getSubject();
	}

	public List<String> extractRoles(String token) {
		return getClaims(token).get("roles", List.class);
	}

	public boolean validateToken(String token) {
		try {
			getClaims(token); // Parsing validates signature and expiry
			return true;
		} catch (Exception e) {
			return false; // Invalid or expired token
		}
	}

	private Claims getClaims(String token) {
		return Jwts.parser().setSigningKey(secret).parseClaimsJws(token).getBody();
	}
}
