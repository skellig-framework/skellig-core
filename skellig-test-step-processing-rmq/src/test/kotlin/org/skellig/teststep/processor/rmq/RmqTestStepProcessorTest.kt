package org.skellig.teststep.processor.rmq

import com.nhaarman.mockitokotlin2.whenever
import org.junit.jupiter.api.*
import org.mockito.ArgumentMatchers
import org.mockito.Mockito
import org.skellig.teststep.processing.converter.TestStepResultConverter
import org.skellig.teststep.processing.exception.ValidationException
import org.skellig.teststep.processing.model.ExpectedResult
import org.skellig.teststep.processing.model.MatchingType
import org.skellig.teststep.processing.model.ValidationDetails
import org.skellig.teststep.processing.processor.TestStepProcessor
import org.skellig.teststep.processing.state.TestScenarioState
import org.skellig.teststep.processing.validation.TestStepResultValidator
import org.skellig.teststep.processor.rmq.model.RmqTestStep
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicReference

internal class RmqTestStepProcessorTest {

    companion object {
        private const val CHANNEL_ID = "host1"
        private const val CHANNEL_ID_2 = "host2"
    }

    private var processor: TestStepProcessor<RmqTestStep>? = null
    private var rmqChannel = Mockito.mock(RmqChannel::class.java)
    private var rmqChannel2 = Mockito.mock(RmqChannel::class.java)
    private var validator = Mockito.mock(TestStepResultValidator::class.java)
    private var testScenarioState = Mockito.mock(TestScenarioState::class.java)

    @BeforeEach
    fun setUp() {
        val rmqChannels = mapOf(
                Pair(CHANNEL_ID, rmqChannel),
                Pair(CHANNEL_ID_2, rmqChannel2))

        processor = RmqTestStepProcessor(rmqChannels, testScenarioState,
                validator, Mockito.mock(TestStepResultConverter::class.java))
    }

    @Test
    @DisplayName("Send data to non-registered channel Then verify exception is captured")
    fun testSendToChannelNotRegistered() {
        val testStep = RmqTestStep.Builder()
                .withSendTo("host3")
                .withTestData("hi")
                .withName("n1")
                .build()
        val ref: AtomicReference<Exception> = AtomicReference<Exception>()

        processor!!.process(testStep as RmqTestStep).subscribe { _, _, e -> ref.set(e) }

        Assertions.assertEquals("Channel 'host3' was not registered in RMQ Test Step Processor", ref.get().message)
    }

    @Nested
    internal inner class SendAndReceiveTest {
        @Test
        @DisplayName("Send data Then verify rmq channel is called")
        fun testSendData() {
            val testStep: RmqTestStep = RmqTestStep.Builder()
                    .withSendTo(CHANNEL_ID)
                    .withTestData("hi")
                    .withName("n1")
                    .build() as RmqTestStep

            val result = processor!!.process(testStep)

            Assertions.assertNotNull(result)
            Mockito.verify(rmqChannel).send(testStep.testData, testStep.routingKey)
        }

        @Test
        @DisplayName("Send and receive data Then verify rmq channel is called and returned response")
        fun testSendAndReceive() {
            val response = "yo"
            val testStep = RmqTestStep.Builder()
                    .withSendTo(CHANNEL_ID)
                    .withReceiveFrom(CHANNEL_ID)
                    .withTestData("hi")
                    .withName("n1")
                    .build()
            whenever(rmqChannel!!.read(ArgumentMatchers.any(), ArgumentMatchers.anyInt())).thenReturn(response.toByteArray())

            val isPassed = AtomicBoolean()
            processor!!.process(testStep as RmqTestStep)
                    .subscribe { _, r, _ ->
                        Assertions.assertEquals(response, String((r as ByteArray?)!!))
                        isPassed.set(true)
                    }

            Assertions.assertTrue(isPassed.get())
            Mockito.verify(testScenarioState).set(testStep.getId + ".result", response.toByteArray())
        }

        @Test
        @DisplayName("Receive and respond to different channel Then verify rmq channel is called to respond")
        fun testReceiveAndRespondToDifferentChannel() {
            val testStep: RmqTestStep = RmqTestStep.Builder()
                    .withRespondTo(CHANNEL_ID_2)
                    .withReceiveFrom(CHANNEL_ID)
                    .withTestData("hi")
                    .withName("n1")
                    .build() as RmqTestStep
            whenever(rmqChannel!!.read(ArgumentMatchers.anyInt(), ArgumentMatchers.anyInt())).thenReturn("yo".toByteArray())

            processor!!.process(testStep)

            Mockito.verify(rmqChannel2).send(testStep.testData, testStep.routingKey)
        }

        @Test
        @DisplayName("Receive invalid response And try to respond Then verify rmq channel did not respond")
        fun testReceiveInvalidAndTryRespond() {
            val response = "yo"
            val expectedResult = ExpectedResult(null, "yo yo", MatchingType.ALL_MATCH)
            val testStep: RmqTestStep = RmqTestStep.Builder()
                    .withRespondTo(CHANNEL_ID)
                    .withReceiveFrom(CHANNEL_ID)
                    .withTestData("hi")
                    .withName("n1")
                    .withValidationDetails(
                            ValidationDetails.Builder()
                                    .withExpectedResult(expectedResult)
                                    .build())
                    .build() as RmqTestStep
            whenever(rmqChannel!!.read(ArgumentMatchers.any(), ArgumentMatchers.anyInt())).thenReturn(response.toByteArray())
            Mockito.doThrow(ValidationException::class.java).whenever(validator).validate(expectedResult, response.toByteArray())

            processor!!.process(testStep)

            Mockito.verify(rmqChannel, Mockito.times(0)).send(testStep.testData, testStep.routingKey)
        }
    }
}