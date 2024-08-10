package org.skellig.teststep.processor.tcp

import org.skellig.teststep.processing.processor.BaseTestStepProcessor
import org.skellig.teststep.processing.processor.TestStepProcessor
import org.skellig.teststep.processing.state.TestScenarioState
import org.skellig.teststep.processing.util.debug
import org.skellig.teststep.processing.util.info
import org.skellig.teststep.processing.util.logger
import org.skellig.teststep.processor.tcp.model.TcpTestStep

/**
 * TcpTestStepProcessor is a class that processes TCP test steps.
 * It processes the test step this way:
 * 1) Sends a message ([TcpTestStep.testData]) to [TcpTestStep.sendTo]
 * 2) Reads a message ([TcpTestStep.testData]) from [TcpTestStep.readFrom]. If [TcpTestStep.respondTo] defined, then
 * responds with message ([TcpTestStep.testData]) to [TcpTestStep.respondTo] if the received message is valid
 * based on [TcpTestStep.validationDetails].
 *
 * All send, read and respond operations are performed in parallel per each channel but blocked until they are all finished.
 *
 * @param tcpChannels A map of TCP channels where the key is the channel ID and the value is the TCPChannel instance.
 * @param testScenarioState The state of the test scenario.
 */
open class TcpTestStepProcessor(
    private val tcpChannels: Map<String, TcpChannel>,
    testScenarioState: TestScenarioState?
) : BaseTestStepProcessor<TcpTestStep>(testScenarioState!!) {

    private val log = logger<TcpTestStepProcessor>()

    override fun processTestStep(testStep: TcpTestStep): Any? {
        var response: Any? = null
        val sendTo = testStep.sendTo
        val readFrom = testStep.readFrom
        val respondTo = testStep.respondTo

        sendTo?.let {
            log.info(testStep, "Start to send message of test step '${testStep.name}' to TCP channels $sendTo")
            send(testStep, it)
        }

        readFrom?.let {
            log.info(testStep, "Start to read message of test step '${testStep.name}' from TCP channels $readFrom")
            response = read(testStep, readFrom)

            respondTo?.let {
                if (isValid(testStep, response)) {
                    log.info(testStep, "Respond to received message to TCP channels '$respondTo'")
                    send(testStep, it)
                }
            }
        }
        return response
    }

    private fun read(testStep: TcpTestStep, channels: Set<String?>): Map<*, Any?> {
        val tasks = channels.associateWith {
            {
                val channel = tcpChannels[it] ?: error(getChannelNotExistErrorMessage(it))
                log.debug(testStep) { "Start to read message from TCP channel '$it'" }
                val message = channel.read(testStep.timeout, testStep.readBufferSize)
                log.debug(testStep) { "Received message from TCP channel '$it'" }
                message
            }
        }
        return runTasksAsyncAndWait(tasks, testStep)
    }

    private fun send(testStep: TcpTestStep, channels: Set<String>) {
        val tasks = channels.associateWith {
            {
                val channel = tcpChannels[it] ?: error(getChannelNotExistErrorMessage(it))
                log.debug(testStep) { "Send message to TCP channel '$it'" }
                channel.send(testStep.testData)
                log.debug(testStep) { "Message has sent to TCP channel '$it'" }
                "sent"
            }
        }
        runTasksAsyncAndWait(tasks, testStep)
    }

    override fun getTestStepClass(): Class<TcpTestStep> {
        return TcpTestStep::class.java
    }

    override fun close() {
        log.info("Close TCP Test Step Processor and all connections to channels")
        tcpChannels.values.forEach { it.close() }
    }

    private fun getChannelNotExistErrorMessage(channelId: String?) =
        "Channel '$channelId' was not registered in TCP Test Step Processor"

    class Builder : BaseTcpProcessorBuilder<TcpTestStep>() {
        override fun build(): TestStepProcessor<TcpTestStep> {
            return TcpTestStepProcessor(tcpChannels, testScenarioState)
        }
    }
}