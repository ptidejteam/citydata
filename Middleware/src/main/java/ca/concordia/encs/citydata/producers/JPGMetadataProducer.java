package ca.concordia.encs.citydata.producers;

import ca.concordia.encs.citydata.core.implementations.JPGProducer;
import org.springframework.security.core.context.SecurityContextHolder;

import ca.concordia.encs.citydata.core.utils.RequestOptions;
import ca.concordia.encs.citydata.services.DatasetAccessService;

public class JPGMetadataProducer extends JPGProducer {

    private String metadataPath;

    public JPGMetadataProducer(String filePath) {
        super(filePath);
    }

    public JPGMetadataProducer(final String filePath, RequestOptions fileOptions) {
        super(filePath, fileOptions);
    }

    public void setMetadataPath (String metadataPath) {
        this.metadataPath = metadataPath;
    }

    @Override
    protected void beforeFetch() {
        if (metadataPath != null) {
            String username = SecurityContextHolder.getContext().getAuthentication().getName();
            new DatasetAccessService().checkAuthorisationForPath(username, metadataPath);
        }
    }

}
