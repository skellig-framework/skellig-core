package org.skellig.teststep.processor.ibmmq;

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
import org.skellig.teststep.processor.ibmmq.model.IbmMqTestStep;

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

class IbmMqTestStepProcessorTest {

    private static final String CHANNEL_ID = "host1";
    private static final String CHANNEL_ID_2 = "host2";

    private TestStepProcessor<IbmMqTestStep> processor;
    private IbmMqChannel ibmMqChannel;
    private IbmMqChannel ibmMqChannel2;
    private TestStepResultValidator validator;
    private TestScenarioState testScenarioState;

    @BeforeEach
    void setUp() {
        Map<String, IbmMqChannel> ibmMqChannels = new HashMap<>();
        ibmMqChannel = mock(IbmMqChannel.class);
        ibmMqChannel2 = mock(IbmMqChannel.class);
        ibmMqChannels.put(CHANNEL_ID, ibmMqChannel);
        ibmMqChannels.put(CHANNEL_ID_2, ibmMqChannel2);

        validator = mock(TestStepResultValidator.class);
        testScenarioState = mock(TestScenarioState.class);
        processor = new IbmMqTestStepProcessor(testScenarioState, validator,
                mock(TestStepResultConverter.class), ibmMqChannels);
    }

    @Test
    @DisplayName("Send data to non-registered channel Then verify exception is captured")
    void testSendToChannelNotRegistered() {
        TestStep testStep =
                new IbmMqTestStep.Builder()
                        .withSendTo("host3")
                        .withTestData("hi")
                        .build();

        AtomicReference<Exception> ref = new AtomicReference<>();
        processor.process((IbmMqTestStep) testStep)
                .subscribe((t, r, e) -> ref.set(e));

        assertEquals("Channel 'host3' was not registered in IBMMQ Test Step Processor", ref.get().getMessage());
    }


    @Nested
    class SendAndReceiveTest {

        @Test
        @DisplayName("Send data Then verify rmq channel is called")
        void testSendData() {
            IbmMqTestStep testStep =
                    (IbmMqTestStep) new IbmMqTestStep.Builder()
                            .withSendTo(CHANNEL_ID)
                            .withTestData("hi")
                            .build();

            TestStepProcessor.TestStepRunResult result = processor.process(testStep);

            assertNotNull(result);
            verify(ibmMqChannel).send(testStep.getTestData());
        }

        @Test
        @DisplayName("Send and receive data Then verify rmq channel is called and returned response")
        void testSendAndReceive() {
            String response = "yo";
            TestStep testStep =
                    new IbmMqTestStep.Builder()
                            .withSendTo(CHANNEL_ID)
                            .withReceiveFrom(CHANNEL_ID)
                            .withTestData("hi")
                            .build();
            when(ibmMqChannel.read(anyInt())).thenReturn(response.getBytes());

            AtomicBoolean isPassed = new AtomicBoolean();

            processor.process((IbmMqTestStep) testStep)
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
            IbmMqTestStep testStep =
                    (IbmMqTestStep) new IbmMqTestStep.Builder()
                            .withRespondTo(CHANNEL_ID_2)
                            .withReceiveFrom(CHANNEL_ID)
                            .withTestData("hi")
                            .build();
            when(ibmMqChannel.read(anyInt())).thenReturn("yo".getBytes());

            processor.process(testStep);

            verify(ibmMqChannel2).send(testStep.getTestData());
        }

        @Test
        @DisplayName("Receive invalid response And try to respond Then verify rmq channel did not respond")
        void testReceiveInvalidAndTryRespond() {
            String response = "yo";
            ExpectedResult expectedResult = new ExpectedResult(null, "yo yo", ValidationType.ALL_MATCH);
            IbmMqTestStep testStep =
                    (IbmMqTestStep) new IbmMqTestStep.Builder()
                            .withRespondTo(CHANNEL_ID)
                            .withReceiveFrom(CHANNEL_ID)
                            .withTestData("hi")
                            .withValidationDetails(
                                    new ValidationDetails.Builder()
                                            .withExpectedResult(expectedResult)
                                            .build())
                            .build();
            when(ibmMqChannel.read(anyInt())).thenReturn(response.getBytes());
            doThrow(ValidationException.class).when(validator).validate(expectedResult, response.getBytes());

            processor.process(testStep);

            verify(ibmMqChannel, times(0)).send(testStep.getTestData());
        }
    }
}