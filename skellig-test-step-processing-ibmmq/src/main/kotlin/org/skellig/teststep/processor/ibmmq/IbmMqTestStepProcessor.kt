package org.skellig.teststep.processor.ibmmq

import com.typesafe.config.Config
import org.skellig.task.async.AsyncTaskUtils.Companion.runTasksAsyncAndWait
import org.skellig.teststep.processing.converter.TestStepResultConverter
import org.skellig.teststep.processing.processor.BaseTestStepProcessor
import org.skellig.teststep.processing.processor.TestStepProcessor
import org.skellig.teststep.processing.state.TestScenarioState
import org.skellig.teststep.processing.validation.TestStepResultValidator
import org.skellig.teststep.processor.ibmmq.model.IbmMqQueueDetails
import org.skellig.teststep.processor.ibmmq.model.IbmMqTestStep

open class IbmMqTestStepProcessor protected constructor(
    testScenarioState: TestScenarioState?,
    validator: TestStepResultValidator?,
    testStepResultConverter: TestStepResultConverter?,
    private val ibmMqChannels: Map<String, IbmMqChannel>
) : BaseTestStepProcessor<IbmMqTestStep>(testScenarioState!!, validator!!, testStepResultConverter) {

    protected override fun processTestStep(testStep: IbmMqTestStep): Any? {
        var response: Map<*, Any?>? = null
        val sendTo = testStep.sendTo
        val readFrom = testStep.readFrom
        val respondTo = testStep.respondTo

        sendTo?.let { send(testStep.testData, it) }

        readFrom?.let {
            response = read(testStep, readFrom)

            respondTo?.let {
                if (isValid(testStep, response)) send(testStep.testData, respondTo)
            }
        }
        return response
    }

    private fun read(testStep: IbmMqTestStep, channels: Set<String?>): Map<*, Any?> {
        val tasks = channels
            .map {
                it to {
                    val channel = ibmMqChannels[it] ?: error(getChannelNotExistErrorMessage(it))
                    channel.read(testStep.timeout)
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

    private fun send(testData: Any?, channels: Set<String>) {
        testData?.let {
            val tasks = channels
                .map {
                    it to {
                        val channel = ibmMqChannels[it] ?: error(getChannelNotExistErrorMessage(it))
                        channel.send(testData)
                    }
                }
                .toMap()
            runTasksAsyncAndWait(tasks)
        }
    }

    private fun getChannelNotExistErrorMessage(channelId: String?) =
        "Channel '$channelId' was not registered in IBM MQ Test Step Processor"

    override fun getTestStepClass(): Class<IbmMqTestStep> {
        return IbmMqTestStep::class.java
    }

    class Builder : BaseTestStepProcessor.Builder<IbmMqTestStep>() {

        private val ibmMqChannels = mutableMapOf<String, IbmMqChannel>()
        private val ibmmqDetailsConfigReader = IbmmqDetailsConfigReader()

        fun ibmMqChannel(mqQueueDetails: IbmMqQueueDetails) = apply {
            ibmMqChannels.putIfAbsent(mqQueueDetails.queueName, IbmMqChannel(mqQueueDetails))
        }

        fun ibmMqChannels(config: Config?) = apply {
            ibmmqDetailsConfigReader.read(config).forEach { ibmMqChannel(it) }
        }

        override fun build(): TestStepProcessor<IbmMqTestStep> {
            return IbmMqTestStepProcessor(testScenarioState, validator, testStepResultConverter, ibmMqChannels)
        }
    }
}