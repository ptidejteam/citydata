package ca.concordia.encs.citydata.core.utils;

import java.security.KeyPair;
import java.security.KeyPairGenerator;

import org.springframework.stereotype.Component;

/*
 * JWT Authentication Implementation
 * Author: Sikandar Ejaz 
 * Date: 18-07-2025
 */

@Component
final class KeyGeneratorUtils {

	private KeyGeneratorUtils() {
	}

	static KeyPair generateRsaKey() {
		KeyPair keyPair;
		try {
			KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
			keyPairGenerator.initialize(2048);
			keyPair = keyPairGenerator.generateKeyPair();
		} catch (Exception ex) {
			throw new IllegalStateException(ex);
		}
		return keyPair;
	}

}
