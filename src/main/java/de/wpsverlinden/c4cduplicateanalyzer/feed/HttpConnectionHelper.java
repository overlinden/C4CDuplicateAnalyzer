package de.wpsverlinden.c4cduplicateanalyzer.feed;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Base64;
import org.apache.olingo.odata2.api.commons.HttpStatusCodes;
import org.springframework.stereotype.Component;

@Component
public class HttpConnectionHelper {

    public static final String HTTP_METHOD_GET = "GET";

    public static final String HTTP_HEADER_CONTENT_TYPE = "Content-Type";
    public static final String HTTP_HEADER_ACCEPT = "Accept";

    public static final String APPLICATION_JSON = "application/json";
    public static final String APPLICATION_XML = "application/xml";
        

    public InputStream getContentInputStream(String relativeUri, String contentType, String user, String password) throws IOException {
        HttpURLConnection connection = initializeConnection(relativeUri, contentType, user, password);
        connection.connect();
        checkStatus(connection);
        InputStream content = (InputStream)connection.getContent();
        return content;
    }
    
    public InputStream getConnectionInputStream(String relativeUri, String contentType, String user, String password) throws IOException {
        HttpURLConnection connection = initializeConnection(relativeUri, contentType, user, password);

        connection.connect();
        checkStatus(connection);

        InputStream content = connection.getInputStream();
        return content;
    }

    private HttpURLConnection initializeConnection(String absolutUri, String contentType, String user, String password)
            throws MalformedURLException, IOException {
        URL url = new URL(absolutUri);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod(HTTP_METHOD_GET);
        connection.setRequestProperty(HTTP_HEADER_ACCEPT, contentType);
        
        String userPassword = user + ":" + password;
        String encodedUserPassword = new String(Base64.getEncoder().encode(userPassword.getBytes()));
        connection.setRequestProperty("Authorization", "Basic " + encodedUserPassword);

        return connection;
    }

    private HttpStatusCodes checkStatus(HttpURLConnection connection) throws IOException {
        HttpStatusCodes httpStatusCode = HttpStatusCodes.fromStatusCode(connection.getResponseCode());
        if (400 <= httpStatusCode.getStatusCode() && httpStatusCode.getStatusCode() <= 599) {
            throw new RuntimeException("Http Connection failed with status " + httpStatusCode.getStatusCode() + " " + httpStatusCode.toString());
        }
        return httpStatusCode;
    }
}
