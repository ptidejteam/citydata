package ca.concordia.encs.citydata.producers;

import org.springframework.security.core.context.SecurityContextHolder;

import ca.concordia.encs.citydata.core.implementations.XLSXProducer;
import ca.concordia.encs.citydata.core.utils.RequestOptions;
import ca.concordia.encs.citydata.services.DatasetAccessService;


public class XLSXTemperatureProducer extends XLSXProducer {

	private String metadataPath;

	public XLSXTemperatureProducer(String filePath) {
		super(filePath);
	}

	public XLSXTemperatureProducer(final String filePath, RequestOptions fileOptions) {
		super(filePath, fileOptions);
	}

	public XLSXTemperatureProducer() {
		super();
	}

	public void setMetadataPath(String metadataPath) {
		this.metadataPath = metadataPath;
	}

	@Override
	protected void beforeFetch() {
		if (metadataPath != null && SecurityContextHolder.getContext().getAuthentication() != null) {
			String username = SecurityContextHolder.getContext().getAuthentication().getName();
			new DatasetAccessService().checkAuthorisationForPath(username, metadataPath);
		}
	}
}