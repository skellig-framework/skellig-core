package org.skellig.teststep.processor.tcp

import org.skellig.task.async.AsyncTaskUtils.Companion.runTasksAsyncAndWait
import org.skellig.teststep.processing.processor.BaseTestStepProcessor
import org.skellig.teststep.processing.processor.TestStepProcessor
import org.skellig.teststep.processing.state.TestScenarioState
import org.skellig.teststep.processor.tcp.model.TcpTestStep

open class TcpTestStepProcessor(
    private val tcpChannels: Map<String, TcpChannel>,
    testScenarioState: TestScenarioState?
) : BaseTestStepProcessor<TcpTestStep>(testScenarioState!!) {

    override fun processTestStep(testStep: TcpTestStep): Any? {
        var response: Any? = null
        val sendTo = testStep.sendTo
        val readFrom = testStep.readFrom
        val respondTo = testStep.respondTo

        sendTo?.let { send(testStep.testData, it) }

        readFrom?.let {
            response = read(testStep, readFrom)

            respondTo?.let {
                if (isValid(testStep, response)) send(testStep.testData, it)
            }
        }
        return response
    }

    private fun read(testStep: TcpTestStep, channels: Set<String?>): Map<*, Any?> {
        val tasks = channels
            .map {
                it to {
                    val channel = tcpChannels[it] ?: error(getChannelNotExistErrorMessage(it))
                    channel.read(testStep.timeout, testStep.readBufferSize)
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
        val tasks = channels
            .map {
                it to {
                    val channel = tcpChannels[it] ?: error(getChannelNotExistErrorMessage(it))
                    channel.send(testData)
                    "sent"
                }
            }
            .toMap()
        runTasksAsyncAndWait(tasks)
    }

    override fun getTestStepClass(): Class<TcpTestStep> {
        return TcpTestStep::class.java
    }

    override fun close() {
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