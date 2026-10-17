package org.skellig.teststep.processor.ui

import com.sun.net.httpserver.HttpServer
import com.typesafe.config.Config
import com.typesafe.config.ConfigFactory
import com.typesafe.config.ConfigUtil
import org.junit.Test
import org.junit.jupiter.api.Assertions.*
import org.openqa.selenium.json.Json
import org.skellig.teststep.runner.context.SkelligTestContext
import java.net.InetSocketAddress
import java.nio.file.Files
import java.util.Base64
import java.util.concurrent.Callable
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.Executors

/** Public context execution against a deterministic remote WebDriver protocol endpoint. */
class UiDiagnosticsTest {
    @Test
    fun `remote screenshots are persisted locally with unique names across concurrent contexts`() {
        val directory = Files.createTempDirectory(java.nio.file.Path.of("."), "ui-relative-")
        try {
            Endpoint().use { endpoint ->
                val executor = Executors.newFixedThreadPool(4)
                try {
                    val failures = executor.invokeAll((1..8).map {
                        Callable { execute(endpoint, directory.resolve("nested").toString()) }
                    }).map { it.get() }
                    assertEquals(8, failures.map { it.screenshot }.toSet().size)
                    failures.forEach {
                        assertTrue(it.screenshot!!.isAbsolute)
                        assertArrayEquals(endpoint.png, Files.readAllBytes(it.screenshot))
                        assertEquals(UiFailureStage.ACTION, it.stage)
                        assertEquals("navigate", it.operation)
                        assertEquals(1, it.actionIndex)
                    }
                    assertEquals(8L, Files.list(directory.resolve("nested")).use { it.count() })
                } finally { executor.shutdownNow() }
            }
        } finally { directory.toFile().deleteRecursively() }
    }

    @Test
    fun `URL screenshot and cleanup errors remain secondary to original action failure`() {
        val directory = Files.createTempDirectory("ui-secondary")
        try {
            Endpoint(failEvidence = true).use { endpoint ->
                val failure = execute(endpoint, directory.toString())
                assertEquals(UiFailureStage.ACTION, failure.stage)
                assertTrue(failure.originalCause.stackTraceToString().contains("navigation rejected"))
                assertNull(failure.url)
                assertNull(failure.screenshot)
                assertEquals(3, failure.suppressed.size)
                assertTrue(failure.suppressed.any { it.message.orEmpty().contains("screenshot rejected") })
                assertTrue(failure.suppressed.any { it.message.orEmpty().contains("cleanup rejected") })
                assertTrue(endpoint.requests.indexOf("GET /session/test/screenshot") < endpoint.requests.indexOf("DELETE /session/test"))
                assertEquals(0L, Files.list(directory).use { it.count() })
            }
        } finally { directory.toFile().deleteRecursively() }
    }

    private fun execute(endpoint: Endpoint, directory: String): UiFailure {
        val config = ConfigFactory.parseString("""
            ui {
                browser { mode = remote, url = "${endpoint.url}" }
                screenshots.path = ${ConfigUtil.quoteString(directory)}
                pages.home { path = "http://example.test", elements.next = "//a" }
            }
        """)
        var failure: UiFailure? = null
        val context = object : SkelligTestContext() {
            override fun createConfig(classLoader: ClassLoader, configPath: String?): Config = config
        }
        context.use {
            val runner = it.initialize(javaClass.classLoader, listOf("ui/steps.sts"))
            runner.run("Open home").subscribe { _, response, error ->
                failure = error as UiFailure
                assertEquals(listOfNotNull(failure!!.screenshot?.toString()), (response as Map<*, *>)["artifacts"])
            }
        }
        context.close()
        return failure!!
    }

    private class Endpoint(private val failEvidence: Boolean = false) : AutoCloseable {
        val png = Base64.getDecoder().decode("iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mP8/x8AAwMCAO+aD1sAAAAASUVORK5CYII=")
        val requests = CopyOnWriteArrayList<String>()
        private val server = HttpServer.create(InetSocketAddress("127.0.0.1", 0), 0)
        val url get() = "http://127.0.0.1:${server.address.port}"
        init {
            server.createContext("/") { exchange ->
                val method = exchange.requestMethod
                val path = exchange.requestURI.path
                exchange.requestBody.close()
                requests.add("$method $path")
                val error = when {
                    method == "POST" && path.endsWith("/url") -> "navigation rejected"
                    failEvidence && path.endsWith("/screenshot") -> "screenshot rejected"
                    failEvidence && method == "GET" && path.endsWith("/url") -> "URL rejected"
                    failEvidence && method == "DELETE" -> "cleanup rejected"
                    else -> null
                }
                val value: Any? = when {
                    error != null -> mapOf("error" to "unknown error", "message" to error, "stacktrace" to "")
                    path == "/session" -> mapOf("sessionId" to "test", "capabilities" to mapOf("browserName" to "chrome"))
                    path.endsWith("/screenshot") -> Base64.getEncoder().encodeToString(png)
                    path.endsWith("/url") -> "http://example.test/failure"
                    else -> null
                }
                val bytes = Json().toJson(mapOf("value" to value)).toByteArray()
                exchange.responseHeaders.set("Content-Type", "application/json")
                exchange.sendResponseHeaders(if (error == null) 200 else 500, bytes.size.toLong())
                exchange.responseBody.use { it.write(bytes) }
            }
            server.start()
        }
        override fun close() = server.stop(0)
    }
}
