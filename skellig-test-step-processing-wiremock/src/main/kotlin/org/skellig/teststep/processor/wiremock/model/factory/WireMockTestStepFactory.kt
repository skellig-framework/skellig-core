package org.skellig.teststep.processor.wiremock.model.factory

import org.skellig.teststep.processing.exception.TestStepCreationException
import org.skellig.teststep.processing.model.DefaultTestStep
import org.skellig.teststep.processing.model.factory.BaseDefaultTestStepFactory
import org.skellig.teststep.processing.model.factory.TestStepRegistry
import org.skellig.teststep.processing.value.ValueExpressionContextFactory
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
        private const val STUB_OPERATION = "stub"
    }

    override fun isConstructableFrom(rawTestStep: Map<ValueExpression, ValueExpression?>) = rawTestStep.containsKey(WIREMOCK_KEYWORD)

    override fun createTestStepBuilder(
        rawTestStep: Map<ValueExpression, ValueExpression?>,
        parameters: Map<String, Any?>
    ): DefaultTestStep.Builder<WireMockTestStep> {
        val operation = convertValue<String>(rawTestStep[WIREMOCK_KEYWORD], parameters)?.trim()
        if (operation != STUB_OPERATION) {
            throw TestStepCreationException("Unsupported wiremock operation '$operation'. Supported operations: $STUB_OPERATION")
        }
        return WireMockTestStep.Builder().apply {
            this.operation = operation
            server = convertValue<String>(rawTestStep[SERVER_KEYWORD], parameters)
            stub = readStub(convertValue<Any>(rawTestStep[STUB_KEYWORD], parameters) as? Map<*, *>)
        }
    }

    private fun readStub(stub: Map<*, *>?): StubDefinition {
        stub ?: throw TestStepCreationException("A wiremock 'stub' step must have a 'stub { request { ... } response { ... } }' block")
        val request = block(stub, "request")
        val response = block(stub, "response")

        return StubDefinition(
            StubRequest(
                method = (request["method"] ?: throw TestStepCreationException("The stub 'request' block must have a 'method'")).toString().uppercase(),
                path = readPath(request["path"]),
                query = readMatchers(request["query"], "query"),
                headers = readMatchers(request["headers"], "headers"),
                body = request["body"]?.let { readMatcher(it) }
            ),
            StubResponse(
                status = (response["status"] ?: 200).toString().toInt(),
                headers = (response["headers"] as? Map<*, *>)?.entries?.associate { it.key.toString() to it.value.toString() } ?: emptyMap(),
                body = response["body"]?.toString(),
                delay = response["delay"]?.let { readInteger(it, "response.delay", 0) }
            ),
            priority = stub["priority"]?.let { readInteger(it, "priority", 1) }
        )
    }

    private fun readInteger(value: Any, name: String, minimum: Int): Int =
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
            "contains" -> RequestMatcher.Contains(text)
            "matches" -> RequestMatcher.Matches(text)
            "json" -> RequestMatcher.Json(text)
            else -> throw TestStepCreationException("Unknown request matcher '$name'. Supported matchers: contains, matches, json, or a plain value for equality")
        }
    }

    private fun block(stub: Map<*, *>, name: String): Map<*, *> =
        stub[name] as? Map<*, *> ?: throw TestStepCreationException("The wiremock 'stub' block must have a '$name { ... }' block")
}
