package org.skellig.teststep.processor.wiremock

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.skellig.teststep.processor.http.model.HttpResponse
import org.skellig.teststep.processor.wiremock.WireMockTestApplication.Companion.freePort
import org.skellig.teststep.processor.wiremock.WireMockTestApplication.Companion.isFree
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

@Tag("integration-test")
class WireMockScenarioIT {
    private val serverPort = freePort()
    private val app = WireMockTestApplication(
        """wiremock { servers = [ { name = payments, port = $serverPort } ] }
           http { services = [ { serviceName = svc, url = "http://127.0.0.1:$serverPort" } ] }""",
        listOf("wiremock/steps.sts", "wiremock/scenario.sts")
    )

    @Test
    fun `a full scenario stubs, serves, polls the journal, captures state, resets and frees the port on close`() {
        app.run { _, runner ->
            with(app) {
                runner.runStep("Stub payment accepted")
                runner.runStep("Stub identity lookup")
            }

            val sender = Executors.newSingleThreadScheduledExecutor()
            try {
                // The system under test calls the stubs, the payment call arriving after polling has started.
                val payment = sender.schedule<java.net.http.HttpResponse<String>>({
                    app.post(serverPort, "/payments", "{\"amount\":10}")
                }, 500, TimeUnit.MILLISECONDS)

                val identity = app.call(serverPort, "/users/42")
                assertEquals("Ada", identity.body())

                with(app) {
                    runner.runStep("Wait for payment request")
                    runner.runStep("Check no unexpected calls")
                    runner.runStep("Check captured payment")
                }

                val paymentResponse = payment.get(5, TimeUnit.SECONDS)
                assertEquals(201, paymentResponse.statusCode())
                assertEquals("{\"status\": \"accepted\"}", paymentResponse.body())
            } finally {
                sender.shutdownNow()
            }

            // An existing HTTP step still goes to the HTTP processor while the mock server module is active.
            with(app) { runner.runStep("Stub ping") }
            val http = with(app) { runner.runStep("Plain http call") } as HttpResponse
            assertEquals(201, http.statusCode)
            assertEquals("pong", http.body)

            // An unexpected call shows up as unmatched.
            app.call(serverPort, "/unexpected")
            assertNotNull(app.run(runner, "Check no unexpected calls"))

            with(app) {
                runner.runStep("Clean slate")
                runner.runStep("Empty journal")
            }
            assertEquals(404, app.call(serverPort, "/users/42").statusCode())
            assertFalse(isFree(serverPort))
        }
        assertTrue(isFree(serverPort), "Port $serverPort still in use after the context closed")
    }
}
