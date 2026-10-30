package org.skellig.teststep.processor.wiremock

import org.skellig.teststep.processing.exception.TestStepProcessingException
import org.skellig.teststep.processing.processor.BaseTestStepProcessor
import org.skellig.teststep.processor.wiremock.config.MockServerSettings
import org.skellig.teststep.processor.wiremock.model.WireMockTestStep
import java.util.UUID

/** Owns the mock servers: they listen from creation until the Skellig context closes. */
internal class WireMockTestStepProcessor private constructor(
    private val servers: Map<String, MockServer>
) : BaseTestStepProcessor<WireMockTestStep>() {

    override fun getTestStepClass() = WireMockTestStep::class.java

    override fun processTestStep(testStep: WireMockTestStep): Any? {
        if (testStep.operation == "reset") return reset(testStep)
        val server = resolveServer(testStep.server)
        if (testStep.operation == "requests") return pollRequests(server, testStep)

        // The same step on the same server always maps to the same id, so re-running it replaces its stub; a step without an id is never replaced
        val id = testStep.id?.let { UUID.nameUUIDFromBytes("${server.name}:$it".toByteArray()) } ?: UUID.randomUUID()
        server.registerStub(id, testStep.rawStub ?: error("WireMock stub step has no stub definition"))
        return mapOf("id" to id.toString(), "server" to server.name, "port" to server.port)
    }

    /** Resets the named server, or every server when none is named. */
    private fun reset(testStep: WireMockTestStep): Map<String, Any> {
        val targets = testStep.server?.let { listOf(resolveServer(it)) } ?: servers.values
        targets.forEach { it.reset(testStep.resetScope) }
        return mapOf("servers" to targets.map { it.name }, "what" to testStep.resetScope.keyword)
    }

    private fun pollRequests(server: MockServer, testStep: WireMockTestStep): Map<String, Any> {
        var result = server.requests(testStep.requestFilter, testStep.unmatched)
        repeat((testStep.attempts - 1).coerceAtLeast(0)) {
            if (isValid(testStep, result)) return result
            if (testStep.delay > 0) Thread.sleep(testStep.delay.toLong())
            result = server.requests(testStep.requestFilter, testStep.unmatched)
        }
        return result
    }

    override fun close() {
        val failures = servers.values.mapNotNull { server -> runCatching { server.stop() }.exceptionOrNull() }
        try {
            super.close()
        } finally {
            failures.firstOrNull()?.let { first ->
                failures.drop(1).forEach(first::addSuppressed)
                throw first
            }
        }
    }

    private fun resolveServer(name: String?): MockServer =
        if (name != null) servers[name] ?: throw TestStepProcessingException("Unknown wiremock server '$name'. Configured servers: ${servers.keys.joinToString()}")
        else servers.values.singleOrNull()
            ?: throw TestStepProcessingException("The 'server' key is required when several wiremock servers are configured: ${servers.keys.joinToString()}")

    companion object {
        /** Create WireMockTestStepProcessor and starts every Wiremock server, or none: servers already started are stopped when a later one fails. */
        fun create(settings: List<MockServerSettings>): WireMockTestStepProcessor {
            val started = linkedMapOf<String, MockServer>()
            try {
                settings.forEach { started[it.name] = MockServer.create(it) }
            } catch (ex: Exception) {
                started.values.forEach { runCatching { it.stop() } }
                throw ex
            }
            return WireMockTestStepProcessor(started)
        }
    }
}
