package org.skellig.teststep.processor.wiremock

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock.aResponse
import com.github.tomakehurst.wiremock.client.WireMock.request
import com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo
import com.github.tomakehurst.wiremock.core.WireMockConfiguration.options
import org.skellig.teststep.processor.wiremock.config.MockServerSettings
import org.skellig.teststep.processor.wiremock.model.StubDefinition
import java.util.UUID

/** The only class that touches WireMock types; everything else works with Skellig's own models. */
internal class MockServer private constructor(val settings: MockServerSettings, private val server: WireMockServer) {

    val name get() = settings.name
    val port get() = settings.port

    fun reset() = server.resetAll()

    /** Registers the stub under [id], first removing the stub it registered earlier so that the latest registration wins. */
    fun registerStub(id: UUID, stub: StubDefinition) {
        if (server.getStubMappings().any { it.id == id }) server.removeStub(id)

        val response = aResponse().withStatus(stub.response.status)
        stub.response.headers.forEach { (name, value) -> response.withHeader(name, value) }
        stub.response.body?.let { response.withBody(it) }

        server.stubFor(request(stub.request.method, urlPathEqualTo(stub.request.path)).withId(id).willReturn(response))
    }

    fun stop() {
        try {
            server.resetAll()
        } finally {
            server.stop()
        }
    }

    companion object {
        /** Create WireMock server and start it. */
        fun create(settings: MockServerSettings): MockServer {
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
