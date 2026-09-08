package ca.concordia.encs.citydata.core.implementations;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import com.google.gson.JsonObject;

import ca.concordia.encs.citydata.core.contracts.IProducer;
import ca.concordia.encs.citydata.core.exceptions.MiddlewareException;
import ca.concordia.encs.citydata.core.utils.RequestOptions;
import org.checkerframework.checker.nullness.qual.NonNull;

import javax.imageio.ImageIO;

public non-sealed class JPGProducer extends AbstractProducer<JsonObject> implements IProducer<JsonObject> {

    public JPGProducer(final String filePath, final RequestOptions fileOptions) {
        super(filePath, fileOptions);
    }

    public JPGProducer(final String filePath) {
        super(filePath);
    }

    @Override
    public void fetch() {
        beforeFetch();
        ArrayList<JsonObject> results = new ArrayList<>();

        try (InputStream inputStream = obtainInputStream()) {

            BufferedImage image = ImageIO.read(inputStream);

            JsonObject metadata = getMetadata(image);

            results.add(metadata);

        } catch (IOException e) {
            throw new MiddlewareException.DatasetNotFound("Error processing JPG data");
        }

        this.setResult(results);
        this.applyOperation();

    }

    private @NonNull JsonObject getMetadata(BufferedImage image) {
        if (image == null) {
            throw new MiddlewareException.DatasetNotFound(
                    "File is not a readable JPG image: " + getFilePath()
            );
        }

        String filePath = getFilePath();
        int width = image.getWidth();
        int height = image.getHeight();

        JsonObject metadata = new JsonObject();

        metadata.addProperty("filePath", filePath);
        metadata.addProperty("format", "JPEG");
        metadata.addProperty("width", width);
        metadata.addProperty("height", height);
        return metadata;
    }


    // For authorization checks - if the user has the right to access a specific producer. Implemented within the producers
    protected void beforeFetch() {

    }

    protected InputStream obtainInputStream() {
        return this.fetchStream();
    }


}
