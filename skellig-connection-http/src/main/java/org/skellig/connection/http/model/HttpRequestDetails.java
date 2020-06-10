package org.skellig.connection.http.model;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;

public class HttpRequestDetails {

    private final HttpMethodName verb;
    private final String url;
    private final Map<String, String> formParams;
    private final String body;
    private final Map<String, String> headers;
    private final Map<String, String> queryParams;
    private final String username;
    private final String password;

    private HttpRequestDetails(HttpMethodName verb, String url, Map<String, String> headers,
                               Map<String, String> formParams, String body, Map<String, String> queryParams,
                               String username, String password) {
        this.verb = verb;
        this.url = url;
        this.headers = headers;
        this.formParams = formParams;
        this.queryParams = queryParams;
        this.body = body;
        this.username = username;
        this.password = password;
    }

    public HttpMethodName getVerb() {
        return verb;
    }

    public String getUrl() {
        return url;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public Map<String, String> getFormParams() {
        return formParams;
    }

    public String getBody() {
        return body;
    }

    public Map<String, String> getQueryParams() {
        return queryParams;
    }

    public Optional<String> getUsername() {
        return Optional.ofNullable(username);
    }

    public Optional<String> getPassword() {
        return Optional.ofNullable(password);
    }

    @Override
    public String toString() {
        return url;
    }

    public static class Builder {

        private HttpMethodName verb;
        private String url;
        private Map<String, String> headers = Collections.emptyMap();
        private Map<String, String> formParams = Collections.emptyMap();
        private Map<String, String> queryParams= Collections.emptyMap();
        private String body;
        private String username;
        private String password;

        public Builder(HttpMethodName verb) {
            this.verb = verb;
        }

        public Builder withUrl(String url) {
            this.url = url;
            return this;
        }

        public Builder withHeaders(Map<String, String> headers) {
            if (headers != null) {
                this.headers = headers;
            }
            return this;
        }

        public Builder withFormParam(Map<String, String> formParams) {
            if (formParams != null) {
                this.formParams = formParams;
            }
            return this;
        }

        public Builder withQueryParam(Map<String, String> queryParams) {
            if (queryParams != null) {
                this.queryParams = queryParams;
            }
            return this;
        }

        public Builder withBody(String body) {
            this.body = body;
            return this;
        }

        public Builder withUsername(String username) {
            this.username = username;
            return this;
        }

        public Builder withPassword(String password) {
            this.password = password;
            return this;
        }

        public HttpRequestDetails build() {
            return new HttpRequestDetails(verb, url, headers, formParams, body, queryParams, username, password);
        }
    }

}
