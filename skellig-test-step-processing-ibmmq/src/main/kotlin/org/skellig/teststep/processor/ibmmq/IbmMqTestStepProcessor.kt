package org.skellig.teststep.processor.ibmmq

import org.skellig.task.async.AsyncTaskUtils.Companion.runTasksAsyncAndWait
import org.skellig.teststep.processing.processor.BaseTestStepProcessor
import org.skellig.teststep.processing.processor.TestStepProcessor
import org.skellig.teststep.processing.state.TestScenarioState
import org.skellig.teststep.processing.util.debug
import org.skellig.teststep.processing.util.info
import org.skellig.teststep.processing.util.logger
import org.skellig.teststep.processor.ibmmq.model.IbmMqTestStep

open class IbmMqTestStepProcessor protected constructor(
    testScenarioState: TestScenarioState?,
    private val ibmMqChannels: Map<String, IbmMqChannel>
) : BaseTestStepProcessor<IbmMqTestStep>(testScenarioState!!) {

    private val log = logger<IbmMqTestStepProcessor>()

    override fun processTestStep(testStep: IbmMqTestStep): Any? {
        var response: Map<*, Any?>? = null
        val sendTo = testStep.sendTo
        val readFrom = testStep.readFrom
        val respondTo = testStep.respondTo

        sendTo?.let {
            log.info(testStep, "Start to send data of test step '${testStep.name}' to IBMMQ queues $sendTo")
            send(testStep, it)
        }

        readFrom?.let {
            log.info(testStep, "Start to read data of test step '${testStep.name}' fro IBMMQ queues $readFrom")
            response = read(testStep, readFrom)
            respondTo?.let {
                if (isValid(testStep, response)) {
                    log.info(testStep, "Respond to received data to IBMMQ queues '$respondTo'")
                    send(testStep, respondTo)
                }
            }
        }
        return response
    }

    private fun read(testStep: IbmMqTestStep, channels: Set<String?>): Map<*, Any?> {
        val tasks = channels.associateWith {
            {
                val channel = ibmMqChannels[it] ?: error(getChannelNotExistErrorMessage(it))
                log.debug(testStep) { "Start to read data from IBMMQ queue '${it}'" }
                val response = channel.read(testStep.timeout)
                log.debug(testStep) { "Received data from IBMMQ queue '${it}'" }

                response
            }
        }
        return runTasksAsyncAndWait(
            tasks,
            { isValid(testStep, it) },
            testStep.delay,
            testStep.attempts,
            testStep.timeout
        )
    }

    private fun send(testStep: IbmMqTestStep, channels: Set<String>) {
        testStep.testData?.let { testData ->
            val tasks = channels.associateWith {
                {
                    val channel = ibmMqChannels[it] ?: error(getChannelNotExistErrorMessage(it))
                    log.debug(testStep) { "Send data to IBMMQ queue '${it}'" }
                    channel.send(testData)
                    log.debug(testStep) { "Data has sent to IBMMQ queue '$it'" }
                }
            }
            runTasksAsyncAndWait(tasks)
        }
    }

    private fun getChannelNotExistErrorMessage(id: String?) =
        "IBMMQ queue '$id' was not registered in IBM MQ Test Step Processor"

    override fun getTestStepClass(): Class<IbmMqTestStep> {
        return IbmMqTestStep::class.java
    }

    class Builder : BaseIbmMqTestStepProcessorBuilder<IbmMqTestStep>() {
        override fun build(): TestStepProcessor<IbmMqTestStep> {
            return IbmMqTestStepProcessor(testScenarioState, ibmMqChannels)
        }
    }
}