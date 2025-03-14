package de.wpsverlinden.c4cduplicateanalyzer.feed;

import de.wpsverlinden.c4cduplicateanalyzer.ApplicationConfiguration;
import java.net.URI;
import java.net.URISyntaxException;
import org.apache.hc.client5.http.ConnectionKeepAliveStrategy;
import org.apache.hc.client5.http.auth.AuthScope;
import org.apache.hc.client5.http.auth.UsernamePasswordCredentials;
import org.apache.hc.client5.http.impl.DefaultConnectionKeepAliveStrategy;
import org.apache.hc.client5.http.impl.auth.BasicCredentialsProvider;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class HTTPClient {
    
    @Autowired
    private ApplicationConfiguration config;
    
    @Bean
    public CloseableHttpClient myHttpClient() throws URISyntaxException {

        URI target = new URI(config.getEndpoint());

        //Add auth details in the httpclient instance
        BasicCredentialsProvider credsProvider = new BasicCredentialsProvider();

        credsProvider.setCredentials(
                new AuthScope(target.getHost(), target.getPort()),
                new UsernamePasswordCredentials(config.getUser(), config.getPassword().toCharArray()));

        PoolingHttpClientConnectionManager poolingConnManager = new PoolingHttpClientConnectionManager();
        poolingConnManager.setDefaultMaxPerRoute(config.getMaxConnections());
        poolingConnManager.setMaxTotal(config.getMaxConnections());

        ConnectionKeepAliveStrategy keepAliveStrategy = new DefaultConnectionKeepAliveStrategy();

        return HttpClients.custom()
                .setDefaultCredentialsProvider(credsProvider)
                .setConnectionManager(poolingConnManager)
                .setKeepAliveStrategy(keepAliveStrategy)
                .build();
    }
}
