package org.skellig.teststep.processor.ui

import org.junit.jupiter.api.Assertions.*
import org.openqa.selenium.chrome.ChromeDriverService
import org.openqa.selenium.chrome.ChromeOptions
import org.openqa.selenium.remote.service.DriverFinder
import java.io.File
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
import org.skellig.teststep.processor.ui.UiTestApplication.Companion.chromeDrivers
import java.util.concurrent.CopyOnWriteArrayList

class UiScenarioIT {

    @Test
    fun `scenario navigates then clicks using discovered UI processor`() {
        UiTestApplication().run { context, runner, _, requests ->
            val initialDrivers = chromeDrivers()
            val driversDuringSteps = mutableListOf<Set<Long>>()
            val dispatcher = DefaultSkelligTestEventDispatcher()
            dispatcher.register(TestStepProcessingFinishedEvent::class) { driversDuringSteps.add(chromeDrivers() - initialDrivers) }

            val failures = runScenario(context, runner, listOf("Open home", "Follow next link"), dispatcher = dispatcher)

            assertTrue(failures.isEmpty(), failures.joinToString { it.trace })
            assertTrue(requests.indexOfFirst { it.path == "/done" } > requests.indexOfFirst { it.path == "/" })
            assertEquals(2, driversDuringSteps.size)
            assertTrue(driversDuringSteps.all { it.size == 1 }, driversDuringSteps.toString())
            assertEquals(driversDuringSteps.first(), driversDuringSteps.last(), "Steps should share one browser session")
        }
    }

    @Test
    fun `scenario waits for a delayed control and reuses its browser across steps`() {
        UiTestApplication().run { context, runner, baseUrl, requests ->
            val result = runScenario(context, runner, listOf("Open explicit URL", "Follow next link"), mapOf("url" to "$baseUrl/delayed"))
            assertTrue(result.isEmpty(), result.joinToString { it.trace })
            assertTrue(requests.any { it.path == "/done" && it.cookie?.contains("journey=active") == true })
            assertEquals("$baseUrl/done", (context.getTestScenarioState().get("follow_result") as Map<*, *>)["url"])
        }
    }

    @Test
    fun `failed action stops without replay and browser remains usable until context close`() {
        UiTestApplication().run { context, runner, baseUrl, requests ->
            val failures = runScenario(context, runner, listOf("Open home", "Click then fail without replay"))

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
        UiTestApplication().run { context, runner, baseUrl, requests ->
            val failures = runScenario(context, runner, listOf("Open explicit URL", "Follow next link"), mapOf("url" to "$baseUrl/ambiguous"))

            assertEquals(1, failures.size)
            assertTrue(failures.single().trace.contains("matched 2 elements"), failures.single().trace)
            assertFalse(requests.any { it.path == "/done" })
        }
    }

    @Test
    fun `browser is reused across scenarios and ordinary steps never start Chrome`() {
        UiTestApplication().run { context, runner, _, requests ->
            val initialDrivers = chromeDrivers()
            val dispatcher = DefaultSkelligTestEventDispatcher()
            val activeDuringOrdinarySteps = mutableListOf<Set<Long>>()
            dispatcher.register(TestStepProcessingFinishedEvent::class) { activeDuringOrdinarySteps.add(chromeDrivers() - initialDrivers) }

            assertTrue(runScenario(context, runner, listOf("No UI work"), dispatcher = dispatcher).isEmpty())

            assertEquals(listOf(emptySet<Long>()), activeDuringOrdinarySteps)
            repeat(2) {
                assertTrue(runScenario(context, runner, listOf("Open home", "Follow next link"), name = "Journey $it").isEmpty())
            }
            assertEquals(2, requests.count { it.path == "/" })
            assertEquals(1, requests.count { it.path == "/" && it.cookie?.contains("journey=active") != true })
        }
    }

    @Test
    fun `validation failure retains the browser for caller directed continuation`() {
        UiTestApplication().run { context, runner, _, requests ->
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
            UiTestApplication().run { context, runner, _, requests ->
                runner.run("Open home").subscribe { _, _, error -> assertNull(error) }
                assertEquals(1, requests.count { it.path == "/" && it.cookie?.contains("journey=active") != true })
            }
        }
    }

    @Test
    fun `invalid navigation fails before any browser interaction`() {
        UiTestApplication().run { context, runner, _, requests ->
            val failures = runScenario(context, runner, listOf("Invalid navigation"))
            assertEquals(1, failures.size)
            assertTrue(failures.single().trace.contains("exactly one of page or url"), failures.single().trace)
            assertTrue(requests.isEmpty())
        }
    }

    @Test
    fun `context fallback closes Chrome even when another extension fails to shut down`() {
        UiTestApplication(failClose = true).run { context, runner, _, _ ->
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
        UiTestApplication().run { context, runner, _, requests ->
            val failures = runScenario(context, runner, listOf("Fill username"), mapOf("username" to "alice", "domain" to "example.test"))
            assertTrue(failures.isEmpty(), failures.joinToString { it.trace })
            assertEquals("username=alice%40example.test", requests.single { it.path == "/submitted" }.query)
        }
    }

    @Test
    fun `login journey clears inputs selects options and sets checkbox state`() {
        UiTestApplication().run { context, runner, baseUrl, requests ->
            val failures = runScenario(context, runner, listOf("Complete login"), mapOf("username" to "alice", "password" to "secret"))
            assertTrue(failures.isEmpty(), failures.joinToString { it.trace })
            assertEquals("username=alice&password=secret&notes=&textChoice=two&valueChoice=two&remember=yes&changes=3",
                requests.single { it.path == "/dashboard" }.query)
            assertTrue((context.getTestScenarioState().get("login_result") as Map<*, *>)["url"].toString().startsWith("$baseUrl/dashboard"))
        }
    }

    @Test
    fun `explicit waits synchronize presence absence visibility hidden and enabled states`() {
        UiTestApplication().run { context, runner, baseUrl, requests ->
            listOf("present", "absent", "visible", "hidden", "enabled", "hidden-missing").forEach { state ->
                val condition = state.substringBefore('-')
                val failures = runScenario(context, runner, listOf("Wait for control"),
                    mapOf("url" to "$baseUrl/wait-$state", "condition" to condition))
                assertTrue(failures.isEmpty(), "$state: " + failures.joinToString { it.trace })
            }
            assertEquals(6, requests.count { it.path == "/after-wait" })
        }
    }

    @Test
    fun `every form operation waits for delayed interactable controls`() {
        UiTestApplication().run { context, runner, baseUrl, requests ->
            listOf("type", "clear", "select", "check").forEach { operation ->
                val failures = runScenario(context, runner, listOf("Open explicit URL", "Try $operation"),
                    mapOf("url" to "$baseUrl/delayed-$operation", "target" to "home.marker"))
                assertTrue(failures.isEmpty(), "$operation: " + failures.joinToString { it.trace })
            }
            assertEquals(listOf("field=typed", "field=", "field=two", "field=yes"),
                requests.filter { it.path == "/submitted" }.map { it.query })
        }
    }

    @Test
    fun `every form operation times out without submitting and ambiguous controls fail fast`() {
        UiTestApplication(timeout = "2s").run { context, runner, baseUrl, requests ->
            listOf("type", "clear", "select", "check", "wait").forEach { operation ->
                runScenario(context, runner, listOf("Open explicit URL"), mapOf("url" to "$baseUrl/ambiguous"))
                val started = System.nanoTime()
                val ambiguous = runScenario(context, runner, listOf("Try $operation"), mapOf("target" to "home.next", "condition" to "hidden"))
                assertEquals(1, ambiguous.size, operation)
                assertTrue(ambiguous.single().trace.contains("matched 2 elements"), ambiguous.single().trace)
                assertTrue(System.nanoTime() - started < 1_500_000_000L, "$operation should fail before the 2 second timeout")
            }
            assertFalse(requests.any { it.path in listOf("/submitted", "/after-wait") })
        }
        UiTestApplication().run { context, runner, _, requests ->
            listOf("type", "clear", "select", "check", "wait").forEach { operation ->
                val failures = runScenario(context, runner, listOf("Open home", "Try $operation"),
                    mapOf("target" to "home.missing", "condition" to "present"))
                assertEquals(1, failures.size, operation)
                assertTrue(failures.single().trace.contains("TimeoutException"), failures.single().trace)
            }
            assertFalse(requests.any { it.path in listOf("/submitted", "/after-wait") })
        }
    }

    @Test
    fun `wait conditions distinguish absent hidden present visible and enabled`() {
        UiTestApplication().run { context, runner, baseUrl, requests ->
            val cases = listOf(
                Triple("/invisible", "present", true), Triple("/invisible", "enabled", true),
                Triple("/invisible", "hidden", true), Triple("/invisible", "absent", false),
                Triple("/invisible", "visible", false), Triple("/wait-hidden-missing", "present", false),
                Triple("/marker", "hidden", false), Triple("/marker", "absent", false),
                Triple("/disabled", "enabled", false)
            )
            cases.forEach { (path, condition, succeeds) ->
                val failures = runScenario(context, runner, listOf("Wait for control"), mapOf("url" to "$baseUrl$path", "condition" to condition))
                assertEquals(succeeds, failures.isEmpty(), "$path / $condition: " + failures.joinToString { it.trace })
            }
            assertEquals(3, requests.count { it.path == "/after-wait" })
        }
    }

    @Test
    fun `invalid operation fields are rejected before navigation`() {
        UiTestApplication().run { context, runner, _, requests ->
            listOf("Invalid append", "Invalid check", "Invalid select", "Invalid wait", "Invalid target").forEach { step ->
                assertEquals(1, runScenario(context, runner, listOf(step)).size, step)
            }
            assertTrue(requests.isEmpty())
        }
    }

    @Test
    fun `failed typing does not expose the input value in diagnostics`() {
        UiTestApplication().run { context, runner, _, _ ->
            val secret = "sensitive-sentinel-85"
            val failures = runScenario(context, runner, listOf("Open home", "Fail typing sensitive input"), mapOf("password" to secret))
            assertEquals(1, failures.size)
            assertTrue(failures.single().trace.contains("home.missing"), failures.single().trace)
            assertFalse(failures.single().trace.contains(secret), "Input values must be masked in failure reports")
        }
    }

    @Test
    fun `captured controls feed state validation and later steps after browser cleanup`() {
        var saved: Any? = null
        UiTestApplication().run { context, runner, _, requests ->
            val failures = runScenario(context, runner, listOf("Capture next control", "Follow next link", "Use saved UI value", "Validate saved UI control"))
            assertTrue(failures.isEmpty(), failures.joinToString { it.trace })
            saved = context.getTestScenarioState().get("captured")
            assertEquals(1, requests.count { it.path == "/" })
        }
        assertEquals(PageElementsStateCollector.UiElementState(true, true, true, false, "Next", null), saved)
    }

    @Test
    fun `captures only action targets without navigating unused catalog pages`() {
        UiTestApplication(extraUi = """
            pages.unused { path = "/never-visit", elements.danger = "//*" }
            pages.home.elements.unused = "//*"
            pages.secondary.elements.next = "//*[@id='next']"
        """).run { context, runner, _, requests ->
            val failures = runScenario(context, runner, listOf("Scoped nested observations"), mapOf("expectedText" to "Next"))
            assertTrue(failures.isEmpty(), failures.joinToString { it.trace })
            val result = context.getTestScenarioState().get("scoped_result") as Map<*, *>
            val pages = result["pages"] as Map<*, *>
            assertEquals(setOf("home", "secondary"), pages.keys)
            assertEquals(setOf("next"), (pages["secondary"] as Map<*, *>).keys)
            assertEquals(setOf("next", "missing"), (pages["home"] as Map<*, *>).keys)
            assertFalse(requests.any { it.path == "/never-visit" })
        }
    }

    @Test
    fun `missing observations are immediate and provide false flags and null data`() {
        UiTestApplication(timeout = "5s").run { context, runner, _, _ ->
            assertTrue(runScenario(context, runner, listOf("Open home")).isEmpty())
            val started = System.nanoTime()
            val failures = runScenario(context, runner, listOf("Capture missing control"))
            assertTrue(failures.isEmpty(), failures.joinToString { it.trace })
            assertTrue(System.nanoTime() - started < 2_000_000_000L, "Observation capture must not wait for an absent control")
            assertEquals(PageElementsStateCollector.UiElementState(false, false, null, null, null, null), context.getTestScenarioState().get("missingControl"))
        }
    }

    @Test
    fun `repeated action targets are captured once before state updates and failed validation`() {
        UiTestApplication().run { context, runner, baseUrl, requests ->
            assertTrue(runScenario(context, runner, listOf("Open explicit URL"), mapOf("url" to "$baseUrl/probe")).isEmpty())
            val started = System.nanoTime()
            val failures = runScenario(context, runner, listOf("Capture probe then fail"))
            assertEquals(1, failures.size)
            assertTrue(failures.single().trace.contains("never-match"), failures.single().trace)
            assertEquals("1", context.getTestScenarioState().get("probeValue"))
            assertTrue(System.nanoTime() - started < 2_000_000_000L, "Validation must run once without polling")
            assertEquals(1, requests.count { it.path == "/probe" })
            assertNotNull(context.getTestScenarioState().get("probe_result"))
            assertTrue(runScenario(context, runner, listOf("Validate failed snapshot")).isEmpty())
        }
    }

    @Test
    fun `dynamic paths existing extractors and matchers consume materialized observations`() {
        UiTestApplication().run { context, runner, _, _ ->
            val failures = runScenario(context, runner, listOf("Capture via extractor"), mapOf("path" to "pages.home.next.text"))
            assertTrue(failures.isEmpty(), failures.joinToString { it.trace })
            assertEquals("Next", context.getTestScenarioState().get("extractedUiText"))
        }
    }

    @Test
    fun `ambiguous observations fail before state updates without replay and retain the browser`() {
        UiTestApplication().run { context, runner, baseUrl, requests ->
            val failures = runScenario(context, runner, listOf("Capture ambiguous control"), mapOf("url" to "$baseUrl/ambiguous"))
            assertEquals(1, failures.size)
            assertTrue(failures.single().trace.contains("during observation capture"), failures.single().trace)
            assertTrue(failures.single().trace.contains("matched 2 elements"), failures.single().trace)
            assertNull(context.getTestScenarioState().get("ambiguousControl"))
            assertEquals(1, requests.count { it.path == "/ambiguous" })
            assertTrue(runScenario(context, runner, listOf("Open home")).isEmpty())
        }
    }

    @Test
    fun `observations reflect final form values selection visibility and enabled state`() {
        UiTestApplication().run { context, runner, _, _ ->
            val failures = runScenario(context, runner, listOf("Capture edited form"))
            assertTrue(failures.isEmpty(), failures.joinToString { it.trace })
            val form = context.getTestScenarioState().get("formSnapshot") as Map<*, *>

            assertEquals("alice", (form["username"] as PageElementsStateCollector.UiElementState).value)
            assertEquals(true, (form["remember"] as PageElementsStateCollector.UiElementState).selected)
            assertEquals(false, (form["invisible"] as PageElementsStateCollector.UiElementState).visible)
            assertEquals(false, (form["disabled"] as PageElementsStateCollector.UiElementState).enabled)
        }
    }

    @Test
    fun `state and validation can refer to the current saved result key`() {
        UiTestApplication().run { context, runner, _, _ ->
            val failures = runScenario(context, runner, listOf("Capture using saved result key", "Capture whole saved result", "Capture implicit result"))
            assertTrue(failures.isEmpty(), failures.joinToString { it.trace })
            assertEquals("Next", context.getTestScenarioState().get("selfText"))
        }
    }

    @Test
    fun `navigation alone captures no controls`() {
        UiTestApplication(extraUi = "pages.home.elements.unused = \"//*\"").run { context, runner, _, _ ->
            val failures = runScenario(context, runner, listOf("Open home"))
            assertTrue(failures.isEmpty(), failures.joinToString { it.trace })
            val result = context.getTestScenarioState().get("open_result") as Map<*, *>
            assertEquals(emptyMap<String, Any?>(), result["pages"])
        }
    }

    @Test
    fun documentedLoginWorkflow() {
        verifyDocumentedLoginWorkflow()
    }

    private fun verifyDocumentedLoginWorkflow(extraUi: String = "") {
        val directory = java.nio.file.Files.createTempDirectory("ui-login-evidence")
        var saved: Any? = null
        try {
            UiTestApplication(extraUi = """
                $extraUi
                screenshots.path = ${com.typesafe.config.ConfigUtil.quoteString(directory.toString())}
            """).run { context, runner, _, requests ->
                val parameters = mapOf("username" to "alice", "password" to "fixture-only-password", "expectedAccount" to "Account")
                val failures = runScenario(context, runner, listOf("Example login and capture", "Example consume account"), parameters)
                assertTrue(failures.isEmpty(), failures.joinToString { it.trace })
                saved = context.getTestScenarioState().get("accountName")
                assertEquals("Account", saved)
                assertEquals(1, requests.count { it.path == "/dashboard" })
                assertTrue(requests.single { it.path == "/dashboard" }.query.orEmpty().contains("username=alice"))
                assertTrue(requests.single { it.path == "/dashboard" }.cookie.orEmpty().contains("journey=active"))

                context.getTestScenarioState().remove("accountName")
                context.getTestScenarioState().remove("exampleLogin_result")
                val failed = runScenario(context, runner, listOf("Example login and capture"), parameters + ("expectedAccount" to "Wrong account"))
                assertEquals(1, failed.size)
                assertTrue(failed.single().trace.contains("Wrong account"), failed.single().trace)
                assertEquals("Account", context.getTestScenarioState().get("accountName"), "State is saved before validation fails")
                assertNotNull(context.getTestScenarioState().get("exampleLogin_result"))
                assertEquals(2, requests.count { it.path == "/dashboard" }, "Failed validation must not replay login")
                assertTrue(runScenario(context, runner, listOf("Example consume account")).isEmpty())

                val screenshots = java.nio.file.Files.list(directory).use { it.toList() }
                assertEquals(4, screenshots.size, "Each login, including the failed validation, saves before and after evidence")
                screenshots.forEach { assertNotNull(javax.imageio.ImageIO.read(it.toFile()), it.toString()) }
            }
            assertEquals("Account", saved, "Captured data survives context cleanup")
        } finally {
            directory.toFile().deleteRecursively()
        }
    }

    @Test
    fun remoteChromeRunsSameScenario() {
        val finder = DriverFinder(ChromeDriverService.createDefaultService(), ChromeOptions())
        val service = ChromeDriverService.Builder().usingDriverExecutable(File(finder.driverPath)).usingAnyFreePort().build()
        service.start()
        try {
            val binary = if (finder.hasBrowserPath()) "binary = ${com.typesafe.config.ConfigUtil.quoteString(finder.browserPath)}" else ""
            verifyDocumentedLoginWorkflow("""
                browser {
                    mode = remote
                    url = "${service.url}"
                    $binary
                }
            """)
        } finally {
            service.stop()
        }
    }

    @Test
    fun `local browser saves one screenshot before and after the action sequence`() {
        val directory = java.nio.file.Files.createTempDirectory("ui-screenshots")
        try {
            UiTestApplication(extraUi = "screenshots.path = ${com.typesafe.config.ConfigUtil.quoteString(directory.toString())}").run { context, runner, _, _ ->
                assertTrue(runScenario(context, runner, listOf("Navigate twice for screenshots")).isEmpty())
                val files = java.nio.file.Files.list(directory).use { it.toList() }
                assertEquals(2, files.size)
                assertEquals(setOf("before", "after"), files.map { it.fileName.toString().substringBefore('_') }.toSet())
                files.forEach { assertNotNull(javax.imageio.ImageIO.read(it.toFile())) }
            }
        } finally { directory.toFile().deleteRecursively() }
    }

    private fun runScenario(
        context: org.skellig.teststep.runner.context.SkelligTestContext,
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
            DefaultTestStepLogger(), dispatcher, context.getDefaultScope()).run(notifier)
        return failures
    }

}
