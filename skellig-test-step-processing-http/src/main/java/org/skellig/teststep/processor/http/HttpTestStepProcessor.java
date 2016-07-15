package org.skellig.teststep.processor.http;

import com.typesafe.config.Config;
import org.skellig.teststep.processing.converter.TestStepResultConverter;
import org.skellig.teststep.processing.exception.TestStepProcessingException;
import org.skellig.teststep.processing.processor.BaseTestStepProcessor;
import org.skellig.teststep.processing.processor.TestStepProcessor;
import org.skellig.teststep.processing.state.TestScenarioState;
import org.skellig.teststep.processing.validation.TestStepResultValidator;
import org.skellig.teststep.processor.http.model.HttpMethodName;
import org.skellig.teststep.processor.http.model.HttpRequestDetails;
import org.skellig.teststep.processor.http.model.HttpTestStep;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HttpTestStepProcessor extends BaseTestStepProcessor<HttpTestStep> {

    private Map<String, HttpChannel> httpChannelPerService;

    protected HttpTestStepProcessor(Map<String, HttpChannel> httpChannelPerService,
                                    TestScenarioState testScenarioState,
                                    TestStepResultValidator validator,
                                    TestStepResultConverter testStepResultConverter) {
        super(testScenarioState, validator, testStepResultConverter);
        this.httpChannelPerService = httpChannelPerService;
    }

    @Override
    protected Object processTestStep(HttpTestStep testStep) {
        if (testStep.getServices().isEmpty()) {
            throw new TestStepProcessingException("No services were provided to run an HTTP request." +
                    " Registered services are: " + httpChannelPerService.keySet().toString());
        }

        Map<String, Object> result = new HashMap<>();
        testStep.getServices().parallelStream()
                .forEach(serviceName -> {
                    if (httpChannelPerService.containsKey(serviceName)) {
                        HttpChannel httpChannel = httpChannelPerService.get(serviceName);
                        HttpRequestDetails request = buildHttpRequestDetails(testStep);

                        result.put(serviceName, httpChannel.send(request));
                    } else {
                        throw new TestStepProcessingException(String.format(
                                "Service '%s' was not registered in HTTP Processor." +
                                        " Registered services are: %s", serviceName, httpChannelPerService.keySet().toString()));
                    }
                });
        return result.size() == 1 ? result.values().stream().findFirst().get() : result;
    }

    private HttpRequestDetails buildHttpRequestDetails(HttpTestStep httpTestStep) {
        Object testData = httpTestStep.getTestData();

        return new HttpRequestDetails.Builder(HttpMethodName.valueOf(httpTestStep.getMethod()))
                .withUrl(httpTestStep.getUrl())
                .withHeaders(httpTestStep.getHeaders())
                .withQueryParam(httpTestStep.getQuery())
                .withFormParam(httpTestStep.getForm())
                .withUsername(httpTestStep.getUsername())
                .withPassword(httpTestStep.getPassword())
                .withBody(testData != null ? String.valueOf(testData) : null)
                .build();
    }

    @Override
    public Class<HttpTestStep> getTestStepClass() {
        return HttpTestStep.class;
    }

    public static class Builder extends BaseTestStepProcessor.Builder<HttpTestStep> {
        private static final String URL_KEYWORD = "url";
        private static final String SERVICE_NAME_KEYWORD = "serviceName";
        private static final String HTTP_CONFIG_KEYWORD = "http";

        private Map<String, HttpChannel> httpChannelPerService;

        public Builder() {
            httpChannelPerService = new HashMap<>();
        }

        public Builder withHttpService(String serviceName, String url) {
            this.httpChannelPerService.put(serviceName, new HttpChannel(url));
            return this;
        }

        public Builder withHttpService(Config config) {
            if (config.hasPath(HTTP_CONFIG_KEYWORD)) {
                ((List<Map<String, String>>) config.getAnyRefList(HTTP_CONFIG_KEYWORD))
                        .forEach(rawHttpService ->
                                withHttpService(rawHttpService.get(SERVICE_NAME_KEYWORD), rawHttpService.get(URL_KEYWORD)));
            }
            return this;
        }

        @Override
        public TestStepProcessor<HttpTestStep> build() {
            return new HttpTestStepProcessor(httpChannelPerService, testScenarioState, validator, testStepResultConverter);
        }
    }
}
