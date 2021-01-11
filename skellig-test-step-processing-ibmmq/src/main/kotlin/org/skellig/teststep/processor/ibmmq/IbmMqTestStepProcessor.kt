package org.skellig.teststep.processor.ibmmq

import com.typesafe.config.Config
import org.skellig.teststep.processing.converter.TestStepResultConverter
import org.skellig.teststep.processing.exception.TestStepProcessingException
import org.skellig.teststep.processing.processor.BaseTestStepProcessor
import org.skellig.teststep.processing.processor.TestStepProcessor
import org.skellig.teststep.processing.state.TestScenarioState
import org.skellig.teststep.processing.validation.TestStepResultValidator
import org.skellig.teststep.processor.ibmmq.model.IbmMqQueueDetails
import org.skellig.teststep.processor.ibmmq.model.IbmMqTestStep

open class IbmMqTestStepProcessor protected constructor(testScenarioState: TestScenarioState?,
                                                        validator: TestStepResultValidator?,
                                                        testStepResultConverter: TestStepResultConverter?,
                                                        private val ibmMqChannels: Map<String, IbmMqChannel>)
    : BaseTestStepProcessor<IbmMqTestStep>(testScenarioState!!, validator!!, testStepResultConverter) {

    protected override fun processTestStep(testStep: IbmMqTestStep): Any? {
        var response: Any? = null
        val sendTo = testStep.sendTo
        val receiveFrom = testStep.receiveFrom
        val respondTo = testStep.respondTo

        sendTo?.let { send(testStep.testData, it) }

        receiveFrom?.let {
            response = ibmMqChannels[receiveFrom]?.read(testStep.timeout)
            validate(testStep, response)

            respondTo?.let { send(testStep.testData, respondTo) }
        }
        return response
    }

    private fun send(testData: Any?, channelId: String) {
        testData?.let {
            if (ibmMqChannels.containsKey(channelId)) {
                ibmMqChannels[channelId]?.send(testData)
            } else {
                throw TestStepProcessingException(String.format("Channel '%s' was not registered " +
                        "in IBMMQ Test Step Processor", channelId))
            }
        }
    }

    override fun getTestStepClass(): Class<IbmMqTestStep> {
        return IbmMqTestStep::class.java
    }

    class Builder : BaseTestStepProcessor.Builder<IbmMqTestStep>() {

        private val ibmMqChannels = mutableMapOf<String, IbmMqChannel>()

        fun withIbmMqChannel(mqQueueDetails: IbmMqQueueDetails) = apply {
            ibmMqChannels.putIfAbsent(mqQueueDetails.channelId, IbmMqChannel(mqQueueDetails))
        }

        fun withIbmMqChannels(config: Config?) = apply {
            return this
        }

        override fun build(): TestStepProcessor<IbmMqTestStep> {
            return IbmMqTestStepProcessor(testScenarioState, validator, testStepResultConverter, ibmMqChannels)
        }
    }
}