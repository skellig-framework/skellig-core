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
            assertTrue(failures.single().trace.contains("UI action 2"), failures.single().trace)
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
    fun `standalone steps need no grouping lifecycle and close cannot recreate a browser`() {
        withApplication { context, runner, _, requests ->
            runner.run("Open home").subscribe { _, _, error -> assertNull(error) }
            runner.run("Follow next link").subscribe { _, _, error -> assertNull(error) }
            assertTrue(requests.any { it.path == "/done" })
            context.close()
            val requestCount = requests.size
            runner.run("Open home").subscribe { _, _, error ->
                assertNotNull(error)
                assertTrue(error!!.message!!.contains("closed"))
            }
            assertEquals(requestCount, requests.size)
            context.close()
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

    private data class Request(val path: String, val cookie: String?)

    private fun withApplication(failClose: Boolean = false, test: (SkelligTestContext, org.skellig.teststep.runner.TestStepRunner, String, List<Request>) -> Unit) {
        val requests = CopyOnWriteArrayList<Request>()
        val server = HttpServer.create(java.net.InetSocketAddress("127.0.0.1", 0), 0)
        server.createContext("/") { exchange ->
            val path = exchange.requestURI.path
            requests.add(Request(path, exchange.requestHeaders.getFirst("Cookie")))
            val link = "<a id='next' href='/done'>Next</a>"
            val content = when (path) {
                "/delayed" -> "<script>setTimeout(()=>document.body.innerHTML=\"$link\",200)</script>"
                "/ambiguous" -> link + link
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
                timeout = 500ms
                base-url = "$baseUrl"
                browser { mode = local, name = chrome, headless = true }
                pages.home {
                    path = "/"
                    elements {
                        next = "//*[@id='next']"
                        missing = "//*[@id='missing']"
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
