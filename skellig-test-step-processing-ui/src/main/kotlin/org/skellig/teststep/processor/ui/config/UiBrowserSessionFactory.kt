package org.skellig.teststep.processor.ui.config

import com.typesafe.config.Config
import org.openqa.selenium.Dimension
import org.openqa.selenium.MutableCapabilities
import org.openqa.selenium.PageLoadStrategy
import org.openqa.selenium.Proxy
import org.openqa.selenium.WebDriver
import org.openqa.selenium.chrome.ChromeDriver
import org.openqa.selenium.chrome.ChromeOptions
import org.openqa.selenium.chromium.ChromiumOptions
import org.openqa.selenium.edge.EdgeOptions
import org.openqa.selenium.firefox.FirefoxOptions
import org.openqa.selenium.safari.SafariOptions
import org.openqa.selenium.remote.AbstractDriverOptions
import org.openqa.selenium.remote.RemoteWebDriver
import java.nio.file.Files
import java.nio.file.Path
import java.util.Base64
import java.net.URI
import java.net.URL
import java.time.Duration

internal class UiBrowserSessionFactory(private val config: Config) {
    private val mode = if (config.hasPath("mode")) config.getString("mode") else "local"
    private val name = if (config.hasPath("name")) config.getString("name") else "chrome"
    private val endpoint: URL?
    private val window: Dimension?
    private val capabilities: MutableCapabilities

    init {
        require(!config.hasPathOrNull("remote-url")) { "Use ui.browser.url, not remote-url" }
        checkKeys(
            config, setOf(
                "mode", "name", "url", "headless", "version", "platform", "accept-insecure-certs",
                "page-load-strategy", "proxy", "binary", "arguments", "preferences", "extensions", "window", "timeouts", "capabilities"
            ), "ui.browser"
        )
        require(mode in setOf("local", "remote")) { "ui.browser.mode must be local or remote" }
        require(mode == "remote" || name == "chrome") { "Local execution supports only Chrome; use ui.browser.mode = remote for $name" }
        require(mode == "remote" || !config.hasPath("url")) { "ui.browser.url requires remote mode" }
        endpoint = if (mode == "remote") {
            require(config.hasPath("url")) { "ui.browser.url is required in remote mode" }
            val uri = runCatching { URI(config.getString("url")) }.getOrNull()
            require(uri != null && uri.scheme in setOf("http", "https") && !uri.host.isNullOrBlank() && uri.fragment == null) {
                "ui.browser.url must be an absolute HTTP(S) WebDriver endpoint without a fragment"
            }
            uri.toURL()
        } else null
        window = readWindow()
        val options: AbstractDriverOptions<*> = when (name) {
            "chrome" -> ChromeOptions()
            "edge" -> EdgeOptions()
            "firefox" -> FirefoxOptions()
            "safari" -> SafariOptions()
            else -> throw IllegalArgumentException("ui.browser.name must be chrome, firefox, edge or safari")
        }
        configureBrowser(options)
        if (config.hasPath("version")) options.setBrowserVersion(config.getString("version"))
        if (config.hasPath("platform")) options.setPlatformName(config.getString("platform"))
        if (config.hasPath("accept-insecure-certs")) options.setAcceptInsecureCerts(config.getBoolean("accept-insecure-certs"))
        if (config.hasPath("page-load-strategy")) options.setPageLoadStrategy(requireNotNull(PageLoadStrategy.fromString(config.getString("page-load-strategy"))) {
            "ui.browser.page-load-strategy must be normal, eager or none"
        })
        if (config.hasPath("proxy")) options.setCapability("proxy", proxy(config.getObject("proxy").unwrapped()))
        if (config.hasPath("timeouts")) checkKeys(config.getConfig("timeouts"), setOf("implicit", "page-load", "script"), "ui.browser.timeouts")
        options.setImplicitWaitTimeout(timeout("implicit") ?: Duration.ZERO)
        timeout("page-load")?.let { options.setPageLoadTimeout(it) }
        timeout("script")?.let { options.setScriptTimeout(it) }
        capabilities = additionalCapabilities(options)
    }

    fun create(): WebDriver {
        val driver = if (endpoint == null) ChromeDriver(ChromeOptions().merge(capabilities)) else RemoteWebDriver(endpoint, capabilities)
        try {
            window?.let { driver.manage().window().size = it }
            return driver
        } catch (failure: Throwable) {
            try {
                driver.quit()
            } catch (cleanup: Throwable) {
                failure.addSuppressed(cleanup)
            }
            throw failure
        }
    }

    private fun configureBrowser(options: AbstractDriverOptions<*>) {
        val headless = config.hasPath("headless") && config.getBoolean("headless")
        if (name == "safari") {
            require(!headless && listOf("binary", "arguments", "preferences", "extensions").none(config::hasPath)) {
                "Safari does not support headless, binary, arguments, preferences or extensions through ui.browser"
            }
        }
        require(name != "firefox" || !config.hasPath("extensions")) { "ui.browser.extensions is supported only for Chrome and Edge" }
        val arguments = if (config.hasPath("arguments")) config.getStringList("arguments").toMutableList() else mutableListOf()
        validateArguments(arguments)
        if (headless) arguments.add(if (name == "firefox") "-headless" else "--headless=new")
        when (options) {
            is ChromiumOptions<*> -> {
                options.addArguments(arguments)
                if (config.hasPath("extensions")) config.getStringList("extensions").forEach { file ->
                    val path = Path.of(file)
                    require(Files.isRegularFile(path) && Files.isReadable(path)) { "ui.browser.extensions requires readable packaged extension files: $file" }
                    options.addEncodedExtensions(Base64.getEncoder().encodeToString(Files.readAllBytes(path)))
                }
                if (config.hasPath("binary")) options.setBinary(config.getString("binary"))
                if (config.hasPath("preferences")) options.setExperimentalOption("prefs", config.getObject("preferences").unwrapped())
            }

            is FirefoxOptions -> {
                options.addArguments(arguments)
                if (config.hasPath("binary")) options.setBinary(config.getString("binary"))
                if (config.hasPath("preferences")) config.getObject("preferences").unwrapped().forEach { (key, value) ->
                    require(value is String || value is Boolean || value is Int) { "Firefox preference '$key' must be a string, boolean or integer" }
                    options.addPreference(key, value)
                }
            }
        }
    }

    private fun validateArguments(arguments: List<String>) {
        require(!config.hasPath("headless") || arguments.none { it.trimStart('-').startsWith("headless") }) {
            "ui.browser.headless conflicts with a headless argument"
        }
        require(window == null || arguments.none {
            it.trimStart('-').substringBefore('=') in setOf(
                "window-size",
                "start-maximized",
                "start-fullscreen",
                "kiosk",
                "width",
                "height"
            )
        }) {
            "ui.browser.window conflicts with window sizing arguments"
        }
    }

    private fun additionalCapabilities(options: AbstractDriverOptions<*>): MutableCapabilities {
        val result = MutableCapabilities(options)
        if (!config.hasPath("capabilities")) return result
        val vendorKey = when (name) {
            "chrome" -> "goog:chromeOptions"; "edge" -> "ms:edgeOptions"; "firefox" -> "moz:firefoxOptions"; else -> null
        }
        val managed = mapOf(
            "browserVersion" to "version",
            "platformName" to "platform",
            "acceptInsecureCerts" to "accept-insecure-certs",
            "pageLoadStrategy" to "page-load-strategy",
            "proxy" to "proxy"
        )
        val standard = managed.keys + setOf("browserName", "timeouts", "setWindowRect", "strictFileInteractability", "unhandledPromptBehavior", "webSocketUrl")
        config.getObject("capabilities").unwrapped().forEach { (key, value) ->
            require(value != null) { "ui.browser.capabilities.$key must not be null" }
            require(':' in key || key in standard) { "Unknown standard capability '$key'; vendor/provider capabilities must use a namespaced key" }
            require(key !in setOf("browserName", "timeouts") && managed[key]?.let(config::hasPath) != true) {
                "ui.browser.capabilities.$key conflicts with managed browser settings; use the corresponding ui.browser field"
            }
            require(key !in setOf("goog:chromeOptions", "ms:edgeOptions", "moz:firefoxOptions") || key == vendorKey) {
                "Capability '$key' is not supported for browser '$name'"
            }
            when (key) {
                vendorKey -> {
                    require(value is Map<*, *>) { "ui.browser.capabilities.$key must be an object" }
                    val fields = mapOf("args" to "arguments", "binary" to "binary", "prefs" to "preferences", "extensions" to "extensions")
                    value.keys.forEach { field ->
                        require(fields[field]?.let(config::hasPath) != true && !(field == "args" && config.hasPath("headless"))) {
                            "ui.browser.capabilities.$key.$field conflicts with managed browser settings"
                        }
                    }
                    value["args"]?.let { args ->
                        require(args is List<*> && args.all { it is String }) { "ui.browser.capabilities.$key.args must be a string array" }
                        validateArguments(args.map { it as String })
                    }
                    val current = result.getCapability(key) as? Map<*, *> ?: emptyMap<Any, Any>()
                    result.setCapability(key, current + value)
                }

                "proxy" -> {
                    require(value is Map<*, *>) { "ui.browser.capabilities.proxy must be an object" }
                    result.setCapability(key, proxy(value))
                }

                else -> result.setCapability(key, value)
            }
        }
        return result
    }

    private fun readWindow(): Dimension? {
        if (!config.hasPath("window")) return null
        val settings = config.getConfig("window")
        checkKeys(settings, setOf("width", "height"), "ui.browser.window")
        require(settings.hasPath("width") && settings.hasPath("height")) { "ui.browser.window requires both width and height" }
        val size = Dimension(settings.getInt("width"), settings.getInt("height"))
        require(size.width > 0 && size.height > 0) { "ui.browser.window dimensions must be positive" }
        return size
    }

    private fun timeout(name: String): Duration? {
        val path = "timeouts.$name"
        if (!config.hasPath(path)) return null
        val duration = config.getDuration(path)
        val millis = runCatching { duration.toMillis() }.getOrNull()
        require(millis != null && millis in 0..9007199254740991L && Duration.ofMillis(millis) == duration) {
            "ui.browser.$path must be a non-negative whole number of milliseconds within the WebDriver range"
        }
        return duration
    }

    private fun proxy(value: Map<*, *>): Map<String, Any> {
        val allowed = setOf("proxyType", "httpProxy", "sslProxy", "socksProxy", "socksVersion", "noProxy", "proxyAutoconfigUrl")
        require(value.keys.all { it in allowed }) { "Unsupported ui.browser.proxy field" }
        val type = value["proxyType"]
        require(type in setOf("direct", "manual", "pac", "autodetect", "system")) { "ui.browser.proxy.proxyType must be direct, manual, pac, autodetect or system" }
        val manual = setOf("httpProxy", "sslProxy", "socksProxy", "socksVersion", "noProxy")
        require(type == "manual" || value.keys.none { it in manual }) { "ui.browser.proxy manual fields require proxyType = manual" }
        require(type == "pac" || "proxyAutoconfigUrl" !in value) { "ui.browser.proxy.proxyAutoconfigUrl requires proxyType = pac" }
        require(type != "pac" || value["proxyAutoconfigUrl"] is String) { "ui.browser.proxy requires proxyAutoconfigUrl for pac" }
        require(type != "manual" || listOf("httpProxy", "sslProxy", "socksProxy").any { value[it] is String }) { "ui.browser.proxy manual mode requires a proxy address" }
        val socksVersion = value["socksVersion"]
        if ("socksProxy" in value) {
            require(socksVersion is Int && socksVersion in 0..255) { "ui.browser.proxy.socksVersion must be an integer from 0 to 255 when socksProxy is set" }
        } else {
            require("socksVersion" !in value) { "ui.browser.proxy.socksVersion requires socksProxy" }
        }
        @Suppress("UNCHECKED_CAST")
        return try {
            Proxy(value as Map<String, Any>).toJson()
        } catch (error: RuntimeException) {
            throw IllegalArgumentException("Invalid ui.browser.proxy settings", error)
        }
    }

    private fun checkKeys(settings: Config, allowed: Set<String>, path: String) {
        val unknown = settings.root().keys - allowed
        require(unknown.isEmpty()) { "Unsupported $path settings: ${unknown.joinToString()}" }
    }
}
