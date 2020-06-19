package org.skellig.teststep.processing.http.processor;

import com.typesafe.config.Config;
import org.skellig.connection.channel.SendingChannel;
import org.skellig.connection.http.HttpChannel;
import org.skellig.connection.http.model.HttpMethodName;
import org.skellig.connection.http.model.HttpRequestDetails;
import org.skellig.connection.http.model.HttpResponse;
import org.skellig.teststep.processing.http.model.HttpTestStep;
import org.skellig.teststep.processing.processor.BaseTestStepProcessor;
import org.skellig.teststep.processing.processor.TestStepProcessor;
import org.skellig.teststep.processing.state.TestScenarioState;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HttpTestStepProcessor extends BaseTestStepProcessor<HttpTestStep> {

    private Map<String, SendingChannel> httpChannelPerService;

    private HttpTestStepProcessor(Map<String, SendingChannel> httpChannelPerService,
                                  TestScenarioState testScenarioState) {
        super(testScenarioState);
        this.httpChannelPerService = httpChannelPerService;
    }

    @Override
    protected Object processTestStep(HttpTestStep testStep) {
        return callHttpServices(testStep);
    }

    private Object callHttpServices(HttpTestStep httpTestStep) {
        Map<String, Object> result = new HashMap<>();
        httpTestStep.getServices().parallelStream()
                .forEach(serviceName -> {
                    SendingChannel httpChannel = httpChannelPerService.get(serviceName);
                    HttpRequestDetails request = buildHttpRequestDetails(httpTestStep);
                    Object httpResponse = httpChannel.send(request).orElse(new HttpResponse.Builder().build());

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

    public static class Builder {
        private static final String URL_KEYWORD = "url";
        private static final String SERVICE_NAME_KEYWORD = "serviceName";
        private static final String HTTP_CONFIG_KEYWORD = "http";

        private Map<String, SendingChannel> httpChannelPerService;
        private TestScenarioState testScenarioState;

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

        public Builder withTestScenarioState(TestScenarioState testScenarioState) {
            this.testScenarioState = testScenarioState;
            return this;
        }

        public TestStepProcessor<HttpTestStep> build() {
            return new HttpTestStepProcessor(httpChannelPerService, testScenarioState);
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
