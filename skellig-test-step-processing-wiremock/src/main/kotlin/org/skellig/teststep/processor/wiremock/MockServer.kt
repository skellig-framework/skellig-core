package org.skellig.teststep.processor.wiremock

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.MappingBuilder
import com.github.tomakehurst.wiremock.client.WireMock.aResponse
import com.github.tomakehurst.wiremock.client.WireMock.request
import com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo
import com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching
import com.github.tomakehurst.wiremock.client.WireMock.equalTo
import com.github.tomakehurst.wiremock.client.WireMock.containing
import com.github.tomakehurst.wiremock.client.WireMock.matching
import com.github.tomakehurst.wiremock.client.WireMock.equalToJson
import com.github.tomakehurst.wiremock.core.WireMockConfiguration.options
import org.skellig.teststep.processor.wiremock.config.MockServerSettings
import org.skellig.teststep.processor.wiremock.model.StubRequest
import org.skellig.teststep.processor.wiremock.model.StubDefinition
import org.skellig.teststep.processor.wiremock.model.RequestMatcher
import java.util.UUID

/** The only class that touches WireMock types; everything else works with Skellig's own models. */
internal class MockServer private constructor(val settings: MockServerSettings, private val server: WireMockServer) {

    val name get() = settings.name
    val port get() = settings.port

    fun reset() = server.resetAll()

    /** Registers the stub under [id], first removing the stub it registered earlier so that the latest registration wins. */
    fun registerStub(id: UUID, stub: StubDefinition) {
        server.removeStub(id)

        val response = aResponse().withStatus(stub.response.status)
        stub.response.headers.forEach { (name, value) -> response.withHeader(name, value) }
        stub.response.body?.let { response.withBody(it) }
        stub.response.delay?.let { response.withFixedDelay(it) }

        val mapping = mapping(stub.request).withId(id)
        stub.priority?.let { mapping.atPriority(it) }
        server.stubFor(mapping.willReturn(response))
    }

    private fun mapping(definition: StubRequest): MappingBuilder {
        val path = when (val matcher = definition.path) {
            is RequestMatcher.Equal -> urlPathEqualTo(matcher.value)
            is RequestMatcher.Matches -> urlPathMatching(matcher.value)
            else -> error("Request path must use equality or regex matching")
        }
        val mapping = request(definition.method, path)
        definition.query.forEach { (name, matcher) -> mapping.withQueryParam(name, pattern(matcher)) }
        definition.headers.forEach { (name, matcher) -> mapping.withHeader(name, pattern(matcher)) }
        definition.body?.let { mapping.withRequestBody(pattern(it)) }
        return mapping
    }

    fun requests(filter: StubRequest?, unmatched: Boolean): Map<String, Any> {
        val pattern = filter?.let { mapping(it).build().request }
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

    private fun pattern(matcher: RequestMatcher) = when (matcher) {
        is RequestMatcher.Equal -> equalTo(matcher.value)
        is RequestMatcher.Contains -> containing(matcher.value)
        is RequestMatcher.Matches -> matching(matcher.value)
        is RequestMatcher.Json -> equalToJson(matcher.value, false, true)
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
