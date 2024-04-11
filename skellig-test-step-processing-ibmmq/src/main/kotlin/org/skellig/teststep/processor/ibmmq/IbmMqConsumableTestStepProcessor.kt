package org.skellig.teststep.processor.ibmmq

import org.skellig.teststep.processing.exception.TestStepProcessingException
import org.skellig.teststep.processing.exception.ValidationException
import org.skellig.teststep.processing.processor.TestStepProcessor
import org.skellig.teststep.processing.processor.ValidatableTestStepProcessor
import org.skellig.teststep.processing.state.TestScenarioState
import org.skellig.teststep.processing.util.debug
import org.skellig.teststep.processing.util.info
import org.skellig.teststep.processing.util.logger
import org.skellig.teststep.processor.ibmmq.model.IbmMqConsumableTestStep

open class IbmMqConsumableTestStepProcessor(
    protected val ibmMqChannels: Map<String, IbmMqChannel>,
    testScenarioState: TestScenarioState?,
) : ValidatableTestStepProcessor<IbmMqConsumableTestStep>(testScenarioState!!) {

    private val log = logger<IbmMqConsumableTestStepProcessor>()

    override fun process(testStep: IbmMqConsumableTestStep): TestStepProcessor.TestStepRunResult {
        val testStepRunResult = TestStepProcessor.TestStepRunResult(testStep)
        testScenarioState.set(testStep.getId, testStep)

        log.info(testStep, "Start to consume data of test step '${testStep.name}' from IBMMQ queues ${testStep.consumeFrom}")
        consume(testStep, testStep.consumeFrom, testStepRunResult)

        return testStepRunResult
    }

    private fun consume(
        testStep: IbmMqConsumableTestStep,
        channels: List<String>,
        result: TestStepProcessor.TestStepRunResult
    ) {
        val respondTo = testStep.respondTo
        val response = testStep.testData
        channels.forEachIndexed { index, channelName ->
            val channel = ibmMqChannels[channelName] ?: error(getChannelNotExistErrorMessage(channelName))
            channel.consume(if (respondTo != null) null else response, testStep.timeout)
            { receivedMessage ->
                log.debug(testStep) { "Received data from IBMMQ queue '${channelName}': $receivedMessage" }
                var error: RuntimeException? = null
                try {
                    validate(testStep, receivedMessage)
                    respondTo?.let {
                        response?.let {
                            log.info(testStep, "Respond to received data to IBMMQ queues '$respondTo'")
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

    private fun send(testData: Any, sendTo: String) {
        val channel = ibmMqChannels[sendTo] ?: error(getChannelNotExistErrorMessage(sendTo))
        channel.send(testData)
    }

    override fun close() {
        ibmMqChannels.values.forEach { it.close() }
    }

    private fun getChannelNotExistErrorMessage(channelId: String) =
        "Channel '$channelId' was not registered in IbmMq Test Step Processor"

    override fun getTestStepClass(): Class<IbmMqConsumableTestStep> {
        return IbmMqConsumableTestStep::class.java
    }

    class Builder : BaseIbmMqTestStepProcessorBuilder<IbmMqConsumableTestStep>() {
        override fun build(): TestStepProcessor<IbmMqConsumableTestStep> {
            return IbmMqConsumableTestStepProcessor(ibmMqChannels, testScenarioState)
        }
    }
}