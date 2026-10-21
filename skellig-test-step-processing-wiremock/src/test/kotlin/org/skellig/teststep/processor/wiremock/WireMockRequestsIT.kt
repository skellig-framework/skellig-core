package org.skellig.teststep.processor.wiremock

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.skellig.teststep.processor.wiremock.WireMockTestApplication.Companion.freePort
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

@Tag("integration-test")
class WireMockRequestsIT {
    private val port = freePort()
    private val app = WireMockTestApplication(
        """wiremock { servers = [ { name = payments, port = $port } ] }""",
        listOf("wiremock/steps.sts", "wiremock/requests.sts")
    )

    @Test
    fun `journal exposes detached request fields from real HTTP calls`() {
        app.run { _, runner ->
            val stub = with(app) { runner.runStep("Stub ping") } as Map<*, *>
            app.call(port, "/ping?tag=first&tag=second&encoded=hello%20world")
            val before = System.currentTimeMillis()
            send("/missing", "raw body", "first", "second")
            val result = with(app) { runner.runStep("Read journal") } as Map<*, *>
            assertEquals(2, result["count"])
            val requests = result["requests"] as List<Map<String, Any?>>
            val matched = requests.single { it["path"] == "/ping" }
            assertEquals("GET", matched["method"])
            assertEquals("/ping?tag=first&tag=second&encoded=hello%20world", matched["url"])
            assertEquals(mapOf("tag" to "first", "encoded" to "hello world"), matched["query"])
            assertEquals(true, matched["matched"])
            assertEquals(stub["id"], matched["stubId"])
            val unmatched = requests.single { it["path"] == "/missing" }
            assertEquals("POST", unmatched["method"])
            assertEquals("raw body", unmatched["body"])
            assertEquals(false, unmatched["matched"])
            assertNull(unmatched["stubId"])
            val headers = unmatched["headers"] as Map<String, String>
            assertEquals("first", headers.entries.single { it.key.equals("X-Token", true) }.value)
            assertTrue((unmatched["loggedTime"] as Long) in before..System.currentTimeMillis())
            app.call(port, "/later")
            assertEquals(2, requests.size)
        }
    }

    @Test
    fun `filters share stub matchers and combine with unmatched mode`() {
        app.run { _, runner ->
            with(app) { runner.runStep("Stub ping") }
            app.call(port, "/ping")
            send("/orders/42?currency=GBP", "{\"amount\":10,\"extra\":true}", "secret-token")
            send("/orders/43?currency=USD", "{\"amount\":20}", "other-token")
            val filter = mapOf(
                "method" to "POST", "path" to mapOf("matches" to "/orders/[0-9]+"),
                "query" to mapOf("currency" to "GBP"),
                "headers" to mapOf("X-Token" to mapOf("contains" to "secret")),
                "body" to mapOf("json" to "{\"amount\":10}")
            )
            fun read(filter: Map<String, Any>, unmatched: Boolean = false): Map<*, *> =
                with(app) { runner.runStep("Filter journal", mapOf("filter" to filter, "unmatched" to unmatched)) } as Map<*, *>
            val result = read(filter, true)
            assertEquals(1, result["count"])
            assertEquals("/orders/42", ((result["requests"] as List<*>).single() as Map<*, *>)["path"])
            assertEquals(0, read(filter + ("method" to "GET"))["count"])
            assertEquals(0, read(filter + ("path" to "/other"))["count"])
            assertEquals(0, read(filter + ("query" to mapOf("currency" to "EUR")))["count"])
            assertEquals(0, read(filter + ("headers" to mapOf("X-Token" to "wrong")))["count"])
            assertEquals(0, read(filter + ("body" to mapOf("contains" to "absent")))["count"])
            assertEquals(2, read(emptyMap(), true)["count"])
            assertEquals(1, read(mapOf("path" to "/ping"))["count"])
            assertEquals(0, read(mapOf("path" to "/ping"), true)["count"])
            assertEquals(1, read(mapOf("body" to mapOf("matches" to ".*20.*")))["count"])
        }
    }

    @Test
    fun `a later step can use a value captured from a journal request`() {
        app.run { _, runner ->
            send("/capture", "captured-value")
            with(app) {
                runner.runStep("Capture request")
                runner.runStep("Use captured request")
            }
        }
    }

    @Test
    fun `polling finds a request that arrives after the initial read`() {
        app.run { _, runner ->
            app.call(port, "/late")
            val sender = Executors.newSingleThreadScheduledExecutor()
            try {
                val sent = sender.schedule({ app.call(port, "/late") }, 700, TimeUnit.MILLISECONDS)
                val result = with(app) { runner.runStep("Poll journal", mapOf("attempts" to 30)) } as Map<*, *>
                sent.get(5, TimeUnit.SECONDS)
                assertEquals(2, result["count"])
            } finally {
                sender.shutdownNow()
                assertTrue(sender.awaitTermination(5, TimeUnit.SECONDS))
            }
        }
    }

    @Test
    fun `a missing request exhausts polling and reports the last journal result`() {
        app.run { _, runner ->
            send("/late", "last-result-marker")
            val started = System.nanoTime()
            var result: Any? = null
            var failure: Throwable? = null
            runner.run("Poll journal", mapOf("attempts" to 3)).subscribe { _, value, error ->
                result = value
                failure = error
            }
            assertTrue(TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - started) >= 200)
            assertEquals(1, (result as Map<*, *>)["count"])
            assertNotNull(failure)
            val message = failure!!.message.orEmpty()
            assertTrue(message.contains("count=1"), message)
            assertTrue(message.contains("last-result-marker"), message)
        }
    }

    @Test
    fun `empty journal failures include the last result when extracting validation or state values`() {
        app.run { _, runner ->
            listOf("Poll empty journal with state capture", "Poll empty journal with extraction validation").forEach { name ->
                val failure = app.run(runner, name)
                assertNotNull(failure)
                val message = failure!!.message.orEmpty()
                assertTrue(message.contains("count=0") && message.contains("requests=[]"), message)
            }
        }
    }

    private fun send(path: String, body: String, vararg tokens: String) {
        val request = HttpRequest.newBuilder(URI.create("http://127.0.0.1:$port$path"))
            .POST(HttpRequest.BodyPublishers.ofString(body))
        tokens.forEach { request.header("X-Token", it) }
        HttpClient.newHttpClient().send(request.build(), HttpResponse.BodyHandlers.ofString())
    }
}
