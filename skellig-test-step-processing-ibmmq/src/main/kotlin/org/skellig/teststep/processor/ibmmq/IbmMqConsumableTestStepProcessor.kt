package org.skellig.teststep.processor.ibmmq

import org.skellig.teststep.processing.exception.ValidationException
import org.skellig.teststep.processing.processor.TestStepProcessor
import org.skellig.teststep.processing.processor.ValidatableTestStepProcessor
import org.skellig.teststep.processing.state.TestScenarioState
import org.skellig.teststep.processing.util.debug
import org.skellig.teststep.processing.util.info
import org.skellig.teststep.processing.util.logger
import org.skellig.teststep.processor.ibmmq.model.IbmMqConsumableTestStep

/**
 * Test step processor for consuming messages from IBMMQ queues.
 * It starts up the consume process and returns the [TestStepProcessor.TestStepRunResult] immediately so others can subscribe
 * to result events coming from each occurrence of consumed message.
 *
 * @constructor Creates a IbmMqConsumableTestStepProcessor with the provided IBMMQ queues and test scenario state.
 * @param ibmMqChannels A map of IBMMQ queues, where the key is the queue [ID][org.skellig.teststep.processor.ibmmq.model.IbmMqQueueDetails.id]
 * and the value is the corresponding [IbmMqChannel] object.
 * @param testScenarioState The test scenario state object.
 */
open class IbmMqConsumableTestStepProcessor(
    protected val ibmMqChannels: Map<String, IbmMqChannel>,
    testScenarioState: TestScenarioState?,
) : ValidatableTestStepProcessor<IbmMqConsumableTestStep>(testScenarioState!!) {

    private val log = logger<IbmMqConsumableTestStepProcessor>()

    override fun process(testStep: IbmMqConsumableTestStep): TestStepProcessor.TestStepRunResult {
        val testStepRunResult = TestStepProcessor.TestStepRunResult(testStep)
        testScenarioState.set(testStep.getId, testStep)

        log.info(testStep, "Start to consume messages for test step '${testStep.name}' from IBMMQ queues ${testStep.consumeFrom}")
        consume(testStep, testStep.consumeFrom, testStepRunResult)

        return testStepRunResult
    }

    /**
     * Consumes data from IBMMQ queues and optionally responds to a corresponding queue (see [IbmMqConsumableTestStep.respondTo]).
     * The response occurs only if [IbmMqConsumableTestStep.respondTo] queues are provided and
     * the received message is valid according to [IbmMqConsumableTestStep.validationDetails].
     *
     * If exception occurs when message is validated or response fails, then it notifies the subscribers to [TestStepProcessor.TestStepRunResult]
     * and consume process is resumed.
     *
     * @param testStep The IBMMQ consumable test step.
     * @param channels The list of queues to consume data from.
     * @param result The test step run result. Used to notify subscribers for each consume result.
     */
    private fun consume(
        testStep: IbmMqConsumableTestStep,
        channels: List<String>,
        result: TestStepProcessor.TestStepRunResult
    ) {
        val respondTo = testStep.respondTo
        val response = testStep.testData
        channels.forEachIndexed { index, channelName ->
            val channel = ibmMqChannels[channelName] ?: error(getChannelNotExistErrorMessage(channelName))
            channel.consume(testStep.timeout)
            { receivedMessage ->
                log.debug(testStep) { "Received data from IBMMQ queue '${channelName}': $receivedMessage" }
                var error: RuntimeException? = null
                try {
                    validate(testStep, receivedMessage)
                    if (respondTo != null && response != null) {
                        log.debug(testStep) { "Respond to received data to IBMMQ queues '$respondTo'" }
                        send(response, respondTo[index])
                    }
                } catch (ex: Exception) {
                    error = when (ex) {
                        !is ValidationException -> ValidationException(ex.message, ex)
                        else -> ex
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
        ibmMqChannels.values.forEach { it.disconnectQueue() }
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