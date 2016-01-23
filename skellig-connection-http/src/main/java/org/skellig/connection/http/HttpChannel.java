package org.skellig.connection.http;


import org.apache.http.NameValuePair;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.skellig.connection.channel.SendingChannel;
import org.skellig.connection.http.exception.HttpClientException;
import org.skellig.connection.http.factory.HttpRequestFactory;
import org.skellig.connection.http.model.HttpRequestDetails;
import org.skellig.connection.http.model.HttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class HttpChannel implements SendingChannel {

    private static Logger logger = LoggerFactory.getLogger(HttpChannel.class);

    private final HttpRequestFactory httpRequestFactory;

    public HttpChannel() {
        this("");
    }

    public HttpChannel(String baseUrl) {
        httpRequestFactory = new HttpRequestFactory(baseUrl);
    }

    @Override
    public Optional<Object> send(Object request) {
        HttpRequestDetails httpRequestDetails = (HttpRequestDetails) request;
        HttpClientBuilder httpClientBuilder = HttpClientBuilder.create();

        if (httpRequestDetails.getUsername().isPresent()) {
            authoriseRequest(httpRequestDetails.getUsername().get(),
                    httpRequestDetails.getPassword().orElse(""), httpClientBuilder);
        }

        try (CloseableHttpClient httpClient = httpClientBuilder.build()) {
            HttpUriRequest httpRequest = createHttpRequest(httpRequestDetails);

            logger.info("Run HTTP request {}", httpRequest.getURI());

            org.apache.http.HttpResponse response = httpClient.execute(httpRequest);

            logger.info("Received HTTP response from {}: {}", httpRequest.getURI(), response.getStatusLine());

            return Optional.of(convertToLocalResponse(response));
        } catch (Exception e) {
            throw new HttpClientException("Failed to send HTTP request to " + httpRequestDetails.getUrl(), e);
        }
    }

    private void authoriseRequest(String userName, String password, HttpClientBuilder httpClientBuilder) {
        CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
        credentialsProvider.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(userName, password));
        httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider);
    }

    private HttpUriRequest createHttpRequest(HttpRequestDetails httpRequestDetails) {
        HttpUriRequest request = httpRequestFactory.createRequest(httpRequestDetails);
        httpRequestDetails.getHeaders().forEach(request::addHeader);
        return request;
    }

    private HttpResponse convertToLocalResponse(org.apache.http.HttpResponse response) throws IOException {
        HttpResponse.Builder builder = new HttpResponse.Builder().withStatusCode(response.getStatusLine().getStatusCode());
        builder.withHeaders(Stream.of(response.getAllHeaders())
                .collect(Collectors.toMap(NameValuePair::getName, NameValuePair::getValue)));
        if (response.getEntity() != null) {
            builder.withBody(EntityUtils.toString(response.getEntity()));
        }
        return builder.build();
    }
}
