package org.skellig.teststep.processor.tcp

import com.typesafe.config.Config
import org.skellig.task.async.AsyncTaskUtils.Companion.runTasksAsyncAndWait
import org.skellig.teststep.processing.converter.TestStepResultConverter
import org.skellig.teststep.processing.processor.BaseTestStepProcessor
import org.skellig.teststep.processing.processor.TestStepProcessor
import org.skellig.teststep.processing.state.TestScenarioState
import org.skellig.teststep.processing.validation.TestStepResultValidator
import org.skellig.teststep.processor.tcp.model.TcpDetails
import org.skellig.teststep.processor.tcp.model.TcpTestStep

open class TcpTestStepProcessor(private val tcpChannels: Map<String, TcpChannel>,
                                testScenarioState: TestScenarioState?,
                                validator: TestStepResultValidator?,
                                testStepResultConverter: TestStepResultConverter?)
    : BaseTestStepProcessor<TcpTestStep>(testScenarioState!!, validator!!, testStepResultConverter) {

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

    class Builder : BaseTestStepProcessor.Builder<TcpTestStep>() {

        companion object {
            private const val TCP_CONFIG_KEYWORD = "tcp"
            const val HOST = "host"
            const val NAME = "name"
            const val PORT = "port"
            const val KEEP_ALIVE = "keepAlive"
        }

        private val tcpChannels = mutableMapOf<String, TcpChannel>()

        fun tcpChannel(tcpDetails: TcpDetails) = apply {
            tcpChannels.putIfAbsent(tcpDetails.channelId, TcpChannel(tcpDetails))
        }

        fun tcpChannels(config: Config) = apply {
            if (config.hasPath(TCP_CONFIG_KEYWORD)) {
                (config.getAnyRefList(TCP_CONFIG_KEYWORD) as List<Map<String, String>>)
                        .forEach {
                            try {
                                val port = it[PORT]?.toInt() ?: 0
                                val keepAlive = if (it.containsKey(KEEP_ALIVE)) it[KEEP_ALIVE].toBoolean() else true
                                tcpChannel(TcpDetails(it[NAME] ?: error("TCP Connection Name must not be null"),
                                        it[HOST] ?: error("TCP host must not be null"), port, keepAlive))
                            } catch (e: NumberFormatException) {
                                throw NumberFormatException("Invalid number assigned to TCP port in configuration")
                            }
                        }
            }
        }

        override fun build(): TestStepProcessor<TcpTestStep> {
            return TcpTestStepProcessor(tcpChannels, testScenarioState, validator, testStepResultConverter)
        }
    }
}