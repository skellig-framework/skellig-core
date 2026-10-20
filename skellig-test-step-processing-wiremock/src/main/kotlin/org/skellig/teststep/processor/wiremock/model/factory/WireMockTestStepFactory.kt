package org.skellig.teststep.processor.wiremock.model.factory

import org.skellig.teststep.processing.exception.TestStepCreationException
import org.skellig.teststep.processing.model.DefaultTestStep
import org.skellig.teststep.processing.model.factory.BaseDefaultTestStepFactory
import org.skellig.teststep.processing.model.factory.TestStepRegistry
import org.skellig.teststep.processing.value.ValueExpressionContextFactory
import org.skellig.teststep.processor.wiremock.model.StubDefinition
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
                path = (request["path"] ?: throw TestStepCreationException("The stub 'request' block must have a 'path'")).toString()
            ),
            StubResponse(
                status = (response["status"] ?: 200).toString().toInt(),
                headers = (response["headers"] as? Map<*, *>)?.entries?.associate { it.key.toString() to it.value.toString() } ?: emptyMap(),
                body = response["body"]?.toString()
            )
        )
    }

    private fun block(stub: Map<*, *>, name: String): Map<*, *> =
        stub[name] as? Map<*, *> ?: throw TestStepCreationException("The wiremock 'stub' block must have a '$name { ... }' block")
}
