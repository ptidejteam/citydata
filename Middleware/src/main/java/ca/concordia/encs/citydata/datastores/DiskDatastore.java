package ca.concordia.encs.citydata.datastores;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.UUID;

import ca.concordia.encs.citydata.core.contracts.IDataStore;
import ca.concordia.encs.citydata.core.implementations.AbstractEntity;
import ca.concordia.encs.citydata.core.exceptions.MiddlewareException.*;

/**
 * A DataStore that persists information in the disk.
 *
 * @author Gabriel C. Ullmann
 * @since 2025-02-19
 */
public class DiskDatastore extends AbstractEntity implements IDataStore<byte[]> {

	private static final String filePrefix = ".citydata";
	private static final String baseFolderPath = "./citydata-files/";
	private static final DiskDatastore storeInstance = new DiskDatastore();

	// Private constructor prevents instantiation (this is a singleton)
	// Create folder to store data files (if it doesn't exist)
	private DiskDatastore() {
		this.setMetadata("role", "datastore");

		final File baseFolder = new File(baseFolderPath);
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
    public void set(UUID key, byte[] value) {
        set(key.toString(), value);
    }

	@Override
	public void set(String key, byte[] value) {
		final String path = baseFolderPath + key + filePrefix;
		try (FileOutputStream fos = new FileOutputStream(path)) {
			fos.write(value);
			System.out.println("Data saved successfully!");
		} catch (IOException e) {
			throw new DataStoreWritingFailureException("Failed to save data to disk: " + e.getMessage());
		}
	}

	@Override
    public byte[] get(UUID key) {
        return get(key.toString());
    }

	@Override
	public byte[] get(String key) {
		final Path filePath = Path.of(baseFolderPath + key + filePrefix);
		try {
			return Files.readAllBytes(filePath);
		} catch (IOException e) {
			throw new DataStoreFailureReadingException("Failed to retrieve data from disk: " + e.getMessage());
		}
	}

	@Override
	public Iterator<byte[]> getValues() {
		System.out.println("Not yet implemented.");
		return null;
	}

	@Override
    public void delete(UUID key) {
        delete(key.toString());
    }

	@Override
	public void delete(String key) {
		final Path filePath = Path.of(baseFolderPath + key + filePrefix);
		try {
			Files.delete(filePath);
		} catch (IOException e) {
			throw new DataStoreDeleteFailureException("Failed to delete data from disk: " + e.getMessage());
		}
    }

}
