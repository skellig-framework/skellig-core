package org.skellig.teststep.processor.wiremock.model.factory

import com.github.tomakehurst.wiremock.client.MappingBuilder
import com.github.tomakehurst.wiremock.client.WireMock.aResponse
import com.github.tomakehurst.wiremock.client.WireMock.containing
import com.github.tomakehurst.wiremock.client.WireMock.equalTo
import com.github.tomakehurst.wiremock.client.WireMock.equalToJson
import com.github.tomakehurst.wiremock.client.WireMock.matching
import com.github.tomakehurst.wiremock.client.WireMock.request
import com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo
import com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching
import com.github.tomakehurst.wiremock.common.Json
import com.github.tomakehurst.wiremock.matching.StringValuePattern
import org.skellig.teststep.processing.exception.TestStepCreationException
import org.skellig.teststep.processing.model.DefaultTestStep
import org.skellig.teststep.processing.model.factory.BaseDefaultTestStepFactory
import org.skellig.teststep.processing.model.factory.TestStepRegistry
import org.skellig.teststep.processing.value.ValueExpressionContextFactory
import org.skellig.teststep.processor.wiremock.model.ResetScope
import org.skellig.teststep.processor.wiremock.model.WireMockTestStep
import org.skellig.teststep.reader.value.expression.ValueExpression

/** Claims only steps with a `wiremock` key, so it never competes with other factories such as HTTP, which uses `url` and `method`. */
internal class WireMockTestStepFactory(
    registry: TestStepRegistry,
    expressions: ValueExpressionContextFactory
) : BaseDefaultTestStepFactory<WireMockTestStep>(registry, expressions) {

    companion object {
        private val WIREMOCK_KEYWORD = fromProperty("wiremock")
        private val SERVER_KEYWORD = fromProperty("server")
        private val STUB_KEYWORD = fromProperty("stub")
        private val REQUEST_KEYWORD = fromProperty("request")
        private val UNMATCHED_KEYWORD = fromProperty("unmatched")
        private val WHAT_KEYWORD = fromProperty("what")
        private const val STUB_OPERATION = "stub"
        private const val REQUESTS_OPERATION = "requests"
        private const val RESET_OPERATION = "reset"
        private const val REQUEST_KEY = "request"
        private const val RESPONSE_KEY = "response"
        private const val METHOD_KEY = "method"
        private const val PATH_KEY = "path"
        private const val QUERY_KEY = "query"
        private const val HEADERS_KEY = "headers"
        private const val BODY_KEY = "body"
        private const val STATUS_KEY = "status"
        private const val DELAY_KEY = "delay"
        private const val PRIORITY_KEY = "priority"
        private const val CONTAINS = "contains"
        private const val MATCHES = "matches"
        private const val JSON = "json"
    }

    override fun isConstructableFrom(rawTestStep: Map<ValueExpression, ValueExpression?>) = rawTestStep.containsKey(WIREMOCK_KEYWORD)

    override fun createTestStepBuilder(
        rawTestStep: Map<ValueExpression, ValueExpression?>,
        parameters: Map<String, Any?>
    ): DefaultTestStep.Builder<WireMockTestStep> {
        val operation = convertValue<String>(rawTestStep[WIREMOCK_KEYWORD], parameters)?.trim()
        if (operation !in setOf(STUB_OPERATION, REQUESTS_OPERATION, RESET_OPERATION)) {
            throw TestStepCreationException("Unsupported wiremock operation '$operation'. Supported operations: $STUB_OPERATION, $REQUESTS_OPERATION, $RESET_OPERATION")
        }
        return WireMockTestStep.Builder().apply {
            this.operation = operation
            server = convertValue<String>(rawTestStep[SERVER_KEYWORD], parameters)
            when (operation) {
                STUB_OPERATION -> {
                    rawStub = readStub(convertValue<Any>(rawTestStep[STUB_KEYWORD], parameters) as? Map<*, *>)
                }
                RESET_OPERATION -> {
                    resetScope = convertValue<String>(rawTestStep[WHAT_KEYWORD], parameters)?.let {
                        ResetScope.fromKeyword(it) ?: throw TestStepCreationException(
                            "Unknown wiremock reset 'what' '$it'. Supported values: ${ResetScope.entries.joinToString { scope -> scope.keyword }}"
                        )
                    } ?: ResetScope.ALL
                }
                else -> {
                    requestFilter = convertValue<Any>(rawTestStep[REQUEST_KEYWORD], parameters)?.let {
                        readRequest(it as? Map<*, *> ?: throw TestStepCreationException("Request filter must be a block"), true).build().request
                    }
                    unmatched = convertValue<Any>(rawTestStep[UNMATCHED_KEYWORD], parameters)?.let {
                        it.toString().toBooleanStrictOrNull()
                            ?: throw TestStepCreationException("WireMock 'unmatched' must be true or false")
                    } ?: false
                }
            }
        }
    }

    /** Returns the WireMock JSON mapping of the `stub` block: its raw `json` as is, or the mapping built from `request {}` and `response {}`. */
    private fun readStub(stub: Map<*, *>?): String {
        stub ?: throw TestStepCreationException("A wiremock 'stub' step must have a 'stub { request { ... } response { ... } }' block")
        if (stub.containsKey(JSON)) return readRawStub(stub)

        val request = block(stub, REQUEST_KEY)
        val response = block(stub, RESPONSE_KEY)

        val mappingResponse = aResponse().withStatus((response[STATUS_KEY] ?: 200).toString().toInt())
        (response[HEADERS_KEY] as? Map<*, *>)?.forEach { (name, value) -> mappingResponse.withHeader(name.toString(), value.toString()) }
        response[BODY_KEY]?.let { mappingResponse.withBody(it.toString()) }
        response[DELAY_KEY]?.let { mappingResponse.withFixedDelay(readInt(it, "response.delay", 0)) }

        val mapping = readRequest(request)
        stub[PRIORITY_KEY]?.let { mapping.atPriority(readInt(it, PRIORITY_KEY, 1)) }
        return Json.write(mapping.willReturn(mappingResponse).build())
    }

    private fun readRawStub(stub: Map<*, *>): String {
        if (stub.size != 1) throw TestStepCreationException("A raw wiremock 'stub { json = ... }' block must contain only 'json'")
        return stub[JSON]?.toString()?.takeIf { it.isNotBlank() }
            ?: throw TestStepCreationException("The wiremock stub 'json' must not be empty")
    }

    private fun readRequest(request: Map<*, *>, filter: Boolean = false): MappingBuilder {
        val method = (request[METHOD_KEY] ?: if (filter) "ANY" else
            throw TestStepCreationException("The stub 'request' block must have a 'method'")).toString().uppercase()
        val path = if (filter && request[PATH_KEY] == null) urlPathMatching(".*") else readPath(request[PATH_KEY])
        val mapping = request(method, path)
        readMatchers(request[QUERY_KEY], QUERY_KEY).forEach { (name, matcher) -> mapping.withQueryParam(name, matcher) }
        readMatchers(request[HEADERS_KEY], HEADERS_KEY).forEach { (name, matcher) -> mapping.withHeader(name, matcher) }
        request[BODY_KEY]?.let { mapping.withRequestBody(readMatcher(it)) }
        return mapping
    }

    private fun readInt(value: Any, name: String, minimum: Int): Int =
        value.toString().toIntOrNull()?.takeIf { it >= minimum }
            ?: throw TestStepCreationException("Stub '$name' must be an integer greater than or equal to $minimum")

    private fun readPath(value: Any?) = when (value) {
        null -> throw TestStepCreationException("The stub 'request' block must have a 'path'")
        is Map<*, *> -> (value.entries.singleOrNull()?.takeIf { it.key == MATCHES }
            ?: throw TestStepCreationException("Request 'path' must be a plain value or a 'matches' regex"))
            .let { urlPathMatching(it.value.toString()) }
        else -> urlPathEqualTo(value.toString())
    }

    private fun readMatchers(value: Any?, name: String): Map<String, StringValuePattern> {
        if (value == null) return emptyMap()
        val entries = value as? Map<*, *>
            ?: throw TestStepCreationException("Request '$name' must be a block of named matchers")
        return entries.entries.associate { it.key.toString() to readMatcher(it.value) }
    }

    private fun readMatcher(value: Any?): StringValuePattern {
        if (value == null) throw TestStepCreationException("A request matcher must have a value")
        if (value !is Map<*, *>) return equalTo(value.toString())
        if (value.size != 1) throw TestStepCreationException("A request matcher must contain exactly one of 'contains', 'matches' or 'json'")
        val (name, expected) = value.entries.single()
        val text = expected?.toString() ?: throw TestStepCreationException("Request matcher '$name' must have a value")
        return when (name) {
            CONTAINS -> containing(text)
            MATCHES -> matching(text)
            JSON -> equalToJson(text, false, true)
            else -> throw TestStepCreationException("Unknown request matcher '$name'. Supported matchers: contains, matches, json, or a plain value for equality")
        }
    }

    private fun block(stub: Map<*, *>, name: String): Map<*, *> =
        stub[name] as? Map<*, *> ?: throw TestStepCreationException("The wiremock 'stub' block must have a '$name { ... }' block")
}
