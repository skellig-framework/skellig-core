package org.skellig.teststep.processor.rmq

import com.rabbitmq.client.AMQP
import org.skellig.teststep.processing.exception.ValidationException
import org.skellig.teststep.processing.processor.TestStepProcessor
import org.skellig.teststep.processing.processor.ValidatableTestStepProcessor
import org.skellig.teststep.processing.state.TestScenarioState
import org.skellig.teststep.processing.util.debug
import org.skellig.teststep.processing.util.info
import org.skellig.teststep.processing.util.logger
import org.skellig.teststep.processor.rmq.model.RmqConsumableTestStep

/**
 * Test step processor for consuming messages from RMQ queues.
 * It starts up the consume process and returns the [TestStepProcessor.TestStepRunResult] immediately so others can subscribe
 * to result events coming from each occurrence of consumed message.
 *
 * @constructor Creates a RmqConsumableTestStepProcessor with the provided RMQ channels and test scenario state.
 * @param rmqChannels A map of RMQ queues, where the key is the queue [ID][org.skellig.teststep.processor.rmq.model.RmqQueueDetails.id] and the value is the corresponding [RmqChannel] object.
 * @param testScenarioState The test scenario state object.
 */
open class RmqConsumableTestStepProcessor(
    protected val rmqChannels: Map<String, RmqChannel>,
    testScenarioState: TestScenarioState?,
) : ValidatableTestStepProcessor<RmqConsumableTestStep>(testScenarioState!!) {

    private val log = logger<RmqConsumableTestStepProcessor>()

    override fun process(testStep: RmqConsumableTestStep): TestStepProcessor.TestStepRunResult {
        val testStepRunResult = TestStepProcessor.TestStepRunResult(testStep)
        testScenarioState.set(testStep.getId, testStep)

        log.info(testStep, "Start to consume messages for test step '${testStep.name}' from RMQ queues ${testStep.consumeFrom}")
        consume(testStep, testStepRunResult)

        return testStepRunResult
    }

    /**
     * Consumes data from RMQ queues and optionally responds to a corresponding queue (see [RmqConsumableTestStep.respondTo]).
     * The response occurs only if [RmqConsumableTestStep.respondTo] queues are provided and
     * the received message is valid according to [RmqConsumableTestStep.validationDetails].
     *
     * If exception occurs when message is validated or response fails, then it notifies the subscribers to [TestStepProcessor.TestStepRunResult]
     * and consume process is resumed.
     *
     * @param testStep The RMQ consumable test step.
     * @param result The test step run result. Used to notify subscribers for each consume result.
     */
    private fun consume(
        testStep: RmqConsumableTestStep,
        result: TestStepProcessor.TestStepRunResult
    ) {
        val respondTo = testStep.respondTo
        val response = testStep.testData
        testStep.consumeFrom.forEachIndexed { index, channelName ->
            val channel = rmqChannels[channelName] ?: error(getChannelNotExistErrorMessage(channelName))
            channel.consume(if (respondTo != null) null else response) { receivedMessage ->
                log.debug(testStep) { "Received message from RMQ queue '${channelName}': ${String(receivedMessage)}" }
                var error: RuntimeException? = null
                try {
                    validate(testStep, receivedMessage)
                } catch (ex: Throwable) {
                    error = when (ex) {
                        !is ValidationException -> ValidationException(ex.message, ex)
                        else -> ex
                    }
                } finally {
                    result.notify(receivedMessage, error)
                }

                if (error == null && respondTo != null && response != null) {
                    log.debug(testStep) { "Respond to received message to RMQ queues '$respondTo'" }
                    send(response, respondTo[index], testStep.routingKey, testStep.getAmqpProperties())
                }
            }
        }
    }

    private fun send(testData: Any?, sendTo: String, routingKey: String?, properties: AMQP.BasicProperties?) {
        val channel = rmqChannels[sendTo] ?: error(getChannelNotExistErrorMessage(sendTo))
        channel.send(testData, routingKey, properties)
    }

    override fun close() {
        rmqChannels.values.forEach { it.close() }
    }

    private fun getChannelNotExistErrorMessage(channelId: String) =
        "Channel '$channelId' was not registered in RMQ Consumable Test Step Processor"

    override fun getTestStepClass(): Class<RmqConsumableTestStep> {
        return RmqConsumableTestStep::class.java
    }

    class Builder : BaseRmqProcessorBuilder<RmqConsumableTestStep>() {
        override fun build(): TestStepProcessor<RmqConsumableTestStep> {
            return RmqConsumableTestStepProcessor(rmqChannels, testScenarioState)
        }
    }
}