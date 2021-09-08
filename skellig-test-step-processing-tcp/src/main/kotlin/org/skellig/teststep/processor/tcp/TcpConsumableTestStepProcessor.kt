package org.skellig.teststep.processor.tcp

import org.skellig.teststep.processing.converter.TestStepResultConverter
import org.skellig.teststep.processing.exception.TestStepProcessingException
import org.skellig.teststep.processing.exception.ValidationException
import org.skellig.teststep.processing.processor.TestStepProcessor
import org.skellig.teststep.processing.processor.ValidatableTestStepProcessor
import org.skellig.teststep.processing.state.TestScenarioState
import org.skellig.teststep.processing.validation.TestStepResultValidator
import org.skellig.teststep.processor.tcp.model.TcpConsumableTestStep
import org.slf4j.LoggerFactory

open class TcpConsumableTestStepProcessor(
    protected val tcpChannels: Map<String, TcpChannel>,
    testScenarioState: TestScenarioState?,
    validator: TestStepResultValidator?,
    testStepResultConverter: TestStepResultConverter?
) : ValidatableTestStepProcessor<TcpConsumableTestStep>(testScenarioState!!, validator!!, testStepResultConverter) {

    companion object {
        private val LOGGER = LoggerFactory.getLogger(TcpConsumableTestStepProcessor::class.java)
    }

    override fun process(testStep: TcpConsumableTestStep): TestStepProcessor.TestStepRunResult {
        val testStepRunResult = TestStepProcessor.TestStepRunResult(testStep)
        testScenarioState.set(testStep.getId, testStep)

        consume(testStep, testStep.consumeFrom, testStepRunResult)

        return testStepRunResult
    }

    private fun consume(testStep: TcpConsumableTestStep,
                        channels: List<String>,
                        result: TestStepProcessor.TestStepRunResult) {
        val respondTo = testStep.respondTo
        val response = testStep.testData
        channels.forEachIndexed { index, channelName ->
            val channel = tcpChannels[channelName] ?: error(getChannelNotExistErrorMessage(channelName))
            channel.consume(if (respondTo != null) null else response,
                            testStep.timeout, testStep.readBufferSize) { receivedMessage ->
                var error: RuntimeException? = null
                try {
                    validate(testStep, receivedMessage)
                    respondTo?.let {
                        response?.let {
                            if (isValid(testStep, receivedMessage)) send(response, respondTo[index])
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

    private fun getChannelNotExistErrorMessage(channelId: String) =
        "Channel '$channelId' was not registered in Tcp Test Step Processor"

    override fun getTestStepClass(): Class<TcpConsumableTestStep> {
        return TcpConsumableTestStep::class.java
    }

    class Builder : BaseTcpProcessorBuilder<TcpConsumableTestStep>() {
        override fun build(): TestStepProcessor<TcpConsumableTestStep> {
            return TcpConsumableTestStepProcessor(tcpChannels, testScenarioState, validator, testStepResultConverter)
        }
    }
}