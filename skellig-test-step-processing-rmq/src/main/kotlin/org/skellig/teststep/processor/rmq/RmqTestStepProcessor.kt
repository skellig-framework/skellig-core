package org.skellig.teststep.processor.rmq

import com.typesafe.config.Config
import org.skellig.teststep.processing.converter.TestStepResultConverter
import org.skellig.teststep.processing.exception.TestStepProcessingException
import org.skellig.teststep.processing.processor.BaseTestStepProcessor
import org.skellig.teststep.processing.processor.TestStepProcessor
import org.skellig.teststep.processing.state.TestScenarioState
import org.skellig.teststep.processing.validation.TestStepResultValidator
import org.skellig.teststep.processor.rmq.model.RmqDetails
import org.skellig.teststep.processor.rmq.model.RmqTestStep
import java.util.function.Consumer

open class RmqTestStepProcessor(protected val rmqChannels: Map<String, RmqChannel>,
                                testScenarioState: TestScenarioState?,
                                validator: TestStepResultValidator?,
                                testStepResultConverter: TestStepResultConverter?)
    : BaseTestStepProcessor<RmqTestStep>(testScenarioState!!, validator!!, testStepResultConverter) {

    protected override fun processTestStep(testStep: RmqTestStep): Any? {
        var response: Any? = null
        val sendTo = testStep.sendTo
        val receiveFrom = testStep.receiveFrom
        val routingKey = testStep.routingKey
        sendTo?.let { send(testStep.testData, it, routingKey) }

        receiveFrom?.let {
            val channel = rmqChannels[receiveFrom]
            val respondTo = testStep.respondTo
            val responseTestData = testStep.testData
            response = channel!!.read(if (respondTo != null) null else responseTestData, testStep.timeout)

            validate(testStep, response)

            respondTo?.let { send(responseTestData, it, routingKey) }
        }
        return response
    }

    private fun send(testData: Any?, channelId: String?, routingKey: String?) {
        if (rmqChannels.containsKey(channelId)) {
            rmqChannels[channelId]?.send(testData, routingKey)
        } else {
            throw TestStepProcessingException(String.format("Channel '%s' was not registered " +
                    "in RMQ Test Step Processor", channelId))
        }
    }

    override fun close() {
        rmqChannels.values.forEach(Consumer { obj: RmqChannel -> obj.close() })
    }

    override fun getTestStepClass(): Class<RmqTestStep> {
        return RmqTestStep::class.java
    }

    class Builder : BaseTestStepProcessor.Builder<RmqTestStep>() {

        private val rmqChannels = hashMapOf<String, RmqChannel>()
        private val rmqDetailsConfigReader: RmqDetailsConfigReader = RmqDetailsConfigReader()

        fun withRmqChannel(rmqDetails: RmqDetails) = apply {
            rmqChannels.putIfAbsent(rmqDetails.channelId, RmqChannel(rmqDetails))
        }

        fun withRmqChannels(config: Config) = apply {
            rmqDetailsConfigReader.read(config).filterNotNull().forEach { withRmqChannel(it) }
        }

        override fun build(): TestStepProcessor<RmqTestStep> {
            return RmqTestStepProcessor(rmqChannels, testScenarioState, validator, testStepResultConverter)
        }
    }
}