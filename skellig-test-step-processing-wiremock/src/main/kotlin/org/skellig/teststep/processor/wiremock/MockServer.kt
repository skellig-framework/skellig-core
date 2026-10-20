package org.skellig.teststep.processor.wiremock

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.core.WireMockConfiguration.options
import org.skellig.teststep.processor.wiremock.config.MockServerSettings

/** The only class that touches WireMock types; everything else works with Skellig's own models. */
internal class MockServer private constructor(val settings: MockServerSettings, private val server: WireMockServer) {

    fun reset() = server.resetAll()

    fun stop() {
        try {
            server.resetAll()
        } finally {
            server.stop()
        }
    }

    companion object {
        fun start(settings: MockServerSettings): MockServer {
            val options = options().port(settings.port)
            settings.journalMaxEntries?.let { options.maxRequestJournalEntries(it) }
            val server = WireMockServer(options)
            try {
                server.start()
            } catch (ex: Exception) {
                runCatching { server.stop() }
                throw IllegalStateException(
                    "Failed to start wiremock server '${settings.name}' on port ${settings.port}: ${ex.message}. If the message does not explain it, check whether the port is already in use.", ex
                )
            }
            return MockServer(settings, server)
        }
    }
}
