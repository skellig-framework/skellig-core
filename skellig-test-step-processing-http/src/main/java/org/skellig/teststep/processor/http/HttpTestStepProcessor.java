package org.skellig.teststep.processor.http;

import com.typesafe.config.Config;
import org.skellig.teststep.processing.converter.TestStepResultConverter;
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

    private HttpTestStepProcessor(Map<String, HttpChannel> httpChannelPerService,
                                  TestScenarioState testScenarioState,
                                  TestStepResultValidator validator,
                                  TestStepResultConverter testStepResultConverter) {
        super(testScenarioState, validator, testStepResultConverter);
        this.httpChannelPerService = httpChannelPerService;
    }

    @Override
    protected Object processTestStep(HttpTestStep testStep) {
        Map<String, Object> result = new HashMap<>();
        testStep.getServices().parallelStream()
                .forEach(serviceName -> {
                    HttpChannel httpChannel = httpChannelPerService.get(serviceName);
                    HttpRequestDetails request = buildHttpRequestDetails(testStep);
                    Object httpResponse = httpChannel.send(request);

                    result.put(serviceName, httpResponse);
                });
        return result.size() == 1 ? result.values().stream().findFirst().get() : result;
    }

    private HttpRequestDetails buildHttpRequestDetails(HttpTestStep httpTestStep) {
        Object serializedPayload = httpTestStep.getTestData();

        return new HttpRequestDetails.Builder(HttpMethodName.valueOf(httpTestStep.getMethod()))
                .withUrl(httpTestStep.getUrl())
                .withHeaders(httpTestStep.getHeaders())
                .withQueryParam(httpTestStep.getQuery())
                .withFormParam(httpTestStep.getForm())
                .withUsername(httpTestStep.getUsername())
                .withPassword(httpTestStep.getPassword())
                .withBody(serializedPayload != null ? String.valueOf(serializedPayload) : null)
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

        public Builder withHttpService(HttpServiceDetails serviceDetails) {
            this.httpChannelPerService.put(serviceDetails.getServiceName(), new HttpChannel(serviceDetails.getUrl()));
            return this;
        }

        public Builder withHttpService(Config config) {
            if (config.hasPath(HTTP_CONFIG_KEYWORD)) {
                ((List<Map<String, String>>) config.getAnyRefList(HTTP_CONFIG_KEYWORD))
                        .forEach(rawHttpService ->
                                withHttpService(new HttpServiceDetails(rawHttpService.get(SERVICE_NAME_KEYWORD),
                                        rawHttpService.get(URL_KEYWORD))));
            }
            return this;
        }

        @Override
        public TestStepProcessor<HttpTestStep> build() {
            return new HttpTestStepProcessor(httpChannelPerService, testScenarioState, validator, testStepResultConverter);
        }
    }

    static class HttpServiceDetails {
        private String serviceName;
        private String url;

        public HttpServiceDetails(String serviceName, String url) {
            this.serviceName = serviceName;
            this.url = url;
        }

        public String getServiceName() {
            return serviceName;
        }

        public String getUrl() {
            return url;
        }
    }
}
