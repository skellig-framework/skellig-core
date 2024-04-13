package org.skellig.teststep.processor.ibmmq

import org.junit.jupiter.api.*
import org.mockito.ArgumentMatchers.anyInt
import org.mockito.kotlin.*
import org.skellig.teststep.processing.exception.ValidationException
import org.skellig.teststep.processing.model.ValidationNode
import org.skellig.teststep.processing.processor.TestStepProcessor
import org.skellig.teststep.processing.state.TestScenarioState
import org.skellig.teststep.processor.ibmmq.model.IbmMqTestStep
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicReference

internal class IbmMqTestStepProcessorTest {

    class IbmMqTestStepProcessorUnderTest(
        testScenarioState: TestScenarioState,
        ibmMqChannels: Map<String, IbmMqChannel>
    ) : IbmMqTestStepProcessor(testScenarioState, ibmMqChannels)


    companion object {
        private const val CHANNEL_ID = "host1"
        private const val CHANNEL_ID_2 = "host2"
    }

    private var processor: TestStepProcessor<IbmMqTestStep>? = null
    private var ibmMqChannel = mock<IbmMqChannel>()
    private var ibmMqChannel2 = mock<IbmMqChannel>()
    private var testScenarioState: TestScenarioState? = null

    @BeforeEach
    fun setUp() {
        val ibmMqChannels = mapOf(
            Pair(CHANNEL_ID, ibmMqChannel),
            Pair(CHANNEL_ID_2, ibmMqChannel2)
        )

        testScenarioState = mock()

        processor = IbmMqTestStepProcessorUnderTest(testScenarioState!!, ibmMqChannels)
    }

    @Test
    @DisplayName("Send data to non-registered channel Then verify exception is captured")
    fun testSendToChannelNotRegistered() {
        val testStep = IbmMqTestStep.Builder()
            .sendTo(setOf("host3"))
            .withTestData("hi")
            .withName("n1")
            .build()
        val ref: AtomicReference<Exception> = AtomicReference<Exception>()

        processor!!.process(testStep).subscribe { _, _, e -> ref.set(e) }

        Assertions.assertEquals("Channel 'host3' was not registered in IBM MQ Test Step Processor", ref.get().message)
    }

    @Nested
    internal inner class SendAndReceiveTest {
        @Test
        @DisplayName("Send data Then verify ibmmq channel is called")
        fun testSendData() {
            val testStep = IbmMqTestStep.Builder()
                .sendTo(setOf(CHANNEL_ID))
                .withTestData("hi")
                .withName("n1")
                .build()

            val result = processor!!.process(testStep)

            Assertions.assertNotNull(result)
            verify(ibmMqChannel).send(testStep.testData!!)
        }

        @Test
        @DisplayName("Send and receive data Then verify ibmmq channel is called and returned response")
        fun testSendAndReceive() {
            val response = "yo"
            val testStep = IbmMqTestStep.Builder()
                .sendTo(setOf(CHANNEL_ID))
                .readFrom(setOf(CHANNEL_ID))
                .withTestData("hi")
                .withName("n1")
                .build()
            whenever(ibmMqChannel.read(anyInt())).thenReturn(response.toByteArray())

            val isPassed = AtomicBoolean()
            processor!!.process(testStep)
                .subscribe { _, r, _ ->
                    Assertions.assertEquals(response, String((r as Map<String, Any>)[CHANNEL_ID] as ByteArray))
                    isPassed.set(true)
                }

            assertAll(
                { Assertions.assertTrue(isPassed.get()) },
                {
                    verify(testScenarioState!!).set(eq(testStep.getId + "_result"),
                        argThat { args -> (args as Map<String, Any>).containsKey(CHANNEL_ID) })
                }
            )
        }

        @Test
        @DisplayName("Receive and respond to different channel Then verify ibmmq channel is called to respond")
        fun testReceiveAndRespondToDifferentChannel() {
            val testStep: IbmMqTestStep = IbmMqTestStep.Builder()
                .respondTo(setOf(CHANNEL_ID_2))
                .readFrom(setOf(CHANNEL_ID))
                .withTestData("hi")
                .withName("n1")
                .build()
            whenever(ibmMqChannel.read(anyInt())).thenReturn("yo".toByteArray())

            processor!!.process(testStep)

            verify(ibmMqChannel2).send(testStep.testData!!)
        }

        @Test
        @DisplayName("Receive invalid response And try to respond Then verify ibmmq channel did not respond")
        fun testReceiveInvalidAndTryRespond() {
            val response = "yo".toByteArray()
            val expectedResult = mock<ValidationNode>()
            val testStep: IbmMqTestStep = IbmMqTestStep.Builder()
                .respondTo(setOf(CHANNEL_ID))
                .readFrom(setOf(CHANNEL_ID))
                .withTestData("hi")
                .withName("n1")
                .withValidationDetails(expectedResult)
                .build()
            whenever(ibmMqChannel.read(anyInt())).thenReturn(response)

            doThrow(ValidationException("oops")).whenever(expectedResult)
                .validate(argThat { args -> (args as Map<*, *>)[CHANNEL_ID] == response })

            processor!!.process(testStep)

            verify(ibmMqChannel, times(0)).send(testStep.testData!!)
        }
    }

}