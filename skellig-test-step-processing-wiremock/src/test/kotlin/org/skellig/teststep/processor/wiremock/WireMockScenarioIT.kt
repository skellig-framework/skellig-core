package org.skellig.teststep.processor.wiremock

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.skellig.teststep.processor.http.model.HttpResponse
import org.skellig.teststep.processor.wiremock.WireMockTestApplication.Companion.freePort
import org.skellig.teststep.processor.wiremock.WireMockTestApplication.Companion.isFree
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse.BodyHandlers
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

@Tag("integration-test")
class WireMockScenarioIT {
    private val mock = freePort()
    private val app = WireMockTestApplication(
        """wiremock { servers = [ { name = payments, port = $mock } ] }
           http { services = [ { serviceName = svc, url = "http://127.0.0.1:$mock" } ] }""",
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
                    HttpClient.newHttpClient().send(
                        HttpRequest.newBuilder(URI.create("http://127.0.0.1:$mock/payments"))
                            .header("Content-Type", "application/json")
                            .POST(HttpRequest.BodyPublishers.ofString("{\"amount\": 10, \"note\": \"extra\"}")).build(),
                        BodyHandlers.ofString()
                    )
                }, 500, TimeUnit.MILLISECONDS)
                val identity = app.call(mock, "/users/42")
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
            app.call(mock, "/unexpected")
            assertNotNull(app.run(runner, "Check no unexpected calls"))

            with(app) {
                runner.runStep("Clean slate")
                runner.runStep("Empty journal")
            }
            assertEquals(404, app.call(mock, "/users/42").statusCode())
            assertFalse(isFree(mock))
        }
        assertTrue(isFree(mock), "Port $mock still in use after the context closed")
    }
}
