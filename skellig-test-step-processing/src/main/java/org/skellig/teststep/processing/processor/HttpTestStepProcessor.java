package org.skellig.teststep.processing.processor;

import org.skellig.connection.channel.SendingChannel;
import org.skellig.connection.http.HttpChannel;
import org.skellig.connection.http.model.HttpMethodName;
import org.skellig.connection.http.model.HttpRequestDetails;
import org.skellig.connection.http.model.HttpResponse;
import org.skellig.teststep.processing.state.TestScenarioState;
import org.skellig.teststep.reader.model.HttpTestStep;
import org.skellig.teststep.reader.model.TestStep;

import java.util.HashMap;
import java.util.Map;

public class HttpTestStepProcessor implements TestStepProcessor {

    private Map<String, SendingChannel> httpChannelPerService;
    private TestScenarioState testScenarioState;

    private HttpTestStepProcessor(Map<String, SendingChannel> httpChannelPerService,
                                  TestScenarioState testScenarioState) {
        this.httpChannelPerService = httpChannelPerService;
    }

    @Override
    public void process(TestStep testStep) {
        HttpTestStep httpTestStep = (HttpTestStep) testStep;

        Object result = callHttpServices(httpTestStep);

        testScenarioState.set(httpTestStep.getId(), result);
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
        private Map<String, SendingChannel> httpChannelPerService;
        private TestScenarioState testScenarioState;

        public Builder() {
            httpChannelPerService = new HashMap<>();
        }

        public Builder withHttpService(String serviceName, String serviceBaseUrl) {
            this.httpChannelPerService.put(serviceName, new HttpChannel(serviceBaseUrl));
            return this;
        }

        public Builder withTestScenarioState(TestScenarioState testScenarioState) {
            this.testScenarioState = testScenarioState;
            return this;
        }

        public TestStepProcessor build() {
            return new HttpTestStepProcessor(httpChannelPerService, testScenarioState);
        }
    }
}
