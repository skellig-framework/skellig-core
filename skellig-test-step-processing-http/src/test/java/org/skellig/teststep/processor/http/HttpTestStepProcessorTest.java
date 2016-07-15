package org.skellig.teststep.processor.http;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatcher;
import org.skellig.teststep.processing.converter.TestStepResultConverter;
import org.skellig.teststep.processing.processor.TestStepProcessor;
import org.skellig.teststep.processing.state.TestScenarioState;
import org.skellig.teststep.processing.validation.TestStepResultValidator;
import org.skellig.teststep.processor.http.model.HttpRequestDetails;
import org.skellig.teststep.processor.http.model.HttpResponse;
import org.skellig.teststep.processor.http.model.HttpTestStep;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class HttpTestStepProcessorTest {

    private static final String SRV_2 = "srv2";
    private static final String SRV_1 = "srv1";

    private TestStepProcessor<HttpTestStep> processor;
    private HttpChannel httpChannel;
    private HttpChannel httpChannel2;

    @BeforeEach
    void setUp() {
        httpChannel = mock(HttpChannel.class);
        httpChannel2 = mock(HttpChannel.class);

        Map<String, HttpChannel> channels = new HashMap<>();
        channels.put("srv1", httpChannel);
        channels.put("srv2", httpChannel2);

        processor = new HttpTestStepProcessor(channels, mock(TestScenarioState.class),
                mock(TestStepResultValidator.class), mock(TestStepResultConverter.class));
    }

    @Test
    @DisplayName("Send HTTP request to a single service Then verify response received un-grouped")
    void testSendHttpRequest() {
        HttpTestStep httpTestStep =
                new HttpTestStep.Builder()
                        .withService(Collections.singletonList(SRV_1))
                        .withUrl("/a/b/c")
                        .withMethod("POST")
                        .build();

        HttpResponse response1 = new HttpResponse.Builder().build();

        when(httpChannel.send(argThat(new ArgumentMatcher<HttpRequestDetails>() {
            @Override
            public boolean matches(Object param) {
                HttpRequestDetails request = (HttpRequestDetails) param;
                return request.getUrl().equals("/a/b/c");
            }
        }))).thenReturn(response1);

        AtomicBoolean isPassed = new AtomicBoolean();

        processor.process(httpTestStep)
                .subscribe((t, r, e) -> {
                    assertEquals(response1, r);
                    isPassed.set(true);
                });

        assertTrue(isPassed.get());
    }

    @Test
    @DisplayName("Send HTTP request to 2 service Then verify response received from both")
    void testSendHttpRequestToAllServices() {
        HttpTestStep httpTestStep =
                new HttpTestStep.Builder()
                        .withService(Arrays.asList(SRV_1, SRV_2))
                        .withUrl("/a/b/c")
                        .withMethod("POST")
                        .build();

        HttpResponse response1 = new HttpResponse.Builder().build();
        HttpResponse response2 = new HttpResponse.Builder().build();

        when(httpChannel.send(any(HttpRequestDetails.class))).thenReturn(response1);
        when(httpChannel2.send(any(HttpRequestDetails.class))).thenReturn(response2);

        AtomicBoolean isPassed = new AtomicBoolean();

        processor.process(httpTestStep)
                .subscribe((t, r, e) -> {
                    assertEquals(response1, ((Map) r).get(SRV_1));
                    assertEquals(response2, ((Map) r).get(SRV_2));
                    isPassed.set(true);
                });

        assertTrue(isPassed.get());
    }

    @Test
    @DisplayName("Send HTTP request When no services provided Then throw an error")
    void testSendHttpRequestWhenNoServicesProvided() {
        HttpTestStep httpTestStep =
                new HttpTestStep.Builder()
                        .withUrl("/a/b/c")
                        .withMethod("POST")
                        .build();

        AtomicBoolean isPassed = new AtomicBoolean();
        processor.process(httpTestStep)
                .subscribe((t, r, e) -> {
                    assertEquals("No services were provided to run an HTTP request." +
                            " Registered services are: [srv1, srv2]", e.getMessage());
                    isPassed.set(true);
                });

        assertTrue(isPassed.get());
    }

    @Test
    @DisplayName("Send HTTP request to a service which not registered Then throw an error")
    void testSendHttpRequestToNonExistentService() {
        HttpTestStep httpTestStep =
                new HttpTestStep.Builder()
                        .withService(Collections.singletonList("srv3"))
                        .withUrl("/a/b/c")
                        .withMethod("POST")
                        .build();

        AtomicBoolean isPassed = new AtomicBoolean();
        processor.process(httpTestStep)
                .subscribe((t, r, e) -> {
                    assertEquals("Service 'srv3' was not registered in HTTP Processor." +
                            " Registered services are: [srv1, srv2]", e.getMessage());
                    isPassed.set(true);
                });

        assertTrue(isPassed.get());
    }
}