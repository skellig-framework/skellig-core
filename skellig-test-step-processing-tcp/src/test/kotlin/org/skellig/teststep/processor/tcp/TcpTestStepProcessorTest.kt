package org.skellig.teststep.processor.tcp

import com.nhaarman.mockitokotlin2.*
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.assertTrue
import org.mockito.ArgumentMatchers
import org.mockito.Mockito
import org.skellig.teststep.processing.exception.ValidationException
import org.skellig.teststep.processing.model.ValidationNode
import org.skellig.teststep.processing.processor.TestStepProcessor
import org.skellig.teststep.processing.state.TestScenarioState
import org.skellig.teststep.processor.tcp.model.TcpTestStep
import java.util.*
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicReference

internal class TcpTestStepProcessorTest {

    companion object {
        private const val CHANNEL_ID = "host1"
        private const val CHANNEL_ID_2 = "host2"
    }

    private var processor: TestStepProcessor<TcpTestStep>? = null
    private var tcpChannel = Mockito.mock(TcpChannel::class.java)
    private var tcpChannel2 = Mockito.mock(TcpChannel::class.java)
    private var testScenarioState: TestScenarioState? = null

    @BeforeEach
    fun setUp() {
        val tcpChannels = mapOf(
                Pair(CHANNEL_ID, tcpChannel),
                Pair(CHANNEL_ID_2, tcpChannel2))

        testScenarioState = Mockito.mock(TestScenarioState::class.java)
        processor = TcpTestStepProcessor(tcpChannels, testScenarioState)
    }

    @Test
    @DisplayName("Send data to non-registered channel Then verify exception is captured")
    fun testSendToChannelNotRegistered() {
        val testStep = TcpTestStep.Builder()
                .sendTo(setOf("host3"))
                .withTestData("hi")
                .withName("n1")
                .build()
        val ref: AtomicReference<Exception> = AtomicReference<Exception>()

        processor!!.process(testStep).subscribe { _, _, e -> ref.set(e) }

        Assertions.assertEquals("Channel 'host3' was not registered in TCP Test Step Processor", ref.get().message)
    }

    @Nested
    internal inner class SendAndReceiveTest {
        @Test
        @DisplayName("Send data Then verify tcp channel is called")
        fun testSendData() {
            val testStep = TcpTestStep.Builder()
                    .sendTo(setOf(CHANNEL_ID))
                    .withTestData("hi")
                    .withName("n1")
                    .build()

            val result = processor!!.process(testStep)

            Assertions.assertNotNull(result)
            Mockito.verify(tcpChannel).send(testStep.testData)
        }

        @Test
        @DisplayName("Send and receive data Then verify tcp channel is called and returned response")
        fun testSendAndReceive() {
            val response = "yo"
            val testStep = TcpTestStep.Builder()
                    .sendTo(setOf(CHANNEL_ID))
                    .readFrom(setOf(CHANNEL_ID))
                    .withTestData("hi")
                    .withName("n1")
                    .build()
            whenever(tcpChannel!!.read(ArgumentMatchers.anyInt(), ArgumentMatchers.anyInt())).thenReturn(response)

            val isPassed = AtomicBoolean()
            processor!!.process(testStep)
                    .subscribe { _, r, _ ->
                        Assertions.assertEquals(response, (r as Map<*,*>)[CHANNEL_ID])
                        isPassed.set(true)
                    }

            assertAll(
                    { assertTrue(isPassed.get()) },
                    {
                        verify(testScenarioState!!).set(eq(testStep.getId + "_result"),
                                argThat { args -> (args as Map<*, *>).containsKey(CHANNEL_ID) })
                    }
            )
        }

        @Test
        @DisplayName("Receive and respond to different channel Then verify tcp channel is called to respond")
        fun testReceiveAndRespondToDifferentChannel() {
            val testStep = TcpTestStep.Builder()
                    .respondTo(setOf(CHANNEL_ID_2))
                    .readFrom(setOf(CHANNEL_ID))
                    .withTestData("hi")
                    .withName("n1")
                    .build()
            whenever(tcpChannel!!.read(ArgumentMatchers.anyInt(), ArgumentMatchers.anyInt())).thenReturn(Optional.of("yo"))

            processor!!.process(testStep)

            Mockito.verify(tcpChannel2).send(testStep.testData)
        }

        @Test
        @DisplayName("Receive invalid response And try to respond Then verify tcp channel did not respond")
        fun testReceiveInvalidAndTryRespond() {
            val response = "yo"
            val expectedResult = mock<ValidationNode>()
            val testStep = TcpTestStep.Builder()
                    .respondTo(setOf(CHANNEL_ID))
                    .readFrom(setOf(CHANNEL_ID))
                    .withTestData("hi")
                    .withName("n1")
                    .withValidationDetails(expectedResult)
                    .build()
            whenever(tcpChannel!!.read(ArgumentMatchers.anyInt(), ArgumentMatchers.anyInt())).thenReturn(response)
            doThrow(ValidationException("oops")).whenever(expectedResult)
                .validate(argThat { args -> (args as Map<*, *>)[CHANNEL_ID] == response })

            processor!!.process(testStep)

            verify(tcpChannel, Mockito.times(0)).send(testStep.testData)
        }
    }
}