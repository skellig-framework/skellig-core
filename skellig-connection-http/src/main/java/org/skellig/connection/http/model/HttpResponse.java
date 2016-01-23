package org.skellig.connection.http.model;

import java.util.HashMap;
import java.util.Map;

public final class HttpResponse {

    private int statusCode;
    private Map<String, String> headers;
    private String body;

    private HttpResponse(int statusCode, Map<String, String> headers, String body) {
        this.statusCode = statusCode;
        this.headers = headers;
        this.body = body;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public String getBody() {
        return body;
    }

    public static class Builder {

        private int statusCode;
        private Map<String, String> headers = new HashMap<>();
        private String body;

        public Builder withBody(String body) {
            this.body = body;
            return this;
        }

        public Builder withHeaders(Map<String, String> theHeaders) {
            this.headers = theHeaders;
            return this;
        }

        public Builder withStatusCode(int code) {
            statusCode = code;
            return this;
        }

        public HttpResponse build() {
            return new HttpResponse(statusCode, headers, body);
        }

    }
}
