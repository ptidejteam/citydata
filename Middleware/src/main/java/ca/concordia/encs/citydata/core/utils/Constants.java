package ca.concordia.encs.citydata.core.utils;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * This is the interface for the storing all constants
 * 
 * @author Rushin Makwana
 * @date 2025-02-07
 */
public interface Constants {
	public static final Path ENV_PATH = Paths.get("env.json").toAbsolutePath();

	public static final String SOURCE_CODE_ROOT_PATH = "./src/main/java/";

	public static final String PRODUCER_ROOT_PACKAGE = SOURCE_CODE_ROOT_PATH + "ca/concordia/encs/citydata/producers/";

	public static final String OPERATION_ROOT_PACKAGE = SOURCE_CODE_ROOT_PATH
			+ "ca/concordia/encs/citydata/operations/";

}
