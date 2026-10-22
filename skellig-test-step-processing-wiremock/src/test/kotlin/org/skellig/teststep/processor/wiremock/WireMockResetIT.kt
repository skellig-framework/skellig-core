package org.skellig.teststep.processor.wiremock

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.skellig.teststep.processor.wiremock.WireMockTestApplication.Companion.freePort

@Tag("integration-test")
class WireMockResetIT {
    private val payments = freePort()
    private val identity = freePort()
    private val app = WireMockTestApplication(
        """wiremock { servers = [ { name = payments, port = $payments }, { name = identity, port = $identity } ] }""",
        listOf("wiremock/steps.sts", "wiremock/requests.sts")
    )

    private fun journalCount(runner: org.skellig.teststep.runner.TestStepRunner, server: String) =
        (with(app) { runner.runStep("Read server journal", mapOf("server" to server)) } as Map<*, *>)["count"]

    private fun stubBoth(runner: org.skellig.teststep.runner.TestStepRunner) {
        with(app) {
            runner.runStep("Stub ping")
            runner.runStep("Stub ping on identity")
        }
    }

    @Test
    fun `stubs reset removes stubs but keeps the journal`() {
        app.run { _, runner ->
            stubBoth(runner)
            assertEquals(201, app.call(payments, "/ping").statusCode())
            val result = with(app) { runner.runStep("Reset what", mapOf("server" to "payments", "what" to "stubs")) } as Map<*, *>
            assertEquals(listOf("payments"), result["servers"])
            assertEquals("stubs", result["what"])
            assertEquals(404, app.call(payments, "/ping").statusCode())
            assertEquals(2, journalCount(runner, "payments"))
            assertEquals(202, app.call(identity, "/ping").statusCode())
        }
    }

    @Test
    fun `journal reset only returns newer requests and keeps stubs`() {
        app.run { _, runner ->
            stubBoth(runner)
            app.call(payments, "/ping")
            val result = with(app) { runner.runStep("Reset what", mapOf("server" to "payments", "what" to "journal")) } as Map<*, *>
            assertEquals("journal", result["what"])
            assertEquals(0, journalCount(runner, "payments"))
            assertEquals(201, app.call(payments, "/ping").statusCode())
            assertEquals(1, journalCount(runner, "payments"))
        }
    }

    @Test
    fun `what defaults to all and clears stubs and journal`() {
        app.run { _, runner ->
            stubBoth(runner)
            app.call(payments, "/ping")
            val result = with(app) { runner.runStep("Reset default", mapOf("server" to "payments")) } as Map<*, *>
            assertEquals("all", result["what"])
            assertEquals(0, journalCount(runner, "payments"))
            assertEquals(404, app.call(payments, "/ping").statusCode())
            assertEquals(202, app.call(identity, "/ping").statusCode())
        }
    }

    @Test
    fun `omitting the server resets every server`() {
        app.run { _, runner ->
            stubBoth(runner)
            app.call(payments, "/ping")
            app.call(identity, "/ping")
            val result = with(app) { runner.runStep("Reset all servers", mapOf("what" to "all")) } as Map<*, *>
            assertEquals(listOf("payments", "identity"), result["servers"])
            assertEquals(0, journalCount(runner, "payments"))
            assertEquals(0, journalCount(runner, "identity"))
            assertEquals(404, app.call(identity, "/ping").statusCode())
        }
    }

    @Test
    fun `unknown what and unknown server fail`() {
        app.run { _, runner ->
            fun failure(params: Map<String, Any?>) = assertThrows(Throwable::class.java) {
                with(app) { runner.runStep("Reset what", params) }
            }.let { error -> generateSequence(error) { it.cause }.joinToString(" | ") { it.message.orEmpty() } }
            assertTrue(failure(mapOf("server" to "payments", "what" to "everything")).contains("everything"))
            assertTrue(failure(mapOf("server" to "nope", "what" to "all")).contains("nope"))
        }
    }
}
