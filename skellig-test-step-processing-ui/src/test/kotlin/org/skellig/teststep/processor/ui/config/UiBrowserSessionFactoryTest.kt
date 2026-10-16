package org.skellig.teststep.processor.ui.config

import com.sun.net.httpserver.HttpServer
import com.typesafe.config.ConfigFactory
import org.junit.Test
import org.junit.jupiter.api.Assertions.*
import org.openqa.selenium.json.Json
import java.net.InetSocketAddress
import java.nio.file.Files
import java.util.Base64
import java.util.concurrent.CopyOnWriteArrayList

class UiBrowserSessionFactoryTest {
    @Test
    fun translatesChromeSettingsAtSessionCreation() {
        Endpoint().use { endpoint ->
            val factory = UiBrowserSessionFactory(ConfigFactory.parseString("""
                mode = remote
                url = "${endpoint.url}"
                name = chrome
                headless = true
                version = "stable"
                platform = "linux"
                accept-insecure-certs = true
                page-load-strategy = eager
                binary = "/opt/chrome"
                arguments = ["--disable-dev-shm-usage"]
                preferences { "download.default_directory" = "/downloads" }
                proxy { proxyType = manual, httpProxy = "proxy.example:8080" }
                window { width = 1280, height = 720 }
                timeouts { page-load = 15s, script = 2500ms }
                capabilities {
                    "se:downloadsEnabled" = true
                    "vendor:options" { build = "ui87" }
                    "goog:chromeOptions" { excludeSwitches = ["enable-automation"] }
                }
            """))
            assertTrue(endpoint.requests.isEmpty(), "Configuration must not start a session")
            factory.create().quit()
            val capabilities = endpoint.capabilities()
            assertEquals("chrome", capabilities["browserName"])
            assertEquals("stable", capabilities["browserVersion"])
            assertEquals("linux", capabilities["platformName"])
            assertEquals(true, capabilities["acceptInsecureCerts"])
            assertEquals("eager", capabilities["pageLoadStrategy"])
            assertEquals(mapOf("implicit" to 0L, "pageLoad" to 15000L, "script" to 2500L), capabilities["timeouts"])
            assertEquals(mapOf("proxyType" to "manual", "httpProxy" to "proxy.example:8080"), capabilities["proxy"])
            assertEquals(true, capabilities["se:downloadsEnabled"])
            assertEquals(mapOf("build" to "ui87"), capabilities["vendor:options"])
            val options = capabilities["goog:chromeOptions"] as Map<*, *>
            assertEquals(listOf("--disable-dev-shm-usage", "--headless=new"), options["args"])
            assertEquals("/opt/chrome", options["binary"])
            assertEquals(mapOf("download.default_directory" to "/downloads"), options["prefs"])
            assertEquals(listOf("enable-automation"), options["excludeSwitches"])
            val resize = endpoint.requests.single { it.path.endsWith("/window/rect") }
            assertEquals(1280L, resize.body["width"])
            assertEquals(720L, resize.body["height"])
            assertEquals("DELETE", endpoint.requests.last().method)
        }
    }

    @Test
    fun translatesRemoteBrowserSpecificSettings() {
        listOf("firefox" to "moz:firefoxOptions", "edge" to "ms:edgeOptions", "safari" to null).forEach { (name, vendorKey) ->
            Endpoint().use { endpoint ->
                val specific = if (vendorKey == null) "" else """
                    headless = true
                    binary = "/opt/browser"
                    arguments = ["--private"]
                    preferences { "test.enabled" = true }
                """
                UiBrowserSessionFactory(ConfigFactory.parseString("""
                    mode = remote
                    url = "${endpoint.url}"
                    name = $name
                    timeouts.implicit = 100ms
                    $specific
                """)).create().quit()
                val capabilities = endpoint.capabilities()
                assertEquals(if (name == "edge") "MicrosoftEdge" else name, capabilities["browserName"])
                assertEquals(mapOf("implicit" to 100L), capabilities["timeouts"])
                if (vendorKey != null) {
                    val options = capabilities[vendorKey] as Map<*, *>
                    assertEquals("/opt/browser", options["binary"])
                    assertEquals(listOf("--private", if (name == "firefox") "-headless" else "--headless=new"), options["args"])
                    assertEquals(true, (options["prefs"] as Map<*, *>)["test.enabled"])
                }
            }
        }
    }

    @Test
    fun rejectsConflictingAndUnsupportedSettingsBeforeOpeningSession() {
        val cases = listOf(
            "mode = invalid" to "mode",
            "mode = local, name = firefox" to "Local",
            "mode = local, url = \"http://localhost:4444\"" to "url",
            "mode = remote" to "url",
            "mode = remote, url = \"file:///tmp/driver\"" to "url",
            "remote-url = \"http://localhost:4444\"" to "url",
            "headless = true, arguments = [\"--headless=old\"]" to "headless",
            "window { width = 0, height = 720 }" to "window",
            "window.width = 800" to "window",
            "timeouts.script = -1s" to "timeouts.script",
            "timeouts.page-load = 1ns" to "timeouts.page-load",
            "page-load-strategy = invalid" to "page-load-strategy",
            "capabilities.browserName = firefox" to "browserName",
            "capabilities.timeouts.implicit = 10" to "timeouts",
            "version = stable, capabilities.browserVersion = beta" to "browserVersion",
            "headless = false, capabilities.\"goog:chromeOptions\".args = [\"--headless\"]" to "args",
            "preferences.x = true, capabilities.\"goog:chromeOptions\".prefs.x = false" to "prefs",
            "capabilities.\"moz:firefoxOptions\".args = []" to "moz:firefoxOptions",
            "proxy { proxyType = direct, httpProxy = \"proxy:8080\" }" to "proxy",
            "timeouts.typo = 2s" to "timeouts",
            "unknown = true" to "unknown",
            "mode = remote, url = \"http://localhost:4444\", name = safari, headless = true" to "safari",
            "mode = remote, url = \"http://localhost:4444\", name = firefox, extensions = []" to "extensions"
        )
        cases.forEach { (settings, message) ->
            val error = assertThrows(IllegalArgumentException::class.java, { UiBrowserSessionFactory(ConfigFactory.parseString(settings)) }, settings)
            assertTrue(error.message.orEmpty().contains(message, ignoreCase = true), "$settings: ${error.message}")
        }
    }

    @Test
    fun sendsChromiumExtensionsFromClientFiles() {
        val extension = Files.createTempFile("ui-extension", ".crx")
        try {
            val content = byteArrayOf(1, 2, 3, 4)
            Files.write(extension, content)
            listOf("chrome" to "goog:chromeOptions", "edge" to "ms:edgeOptions").forEach { (name, key) ->
                Endpoint().use { endpoint ->
                    UiBrowserSessionFactory(ConfigFactory.parseString("""
                        mode = remote
                        url = "${endpoint.url}"
                        name = $name
                        extensions = [${com.typesafe.config.ConfigUtil.quoteString(extension.toString())}]
                    """)).create().quit()
                    val options = endpoint.capabilities()[key] as Map<*, *>
                    assertEquals(listOf(Base64.getEncoder().encodeToString(content)), options["extensions"])
                }
            }
        } finally {
            Files.deleteIfExists(extension)
        }
    }

    @Test
    fun preservesAdditionalCapabilitiesAndDefaults() {
        Endpoint().use { endpoint ->
            UiBrowserSessionFactory(ConfigFactory.parseString("""
                mode = remote
                url = "${endpoint.url}"
                capabilities {
                    acceptInsecureCerts = true
                    unhandledPromptBehavior = dismiss
                    "goog:chromeOptions" { args = ["--incognito"], prefs { "custom.pref" = true } }
                }
            """)).create().quit()
            val capabilities = endpoint.capabilities()
            assertEquals("chrome", capabilities["browserName"])
            assertEquals(mapOf("implicit" to 0L), capabilities["timeouts"])
            assertEquals(true, capabilities["acceptInsecureCerts"])
            assertEquals("dismiss", capabilities["unhandledPromptBehavior"])
            val options = capabilities["goog:chromeOptions"] as Map<*, *>
            assertEquals(listOf("--incognito"), options["args"])
            assertEquals(mapOf("custom.pref" to true), options["prefs"])
            assertFalse(endpoint.requests.any { it.path.endsWith("/window/rect") })
        }
    }

    @Test
    fun quitsSessionWhenWindowSetupFails() {
        Endpoint(failResize = true).use { endpoint ->
            val factory = UiBrowserSessionFactory(ConfigFactory.parseString("""
                mode = remote
                url = "${endpoint.url}"
                window { width = 800, height = 600 }
            """))
            val error = assertThrows(org.openqa.selenium.WebDriverException::class.java) { factory.create() }
            assertTrue(error.message.orEmpty().contains("resize rejected"))
            assertEquals("DELETE", endpoint.requests.last().method)
            assertEquals("/session/test-session", endpoint.requests.last().path)
        }
    }

    @Test
    fun rejectsInvalidSocksProxyVersions() {
        listOf(
            "socksProxy = \"proxy:1080\"",
            "socksProxy = \"proxy:1080\", socksVersion = 4.9",
            "socksProxy = \"proxy:1080\", socksVersion = -1",
            "socksProxy = \"proxy:1080\", socksVersion = 256",
            "httpProxy = \"proxy:8080\", socksVersion = 5"
        ).forEach { fields ->
            val error = assertThrows(IllegalArgumentException::class.java) {
                UiBrowserSessionFactory(ConfigFactory.parseString("proxy { proxyType = manual, $fields }"))
            }
            assertTrue(error.message.orEmpty().contains("socks"))
        }
    }

    private data class Request(val method: String, val path: String, val body: Map<String, Any?>)

    private class Endpoint(private val failResize: Boolean = false) : AutoCloseable {
        val requests = CopyOnWriteArrayList<Request>()
        private val json = Json()
        private val server = HttpServer.create(InetSocketAddress("127.0.0.1", 0), 0)
        val url get() = "http://127.0.0.1:${server.address.port}"

        init {
            server.createContext("/") { exchange ->
                val body = exchange.requestBody.bufferedReader().readText()
                val request = Request(exchange.requestMethod, exchange.requestURI.path,
                    if (body.isBlank()) emptyMap() else json.toType(body, Json.MAP_TYPE))
                requests.add(request)
                val failed = failResize && request.path.endsWith("/window/rect")
                val value: Any? = when {
                    failed -> mapOf("error" to "unknown error", "message" to "resize rejected", "stacktrace" to "")
                    request.path == "/session" -> mapOf("sessionId" to "test-session", "capabilities" to mapOf("browserName" to "chrome"))
                    request.path.endsWith("/window/rect") -> mapOf("x" to 0, "y" to 0, "width" to 1280, "height" to 720)
                    else -> null
                }
                val response = json.toJson(mapOf("value" to value)).toByteArray()
                exchange.responseHeaders.set("Content-Type", "application/json")
                exchange.sendResponseHeaders(if (failed) 500 else 200, response.size.toLong())
                exchange.responseBody.use { it.write(response) }
            }
            server.start()
        }

        fun capabilities(): Map<*, *> {
            val request = requests.single { it.path == "/session" }
            val capabilities = request.body["capabilities"] as Map<*, *>
            return capabilities["alwaysMatch"] as? Map<*, *>
                ?: (capabilities["firstMatch"] as List<*>).single() as Map<*, *>
        }

        override fun close() = server.stop(0)
    }
}
