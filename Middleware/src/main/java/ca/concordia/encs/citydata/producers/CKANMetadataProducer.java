package ca.concordia.encs.citydata.producers;

import java.util.ArrayList;

import com.google.gson.JsonObject;

import ca.concordia.encs.citydata.core.implementations.AbstractProducer;
import ca.concordia.encs.citydata.core.contracts.IOperation;
import ca.concordia.encs.citydata.core.contracts.IProducer;
import ca.concordia.encs.citydata.core.contracts.IRunner;
import ca.concordia.encs.citydata.core.utils.RequestOptions;
import ca.concordia.encs.citydata.producers.base.JSONProducer;

/**
 * This producer can connect to a CKAN instance and fetch either dataset or resource metadata.
 * @author Gabriel C. Ullmann
 * @since 2025-02-12
 */
public class CKANMetadataProducer extends AbstractProducer<JsonObject> implements IProducer<JsonObject> {

	private String url;
	private String resourceId;
	private String datasetName;
    private IOperation<JsonObject> jsonProducerOperation;
	private IRunner runnerObserver;

	public void setUrl(String url) {
		if (url != null) {
			if (url.contains("http")) {
				this.url = url;
			} else {
				this.url = "https://" + url;
			}
		}
	}

	public void setDatasetName(String datasetName) {
		this.datasetName = datasetName;
	}

	public void setResourceId(String resourceId) {
		this.resourceId = resourceId;
	}

	@SuppressWarnings("rawtypes")
    @Override
	public void setOperation(IOperation operation) {
        //noinspection unchecked
        this.jsonProducerOperation = operation;
	}

	@Override
	public void fetch() {
		if (this.url != null) {
			JSONProducer jsonProducer = getJsonProducer();
			jsonProducer.addObserver(this.runnerObserver);
			jsonProducer.fetch();
		} else {
			final JsonObject errorObject = new JsonObject();
			errorObject.addProperty("error",
					"No URL informed. Please use the 'url' parameter to specify a CKAN server URL.");
			final ArrayList<JsonObject> result = new ArrayList<>();
			result.add(errorObject);
			this.setResult(result);
			super.addObserver(this.runnerObserver);
			super.setOperation(this.jsonProducerOperation);
			this.applyOperation();
		}
	}

	private JSONProducer getJsonProducer() {
		String actionUrl = this.url;
		if (this.resourceId != null) {
			actionUrl += "/action/resource_show?id=" + this.resourceId;
		} else if (this.datasetName != null) {
			actionUrl += "/action/package_show?id=" + this.datasetName;
		} else {
			// if no dataset or resource is specified, fetch the list of all datasets
			actionUrl += "/action/package_list";
		}

		// all CKAN metadata routes are GET routes
		RequestOptions requestOptions = new RequestOptions();
		requestOptions.setMethod("GET");

		// delegate to JSON producer
		JSONProducer jsonProducer = new JSONProducer(actionUrl, requestOptions);
		jsonProducer.setOperation(this.jsonProducerOperation);
		return jsonProducer;
	}

	@Override
	public void addObserver(final IRunner aRunner) {
		this.runnerObserver = aRunner;
	}

}