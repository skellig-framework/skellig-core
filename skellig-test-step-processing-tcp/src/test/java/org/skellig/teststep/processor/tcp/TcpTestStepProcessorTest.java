package org.skellig.teststep.processor.tcp;

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
import org.skellig.teststep.processor.tcp.model.TcpTestStep;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
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

class TcpTestStepProcessorTest {

    private static final String CHANNEL_ID = "host1";
    private static final String CHANNEL_ID_2 = "host2";

    private TestStepProcessor<TcpTestStep> processor;
    private TcpChannel tcpChannel;
    private TcpChannel tcpChannel2;
    private TestStepResultValidator validator;
    private TestScenarioState testScenarioState;

    @BeforeEach
    void setUp() {
        Map<String, TcpChannel> tcpChannels = new HashMap<>();
        tcpChannel = mock(TcpChannel.class);
        tcpChannel2 = mock(TcpChannel.class);
        tcpChannels.put(CHANNEL_ID, tcpChannel);
        tcpChannels.put(CHANNEL_ID_2, tcpChannel2);

        validator = mock(TestStepResultValidator.class);
        testScenarioState = mock(TestScenarioState.class);
        processor = new TcpTestStepProcessor(tcpChannels, testScenarioState,
                validator, mock(TestStepResultConverter.class));
    }

    @Test
    @DisplayName("Send data to non-registered channel Then verify exception is captured")
    void testSendToChannelNotRegistered() {
        TestStep testStep =
                new TcpTestStep.Builder()
                        .withSendTo("host3")
                        .withTestData("hi")
                        .build();

        AtomicReference<Exception> ref = new AtomicReference<>();
        processor.process((TcpTestStep) testStep)
                .subscribe((t, r, e) -> ref.set(e));

        assertEquals("Channel 'host3' was not registered in TCP Test Step Processor", ref.get().getMessage());
    }


    @Nested
    class SendAndReceiveTest {

        @Test
        @DisplayName("Send data Then verify tcp channel is called")
        void testSendData() {
            TestStep testStep =
                    new TcpTestStep.Builder()
                            .withSendTo(CHANNEL_ID)
                            .withTestData("hi")
                            .build();

            TestStepProcessor.TestStepRunResult result = processor.process((TcpTestStep) testStep);

            assertNotNull(result);
            verify(tcpChannel).send(testStep.getTestData());
        }

        @Test
        @DisplayName("Send and receive data Then verify tcp channel is called and returned response")
        void testSendAndReceive() {
            String response = "yo";
            TestStep testStep =
                    new TcpTestStep.Builder()
                            .withSendTo(CHANNEL_ID)
                            .withReceiveFrom(CHANNEL_ID)
                            .withTestData("hi")
                            .build();
            when(tcpChannel.read(anyInt(), anyInt())).thenReturn(response);

            AtomicBoolean isPassed = new AtomicBoolean();

            processor.process((TcpTestStep) testStep)
                    .subscribe((t, r, e) -> {
                        assertEquals(response, r);
                        isPassed.set(true);
                    });

            assertTrue(isPassed.get());
            verify(testScenarioState).set(testStep.getId() + ".result", response);
        }

        @Test
        @DisplayName("Receive and respond to different channel Then verify tcp channel is called to respond")
        void testReceiveAndRespondToDifferentChannel() {
            TestStep testStep =
                    new TcpTestStep.Builder()
                            .withRespondTo(CHANNEL_ID_2)
                            .withReceiveFrom(CHANNEL_ID)
                            .withTestData("hi")
                            .build();
            when(tcpChannel.read(anyInt(), anyInt())).thenReturn(Optional.of("yo"));

            processor.process((TcpTestStep) testStep);

            verify(tcpChannel2).send(testStep.getTestData());
        }

        @Test
        @DisplayName("Receive invalid response And try to respond Then verify tcp channel did not respond")
        void testReceiveInvalidAndTryRespond() {
            String response = "yo";
            ExpectedResult expectedResult = new ExpectedResult(null, "yo yo", ValidationType.ALL_MATCH);
            TestStep testStep =
                    new TcpTestStep.Builder()
                            .withRespondTo(CHANNEL_ID)
                            .withReceiveFrom(CHANNEL_ID)
                            .withTestData("hi")
                            .withValidationDetails(
                                    new ValidationDetails.Builder()
                                            .withExpectedResult(expectedResult)
                                            .build())
                            .build();
            when(tcpChannel.read(anyInt(), anyInt())).thenReturn(response);
            doThrow(ValidationException.class).when(validator).validate(expectedResult, response);

            processor.process((TcpTestStep) testStep);

            verify(tcpChannel, times(0)).send(testStep.getTestData());
        }
    }
}