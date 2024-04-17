package org.skellig.teststep.processor.ibmmq

import org.skellig.task.async.AsyncTaskUtils.Companion.runTasksAsyncAndWait
import org.skellig.teststep.processing.processor.BaseTestStepProcessor
import org.skellig.teststep.processing.processor.TestStepProcessor
import org.skellig.teststep.processing.state.TestScenarioState
import org.skellig.teststep.processing.util.debug
import org.skellig.teststep.processing.util.info
import org.skellig.teststep.processing.util.logger
import org.skellig.teststep.processor.ibmmq.model.IbmMqTestStep

/**
 * IbmMqTestStepProcessor is a class that processes IBMMQ test steps.
 * It processes the test step this way:
 * 1) Sends a message ([IbmMqTestStep.testData]) to [IbmMqTestStep.sendTo]
 * 2) Reads a message ([IbmMqTestStep.testData]) from [IbmMqTestStep.readFrom]. If [IbmMqTestStep.respondTo] defined, then
 * responds with message ([IbmMqTestStep.testData]) to [IbmMqTestStep.respondTo] if the received message is valid
 * based on [IbmMqTestStep.validationDetails].
 *
 * All send, read and respond operations are performed in parallel per each channel but blocked until they are all finished.
 *
 * @param ibmMqChannels A map of IBMMQ queues where the key is the queue [ID][org.skellig.teststep.processor.ibmmq.model.IbmMqQueueDetails.id]
 * and the value is the [IbmMqChannel] instance.
 * @param testScenarioState The state of the test scenario.
 */
open class IbmMqTestStepProcessor protected constructor(
    testScenarioState: TestScenarioState,
    private val ibmMqChannels: Map<String, IbmMqChannel>
) : BaseTestStepProcessor<IbmMqTestStep>(testScenarioState) {

    private val log = logger<IbmMqTestStepProcessor>()

    override fun processTestStep(testStep: IbmMqTestStep): Any? {
        var response: Map<*, Any?>? = null
        val sendTo = testStep.sendTo
        val readFrom = testStep.readFrom
        val respondTo = testStep.respondTo

        sendTo?.let {
            log.info(testStep, "Start to send message of test step '${testStep.name}' to IBMMQ queues $sendTo")
            send(testStep, it)
        }

        readFrom?.let {
            log.info(testStep, "Start to read message of test step '${testStep.name}' from IBMMQ queues $readFrom")
            response = read(testStep, readFrom)
            respondTo?.let {
                if (isValid(testStep, response)) {
                    log.info(testStep, "Respond to received message to IBMMQ queues '$respondTo'")
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
                log.debug(testStep) { "Start to read message from IBMMQ queue '$it'" }
                val message = channel.read(testStep.timeout)
                log.debug(testStep) { "Received message from IBMMQ queue '$it'" }

                message
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
                    log.debug(testStep) { "Send message to IBMMQ queue '$it'" }
                    channel.send(testData)
                    log.debug(testStep) { "Message has sent to IBMMQ queue '$it'" }
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

    override fun close() {
        log.info("Close IBMMQ Test Step Processor and all connections to queues")
        ibmMqChannels.values.forEach { it.close() }
    }

    class Builder : BaseIbmMqTestStepProcessorBuilder<IbmMqTestStep>() {
        override fun build(): TestStepProcessor<IbmMqTestStep> {
            return IbmMqTestStepProcessor(
                testScenarioState ?: error("Test Scenario State is mandatory for IbmMqTestStepProcessor"),
                ibmMqChannels
            )
        }
    }
}