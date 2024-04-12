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
                if (respondTo != null) null else response,
                testStep.timeout, testStep.readBufferSize
            ) { receivedMessage ->
                log.debug(testStep) { "Received message from TCP channel '$id': $receivedMessage" }
                var error: RuntimeException? = null
                try {
                    validate(testStep, receivedMessage)
                    respondTo?.let {
                        response?.let {
                            log.debug(testStep) { "Respond to received message to TCP channels '$respondTo'" }
                            send(response, respondTo[index])
                        }
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