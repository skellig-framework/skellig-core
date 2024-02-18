package org.skellig.teststep.processor.rmq


import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.mockito.ArgumentMatchers
import org.mockito.Mockito
import org.mockito.kotlin.*
import org.skellig.teststep.processing.exception.ValidationException
import org.skellig.teststep.processing.model.ValidationNode
import org.skellig.teststep.processing.processor.TestStepProcessor
import org.skellig.teststep.processing.state.TestScenarioState
import org.skellig.teststep.processor.rmq.model.RmqTestStep
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicReference

@DisplayName("Test rmq")
class RmqTestStepProcessorTest {

    companion object {
        private const val CHANNEL_NAME = "host1"
        private const val CHANNEL_NAME_2 = "host2"
    }

    private var processor: TestStepProcessor<RmqTestStep>? = null
    private var rmqChannel = Mockito.mock(RmqChannel::class.java)
    private var rmqChannel2 = Mockito.mock(RmqChannel::class.java)
    private var testScenarioState = Mockito.mock(TestScenarioState::class.java)

    @BeforeEach
    fun setUp() {
        val rmqChannels = mapOf(
            Pair(CHANNEL_NAME, rmqChannel),
            Pair(CHANNEL_NAME_2, rmqChannel2)
        )

        processor = RmqTestStepProcessor(rmqChannels, testScenarioState)
    }

    @Test
    @DisplayName("Send data to non-registered channel Then verify exception is captured")
    fun testSendToNotRegisteredChannel() {
        val testStep = RmqTestStep.Builder()
            .sendTo(setOf("host3"))
            .withTestData("hi")
            .withName("n1")
            .build()
        val ref: AtomicReference<Exception> = AtomicReference<Exception>()

        processor!!.process(testStep).subscribe { _, _, e -> ref.set(e) }

        assertEquals("Channel 'host3' was not registered in RMQ Test Step Processor", ref.get().message)
    }

    @Test
    @DisplayName("Read data from non-registered channel Then verify exception is captured")
    fun testReadFromNotRegisteredChannel() {
        val testStep = RmqTestStep.Builder()
            .readFrom(setOf("host3"))
            .withTestData("hi")
            .withName("n1")
            .build()
        val ref: AtomicReference<Exception> = AtomicReference<Exception>()

        processor!!.process(testStep).subscribe { _, _, e -> ref.set(e) }

        assertEquals("Channel 'host3' was not registered in RMQ Test Step Processor", ref.get().message)
    }

    @Nested
    internal inner class SendAndReceiveTest {
        @Test
        @DisplayName("Send data Then verify rmq channel is called")
        fun testSendData() {
            val testStep: RmqTestStep = RmqTestStep.Builder()
                .sendTo(setOf(CHANNEL_NAME))
                .withTestData("hi")
                .withName("n1")
                .build()

            val result = processor!!.process(testStep)

            Assertions.assertNotNull(result)
            Mockito.verify(rmqChannel).send(testStep.testData, testStep.routingKey)
        }

        @Test
        @DisplayName("Send and receive data Then verify rmq channel is called and returned response")
        fun testSendAndReceive() {
            val response = "yo"
            val testStep = RmqTestStep.Builder()
                .sendTo(setOf(CHANNEL_NAME))
                .readFrom(setOf(CHANNEL_NAME))
                .withTestData("hi")
                .withName("n1")
                .build()
            whenever(rmqChannel!!.read(ArgumentMatchers.any())).thenReturn(response.toByteArray())

            val isPassed = AtomicBoolean()
            processor!!.process(testStep)
                .subscribe { _, r, _ ->
                    assertEquals(response, String((r as Map<*, *>)[CHANNEL_NAME] as ByteArray))
                    isPassed.set(true)
                }

            assertAll(
                { assertTrue(isPassed.get()) },
                {
                    verify(testScenarioState!!).set(eq(testStep.getId + "_result"),
                        argThat { args -> (args as Map<*, *>).containsKey(CHANNEL_NAME) })
                }
            )
        }

        @Test
        @DisplayName("Receive and respond to different channel Then verify rmq channel is called to respond")
        fun testReceiveAndRespondToDifferentChannel() {
            val testStep: RmqTestStep = RmqTestStep.Builder()
                .respondTo(setOf(CHANNEL_NAME_2))
                .readFrom(setOf(CHANNEL_NAME))
                .withTestData("hi")
                .withName("n1")
                .build()
            whenever(rmqChannel!!.read(ArgumentMatchers.anyInt())).thenReturn("yo".toByteArray())

            processor!!.process(testStep)

            verify(rmqChannel2).send(testStep.testData, testStep.routingKey)
        }

        @Test
        @DisplayName("Receive invalid response And try to respond Then verify rmq channel did not respond")
        fun testReceiveInvalidAndTryRespond() {
            val response = "yo".toByteArray()
            val expectedResult = mock<ValidationNode>()
            val testStep: RmqTestStep = RmqTestStep.Builder()
                .respondTo(setOf(CHANNEL_NAME))
                .readFrom(setOf(CHANNEL_NAME))
                .withTestData("hi")
                .withName("n1")
                .withValidationDetails(expectedResult)
                .build()
            whenever(rmqChannel!!.read(ArgumentMatchers.any())).thenReturn(response)
            doThrow(ValidationException("oops")).whenever(expectedResult)
                .validate(argThat { args -> (args as Map<*, *>)[CHANNEL_NAME] == response })

            processor!!.process(testStep)

            verify(rmqChannel, Mockito.times(0)).send(testStep.testData, testStep.routingKey)
        }
    }
}