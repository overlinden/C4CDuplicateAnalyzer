package de.wpsverlinden.c4cduplicateanalyzer.feed;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLStreamHandlerFactory;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import static org.assertj.core.api.Assertions.*;
import org.junit.Rule;
import org.junit.rules.ExpectedException;
import org.mockito.ArgumentCaptor;

@RunWith(MockitoJUnitRunner.class)
public class HttpConnectionHelperTest {

    private static HttpUrlStreamHandler httpUrlStreamHandler;

    @Rule
    public ExpectedException fileNotFoundException = ExpectedException.none();

    @BeforeClass
    public static void setupURLStreamHandlerFactory() {
        // Allows for mocking URL connections
        URLStreamHandlerFactory urlStreamHandlerFactory = Mockito.mock(URLStreamHandlerFactory.class);
        URL.setURLStreamHandlerFactory(urlStreamHandlerFactory);

        httpUrlStreamHandler = new HttpUrlStreamHandler();
        Mockito.when(urlStreamHandlerFactory.createURLStreamHandler("http")).thenReturn(httpUrlStreamHandler);
    }

    @Before
    public void reset() {
        httpUrlStreamHandler.resetConnections();
    }

    @Test
    public void invalidHttpStatusCodeRaisesRuntimeException() throws Exception {
        fileNotFoundException.expect(RuntimeException.class);
        fileNotFoundException.expectMessage("Http Connection failed with status 404 Not Found");

        // Given
        HttpConnectionHelper helper = new HttpConnectionHelper();

        String href = "http://example.com/bad-image-reference";

        HttpURLConnection urlConnection = Mockito.mock(HttpURLConnection.class);
        httpUrlStreamHandler.addConnection(new URL(href), urlConnection);

        Mockito.when(urlConnection.getResponseCode()).thenReturn(404);

        // When
        helper.getContentInputStream(href, "application/json", "user", "password");

        // Then exception is thrown
    }

    @Test
    public void getContentInputStreamReturnsExpectedOutput() throws Exception {
        // Given
        HttpConnectionHelper helper = new HttpConnectionHelper();

        String href = "http://example.com/";

        HttpURLConnection urlConnection = Mockito.mock(HttpURLConnection.class);
        httpUrlStreamHandler.addConnection(new URL(href), urlConnection);

        byte[] response = "Test content".getBytes(StandardCharsets.UTF_8);
        Mockito.when(urlConnection.getContent()).thenReturn(new ByteArrayInputStream(response));
        Mockito.when(urlConnection.getResponseCode()).thenReturn(200);

        // When
        InputStream contentInputStream = helper.getContentInputStream(href, "application/json", "user", "password");

        // Then
        assertThat(inputStreamToByteArray(contentInputStream)).isEqualTo(response);
    }

    @Test
    public void getConnectionInputStreamReturnsExpectedOutput() throws Exception {
        // Given
        HttpConnectionHelper helper = new HttpConnectionHelper();

        String href = "http://example.com/";

        HttpURLConnection urlConnection = Mockito.mock(HttpURLConnection.class);
        httpUrlStreamHandler.addConnection(new URL(href), urlConnection);

        byte[] response = "Test stream".getBytes(StandardCharsets.UTF_8);
        Mockito.when(urlConnection.getInputStream()).thenReturn(new ByteArrayInputStream(response));
        Mockito.when(urlConnection.getResponseCode()).thenReturn(200);

        // When
        InputStream connectionInputStream = helper.getConnectionInputStream(href, "application/json", "user", "password");

        // Then
        assertThat(inputStreamToByteArray(connectionInputStream)).isEqualTo(response);
    }

    @Test
    public void checkThatHttpRequestMethodIsGet() throws Exception {
        // Given
        HttpConnectionHelper helper = new HttpConnectionHelper();

        String href = "http://example.com/";

        HttpURLConnection urlConnection = Mockito.mock(HttpURLConnection.class);
        httpUrlStreamHandler.addConnection(new URL(href), urlConnection);

        byte[] response = "Test stream".getBytes(StandardCharsets.UTF_8);
        Mockito.when(urlConnection.getInputStream()).thenReturn(new ByteArrayInputStream(response));
        Mockito.when(urlConnection.getResponseCode()).thenReturn(200);

        // When
        helper.getConnectionInputStream(href, "accept_header", "user", "password");

        // Then
        ArgumentCaptor<String> requestMethodArgument = ArgumentCaptor.forClass(String.class);
        Mockito.verify(urlConnection).setRequestMethod(requestMethodArgument.capture());
        assertThat(requestMethodArgument.getValue()).isEqualTo("GET");
    }

    @Test
    public void checkThatHttpAcceptHeaderIsConsidered() throws Exception {
        // Given
        HttpConnectionHelper helper = new HttpConnectionHelper();

        String href = "http://example.com/";

        HttpURLConnection urlConnection = Mockito.mock(HttpURLConnection.class);
        httpUrlStreamHandler.addConnection(new URL(href), urlConnection);

        byte[] response = "Test stream".getBytes(StandardCharsets.UTF_8);
        Mockito.when(urlConnection.getInputStream()).thenReturn(new ByteArrayInputStream(response));
        Mockito.when(urlConnection.getResponseCode()).thenReturn(200);

        // When
        helper.getConnectionInputStream(href, "accept_header", "user", "password");

        // Then
        ArgumentCaptor<String> acceptArgument = ArgumentCaptor.forClass(String.class);

        Mockito.verify(urlConnection).setRequestProperty(Mockito.eq("Accept"), acceptArgument.capture());
        assertThat(acceptArgument.getValue()).isEqualTo("accept_header");
    }

    @Test
    public void checkThatHttpAuthorizationHeaderIsConsidered() throws Exception {
        // Given
        HttpConnectionHelper helper = new HttpConnectionHelper();

        String href = "http://example.com/";

        HttpURLConnection urlConnection = Mockito.mock(HttpURLConnection.class);
        httpUrlStreamHandler.addConnection(new URL(href), urlConnection);

        byte[] response = "Test stream".getBytes(StandardCharsets.UTF_8);
        Mockito.when(urlConnection.getInputStream()).thenReturn(new ByteArrayInputStream(response));
        Mockito.when(urlConnection.getResponseCode()).thenReturn(200);

        // When
        helper.getConnectionInputStream(href, "accept_header", "user", "password");

        // Then
        ArgumentCaptor<String> authorizationArgument = ArgumentCaptor.forClass(String.class);

        Mockito.verify(urlConnection).setRequestProperty(Mockito.eq("Authorization"), authorizationArgument.capture());
        String encodedUserPassword = new String(Base64.getEncoder().encode("user:password".getBytes()));
        assertThat(authorizationArgument.getValue()).isEqualTo("Basic " + encodedUserPassword);
    }

    private byte[] inputStreamToByteArray(InputStream in) throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        int nRead;
        byte[] data = new byte[1024];
        while ((nRead = in.read(data, 0, data.length)) != -1) {
            buffer.write(data, 0, nRead);
        }

        buffer.flush();
        return buffer.toByteArray();
    }
}