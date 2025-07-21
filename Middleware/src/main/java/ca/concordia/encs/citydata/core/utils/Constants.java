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
	Path ENV_PATH = Paths.get("env.json").toAbsolutePath();

	String SOURCE_CODE_ROOT_PATH = "./src/main/java/";

	String PRODUCER_ROOT_PACKAGE = SOURCE_CODE_ROOT_PATH + "ca/concordia/encs/citydata/producers/";

	String OPERATION_ROOT_PACKAGE = SOURCE_CODE_ROOT_PATH
			+ "ca/concordia/encs/citydata/operations/";

}
