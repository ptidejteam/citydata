package ca.concordia.encs.citydata.core.implementations;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import ca.concordia.encs.citydata.core.contracts.IProducer;
import ca.concordia.encs.citydata.core.exceptions.MiddlewareException;
import ca.concordia.encs.citydata.core.utils.RequestOptions;


public non-sealed class XmlProducer extends AbstractProducer<JsonObject>
        implements IProducer<JsonObject> {

    private String recordTag = null;
    private boolean structured = false;

    public XmlProducer(final String filePath, final RequestOptions fileOptions) {
        super(filePath, fileOptions);
    }

    public XmlProducer(final String filePath) {
        super(filePath);
    }

    public void setRecordTag(String recordTag) {
        this.recordTag = recordTag;
    }

    public void setStructured(Boolean structured) {
        this.structured = structured;
    }

    @Override
    public void fetch() {
        beforeFetch();
        ArrayList<JsonObject> records = new ArrayList<>();

        try (InputStream inputStream = obtainInputStream()) {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

            // Secure XML parser against XXE attacks
            factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
            factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
            factory.setXIncludeAware(false);
            factory.setExpandEntityReferences(false);

            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(inputStream);
            doc.getDocumentElement().normalize();

            if (recordTag != null && !recordTag.isBlank()) {
                // One record per matching element
                NodeList recordNodes = doc.getElementsByTagName(recordTag);
                for (int i = 0; i < recordNodes.getLength(); i++) {
                    records.add(parseRecord((Element) recordNodes.item(i)));
                }
            } else {
                // Whole document as a single record
                records.add(parseRecord(doc.getDocumentElement()));
            }

        } catch (IOException | SAXException | ParserConfigurationException e) {
            throw new MiddlewareException.DatasetNotFound(
                    "Error processing XML data: " + e.getMessage());
        }

        this.setResult(records);
        this.applyOperation();
    }

    // For authorization checks - if the user has the right to access a specific producer. Implemented within the producers
    protected void beforeFetch() {

    }

    protected InputStream obtainInputStream() {
        return this.fetchStream();
    }

    // Only called when structured = true. Overrided in concrete producers
    protected JsonObject parseRecord(Element element) {
        return rawRecord(element);
    }

    // Default output: implemented for users to access raw data from hub
    protected JsonObject rawRecord(Element element) {
        JsonObject record = new JsonObject();
        record.add(element.getNodeName(), parseElement(element));
        return record;
    }

    protected JsonObject parseElement(Element element) {
        JsonObject result = new JsonObject();

        // 1. Attributes, prefixed with '@' so they cannot collide with child tags
        NamedNodeMap attributes = element.getAttributes();
        for (int i = 0; i < attributes.getLength(); i++) {
            Node attr = attributes.item(i);
            result.addProperty("@" + attr.getNodeName(), attr.getNodeValue());
        }

        // 2. Group child elements by tag name (XML allows repeats, JSON keys do not)
        Map<String, ArrayList<Element>> childrenByName = new LinkedHashMap<>();
        NodeList children = element.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node node = children.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                Element child = (Element) node;
                childrenByName
                        .computeIfAbsent(child.getNodeName(), key -> new ArrayList<>())
                        .add(child);
            }
        }

        // 3. One occurrence -> object, several -> array
        for (Map.Entry<String, ArrayList<Element>> entry : childrenByName.entrySet()) {
            String tagName = entry.getKey();
            ArrayList<Element> group = entry.getValue();

            if (group.size() == 1) {
                result.add(tagName, parseElement(group.get(0)));
            } else {
                JsonArray array = new JsonArray();
                for (Element item : group) {
                    array.add(parseElement(item));
                }
                result.add(tagName, array);
            }
        }

        // 4. Leaf node: store its text, skipping empties
        if (childrenByName.isEmpty()) {
            String text = element.getTextContent().trim();
            if (!text.isEmpty()) {
                result.addProperty("#text", text);
            }
        }

        return result;
    }
}