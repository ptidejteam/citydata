package ca.concordia.encs.citydata.producers;

import org.springframework.security.core.context.SecurityContextHolder;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.google.gson.JsonObject;

import ca.concordia.encs.citydata.core.implementations.XmlProducer;
import ca.concordia.encs.citydata.core.utils.RequestOptions;
import ca.concordia.encs.citydata.services.DatasetAccessService;


public class XMLBuildingProducer extends XmlProducer {

	private String metadataPath;

	public XMLBuildingProducer(String filePath) {
		super(filePath);
		this.setRecordTag("building");
	}

	public XMLBuildingProducer(final String filePath, RequestOptions fileOptions) {
		super(filePath, fileOptions);
		this.setRecordTag("building");
	}

	public void setMetadataPath(String metadataPath) {
		this.metadataPath = metadataPath;
	}

	@Override
	protected void beforeFetch() {
		if (metadataPath != null) {
			String username = SecurityContextHolder.getContext().getAuthentication().getName();
			new DatasetAccessService().checkAuthorisationForPath(username, metadataPath);
		}
	}

	@Override
	protected JsonObject parseRecord(Element element) {
		JsonObject record = new JsonObject();
		record.addProperty("id", element.getAttribute("id"));
		record.addProperty("address", textOf(element, "address"));
		record.addProperty("city", textOf(element, "city"));
		record.addProperty("yearBuilt", textOf(element, "yearBuilt"));
		record.addProperty("floors", textOf(element, "floors"));
		return record;
	}

	private String textOf(Element parent, String tagName) {
		NodeList nodes = parent.getElementsByTagName(tagName);
		return nodes.getLength() > 0 ? nodes.item(0).getTextContent().trim() : "";
	}
}