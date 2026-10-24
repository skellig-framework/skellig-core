package org.skellig.teststep.processor.wiremock

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.skellig.teststep.processor.wiremock.WireMockTestApplication.Companion.freePort

@Tag("integration-test")
class WireMockStubIT {

    private val payments = freePort()
    private val identity = freePort()
    private val two = """wiremock { servers = [ { name = payments, port = $payments }, { name = identity, port = $identity } ] }"""

    @Test
    fun `a stub answers a real http call with the defined response and returns its id, server and port`() {
        val app = WireMockTestApplication(two)
        app.run { _, runner ->
            val result = with(app) { runner.runStep("Stub ping") } as Map<*, *>
            assertEquals("payments", result["server"])
            assertEquals(payments, result["port"])
            assertNotNull(result["id"])

            val response = app.call(payments, "/ping")
            assertEquals(201, response.statusCode())
            assertEquals("pong", response.body())
            assertEquals("first", response.headers().firstValue("X-Stub").orElse(null))
        }
    }

    @Test
    fun `re-running a step id replaces the earlier stub`() {
        val app = WireMockTestApplication(two)
        app.run { _, runner ->
            val first = with(app) { runner.runStep("Stub ping") } as Map<*, *>
            val second = with(app) { runner.runStep("Stub ping again") } as Map<*, *>

            assertEquals(first["id"], second["id"])
            assertEquals("pong again", app.call(payments, "/ping").body())
            assertEquals(200, app.call(payments, "/ping").statusCode())
        }
    }

    @Test
    fun `the same step id on two servers is independent`() {
        val app = WireMockTestApplication(two)
        app.run { _, runner ->
            with(app) {
                runner.runStep("Stub ping")
                runner.runStep("Stub ping on identity")
            }
            assertEquals("pong", app.call(payments, "/ping").body())
            assertEquals("identity pong", app.call(identity, "/ping").body())
        }
    }

    @Test
    fun `steps without an id are never replaced`() {
        val app = WireMockTestApplication(two)
        app.run { _, runner ->
            with(app) {
                runner.runStep("Stub anonymous one")
                runner.runStep("Stub anonymous two")
            }
            assertEquals("one", app.post(payments, "/anon/one").body())
            assertEquals("two", app.post(payments, "/anon/two").body())
        }
    }

    @Test
    fun `an unknown server fails listing the configured names`() {
        val app = WireMockTestApplication(two)
        app.run { _, runner ->
            val message = app.run(runner, "Stub on unknown server")?.message.orEmpty()
            assertTrue(message.contains("nowhere") && message.contains("payments") && message.contains("identity"), message)
        }
    }

    @Test
    fun `the server key is optional when exactly one server is configured`() {
        val app = WireMockTestApplication("""wiremock { servers = [ { name = only, port = $payments } ] }""")
        app.run { _, runner ->
            with(app) { runner.runStep("Stub without server") }
            assertEquals("lone", app.call(payments, "/lone").body())
        }
    }

    @Test
    fun `the server key is required when several servers are configured`() {
        val app = WireMockTestApplication(two)
        app.run { _, runner ->
            val message = app.run(runner, "Stub without server")?.message.orEmpty()
            assertTrue(message.contains("payments") && message.contains("identity"), message)
        }
    }

    @Test
    fun `a step with wiremock beside url and method goes to the mock server and plain http steps are unaffected`() {
        val app = WireMockTestApplication(
            """wiremock { servers = [ { name = payments, port = $payments } ] }
               http { services = [ { serviceName = svc, url = "http://127.0.0.1:$payments" } ] }"""
        )
        app.run { _, runner ->
            with(app) { runner.runStep("Stub with url and method keys") }
            assertEquals("mock", app.call(payments, "/clash").body())

            with(app) { runner.runStep("Stub ping") }
            val httpResult = with(app) { runner.runStep("Plain http call") } as org.skellig.teststep.processor.http.model.HttpResponse
            assertEquals(201, httpResult.statusCode)
            assertEquals("pong", httpResult.body)
        }
    }

    @Test
    fun `an inline raw json stub answers a real http call`() {
        val app = WireMockTestApplication(two)
        app.run { _, runner ->
            val result = with(app) { runner.runStep("Stub raw json inline") } as Map<*, *>
            assertNotNull(result["id"])

            val response = app.call(payments, "/raw")
            assertEquals(202, response.statusCode())
            assertEquals("raw inline", response.body())
            assertEquals("yes", response.headers().firstValue("X-Raw").orElse(null))
        }
    }

    @Test
    fun `a raw json stub read from a file answers a real http call`() {
        val app = WireMockTestApplication(two)
        app.run { _, runner ->
            with(app) { runner.runStep("Stub raw json from file") }

            val response = app.call(payments, "/raw-file")
            assertEquals(203, response.statusCode())
            assertEquals("raw from file", response.body())
        }
    }

    @Test
    fun `re-running a raw json step id replaces the earlier stub`() {
        val app = WireMockTestApplication(two)
        app.run { _, runner ->
            val first = with(app) { runner.runStep("Stub raw json inline") } as Map<*, *>
            val second = with(app) { runner.runStep("Stub raw json inline again") } as Map<*, *>

            assertEquals(first["id"], second["id"])
            assertEquals("raw replaced", app.call(payments, "/raw").body())
        }
    }

    @Test
    fun `invalid raw json fails the step with a clear message`() {
        val app = WireMockTestApplication(two)
        app.run { _, runner ->
            val message = app.run(runner, "Stub raw json invalid")?.message.orEmpty()
            assertTrue(message.contains("Invalid wiremock stub JSON"), message)
        }
    }

    @Test
    fun `a raw json null mapping fails the step with a clear message`() {
        val app = WireMockTestApplication(two)
        app.run { _, runner ->
            val message = app.run(runner, "Stub raw json null")?.message.orEmpty()
            assertTrue(message.contains("Invalid wiremock stub JSON") && message.contains("request"), message)
        }
    }
}
