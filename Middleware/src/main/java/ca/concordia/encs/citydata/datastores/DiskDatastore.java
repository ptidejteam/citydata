package ca.concordia.encs.citydata.datastores;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import ca.concordia.encs.citydata.core.IDataStore;
import ca.concordia.encs.citydata.core.MiddlewareEntity;

/**
 *
 * A DataStore that persists information in the disk.
 * 
 */
public class DiskDatastore extends MiddlewareEntity implements IDataStore<byte[]> {

	private static final String filePrefix = ".ptidejmw";
	private static final String baseFolderPath = "./ptidejmw-files";
	private static final DiskDatastore storeInstance = new DiskDatastore();

	// Private constructor prevents instantiation (this is a singleton)
	// Create folder to store data files (if it doesn't exist)
	private DiskDatastore() {
		this.setMetadata("role", "datastore");

		File baseFolder = new File(baseFolderPath);
		if (!baseFolder.exists()) {
			if (baseFolder.mkdirs()) {
				this.setMetadata("absoluteBasePath", baseFolder.getAbsolutePath());
				System.out.println("Datastore base folder created.");
			}
		}
	}

	// Public method to provide access to the instance
	public static DiskDatastore getInstance() {
		return storeInstance;
	}

	@Override
	public void set(String key, byte[] value) {
		String path = baseFolderPath + "/" + key + filePrefix;
		try (FileOutputStream fos = new FileOutputStream(path)) {
			fos.write(value);
			System.out.println("Data saved successfully!");
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	@Override
	public byte[] get(String key) {
		try {
			Path filePath = Path.of(baseFolderPath + "/" + key + filePrefix);
			return Files.readAllBytes(filePath);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public void delete(String key) {
		Path filePath = Path.of(baseFolderPath + "/" + key + filePrefix);
		try {
			Files.delete(filePath);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
