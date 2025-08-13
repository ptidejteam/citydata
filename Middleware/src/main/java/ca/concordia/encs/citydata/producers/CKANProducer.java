package ca.concordia.encs.citydata.producers;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import ca.concordia.encs.citydata.core.exceptions.MiddlewareException;
import ca.concordia.encs.citydata.core.exceptions.MiddlewareException.DataStoreFailureReadingException;
import com.google.gson.JsonObject;

import ca.concordia.encs.citydata.core.implementations.AbstractProducer;
import ca.concordia.encs.citydata.core.implementations.AbstractRunner;
import ca.concordia.encs.citydata.core.contracts.IProducer;
import ca.concordia.encs.citydata.core.utils.RequestOptions;
import ca.concordia.encs.citydata.datastores.DiskDatastore;
import ca.concordia.encs.citydata.datastores.InMemoryDataStore;
import ca.concordia.encs.citydata.runners.SingleStepRunner;

/**
 * This producer can connect to a CKAN instance and fetch a resource.
 * @author Gabriel C. Ullmann, Rushin D. Makwana
 * @since 2025-02-12
 */
public class CKANProducer extends AbstractProducer<String> implements IProducer<String> {

	private String url;
	private String resourceId;
	private final DiskDatastore diskStore = DiskDatastore.getInstance();
	private final ArrayList<String> intermediateResult = new ArrayList<>();

	public void setUrl(String url) {
		if (url != null) {
			if (url.contains("http")) {
				this.url = url;
			} else {
				this.url = "https://" + url;
			}
		}
	}

	public void setResourceId(String resourceId) {
		this.resourceId = resourceId;
	}

	@SuppressWarnings("unchecked")
	private ArrayList<JsonObject> getMetadataObject(AbstractRunner aRunner) {
		final InMemoryDataStore memoryStore = InMemoryDataStore.getInstance();
		final String runnerId = aRunner.getMetadata("id").toString();
		final IProducer<?> storeResult = memoryStore.get(runnerId);
		if (storeResult != null) {
			return (ArrayList<JsonObject>) memoryStore.get(runnerId).getResult();
		}
		return new ArrayList<>();
	}

	private JsonObject getResourceAttributes(ArrayList<JsonObject> metadataObject) {
		final JsonObject metadataResults = !metadataObject.isEmpty() ? metadataObject.getFirst().get("result").getAsJsonObject()
				: null;

		final JsonObject attributesObject = new JsonObject();
        assert metadataResults != null;
        attributesObject.addProperty("sizeInMb", metadataResults.get("size").getAsInt() / 1000000);
		attributesObject.addProperty("mimetype", metadataResults.get("mimetype").getAsString());
		attributesObject.addProperty("url", metadataResults.get("url").getAsString());

		return attributesObject;
	}

	private boolean isFileSupported(String mimetype) {
		final List<String> supportedFormats = List.of("csv", "json", "xml", "txt", "text", "xls");
		for (String supportedFormat : supportedFormats) {
			if (mimetype.contains(supportedFormat)) {
				return true;
			}
		}
		return false;
	}

	private OutputStream fetchFromCkan() {
		try {
			// fetch resource metadata first
			final CKANMetadataProducer metadataProducer = new CKANMetadataProducer();
			metadataProducer.setUrl(this.url);
			metadataProducer.setResourceId(this.resourceId);
			final SingleStepRunner deckard = new SingleStepRunner(metadataProducer);
			final Thread runnerTask = new Thread(() -> {
                try {
                    deckard.runSteps();
                    while (!deckard.isDone()) {
                        System.out.println("Busy waiting!");
                    }
                } catch (Exception e) {
                    deckard.setAsDone();
                    System.out.println(e.getMessage());
                }

            });
			runnerTask.start();
			runnerTask.join();

			final ArrayList<JsonObject> metadataObject = getMetadataObject(deckard);
			final JsonObject resourceAttributes = getResourceAttributes(metadataObject);
			final String resourceUrl = resourceAttributes.get("url").getAsString();
			final String mimetype = resourceAttributes.get("mimetype").getAsString();
			final int size = resourceAttributes.get("sizeInMb").getAsInt();

			// if the file is supported, download it and save to disk
			if (isFileSupported(mimetype)) {
				if (size > 800) {
					intermediateResult
							.add("Sorry, files larger than 800MB are not currently supported by this producer. "
									+ "If you wish to download the resource, please open this link in your browser: "
									+ resourceAttributes.get("url") + " .");
				}
				final RequestOptions requestOptions = new RequestOptions();
				requestOptions.setMethod("GET");
				this.setFilePath(resourceUrl);
				this.setFileOptions(requestOptions);
                return this.fetchFromPath();
			} else {
				intermediateResult.add("Sorry, the " + mimetype + " type is not currently supported by this producer. "
						+ "If you wish to download the resource, please open this link in your browser: " + resourceUrl
						+ " .");
			}
		} catch (InterruptedException e) {
			final ArrayList<String> errorMessageList = new ArrayList<>();
			errorMessageList.add(e.getMessage());
			this.setResult(errorMessageList);
		}

		return new ByteArrayOutputStream();
	}

	@Override
	public void fetch() {

		if (this.resourceId != null) {
			// before attempting to fetch, check if a file with this resource ID already
			// exists in the disk
			OutputStream fileStream = null;
			try {
				fileStream = new ByteArrayOutputStream();
				fileStream.write(diskStore.get(this.resourceId));
			} catch (DataStoreFailureReadingException e) {
				// if not, fetch from CKAN and save on disk
				fileStream = fetchFromCkan();
				diskStore.set(this.resourceId, ((ByteArrayOutputStream) fileStream).toByteArray());
			} catch (IOException e) {
				throw new MiddlewareException.DatasetNotFound("Data Not Found");
			}
			this.intermediateResult.add(fileStream.toString());
			this.setResult(this.intermediateResult);
			this.applyOperation();
		}

	}

}