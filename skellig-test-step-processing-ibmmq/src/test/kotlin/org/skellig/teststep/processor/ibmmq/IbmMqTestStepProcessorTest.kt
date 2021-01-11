package org.skellig.teststep.processor.ibmmq

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
import org.skellig.teststep.processor.ibmmq.model.IbmMqTestStep
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicReference

internal class IbmMqTestStepProcessorTest {

    class IbmMqTestStepProcessorUnderTest(testScenarioState: TestScenarioState?,
                                          validator: TestStepResultValidator?,
                                          testStepResultConverter: TestStepResultConverter?,
                                          ibmMqChannels: Map<String, IbmMqChannel>)
        : IbmMqTestStepProcessor(testScenarioState, validator, testStepResultConverter, ibmMqChannels)


    companion object {
        private const val CHANNEL_ID = "host1"
        private const val CHANNEL_ID_2 = "host2"
    }

    private var processor: TestStepProcessor<IbmMqTestStep>? = null
    private var ibmMqChannel = Mockito.mock(IbmMqChannel::class.java)
    private var ibmMqChannel2 = Mockito.mock(IbmMqChannel::class.java)
    private var validator: TestStepResultValidator? = null
    private var testScenarioState: TestScenarioState? = null

    @BeforeEach
    fun setUp() {
        val ibmMqChannels = mapOf(
                Pair(CHANNEL_ID, ibmMqChannel),
                Pair(CHANNEL_ID_2, ibmMqChannel2))

        validator = Mockito.mock(TestStepResultValidator::class.java)
        testScenarioState = Mockito.mock(TestScenarioState::class.java)

        processor = IbmMqTestStepProcessorUnderTest(testScenarioState, validator,
                Mockito.mock(TestStepResultConverter::class.java), ibmMqChannels)
    }

    @Test
    @DisplayName("Send data to non-registered channel Then verify exception is captured")
    fun testSendToChannelNotRegistered() {
        val testStep = IbmMqTestStep.Builder()
                .withSendTo("host3")
                .withTestData("hi")
                .withName("n1")
                .build() as IbmMqTestStep
        val ref: AtomicReference<Exception> = AtomicReference<Exception>()

        processor!!.process(testStep)
                .subscribe { _, _, e -> ref.set(e) }

        Assertions.assertEquals("Channel 'host3' was not registered in IBMMQ Test Step Processor", ref.get().message)
    }

    @Nested
    internal inner class SendAndReceiveTest {
        @Test
        @DisplayName("Send data Then verify rmq channel is called")
        fun testSendData() {
            val testStep = IbmMqTestStep.Builder()
                    .withSendTo(CHANNEL_ID)
                    .withTestData("hi")
                    .withName("n1")
                    .build() as IbmMqTestStep

            val result = processor!!.process(testStep)

            Assertions.assertNotNull(result)
            Mockito.verify(ibmMqChannel).send(testStep.testData!!)
        }

        @Test
        @DisplayName("Send and receive data Then verify rmq channel is called and returned response")
        fun testSendAndReceive() {
            val response = "yo"
            val testStep = IbmMqTestStep.Builder()
                    .withSendTo(CHANNEL_ID)
                    .withReceiveFrom(CHANNEL_ID)
                    .withTestData("hi")
                    .withName("n1")
                    .build() as IbmMqTestStep
            whenever(ibmMqChannel!!.read(ArgumentMatchers.anyInt())).thenReturn(response.toByteArray())

            val isPassed = AtomicBoolean()
            processor!!.process(testStep)
                    .subscribe { _, r, _ ->
                        Assertions.assertEquals(response, String((r as ByteArray?)!!))
                        isPassed.set(true)
                    }

            Assertions.assertTrue(isPassed.get())
            Mockito.verify<TestScenarioState>(testScenarioState).set(testStep.getId + ".result", response.toByteArray())
        }

        @Test
        @DisplayName("Receive and respond to different channel Then verify rmq channel is called to respond")
        fun testReceiveAndRespondToDifferentChannel() {
            val testStep: IbmMqTestStep = IbmMqTestStep.Builder()
                    .withRespondTo(CHANNEL_ID_2)
                    .withReceiveFrom(CHANNEL_ID)
                    .withTestData("hi")
                    .withName("n1")
                    .build() as IbmMqTestStep
            whenever(ibmMqChannel!!.read(ArgumentMatchers.anyInt())).thenReturn("yo".toByteArray())

            processor!!.process(testStep)

            Mockito.verify(ibmMqChannel2).send(testStep.testData!!)
        }

        @Test
        @DisplayName("Receive invalid response And try to respond Then verify rmq channel did not respond")
        fun testReceiveInvalidAndTryRespond() {
            val response = "yo"
            val expectedResult = ExpectedResult(null, "yo yo", MatchingType.ALL_MATCH)
            val testStep: IbmMqTestStep = IbmMqTestStep.Builder()
                    .withRespondTo(CHANNEL_ID)
                    .withReceiveFrom(CHANNEL_ID)
                    .withTestData("hi")
                    .withValidationDetails(
                            ValidationDetails.Builder()
                                    .withExpectedResult(expectedResult)
                                    .build())
                    .withName("n1")
                    .build() as IbmMqTestStep
            whenever(ibmMqChannel!!.read(ArgumentMatchers.anyInt())).thenReturn(response.toByteArray())
            Mockito.doThrow(ValidationException::class.java).`when`<TestStepResultValidator?>(validator).validate(expectedResult, response.toByteArray())

            processor!!.process(testStep)

            Mockito.verify(ibmMqChannel, Mockito.times(0)).send(testStep.testData!!)
        }
    }

}