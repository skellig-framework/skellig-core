package org.skellig.teststep.processor.http;


import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.NameValuePair;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicNameValuePair;
import org.skellig.teststep.processing.exception.TestStepProcessingException;
import org.skellig.teststep.processor.http.model.HttpMethodName;
import org.skellig.teststep.processor.http.model.HttpRequestDetails;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

class HttpRequestFactory {

    private static final int DEFAULT_HTTP_READ_TIMEOUT = 5000;

    private String baseUrl;

    public HttpRequestFactory(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    HttpUriRequest createRequest(HttpRequestDetails httpRequestDetails) {
        HttpRequestBase request;
        try {
            request = createHttpRequest(httpRequestDetails, baseUrl + createUrl(httpRequestDetails));

            request.setConfig(RequestConfig.custom()
                    .setSocketTimeout(DEFAULT_HTTP_READ_TIMEOUT)
                    .setConnectTimeout(DEFAULT_HTTP_READ_TIMEOUT)
                    .setConnectionRequestTimeout(DEFAULT_HTTP_READ_TIMEOUT)
                    .build());
        } catch (UnsupportedEncodingException e) {
            throw new TestStepProcessingException(e.getMessage());
        }

        return request;
    }

    private HttpRequestBase createHttpRequest(HttpRequestDetails httpRequestDetails, String url) throws UnsupportedEncodingException {
        switch (httpRequestDetails.getVerb()) {
            case GET:
                return new HttpGet(url);
            case DELETE:
                return new HttpDelete(url);
            case POST:
                return createRequestWithEntity(httpRequestDetails, new HttpPost(url));
            case PUT:
                return createRequestWithEntity(httpRequestDetails, new HttpPut(url));
            default:
                throw new IllegalArgumentException("Supported HTTP methods: " + Arrays.toString(HttpMethodName.values()));
        }
    }

    private String createUrl(HttpRequestDetails httpRequest) {
        if (httpRequest.getQueryParams().isEmpty()) {
            return httpRequest.getUrl();
        } else {
            List<BasicNameValuePair> parameters =
                    httpRequest.getQueryParams().entrySet().stream()
                            .map(entry -> new BasicNameValuePair(entry.getKey(), entry.getValue()))
                            .collect(Collectors.toList());
            return httpRequest.getUrl() + "?" + URLEncodedUtils.format(parameters, StandardCharsets.UTF_8);
        }
    }

    private HttpRequestBase createRequestWithEntity(HttpRequestDetails httpRequestDetails, HttpEntityEnclosingRequest request) throws UnsupportedEncodingException {
        if (httpRequestDetails.getBody() != null) {
            request.setEntity(new StringEntity(httpRequestDetails.getBody()));
        } else if (httpRequestDetails.getFormParams().size() > 0) {
            List<NameValuePair> params = new ArrayList<>();
            httpRequestDetails.getFormParams().forEach((key, value) -> params.add(new BasicNameValuePair(key, value)));
            request.setEntity(new UrlEncodedFormEntity(params));
        }

        return (HttpRequestBase) request;
    }
}