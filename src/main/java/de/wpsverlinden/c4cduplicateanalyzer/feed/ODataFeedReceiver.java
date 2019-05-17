package de.wpsverlinden.c4cduplicateanalyzer.feed;

import de.wpsverlinden.c4cduplicateanalyzer.ApplicationConfiguration;
import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;
import org.apache.olingo.odata2.api.edm.Edm;
import org.apache.olingo.odata2.api.edm.EdmEntityContainer;
import org.apache.olingo.odata2.api.ep.EntityProvider;
import org.apache.olingo.odata2.api.ep.EntityProviderReadProperties;
import org.apache.olingo.odata2.api.ep.entry.ODataEntry;
import org.apache.olingo.odata2.api.ep.feed.ODataFeed;
import org.apache.olingo.odata2.api.exception.ODataException;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ODataFeedReceiver {

    public static final String SEPARATOR = "/";

    @Autowired
    private EDMReceiver edmReceiver;

    @Autowired
    private HttpConnectionHelper httpConnectionHelper;

    @Autowired
    private Logger LOG;

    @Autowired
    private ApplicationConfiguration config;

    public ODataFeed readFeed(String entitySetName, Optional<String> urlParameter) throws IOException, ODataException {
        Edm edm = edmReceiver.getEdm();
        EdmEntityContainer entityContainer = edm.getDefaultEntityContainer();
        String targetUri = buildUri(edmReceiver.getEndpoint(), entitySetName, urlParameter);
        LOG.debug("Generated request url '{}'", targetUri);
        InputStream content = httpConnectionHelper.getContentInputStream(targetUri, HttpConnectionHelper.APPLICATION_JSON, config.getUser(), config.getPassword());
        return EntityProvider.readFeed(HttpConnectionHelper.APPLICATION_JSON,
                entityContainer.getEntitySet(entitySetName),
                content,
                EntityProviderReadProperties.init().build());
    }

    public ODataEntry readEntry(String entitySetName, Optional<String> urlParameter) throws IOException, ODataException {
        Edm edm = edmReceiver.getEdm();
        EdmEntityContainer entityContainer = edm.getDefaultEntityContainer();
        String targetUri = buildUri(edmReceiver.getEndpoint(), entitySetName, urlParameter);
        LOG.debug("Generated request url '{}'", targetUri);
        InputStream content = httpConnectionHelper.getContentInputStream(targetUri, HttpConnectionHelper.APPLICATION_JSON, config.getUser(), config.getPassword());
        return EntityProvider.readEntry(HttpConnectionHelper.APPLICATION_JSON,
                entityContainer.getEntitySet(entitySetName),
                content,
                EntityProviderReadProperties.init().build());
    }

    private String buildUri(String serviceUri, String entitySetName, Optional<String> urlParameter) {
        final StringBuilder targetUri = new StringBuilder(serviceUri).append(SEPARATOR).append(entitySetName);
        urlParameter.ifPresent(p -> targetUri.append(p));
        return targetUri.toString();
    }

}
