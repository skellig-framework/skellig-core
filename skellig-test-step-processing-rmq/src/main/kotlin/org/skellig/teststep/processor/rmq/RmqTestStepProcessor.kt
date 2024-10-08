package org.skellig.teststep.processor.rmq

import com.rabbitmq.client.AMQP
import org.skellig.teststep.processing.processor.BaseTestStepProcessor
import org.skellig.teststep.processing.processor.TestStepProcessor
import org.skellig.teststep.processing.state.TestScenarioState
import org.skellig.teststep.processing.util.debug
import org.skellig.teststep.processing.util.info
import org.skellig.teststep.processing.util.logger
import org.skellig.teststep.processor.rmq.model.RmqTestStep

/**
 * RmqTestStepProcessor is a class that processes RMQ test steps.
 * It processes the test step this way:
 * 1) Sends a message ([RmqTestStep.testData]) to [RmqTestStep.sendTo]
 * 2) Reads a message ([RmqTestStep.testData]) from [RmqTestStep.readFrom]. If [RmqTestStep.respondTo] defined, then
 * responds with message ([RmqTestStep.testData]) to [RmqTestStep.respondTo] if the received message is valid
 * based on [RmqTestStep.validationDetails].
 *
 * All send, read and respond operations are performed in parallel per each channel but blocked until they are all finished.
 *
 * @param rmqChannels A map of RMQ channels where the key is the channel ID and the value is the [RmqChannel] instance.
 * @param testScenarioState The state of the test scenario.
 */
open class RmqTestStepProcessor(
    protected val rmqChannels: Map<String, RmqChannel>,
    testScenarioState: TestScenarioState?
) : BaseTestStepProcessor<RmqTestStep>(testScenarioState!!) {

    private val log = logger<RmqTestStepProcessor>()

    override fun processTestStep(testStep: RmqTestStep): Any? {
        var response: Map<*, Any?>? = null
        val sendTo = testStep.sendTo
        val readFrom = testStep.readFrom
        val routingKey = testStep.routingKey

        sendTo?.let {
            log.info(testStep, "Start to send message of test step '${testStep.name}' to RMQ queues $sendTo")
            send(testStep, it, routingKey, testStep.getAmqpProperties())
        }

        readFrom?.let {
            log.info(testStep, "Start to read message of test step '${testStep.name}' from RMQ queues $readFrom")
            val respondTo = testStep.respondTo
            response = read(testStep, readFrom, if (respondTo != null) null else testStep.testData)
            respondTo?.let {
                if (isValid(testStep, response)) {
                    log.info(testStep, "Respond to received message to RMQ queues '$respondTo'")
                    send(testStep, respondTo, routingKey, testStep.getAmqpProperties())
                } else {
                    log.warn("Can't respond to '{}' queues because the response is not valid: {}", respondTo, response)
                }
            }
        }
        return response
    }

    private fun read(testStep: RmqTestStep, channels: Set<String?>, responseTestData: Any? = null): Map<*, Any?> {
        val tasks = channels.associateWith {
            {
                val channel = rmqChannels[it] ?: error(getChannelNotExistErrorMessage(it))
                log.debug(testStep) { "Start to read message from RMQ queue '$it'" }
                val message = channel.read(responseTestData)
                log.debug(testStep) { "Received message from RMQ queue '$it'" }
                message
            }
        }
        return runTasksAsyncAndWait(tasks, testStep)
    }

    private fun send(testStep: RmqTestStep, channels: Set<String>, routingKey: String?, properties: AMQP.BasicProperties?) {
        val tasks = channels.associateWith {
            {
                val channel = rmqChannels[it] ?: error(getChannelNotExistErrorMessage(it))
                log.debug(testStep) { "Send message to RMQ queue '$it'" }
                channel.send(testStep.testData, routingKey, properties)
                log.debug(testStep) { "Message has sent to RMQ queue '$it'" }
                "sent"
            }
        }
        runTasksAsyncAndWait(tasks, testStep)
    }

    override fun close() {
        log.info("Close RMQ Test Step Processor and all connections to queues")
        rmqChannels.values.forEach { it.close() }
    }

    override fun getTestStepClass(): Class<RmqTestStep> {
        return RmqTestStep::class.java
    }

    private fun getChannelNotExistErrorMessage(channelId: String?) =
        "Channel '$channelId' was not registered in RMQ Test Step Processor"

    class Builder : BaseRmqProcessorBuilder<RmqTestStep>() {
        override fun build(): TestStepProcessor<RmqTestStep> {
            return RmqTestStepProcessor(rmqChannels, testScenarioState)
        }
    }
}