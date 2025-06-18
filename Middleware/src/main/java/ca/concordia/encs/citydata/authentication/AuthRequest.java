package ca.concordia.encs.citydata.authentication;

public class AuthRequest {

	private String username;
	private String password;

	// Default constructor
	public AuthRequest() {
	}

	// Constructor with parameters
	public AuthRequest(String username, String password) {
		this.username = username;
		this.password = password;
	}

	// Getter and Setter for username
	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	// Getter and Setter for password
	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}
}
