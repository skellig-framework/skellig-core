package org.skellig.teststep.processor.tcp

import com.typesafe.config.Config
import org.skellig.teststep.processing.processor.BaseTestStepProcessor
import org.skellig.teststep.processor.tcp.model.BaseTcpTestStep
import org.skellig.teststep.processor.tcp.model.TcpDetails

abstract class BaseTcpProcessorBuilder<T : BaseTcpTestStep> : BaseTestStepProcessor.Builder<T>() {

    companion object {
        private const val TCP_CONFIG_KEYWORD = "tcp.hosts"
        const val ID = "id"
        const val HOST = "host"
        const val PORT = "port"
        const val KEEP_ALIVE = "keepAlive"

        val tcpChannels = mutableMapOf<String, TcpChannel>()
    }

    fun tcpChannel(tcpDetails: TcpDetails) = apply {
        tcpChannels.putIfAbsent(tcpDetails.id, TcpChannel(tcpDetails))
    }

    fun tcpChannels(config: Config) = apply {
        if (config.hasPath(TCP_CONFIG_KEYWORD)) {
            (config.getAnyRefList(TCP_CONFIG_KEYWORD) as List<*>)
                .forEach {
                    if (it is Map<*, *>) {
                        try {
                            val port = it[PORT]?.toString()?.toInt() ?: 0
                            val keepAlive = if (it.containsKey(KEEP_ALIVE)) it[KEEP_ALIVE].toString().toBoolean() else true
                            tcpChannel(
                                TcpDetails(
                                    it[ID]?.toString() ?: it[HOST]?.toString() ?: "",
                                    it[HOST]?.toString() ?: error("TCP host must not be null"),
                                    port,
                                    keepAlive
                                )
                            )
                        } catch (e: NumberFormatException) {
                            throw NumberFormatException("Invalid number assigned to TCP port in configuration")
                        }
                    }
                }
        }
    }
}