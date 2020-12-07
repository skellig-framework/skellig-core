package org.skellig.teststep.processor.http.model;

import org.skellig.teststep.processing.model.TestStep;
import org.skellig.teststep.processing.model.TestStepExecutionType;
import org.skellig.teststep.processing.model.ValidationDetails;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

public class HttpTestStep extends TestStep {

    private String url;
    private Collection<String> services;
    private String method;
    private String username;
    private String password;
    private Map<String, String> headers;
    private Map<String, String> query;
    private Map<String, String> form;

    protected HttpTestStep(String id, String name, TestStepExecutionType execution, int timeout, int delay,
                           Map<String, Object> variables, Object testData, ValidationDetails validationDetails,
                           String url, Collection<String> services, String method, String username, String password,
                           Map<String, String> headers, Map<String, String> query, Map<String, String> form) {
        super(id, name, execution, timeout, delay, variables, testData, validationDetails);
        this.url = url;
        this.services = services;
        this.method = method;
        this.username = username;
        this.password = password;
        this.headers = headers;
        this.query = query;
        this.form = form;
    }

    public String getUrl() {
        return url;
    }

    public Collection<String> getServices() {
        return services;
    }

    public String getMethod() {
        return method;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public Map<String, String> getQuery() {
        return query;
    }

    public Map<String, String> getForm() {
        return form;
    }

    public static class Builder extends TestStep.Builder {

        private Collection<String> services = Collections.emptyList();
        private String url;
        private String method;
        private String username;
        private String password;
        private Map<String, String> headers;
        private Map<String, String> query;
        private Map<String, String> form;


        public Builder withService(Collection<String> services) {
            this.services = services;
            return this;
        }

        public Builder withUrl(String url) {
            this.url = url;
            return this;
        }

        public Builder withMethod(String method) {
            this.method = method;
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

        public Builder withHeaders(Map<String, String> headers) {
            this.headers = headers;
            return this;
        }

        public Builder withQuery(Map<String, String> query) {
            this.query = query;
            return this;
        }

        public Builder withForm(Map<String, String> form) {
            this.form = form;
            return this;
        }

        @Override
        public HttpTestStep build() {
            return new HttpTestStep(id, name, execution, timeout, delay, variables, testData, validationDetails,
                    url, services, method, username, password, headers, query, form);
        }
    }
}