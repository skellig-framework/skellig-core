package org.skellig.teststep.processor.wiremock

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import java.net.URI
import java.net.URLEncoder
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

@Tag("integration-test")
class WireMockMatchingIT {
    private val port = WireMockTestApplication.freePort()
    private val app = WireMockTestApplication(
        """wiremock { servers = [ { name = only, port = $port } ] }""",
        listOf("wiremock/matching.sts")
    )
    private val client = HttpClient.newHttpClient()

    @Test
    fun `response delay is observed by the HTTP client`() {
        app.run { _, runner ->
            with(app) { runner.run("Stub delayed") }
            // Warm up the connection so initial connection setup is not mistaken for the delay.
            app.call(port, "/warmup")
            val start = System.nanoTime()
            val response = app.call(port, "/delayed")
            val elapsedMillis = (System.nanoTime() - start) / 1_000_000
            assertEquals(200, response.statusCode())
            assertEquals("delayed", response.body())
            assertTrue(elapsedMillis >= 250, "Expected at least 250 ms, got $elapsedMillis ms")
        }
    }

    @Test
    fun `higher priority beats a newer general stub and equal priorities use latest registration`() {
        app.run { _, runner ->
            fun register(id: String, priority: Int, path: Any, reply: String) {
                with(app) { runner.run("Stub priority", mapOf("stubId" to id, "priority" to priority, "path" to path, "reply" to reply)) }
            }
            register("specific", 1, "/priority", "specific")
            register("general", 5, mapOf("matches" to "/.*"), "general")
            assertEquals("specific", app.call(port, "/priority").body())
            assertEquals("general", app.call(port, "/other").body())
            register("tie", 1, "/priority", "latest")
            assertEquals("latest", app.call(port, "/priority").body())
            register("specific", 1, "/priority", "registered again")
            assertEquals("registered again", app.call(port, "/priority").body())
        }
    }

    @Test
    fun `path regex and nested matchers combine and every request part must match`() {
        app.run { _, runner ->
            with(app) { runner.run("Stub combined matchers") }
            fun send(
                path: String = "/orders/123", query: String = "currency=GBP",
                token: String = "a-secret-token", body: String = """{"amount":10,"extra":true}""",
                method: String = "POST"
            ): Int {
                val request = HttpRequest.newBuilder(URI.create("http://127.0.0.1:$port$path?$query"))
                    .header("X-Token", token).method(method, HttpRequest.BodyPublishers.ofString(body)).build()
                return client.send(request, HttpResponse.BodyHandlers.ofString()).statusCode()
            }
            assertEquals(200, send())
            assertEquals(404, send(path = "/orders/no"))
            assertEquals(404, send(query = "currency=USD"))
            assertEquals(404, send(token = "wrong"))
            assertEquals(404, send(body = """{"amount":11}"""))
            assertEquals(404, send(method = "PUT"))

            with(app) { runner.run("Stub matching request", mapOf("request" to mapOf("method" to "GET", "path" to "/literal.+"))) }
            assertEquals(200, app.call(port, "/literal.+?unmatched=query").statusCode())
            assertEquals(404, app.call(port, "/literalXYZ").statusCode())
        }
    }

    @Test
    fun `query headers and body match plain equality contains regex and JSON and reject mismatches`() {
        val cases = listOf(
            Triple<Any, String, String>("hello", "hello", "hello!"),
            Triple(mapOf("contains" to "hello"), "say hello!", "goodbye"),
            Triple(mapOf("matches" to "hello-[0-9]+"), "hello-12", "hello-no"),
            Triple(mapOf("json" to """{"id":1}"""), """{"extra":true,"id":1}""", """{"id":2}""")
        )
        app.run { _, runner ->
            for (part in listOf("query", "headers", "body")) {
                for ((matcher, matching, rejected) in cases) {
                    val request = mapOf("method" to "POST", "path" to "/match", part to
                        if (part == "body") matcher else mapOf("value" to matcher))
                    with(app) { runner.run("Stub matching request", mapOf("request" to request)) }
                    assertEquals(200, call(part, matching).statusCode(), "$part $matcher should match")
                    assertEquals(404, call(part, rejected).statusCode(), "$part $matcher should reject")
                    assertEquals(404, call("none", "").statusCode(), "$part $matcher requires a value")
                }
            }
        }
    }

    private fun call(part: String, value: String): HttpResponse<String> {
        val query = if (part == "query") "?value=" + URLEncoder.encode(value, Charsets.UTF_8) else ""
        val request = HttpRequest.newBuilder(URI.create("http://127.0.0.1:$port/match$query"))
        if (part == "headers") request.header("value", value)
        request.POST(HttpRequest.BodyPublishers.ofString(if (part == "body") value else ""))
        return client.send(request.build(), HttpResponse.BodyHandlers.ofString())
    }
}
