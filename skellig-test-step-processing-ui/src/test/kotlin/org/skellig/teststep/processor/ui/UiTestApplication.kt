package org.skellig.teststep.processor.ui

import com.sun.net.httpserver.HttpServer
import com.typesafe.config.Config
import com.typesafe.config.ConfigFactory
import org.junit.jupiter.api.Assertions.assertEquals
import org.skellig.teststep.runner.TestStepRunner
import org.skellig.teststep.runner.context.SkelligTestContext
import java.util.concurrent.CopyOnWriteArrayList

internal class UiTestApplication(
    private val failClose: Boolean = false,
    private val timeout: String = "500ms",
    private val extraUi: String = ""
) {
    data class Request(val path: String, val cookie: String?, val query: String?)

    fun run(test: (SkelligTestContext, TestStepRunner, String, List<Request>) -> Unit) {
        val requests = CopyOnWriteArrayList<Request>()
        val server = createServer(requests)
        val originalDrivers = chromeDrivers()
        try {
            server.start()
            val baseUrl = "http://127.0.0.1:${server.address.port}"
            val config = createConfig(baseUrl)
            val context = object : SkelligTestContext() {
                override fun createConfig(classLoader: ClassLoader, configPath: String?): Config = config
            }
            context.use {
                val runner = it.initialize(javaClass.classLoader, listOf("ui/steps.sts", "ui/example/login.sts"))
                test(it, runner, baseUrl, requests)
            }
        } finally {
            server.stop(0)
            assertEquals(originalDrivers, chromeDrivers(), "Context close left a ChromeDriver process running")
        }
    }

    private fun createServer(requests: MutableList<Request>): HttpServer {
        val server = HttpServer.create(java.net.InetSocketAddress("127.0.0.1", 0), 0)
        server.createContext("/") { exchange ->
            val path = exchange.requestURI.path
            requests.add(Request(path, exchange.requestHeaders.getFirst("Cookie"), exchange.requestURI.rawQuery))
            val link = "<a id='next' href='/done'>Next</a>"
            val content = when (path) {
                "/delayed" -> "<script>setTimeout(()=>document.body.innerHTML=\"$link\",200)</script>"
                "/ambiguous" -> link + link
                "/probe" -> """<input id='probe'><script>let reads=0;Object.defineProperty(probe,'value',{get:()=>String(++reads)})</script>"""
                "/submitted" -> "<h1 id='submitted'>Submitted</h1>"
                "/marker" -> "<input id='marker'>"
                "/invisible" -> "<input id='marker' style='display:none'>"
                "/disabled" -> "<input id='marker' disabled>"
                "/delayed-type", "/delayed-clear", "/delayed-check", "/delayed-select" -> {
                    val control = when (path) {
                        "/delayed-select" -> "<select id='marker' name='field'><option value='one'>First</option><option value='two'>Second</option></select>"
                        "/delayed-check" -> "<input id='marker' name='field' type='checkbox' value='yes'>"
                        else -> "<input id='marker' name='field' value='old'>"
                    }
                    """<form action='/submitted'>$control<button id='submit'>Submit</button></form>
                        <script>marker.disabled=true;marker.style.display='none';setTimeout(()=>{marker.disabled=false;marker.style.display='block'},200)</script>"""
                }
                "/wait-present" -> """<script>setTimeout(()=>document.body.innerHTML="<input id='marker'>",200)</script>"""
                "/wait-absent" -> "<input id='marker'><script>setTimeout(()=>marker.remove(),200)</script>"
                "/wait-visible" -> "<input id='marker' style='display:none'><script>setTimeout(()=>marker.style.display='block',200)</script>"
                "/wait-hidden" -> "<input id='marker'><script>setTimeout(()=>marker.style.display='none',200)</script>"
                "/wait-enabled" -> "<input id='marker' disabled><script>setTimeout(()=>marker.disabled=false,200)</script>"
                "/wait-hidden-missing" -> ""
                "/login" -> """
                    <form action='/dashboard'>
                      <input id='username' name='username' value='old'>
                      <input id='password' name='password' type='password'>
                      <input id='invisible' style='display:none'><input id='disabled' disabled><textarea id='notes' name='notes'>old notes</textarea>
                      <select id='textChoice' name='textChoice'><option value='one'>First</option><option value='two'>Second</option></select>
                      <select id='valueChoice' name='valueChoice'><option value='one'>First</option><option value='two'>Second</option></select>
                      <input id='remember' name='remember' type='checkbox' value='yes' onchange="changes.value=Number(changes.value)+1">
                      <input id='changes' name='changes' type='hidden' value='0'>
                      <button id='submit'>Login</button>
                    </form>
                """.trimIndent()
                "/dashboard" -> """<script>setTimeout(()=>document.body.innerHTML="<h1 id='dashboard'>Account</h1>",200)</script>"""
                "/form" -> "<form action='/submitted'><input id='username' name='username' value='old'><button id='submit'>Submit</button></form>"
                else -> link
            }
            val html = "<html><body>$content</body></html>".toByteArray()
            exchange.responseHeaders.set("Content-Type", "text/html")
            if (path != "/done") exchange.responseHeaders.add("Set-Cookie", "journey=active; Path=/")
            exchange.sendResponseHeaders(200, html.size.toLong())
            exchange.responseBody.use { it.write(html) }
        }
        return server
    }

    private fun createConfig(baseUrl: String): Config {
        return ConfigFactory.parseString("""
            shutdown-failure = $failClose
            ui {
                timeout = $timeout
                base-url = "$baseUrl"
                browser { mode = local, name = chrome, headless = true }
                pages.afterWait { path = "/after-wait" }
                pages.login { path = "/login" }
                pages.form { path = "/form" }
                pages.home {
                    path = "/"
                    elements {
                        next = "//*[@id='next']"
                        missing = "//*[@id='missing']"
                        username = "//*[@id='username']"
                        submit = "//*[@id='submit']"
                        password = "//*[@id='password']"
                        notes = "//*[@id='notes']"
                        textChoice = "//*[@id='textChoice']"
                        valueChoice = "//*[@id='valueChoice']"
                        remember = "//*[@id='remember']"
                        marker = "//*[@id='marker']"
                        dashboard = "//*[@id='dashboard']"
                        submitted = "//*[@id='submitted']"
                        probe = "//*[@id='probe']"
                        invisible = "//*[@id='invisible']"
                        disabled = "//*[@id='disabled']"
                    }
                }
                $extraUi
            }
        """.trimIndent()).withFallback(ConfigFactory.parseResources("ui/example/login.conf")).resolve()
    }

    companion object {
        fun chromeDrivers(): Set<Long> = ProcessHandle.current().children().use { processes ->
            processes.filter { it.info().command().orElse("").substringAfterLast('/') == "chromedriver" }
                .map { it.pid() }.toList().toSet()
        }
    }
}
