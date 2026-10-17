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
import java.util.concurrent.CopyOnWriteArrayList

/** Public context execution against a deterministic remote WebDriver protocol endpoint. */
class UiDiagnosticsTest {
    @Test
    fun `captures only before and after the complete action sequence`() {
        val directory = Files.createTempDirectory(java.nio.file.Path.of("."), "ui-relative-")
        try {
            Endpoint().use { endpoint ->
                val destination = directory.resolve("nested")
                assertNull(execute(endpoint, destination.toString()))
                assertEquals(listOf("screenshot", "navigate", "navigate", "screenshot"), endpoint.actions())
                val files = Files.list(destination).use { it.toList() }
                assertEquals(2, files.size)
                listOf("before", "after").forEach { phase ->
                    val file = files.single { it.fileName.toString().startsWith(phase) }
                    assertTrue(file.fileName.toString().matches(Regex(
                        "${phase}_Navigate twice for screenshots_[0-9]{4}-[0-9]{2}-[0-9]{2}_[0-9]{2}-[0-9]{2}-[0-9]{2}-[0-9]{9}\\.png")))
                    assertArrayEquals(endpoint.png, Files.readAllBytes(file))
                }
            }
        } finally { directory.toFile().deleteRecursively() }
    }

    @Test
    fun `captures after failed actions without replacing their failure`() {
        val directory = Files.createTempDirectory("ui-failed-action")
        try {
            Endpoint(failAction = true).use { endpoint ->
                val failure = execute(endpoint, directory.toString())
                assertTrue(failure!!.stackTraceToString().contains("navigation rejected"))
                assertEquals(listOf("screenshot", "navigate", "screenshot"), endpoint.actions())
                assertEquals(2L, Files.list(directory).use { it.count() })
            }
        } finally { directory.toFile().deleteRecursively() }
    }

    @Test
    fun `screenshot errors do not prevent actions or replace action errors`() {
        val directory = Files.createTempDirectory("ui-screenshot-error")
        try {
            listOf(false, true).forEach { failAction ->
                Endpoint(failAction = failAction, failEvidence = true).use { endpoint ->
                    val failure = execute(endpoint, directory.toString())
                    if (failAction) assertTrue(failure!!.stackTraceToString().contains("navigation rejected"))
                    else assertNull(failure)
                    assertEquals(2, endpoint.actions().count { it == "screenshot" })
                }
            }
            assertEquals(0L, Files.list(directory).use { it.count() })
        } finally { directory.toFile().deleteRecursively() }
    }

    @Test
    fun `persistence errors do not prevent execution`() {
        val file = Files.createTempFile("ui-not-directory", ".txt")
        try {
            Endpoint().use { endpoint ->
                assertNull(execute(endpoint, file.toString()))
                assertEquals(2, endpoint.actions().count { it == "navigate" })
                assertEquals(0L, Files.size(file))
            }
        } finally { Files.delete(file) }
    }

    private fun execute(endpoint: Endpoint, directory: String): RuntimeException? {
        val config = ConfigFactory.parseString("""
            ui {
                browser { mode = remote, url = "${endpoint.url}" }
                screenshots.path = ${ConfigUtil.quoteString(directory)}
                pages.home { path = "http://example.test", elements.next = "//a" }
            }
        """)
        var failure: RuntimeException? = null
        val context = object : SkelligTestContext() {
            override fun createConfig(classLoader: ClassLoader, configPath: String?): Config = config
        }
        context.use {
            val runner = it.initialize(javaClass.classLoader, listOf("ui/steps.sts"))
            runner.run("Navigate twice for screenshots").subscribe { _, response, error ->
                failure = error
                if (error == null) assertEquals(setOf("url", "pages"), (response as Map<*, *>).keys)
            }
        }
        return failure
    }

    private class Endpoint(private val failEvidence: Boolean = false, private val failAction: Boolean = false) : AutoCloseable {
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
                    failAction && method == "POST" && path.endsWith("/url") -> "navigation rejected"
                    failEvidence && path.endsWith("/screenshot") -> "screenshot rejected"
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
        fun actions() = requests.mapNotNull {
            when (it) {
                "GET /session/test/screenshot" -> "screenshot"
                "POST /session/test/url" -> "navigate"
                else -> null
            }
        }

        override fun close() = server.stop(0)
    }
}
