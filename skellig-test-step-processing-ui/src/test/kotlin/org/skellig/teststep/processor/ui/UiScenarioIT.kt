package org.skellig.teststep.processor.ui

import com.sun.net.httpserver.HttpServer
import com.typesafe.config.Config
import com.typesafe.config.ConfigFactory
import org.junit.jupiter.api.Assertions.*
import org.junit.Test
import org.junit.runner.notification.Failure
import org.junit.runner.notification.RunListener
import org.junit.runner.notification.RunNotifier
import org.skellig.feature.TestScenario
import org.skellig.feature.TestStep
import org.skellig.feature.event.DefaultSkelligTestEventDispatcher
import org.skellig.feature.event.TestStepProcessingFinishedEvent
import org.skellig.feature.hook.DefaultSkelligHookRunner
import org.skellig.feature.hook.DefaultSkelligTestHooksRegistry
import org.skellig.plugin.report.DefaultTestStepLogger
import org.skellig.runner.TestScenarioRunner
import org.skellig.teststep.runner.context.SkelligTestContext
import java.util.concurrent.CopyOnWriteArrayList

class UiScenarioIT {

    @Test
    fun `scenario navigates then clicks using discovered UI processor`() {
        withApplication { _, runner, _, requests ->
            val initialDrivers = chromeDrivers()
            val driversDuringSteps = mutableListOf<Set<Long>>()
            val dispatcher = DefaultSkelligTestEventDispatcher()
            dispatcher.register(TestStepProcessingFinishedEvent::class) { driversDuringSteps.add(chromeDrivers() - initialDrivers) }

            val failures = runScenario(runner, listOf("Open home", "Follow next link"), dispatcher = dispatcher)

            assertTrue(failures.isEmpty(), failures.joinToString { it.trace })
            assertTrue(requests.indexOfFirst { it.path == "/done" } > requests.indexOfFirst { it.path == "/" })
            assertEquals(2, driversDuringSteps.size)
            assertTrue(driversDuringSteps.all { it.size == 1 }, driversDuringSteps.toString())
            assertEquals(driversDuringSteps.first(), driversDuringSteps.last(), "Steps should share one browser session")
        }
    }

    @Test
    fun `scenario waits for a delayed control and reuses its browser across steps`() {
        withApplication { context, runner, baseUrl, requests ->
            val result = runScenario(runner, listOf("Open explicit URL", "Follow next link"), mapOf("url" to "$baseUrl/delayed"))
            assertTrue(result.isEmpty(), result.joinToString { it.trace })
            assertTrue(requests.any { it.path == "/done" && it.cookie?.contains("journey=active") == true })
            assertEquals("$baseUrl/done", (context.getTestScenarioState().get("follow_result") as Map<*, *>)["url"])
        }
    }

    @Test
    fun `failed action stops without replay and browser remains usable until context close`() {
        withApplication { _, runner, baseUrl, requests ->
            val failures = runScenario(runner, listOf("Open home", "Click then fail without replay"))

            assertEquals(1, failures.size)
            assertTrue(failures.single().trace.contains("UI action (Click(target=home.missing, xpath=//*[@id='missing'])) failed"), failures.single().trace)
            assertEquals(1, requests.count { it.path == "/done" })
            assertEquals(1, requests.count { it.path == "/" })

            val activeDrivers = chromeDrivers()
            assertFalse(activeDrivers.isEmpty())
            runner.run("Open explicit URL", mapOf("url" to "$baseUrl/after-failure"))
                .subscribe { _, _, error -> assertNull(error) }
            assertEquals(activeDrivers, chromeDrivers(), "Failure must not replace the browser")
            assertTrue(requests.any { it.path == "/after-failure" && it.cookie?.contains("journey=active") == true })
        }
    }

    @Test
    fun `ambiguous target fails without clicking any match`() {
        withApplication { _, runner, baseUrl, requests ->
            val failures = runScenario(runner, listOf("Open explicit URL", "Follow next link"), mapOf("url" to "$baseUrl/ambiguous"))

            assertEquals(1, failures.size)
            assertTrue(failures.single().trace.contains("matched 2 elements"), failures.single().trace)
            assertFalse(requests.any { it.path == "/done" })
        }
    }

    @Test
    fun `browser is reused across scenarios and ordinary steps never start Chrome`() {
        withApplication { _, runner, _, requests ->
            val initialDrivers = chromeDrivers()
            val dispatcher = DefaultSkelligTestEventDispatcher()
            val activeDuringOrdinarySteps = mutableListOf<Set<Long>>()
            dispatcher.register(TestStepProcessingFinishedEvent::class) { activeDuringOrdinarySteps.add(chromeDrivers() - initialDrivers) }

            assertTrue(runScenario(runner, listOf("No UI work"), dispatcher = dispatcher).isEmpty())

            assertEquals(listOf(emptySet<Long>()), activeDuringOrdinarySteps)
            repeat(2) {
                assertTrue(runScenario(runner, listOf("Open home", "Follow next link"), name = "Journey $it").isEmpty())
            }
            assertEquals(2, requests.count { it.path == "/" })
            assertEquals(1, requests.count { it.path == "/" && it.cookie?.contains("journey=active") != true })
        }
    }

    @Test
    fun `validation failure retains the browser for caller directed continuation`() {
        withApplication { _, runner, _, requests ->
            runner.run("Fail UI validation").subscribe { _, _, error -> assertNotNull(error) }
            val activeDrivers = chromeDrivers()
            assertFalse(activeDrivers.isEmpty())

            runner.run("Follow next link").subscribe { _, _, error -> assertNull(error) }

            assertEquals(activeDrivers, chromeDrivers())
            assertEquals(1, requests.count { it.path == "/" })
            assertTrue(requests.any { it.path == "/done" && it.cookie?.contains("journey=active") == true })
        }
    }

    @Test
    fun `separate contexts have independent browser state`() {
        repeat(2) {
            withApplication { _, runner, _, requests ->
                runner.run("Open home").subscribe { _, _, error -> assertNull(error) }
                assertEquals(1, requests.count { it.path == "/" && it.cookie?.contains("journey=active") != true })
            }
        }
    }

    @Test
    fun `invalid navigation fails before any browser interaction`() {
        withApplication { _, runner, _, requests ->
            val failures = runScenario(runner, listOf("Invalid navigation"))
            assertEquals(1, failures.size)
            assertTrue(failures.single().trace.contains("exactly one of page or url"), failures.single().trace)
            assertTrue(requests.isEmpty())
        }
    }

    @Test
    fun `context fallback closes Chrome even when another extension fails to shut down`() {
        withApplication(failClose = true) { context, runner, _, _ ->
            val originalDrivers = chromeDrivers()
            runner.run("Open home").subscribe { _, _, error -> assertNull(error) }
            assertEquals(1, (chromeDrivers() - originalDrivers).size)
            val error = assertThrows(IllegalStateException::class.java) { context.close() }
            assertEquals("Simulated extension shutdown failure", error.message)
            assertEquals(originalDrivers, chromeDrivers(), "Context fallback must reach Chrome cleanup")
        }
    }

    @Test
    fun `typing replaces existing text and can append evaluated parameters`() {
        withApplication { _, runner, _, requests ->
            val failures = runScenario(runner, listOf("Fill username"), mapOf("username" to "alice", "domain" to "example.test"))
            assertTrue(failures.isEmpty(), failures.joinToString { it.trace })
            assertEquals("username=alice%40example.test", requests.single { it.path == "/submitted" }.query)
        }
    }

    @Test
    fun `login journey clears inputs selects options and sets checkbox state`() {
        withApplication { context, runner, baseUrl, requests ->
            val failures = runScenario(runner, listOf("Complete login"), mapOf("username" to "alice", "password" to "secret"))
            assertTrue(failures.isEmpty(), failures.joinToString { it.trace })
            assertEquals("username=alice&password=secret&notes=&textChoice=two&valueChoice=two&remember=yes&changes=3",
                requests.single { it.path == "/dashboard" }.query)
            assertTrue((context.getTestScenarioState().get("login_result") as Map<*, *>)["url"].toString().startsWith("$baseUrl/dashboard"))
        }
    }

    @Test
    fun `explicit waits synchronize presence absence visibility hidden and enabled states`() {
        withApplication { _, runner, baseUrl, requests ->
            listOf("present", "absent", "visible", "hidden", "enabled", "hidden-missing").forEach { state ->
                val condition = state.substringBefore('-')
                val failures = runScenario(runner, listOf("Wait for control"),
                    mapOf("url" to "$baseUrl/wait-$state", "condition" to condition))
                assertTrue(failures.isEmpty(), "$state: " + failures.joinToString { it.trace })
            }
            assertEquals(6, requests.count { it.path == "/after-wait" })
        }
    }

    @Test
    fun `every form operation waits for delayed interactable controls`() {
        withApplication { _, runner, baseUrl, requests ->
            listOf("type", "clear", "select", "check").forEach { operation ->
                val failures = runScenario(runner, listOf("Open explicit URL", "Try $operation"),
                    mapOf("url" to "$baseUrl/delayed-$operation", "target" to "home.marker"))
                assertTrue(failures.isEmpty(), "$operation: " + failures.joinToString { it.trace })
            }
            assertEquals(listOf("field=typed", "field=", "field=two", "field=yes"),
                requests.filter { it.path == "/submitted" }.map { it.query })
        }
    }

    @Test
    fun `every form operation times out without submitting and ambiguous controls fail fast`() {
        withApplication(timeout = "2s") { _, runner, baseUrl, requests ->
            listOf("type", "clear", "select", "check", "wait").forEach { operation ->
                runScenario(runner, listOf("Open explicit URL"), mapOf("url" to "$baseUrl/ambiguous"))
                val started = System.nanoTime()
                val ambiguous = runScenario(runner, listOf("Try $operation"), mapOf("target" to "home.next", "condition" to "hidden"))
                assertEquals(1, ambiguous.size, operation)
                assertTrue(ambiguous.single().trace.contains("matched 2 elements"), ambiguous.single().trace)
                assertTrue(System.nanoTime() - started < 1_500_000_000L, "$operation should fail before the 2 second timeout")
            }
            assertFalse(requests.any { it.path in listOf("/submitted", "/after-wait") })
        }
        withApplication { _, runner, _, requests ->
            listOf("type", "clear", "select", "check", "wait").forEach { operation ->
                val failures = runScenario(runner, listOf("Open home", "Try $operation"),
                    mapOf("target" to "home.missing", "condition" to "present"))
                assertEquals(1, failures.size, operation)
                assertTrue(failures.single().trace.contains("TimeoutException"), failures.single().trace)
            }
            assertFalse(requests.any { it.path in listOf("/submitted", "/after-wait") })
        }
    }

    @Test
    fun `wait conditions distinguish absent hidden present visible and enabled`() {
        withApplication { _, runner, baseUrl, requests ->
            val cases = listOf(
                Triple("/invisible", "present", true), Triple("/invisible", "enabled", true),
                Triple("/invisible", "hidden", true), Triple("/invisible", "absent", false),
                Triple("/invisible", "visible", false), Triple("/wait-hidden-missing", "present", false),
                Triple("/marker", "hidden", false), Triple("/marker", "absent", false),
                Triple("/disabled", "enabled", false)
            )
            cases.forEach { (path, condition, succeeds) ->
                val failures = runScenario(runner, listOf("Wait for control"), mapOf("url" to "$baseUrl$path", "condition" to condition))
                assertEquals(succeeds, failures.isEmpty(), "$path / $condition: " + failures.joinToString { it.trace })
            }
            assertEquals(3, requests.count { it.path == "/after-wait" })
        }
    }

    @Test
    fun `invalid operation fields are rejected before navigation`() {
        withApplication { _, runner, _, requests ->
            listOf("Invalid append", "Invalid check", "Invalid select", "Invalid wait", "Invalid target").forEach { step ->
                assertEquals(1, runScenario(runner, listOf(step)).size, step)
            }
            assertTrue(requests.isEmpty())
        }
    }

    @Test
    fun `failed typing does not expose the input value in diagnostics`() {
        withApplication { _, runner, _, _ ->
            val secret = "sensitive-sentinel-85"
            val failures = runScenario(runner, listOf("Open home", "Fail typing sensitive input"), mapOf("password" to secret))
            assertEquals(1, failures.size)
            assertTrue(failures.single().trace.contains("home.missing"), failures.single().trace)
            assertFalse(failures.single().trace.contains(secret), "Input values must be masked in failure reports")
        }
    }

    private data class Request(val path: String, val cookie: String?, val query: String?)

    private fun withApplication(failClose: Boolean = false, timeout: String = "500ms", test: (SkelligTestContext, org.skellig.teststep.runner.TestStepRunner, String, List<Request>) -> Unit) {
        val requests = CopyOnWriteArrayList<Request>()
        val server = HttpServer.create(java.net.InetSocketAddress("127.0.0.1", 0), 0)
        server.createContext("/") { exchange ->
            val path = exchange.requestURI.path
            requests.add(Request(path, exchange.requestHeaders.getFirst("Cookie"), exchange.requestURI.rawQuery))
            val link = "<a id='next' href='/done'>Next</a>"
            val content = when (path) {
                "/delayed" -> "<script>setTimeout(()=>document.body.innerHTML=\"$link\",200)</script>"
                "/ambiguous" -> link + link
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
                      <textarea id='notes' name='notes'>old notes</textarea>
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
        server.start()
        val baseUrl = "http://127.0.0.1:${server.address.port}"
        val config = ConfigFactory.parseString("""
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
                    }
                }
            }
        """.trimIndent())
        val originalDrivers = chromeDrivers()
        val context = object : SkelligTestContext() {
            override fun createConfig(classLoader: ClassLoader, configPath: String?): Config = config
        }
        try {
            context.use { test(it, it.initialize(javaClass.classLoader, listOf("ui/steps.sts")), baseUrl, requests) }
        } finally {
            server.stop(0)
            assertEquals(originalDrivers, chromeDrivers(), "Context close left a ChromeDriver process running")
        }
    }

    private fun runScenario(
        runner: org.skellig.teststep.runner.TestStepRunner,
        steps: List<String>, parameters: Map<String, String?> = emptyMap(), name: String = "UI scenario",
        dispatcher: DefaultSkelligTestEventDispatcher = DefaultSkelligTestEventDispatcher()
    ): List<Failure> {
        val scenario = TestScenario.Builder().withName(name).withParent("ui-feature")
        steps.forEach { scenario.withStep(TestStep.Builder().withName(it).withParameters(parameters.toMutableMap())) }
        val failures = CopyOnWriteArrayList<Failure>()
        val notifier = RunNotifier()
        notifier.addListener(object : RunListener() {
            override fun testFailure(failure: Failure) { failures.add(failure) }
        })
        TestScenarioRunner.create(scenario.build().single(), 1, runner,
            DefaultSkelligHookRunner(DefaultSkelligTestHooksRegistry(listOf("ui-no-hooks"), mutableMapOf())),
            DefaultTestStepLogger(), dispatcher).run(notifier)
        return failures
    }

    private fun chromeDrivers(): Set<Long> = ProcessHandle.current().children().use { processes ->
        processes.filter { it.info().command().orElse("").substringAfterLast('/') == "chromedriver" }
            .map { it.pid() }.toList().toSet()
    }
}
