package ca.concordia.encs.citydata.core.utils;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * This is the interface for the storing all constants
 * 
 * @author Rushin Makwana
 * @since 2025-02-07
 */
public interface Constants {
	// IntelliJ points out as an issue, but these should all be static
	// and final, so we prevent accidental changes
	public static final Path ENV_PATH = Paths.get("env.json").toAbsolutePath();

	static String SOURCE_CODE_ROOT_PATH = "./src/main/java/";

	static String PRODUCER_ROOT_PACKAGE = SOURCE_CODE_ROOT_PATH + "ca/concordia/encs" +
			"/citydata/producers/";

	static String OPERATION_ROOT_PACKAGE = SOURCE_CODE_ROOT_PATH
			+ "ca/concordia/encs/citydata/operations/";

}
