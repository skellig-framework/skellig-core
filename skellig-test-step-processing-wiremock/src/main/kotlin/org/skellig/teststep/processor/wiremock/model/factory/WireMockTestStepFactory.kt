package org.skellig.teststep.processor.wiremock.model.factory

import org.skellig.teststep.processing.exception.TestStepCreationException
import org.skellig.teststep.processing.model.DefaultTestStep
import org.skellig.teststep.processing.model.factory.BaseDefaultTestStepFactory
import org.skellig.teststep.processing.model.factory.TestStepRegistry
import org.skellig.teststep.processing.value.ValueExpressionContextFactory
import org.skellig.teststep.processor.wiremock.model.ResetScope
import org.skellig.teststep.processor.wiremock.model.StubDefinition
import org.skellig.teststep.processor.wiremock.model.RequestMatcher
import org.skellig.teststep.processor.wiremock.model.StubRequest
import org.skellig.teststep.processor.wiremock.model.StubResponse
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
            if (operation == STUB_OPERATION) {
                val stubBlock = convertValue<Any>(rawTestStep[STUB_KEYWORD], parameters) as? Map<*, *>
                if (stubBlock?.containsKey(JSON) == true) {
                    rawStub = readRawStub(stubBlock)
                } else {
                    stub = readStub(stubBlock)
                }
            } else if (operation == RESET_OPERATION) {
                resetScope = convertValue<String>(rawTestStep[WHAT_KEYWORD], parameters)?.let {
                    ResetScope.fromKeyword(it) ?: throw TestStepCreationException(
                        "Unknown wiremock reset 'what' '$it'. Supported values: ${ResetScope.entries.joinToString { scope -> scope.keyword }}"
                    )
                } ?: ResetScope.ALL
            } else {
                requestFilter = convertValue<Any>(rawTestStep[REQUEST_KEYWORD], parameters)?.let {
                    readRequest(it as? Map<*, *> ?: throw TestStepCreationException("Request filter must be a block"), true)
                }
                unmatched = convertValue<Any>(rawTestStep[UNMATCHED_KEYWORD], parameters)?.let {
                    it.toString().toBooleanStrictOrNull()
                        ?: throw TestStepCreationException("WireMock 'unmatched' must be true or false")
                } ?: false
            }
        }
    }

    private fun readRawStub(stub: Map<*, *>): String {
        if (stub.size != 1) throw TestStepCreationException("A raw wiremock 'stub { json = ... }' block must contain only 'json'")
        return stub[JSON]?.toString()?.takeIf { it.isNotBlank() }
            ?: throw TestStepCreationException("The wiremock stub 'json' must not be empty")
    }

    private fun readStub(stub: Map<*, *>?): StubDefinition {
        stub ?: throw TestStepCreationException("A wiremock 'stub' step must have a 'stub { request { ... } response { ... } }' block")
        val request = block(stub, REQUEST_KEY)
        val response = block(stub, RESPONSE_KEY)

        return StubDefinition(
            readRequest(request),
            StubResponse(
                status = (response[STATUS_KEY] ?: 200).toString().toInt(),
                headers = (response[HEADERS_KEY] as? Map<*, *>)?.entries?.associate { it.key.toString() to it.value.toString() } ?: emptyMap(),
                body = response[BODY_KEY]?.toString(),
                delay = response[DELAY_KEY]?.let { readInt(it, "response.delay", 0) }
            ),
            priority = stub[PRIORITY_KEY]?.let { readInt(it, PRIORITY_KEY, 1) }
        )
    }

    private fun readRequest(request: Map<*, *>, filter: Boolean = false) = StubRequest(
        method = (request[METHOD_KEY] ?: if (filter) "ANY" else
            throw TestStepCreationException("The stub 'request' block must have a 'method'")).toString().uppercase(),
        path = if (filter && request[PATH_KEY] == null) RequestMatcher.Matches(".*") else readPath(request[PATH_KEY]),
        query = readMatchers(request[QUERY_KEY], QUERY_KEY),
        headers = readMatchers(request[HEADERS_KEY], HEADERS_KEY),
        body = request[BODY_KEY]?.let { readMatcher(it) }
    )

    private fun readInt(value: Any, name: String, minimum: Int): Int =
        value.toString().toIntOrNull()?.takeIf { it >= minimum }
            ?: throw TestStepCreationException("Stub '$name' must be an integer greater than or equal to $minimum")

    private fun readPath(value: Any?): RequestMatcher {
        val matcher = readMatcher(value ?: throw TestStepCreationException("The stub 'request' block must have a 'path'"))
        if (matcher !is RequestMatcher.Equal && matcher !is RequestMatcher.Matches) {
            throw TestStepCreationException("Request 'path' must be a plain value or a 'matches' regex")
        }
        return matcher
    }

    private fun readMatchers(value: Any?, name: String): Map<String, RequestMatcher> {
        if (value == null) return emptyMap()
        val entries = value as? Map<*, *>
            ?: throw TestStepCreationException("Request '$name' must be a block of named matchers")
        return entries.entries.associate { it.key.toString() to readMatcher(it.value) }
    }

    private fun readMatcher(value: Any?): RequestMatcher {
        if (value == null) throw TestStepCreationException("A request matcher must have a value")
        if (value !is Map<*, *>) return RequestMatcher.Equal(value.toString())
        if (value.size != 1) throw TestStepCreationException("A request matcher must contain exactly one of 'contains', 'matches' or 'json'")
        val (name, expected) = value.entries.single()
        val text = expected?.toString() ?: throw TestStepCreationException("Request matcher '$name' must have a value")
        return when (name) {
            CONTAINS -> RequestMatcher.Contains(text)
            MATCHES -> RequestMatcher.Matches(text)
            JSON -> RequestMatcher.Json(text)
            else -> throw TestStepCreationException("Unknown request matcher '$name'. Supported matchers: contains, matches, json, or a plain value for equality")
        }
    }

    private fun block(stub: Map<*, *>, name: String): Map<*, *> =
        stub[name] as? Map<*, *> ?: throw TestStepCreationException("The wiremock 'stub' block must have a '$name { ... }' block")
}
