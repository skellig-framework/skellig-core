package org.skellig.teststep.processor.ui

import org.openqa.selenium.OutputType
import org.openqa.selenium.TakesScreenshot
import org.openqa.selenium.WebDriver
import org.skellig.teststep.processing.processor.TestStepProcessor
import org.skellig.teststep.processing.processor.TestStepProcessor.TestStepRunResult
import org.skellig.teststep.processing.state.TestScenarioState
import org.skellig.teststep.processor.ui.handler.CompositeUiActionHandler
import org.skellig.teststep.processor.ui.model.UiAction
import org.skellig.teststep.processor.ui.model.UiTestStep
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardOpenOption
import java.time.Duration
import java.util.UUID

/** Synchronous execution keeps failure evidence and state/validation within the browser's ownership lock. */
internal class UiTestStepProcessor(
    private val state: TestScenarioState,
    timeout: Duration,
    private val createDriver: () -> WebDriver,
    private val screenshotsPath: Path = Path.of("build", "ui-screenshots")
) : TestStepProcessor<UiTestStep> {
    private var browser: WebDriver? = null
    private var closed = false
    private val contextId = UUID.randomUUID().toString()
    private val failures = mutableListOf<UiFailure>()
    private val inputValues = mutableSetOf<String>()
    private val uiActionsHandler = CompositeUiActionHandler(timeout)

    override fun getTestStepClass() = UiTestStep::class.java

    @Synchronized
    override fun process(testStep: UiTestStep): TestStepRunResult {
        val started = System.nanoTime()
        val outcomes = mutableListOf<UiActionOutcome>()
        var stage = UiFailureStage.STATE_UPDATE
        var actionIndex: Int? = null
        var action: UiAction? = null
        var operation = "store-step"
        val result = linkedMapOf<String, Any?>("actions" to outcomes)
        var failure: UiFailure? = null
        inputValues.addAll(testStep.actions.mapNotNull {
            when (it) {
                is UiAction.Type -> it.value
                is UiAction.Select -> it.value
                else -> null
            }
        }.filter { it.isNotEmpty() })
        val secrets = inputValues.sortedByDescending { it.length }
        val redact: (String) -> String = { text -> secrets.fold(text) { value, secret -> value.replace(secret, "<redacted>") } }
        try {
            check(!closed) { "UI processor is closed" }
            state.set(testStep.getId, testStep)
            stage = UiFailureStage.SESSION
            operation = "create-session"
            val driver = browser ?: createDriver().also { browser = it }
            stage = UiFailureStage.ACTION
            testStep.actions.forEachIndexed { index, current ->
                action = current
                actionIndex = index + 1
                operation = current.javaClass.simpleName.lowercase()
                val actionStarted = System.nanoTime()
                try {
                    uiActionsHandler.handle(current, driver)
                    outcomes.add(current.outcome(index + 1, elapsed(actionStarted), true))
                } catch (error: Throwable) {
                    outcomes.add(current.outcome(index + 1, elapsed(actionStarted), false))
                    throw error
                }
            }
            stage = UiFailureStage.OBSERVATION
            action = null
            actionIndex = null
            operation = "current-url"
            result["url"] = driver.currentUrl
            operation = "capture-controls"
            result["pages"] = PageElementsStateCollector.collect(driver, testStep.actions) { target, xpath ->
                action = UiAction.NavigableElement(target, xpath)
            }
            action = null
            stage = UiFailureStage.STATE_UPDATE
            operation = "store-result"
            result["durationMillis"] = elapsed(started)
            state.set(testStep.getId + TestStepProcessor.RESULT_SAVE_SUFFIX, result)
            operation = "update-state"
            testStep.scenarioStateUpdaters?.forEach { it.update(result, state) }
            stage = UiFailureStage.VALIDATION
            operation = "validate"
            testStep.validationDetails?.validate(result)
        } catch (error: Throwable) {
            val secondary = mutableListOf<Throwable>()
            val url = browser?.let { driver ->
                try { driver.currentUrl } catch (ex: Throwable) { secondary.add(ex); null }
            }
            val screenshot = browser?.let { driver ->
                try {
                    captureScreenshot(driver)
                } catch (ex: Throwable) { secondary.add(ex); null }
            }
            val element = action as? UiAction.NavigableElement
            failure = UiFailure(stage, actionIndex, operation, element?.target, element?.xpath,
                elapsed(started), url?.let(redact), screenshot, error, redact)
            secondary.forEach(failure::retainSecondary)
            failures.add(failure)
            result["failure"] = failure
            result["url"] = url
            result["artifacts"] = listOfNotNull(screenshot?.toString())
        }
        result["durationMillis"] = elapsed(started)
        return TestStepRunResult(testStep).also { it.notify(result, failure) }
    }

    @Synchronized
    override fun close() {
        if (closed) return
        closed = true
        try {
            browser?.quit()
        } catch (error: Throwable) {
            if (failures.isEmpty()) throw error
            failures.forEach { it.retainSecondary(error) }
        } finally {
            browser = null
            failures.clear()
            inputValues.clear()
        }
    }

    private fun captureScreenshot(driver: WebDriver): Path {
        val bytes = (driver as TakesScreenshot).getScreenshotAs(OutputType.BYTES)
        val directory = screenshotsPath.toAbsolutePath().normalize()
        Files.createDirectories(directory)
        // No step names or parameter values in filenames. CREATE_NEW prevents overwrites.
        val path = directory.resolve("ui-$contextId-step-${UUID.randomUUID()}.png")
        Files.write(path, bytes, StandardOpenOption.CREATE_NEW)
        return path
    }

    private fun elapsed(started: Long) = (System.nanoTime() - started) / 1_000_000
}
