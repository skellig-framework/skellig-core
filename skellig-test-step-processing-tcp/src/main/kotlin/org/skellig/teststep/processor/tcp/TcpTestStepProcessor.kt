package org.skellig.teststep.processor.tcp

import com.typesafe.config.Config
import org.skellig.teststep.processing.converter.TestStepResultConverter
import org.skellig.teststep.processing.exception.TestStepProcessingException
import org.skellig.teststep.processing.processor.BaseTestStepProcessor
import org.skellig.teststep.processing.processor.TestStepProcessor
import org.skellig.teststep.processing.state.TestScenarioState
import org.skellig.teststep.processing.validation.TestStepResultValidator
import org.skellig.teststep.processor.tcp.model.TcpDetails
import org.skellig.teststep.processor.tcp.model.TcpTestStep
import java.lang.Boolean

open class TcpTestStepProcessor(private val tcpChannels: Map<String, TcpChannel>,
                                testScenarioState: TestScenarioState?,
                                validator: TestStepResultValidator?,
                                testStepResultConverter: TestStepResultConverter?)
    : BaseTestStepProcessor<TcpTestStep>(testScenarioState!!, validator!!, testStepResultConverter) {

    protected override fun processTestStep(testStep: TcpTestStep): Any? {
        var response: Any? = null
        val sendTo = testStep.sendTo
        val receiveFrom = testStep.receiveFrom
        val respondTo = testStep.respondTo

        sendTo?.let { send(testStep.testData, it) }

        receiveFrom?.let {
            response = tcpChannels[receiveFrom]?.read(testStep.timeout, testStep.readBufferSize)

            validate(testStep, response)

            respondTo?.let { send(testStep.testData, it) }
        }
        return response
    }

    private fun send(testData: Any?, channel: String?) {
        if (tcpChannels.containsKey(channel)) {
            tcpChannels[channel]?.send(testData)
        } else {
            throw TestStepProcessingException(String.format("Channel '%s' was not registered " +
                    "in TCP Test Step Processor", channel))
        }
    }

    override fun getTestStepClass(): Class<TcpTestStep> {
        return TcpTestStep::class.java
    }

    override fun close() {
        tcpChannels.values.forEach { it.close() }
    }

    class Builder : BaseTestStepProcessor.Builder<TcpTestStep>() {

        companion object {
            private const val TCP_CONFIG_KEYWORD = "tcp"
            const val HOST = "host"
            const val CHANNEL_ID = "channelId"
            const val PORT = "port"
            const val KEEP_ALIVE = "keepAlive"
        }

        private val tcpChannels = mutableMapOf<String, TcpChannel>()

        fun withTcpChannel(tcpDetails: TcpDetails): Builder {
            tcpChannels.putIfAbsent(tcpDetails.channelId, TcpChannel(tcpDetails))
            return this
        }

        fun withTcpChannels(config: Config): Builder {
            if (config.hasPath(TCP_CONFIG_KEYWORD)) {
                (config.getAnyRefList(TCP_CONFIG_KEYWORD) as List<Map<String, String>>)
                        .forEach {
                            try {
                                val port = it[PORT]?.toInt() ?: 0
                                val keepAlive = if (it.containsKey(KEEP_ALIVE)) Boolean.parseBoolean(it[KEEP_ALIVE]) else true
                                withTcpChannel(TcpDetails(it[CHANNEL_ID] ?: error("TCP Channel ID must not be null"),
                                        it[HOST] ?: error("TCP host must not be null"), port, keepAlive))
                            } catch (e: NumberFormatException) {
                                throw NumberFormatException("Invalid number assigned to TCP port in configuration")
                            }
                        }
            }
            return this
        }

        override fun build(): TestStepProcessor<TcpTestStep> {
            return TcpTestStepProcessor(tcpChannels, testScenarioState, validator, testStepResultConverter)
        }
    }
}