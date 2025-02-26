package de.wpsverlinden.c4cduplicateanalyzer.feed;

import de.wpsverlinden.c4cduplicateanalyzer.ApplicationConfiguration;
import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;
import org.apache.olingo.odata2.api.edm.Edm;
import org.apache.olingo.odata2.api.edm.EdmEntityContainer;
import org.apache.olingo.odata2.api.ep.EntityProvider;
import org.apache.olingo.odata2.api.ep.EntityProviderException;
import org.apache.olingo.odata2.api.ep.EntityProviderReadProperties;
import org.apache.olingo.odata2.api.ep.entry.ODataEntry;
import org.apache.olingo.odata2.api.ep.feed.ODataFeed;
import org.apache.olingo.odata2.api.exception.ODataException;
import org.slf4j.Logger;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import jakarta.annotation.PostConstruct;


@Component
public class ODataFeedReceiver implements Tasklet {

    public static final String SEPARATOR = "/";

    @Autowired
    private HttpConnectionHelper httpConnectionHelper;

    @Autowired
    private Logger LOG;

    @Autowired
    private ApplicationConfiguration config;

    private Edm edm;

    @PostConstruct
    private void initialize() throws IOException, EntityProviderException {
        final String metadata_endpoint = config.getEndpoint() + "/$metadata";
        LOG.debug("Requesting service metadata from '{}'", metadata_endpoint);
        InputStream content = httpConnectionHelper.getConnectionInputStream(metadata_endpoint, HttpConnectionHelper.APPLICATION_XML, config.getUser(), config.getPassword());
        this.edm = EntityProvider.readMetadata(content, true);
        LOG.debug("Received service metadata '{}'", edm);
    }

    public ODataFeed readFeed(String entitySetName, Optional<String> urlParameter) throws IOException, ODataException {
        EdmEntityContainer entityContainer = edm.getDefaultEntityContainer();
        String targetUri = buildUri(config.getEndpoint(), entitySetName, urlParameter);
        LOG.debug("Generated request url '{}'", targetUri);
        InputStream content = httpConnectionHelper.getContentInputStream(targetUri, HttpConnectionHelper.APPLICATION_JSON, config.getUser(), config.getPassword());
        return EntityProvider.readFeed(HttpConnectionHelper.APPLICATION_JSON,
                entityContainer.getEntitySet(entitySetName),
                content,
                EntityProviderReadProperties.init().build());
    }

    public ODataEntry readEntry(String entitySetName, Optional<String> urlParameter) throws IOException, ODataException {
        EdmEntityContainer entityContainer = edm.getDefaultEntityContainer();
        String targetUri = buildUri(config.getEndpoint(), entitySetName, urlParameter);
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

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
        try {
            initialize();
        } catch (IOException | EntityProviderException e) {
            LOG.error("Error initializing EDM: {}", e);
            throw e;
        }
        return RepeatStatus.FINISHED;
    }
}
