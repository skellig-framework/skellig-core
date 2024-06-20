package org.skellig.performance.runner.junit

import org.junit.Test
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.runner.RunWith
import org.junit.runner.notification.Failure
import org.junit.runner.notification.RunListener
import org.junit.runner.notification.RunNotifier
import org.skellig.performance.runner.junit.annotation.SkelligPerformanceOptions
import org.skellig.teststep.processor.performance.exception.PerformanceTestStepException


class SkelligPerformanceRunnerTest {

    @Test
    fun `run performance test where after step fails then verify correct error captured`() {
        val runner = SkelligPerformanceRunner(ValidPerformanceRunnerTest::class.java)
        var exception: Throwable? = null
        var originalTestStepName: String? = null
        val notifier = RunNotifier()
        notifier.addListener(object : RunListener() {
            override fun testFailure(failure: Failure?) {
                originalTestStepName = failure?.description?.methodName
                exception = failure?.exception
            }
        })

        runner.run(notifier)

        assertEquals("check performance of storing data in state with failed after step", originalTestStepName)
        assertEquals(PerformanceTestStepException::class.java, exception?.javaClass)
    }

    @Test
    fun `run non existent performance test then check exception is captured`() {
        val runner = SkelligPerformanceRunner(PerformanceRunnerTestWithNonExistingTestStep::class.java)
        var exception: Throwable? = null
        var originalTestStepName: String? = null
        val notifier = RunNotifier()
        notifier.addListener(object : RunListener() {
            override fun testFailure(failure: Failure?) {
                originalTestStepName = failure?.description?.methodName
                exception = failure?.exception
            }
        })

        runner.run(notifier)

        assertEquals("some test step", originalTestStepName)
        assertEquals("Test step 'some test step' is not found in any of registered test data files in resources or classes of the classloader", exception?.message)
    }

    @SkelligPerformanceOptions(
        testName = "check performance of storing data in state with failed after step",
        testSteps = ["steps", "org.skellig.performance"],
        config = "test.conf")
    inner class ValidPerformanceRunnerTest

    @SkelligPerformanceOptions(
        testName = "some test step",
        testSteps = ["steps", "org.skellig.performance"],
        config = "test.conf")
    inner class PerformanceRunnerTestWithNonExistingTestStep
}