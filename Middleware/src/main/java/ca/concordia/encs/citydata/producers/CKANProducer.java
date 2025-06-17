package ca.concordia.encs.citydata.producers;

import java.util.ArrayList;
import java.util.List;

import ca.concordia.encs.citydata.core.exceptions.MiddlewareException.DataStoreFailureReadingException;
import com.google.gson.JsonObject;

import ca.concordia.encs.citydata.core.implementations.AbstractProducer;
import ca.concordia.encs.citydata.core.implementations.AbstractRunner;
import ca.concordia.encs.citydata.core.contracts.IProducer;
import ca.concordia.encs.citydata.core.utils.RequestOptions;
import ca.concordia.encs.citydata.datastores.DiskDatastore;
import ca.concordia.encs.citydata.datastores.InMemoryDataStore;
import ca.concordia.encs.citydata.runners.SingleStepRunner;

/* This producer can connect to a CKAN instance and fetch a resource.
 * Author: Gabriel C. Ullmann 
 * Date: 2025-02-12
 */
public class CKANProducer extends AbstractProducer<String> implements IProducer<String> {

	private String url;
	private String resourceId;
	private DiskDatastore diskStore = DiskDatastore.getInstance();
	private ArrayList<String> intermediateResult = new ArrayList<>();

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

	private ArrayList<JsonObject> getMetadataObject(AbstractRunner aRunner) {
		InMemoryDataStore memoryStore = InMemoryDataStore.getInstance();
		String runnerId = aRunner.getMetadata("id").toString();
		IProducer<?> storeResult = memoryStore.get(runnerId);
		if (storeResult != null) {
			return (ArrayList<JsonObject>) memoryStore.get(runnerId).getResult();
		}
		return new ArrayList<>();
	}

	private JsonObject getResourceAttributes(ArrayList<JsonObject> metadataObject) {
		JsonObject metadataResults = metadataObject.size() > 0 ? metadataObject.get(0).get("result").getAsJsonObject()
				: null;

		JsonObject attributesObject = new JsonObject();
		attributesObject.addProperty("sizeInMb", metadataResults.get("size").getAsInt() / 1000000);
		attributesObject.addProperty("mimetype", metadataResults.get("mimetype").getAsString());
		attributesObject.addProperty("url", metadataResults.get("url").getAsString());

		return attributesObject;
	}

	private boolean isFileSupported(String mimetype) {
		List<String> supportedFormats = List.of("csv", "json", "xml", "txt", "text", "xls");
		for (String supportedFormat : supportedFormats) {
			if (mimetype.contains(supportedFormat)) {
				return true;
			}
		}
		return false;
	}

	private byte[] fetchFromCkan() {
		try {
			// fetch resource metadata first
			CKANMetadataProducer metadataProducer = new CKANMetadataProducer();
			metadataProducer.setUrl(this.url);
			metadataProducer.setResourceId(this.resourceId);
			SingleStepRunner deckard = new SingleStepRunner(metadataProducer);
			Thread runnerTask = new Thread() {
				public void run() {
					try {
						deckard.runSteps();
						while (!deckard.isDone()) {
							System.out.println("Busy waiting!");
						}
					} catch (Exception e) {
						deckard.setAsDone();
						System.out.println(e.getMessage());
					}

				}
			};
			runnerTask.start();
			runnerTask.join();

			ArrayList<JsonObject> metadataObject = getMetadataObject(deckard);
			JsonObject resourceAttributes = getResourceAttributes(metadataObject);
			String resourceUrl = resourceAttributes.get("url").getAsString();
			String mimetype = resourceAttributes.get("mimetype").getAsString();
			int size = resourceAttributes.get("sizeInMb").getAsInt();

			// if the file is supported, download it and save to disk
			if (isFileSupported(mimetype)) {
				if (size > 800) {
					intermediateResult
							.add("Sorry, files larger than 800MB are not currently supported by this producer. "
									+ "If you wish to download the resource, please open this link in your browser: "
									+ resourceAttributes.get("url") + " .");
				}
				RequestOptions requestOptions = new RequestOptions();
				requestOptions.method = "GET";
				this.filePath = resourceUrl;
				this.fileOptions = requestOptions;
				return this.fetchFromPath();
			} else {
				intermediateResult.add("Sorry, the " + mimetype + " type is not currently supported by this producer. "
						+ "If you wish to download the resource, please open this link in your browser: " + resourceUrl
						+ " .");
			}
		} catch (InterruptedException e) {
			ArrayList<String> errorMessageList = new ArrayList<>();
			errorMessageList.add(e.getMessage());
			this.result = errorMessageList;
		}

		return new byte[0];
	}

	@Override
	public void fetch() {

		if (this.resourceId != null) {
			// before attempting to fetch, check if a file with this resource ID already
			// exists in the disk
			byte[] file;
			try {
				file = diskStore.get(this.resourceId);
			} catch (DataStoreFailureReadingException e) {
				// if not, fetch from CKAN and save on disk
				file = fetchFromCkan();
				diskStore.set(this.resourceId, file);
			}
			intermediateResult.add(new String(file));
			this.result = this.intermediateResult;
			this.applyOperation();
		}

	}

}
