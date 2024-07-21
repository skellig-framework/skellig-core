package org.skellig.teststep.processor.tcp

import org.skellig.teststep.processing.exception.TestStepProcessingException
import org.skellig.teststep.processing.exception.ValidationException
import org.skellig.teststep.processing.processor.TestStepProcessor
import org.skellig.teststep.processing.processor.ValidatableTestStepProcessor
import org.skellig.teststep.processing.state.TestScenarioState
import org.skellig.teststep.processing.util.debug
import org.skellig.teststep.processing.util.info
import org.skellig.teststep.processing.util.logger
import org.skellig.teststep.processor.tcp.model.TcpConsumableTestStep

/**
 * Test step processor for consuming messages from TCP channels.
 * It starts up the consume process and returns the [TestStepProcessor.TestStepRunResult] immediately so others can subscribe
 * to result events coming from each occurrence of consumed message.
 *
 * @constructor Creates a TcpConsumableTestStepProcessor with the provided TCP channels and test scenario state.
 * @param tcpChannels A map of TCP channels, where the key is the channel [ID][org.skellig.teststep.processor.tcp.model.TcpDetails.id] and the value is the corresponding [TcpChannel] object.
 * @param testScenarioState The test scenario state object.
 */
open class TcpConsumableTestStepProcessor(
    protected val tcpChannels: Map<String, TcpChannel>,
    testScenarioState: TestScenarioState?,
) : ValidatableTestStepProcessor<TcpConsumableTestStep>(testScenarioState!!) {

    private val log = logger<TcpConsumableTestStepProcessor>()

    override fun process(testStep: TcpConsumableTestStep): TestStepProcessor.TestStepRunResult {
        val testStepRunResult = TestStepProcessor.TestStepRunResult(testStep)
        testScenarioState.set(testStep.getId, testStep)

        log.info(testStep, "Start to consume messages for test step '${testStep.name}' from TCP channels ${testStep.consumeFrom}")
        consume(testStep, testStep.consumeFrom, testStepRunResult)

        return testStepRunResult
    }

    /**
     * Consumes data from TCP channels and optionally responds to a corresponding channel (see [TcpConsumableTestStep.respondTo]).
     * The response occurs only if [TcpConsumableTestStep.respondTo] channels are provided and
     * the received message is valid according to [TcpConsumableTestStep.validationDetails].
     *
     * If exception occurs when message is validated or response fails, then it notifies the subscribers to [TestStepProcessor.TestStepRunResult]
     * and consume process is resumed.
     *
     * @param testStep The TCP consumable test step.
     * @param channels The list of channels to consume data from.
     * @param result The test step run result. Used to notify subscribers for each consume result.
     */
    private fun consume(
        testStep: TcpConsumableTestStep,
        channels: List<String>,
        result: TestStepProcessor.TestStepRunResult
    ) {
        val respondTo = testStep.respondTo
        val response = testStep.testData
        channels.forEachIndexed { index, id ->
            val channel = tcpChannels[id] ?: error(getChannelNotExistErrorMessage(id))
            channel.consume(
                testStep.timeout, testStep.readBufferSize
            ) { receivedMessage ->
                log.debug(testStep) { "Received message from TCP channel '$id': $receivedMessage" }
                var error: RuntimeException? = null
                try {
                    validate(testStep, receivedMessage)
                    if (respondTo != null && response != null) {
                        log.debug(testStep) { "Respond to received data to TCP channels '$respondTo'" }
                        send(response, respondTo[index])
                    }
                } catch (ex: Exception) {
                    error = when (ex) {
                        is ValidationException, is TestStepProcessingException -> ex as RuntimeException
                        else -> TestStepProcessingException(ex.message, ex)
                    }
                } finally {
                    result.notify(receivedMessage, error)
                }
            }
        }
    }

    private fun send(testData: Any, respondTo: String) {
        val channel = tcpChannels[respondTo] ?: error(getChannelNotExistErrorMessage(respondTo))
        channel.send(testData)
    }

    override fun close() {
        tcpChannels.values.forEach { it.close() }
    }

    private fun getChannelNotExistErrorMessage(id: String) =
        "Channel '$id' was not registered in Tcp Test Step Processor"

    override fun getTestStepClass(): Class<TcpConsumableTestStep> {
        return TcpConsumableTestStep::class.java
    }

    class Builder : BaseTcpProcessorBuilder<TcpConsumableTestStep>() {
        override fun build(): TestStepProcessor<TcpConsumableTestStep> {
            return TcpConsumableTestStepProcessor(tcpChannels, testScenarioState)
        }
    }
}