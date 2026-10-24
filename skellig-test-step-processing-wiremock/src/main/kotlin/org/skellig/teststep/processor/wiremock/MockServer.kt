package org.skellig.teststep.processor.wiremock

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.core.WireMockConfiguration.options
import org.skellig.teststep.processor.wiremock.config.MockServerSettings
import org.skellig.teststep.processor.wiremock.model.ResetScope
import com.github.tomakehurst.wiremock.matching.RequestPattern
import com.github.tomakehurst.wiremock.common.Json
import com.github.tomakehurst.wiremock.common.InvalidInputException
import com.github.tomakehurst.wiremock.stubbing.StubMapping
import org.skellig.teststep.processing.exception.TestStepProcessingException
import java.util.UUID

/** The only class that touches WireMock types; everything else works with Skellig's own models. */
internal class MockServer private constructor(val settings: MockServerSettings, private val server: WireMockServer) {

    val name get() = settings.name
    val port get() = settings.port

    fun reset(what: ResetScope) = when (what) {
        ResetScope.ALL -> server.resetAll()
        ResetScope.STUBS -> server.resetMappings()
        ResetScope.JOURNAL -> server.resetRequests()
    }

    /** Registers a WireMock JSON mapping under [id], replacing the stub registered earlier with that id. */
    fun registerStub(id: UUID, json: String) {
        val mapping: StubMapping? = try {
            Json.read(json, StubMapping::class.java)
        } catch (ex: InvalidInputException) {
            throw TestStepProcessingException("Invalid wiremock stub JSON: ${ex.errors?.errors?.firstOrNull()?.detail ?: ex.message}", ex)
        }
        if (mapping?.request == null) {
            throw TestStepProcessingException("Invalid wiremock stub JSON: a mapping must be an object with a 'request'")
        }
        server.removeStub(id)
        server.addStubMapping(mapping.transform { it.setId(id) })
    }

    fun requests(pattern: RequestPattern?, unmatched: Boolean): Map<String, Any> {
        val requests = server.allServeEvents
            .filter { (!unmatched || it.isNoExactMatch) && (pattern == null || pattern.match(it.request).isExactMatch) }
            .map { event ->
                val request = event.request
                mapOf(
                    "method" to request.method.name,
                    "url" to request.url,
                    "path" to request.url.substringBefore('?'),
                    "query" to request.queryParams.mapValues { it.value.firstValue() },
                    "headers" to request.headers.all().associate { it.key() to it.firstValue() },
                    "body" to request.bodyAsString,
                    "loggedTime" to request.loggedDate.time,
                    "matched" to !event.isNoExactMatch,
                    "stubId" to event.stubMapping?.takeUnless { event.isNoExactMatch }?.id?.toString()
                )
            }
        return mapOf("count" to requests.size, "requests" to requests)
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
            settings.bindAddress?.let { options.bindAddress(it) }
            settings.filesDirectory?.let { options.usingFilesUnderDirectory(it.path) }
            settings.https?.let {
                options.httpsPort(it.port).keystorePath(it.keystorePath).keystorePassword(it.keystorePassword)
                    .keystoreType(it.keystoreType).keyManagerPassword(it.keystorePassword)
            }
            var server: WireMockServer? = null
            try {
                server = WireMockServer(options)
                server.start()
            } catch (ex: Exception) {
                runCatching { server?.stop() }
                throw IllegalStateException(
                    "Failed to start wiremock server '${settings.name}' on port ${settings.port}${settings.https?.let { " (https port ${it.port})" }.orEmpty()}: ${ex.message}. If the message does not explain it, check whether the port is already in use, and that the keystore, password and files directory are valid.", ex
                )
            }
            return MockServer(settings, server)
        }
    }
}
