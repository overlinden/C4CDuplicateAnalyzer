package de.wpsverlinden.c4cduplicateanalyzer.feed;

import de.wpsverlinden.c4cduplicateanalyzer.ApplicationConfiguration;
import java.io.IOException;
import java.io.InputStream;
import javax.annotation.PostConstruct;
import org.apache.olingo.odata2.api.edm.Edm;
import org.apache.olingo.odata2.api.ep.EntityProvider;
import org.apache.olingo.odata2.api.ep.EntityProviderException;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class EDMReceiver {

    public static final String METADATA = "$metadata";

    @Autowired
    private HttpConnectionHelper httpConnectionHelper;

    @Autowired
    private Logger LOG;

    @Autowired
    private ApplicationConfiguration config;

    private Edm edm;

    @PostConstruct
    private void initialize() throws IOException, EntityProviderException {
        final String metadata_endpoint = config.getEndpoint() + "/" + METADATA;
        LOG.debug("Requesting service metadata from '{}'", metadata_endpoint);
        InputStream content = httpConnectionHelper.getConnectionInputStream(metadata_endpoint, HttpConnectionHelper.APPLICATION_XML, config.getUser(), config.getPassword());
        this.edm = EntityProvider.readMetadata(content, true);
        LOG.debug("Received service metadata '{}'", edm);
    }

    public Edm getEdm() {
        return edm;
    }

    public String getEndpoint() {
        return config.getEndpoint();
    }
}
