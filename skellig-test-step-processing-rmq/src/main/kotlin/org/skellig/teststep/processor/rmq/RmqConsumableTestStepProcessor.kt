package org.skellig.teststep.processor.rmq

import com.rabbitmq.client.AMQP
import org.skellig.teststep.processing.exception.TestStepProcessingException
import org.skellig.teststep.processing.exception.ValidationException
import org.skellig.teststep.processing.processor.TestStepProcessor
import org.skellig.teststep.processing.processor.ValidatableTestStepProcessor
import org.skellig.teststep.processing.state.TestScenarioState
import org.skellig.teststep.processing.util.debug
import org.skellig.teststep.processing.util.info
import org.skellig.teststep.processing.util.logger
import org.skellig.teststep.processor.rmq.model.RmqConsumableTestStep

open class RmqConsumableTestStepProcessor(
    protected val rmqChannels: Map<String, RmqChannel>,
    testScenarioState: TestScenarioState?,
) : ValidatableTestStepProcessor<RmqConsumableTestStep>(testScenarioState!!) {

    private val log = logger<RmqConsumableTestStepProcessor>()

    override fun process(testStep: RmqConsumableTestStep): TestStepProcessor.TestStepRunResult {
        val testStepRunResult = TestStepProcessor.TestStepRunResult(testStep)
        testScenarioState.set(testStep.getId, testStep)

        log.info(testStep, "Start to consume messages for test step '${testStep.name}' from RMQ queues ${testStep.consumeFrom}")
        consume(testStep, testStep.consumeFrom, testStepRunResult)

        return testStepRunResult
    }

    private fun consume(
        testStep: RmqConsumableTestStep,
        channels: List<String>,
        result: TestStepProcessor.TestStepRunResult
    ) {
        val respondTo = testStep.respondTo
        val response = testStep.testData
        channels.forEachIndexed { index, channelName ->
            val channel = rmqChannels[channelName] ?: error(getChannelNotExistErrorMessage(channelName))
            channel.consume(if (respondTo != null) null else response) { receivedMessage ->
                log.debug(testStep) { "Received message from RMQ queue '${channelName}': $receivedMessage" }
                var error: RuntimeException? = null
                try {
                    validate(testStep, receivedMessage)
                } catch (ex: Exception) {
                    error = when (ex) {
                        is ValidationException, is TestStepProcessingException -> ex as RuntimeException
                        else -> TestStepProcessingException(ex.message, ex)
                    }
                } finally {
                    result.notify(receivedMessage, error)
                }

                respondTo?.let {
                    response?.let {
                        log.debug(testStep) { "Respond to received message to RMQ queues '$respondTo'" }
                        send(response, respondTo[index], testStep.routingKey, testStep.getAmqpProperties())
                    }
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
        "Channel '$channelId' was not registered in RMQ Test Step Processor"

    override fun getTestStepClass(): Class<RmqConsumableTestStep> {
        return RmqConsumableTestStep::class.java
    }

    class Builder : BaseRmqProcessorBuilder<RmqConsumableTestStep>() {
        override fun build(): TestStepProcessor<RmqConsumableTestStep> {
            return RmqConsumableTestStepProcessor(rmqChannels, testScenarioState)
        }
    }
}