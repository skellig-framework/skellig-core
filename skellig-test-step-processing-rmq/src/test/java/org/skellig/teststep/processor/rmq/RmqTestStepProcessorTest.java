package org.skellig.teststep.processor.rmq;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.skellig.teststep.processing.converter.TestStepResultConverter;
import org.skellig.teststep.processing.exception.ValidationException;
import org.skellig.teststep.processing.model.ExpectedResult;
import org.skellig.teststep.processing.model.TestStep;
import org.skellig.teststep.processing.model.ValidationDetails;
import org.skellig.teststep.processing.model.ValidationType;
import org.skellig.teststep.processing.processor.TestStepProcessor;
import org.skellig.teststep.processing.state.TestScenarioState;
import org.skellig.teststep.processing.validation.TestStepResultValidator;
import org.skellig.teststep.processor.rmq.model.RmqTestStep;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class RmqTestStepProcessorTest {

    private static final String CHANNEL_ID = "host1";
    private static final String CHANNEL_ID_2 = "host2";

    private TestStepProcessor<RmqTestStep> processor;
    private RmqChannel rmqChannel;
    private RmqChannel rmqChannel2;
    private TestStepResultValidator validator;
    private TestScenarioState testScenarioState;

    @BeforeEach
    void setUp() {
        Map<String, RmqChannel> rmqChannels = new HashMap<>();
        rmqChannel = mock(RmqChannel.class);
        rmqChannel2 = mock(RmqChannel.class);
        rmqChannels.put(CHANNEL_ID, rmqChannel);
        rmqChannels.put(CHANNEL_ID_2, rmqChannel2);

        validator = mock(TestStepResultValidator.class);
        testScenarioState = mock(TestScenarioState.class);
        processor = new RmqTestStepProcessor(rmqChannels, testScenarioState,
                validator, mock(TestStepResultConverter.class));
    }

    @Test
    @DisplayName("Send data to non-registered channel Then verify exception is captured")
    void testSendToChannelNotRegistered() {
        TestStep testStep =
                new RmqTestStep.Builder()
                        .withSendTo("host3")
                        .withTestData("hi")
                        .build();

        AtomicReference<Exception> ref = new AtomicReference<>();
        processor.process((RmqTestStep) testStep)
                .subscribe((t, r, e) -> ref.set(e));

        assertEquals("Channel 'host3' was not registered in RMQ Test Step Processor", ref.get().getMessage());
    }


    @Nested
    class SendAndReceiveTest {

        @Test
        @DisplayName("Send data Then verify rmq channel is called")
        void testSendData() {
            RmqTestStep testStep =
                    (RmqTestStep) new RmqTestStep.Builder()
                            .withSendTo(CHANNEL_ID)
                            .withTestData("hi")
                            .build();

            TestStepProcessor.TestStepRunResult result = processor.process(testStep);

            assertNotNull(result);
            verify(rmqChannel).send(testStep.getTestData(), testStep.getRoutingKey());
        }

        @Test
        @DisplayName("Send and receive data Then verify rmq channel is called and returned response")
        void testSendAndReceive() {
            String response = "yo";
            TestStep testStep =
                    new RmqTestStep.Builder()
                            .withSendTo(CHANNEL_ID)
                            .withReceiveFrom(CHANNEL_ID)
                            .withTestData("hi")
                            .build();
            when(rmqChannel.read(anyInt(), anyInt())).thenReturn(response.getBytes());

            AtomicBoolean isPassed = new AtomicBoolean();

            processor.process((RmqTestStep) testStep)
                    .subscribe((t, r, e) -> {
                        assertEquals(response, new String((byte[]) r));
                        isPassed.set(true);
                    });

            assertTrue(isPassed.get());
            verify(testScenarioState).set(testStep.getId() + ".result", response.getBytes());
        }

        @Test
        @DisplayName("Receive and respond to different channel Then verify rmq channel is called to respond")
        void testReceiveAndRespondToDifferentChannel() {
            RmqTestStep testStep =
                    (RmqTestStep) new RmqTestStep.Builder()
                            .withRespondTo(CHANNEL_ID_2)
                            .withReceiveFrom(CHANNEL_ID)
                            .withTestData("hi")
                            .build();
            when(rmqChannel.read(anyInt(), anyInt())).thenReturn("yo".getBytes());

            processor.process(testStep);

            verify(rmqChannel2).send(testStep.getTestData(), testStep.getRoutingKey());
        }

        @Test
        @DisplayName("Receive invalid response And try to respond Then verify rmq channel did not respond")
        void testReceiveInvalidAndTryRespond() {
            String response = "yo";
            ExpectedResult expectedResult = new ExpectedResult(null, "yo yo", ValidationType.ALL_MATCH);
            RmqTestStep testStep =
                    (RmqTestStep) new RmqTestStep.Builder()
                            .withRespondTo(CHANNEL_ID)
                            .withReceiveFrom(CHANNEL_ID)
                            .withTestData("hi")
                            .withValidationDetails(
                                    new ValidationDetails.Builder()
                                            .withExpectedResult(expectedResult)
                                            .build())
                            .build();
            when(rmqChannel.read(anyInt(), anyInt())).thenReturn(response.getBytes());
            doThrow(ValidationException.class).when(validator).validate(expectedResult, response.getBytes());

            processor.process(testStep);

            verify(rmqChannel, times(0)).send(testStep.getTestData(), testStep.getRoutingKey());
        }
    }

}