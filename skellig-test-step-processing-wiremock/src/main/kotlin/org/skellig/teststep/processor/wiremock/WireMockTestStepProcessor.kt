package org.skellig.teststep.processor.wiremock

import org.skellig.teststep.processing.processor.BaseTestStepProcessor
import org.skellig.teststep.processing.state.TestScenarioState
import org.skellig.teststep.processor.wiremock.config.MockServerSettings
import org.skellig.teststep.processor.wiremock.model.WireMockTestStep

/** Owns the mock servers: they listen from creation until the Skellig context closes. */
internal class WireMockTestStepProcessor private constructor(
    state: TestScenarioState,
    private val servers: Map<String, MockServer>
) : BaseTestStepProcessor<WireMockTestStep>(state) {

    override fun getTestStepClass() = WireMockTestStep::class.java

    override fun processTestStep(testStep: WireMockTestStep): Any? = null

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

    companion object {
        /** Starts every server, or none: servers already started are stopped when a later one fails. */
        fun start(state: TestScenarioState, settings: List<MockServerSettings>): WireMockTestStepProcessor {
            val started = linkedMapOf<String, MockServer>()
            try {
                settings.forEach { started[it.name] = MockServer.start(it) }
            } catch (ex: Exception) {
                started.values.forEach { runCatching { it.stop() } }
                throw ex
            }
            return WireMockTestStepProcessor(state, started)
        }
    }
}
