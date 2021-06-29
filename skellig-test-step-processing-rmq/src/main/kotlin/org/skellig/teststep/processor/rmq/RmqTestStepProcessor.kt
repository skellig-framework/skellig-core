package org.skellig.teststep.processor.rmq

import com.rabbitmq.client.AMQP
import com.typesafe.config.Config
import org.skellig.task.async.AsyncTaskUtils.Companion.runTasksAsyncAndWait
import org.skellig.teststep.processing.converter.TestStepResultConverter
import org.skellig.teststep.processing.processor.BaseTestStepProcessor
import org.skellig.teststep.processing.processor.TestStepProcessor
import org.skellig.teststep.processing.state.TestScenarioState
import org.skellig.teststep.processing.validation.TestStepResultValidator
import org.skellig.teststep.processor.rmq.model.RmqDetails
import org.skellig.teststep.processor.rmq.model.RmqTestStep
import java.util.*

open class RmqTestStepProcessor(
        protected val rmqChannels: Map<String, RmqChannel>,
        testScenarioState: TestScenarioState?,
        validator: TestStepResultValidator?,
        testStepResultConverter: TestStepResultConverter?
) : BaseTestStepProcessor<RmqTestStep>(testScenarioState!!, validator!!, testStepResultConverter) {

    protected override fun processTestStep(testStep: RmqTestStep): Any? {
        var response: Map<*, Any?>? = null
        val sendTo = testStep.sendTo
        val receiveFrom = testStep.receiveFrom
        val routingKey = testStep.routingKey
        sendTo?.let { send(testStep.testData, it, routingKey, testStep.getAmqpProperties()) }

        receiveFrom?.let {
            val respondTo = testStep.respondTo
            val responseTestData = testStep.testData
            response = read(testStep, receiveFrom, if (respondTo != null) null else responseTestData)
            respondTo?.let {
                if (isValid(testStep, response)) send(responseTestData, respondTo, routingKey, testStep.getAmqpProperties())
            }
        }
        return response
    }

    private fun read(testStep: RmqTestStep, channels: Set<String?>, responseTestData: Any? = null): Map<*, Any?> {
        val tasks = channels
                .map {
                    it to {
                        val channel = rmqChannels[it] ?: error(getChannelNotExistErrorMessage(it))
                        channel.read(responseTestData)
                    }
                }
                .toMap()
        return runTasksAsyncAndWait(
                tasks,
                { isValid(testStep, it) },
                testStep.delay,
                testStep.attempts,
                testStep.timeout
        )
    }

    private fun send(testData: Any?, channels: Set<String>, routingKey: String?, properties: AMQP.BasicProperties?) {
        val tasks = channels
                .map {
                    it to {
                        val channel = rmqChannels[it] ?: error(getChannelNotExistErrorMessage(it))
                        channel.send(testData, routingKey, properties)
                        "sent"
                    }
                }
                .toMap()
        runTasksAsyncAndWait(tasks)
    }

    override fun close() {
        rmqChannels.values.forEach { it.close() }
    }

    override fun getTestStepClass(): Class<RmqTestStep> {
        return RmqTestStep::class.java
    }

    private fun getChannelNotExistErrorMessage(channelId: String?) =
            "Channel '$channelId' was not registered in RMQ Test Step Processor"

    class Builder : BaseRmqProcessorBuilder<RmqTestStep>() {
        override fun build(): TestStepProcessor<RmqTestStep> {
            return RmqTestStepProcessor(rmqChannels, testScenarioState, validator, testStepResultConverter)
        }
    }
}